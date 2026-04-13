package com.ultra.megamod.feature.combat.arsenal;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.arsenal.config.RangedConfigFile;
import com.ultra.megamod.feature.combat.arsenal.item.ArsenalBows;
import com.ultra.megamod.feature.combat.arsenal.item.ArsenalShields;
import com.ultra.megamod.feature.combat.arsenal.item.ArsenalWeapons;
import com.ultra.megamod.feature.combat.arsenal.item.Group;
import com.ultra.megamod.feature.combat.arsenal.spell.ArsenalEffects;
import com.ultra.megamod.feature.combat.arsenal.spell.ArsenalSounds;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import net.neoforged.bus.api.IEventBus;

/**
 * Arsenal content mod - 40+ epic weapons with passive spell-on-hit effects.
 * Ported 1:1 from Arsenal mod for Fabric.
 */
public class ArsenalMod {
    public static final String NAMESPACE = MegaMod.MODID;

    // Static config instances (no tiny_config on NeoForge)
    public static final ConfigFile.Equipment itemConfig = new ConfigFile.Equipment();
    public static final RangedConfigFile rangedConfig = new RangedConfigFile();
    public static final ConfigFile.Shields shieldConfig = new ConfigFile.Shields();
    public static final ConfigFile.Effects effectConfig = new ConfigFile.Effects();

    /**
     * Initialize all Arsenal systems. Call from MegaMod constructor.
     */
    public static void init(IEventBus modEventBus) {
        ArsenalSounds.init(modEventBus);
        ArsenalEffects.register(effectConfig);
        Group.init(modEventBus);
        // ArsenalWeapons, ArsenalBows, ArsenalShields removed —
        // all unique_* items are registered by RelicRegistry
    }
}
