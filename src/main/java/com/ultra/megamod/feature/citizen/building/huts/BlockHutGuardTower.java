package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the guard_tower building.
 */
public class BlockHutGuardTower extends AbstractBlockHut<BlockHutGuardTower> {

    public static final MapCodec<BlockHutGuardTower> CODEC = simpleCodec(BlockHutGuardTower::new);

    public BlockHutGuardTower(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutGuardTower> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "guard_tower";
    }
}
