package com.ultra.megamod.feature.dungeons.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Port of DNL's WeightedListProcessor.
 * Replaces a specific input block with one of several weighted output blocks.
 * Used for randomizing decorative blocks (pebbles, cobwebs, bookshelves, etc.)
 */
public class WeightedListProcessor extends StructureProcessor {

    public record WeightedBlock(String block, int weight) {
        public static final Codec<WeightedBlock> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("block").forGetter(WeightedBlock::block),
                Codec.INT.fieldOf("weight").forGetter(WeightedBlock::weight)
        ).apply(inst, WeightedBlock::new));
    }

    public static final MapCodec<WeightedListProcessor> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("input_block").forGetter(p -> p.inputBlock),
            WeightedBlock.CODEC.listOf().fieldOf("outputs").forGetter(p -> p.outputs)
    ).apply(inst, WeightedListProcessor::new));

    private final String inputBlock;
    private final List<WeightedBlock> outputs;
    private final int totalWeight;

    public WeightedListProcessor(String inputBlock, List<WeightedBlock> outputs) {
        this.inputBlock = inputBlock;
        this.outputs = outputs;
        int tw = 0;
        for (WeightedBlock wb : outputs) tw += wb.weight;
        this.totalWeight = tw;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level,
            BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo,
            StructureTemplate.StructureBlockInfo relativeBlockInfo,
            StructurePlaceSettings settings) {
        Block inputBl = parseBlock(this.inputBlock);
        if (relativeBlockInfo.state().getBlock() != inputBl) {
            return relativeBlockInfo;
        }

        RandomSource random = settings.getRandom(relativeBlockInfo.pos());
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (WeightedBlock wb : outputs) {
            cumulative += wb.weight;
            if (roll < cumulative) {
                Block outputBl = parseBlock(wb.block);
                return new StructureTemplate.StructureBlockInfo(
                        relativeBlockInfo.pos(),
                        outputBl.defaultBlockState(),
                        relativeBlockInfo.nbt()
                );
            }
        }
        return relativeBlockInfo;
    }

    private static Block parseBlock(String blockStr) {
        try {
            Identifier id = Identifier.parse(blockStr);
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            return block != null ? block : Blocks.AIR;
        } catch (Exception e) {
            return Blocks.AIR;
        }
    }

    @Override
    public StructureProcessorType<?> getType() {
        return DungeonProcessorRegistry.WEIGHTED_LIST_PROCESSOR.get();
    }
}
