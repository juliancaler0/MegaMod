package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the chicken_herder building.
 */
public class BlockHutChickenHerder extends AbstractBlockHut<BlockHutChickenHerder> {

    public static final MapCodec<BlockHutChickenHerder> CODEC = simpleCodec(BlockHutChickenHerder::new);

    public BlockHutChickenHerder(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutChickenHerder> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "chicken_herder";
    }
}
