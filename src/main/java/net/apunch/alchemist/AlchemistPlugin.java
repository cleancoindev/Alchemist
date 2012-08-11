package net.apunch.alchemist;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.apunch.alchemist.util.Settings;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class AlchemistPlugin extends JavaPlugin {
	private Settings settings;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be in-game to execute commands.");
			return true;
		}
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Incorrect usage. /alchemist help");
			return true;
		}

		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase("help")) {
			player.sendMessage(ChatColor.GOLD + "----- Alchemist Help -----");
			player.sendMessage(ChatColor.GREEN + "/alchemist recipe" + ChatColor.GRAY + " -- Shows current recipe");
			player.sendMessage(ChatColor.GREEN + "/alchemist recipe [name]" + ChatColor.GRAY
					+ " -- Changes current recipe");
			player.sendMessage(ChatColor.GREEN + "/alchemist recipes" + ChatColor.GRAY + " -- Lists available recipes");
			return true;
		}
		NPC npc = null;
		if (player.getMetadata("selected").size() > 0)
			npc = CitizensAPI.getNPCRegistry().getById((player.getMetadata("selected").get(0).asInt()));
		if (npc == null) {
			player.sendMessage(ChatColor.RED + "You must have an alchemist selected.");
			return true;
		}
		if (!npc.getTrait(Owner.class).getOwner().equals(player.getName())) {
			player.sendMessage(ChatColor.RED + "You must be the owner of the alchemist to execute commands.");
			return true;
		}
		if (!npc.hasTrait(AlchemistTrait.class)) {
			player.sendMessage(ChatColor.RED + "That command must be performed on an alchemist!");
			return true;
		}
		AlchemistTrait alchemist = (AlchemistTrait) npc.getTrait(AlchemistTrait.class);
		if (args[0].equalsIgnoreCase("recipe")) {
			if (args.length == 1) {
				player.sendMessage(ChatColor.GREEN + npc.getName() + "'s" + " Recipe: " + ChatColor.GOLD
						+ alchemist.getRecipe());
				return true;
			}
			if (!settings.getConfig().getKey("recipes").keyExists(args[1])) {
				player.sendMessage(ChatColor.RED + "The recipe '" + args[1] + "' does not exist.");
				return true;
			}
			alchemist.setRecipe(args[1]);
			player.sendMessage(ChatColor.GOLD + npc.getName() + "'s " + ChatColor.GREEN + "recipe is now "
					+ ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
		} else if (args[0].equalsIgnoreCase("recipes")) {
			player.sendMessage(ChatColor.GOLD + "----- Available Recipes -----");
			for (DataKey key : settings.getConfig().getKey("recipes").getSubKeys())
				player.sendMessage(ChatColor.GRAY + "  - " + ChatColor.GREEN + key.name());
		}
		return true;
	}

	@Override
	public void onDisable() {
		settings.save();

		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		settings = new Settings(this);
		settings.load();

		writeDefaultRecipe();

		CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(AlchemistTrait.class).withName("alchemist"));


		getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " enabled.");
	}

	public PotionRecipe getRecipe(String name) throws NPCLoadException {
		DataKey root = settings.getConfig().getKey("recipes." + name);
		try {
			ItemStack[] ingredients = new ItemStack[36];
			for (DataKey id : root.getRelative("ingredients").getIntegerSubKeys())
				ingredients[Integer.parseInt(id.name())] = getIngredient(id);

			return new PotionRecipe(name, PotionEffectType.getByName(root.getString("result.effect").toUpperCase()
					.replace('-', '_')), root.getInt("result.duration") * 20, root.getInt("result.amplifier"), root
					.getBoolean("result.splash"), ingredients);
		} catch (Exception ex) {
			throw new NPCLoadException("Invalid configuration for the recipe '" + name + "'. " + ex.getMessage());
		}
	}

	private ItemStack getIngredient(DataKey key) throws NPCLoadException {
		try {
			short data = 0;
			if (key.keyExists("data"))
				data = (short) key.getInt("data");
			ItemStack item = new ItemStack(Material.getMaterial(key.getString("name").toUpperCase().replace('-', '_')),
					key.getInt("amount"), data);
			if (key.keyExists("enchantments")) {
				Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
				for (DataKey subKey : key.getRelative("enchantments").getSubKeys()) {
					Enchantment enchantment = Enchantment.getByName(subKey.name().toUpperCase().replace('-', '_'));
					if (enchantment != null && enchantment.canEnchantItem(item))
						enchantments.put(enchantment, subKey.getInt("") <= enchantment.getMaxLevel() ? subKey
								.getInt("") : enchantment.getMaxLevel());
				}
				item.addEnchantments(enchantments);
			}
			return item;
		} catch (Exception ex) {
			throw new NPCLoadException("Invalid item. " + ex.getMessage());
		}
	}

	private void writeDefaultRecipe() {
		DataKey root = settings.getConfig().getKey("");
		if (!root.keyExists("recipes.default")) {
			DataKey def = root.getRelative("recipes.default");
			def.setString("result.effect", "fire-resistance");
			def.setInt("result.duration", 60);
			def.setInt("result.amplifier", 1);
			def.setBoolean("result.splash", false);
			def.setString("ingredients.0.name", "sugar");
			def.setInt("ingredients.0.amount", 1);
			def.setString("ingredients.1.name", "glass-bottle");
			def.setInt("ingredients.1.amount", 1);
			def.setString("ingredients.2.name", "wool");
			def.setInt("ingredients.2.amount", 1);
			def.setInt("ingredients.2.data", 6);
		}
	}
}