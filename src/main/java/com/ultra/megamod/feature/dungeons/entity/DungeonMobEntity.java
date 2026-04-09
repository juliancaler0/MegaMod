/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.AnimationState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.ValueInput
 *  net.minecraft.world.level.storage.ValueOutput
 */
package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DungeonMobEntity
extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int skinVariant = 0;

    public DungeonMobEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.skinVariant = level.getRandom().nextInt(4);
    }

    public int getSkinVariant() {
        return this.skinVariant;
    }

    public static AttributeSupplier.Builder createDungeonMobAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.0, false));
        this.goalSelector.addGoal(5, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 0.8));
        this.goalSelector.addGoal(6, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, (Goal)new RandomLookAroundGoal((Mob)this));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void applyDungeonScaling(DungeonTier tier) {
        this.tierLevel = tier.getLevel();
        this.difficultyMultiplier = tier.getDifficultyMultiplier();
        float scaledHP = 30.0f * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)scaledHP);
        this.setHealth(scaledHP);
        // Damage uses diminishing scaling — stays dangerous but not instant-kill
        // Normal=1.0x, Hard=1.25x, Nightmare=1.75x, Infernal=2.5x, Mythic=3.5x, Eternal=5.5x
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = 5.0f * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + (double)this.tierLevel);
        tier.applyMobEffects(this);
    }

    public static DungeonMobEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        DungeonMobEntity mob = new DungeonMobEntity(DungeonEntityRegistry.DUNGEON_MOB.get(), (Level)level);
        mob.setPos((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5);
        mob.setYRot(level.getRandom().nextFloat() * 360.0f);
        mob.applyDungeonScaling(tier);
        level.addFreshEntity((Entity)mob);
        return mob;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.6f) {
            this.spawnAtLocation(level, new ItemStack(DungeonEntityRegistry.SKELETON_BONE.get()));
        }
        if (this.getRandom().nextFloat() < 0.05f) {
            this.spawnAtLocation(level, new ItemStack(DungeonEntityRegistry.SKELETON_HEAD.get()));
        }
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putInt("SkinVariant", this.skinVariant);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.skinVariant = input.getIntOr("SkinVariant", 0);
        float scaledHP = 30.0f * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = 5.0f * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + (double)this.tierLevel);
        // Re-apply tier potion effects (Resistance, Speed, Strength, etc.)
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}

