package com.ultra.megamod.feature.furniture;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Quest Board block that spawns in villages.
 * When right-clicked, opens the Quest Board screen (same UI as the Royal Herald)
 * where players can accept dungeon quests.
 */
public class QuestBoardBlock extends FurnitureBlock {
    public static final MapCodec<QuestBoardBlock> CODEC = QuestBoardBlock.simpleCodec(QuestBoardBlock::new);

    public QuestBoardBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends QuestBoardBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            QuestBoardClientHelper.openScreen();
        }
        return InteractionResult.SUCCESS;
    }
}
