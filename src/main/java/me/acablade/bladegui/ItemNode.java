package me.acablade.bladegui;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

@Data
@Builder
public class ItemNode {

	public static final BiConsumer<GUI, ItemNode> DEFAULT_RENDERER = (gui, node) -> {
		for (int i = node.getLocation().getX(); i < node.getLocation().getX()+node.getWidth(); i++) {
			for (int j = node.getLocation().getY(); j < node.getLocation().getY()+node.getHeight(); j++) {
				gui.getInventory().setItem(j*9+i,node.getItemStack());
			}
		}
	};

	private final ItemStack itemStack;
	private Location location;
	private BiConsumer<GUI, ItemNode> renderer = DEFAULT_RENDERER;
	private BiConsumer<GUI, ItemNode> update;
	private final InventoryClickHandler click;
	private int width;
	private int height;



	public boolean setLocation(GUI gui,Location location){
		if(gui.getItemNode(location).isPresent()) return false;
		this.location = location;
		return true;
	}


	@Data
	public static class Location {
		private int x,y;

		public Location(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public interface InventoryClickHandler{
		InventoryReturnType handle(ItemNode node,InventoryClickEvent event);
	}

	public enum InventoryReturnType{
		CLOSE,CANCEL
	}

}
