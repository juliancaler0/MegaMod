package com.ultra.megamod.feature.combat.rogues.village;

import com.ultra.megamod.feature.combat.village.CombatVillagerRegistry;

/**
 * Rogues & Warriors villager profession references.
 * Ported from net.rogues.village.RogueVillagers.
 *
 * The Arms Merchant profession (POI, profession, and trades) is registered
 * through {@link CombatVillagerRegistry}. This class provides convenience
 * access for rogues-specific code.
 *
 * Arms Merchant trades (5 levels):
 * L1: Buy leather, sell flint dagger + stone double axe
 * L2: Buy iron ingot, sell iron sickle + glaive, rogue/warrior head armor
 * L3: Sell iron dagger + double axe, rogue/warrior leg/feet armor
 * L4: Sell rogue/warrior chest armor
 * L5: Sell diamond-tier daggers, sickles, double axes, glaives
 */
public class RogueVillagers {
    public static final String MERCHANT = "arms_merchant";

    /**
     * Check if the Arms Merchant profession is registered.
     */
    public static boolean isRegistered() {
        return CombatVillagerRegistry.ARMS_MERCHANT != null;
    }
}
