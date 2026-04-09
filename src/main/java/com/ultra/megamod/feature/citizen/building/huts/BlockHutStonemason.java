package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the stonemason building.
 */
public class BlockHutStonemason extends AbstractBlockHut<BlockHutStonemason> {

    public static final MapCodec<BlockHutStonemason> CODEC = simpleCodec(BlockHutStonemason::new);

    public BlockHutStonemason(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutStonemason> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "stonemason";
    }
}
