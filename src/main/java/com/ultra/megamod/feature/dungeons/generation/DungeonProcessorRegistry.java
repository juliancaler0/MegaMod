package com.ultra.megamod.feature.dungeons.generation;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * DeferredRegister for custom StructureProcessorTypes used by the dungeon generation system.
 */
public class DungeonProcessorRegistry {

    private static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MegaMod.MODID);

    public static final Supplier<StructureProcessorType<WeightedListProcessor>> WEIGHTED_LIST_PROCESSOR =
            PROCESSORS.register("weighted_list", () -> () -> WeightedListProcessor.CODEC);

    public static final Supplier<StructureProcessorType<WaterloggingFixProcessor>> WATERLOGGING_FIX_PROCESSOR =
            PROCESSORS.register("waterlogging_fix", () -> () -> WaterloggingFixProcessor.CODEC);

    public static void init(IEventBus modEventBus) {
        PROCESSORS.register(modEventBus);
    }
}
