package com.ultra.megamod.feature.recipes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Auto-unlocks all MegaMod crafting recipes in the recipe book on player login.
 * Special backpack variants were previously gated behind skill tree nodes.
 * TODO: Reconnect with Pufferfish Skills API for backpack recipe gating.
 */
@EventBusSubscriber(modid = "megamod")
public class RecipeUnlocker {

    /** Backpack variant IDs that previously required Explorer/Navigator T3+ to see in recipe book */
    private static final Set<String> LOCKED_BACKPACK_IDS = Set.of(
            "netherite_backpack", "diamond_backpack", "gold_backpack", "iron_backpack",
            "emerald_backpack", "lapis_backpack", "redstone_backpack", "coal_backpack",
            "quartz_backpack", "bookshelf_backpack", "sandstone_backpack", "snow_backpack",
            "sponge_backpack", "cake_backpack", "cactus_backpack", "hay_backpack",
            "melon_backpack", "pumpkin_backpack", "creeper_backpack", "dragon_backpack",
            "enderman_backpack", "blaze_backpack", "ghast_backpack", "magma_cube_backpack",
            "skeleton_backpack", "spider_backpack", "wither_backpack", "warden_backpack",
            "bat_backpack", "bee_backpack", "wolf_backpack", "fox_backpack",
            "ocelot_backpack", "horse_backpack", "cow_backpack", "pig_backpack",
            "sheep_backpack", "chicken_backpack", "squid_backpack", "villager_backpack",
            "iron_golem_backpack", "jukebox_backpack"
    );

    /** Dye variant prefixes that also require the skill */
    private static final String DYE_BACKPACK_PREFIX = "dye_";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        awardRecipes(sp);
    }

    /**
     * Awards all megamod recipes to the player.
     * Backpack skill gating removed (old skill system deleted).
     * All backpack recipes are now unlocked for everyone.
     */
    public static void awardRecipes(ServerPlayer sp) {
        var recipeManager = sp.level().getServer().getRecipeManager();
        // TODO: Reconnect with Pufferfish Skills API (was SkillManager/SkillBranch gating)
        boolean hasBackpackSkill = true; // stub: always unlocked

        List<RecipeHolder<?>> toAward = new ArrayList<>();

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            String namespace = holder.id().identifier().getNamespace();
            if (!namespace.equals("megamod")) continue;

            String path = holder.id().identifier().getPath();

            // Check if this is a locked backpack recipe
            if (isLockedBackpackRecipe(path) && !hasBackpackSkill) {
                continue; // Skip — player hasn't unlocked this yet
            }

            toAward.add(holder);
        }

        if (!toAward.isEmpty()) {
            sp.awardRecipes(toAward);
        }
    }

    /**
     * Called when a player unlocks a skill node — previously awarded backpack recipes
     * when Explorer/Navigator T3+ was gained.
     * TODO: Reconnect with Pufferfish Skills API
     */
    public static void onSkillUnlocked(ServerPlayer sp, String nodeId) {
        // Stub: old skill system removed
    }

    private static boolean isLockedBackpackRecipe(String path) {
        if (LOCKED_BACKPACK_IDS.contains(path)) return true;
        // Dye variants: dye_bee_backpack, dye_creeper_backpack, etc.
        if (path.startsWith(DYE_BACKPACK_PREFIX) && path.endsWith("_backpack")) return true;
        return false;
    }
}
