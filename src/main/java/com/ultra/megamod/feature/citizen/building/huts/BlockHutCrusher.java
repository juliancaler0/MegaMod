package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the crusher building.
 */
public class BlockHutCrusher extends AbstractBlockHut<BlockHutCrusher> {

    public static final MapCodec<BlockHutCrusher> CODEC = simpleCodec(BlockHutCrusher::new);

    public BlockHutCrusher(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutCrusher> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "crusher";
    }
}
