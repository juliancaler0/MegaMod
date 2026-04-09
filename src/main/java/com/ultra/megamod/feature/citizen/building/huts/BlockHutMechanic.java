package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the mechanic building.
 */
public class BlockHutMechanic extends AbstractBlockHut<BlockHutMechanic> {

    public static final MapCodec<BlockHutMechanic> CODEC = simpleCodec(BlockHutMechanic::new);

    public BlockHutMechanic(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutMechanic> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "mechanic";
    }
}
