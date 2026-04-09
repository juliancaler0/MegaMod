package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the deliveryman building.
 */
public class BlockHutDeliveryman extends AbstractBlockHut<BlockHutDeliveryman> {

    public static final MapCodec<BlockHutDeliveryman> CODEC = simpleCodec(BlockHutDeliveryman::new);

    public BlockHutDeliveryman(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutDeliveryman> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "deliveryman";
    }
}
