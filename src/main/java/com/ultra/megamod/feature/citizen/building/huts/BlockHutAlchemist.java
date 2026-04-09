package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the alchemist building.
 */
public class BlockHutAlchemist extends AbstractBlockHut<BlockHutAlchemist> {

    public static final MapCodec<BlockHutAlchemist> CODEC = simpleCodec(BlockHutAlchemist::new);

    public BlockHutAlchemist(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutAlchemist> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "alchemist";
    }
}
