package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the glassblower building.
 */
public class BlockHutGlassblower extends AbstractBlockHut<BlockHutGlassblower> {

    public static final MapCodec<BlockHutGlassblower> CODEC = simpleCodec(BlockHutGlassblower::new);

    public BlockHutGlassblower(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutGlassblower> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "glassblower";
    }
}
