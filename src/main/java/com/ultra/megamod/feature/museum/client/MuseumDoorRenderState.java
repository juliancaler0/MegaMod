package com.ultra.megamod.feature.museum.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class MuseumDoorRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public DoubleBlockHalf half = DoubleBlockHalf.LOWER;
}
