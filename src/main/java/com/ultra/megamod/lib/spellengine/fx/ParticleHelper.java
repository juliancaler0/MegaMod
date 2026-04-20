package com.ultra.megamod.lib.spellengine.fx;



import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.particle.TemplateParticleEffect;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.network.Packets;
import com.ultra.megamod.lib.spellengine.utils.TargetHelper;
import com.ultra.megamod.lib.spellengine.utils.VectorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ParticleHelper {
    private static Random rng = new Random();

    public static void sendBatches(Entity trackedEntity, ParticleBatch[] batches) {
        sendBatches(trackedEntity, batches, true);
    }

    public static void sendBatches(Entity trackedEntity, ParticleBatch[] batches, boolean includeSourceEntity) {
        sendBatches(trackedEntity, null, batches, 1, trackedEntity.level() instanceof ServerLevel sl ? sl.getChunkSource().chunkMap.getPlayers(trackedEntity.chunkPosition(), false) : java.util.List.of(), includeSourceEntity);
    }

    public static void sendBatches(Entity trackedEntity, ParticleBatch[] batches, float countMultiplier, Collection<ServerPlayer> trackers) {
        sendBatches(trackedEntity, null, batches, countMultiplier, trackers, true);
    }

    public static void sendBatches(Vec3 location, LivingEntity caster, ParticleBatch[] batches) {
        Collection<ServerPlayer> trackers;
        if (caster instanceof ServerPlayer serverPlayer) {
            var array = new ArrayList<ServerPlayer>(caster.level() instanceof ServerLevel sl ? sl.getChunkSource().chunkMap.getPlayers(caster.chunkPosition(), false) : java.util.List.of());
            array.add(serverPlayer);
            trackers = array;
        } else {
            trackers = caster.level() instanceof ServerLevel sl ? sl.getChunkSource().chunkMap.getPlayers(caster.chunkPosition(), false) : java.util.List.of();
        }
        sendBatches(null, location, batches, 1, trackers, false);
    }

    public static void sendBatches(@Nullable Entity trackedEntity, @Nullable Vec3 location, ParticleBatch[] batches, float countMultiplier, Collection<ServerPlayer> trackers, boolean includeSourceEntity) {
        if (batches == null || batches.length == 0) {
            return;
        }
        int sourceEntityId = 0;
        var sourceType = Packets.ParticleBatches.SourceType.COORDINATE;
        if (trackedEntity != null) {
            sourceEntityId = trackedEntity.getId();
            sourceType = Packets.ParticleBatches.SourceType.ENTITY;
        }
        ArrayList<Packets.ParticleBatches.Spawn> spawns = new ArrayList<>();
        for(var batch : batches) {
            Vec3 sourceLocation = Vec3.ZERO;
            switch (sourceType) {
                case ENTITY -> {
                    sourceLocation = origin(trackedEntity, batch.origin);
                }
                case COORDINATE -> {
                    if (location != null) {
                        sourceLocation = location;
                    }
                }
            }
            float yaw = 0;
            float pitch = 0;
            if (trackedEntity != null) {
                yaw = trackedEntity.getYRot();
                pitch = trackedEntity.getXRot();
            }
            spawns.add(new Packets.ParticleBatches.Spawn(
                    includeSourceEntity ? sourceEntityId : 0,
                    yaw,
                    pitch,
                    sourceLocation, batch));
        }
        var packet = new Packets.ParticleBatches(sourceType, countMultiplier, spawns);
        if (trackedEntity instanceof ServerPlayer serverPlayer) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
        trackers.forEach(serverPlayer -> {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, packet);
        });
    }

    public static void play(Level world, Entity source, ParticleBatch[] batches) {
        if (batches == null) {
            return;
        }
        for (var batch: batches) {
            play(world, source, 0, 0, batch);
        }
    }

    public static void play(Level world, Entity source, ParticleBatch batch) {
        play(world, source, 0, 0, batch);
    }

    public static void play(Level world, Entity entity, float yaw, float pitch, ParticleBatch batch) {
        play(world, entity.tickCount, origin(entity, batch.origin), entity.getBbWidth(), yaw, pitch, batch, entity);
    }

    private static ParticleOptions resolveParticleType(ParticleBatch batch, @Nullable Entity sourceEntity) {
        var id = Identifier.parse(batch.particle_id);
        var particleOpt = BuiltInRegistries.PARTICLE_TYPE.get(id);
        if (particleOpt.isEmpty()) return null;
        var particle = (ParticleOptions) particleOpt.get().value();

        if (particle instanceof TemplateParticleEffect templateParticleEffect) {
            var copy = templateParticleEffect.copy();
            var appearance = copy.createOrDefaultAppearance();
            if (batch.color_rgba >= 0) {
                appearance.color = Color.fromRGBA(batch.color_rgba);
            }
            if (batch.follow_entity) {
                appearance.entityFollowed = sourceEntity;
            }
            if (batch.scale != 1) {
                appearance.scale = batch.scale;
            }
            if (batch.origin == ParticleBatch.Origin.GROUND) {
                appearance.grounded = true;
            }
            appearance.max_age = batch.max_age;
            particle = copy;
        }
        return particle;
    }

    public static void play(Level world, long time, Vec3 origin, float width, float yaw, float pitch, ParticleBatch batch, @Nullable Entity sourceEntity) {
        try {
            var particle = resolveParticleType(batch, sourceEntity);

            var count = batch.count;
            if (batch.count < 1) {
                count = rng.nextFloat() < batch.count ? 1 : 0;
            }
            for(int i = 0; i < count; ++i) {
                var direction = direction(batch, time, yaw, pitch);
                var particleSpecificOrigin = origin.add(offset(width, batch.extent, batch.shape, direction.normalize(), batch.rotation, yaw, pitch));
                if (batch.pre_spawn_travel != 0) {
                    particleSpecificOrigin = particleSpecificOrigin.add(direction.scale(batch.pre_spawn_travel));
                }
                if (batch.invert) {
                    direction = direction.scale(-1);
                }
                // Source SpellEngine encodes appearance (color_rgba, scale, follow entity)
                // into a TemplateParticleType instance. Our registry uses SimpleParticleType
                // so that codec path is a no-op — we propagate appearance via a ThreadLocal
                // the particle factories read during construction. Set it *per spawn* because
                // the factory's consumeAppearance() clears it on read.
                publishAppearance(batch, sourceEntity);
                world.addParticle(particle,
                        particleSpecificOrigin.x, particleSpecificOrigin.y, particleSpecificOrigin.z,
                        direction.x, direction.y, direction.z);
            }
        } catch (Exception e) {
            System.err.println("Failed to play particle batch - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the {@code feature.combat.spell.client.particle.TemplateParticleType} ThreadLocal
     * so magic particle factories (SpellUniversalParticle, SpellFlameParticle, etc.) can apply
     * per-spawn color / scale / follow-entity from the spell JSON's {@code ParticleBatch}.
     * Called immediately before each {@code level.addParticle} so the appearance is fresh.
     */
    private static void publishAppearance(ParticleBatch batch, @Nullable Entity sourceEntity) {
        var appearance = new com.ultra.megamod.feature.combat.spell.client.particle.TemplateParticleType.Appearance();
        if (batch.color_rgba != 0) {
            appearance.color = com.ultra.megamod.feature.combat.spell.client.util.Color.fromRGBA(batch.color_rgba);
        }
        if (batch.scale != 1) {
            appearance.scale = batch.scale;
        }
        if (batch.follow_entity) {
            appearance.entityFollowed = sourceEntity;
        }
        if (batch.max_age > 0) {
            appearance.max_age = batch.max_age;
        }
        com.ultra.megamod.feature.combat.spell.client.particle.TemplateParticleType.setAppearance(appearance);
    }

    public static List<SpawnInstruction> convertToInstructions(Level world, Packets.ParticleBatches packet) {
        var instructions = new ArrayList<SpawnInstruction>();
        var sourceType = packet.sourceType();
        for(var spawn: packet.spawns()) {
            float yaw = spawn.yaw();
            float pitch = spawn.pitch();
            var batch = spawn.batch();
            var origin = Vec3.ZERO;
            float width = 0.5F;
            Entity sourceEntity = world.getEntity(spawn.sourceEntityId());
            switch (sourceType) {
                case ENTITY -> {
                    origin = sourceEntity != null
                            ? origin(sourceEntity, batch.origin)
                            : origin(world, spawn.sourceLocation(), 2, batch.origin);
                }
                case COORDINATE -> {
                    origin = spawn.sourceLocation();
                }
            }

            var particle = resolveParticleType(batch, sourceEntity);

            var count = batch.count;
            if (batch.count < 1) {
                count = rng.nextFloat() < batch.count ? 1 : 0;
            }
            for(int i = 0; i < count; ++i) {
                var direction = direction(batch, world.getGameTime(), yaw, pitch);
                var particleSpecificOrigin = origin.add(offset(width, batch.extent, batch.shape, direction.normalize(), batch.rotation, yaw, pitch));
                if (batch.pre_spawn_travel != 0) {
                    particleSpecificOrigin = particleSpecificOrigin.add(direction.scale(batch.pre_spawn_travel));
                }
                if (batch.invert) {
                    direction = direction.scale(-1);
                }
                instructions.add(new SpawnInstruction(particle, sourceEntity, batch,
                        particleSpecificOrigin.x, particleSpecificOrigin.y, particleSpecificOrigin.z,
                        direction.x, direction.y, direction.z));
            }
        }
        return instructions;
    }

    /**
     * A deferred particle spawn. Carries the {@link ParticleBatch} so that when
     * {@link #perform} runs on the client render thread, it can re-publish the
     * appearance ThreadLocal fresh for each spawn (factories consume it once).
     */
    public record SpawnInstruction(ParticleOptions particle, @Nullable Entity sourceEntity,
                                   @Nullable ParticleBatch batch,
                                   double positionX, double positionY, double positionZ,
                                   double velocityX, double velocityY, double velocityZ) {
        public SpawnInstruction(ParticleOptions particle, @Nullable Entity sourceEntity,
                                double positionX, double positionY, double positionZ,
                                double velocityX, double velocityY, double velocityZ) {
            this(particle, sourceEntity, null, positionX, positionY, positionZ, velocityX, velocityY, velocityZ);
        }

        public void perform(Level world) {
            try {
                if (batch != null) {
                    publishAppearance(batch, sourceEntity);
                }
                world.addParticle(particle,
                        positionX, positionY, positionZ,
                        velocityX, velocityY, velocityZ);
            } catch (Exception e) {
                System.err.println("Failed to perform particle SpawnInstruction");
            }
        }
    }

    private static Vec3 origin(Entity entity, ParticleBatch.Origin origin) {
        switch (origin) {
            case FEET -> {
                return entity.position().add(0, entity.getBbHeight() * 0.1F, 0);
            }
            case CENTER -> {
                return entity.position().add(0, entity.getBbHeight() * 0.5F, 0);
            }
            case LAUNCH_POINT -> {
                if (entity instanceof LivingEntity livingEntity) {
                    return SpellHelper.launchPoint(livingEntity);
                } else {
                    return entity.position().add(0, entity.getBbHeight() * 0.5F, 0);
                }
            }
            case GROUND -> {
                var position = TargetHelper.findSolidBelow(entity, entity.position(), entity.level(), -2);
                if (position != null) {
                    return new Vec3(entity.getX(), position.y + 0.1F, entity.getZ());
                } else {
                    return entity.position().add(0, 0.1F, 0);
                }
            }
        }
        assert true;
        return entity.position();
    }

    private static Vec3 origin(Level world, Vec3 entityPos, float entityHeight, ParticleBatch.Origin origin) {
        switch (origin) {
            case FEET -> {
                return entityPos.add(0, entityHeight * 0.1F, 0);
            }
            case CENTER -> {
                return entityPos.add(0, entityHeight * 0.5F, 0);
            }
            case LAUNCH_POINT -> {
                return entityPos.add(0, entityHeight * 0.75F, 0);
            }
            case GROUND -> {
                var position = TargetHelper.findSolidBlockBelow(null, entityPos, world, -2);
                if (position != null) {
                    return new Vec3(entityPos.x, position.y + 0.1F, entityPos.z);
                } else {
                    return entityPos.add(0, 0.1F, 0);
                }
            }
        }
        assert true;
        return entityPos;
    }

    private static Vec3 offset(float width, float extent, ParticleBatch.Shape shape, Vec3 direction,
                                ParticleBatch.Rotation rotation, float yaw, float pitch) {
        var offset = Vec3.ZERO;
        if (extent >= ParticleBatch.EXTENT_TRESHOLD) {
            width = 0;
            extent -= ParticleBatch.EXTENT_TRESHOLD;
        }
        switch (shape) {
            case LINE_VERTICAL, CIRCLE, CONE, SPHERE -> {
                if (extent > 0) {
                    offset = direction.scale(extent);
                }
                return offset;
            }
            case PIPE -> {
                var size = width * 0.5F + extent;
                var angle = (float) Math.toRadians(rng.nextFloat() * 360F);
                offset = new Vec3(size,0,0).yRot(angle);
            }
            case WIDE_PIPE -> {
                var size = width + extent;
                var angle = (float) Math.toRadians(rng.nextFloat() * 360F);
                offset = new Vec3(size,0,0).yRot(angle);
            }
            case PILLAR -> {
                var x = (width * 0.5F + extent) * rng.nextFloat();
                var angle = (float) Math.toRadians(rng.nextFloat() * 360F);
                offset = new Vec3(x,0,0).yRot(angle);
            }
        }

        if (rotation != null) {
            switch (rotation) {
                case LOOK -> {
                    offset = offset
                            .xRot((float) Math.toRadians(-1 * (pitch + 90)))
                            .yRot((float) Math.toRadians(-yaw));
                }
            }
        }
        return offset;
    }

    private static Vec3 direction(ParticleBatch batch, long time, float yaw, float pitch) {
        var direction = Vec3.ZERO;

        float rotateAroundX = 0;
        float rotateAroundY = 0;
        switch (batch.shape) {
            case LINE -> {
                direction = new Vec3(0, 0, randomInRange(batch.min_speed, batch.max_speed));
                pitch = -pitch; // Inverting pitch, do not remove, it makes things work :D
            }
            case CONE -> {
                direction = new Vec3(0, randomInRange(batch.min_speed, batch.max_speed), 0);
                rotateAroundX += rng.nextFloat() * batch.angle - (batch.angle * 0.5F);
                rotateAroundY += rng.nextFloat() * batch.angle - (batch.angle * 0.5F);
            }
            case CIRCLE -> {
                direction = new Vec3(0, 0, randomInRange(batch.min_speed, batch.max_speed))
                        .yRot((float) Math.toRadians(rng.nextFloat() * 360F));
            }
            case LINE_VERTICAL, PILLAR, PIPE, WIDE_PIPE -> {
                direction = new Vec3(0, randomInRange(batch.min_speed, batch.max_speed), 0);
            }
            case SPHERE -> {
                direction = new Vec3(randomInRange(batch.min_speed, batch.max_speed), 0, 0)
                        .zRot((float) Math.toRadians(rng.nextFloat() * 360F))
                        .yRot((float) Math.toRadians(rng.nextFloat() * 360F));
            }
        }
        if (batch.rotation != null) {
            switch (batch.rotation) {
                case LOOK -> {
                    // Find actual rotation
                    float pRot = -pitch;
                    float yRot = yaw * (-1F);

                    direction = direction
                            .xRot((float) Math.toRadians(pRot - 90 + rotateAroundX))
                            .yRot((float) Math.toRadians(yRot + rotateAroundY));

                    if (batch.roll > 0) {
                        var axis = VectorHelper.axisFromRotation(yRot, pRot).scale(-1);
                        var diff = ((time * batch.roll) % 360) + batch.roll_offset;
                        direction = VectorHelper.rotateAround(direction, axis, diff);
                    }
                }
            }
        } else {
            direction = direction
                    .xRot((float) Math.toRadians(rotateAroundX))
                    .yRot((float) Math.toRadians(rotateAroundY));

            if (batch.roll > 0) {
                var diff = ((time * batch.roll) % 360) + batch.roll_offset;
                direction = direction.yRot((float) Math.toRadians(diff));
            }
        }

        return direction;
    }

    private static float randomInRange(float min, float max) {
        float range = max - min;
        return min + (range * rng.nextFloat());
    }

    private static float randomSignedInRange(float min, float max) {
        var rand = rng.nextFloat();
        var range = max - min;
        float sign = (rand > 0.5F) ? 1 : (-1);
        var base = sign * min;
        var varied = sign * range * rand;
        return base + varied;
    }
}
