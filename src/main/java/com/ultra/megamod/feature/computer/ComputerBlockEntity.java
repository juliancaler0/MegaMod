/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.ultra.megamod.feature.computer;

import com.ultra.megamod.feature.computer.ComputerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ComputerBlockEntity
extends BlockEntity {
    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ComputerRegistry.COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
    }
}

