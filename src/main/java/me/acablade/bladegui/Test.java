package me.acablade.bladegui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Test extends JavaPlugin implements Listener {

	private GUI gui;

	private final ItemStack[] materials = Arrays.stream(Material.values())
			.filter(material -> material.name().contains("STAINED_GLASS_PANE"))
			.limit(6)
			.map(ItemStack::new)
			.collect(Collectors.toList())
			.toArray(new ItemStack[0]);
	public int turn = 0;

	@Override
	public void onEnable() {
		gui = new GUI("&ctest", 45);
		System.out.println(materials.length);
		ItemNode itemNode = ItemNode.builder()
						.itemStack(new ItemStack(Material.STONE))
						.location(new ItemNode.Location(3,1))
						.click((node, e) -> {
							int middleX = (int) Math.ceil((node.getLocation().getX() + node.getWidth() - 1) / 2.0);
							int middleY = (int) Math.ceil((node.getLocation().getY() + node.getHeight() - 1) / 2.0);

							int middleSlot = middleY*9+middleX;

							if(e.getSlot()==middleSlot) return ItemNode.InventoryReturnType.CANCEL;

							e.getWhoClicked().sendMessage("test 1-2");
							return ItemNode.InventoryReturnType.CANCEL;
						})
						.update((gui,node) -> turn++)
						.renderer((gui, node) -> {

							ItemStack array2d[][] = printSpiralOrder(ItemStack.class,materials,node.getHeight(),node.getWidth(),turn);

							for (int x = node.getLocation().getX(); x < node.getLocation().getX() + node.getWidth(); x++) {
								for (int y = node.getLocation().getY(); y < node.getLocation().getY() + node.getHeight(); y++) {
									int slot = y*9 + x;
									gui.getInventory().setItem(slot,array2d[y-node.getLocation().getY()][x-node.getLocation().getX()]);
								}
							}
						})
						.width(3)
						.height(3)
						.build();
		System.out.println(gui.addItemNode(itemNode));
		gui.register(this,5);
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	private int[] calculateCoords(int index, int columns, int rows){
		int[] currentCoords = {0,0};

		char face = 'n';

		int x = currentCoords[1];
		int y = currentCoords[0];

		if(y==0) face = 'r';
		else if(y==rows-1) face = 'l';
		else if(x==columns-1) face = 'd';
		else if(x==0) face = 'u';

		for (int i = 0; i < index; i++) {
			x = currentCoords[1];
			y = currentCoords[0];

			if(y==0&&x==columns-1) face = 'd';
			else if(y==rows-1&&x==columns-1) face = 'l';
			else if(y==rows-1&&x==0) face = 'u';
			else if(y==0&&x==0) face = 'r';

			switch (face){
				case 'r':
					currentCoords[1]=x+1;
					break;
				case 'l':
					currentCoords[1]=x-1;
					break;
				case 'u':
					currentCoords[0]=y-1;
					break;
				case 'd':
					currentCoords[0]=y+1;
					break;
			}
		}
		return currentCoords;
	}

	private <T> T[][] printSpiralOrder(Class<T> tClass,T[] arr, int rows, int columns, int turn)
	{
		if (arr == null) {
			return null;
		}

		T[][] mat = (T[][])Array.newInstance(tClass, rows, columns);

		int[] currentCoords = calculateCoords(turn,columns,rows);

		// r,l,u,d

		char face = 'r';

		int x = currentCoords[1];
		int y = currentCoords[0];

		if(y==0) face = 'r';
		else if(y==rows-1) face = 'l';
		else if(x==columns-1) face = 'd';
		else if(x==0) face = 'u';

		for (int i = 0; i < arr.length; i++) {

			x = currentCoords[1];
			y = currentCoords[0];

			if(y==0&&x==columns-1) face = 'd';
			else if(y==rows-1&&x==columns-1) face = 'l';
			else if(y==rows-1&&x==0) face = 'u';
			else if(y==0&&x==0) face = 'r';

			switch (face){
				case 'r':
					currentCoords[1]=x+1;
					break;
				case 'l':
					currentCoords[1]=x-1;
					break;
				case 'u':
					currentCoords[0]=y-1;
					break;
				case 'd':
					currentCoords[0]=y+1;
					break;
			}
			mat[currentCoords[0]][currentCoords[1]]=arr[i%arr.length];
		}

		return mat;

	}


	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		Bukkit.getScheduler().runTask(this, () -> gui.open(event.getPlayer()));

	}
}
