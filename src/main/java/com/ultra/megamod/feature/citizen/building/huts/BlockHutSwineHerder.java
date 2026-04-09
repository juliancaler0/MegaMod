package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the swine_herder building.
 */
public class BlockHutSwineHerder extends AbstractBlockHut<BlockHutSwineHerder> {

    public static final MapCodec<BlockHutSwineHerder> CODEC = simpleCodec(BlockHutSwineHerder::new);

    public BlockHutSwineHerder(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutSwineHerder> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "swine_herder";
    }
}
