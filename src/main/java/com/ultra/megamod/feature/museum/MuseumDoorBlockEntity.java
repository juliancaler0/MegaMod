package com.ultra.megamod.feature.museum;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MuseumDoorBlockEntity extends BlockEntity {
    public MuseumDoorBlockEntity(BlockPos pos, BlockState state) {
        super(MuseumRegistry.MUSEUM_DOOR_BE.get(), pos, state);
    }
}
