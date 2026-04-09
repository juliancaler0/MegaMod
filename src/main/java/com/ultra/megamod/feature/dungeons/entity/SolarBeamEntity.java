package com.ultra.megamod.feature.dungeons.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SolarBeamEntity extends Projectile {
    private static final int MAX_LIFETIME = 40;
    private static final float DAMAGE = 10.0f;
    private int lifetime = 0;

    public SolarBeamEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public SolarBeamEntity(Level level, LivingEntity owner, LivingEntity target) {
        super(DungeonEntityRegistry.SOLAR_BEAM.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getY() + owner.getBbHeight() / 2.0, owner.getZ());
        // Direction toward target
        Vec3 direction = target.position().add(0, target.getBbHeight() / 2.0, 0)
                .subtract(this.position()).normalize();
        this.setDeltaMovement(direction.scale(1.5));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        ++this.lifetime;
        if (this.lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // No homing - fast straight line projectile

        // Move
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        // Check for collision
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            if (hitResult instanceof EntityHitResult entityHit) {
                this.onHitEntity(entityHit);
            } else if (hitResult instanceof BlockHitResult) {
                this.discard();
            }
        }

        // Particle trail - 3 particles per tick for high visibility
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(), 3, 0.05, 0.05, 0.05, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity living && !this.level().isClientSide()) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity ownerLiving) {
                living.hurt(this.damageSources().indirectMagic(this, ownerLiving), DAMAGE);
            } else {
                living.hurt(this.damageSources().magic(), DAMAGE);
            }
            // Set target on fire for 3 seconds
            living.igniteForSeconds(3);
        }
        // FLAME burst on hit - 8 particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 8, 0.3, 0.3, 0.3, 0.0);
        }
        this.discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Only hit players — prevents friendly fire on dungeon mobs
        return entity instanceof net.minecraft.world.entity.player.Player && super.canHitEntity(entity);
    }

    public static SolarBeamEntity shoot(Level level, LivingEntity owner, LivingEntity target) {
        SolarBeamEntity beam = new SolarBeamEntity(level, owner, target);
        level.addFreshEntity(beam);
        return beam;
    }
}
