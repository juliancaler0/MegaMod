package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the barracks_tower building.
 */
public class BlockHutBarracksTower extends AbstractBlockHut<BlockHutBarracksTower> {

    public static final MapCodec<BlockHutBarracksTower> CODEC = simpleCodec(BlockHutBarracksTower::new);

    public BlockHutBarracksTower(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutBarracksTower> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "barracks_tower";
    }
}
