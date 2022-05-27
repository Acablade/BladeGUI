package me.acablade.bladegui;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class GUI implements Listener {

	private final Inventory inventory;
	private final long updateTick;

	private BukkitTask task;

	private final Map<Integer, BladeItem> itemStackMap;

	private UpdateGUI update;

	private Set<Player> viewers;

	public GUI(String name, int size, long updateTick){

		this.inventory = Bukkit.createInventory(null,clamp(round(size),9,54), ChatColor.translateAlternateColorCodes('&',name));
		this.updateTick = updateTick;
		this.itemStackMap = new HashMap<>();
		this.viewers = new HashSet<>();

	}


	public void register(GUIManager manager){
		Bukkit.getPluginManager().registerEvents(this,manager.getPlugin());
		if(this.updateTick<=0) return;
		if(this.task!=null) return;
		this.task = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), () -> {
			update.update(this);
		},0,updateTick);
	}

	public void addItem(ItemStack itemStack, InventoryClickHandler clickHandler){
		this.addItem(new BladeItem(itemStack,clickHandler));
	}

	public void addItem(BladeItem bladeItem){
		this.setItem(inventory.firstEmpty(),bladeItem);

	}

	public void setItem(int slot, ItemStack itemStack, InventoryClickHandler clickHandler){
		this.setItem(slot, new BladeItem(itemStack,clickHandler));
	}

	public void setItem(int slot, BladeItem bladeItem){
		itemStackMap.put(slot,bladeItem);
		inventory.setItem(slot, bladeItem.getItemStack());
	}

	private int clamp(int value, int min, int max){
		return Math.min(Math.max(min,value),max);
	}

	private int round(int value){
		return (int) Math.ceil(value/9.0)*9;
	}


	@EventHandler
	public void onClick(InventoryClickEvent event){
		if(event.getClickedInventory()!=event.getView().getTopInventory()) return;
		if(!this.viewers.contains((Player)event.getWhoClicked())) return;
		int slot = event.getSlot();
		if(!itemStackMap.containsKey(slot)) return;
		InventoryReturnType returnType = itemStackMap.get(slot).clickHandler.handle(event);
		switch (returnType){
			case CLOSE:
				event.getWhoClicked().closeInventory();
			case CANCEL:
				event.setCancelled(true);
		}

	}

	@EventHandler
	public void onOpen(InventoryOpenEvent event){
		if(event.getInventory()!=this.inventory) return;
		viewers.add((Player)event.getPlayer());
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event){
		if(event.getInventory()!=this.inventory) return;
		viewers.remove((Player)event.getPlayer());
	}

	@Data
	public class BladeItem{

		private final ItemStack itemStack;
		private final InventoryClickHandler clickHandler;

	}

	public interface InventoryClickHandler{
		InventoryReturnType handle(InventoryClickEvent event);
	}

	public enum InventoryReturnType{
		CLOSE,CANCEL
	}

}
