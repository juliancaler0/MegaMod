package com.ultra.megamod.feature.dungeons.generation;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * DeferredRegister for custom Features used by the dungeon generation system.
 */
public class DungeonFeatureRegistry {

    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, MegaMod.MODID);

    public static final Supplier<DungeonMobFeature> DUNGEON_MOB_FEATURE =
            FEATURES.register("dungeon_mob", () -> new DungeonMobFeature(DungeonMobFeatureConfig.CODEC));

    public static void init(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
