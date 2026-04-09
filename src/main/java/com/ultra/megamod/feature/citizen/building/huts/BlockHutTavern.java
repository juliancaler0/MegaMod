package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the tavern building.
 */
public class BlockHutTavern extends AbstractBlockHut<BlockHutTavern> {

    public static final MapCodec<BlockHutTavern> CODEC = simpleCodec(BlockHutTavern::new);

    public BlockHutTavern(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutTavern> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "tavern";
    }
}
