package com.ultra.megamod.feature.citizen.worldgen;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registers colony world-gen structure processors and structure piece types.
 */
public class ColonyWorldGenRegistry {

    private static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MegaMod.MODID);

    private static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MegaMod.MODID);

    // Structure Types
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MegaMod.MODID);

    public static final Supplier<StructureType<EmptyColonyStructure>> EMPTY_COLONY =
            STRUCTURE_TYPES.register("empty_colony", () -> () -> EmptyColonyStructure.COLONY_CODEC);

    // Structure Processors
    public static final Supplier<StructureProcessorType<ColonyStructureProcessor>> COLONY_STRUCTURE_PROCESSOR =
            PROCESSORS.register("colony_structure_processor", () -> () -> ColonyStructureProcessor.CODEC);

    // Structure Piece Types
    public static final Supplier<StructurePieceType> RAIDER_CAMP_PIECE_TYPE =
            PIECE_TYPES.register("raider_camp", () -> RaiderCampPiece::new);

    public static void init(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
        PROCESSORS.register(modEventBus);
        PIECE_TYPES.register(modEventBus);
    }
}
