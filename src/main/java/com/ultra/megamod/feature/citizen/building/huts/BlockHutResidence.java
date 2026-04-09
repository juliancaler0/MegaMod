package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the residence building.
 */
public class BlockHutResidence extends AbstractBlockHut<BlockHutResidence> {

    public static final MapCodec<BlockHutResidence> CODEC = simpleCodec(BlockHutResidence::new);

    public BlockHutResidence(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutResidence> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "residence";
    }
}
