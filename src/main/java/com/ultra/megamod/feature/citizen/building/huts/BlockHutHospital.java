package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the hospital building.
 */
public class BlockHutHospital extends AbstractBlockHut<BlockHutHospital> {

    public static final MapCodec<BlockHutHospital> CODEC = simpleCodec(BlockHutHospital::new);

    public BlockHutHospital(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutHospital> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "hospital";
    }
}
