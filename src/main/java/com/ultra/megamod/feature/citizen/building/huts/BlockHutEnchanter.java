package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the enchanter building.
 */
public class BlockHutEnchanter extends AbstractBlockHut<BlockHutEnchanter> {

    public static final MapCodec<BlockHutEnchanter> CODEC = simpleCodec(BlockHutEnchanter::new);

    public BlockHutEnchanter(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutEnchanter> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "enchanter";
    }
}
