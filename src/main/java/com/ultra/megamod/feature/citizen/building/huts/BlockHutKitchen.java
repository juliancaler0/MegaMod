package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the kitchen building.
 */
public class BlockHutKitchen extends AbstractBlockHut<BlockHutKitchen> {

    public static final MapCodec<BlockHutKitchen> CODEC = simpleCodec(BlockHutKitchen::new);

    public BlockHutKitchen(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutKitchen> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "kitchen";
    }
}
