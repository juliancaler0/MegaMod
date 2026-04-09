package com.ultra.megamod.feature.dimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PortalBlockEntity extends BlockEntity {
    public PortalBlockEntity(BlockPos pos, BlockState state) {
        super(DimensionRegistry.PORTAL_BE.get(), pos, state);
    }
}
