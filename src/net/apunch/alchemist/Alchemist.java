package net.apunch.alchemist;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.YamlStorage;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class Alchemist extends JavaPlugin {
    private static YamlStorage config;

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        config = new YamlStorage(getDataFolder() + File.separator + "config.yml", "Alchemist NPCs Configuration");
        CitizensAPI.getCharacterManager().register(AlchemistCharacter.class);

        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " enabled.");
    }

    public static PotionRecipe getRecipe(String name) throws NPCLoadException {
        DataKey root = config.getKey("recipes." + name);
        try {
            ItemStack[] ingredients = new ItemStack[36];
            for (DataKey id : root.getRelative("ingredients").getIntegerSubKeys())
                ingredients[Integer.parseInt(id.name())] = getIngredient(id);

            return new PotionRecipe(name, PotionEffectType.getByName(root.getString("result.effect").toUpperCase()
                    .replace('-', '_')), root.getInt("result.duration") * 20, root.getInt("result.amplifier"),
                    ingredients);
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid configuration for the recipe '" + name + "'. " + ex.getMessage());
        }
    }

    private static ItemStack getIngredient(DataKey key) throws NPCLoadException {
        try {
            ItemStack item = new ItemStack(Material.getMaterial(key.getString("name").toUpperCase().replace('-', '_')),
                    key.getInt("amount"), (short) key.getLong("data"));
            if (key.keyExists("enchantments")) {
                Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
                for (DataKey subKey : key.getRelative("enchantments").getSubKeys()) {
                    Enchantment enchantment = Enchantment.getByName(subKey.name().toUpperCase().replace('-', '_'));
                    if (enchantment != null && enchantment.canEnchantItem(item))
                        enchantments.put(
                                enchantment,
                                subKey.getInt("") <= enchantment.getMaxLevel() ? subKey.getInt("") : enchantment
                                        .getMaxLevel());
                }
                item.addEnchantments(enchantments);
            }
            return item;
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid item. " + ex.getMessage());
        }
    }
}