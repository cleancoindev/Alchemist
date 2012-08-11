package net.apunch.alchemist;

import net.apunch.alchemist.util.Settings.Setting;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.potion.Potion;

public class BrewingSession {
    private final Player player;
    private final NPC npc;
    private PotionRecipe recipe;

    public BrewingSession(Player player, NPC npc, PotionRecipe recipe) {
        this.player = player;
        this.npc = npc;
        this.recipe = recipe;
        player.sendMessage(Setting.INIT_MESSAGE.asString());
    }

    // Return whether the session should end
    public boolean handleClick() {
        if (recipe.hasIngredient(player.getItemInHand())) {
            if (!recipe.removeIngredientFromHand(player)) {
                player.sendMessage( "<e>I will need more of that item!");
                return false;
            }
            if (recipe.isComplete()) {
                applyResult();
                player.sendMessage((Setting.SUCCESS_MESSAGE.asString().replace("<potion>",         Potion.fromItemStack(recipe.getResult()).getType().name().toLowerCase().replace('_', ' '))));
                return true;
            }
            player.sendMessage( "<e>The recipe is not complete yet! Give me more!");
        } else
            player.sendMessage( "<c>Why would I want that? Give me something else!");
        return false;
    }

    private void applyResult() {
        player.playEffect(player.getLocation(), Effect.POTION_BREAK, 0);
        player.getWorld().dropItemNaturally(npc.getBukkitEntity().getLocation(), recipe.getResult());
    }
}