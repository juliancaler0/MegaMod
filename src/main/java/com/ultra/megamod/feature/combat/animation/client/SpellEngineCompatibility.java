package com.ultra.megamod.feature.combat.animation.client;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/**
 * Compatibility check for SpellEngine mod to prevent attacking while casting.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.compat.SpellEngineCompatibility).
 */
public class SpellEngineCompatibility {
    private static Boolean isLoaded = null;

    public static boolean isCastingSpell(Player player) {
        if (isLoaded == null) {
            isLoaded = ModList.get().isLoaded("spell_engine");
        }
        if (isLoaded) {
            try {
                // SpellEngine not present in MegaMod — always return false
                // If SpellEngine is ever integrated, add the cast check here
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
