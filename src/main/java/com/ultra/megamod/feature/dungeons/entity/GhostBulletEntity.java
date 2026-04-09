package com.ultra.megamod.feature.dungeons.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

public class GhostBulletEntity extends Projectile {
    private static final int MAX_LIFETIME = 100;
    private static final float DAMAGE = 4.0f;
    private int lifetime = 0;
    private LivingEntity homingTarget;

    public GhostBulletEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public GhostBulletEntity(Level level, LivingEntity owner, LivingEntity target) {
        super(DungeonEntityRegistry.GHOST_BULLET.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getY() + owner.getBbHeight() / 2.0, owner.getZ());
        this.homingTarget = target;
        // Initial direction toward target
        Vec3 direction = target.position().add(0, target.getBbHeight() / 2.0, 0)
                .subtract(this.position()).normalize();
        this.setDeltaMovement(direction.scale(0.4));
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

        // 10% homing behavior
        if (this.homingTarget != null && this.homingTarget.isAlive() && !this.level().isClientSide()) {
            Vec3 toTarget = this.homingTarget.position().add(0, this.homingTarget.getBbHeight() / 2.0, 0)
                    .subtract(this.position()).normalize();
            Vec3 current = this.getDeltaMovement().normalize();
            // Gentle 10% homing blend
            Vec3 newDir = current.scale(0.9).add(toTarget.scale(0.1)).normalize();
            this.setDeltaMovement(newDir.scale(0.4));
        }

        // Move
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        // Check for collision
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            if (hitResult instanceof EntityHitResult entityHit) {
                this.onHitEntity(entityHit);
            } else if (hitResult instanceof BlockHitResult blockHit) {
                // Pass through chaos_spawner cage blocks instead of dying
                net.minecraft.world.level.block.Block hitBlock = this.level().getBlockState(blockHit.getBlockPos()).getBlock();
                String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(hitBlock).toString();
                if (!blockId.startsWith("dungeonnowloading:chaos_spawner")) {
                    this.discard();
                }
            }
        }

        // Particle trail
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
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
            // Apply Weakness I for 40 ticks
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.POOF,
                    this.getX(), this.getY(), this.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
        }
        this.discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Only hit players — prevents friendly fire on dungeon mobs
        return entity instanceof net.minecraft.world.entity.player.Player && super.canHitEntity(entity);
    }

    public static GhostBulletEntity shoot(Level level, LivingEntity owner, LivingEntity target) {
        GhostBulletEntity bullet = new GhostBulletEntity(level, owner, target);
        level.addFreshEntity(bullet);
        return bullet;
    }
}
