package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the mystical_site building.
 */
public class BlockHutMysticalSite extends AbstractBlockHut<BlockHutMysticalSite> {

    public static final MapCodec<BlockHutMysticalSite> CODEC = simpleCodec(BlockHutMysticalSite::new);

    public BlockHutMysticalSite(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutMysticalSite> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "mystical_site";
    }
}
