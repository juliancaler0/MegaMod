package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the school building.
 */
public class BlockHutSchool extends AbstractBlockHut<BlockHutSchool> {

    public static final MapCodec<BlockHutSchool> CODEC = simpleCodec(BlockHutSchool::new);

    public BlockHutSchool(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutSchool> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "school";
    }
}
