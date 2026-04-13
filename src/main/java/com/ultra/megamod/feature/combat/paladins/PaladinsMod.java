package com.ultra.megamod.feature.combat.paladins;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.block.PaladinBlocks;
import com.ultra.megamod.feature.combat.paladins.config.Default;
import com.ultra.megamod.feature.combat.paladins.config.TweaksConfig;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSounds;
import com.ultra.megamod.feature.combat.paladins.effect.PaladinEffects;
import com.ultra.megamod.feature.combat.paladins.entity.PaladinEntities;
import com.ultra.megamod.feature.combat.paladins.item.Group;
import com.ultra.megamod.feature.combat.paladins.item.PaladinBooks;
import com.ultra.megamod.feature.combat.paladins.item.PaladinShields;
import com.ultra.megamod.feature.combat.paladins.item.PaladinWeapons;
import com.ultra.megamod.feature.combat.paladins.item.armor.Armors;
import com.ultra.megamod.feature.combat.paladins.village.PaladinVillagers;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import net.neoforged.bus.api.IEventBus;

/**
 * Paladins & Priests class content - ported from Paladins mod.
 * Adds paladin/priest spells (healing, holy damage, divine protection),
 * holy weapons, armor sets, and related content.
 */
public class PaladinsMod {
    public static final String ID = MegaMod.MODID;

    // Configs - using static defaults instead of tiny_config ConfigManager
    public static final ConfigFile.Equipment itemConfig = Default.itemConfig;
    public static final ConfigFile.Shields shieldConfig = new ConfigFile.Shields();
    public static final ConfigFile.Effects effectsConfig = new ConfigFile.Effects();
    public static final TweaksConfig tweaksConfig = new TweaksConfig();

    /**
     * Initialize all paladin systems. Call from MegaMod constructor.
     */
    public static void init(IEventBus modEventBus) {
        // Always include items from mods we don't actually have loaded
        tweaksConfig.ignore_items_required_mods = true;

        PaladinSounds.init(modEventBus);
        // PaladinBlocks NOT registered here — monk_workbench already in RuneWorkbenchRegistry
        PaladinEffects.init(modEventBus);
        PaladinEntities.init(modEventBus);
    }

    /**
     * Register items - call after deferred registers are set up.
     */
    public static void registerItems(IEventBus modEventBus) {
        Group.init(modEventBus);
        PaladinBooks.register();
        PaladinWeapons.init(modEventBus);
        PaladinShields.init(modEventBus);
        Armors.init(modEventBus);
    }
}
