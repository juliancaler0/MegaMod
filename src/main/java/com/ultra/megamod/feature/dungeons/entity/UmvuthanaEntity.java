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

public class UmvuthanaEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState summonAnimationState = new AnimationState();

    private static final float BASE_HP = 28.0f;
    private static final float BASE_DAMAGE = 6.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private boolean hasSummonedAid = false;
    private int maskType;

    public UmvuthanaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.maskType = level.getRandom().nextInt(6);
    }

    public int getMaskType() {
        return this.maskType;
    }

    public static AttributeSupplier.Builder createUmvuthanaAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.2, false));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Each mask type applies a different debuff (level 0, 40 ticks)
            switch (this.maskType) {
                case 0 -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
                case 1 -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true));
                case 2 -> living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, true));
                case 3 -> living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true));
                case 4 -> living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, false, true));
                case 5 -> living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0, false, true));
            }
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(4.0 + (double) this.tierLevel);
        tier.applyMobEffects(this);
    }

    public static UmvuthanaEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        UmvuthanaEntity umvuthana = new UmvuthanaEntity(DungeonEntityRegistry.UMVUTHANA.get(), (Level) level);
        umvuthana.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        umvuthana.setYRot(level.getRandom().nextFloat() * 360.0f);
        umvuthana.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) umvuthana);
        return umvuthana;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.3f) {
            this.spawnAtLocation(level, new ItemStack(Items.GOLD_NUGGET, 3));
        }
        if (this.getRandom().nextFloat() < 0.1f) {
            this.spawnAtLocation(level, new ItemStack(Items.GOLDEN_APPLE));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PILLAGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putInt("MaskType", this.maskType);
        output.putBoolean("HasSummonedAid", this.hasSummonedAid);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.maskType = input.getIntOr("MaskType", 0);
        this.hasSummonedAid = input.getBooleanOr("HasSummonedAid", false);
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(4.0 + (double) this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
