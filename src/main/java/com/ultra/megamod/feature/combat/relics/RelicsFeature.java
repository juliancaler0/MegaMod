package com.ultra.megamod.feature.combat.relics;

import com.ultra.megamod.feature.combat.relics.compat.AccessoriesHelper;
import com.ultra.megamod.feature.combat.relics.config.ItemConfig;
import com.ultra.megamod.feature.combat.relics.item.RelicItems;
import com.ultra.megamod.feature.combat.relics.spell.RelicEffects;
import com.ultra.megamod.feature.combat.relics.spell.RelicMechanics;
import com.ultra.megamod.feature.combat.relics.spell.RelicSounds;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import net.neoforged.bus.api.IEventBus;

/**
 * Glue class combining Relics-1.21.1's RelicsMod entry points.
 * Called from MegaMod at mod-load; orchestrates DeferredRegister binding,
 * accessories factory swap, spell-handler event registration, and config seeding.
 */
public final class RelicsFeature {

    private RelicsFeature() {}

    public static final ItemConfig itemConfig = new ItemConfig();
    public static final ConfigFile.Effects effectConfig = new ConfigFile.Effects();

    /** Invoked from MegaMod(IEventBus) — must run BEFORE DeferredRegister.register. */
    public static void initEarly() {
        // Route relic items into the accessories charm slot.
        AccessoriesHelper.registerFactory();
    }

    /** Invoked from MegaMod(IEventBus) — after initEarly; binds DeferredRegister + mechanics. */
    public static void init(IEventBus modEventBus) {
        RelicItems.init(modEventBus);
        RelicSounds.init(modEventBus);

        // Seed config entries so Item.Properties see their attribute values on first access.
        RelicItems.register(itemConfig.entries);

        // Register custom spell-impact handlers (shield_reset etc.)
        RelicMechanics.init();

        // Register effects into the spell_engine effects map.
        RelicEffects.register(effectConfig);
    }
}
