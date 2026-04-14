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
import net.minecraft.world.phys.Vec3;

public class UndeadKnightEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private static final float BASE_HP = 40.0f;
    private static final float BASE_DAMAGE = 8.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;

    public UndeadKnightEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createKnightAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.0, false));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.6));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Heavy knockback
            Vec3 knockback = target.position().subtract(this.position()).normalize().scale(1.5);
            target.push(knockback.x, 0.3, knockback.z);
            // Apply Mining Fatigue on hit
            living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 0, false, true));
            // Heavy cleave causes Bleeding
            living.addEffect(new MobEffectInstance(
                com.ultra.megamod.feature.relics.effect.RelicEffectRegistry.BLEEDING, 120, 0, false, true));
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 25% chance to block (take 50% reduced damage)
        if (this.getRandom().nextFloat() < 0.25f && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
            amount *= 0.5f;
        }
        return super.hurtServer(level, source, amount);
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(6.0 + (double) this.tierLevel);
        tier.applyMobEffects(this);
    }

    public static UndeadKnightEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        UndeadKnightEntity knight = new UndeadKnightEntity(DungeonEntityRegistry.UNDEAD_KNIGHT.get(), (Level) level);
        knight.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        knight.setYRot(level.getRandom().nextFloat() * 360.0f);
        knight.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) knight);
        return knight;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.8f) {
            this.spawnAtLocation(level, new ItemStack(DungeonEntityRegistry.SKELETON_BONE.get()));
        }
        if (this.getRandom().nextFloat() < 0.1f) {
            this.spawnAtLocation(level, new ItemStack(DungeonEntityRegistry.SKELETON_HEAD.get()));
        }
        if (this.getRandom().nextFloat() < 0.05f) {
            int roll = this.getRandom().nextInt(4);
            switch (roll) {
                case 0 -> this.spawnAtLocation(level, new ItemStack(Items.IRON_HELMET));
                case 1 -> this.spawnAtLocation(level, new ItemStack(Items.IRON_CHESTPLATE));
                case 2 -> this.spawnAtLocation(level, new ItemStack(Items.IRON_LEGGINGS));
                case 3 -> this.spawnAtLocation(level, new ItemStack(Items.IRON_BOOTS));
            }
        }
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(6.0 + (double) this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
