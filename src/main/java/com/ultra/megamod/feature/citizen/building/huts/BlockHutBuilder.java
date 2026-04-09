package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the builder building.
 */
public class BlockHutBuilder extends AbstractBlockHut<BlockHutBuilder> {

    public static final MapCodec<BlockHutBuilder> CODEC = simpleCodec(BlockHutBuilder::new);

    public BlockHutBuilder(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutBuilder> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "builder";
    }
}
