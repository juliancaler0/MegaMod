package com.ultra.megamod.feature.combat.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages active spell barrier entities spawned by executeSpawn().
 * Each barrier applies knockback to nearby enemies every tick and despawns after its duration expires.
 * Registered on the game event bus via @EventBusSubscriber.
 */
@EventBusSubscriber(modid = "megamod")
public class SpellBarrierManager {

    private static final List<ActiveBarrier> activeBarriers = new CopyOnWriteArrayList<>();

    /**
     * Register a new barrier to be ticked.
     */
    public static void registerBarrier(ServerLevel level, UUID entityId, UUID casterId,
                                        Vec3 position, float radius, int durationTicks) {
        activeBarriers.add(new ActiveBarrier(level.dimension().identifier().toString(),
            entityId, casterId, position, radius, durationTicks, 0));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (activeBarriers.isEmpty()) return;

        List<ActiveBarrier> toRemove = new ArrayList<>();

        for (ActiveBarrier barrier : activeBarriers) {
            barrier.ticksAlive++;

            // Find the server level
            ServerLevel level = null;
            for (ServerLevel sl : event.getServer().getAllLevels()) {
                if (sl.dimension().identifier().toString().equals(barrier.dimensionKey)) {
                    level = sl;
                    break;
                }
            }

            if (level == null) {
                toRemove.add(barrier);
                continue;
            }

            // Check if barrier entity still exists
            Entity barrierEntity = level.getEntity(barrier.entityId);
            if (barrierEntity == null || !barrierEntity.isAlive()) {
                toRemove.add(barrier);
                continue;
            }

            // Check duration
            if (barrier.ticksAlive >= barrier.durationTicks) {
                barrierEntity.discard();
                // Despawn particles
                level.sendParticles(ParticleTypes.CLOUD, barrier.position.x,
                    barrier.position.y + 1, barrier.position.z, 10, 0.5, 0.5, 0.5, 0.05);
                toRemove.add(barrier);
                continue;
            }

            // Every tick: push away enemy entities within radius
            AABB knockbackBox = new AABB(
                barrier.position.x - barrier.radius,
                barrier.position.y - 1,
                barrier.position.z - barrier.radius,
                barrier.position.x + barrier.radius,
                barrier.position.y + 3,
                barrier.position.z + barrier.radius
            );

            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, knockbackBox,
                e -> e.isAlive() && !e.getUUID().equals(barrier.casterId)
                    && e.distanceToSqr(barrier.position) <= barrier.radius * barrier.radius);

            for (LivingEntity entity : nearby) {
                // Only push hostile mobs away from the barrier center (not other players or passive mobs)
                if (entity instanceof net.minecraft.world.entity.monster.Monster) {
                    Vec3 pushDir = entity.position().subtract(barrier.position).normalize();
                    double pushStrength = 0.3;
                    entity.push(pushDir.x * pushStrength, 0.05, pushDir.z * pushStrength);
                    entity.hurtMarked = true;
                }
            }

            // Ambient particles every 10 ticks
            if (barrier.ticksAlive % 10 == 0) {
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2.0 / 8) * i;
                    double px = barrier.position.x + Math.cos(angle) * barrier.radius * 0.8;
                    double pz = barrier.position.z + Math.sin(angle) * barrier.radius * 0.8;
                    level.sendParticles(ParticleTypes.END_ROD, px, barrier.position.y + 1.5, pz,
                        1, 0, 0.2, 0, 0);
                }
            }
        }

        if (!toRemove.isEmpty()) {
            activeBarriers.removeAll(toRemove);
        }
    }

    /**
     * Clear all barriers (e.g., on server stop).
     */
    public static void clearAll() {
        activeBarriers.clear();
    }

    private static class ActiveBarrier {
        final String dimensionKey;
        final UUID entityId;
        final UUID casterId;
        final Vec3 position;
        final float radius;
        final int durationTicks;
        int ticksAlive;

        ActiveBarrier(String dimensionKey, UUID entityId, UUID casterId,
                      Vec3 position, float radius, int durationTicks, int ticksAlive) {
            this.dimensionKey = dimensionKey;
            this.entityId = entityId;
            this.casterId = casterId;
            this.position = position;
            this.radius = radius;
            this.durationTicks = durationTicks;
            this.ticksAlive = ticksAlive;
        }
    }
}
