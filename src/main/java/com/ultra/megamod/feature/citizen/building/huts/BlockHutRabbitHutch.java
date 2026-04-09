package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the rabbit_hutch building.
 */
public class BlockHutRabbitHutch extends AbstractBlockHut<BlockHutRabbitHutch> {

    public static final MapCodec<BlockHutRabbitHutch> CODEC = simpleCodec(BlockHutRabbitHutch::new);

    public BlockHutRabbitHutch(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutRabbitHutch> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "rabbit_hutch";
    }
}
