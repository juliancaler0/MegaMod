package com.ultra.megamod.feature.dungeons.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AxeSwingEntity extends Projectile {
    private static final int MAX_LIFETIME = 15;
    private int lifetime = 0;

    public AxeSwingEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public AxeSwingEntity(Level level, LivingEntity owner, LivingEntity target) {
        super(DungeonEntityRegistry.AXE_SWING.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getY() + owner.getBbHeight() / 2.0, owner.getZ());
        // Direction toward target
        Vec3 direction = target.position().add(0, target.getBbHeight() / 2.0, 0)
                .subtract(this.position()).normalize();
        this.setDeltaMovement(direction.scale(0.3));
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

        // No homing - visual only projectile

        // Move
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        // No hit detection - purely visual

        // Particle trail - EXPLOSION (1) + FLAME (2)
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION,
                    this.getX(), this.getY(), this.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 2, 0.1, 0.1, 0.1, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // No-op: visual only, damage is handled by the axe slam itself
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Never hit any entity - purely visual
        return false;
    }

    public static AxeSwingEntity shoot(Level level, LivingEntity owner, LivingEntity target) {
        AxeSwingEntity swing = new AxeSwingEntity(level, owner, target);
        level.addFreshEntity(swing);
        return swing;
    }
}
