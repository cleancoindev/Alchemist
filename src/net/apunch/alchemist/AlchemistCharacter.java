package net.apunch.alchemist;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.apunch.alchemist.util.Settings.Setting;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.util.DataKey;

@SaveId("alchemist")
public class AlchemistCharacter extends Character {
    private Alchemist plugin;
    private String recipe;
    private final Map<String, BrewingSession> sessions = new HashMap<String, BrewingSession>();
    private final Map<String, Calendar> cooldowns = new HashMap<String, Calendar>();;

    public AlchemistCharacter() {
        plugin = (Alchemist) Bukkit.getServer().getPluginManager().getPlugin("Alchemist");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        recipe = key.getString("recipe");
        if (plugin.getRecipe(recipe) == null)
            throw new NPCLoadException("No recipe with the name '" + recipe + "' exists.");
    }

    @Override
    public void onRightClick(NPC npc, Player player) {
        if (!player.hasPermission("alchemist.interact"))
            return;

        if (cooldowns.get(player.getName()) != null) {
            if (!Calendar.getInstance().after(cooldowns.get(player.getName()))) {
                player.sendMessage(ChatColor.RED + "You must wait more time before you can use this alchemist again.");
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