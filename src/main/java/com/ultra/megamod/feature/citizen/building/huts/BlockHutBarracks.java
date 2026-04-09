package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the barracks building.
 */
public class BlockHutBarracks extends AbstractBlockHut<BlockHutBarracks> {

    public static final MapCodec<BlockHutBarracks> CODEC = simpleCodec(BlockHutBarracks::new);

    public BlockHutBarracks(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutBarracks> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "barracks";
    }
}
