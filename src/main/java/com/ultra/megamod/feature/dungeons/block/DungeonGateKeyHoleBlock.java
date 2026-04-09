/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 */
package com.ultra.megamod.feature.dungeons.block;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.item.DungeonMiniKeyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DungeonGateKeyHoleBlock
extends Block {
    public static final MapCodec<DungeonGateKeyHoleBlock> CODEC = DungeonGateKeyHoleBlock.simpleCodec(DungeonGateKeyHoleBlock::new);

    public DungeonGateKeyHoleBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends DungeonGateKeyHoleBlock> codec() {
        return CODEC;
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.CONSUME;
        }
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof DungeonMiniKeyItem) {
            held.shrink(1);
            int radius = 3;
            Block gateBlock = (Block)DungeonEntityRegistry.DUNGEON_GATE_BLOCK.get();
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dy = -radius; dy <= radius; ++dy) {
                    for (int dz = -radius; dz <= radius; ++dz) {
                        BlockPos checkPos = pos.offset(dx, dy, dz);
                        if (!level.getBlockState(checkPos).is(gateBlock)) continue;
                        level.destroyBlock(checkPos, false);
                    }
                }
            }
            level.destroyBlock(pos, false);
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}

