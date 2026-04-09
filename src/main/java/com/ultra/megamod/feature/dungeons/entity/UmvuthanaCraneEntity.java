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
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Umvuthana Crane — healer variant that heals nearby allies and teleports to safety.
 * Reference: MowziesMobs EntityUmvuthanaCrane. FAITH mask, avoids players, heals Umvuthi boss.
 * Periodically heals the lowest-health ally within range. Teleports away when attacked.
 */
public class UmvuthanaCraneEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState healAnimationState = new AnimationState();

    private static final float BASE_HP = 20.0f;
    private static final float BASE_DAMAGE = 3.0f;
    private static final float HEAL_RANGE = 12.0f;
    private static final float HEAL_AMOUNT = 4.0f;
    private static final int HEAL_INTERVAL = 60; // 3 seconds
    private static final int TELEPORT_COOLDOWN = 100; // 5 seconds

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int healCooldown = 0;
    private int teleportCooldown = 0;

    public UmvuthanaCraneEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public int getMaskType() { return 5; } // FAITH mask

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new AvoidEntityGoal<>((PathfinderMob) this, Player.class, 7.0f, 1.0, 1.2));
        this.goalSelector.addGoal(2, (Goal) new HealAllyGoal(this));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
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

        if (this.healCooldown > 0) this.healCooldown--;
        if (this.teleportCooldown > 0) this.teleportCooldown--;

        // Heal nearby allies periodically
        if (this.healCooldown <= 0) {
            LivingEntity target = findHealTarget();
            if (target != null) {
                target.heal(HEAL_AMOUNT * this.difficultyMultiplier);
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true));
                this.healCooldown = HEAL_INTERVAL;
                this.healAnimationState.start(this.tickCount);
                this.level().playSound(null, this.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.HOSTILE, 0.8f, 1.5f);
            }
        }
    }

    private LivingEntity findHealTarget() {
        AABB searchBox = this.getBoundingBox().inflate(HEAL_RANGE);
        ServerLevel serverLevel = (ServerLevel) this.level();
        List<LivingEntity> allies = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox, e ->
                e != this && e.isAlive() && e.getHealth() < e.getMaxHealth() && isAlly(e));
        if (allies.isEmpty()) return null;
        // Heal the lowest-health ally
        allies.sort((a, b) -> Float.compare(a.getHealth() / a.getMaxHealth(), b.getHealth() / b.getMaxHealth()));
        return allies.getFirst();
    }

    private boolean isAlly(LivingEntity entity) {
        return entity instanceof UmvuthanaEntity
                || entity instanceof UmvuthanaRaptorEntity
                || entity instanceof UmvuthanaFollowerEntity
                || entity instanceof UmvuthanaCraneEntity
                || entity instanceof com.ultra.megamod.feature.dungeons.boss.UmvuthiBoss;
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        super.actuallyHurt(level, source, amount);
        // Teleport to safety when hit
        if (this.teleportCooldown <= 0) {
            if (tryTeleportToSafety()) {
                this.teleportCooldown = TELEPORT_COOLDOWN;
                level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.HOSTILE, 1.0f, 1.2f);
            }
        }
    }

    private boolean tryTeleportToSafety() {
        ServerLevel level = (ServerLevel) this.level();
        for (int attempt = 0; attempt < 20; attempt++) {
            double dx = (this.getRandom().nextDouble() - 0.5) * 16.0;
            double dz = (this.getRandom().nextDouble() - 0.5) * 16.0;
            BlockPos target = BlockPos.containing(this.getX() + dx, this.getY(), this.getZ() + dz);
            // Scan up/down to find solid ground
            for (int dy = -3; dy <= 3; dy++) {
                BlockPos check = target.above(dy);
                if (level.getBlockState(check.below()).isSolid()
                        && level.getBlockState(check).isAir()
                        && level.getBlockState(check.above()).isAir()) {
                    // Check no players within 5 blocks of target
                    List<Player> nearby = level.getEntitiesOfClass(Player.class,
                            new AABB(check).inflate(5.0));
                    if (nearby.isEmpty()) {
                        this.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.25f)
            this.spawnAtLocation(level, new ItemStack(Items.GOLD_NUGGET, 2));
    }

    public static UmvuthanaCraneEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        UmvuthanaCraneEntity crane = new UmvuthanaCraneEntity(DungeonEntityRegistry.UMVUTHANA_CRANE.get(), level);
        crane.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        crane.setYRot(level.getRandom().nextFloat() * 360.0f);
        crane.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) crane);
        return crane;
    }

    protected SoundEvent getAmbientSound() { return SoundEvents.PILLAGER_AMBIENT; }
    protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.ENDERMAN_HURT; }
    protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BASE_HP * this.difficultyMultiplier);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }

    /**
     * AI goal: move toward the lowest-health ally to heal them.
     */
    static class HealAllyGoal extends Goal {
        private final UmvuthanaCraneEntity crane;
        private LivingEntity healTarget;

        HealAllyGoal(UmvuthanaCraneEntity crane) {
            this.crane = crane;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (crane.healCooldown > HEAL_INTERVAL / 2) return false;
            this.healTarget = crane.findHealTarget();
            return this.healTarget != null && crane.distanceTo(healTarget) > 3.0;
        }

        @Override
        public boolean canContinueToUse() {
            return healTarget != null && healTarget.isAlive()
                    && healTarget.getHealth() < healTarget.getMaxHealth()
                    && crane.distanceTo(healTarget) > 2.0;
        }

        @Override
        public void tick() {
            if (healTarget != null) {
                crane.getLookControl().setLookAt(healTarget);
                crane.getNavigation().moveTo(healTarget, 1.0);
            }
        }
    }
}
