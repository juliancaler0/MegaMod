package com.ultra.megamod.feature.combat.wizards;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.wizards.content.WizardSounds;
import net.neoforged.bus.api.IEventBus;

/**
 * Wizards class content initialization.
 * Registers wizard-specific sounds (effects, items, and armor already registered
 * in SpellEffects, ClassWeaponRegistry, and ClassArmorRegistry respectively).
 * Spell definitions are in SpellRegistry. Client renderers in WizardsClientInit.
 *
 * Ported from Wizards mod (Fabric) to NeoForge 1.21.11.
 */
public class WizardsMod {
    public static final String ID = MegaMod.MODID;

    /**
     * Initialize wizard systems. Call from MegaMod constructor.
     */
    public static void init(IEventBus modEventBus) {
        WizardSounds.init(modEventBus);
    }
}
