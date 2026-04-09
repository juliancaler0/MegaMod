package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Umvuthana Raptor — pack alpha that spawns followers on creation.
 * Reference: MowziesMobs EntityUmvuthanaRaptor. FURY mask, higher stats.
 * Spawns 2-3 UmvuthanaFollowerEntity around itself. When raptor dies, followers go independent.
 */
public class UmvuthanaRaptorEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private static final float BASE_HP = 35.0f;
    private static final float BASE_DAMAGE = 9.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private boolean hasSpawnedPack = false;
    private final List<UUID> followerUUIDs = new ArrayList<>();

    public UmvuthanaRaptorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public int getMaskType() { return 0; } // FURY mask

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.3, false));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal<>((Mob) this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // FURY mask: Strength-based, applies Weakness
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true));
        }
        return hit;
    }

    public void applyDungeonScaling(DungeonTier tier) {
        this.tierLevel = tier.getLevel();
        this.difficultyMultiplier = tier.getDifficultyMultiplier();
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(scaledHP);
        this.setHealth(scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(4.0 + tierLevel);
        tier.applyMobEffects(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }

        // Spawn pack on first tick (1-2 followers, capped by nearby minion count)
        if (!this.hasSpawnedPack) {
            this.hasSpawnedPack = true;
            ServerLevel serverLevel = (ServerLevel) this.level();
            int nearbyMinions = serverLevel.getEntitiesOfClass(Monster.class,
                    this.getBoundingBox().inflate(32.0),
                    e -> e != this && e.isAlive() && (
                        e instanceof UmvuthanaEntity ||
                        e instanceof UmvuthanaFollowerEntity ||
                        e instanceof UmvuthanaRaptorEntity ||
                        e instanceof UmvuthanaCraneEntity
                    )).size();
            int packSize = Math.min(1 + serverLevel.getRandom().nextInt(2), Math.max(0, 6 - nearbyMinions)); // 1-2 followers, capped at 6 total
            for (int i = 0; i < packSize; i++) {
                UmvuthanaFollowerEntity follower = new UmvuthanaFollowerEntity(
                        DungeonEntityRegistry.UMVUTHANA_FOLLOWER.get(), this.level());
                double angle = 2.0 * Math.PI * i / Math.max(1, packSize);
                follower.setPos(this.getX() + Math.cos(angle) * 3.0,
                        this.getY(), this.getZ() + Math.sin(angle) * 3.0);
                follower.setYRot(this.getRandom().nextFloat() * 360.0f);
                follower.setLeaderUUID(this.getUUID());
                follower.applyDungeonScaling(DungeonTier.fromLevel(this.tierLevel));
                serverLevel.addFreshEntity((Entity) follower);
                this.followerUUIDs.add(follower.getUUID());
            }
            if (packSize > 0) {
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON,
                        SoundSource.HOSTILE, 1.0f, 1.2f);
            }
        }

        // Teleport distant followers back to raptor every 100 ticks
        if (this.tickCount % 100 == 0 && !this.followerUUIDs.isEmpty()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (UUID uuid : this.followerUUIDs) {
                Entity e = serverLevel.getEntity(uuid);
                if (e instanceof UmvuthanaFollowerEntity follower && follower.isAlive()) {
                    if (follower.distanceTo(this) > 20.0) {
                        follower.setPos(this.getX() + (this.getRandom().nextDouble() - 0.5) * 4.0,
                                this.getY(), this.getZ() + (this.getRandom().nextDouble() - 0.5) * 4.0);
                    }
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.4f)
            this.spawnAtLocation(level, new ItemStack(Items.GOLD_NUGGET, 5));
        if (this.getRandom().nextFloat() < 0.15f)
            this.spawnAtLocation(level, new ItemStack(Items.GOLDEN_APPLE));
    }

    public static UmvuthanaRaptorEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        UmvuthanaRaptorEntity raptor = new UmvuthanaRaptorEntity(DungeonEntityRegistry.UMVUTHANA_RAPTOR.get(), level);
        raptor.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        raptor.setYRot(level.getRandom().nextFloat() * 360.0f);
        raptor.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) raptor);
        return raptor;
    }

    protected SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.PILLAGER_HURT; }
    protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putBoolean("HasSpawnedPack", this.hasSpawnedPack);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.hasSpawnedPack = input.getBooleanOr("HasSpawnedPack", false);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BASE_HP * this.difficultyMultiplier);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(4.0 + this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
