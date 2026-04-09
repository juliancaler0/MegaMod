package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Architects Cutter. Minimal — the cutter itself stores no persistent data.
 * Crafting state lives entirely in the menu (ArchitectsCutterMenu).
 */
public class ArchitectsCutterBlockEntity extends BlockEntity {

    public ArchitectsCutterBlockEntity(BlockPos pos, BlockState state) {
        super(OrnamentRegistry.CUTTER_BLOCK_ENTITY.get(), pos, state);
    }
}
