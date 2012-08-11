package net.apunch.alchemist;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.apunch.alchemist.util.Settings.Setting;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class AlchemistTrait extends Trait {
	private AlchemistPlugin plugin;
	private String recipe = "default";
	private final Map<String, BrewingSession> sessions = new HashMap<String, BrewingSession>();
	private final Map<String, Calendar> cooldowns = new HashMap<String, Calendar>();

	public AlchemistTrait() {
		super("alchemist");
		plugin = (AlchemistPlugin) Bukkit.getServer().getPluginManager().getPlugin("Alchemist");
	}

	@Override
	public void load(DataKey key) throws NPCLoadException {
		if (plugin.getRecipe(key.getString("recipe")) != null)
			recipe = key.getString("recipe");
	}

	@EventHandler
	public void onRightClick(NPCRightClickEvent event) {
		if(this.npc!=event.getNPC()) return;
		
		
		Player player = event.getClicker();
		if (!player.hasPermission("alchemist.interact"))
			return;

		if (cooldowns.get(player.getName()) != null) {
			if (!Calendar.getInstance().after(cooldowns.get(player.getName()))) {
				player.sendMessage( Setting.COOLDOWN_UNEXPIRED_MESSAGE.asString());
				return;
			}
			cooldowns.remove(player.getName());
		}

		BrewingSession session = sessions.get(player.getName());
		if (session != null) {
			if (session.handleClick()) {
				sessions.remove(player.getName());
				Calendar wait = Calendar.getInstance();
				wait.add(Calendar.SECOND, Setting.COOLDOWN.asInt());
				cooldowns.put(player.getName(), wait);
			}
		} else {
			try {
				sessions.put(player.getName(), new BrewingSession(player, npc, plugin.getRecipe(recipe)));
			} catch (NPCLoadException ex) {
				plugin.getLogger().log(Level.SEVERE, "Invalid recipe. " + ex.getMessage());
			}
		}
	}

	@Override
	public void save(DataKey key) {
		key.setString("recipe", recipe);
	}

	public String getRecipe() {
		return recipe;
	}

	public void setRecipe(String recipe) {
		this.recipe = recipe;
		sessions.clear();
	}
}