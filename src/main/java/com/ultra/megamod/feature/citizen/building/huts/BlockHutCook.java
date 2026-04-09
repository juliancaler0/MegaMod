package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the cook building.
 */
public class BlockHutCook extends AbstractBlockHut<BlockHutCook> {

    public static final MapCodec<BlockHutCook> CODEC = simpleCodec(BlockHutCook::new);

    public BlockHutCook(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutCook> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "cook";
    }
}
