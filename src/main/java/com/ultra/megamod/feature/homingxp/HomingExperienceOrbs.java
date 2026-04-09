/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.homingxp;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class HomingExperienceOrbs {
    private static final double HOMING_RANGE = 16.0;
    private static final double HOMING_SPEED = 0.35;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        ServerLevel level = player2.level();
        if (level.getGameTime() % 10L != 0L) {
            return;
        }
        Vec3 playerPos = player2.position();
        AABB searchBox = player2.getBoundingBox().inflate(HOMING_RANGE);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, searchBox);
        for (ExperienceOrb orb : orbs) {
            Vec3 orbPos;
            Vec3 direction;
            double distance;
            double distSq = orb.distanceToSqr(playerPos);
            if (distSq > HOMING_RANGE * HOMING_RANGE || (distance = (direction = playerPos.subtract(orbPos = orb.position())).length()) < 0.5) continue;
            Vec3 velocity = direction.normalize().scale(0.35);
            orb.setDeltaMovement(velocity);
        }
    }
}

