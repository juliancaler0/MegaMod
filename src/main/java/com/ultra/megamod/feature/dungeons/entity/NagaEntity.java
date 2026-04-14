package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.dungeons.entity.PoisonBallEntity;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class NagaEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState lungeAnimationState = new AnimationState();

    private static final float BASE_HP = 35.0f;
    private static final float BASE_DAMAGE = 7.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int lungeCooldown = 0;

    public NagaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createNagaAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FLYING_SPEED, 0.32);
    }

    protected void registerGoals() {
        // No MeleeAttackGoal or WaterAvoidingRandomStrollGoal — flying entity handles movement/attacks in tick()
        this.goalSelector.addGoal(5, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 24.0f));
        this.goalSelector.addGoal(6, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, false));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Apply Poison II for 80 ticks on hit
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true));
            // Venom paralyzes briefly — prevents the target from kiting
            living.addEffect(new MobEffectInstance(
                com.ultra.megamod.feature.relics.effect.RelicEffectRegistry.PARALYSIS, 20, 0, false, true));
            // Lunge toward target on hit
            Vec3 direction = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(direction.x * 0.8, 0.25, direction.z * 0.8);
        }
        return hit;
    }

    public void applyDungeonScaling(DungeonTier tier) {
        this.tierLevel = tier.getLevel();
        this.difficultyMultiplier = tier.getDifficultyMultiplier();
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        this.setHealth(scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(3.0 + (double) this.tierLevel);
        tier.applyMobEffects(this);
    }

    public static NagaEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        NagaEntity naga = new NagaEntity(DungeonEntityRegistry.NAGA.get(), (Level) level);
        naga.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        naga.setYRot(level.getRandom().nextFloat() * 360.0f);
        naga.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) naga);
        return naga;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.4f) {
            this.spawnAtLocation(level, new ItemStack(Items.BONE));
        }
        if (this.getRandom().nextFloat() < 0.15f) {
            this.spawnAtLocation(level, new ItemStack(Items.BONE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
        // Flying movement — swoop toward target for melee, pull back for ranged
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double dist = this.distanceTo(target);
            Vec3 direction = target.position().subtract(this.position()).normalize();

            if (dist > 4.0) {
                // Fly toward target — hover slightly above
                double speed = 0.08;
                double hoverY = target.getY() + 1.5;
                this.setDeltaMovement(
                        direction.x * speed,
                        (hoverY - this.getY()) * 0.06,
                        direction.z * speed
                );
            } else {
                // Close range — slow down, hover at target height for melee
                this.setDeltaMovement(
                        direction.x * 0.02,
                        (target.getY() + 0.5 - this.getY()) * 0.05,
                        direction.z * 0.02
                );
            }

            // Melee attack when within 3 blocks
            if (dist <= 3.0 && this.lungeCooldown <= 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.doHurtTarget(serverLevel, target);
                }
                this.lungeCooldown = 20; // short cooldown between melee swipes
                this.attackAnimationState.start(this.tickCount);
            }

            // Ranged poison ball when 6-20 blocks away
            if (dist >= 6.0 && dist <= 20.0 && this.lungeCooldown <= 0 && this.tickCount % 10 == 0) {
                PoisonBallEntity.shoot(this.level(), this, target);
                this.lungeCooldown = 60;
                this.attackAnimationState.start(this.tickCount);
                this.level().playSound(null, this.blockPosition(), SoundEvents.PHANTOM_SWOOP, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.2f);
            }
        } else {
            // Gentle bob when idle
            double bobSpeed = Math.sin(this.tickCount * 0.05) * 0.01;
            this.setDeltaMovement(0, bobSpeed, 0);
        }

        // Cooldown decrement
        if (this.lungeCooldown > 0) {
            --this.lungeCooldown;
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(3.0 + (double) this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
