package com.ultra.megamod.lib.spellengine;

import com.ultra.megamod.lib.spellengine.api.effect.RemoveOnHit;
import com.ultra.megamod.lib.spellengine.api.effect.StatusEffectClassification;
import com.ultra.megamod.lib.spellengine.api.item.set.EquipmentSetFeature;
import com.ultra.megamod.lib.spellengine.api.spell.ExternalSpellSchools;
import com.ultra.megamod.lib.spellengine.api.spell.event.SpellEvents;
import com.ultra.megamod.lib.spellengine.api.spell.weakness.SpellSchoolWeakness;
import com.ultra.megamod.lib.spellengine.compat.CompatFeatures;
import com.ultra.megamod.lib.spellengine.config.FallbackConfig;
import com.ultra.megamod.lib.spellengine.config.ServerConfig;
import com.ultra.megamod.lib.spellengine.config.WeaknessConfig;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;
import com.ultra.megamod.lib.spellengine.internals.container.SpellAssignments;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;
import com.ultra.megamod.lib.spellengine.internals.delivery.SpellStashHelper;
import com.ultra.megamod.lib.spellengine.rpg_series.RPGSeriesCore;
import com.ultra.megamod.lib.spellengine.utils.StatusEffectUtil;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

/**
 * SpellEngine library entry point, ported from Fabric SpellEngine mod.
 * Initialization is called from MegaMod main class.
 */
public class SpellEngineMod {
    public static final String ID = "megamod";

    public static ServerConfig config = new ServerConfig();

    public static WeaknessConfig weaknessConfig = SpellSchoolWeakness.createDefault();
    public static FallbackConfig fallbackConfig = FallbackConfig.defaults();

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        SpellAssignments.init();

        SpellEvents.SPELL_CAST.register(args -> {
            // Criteria triggers can be added here
        });

        ExternalSpellSchools.init();
        RPGSeriesCore.init();
        SpellStashHelper.init();
        SpellTriggers.init();
        SpellContainerSource.init();
        StatusEffectClassification.init();
        EquipmentSetFeature.init();
        CompatFeatures.initialize();
    }
}
