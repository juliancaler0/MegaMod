package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the post_box building.
 * Opens the WindowPostBoxMain colony request screen.
 */
public class BlockPostBox extends AbstractBlockHut<BlockPostBox> {

    public static final MapCodec<BlockPostBox> CODEC = simpleCodec(BlockPostBox::new);

    public BlockPostBox(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockPostBox> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "post_box";
    }
}
