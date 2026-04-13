package com.ultra.megamod.feature.combat.archers;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.archers.block.ArcherBlocks;
import com.ultra.megamod.feature.combat.archers.config.ArchersItemConfig;
import com.ultra.megamod.feature.combat.archers.config.Default;
import com.ultra.megamod.feature.combat.archers.config.TweaksConfig;
import com.ultra.megamod.feature.combat.archers.effect.ArcherEffects;
import com.ultra.megamod.feature.combat.archers.item.Group;
import com.ultra.megamod.feature.combat.archers.item.ArcherWeapons;
import com.ultra.megamod.feature.combat.archers.item.ArcherArmors;
import com.ultra.megamod.feature.combat.archers.item.misc.Misc;
import com.ultra.megamod.feature.combat.archers.content.ArcherSounds;
import com.ultra.megamod.feature.combat.archers.village.ArcherVillagers;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;

public class ArchersMod {
    public static final String ID = MegaMod.MODID;

    // Config values stored as simple static fields (no tiny_config on NeoForge)
    public static ArchersItemConfig itemConfig = Default.itemConfig;
    public static ConfigFile.Effects effectsConfig = new ConfigFile.Effects();
    public static TweaksConfig tweaksConfig = new TweaksConfig();

    public static void init(IEventBus modEventBus) {
        // Archers is always present in MegaMod — no conditional mod loading needed
        tweaksConfig.ignore_items_required_mods = true;

        ArcherSounds.register(modEventBus);
        // ArcherBlocks removed — archers_workbench is registered by RuneWorkbenchRegistry
        ArcherEffects.register(effectsConfig);
        // Misc removed — auto_fire_hook, quivers registered by ArcherItemRegistry
        // ArcherWeapons removed — all weapons registered by ClassWeaponRegistry
        // ArcherArmors removed — all armor registered by ClassArmorRegistry
        // ArcherVillagers removed — archery_artisan profession registered by CombatVillagerRegistry
    }
}
