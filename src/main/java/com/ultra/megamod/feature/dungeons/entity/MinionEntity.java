/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
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

import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MinionEntity
extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    private static final int MAX_LIFETIME_TICKS = 600;
    private static final float BASE_HP = 20.0f;
    private static final float BASE_DAMAGE = 4.0f;
    private UUID parentBossUUID = null;
    private int lifetimeTicks = 0;
    private float difficultyMultiplier = 1.0f;

    public MinionEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createMinionAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 1.0).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.MOVEMENT_SPEED, 0.32);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(2, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.2, false));
        this.goalSelector.addGoal(5, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0));
        this.goalSelector.addGoal(6, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 10.0f));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    public void setParentBossUUID(UUID uuid) {
        this.parentBossUUID = uuid;
    }

    public UUID getParentBossUUID() {
        return this.parentBossUUID;
    }

    public void setDifficultyMultiplier(float mult) {
        this.difficultyMultiplier = mult;
        float scaledHP = 20.0f + 20.0f * (mult - 1.0f) * 0.5f;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)scaledHP);
        this.setHealth(scaledHP);
        float scaledDamage = 4.0f * mult * 0.6f;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)scaledDamage);
    }

    public void tick() {
        ServerLevel serverLevel;
        Entity parent;
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
        ++this.lifetimeTicks;
        if (this.lifetimeTicks >= 600) {
            this.discard();
            return;
        }
        if (!(this.parentBossUUID == null || this.lifetimeTicks % 20 != 0 || (parent = (serverLevel = (ServerLevel)this.level()).getEntity(this.parentBossUUID)) != null && parent.isAlive())) {
            this.discard();
            return;
        }
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifetimeTicks", this.lifetimeTicks);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        if (this.parentBossUUID != null) {
            output.putString("ParentBossUUID", this.parentBossUUID.toString());
        }
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifetimeTicks = input.getIntOr("LifetimeTicks", 0);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        String uuidStr = input.getStringOr("ParentBossUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.parentBossUUID = UUID.fromString(uuidStr);
            }
            catch (IllegalArgumentException ignored) {
                this.parentBossUUID = null;
            }
        }
    }
}

