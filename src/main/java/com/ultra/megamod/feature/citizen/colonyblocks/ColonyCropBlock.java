package com.ultra.megamod.feature.citizen.colonyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

/**
 * Base crop block for colony crops.
 * Uses the standard 8-stage age system (0-7) from CropBlock.
 */
public class ColonyCropBlock extends CropBlock {
    public static final MapCodec<ColonyCropBlock> CODEC = ColonyCropBlock.simpleCodec(ColonyCropBlock::new);

    private Supplier<? extends Item> seedItemSupplier;

    public ColonyCropBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    public ColonyCropBlock withSeed(Supplier<? extends Item> seedSupplier) {
        this.seedItemSupplier = seedSupplier;
        return this;
    }

    @Override
    public MapCodec<? extends ColonyCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        if (seedItemSupplier != null) {
            return seedItemSupplier.get();
        }
        return super.getBaseSeedId();
    }
}
