package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the graveyard building.
 */
public class BlockHutGraveyard extends AbstractBlockHut<BlockHutGraveyard> {

    public static final MapCodec<BlockHutGraveyard> CODEC = simpleCodec(BlockHutGraveyard::new);

    public BlockHutGraveyard(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutGraveyard> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "graveyard";
    }
}
