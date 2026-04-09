package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the blacksmith building.
 */
public class BlockHutBlacksmith extends AbstractBlockHut<BlockHutBlacksmith> {

    public static final MapCodec<BlockHutBlacksmith> CODEC = simpleCodec(BlockHutBlacksmith::new);

    public BlockHutBlacksmith(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutBlacksmith> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "blacksmith";
    }
}
