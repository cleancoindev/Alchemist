package net.apunch.alchemist;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionRecipe {
    private final String name;
    private PotionEffect result;
    public Set<ItemStack> ingredients;

    public PotionRecipe(String name, PotionEffectType type, int duration, int amplifier, ItemStack... required) {
        this.name = name;
        result = new PotionEffect(type, duration, amplifier);
        ingredients = new HashSet<ItemStack>(Arrays.asList(required));
    }

    public String getName() {
        return name;
    }

    public PotionEffect getResult() {
        return result;
    }

    public boolean hasIngredient(ItemStack ingredient) {
        for (ItemStack stack : ingredients)
            if (stack != null && stack.getTypeId() == ingredient.getTypeId()
                    && stack.getDurability() == ingredient.getDurability()
                    && (stack.getEnchantments().equals(ingredient.getEnchantments())))
                return true;
        return false;
    }

    // Returns whether the player had enough of the item
    public boolean removeIngredientFromHand(Player player) {
        ItemStack hand = player.getItemInHand();
        // Try to remove entire stack
        if (ingredients.contains(hand)) {
            ingredients.remove(hand);
            player.setItemInHand(null);
            return true;
        }
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null && ingredient.getTypeId() == hand.getTypeId()
                    && ingredient.getDurability() == hand.getDurability()
                    && ingredient.getEnchantments().equals(hand.getEnchantments())) {
                ingredients.remove(ingredient);
                if (hand.getAmount() - ingredient.getAmount() > 0)
                    hand.setAmount(hand.getAmount() - ingredient.getAmount());
                else {
                    hand.setAmount(ingredient.getAmount() - hand.getAmount());
                    ingredients.add(hand);
                    player.setItemInHand(null);
                    return false;
                }
                hand.setAmount(hand.getAmount() - ingredient.getAmount() > 0 ? hand.getAmount() : 0);
                player.setItemInHand(hand);
                return true;
            }
        }
        return false;
    }

    public boolean isComplete() {
        return ingredients.size() == 1;
    }
}