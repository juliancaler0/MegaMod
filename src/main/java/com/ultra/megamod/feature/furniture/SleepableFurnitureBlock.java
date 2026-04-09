package com.ultra.megamod.feature.furniture;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SleepableFurnitureBlock extends FurnitureBlock {
    public static final MapCodec<SleepableFurnitureBlock> CODEC = SleepableFurnitureBlock.simpleCodec(SleepableFurnitureBlock::new);

    private static final Map<Block, BedConfig> BED_CONFIGS = new HashMap<>();
    private static final Map<BlockPos, Set<UUID>> SLEEPING_AT = new HashMap<>();

    public record BedConfig(int maxSleepers) {}

    public SleepableFurnitureBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    public static void registerBed(Block block, int maxSleepers) {
        BED_CONFIGS.put(block, new BedConfig(maxSleepers));
    }

    protected MapCodec<? extends SleepableFurnitureBlock> codec() {
        return CODEC;
    }

    // NeoForge bed hooks — allows the sleeping system to treat this as a valid bed
    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, @Nullable LivingEntity sleeper) {
        return true;
    }

    @Override
    public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(FACING);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        BedConfig config = BED_CONFIGS.getOrDefault(this, new BedConfig(1));

        // Clean up stale sleeper entries
        Set<UUID> sleepers = SLEEPING_AT.computeIfAbsent(pos, k -> new HashSet<>());
        sleepers.removeIf(uuid -> {
            ServerPlayer sp = ((net.minecraft.server.level.ServerLevel) level).getServer().getPlayerList().getPlayer(uuid);
            return sp == null || !sp.isSleeping();
        });

        if (sleepers.size() >= config.maxSleepers()) {
            serverPlayer.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            return InteractionResult.SUCCESS;
        }

        // Try to sleep — vanilla handles time/monster/dimension checks
        // isBed() returning true ensures the sleeping system treats this block as a bed
        // and vanilla automatically sets the respawn point
        serverPlayer.startSleepInBed(pos).ifLeft(problem -> {
            serverPlayer.displayClientMessage(Component.literal("You can't sleep right now"), true);
        }).ifRight(unit -> {
            sleepers.add(serverPlayer.getUUID());
        });

        return InteractionResult.SUCCESS;
    }
}
