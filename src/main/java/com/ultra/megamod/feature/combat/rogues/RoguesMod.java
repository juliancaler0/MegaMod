package com.ultra.megamod.feature.combat.rogues;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.config.TweaksConfig;
import com.ultra.megamod.feature.combat.rogues.item.Group;
import com.ultra.megamod.feature.combat.rogues.item.RogueWeapons;
import com.ultra.megamod.feature.combat.rogues.item.armor.Armors;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rogues & Warriors content module.
 * Ported from the Rogues mod (Fabric) to NeoForge for MegaMod.
 *
 * <p>Weapons (daggers, sickles, glaives, double-axes) and armor sets
 * (rogue/assassin/warrior/berserker + netherite variants) are registered
 * through SpellEngine's Weapon/Armor factory pipeline so they carry
 * {@code SpellContainer} components. Weapon-skill spells (FAN_OF_KNIVES,
 * SWIPE, THRUST, HEAVY_STRIKE) fire on right-click; set-bonus MODIFIER
 * spells on armor feed attribute buffs through the SpellEngine attribute
 * resolver.</p>
 *
 * <p>Legacy {@code ClassWeaponRegistry.XYZ} / {@code ClassArmorRegistry.XYZ}
 * references now resolve via {@code Lookup} shims to the SpellEngine-owned
 * items.</p>
 */
public class RoguesMod {

    private static final Logger LOGGER = LoggerFactory.getLogger("RoguesMod");

    public static final String NAMESPACE = MegaMod.MODID;
    public static final String ID = MegaMod.MODID;

    /** Tweaks configuration - controls stealth behavior. */
    public static final TweaksConfig tweaksConfig = new TweaksConfig();

    /** SpellEngine equipment config (weapons + armor sets). */
    public static final ConfigFile.Equipment itemConfig = new ConfigFile.Equipment();

    private static boolean initialized = false;

    /**
     * Register rogue weapons + armor + creative tab via SpellEngine factories.
     * Called from {@link MegaMod} alongside {@code WizardsMod.registerItems} /
     * {@code PaladinsMod.registerItems} so all SpellEngine-backed items register
     * before {@code RPGItemRegistry.init}.
     */
    public static void registerItems(IEventBus modEventBus) {
        Group.init(modEventBus);
        RogueWeapons.init(modEventBus);
        Armors.init(modEventBus);
    }

    /**
     * Logical init. Keep after {@link #registerItems(IEventBus)} — fired in
     * {@link MegaMod}'s constructor to log readiness.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("[Rogues] Rogues & Warriors module initialized");
    }
}
