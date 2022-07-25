package me.acablade.bladegui;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Data
public class GUI implements Runnable, Listener, InventoryHolder {

	private final Set<ItemNode> itemNodeSet = new HashSet<>();
	private final Set<UUID> viewers = new HashSet<>();

	private final Inventory inventory;

	private ItemNode.InventoryReturnType defaultReturn = ItemNode.InventoryReturnType.CANCEL;

	private boolean registered;

	private final int size;

	public GUI(String title, int size){
		this.size = (int) Math.ceil(size/9.0)*9;
		this.inventory = Bukkit.createInventory(
				this,
				this.size,
				ChatColor.translateAlternateColorCodes('&',title)
		);
	}

	public void setDefaultReturn(ItemNode.InventoryReturnType defaultReturn) {
		this.defaultReturn = defaultReturn;
	}

	public void register(JavaPlugin plugin, long updateTick){
		this.registered = true;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		if(updateTick > 0){
			Bukkit.getScheduler().runTaskTimer(plugin,this,0,updateTick);
		}
	}


	public Optional<ItemNode> getItemNode(ItemNode.Location location){
		return getItemNode(location.getX(), location.getY());
	}


	public Optional<ItemNode> getItemNode(int x, int y){
		for(ItemNode node: itemNodeSet){

			ItemNode.Location location = node.getLocation();

			if(location.getX() == x && location.getY() == y) return Optional.of(node);

			int maxX = location.getX()+node.getWidth()-1;
			int maxY = location.getY()+node.getHeight()-1;

			boolean isInsideX = x >= location.getX() && x <= maxX;
			boolean isInsideY = y >= location.getY() && y <= maxY;

			if(isInsideX && isInsideY) return Optional.of(node);
		}
		return Optional.empty();
	}

	public boolean addItemNode(ItemNode itemNode){
		if(getItemNode(itemNode.getLocation()).isPresent()) return false;
		int[] coords = toCoordinate(size-1);
		if(itemNode.getLocation().getX()+(itemNode.getWidth()-1)>coords[0] || itemNode.getLocation().getY()+(itemNode.getHeight()-1)>coords[1]) return false;
		itemNodeSet.add(itemNode);
		return true;
	}

	@Override
	public void run() {
		this.getInventory().clear();
		for(ItemNode itemNode : itemNodeSet) {
			if (itemNode.getRenderer() != null) itemNode.getRenderer().accept(this,itemNode);
			if (itemNode.getUpdate() != null) itemNode.getUpdate().accept(this,itemNode);
		}
	}

	private int[] toCoordinate(int slot){
		int y = slot/9;
		int x = slot-y*9;
		return new int[]{x, y};
	}

	public int toSlot(int x, int y){
		return y*9+x;
	}

	public void open(Player player){
		player.closeInventory();
		player.openInventory(getInventory());
	}

	@EventHandler
	public void onClick(InventoryClickEvent event){
		if(event.getClickedInventory()!=event.getView().getTopInventory()) return;
		if(!this.viewers.contains(event.getWhoClicked().getUniqueId())) return;
		if(event.isShiftClick()) {
			event.setResult(Event.Result.DENY);
			return;
		}
		int slot = event.getSlot();
		int[] coords = toCoordinate(slot);
		Optional<ItemNode> itemNodeOptional = getItemNode(coords[0],coords[1]);
		ItemNode.InventoryReturnType returnType = defaultReturn;
		if(!itemNodeOptional.isPresent()){
			switch (returnType){
				case CLOSE:
					event.getWhoClicked().closeInventory();
				case CANCEL:
					event.setResult(Event.Result.DENY);
			}
			return;
		}
		ItemNode node = itemNodeOptional.get();
		returnType = node.getClick().handle(node,event);
		switch (returnType){
			case CLOSE:
				event.getWhoClicked().closeInventory();
			case CANCEL:
				event.setResult(Event.Result.DENY);
		}

	}

	@EventHandler
	public void onDrag(InventoryDragEvent event){
		InventoryView view = event.getView();
		Inventory top = view.getTopInventory();

		// We are only processing drags that affect menus
		if (top.getHolder() instanceof GUI) {
			event.setResult(Event.Result.DENY);
		}
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent event){
		if(event.getInventory()!=this.inventory) return;
		viewers.add(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event){
		if(event.getInventory()!=this.inventory) return;
		viewers.remove(event.getPlayer().getUniqueId());
	}
}
