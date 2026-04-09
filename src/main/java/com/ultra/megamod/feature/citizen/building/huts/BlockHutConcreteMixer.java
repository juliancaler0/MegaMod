package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the concrete_mixer building.
 */
public class BlockHutConcreteMixer extends AbstractBlockHut<BlockHutConcreteMixer> {

    public static final MapCodec<BlockHutConcreteMixer> CODEC = simpleCodec(BlockHutConcreteMixer::new);

    public BlockHutConcreteMixer(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutConcreteMixer> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "concrete_mixer";
    }
}
