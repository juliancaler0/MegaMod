package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the sifter building.
 */
public class BlockHutSifter extends AbstractBlockHut<BlockHutSifter> {

    public static final MapCodec<BlockHutSifter> CODEC = simpleCodec(BlockHutSifter::new);

    public BlockHutSifter(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutSifter> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "sifter";
    }
}
