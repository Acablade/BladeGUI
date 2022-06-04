package me.acablade.bladegui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class GUIManager {

	private List<Runnable> runnableList = new ArrayList<>();
	private final JavaPlugin plugin;

	public void register(GUI gui){
		gui.register(this);
	}

}
