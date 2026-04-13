package com.ultra.megamod.feature.worldedit.history;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A single block mutation: before/after at a position. */
public record BlockChange(BlockPos pos, BlockState oldState, BlockState newState) {}
