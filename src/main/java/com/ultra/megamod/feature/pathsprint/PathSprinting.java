/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.Blocks
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.pathsprint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid="megamod")
public class PathSprinting {

    /** Cache last checked block position and result per player to avoid checking every tick. */
    private static final Map<UUID, CachedPathCheck> pathCache = new ConcurrentHashMap<>();

    private record CachedPathCheck(BlockPos pos, boolean onPath) {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        double dz;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        BlockPos belowPos = player2.blockPosition().below();
        UUID playerId = player2.getUUID();

        // Only re-check the block if the player moved to a new block position
        boolean onPath;
        CachedPathCheck cached = pathCache.get(playerId);
        if (cached != null && cached.pos().equals(belowPos)) {
            onPath = cached.onPath();
        } else {
            onPath = player2.level().getBlockState(belowPos).is(Blocks.DIRT_PATH);
            pathCache.put(playerId, new CachedPathCheck(belowPos, onPath));
        }

        if (!onPath) {
            return;
        }
        double dx = player2.getDeltaMovement().x();
        if (dx * dx + (dz = player2.getDeltaMovement().z()) * dz <= 1.0E-4) {
            return;
        }
        player2.addEffect(new MobEffectInstance(MobEffects.SPEED, 5, 0, false, false, false));
    }
}

