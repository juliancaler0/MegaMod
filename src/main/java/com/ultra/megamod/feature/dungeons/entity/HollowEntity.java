package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HollowEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private static final float BASE_HP = 15.0f;
    private static final float BASE_DAMAGE = 5.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int retreatCooldown = 0;
    private int chargeCooldown = 0;

    public HollowEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createHollowAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.2, false));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 1.0));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 16.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Apply Blindness II on hit for 40 ticks
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 1, false, true));
            // Hollow's disorienting presence inverts movement briefly
            living.addEffect(new MobEffectInstance(
                com.ultra.megamod.feature.relics.effect.RelicEffectRegistry.CONFUSION, 40, 0, false, true));
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        // Teleport short distance when hurt
        if (hurt && this.retreatCooldown <= 0) {
            this.retreatCooldown = 60; // 3 second cooldown
            double dx = (this.getRandom().nextDouble() - 0.5) * 8.0;
            double dz = (this.getRandom().nextDouble() - 0.5) * 8.0;
            double newX = this.getX() + dx;
            double newZ = this.getZ() + dz;
            this.teleportTo(newX, this.getY(), newZ);
            level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.HOSTILE, 0.5f, 1.5f);
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

    public static HollowEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        HollowEntity hollow = new HollowEntity(DungeonEntityRegistry.HOLLOW.get(), (Level) level);
        hollow.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        hollow.setYRot(level.getRandom().nextFloat() * 360.0f);
        hollow.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) hollow);
        return hollow;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.2f) {
            this.spawnAtLocation(level, new ItemStack(Items.ENDER_PEARL));
        }
        if (this.getRandom().nextFloat() < 0.1f) {
            this.spawnAtLocation(level, new ItemStack(Items.PHANTOM_MEMBRANE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
        if (this.retreatCooldown > 0) {
            --this.retreatCooldown;
        }
        // Charge attack: dash at target
        if (this.chargeCooldown > 0) {
            --this.chargeCooldown;
        }
        LivingEntity target = this.getTarget();
        if (this.chargeCooldown <= 0 && target != null && target.isAlive()) {
            double dist = this.distanceTo(target);
            if (dist >= 5.0 && dist <= 16.0) {
                Vec3 dir = target.position().subtract(this.position()).normalize();
                this.setDeltaMovement(dir.x * 0.8, 0.1, dir.z * 0.8);
                this.chargeCooldown = 100;
                this.attackAnimationState.start(this.tickCount);
                ((ServerLevel) this.level()).playSound(null, this.blockPosition(),
                        SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 1.0f, 1.5f);
            }
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
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
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
