/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.LadderBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.dropladder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class DropLadder {
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<UUID, Long>();
    private static final int COOLDOWN_TICKS = 10;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        BlockPos below;
        BlockState belowState;
        BlockPos belowPlayer;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (!player2.isShiftKeyDown()) {
            return;
        }
        if (!player2.onClimbable()) {
            return;
        }
        if (player2.getXRot() <= 45.0f) {
            return;
        }
        ServerLevel level = player2.level();
        long gameTime = level.getGameTime();
        UUID playerId = player2.getUUID();
        Long nextAllowed = COOLDOWNS.get(playerId);
        if (nextAllowed != null && gameTime < nextAllowed) {
            return;
        }
        BlockPos playerPos = player2.blockPosition();
        BlockState currentState = level.getBlockState(playerPos);
        if (!(currentState.getBlock() instanceof LadderBlock) && !((currentState = level.getBlockState(belowPlayer = playerPos.below())).getBlock() instanceof LadderBlock)) {
            return;
        }
        Direction facing = (Direction)currentState.getValue((Property)HorizontalDirectionalBlock.FACING);
        BlockPos lowestLadder = playerPos;
        if (!(level.getBlockState(lowestLadder).getBlock() instanceof LadderBlock)) {
            lowestLadder = playerPos.below();
        }
        while ((belowState = level.getBlockState(below = lowestLadder.below())).getBlock() instanceof LadderBlock) {
            lowestLadder = below;
        }
        BlockPos targetPos = lowestLadder.below();
        BlockState targetState = level.getBlockState(targetPos);
        if (!targetState.isAir() && !targetState.canBeReplaced()) {
            return;
        }
        BlockPos attachPos = targetPos.relative(facing.getOpposite());
        BlockState attachState = level.getBlockState(attachPos);
        if (!attachState.isFaceSturdy((BlockGetter)level, attachPos, facing)) {
            return;
        }
        int ladderSlot = DropLadder.findLadderSlot(player2);
        if (ladderSlot == -1) {
            return;
        }
        BlockState ladderState = (BlockState)Blocks.LADDER.defaultBlockState().setValue((Property)HorizontalDirectionalBlock.FACING, (Comparable)facing);
        level.setBlock(targetPos, ladderState, 3);
        if (!player2.isCreative()) {
            player2.getInventory().removeItem(ladderSlot, 1);
        }
        COOLDOWNS.put(playerId, gameTime + 10L);
    }

    private static int findLadderSlot(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(Items.LADDER) || stack.isEmpty()) continue;
            return i;
        }
        return -1;
    }
}

