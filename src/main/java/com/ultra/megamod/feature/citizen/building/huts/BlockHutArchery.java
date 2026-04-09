package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the archery building.
 */
public class BlockHutArchery extends AbstractBlockHut<BlockHutArchery> {

    public static final MapCodec<BlockHutArchery> CODEC = simpleCodec(BlockHutArchery::new);

    public BlockHutArchery(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutArchery> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "archery";
    }
}
