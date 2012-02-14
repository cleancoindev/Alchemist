package net.apunch.alchemist;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.util.DataKey;

@SaveId("alchemist")
public class AlchemistCharacter extends Character {
    private Alchemist plugin;
    private String recipe;
    private BrewingSession session;

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
        if (session != null) {
            if (!session.getPlayer().getName().equals(player.getName())) {
                player.sendMessage(ChatColor.RED + npc.getName() + " is busy with another player. Come back later!");
                return;
            }
            if (session.handleClick())
                session = null;
        } else {
            try {
                session = new BrewingSession(player, npc, plugin.getRecipe(recipe));
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
        session = null;
    }
}