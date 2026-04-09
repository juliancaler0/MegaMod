package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the composter building.
 */
public class BlockHutComposter extends AbstractBlockHut<BlockHutComposter> {

    public static final MapCodec<BlockHutComposter> CODEC = simpleCodec(BlockHutComposter::new);

    public BlockHutComposter(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutComposter> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "composter";
    }
}
