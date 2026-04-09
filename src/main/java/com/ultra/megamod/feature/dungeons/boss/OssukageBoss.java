/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.dungeons.entity.KunaiEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class OssukageBoss
extends DungeonBossEntity {
    private static final float BASE_HP = 300.0f;
    private static final float BASE_DAMAGE = 12.0f;
    private boolean isDashing = false;
    private int dashTicks = 0;
    private Vec3 dashDirection = Vec3.ZERO;

    public OssukageBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new AvoidSpikeBlockGoal((PathfinderMob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.0, true));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 16.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    public static AttributeSupplier.Builder createOssukageAttributes() {
        return DungeonBossEntity.createBossAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ARMOR, 8.0).add(Attributes.ARMOR_TOUGHNESS, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    public int getMaxPhases() {
        return 4;
    }

    @Override
    public float getBaseMaxHealth() {
        return 300.0f;
    }

    @Override
    public float getBaseDamage() {
        return 12.0f;
    }

    @Override
    public Component getBossDisplayName() {
        return Component.literal((String)"Ossukage").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD});
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel)this.level();
        switch (newPhase) {
            case 2: {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.5f, 0.8f);
                break;
            }
            case 3: {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.HOSTILE, 1.5f, 0.6f);
                break;
            }
            case 4: {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 2, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 2, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0f, 0.5f);
            }
        }
    }

    @Override
    public void performAttack(int phase) {
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)this.level();
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        switch (phase) {
            case 1: {
                this.performHeavySwing(serverLevel, target);
                break;
            }
            case 2: {
                this.performDashCharge(serverLevel, target);
                break;
            }
            case 3: {
                this.performKunaiThrow(serverLevel, target);
                break;
            }
            case 4: {
                this.performBerserkCombo(serverLevel, target);
            }
        }
    }

    private void performHeavySwing(ServerLevel level, LivingEntity target) {
        double dist = this.distanceToSqr((Entity)target);
        if (dist > 9.0) {
            return;
        }
        float damage = 12.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().mobAttack((LivingEntity)this);
        target.hurt(source, damage);
        Vec3 knockback = target.position().subtract(this.position()).normalize().scale(2.0);
        target.push(knockback.x, 0.5, knockback.z);
        level.sendParticles((ParticleOptions)ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.5, 0.3, 0.5, 0.0);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5f, 0.7f);
    }

    private void performDashCharge(ServerLevel level, LivingEntity target) {
        if (!this.isDashing) {
            this.isDashing = true;
            this.dashTicks = 15;
            this.dashDirection = target.position().subtract(this.position()).normalize();
            level.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_STEP, SoundSource.HOSTILE, 2.0f, 0.5f);
        }
    }

    private void performKunaiThrow(ServerLevel level, LivingEntity target) {
        // Use KunaiEntity instead of SmallFireball
        KunaiEntity.shoot(this, target);
        double dist = this.distanceToSqr((Entity)target);
        if (dist < 9.0) {
            float damage = 9.6f * this.getDamageMultiplier();
            target.hurt(this.damageSources().mobAttack((LivingEntity)this), damage);
        }
        level.playSound(null, this.blockPosition(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.0f, 0.9f);
    }

    private void performBerserkCombo(ServerLevel level, LivingEntity target) {
        double dist = this.distanceToSqr((Entity)target);
        if (dist < 16.0) {
            float damage = 24.0f * this.getDamageMultiplier();
            DamageSource source = this.damageSources().mobAttack((LivingEntity)this);
            target.hurt(source, damage);
            if (target instanceof LivingEntity) {
                LivingEntity living = target;
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, true));
            }
            target.igniteForSeconds(3.0f);
            level.sendParticles((ParticleOptions)ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.5, 0.5, 0.5, 0.2);
            level.sendParticles((ParticleOptions)ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.5f, 0.6f);
            this.attackCooldown = Math.max(8, 20 - this.currentPhase * 3);
        } else if (dist < 400.0) {
            this.performDashCharge(level, target);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel)this.level();
        if (this.isDashing && this.dashTicks > 0) {
            --this.dashTicks;
            // Check for dangerous drops ahead in dash path — abort dash if pit detected
            BlockPos aheadPos = BlockPos.containing(
                this.getX() + this.dashDirection.x * 2.0,
                this.getY(),
                this.getZ() + this.dashDirection.z * 2.0);
            if (isDangerousDrop(aheadPos)) {
                this.isDashing = false;
                this.dashTicks = 0;
                this.setDeltaMovement(Vec3.ZERO);
                // Re-engage pathfinding so the boss doesn't freeze
                LivingEntity dashTarget = this.getTarget();
                if (dashTarget != null) {
                    this.getNavigation().moveTo(dashTarget, 1.0);
                }
            } else {
                double dashSpeed = 1.2;
                this.setDeltaMovement(this.dashDirection.x * dashSpeed, 0.0, this.dashDirection.z * dashSpeed);
                level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
                List<Player> nearby = level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(1.5));
                for (Player player : nearby) {
                    float damage = 14.400001f * this.getDamageMultiplier();
                    player.hurt(this.damageSources().mobAttack((LivingEntity)this), damage);
                    Vec3 knockback = player.position().subtract(this.position()).normalize().scale(1.5);
                    player.push(knockback.x, 0.4, knockback.z);
                }
                if (this.dashTicks <= 0) {
                    this.isDashing = false;
                    this.setDeltaMovement(Vec3.ZERO);
                }
            }
        }
        if (this.currentPhase == 4 && this.tickCount % 10 == 0) {
            level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 5, 1.0, 0.1, 1.0, 0.01);
        }
    }
}

