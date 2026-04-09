package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.entity.AnimationState;
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
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BluffEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState puffAnimationState = new AnimationState();

    private static final float BASE_HP = 15.0f;
    private static final float BASE_DAMAGE = 2.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private boolean puffedUp = false;
    private int puffTimer = 0;

    public BluffEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public boolean isPuffedUp() {
        return this.puffedUp;
    }

    public static AttributeSupplier.Builder createBluffAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new PanicGoal((PathfinderMob) this, 1.4));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        // No targeting - passive mob, only retaliates with puff
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !this.puffedUp) {
            this.puffedUp = true;
            this.puffTimer = 60; // 3 seconds
            this.puffAnimationState.start(this.tickCount);

            // AOE knockback
            AABB area = this.getBoundingBox().inflate(3.0);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity entity : nearby) {
                if (entity == this) continue;
                Vec3 knockback = entity.position().subtract(this.position()).normalize().scale(1.5);
                entity.push(knockback.x, 0.5, knockback.z);
            }

            level.sendParticles((ParticleOptions) ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 0.5, this.getZ(), 15, 1.0, 0.5, 1.0, 0.1);
            level.playSound(null, this.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        return hurt;
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
        tier.applyMobEffects(this);
    }

    public static BluffEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        BluffEntity bluff = new BluffEntity(DungeonEntityRegistry.BLUFF.get(), (Level) level);
        bluff.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        bluff.setYRot(level.getRandom().nextFloat() * 360.0f);
        bluff.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) bluff);
        return bluff;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.5f) {
            this.spawnAtLocation(level, new ItemStack(DungeonEntityRegistry.BLUFF_ROD.get(), 1 + this.getRandom().nextInt(2)));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
        if (this.puffedUp) {
            if (--this.puffTimer <= 0) {
                this.puffedUp = false;
                if (this.level() instanceof ServerLevel sl) {
                    sl.playSound(null, this.blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SLIME_SQUISH;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putBoolean("PuffedUp", this.puffedUp);
        output.putInt("PuffTimer", this.puffTimer);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.puffedUp = input.getBooleanOr("PuffedUp", false);
        this.puffTimer = input.getIntOr("PuffTimer", 0);
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
