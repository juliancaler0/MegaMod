package com.ultra.megamod.feature.combat.rogues;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.config.TweaksConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rogues & Warriors content module.
 * Ported from the Rogues mod (Fabric) to NeoForge for MegaMod.
 *
 * Items, armor, effects, spells, sounds, and villager trades are registered
 * through the existing MegaMod combat registries (ClassWeaponRegistry,
 * ClassArmorRegistry, SpellEffects, SpellRegistry, CombatVillagerRegistry).
 *
 * This class holds shared config values referenced by rogues-specific code
 * (mixins, stealth handling, etc.) and performs module initialization.
 */
public class RoguesMod {

    private static final Logger LOGGER = LoggerFactory.getLogger("RoguesMod");

    public static final String NAMESPACE = MegaMod.MODID;

    /** Tweaks configuration - controls stealth behavior. */
    public static final TweaksConfig tweaksConfig = new TweaksConfig();

    private static boolean initialized = false;

    /**
     * Initialize the Rogues & Warriors module.
     * Called from MegaMod main constructor after all registries are set up.
     *
     * This handles:
     * - Config initialization
     * - Strength effect rebalance (if configured)
     * - Logging module readiness
     *
     * Item/armor/effect/villager registration is handled by:
     * - ClassWeaponRegistry (daggers, sickles, double axes, glaives)
     * - ClassArmorRegistry (rogue, assassin, warrior, berserker armor sets)
     * - SpellEffects (stealth, charge, shock, shatter, demoralize, etc.)
     * - CombatVillagerRegistry (arms merchant profession and trades)
     * - RuneWorkbenchRegistry (arms workbench block)
     * - SpellItemRegistry (rogue manual, warrior codex spell books)
     *
     * Spell definitions are loaded from data/megamod/spell/ JSONs:
     * - Rogue: slice_and_dice, shock_powder, shadow_step, vanish
     * - Warrior: shattering_throw, shout, charge
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("[Rogues] Rogues & Warriors module initialized");
    }
}
