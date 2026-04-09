package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
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

import java.util.UUID;

/**
 * Umvuthana Follower — pack member that follows an UmvuthanaRaptor leader.
 * Reference: MowziesMobs EntityUmvuthanaFollowerToRaptor. FEAR mask, lower stats.
 * Circles leader in formation, syncs target with leader. Goes independent if leader dies.
 */
public class UmvuthanaFollowerEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private static final float BASE_HP = 22.0f;
    private static final float BASE_DAMAGE = 5.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private UUID leaderUUID = null;
    private int circleTick = 0;

    public UmvuthanaFollowerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public int getMaskType() { return 1; } // FEAR mask

    public void setLeaderUUID(UUID uuid) { this.leaderUUID = uuid; }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.2, false));
        this.goalSelector.addGoal(4, (Goal) new FollowLeaderGoal(this));
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
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true));
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + tierLevel);
        tier.applyMobEffects(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }

        this.circleTick++;

        // Sync target with leader
        if (this.leaderUUID != null && this.getTarget() == null) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            Entity leader = serverLevel.getEntity(this.leaderUUID);
            if (leader instanceof UmvuthanaRaptorEntity raptor && raptor.isAlive()) {
                LivingEntity leaderTarget = raptor.getTarget();
                if (leaderTarget != null) {
                    this.setTarget(leaderTarget);
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.2f)
            this.spawnAtLocation(level, new ItemStack(Items.GOLD_NUGGET, 2));
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
        if (this.leaderUUID != null) output.putString("LeaderUUID", this.leaderUUID.toString());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        String uuid = input.getStringOr("LeaderUUID", "");
        if (!uuid.isEmpty()) {
            try { this.leaderUUID = UUID.fromString(uuid); } catch (Exception ignored) {}
        }
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BASE_HP * this.difficultyMultiplier);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }

    /**
     * AI goal: move toward leader in a circular formation when not attacking.
     */
    static class FollowLeaderGoal extends Goal {
        private final UmvuthanaFollowerEntity follower;

        FollowLeaderGoal(UmvuthanaFollowerEntity follower) {
            this.follower = follower;
        }

        @Override
        public boolean canUse() {
            if (follower.leaderUUID == null || follower.getTarget() != null) return false;
            if (!(follower.level() instanceof ServerLevel serverLevel)) return false;
            Entity leader = serverLevel.getEntity(follower.leaderUUID);
            return leader instanceof UmvuthanaRaptorEntity raptor && raptor.isAlive()
                    && follower.distanceTo(raptor) > 4.0;
        }

        @Override
        public void tick() {
            if (!(follower.level() instanceof ServerLevel serverLevel)) return;
            Entity leader = serverLevel.getEntity(follower.leaderUUID);
            if (leader == null) return;
            // Circle around leader at ~3 block radius
            double angle = follower.circleTick * 0.05;
            double targetX = leader.getX() + Math.cos(angle) * 3.0;
            double targetZ = leader.getZ() + Math.sin(angle) * 3.0;
            follower.getNavigation().moveTo(targetX, leader.getY(), targetZ, 1.0);
        }
    }
}
