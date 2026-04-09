/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.CakeBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Post
 */
package com.ultra.megamod.feature.partycake;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid="megamod")
public class PartyCake {
    private static final Map<String, Set<BlockPos>> PARTY_CAKES = new ConcurrentHashMap<String, Set<BlockPos>>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (serverLevel.getGameTime() % 5L != 0L) {
            return;
        }
        String dimKey = serverLevel.dimension().identifier().toString();
        Set<BlockPos> cakes = PARTY_CAKES.get(dimKey);
        if (cakes != null) {
            Iterator<BlockPos> it = cakes.iterator();
            while (it.hasNext()) {
                BlockPos pos = it.next();
                if (serverLevel.getBlockState(pos).getBlock() instanceof CakeBlock) continue;
                it.remove();
            }
        }
        for (Player player : serverLevel.players()) {
            AABB playerArea = player.getBoundingBox().inflate(16.0);
            List<ItemEntity> fireworkEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, playerArea, itemEntity -> itemEntity.getItem().is(Items.FIREWORK_ROCKET));
            block1: for (ItemEntity fireworkEntity : fireworkEntities) {
                BlockPos fireworkPos = fireworkEntity.blockPosition();
                for (BlockPos checkPos : BlockPos.betweenClosed((BlockPos)fireworkPos.offset(-1, -1, -1), (BlockPos)fireworkPos.offset(1, 1, 1))) {
                    BlockState state = serverLevel.getBlockState(checkPos);
                    if (!(state.getBlock() instanceof CakeBlock)) continue;
                    BlockPos cakePos = checkPos.immutable();
                    Set dimCakes = PARTY_CAKES.computeIfAbsent(dimKey, k -> new HashSet());
                    if (dimCakes.contains(cakePos)) continue;
                    dimCakes.add(cakePos);
                    fireworkEntity.getItem().shrink(1);
                    if (fireworkEntity.getItem().isEmpty()) {
                        fireworkEntity.discard();
                    }
                    serverLevel.sendParticles((ParticleOptions)ParticleTypes.FIREWORK, (double)cakePos.getX() + 0.5, (double)cakePos.getY() + 1.0, (double)cakePos.getZ() + 0.5, 10, 0.3, 0.2, 0.3, 0.05);
                    continue block1;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos cakePos;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (event.getLevel().isClientSide()) {
            return;
        }
        ServerLevel level = player2.level();
        BlockState state = level.getBlockState(cakePos = event.getPos());
        if (!(state.getBlock() instanceof CakeBlock)) {
            return;
        }
        String dimKey = level.dimension().identifier().toString();
        Set<BlockPos> cakes = PARTY_CAKES.get(dimKey);
        if (cakes == null || !cakes.contains(cakePos)) {
            return;
        }
        double x = (double)cakePos.getX() + 0.5;
        double y = (double)cakePos.getY() + 1.0;
        double z = (double)cakePos.getZ() + 0.5;
        level.sendParticles((ParticleOptions)ParticleTypes.FIREWORK, x, y, z, 30, 0.5, 0.5, 0.5, 0.1);
        level.playSound(null, cakePos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}

