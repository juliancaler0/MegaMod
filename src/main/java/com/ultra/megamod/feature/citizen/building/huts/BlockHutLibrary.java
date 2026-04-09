package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the library building.
 */
public class BlockHutLibrary extends AbstractBlockHut<BlockHutLibrary> {

    public static final MapCodec<BlockHutLibrary> CODEC = simpleCodec(BlockHutLibrary::new);

    public BlockHutLibrary(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutLibrary> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "library";
    }
}
