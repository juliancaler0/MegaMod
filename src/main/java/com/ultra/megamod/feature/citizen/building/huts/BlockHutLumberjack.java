package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the lumberjack building.
 */
public class BlockHutLumberjack extends AbstractBlockHut<BlockHutLumberjack> {

    public static final MapCodec<BlockHutLumberjack> CODEC = simpleCodec(BlockHutLumberjack::new);

    public BlockHutLumberjack(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutLumberjack> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "lumberjack";
    }
}
