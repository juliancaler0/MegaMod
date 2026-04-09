package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class LanternEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private static final float BASE_HP = 20.0f;
    private static final float BASE_DAMAGE = 5.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int fireballCooldown = 60;
    private int puffCooldown = 0;

    public LanternEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createLanternAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    protected void registerGoals() {
        // No FloatGoal - this entity flies
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 16.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
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

    public static LanternEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        LanternEntity lantern = new LanternEntity(DungeonEntityRegistry.LANTERN.get(), (Level) level);
        lantern.setPos((double) pos.getX() + 0.5, pos.getY() + 3.0, (double) pos.getZ() + 0.5);
        lantern.setYRot(level.getRandom().nextFloat() * 360.0f);
        lantern.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) lantern);
        return lantern;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.8f) {
            this.spawnAtLocation(level, new ItemStack(Items.GLOWSTONE_DUST, 1 + this.getRandom().nextInt(3)));
        }
        if (this.getRandom().nextFloat() < 0.2f) {
            this.spawnAtLocation(level, new ItemStack(Items.BLAZE_POWDER));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }

        // Hover logic: maintain 3 blocks above ground
        BlockPos groundPos = this.blockPosition();
        while (groundPos.getY() > this.level().getMinY() && this.level().isEmptyBlock(groundPos.below())) {
            groundPos = groundPos.below();
        }
        double targetY = groundPos.getY() + 3.0;
        double currentY = this.getY();
        if (currentY < targetY - 0.5) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.04, 0));
        } else if (currentY > targetY + 0.5) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
        } else {
            // Gentle hovering bob
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }

        // Move toward target slowly
        LivingEntity target = this.getTarget();
        if (target != null) {
            Vec3 direction = target.position().subtract(this.position()).normalize();
            Vec3 currentMotion = this.getDeltaMovement();
            this.setDeltaMovement(currentMotion.x * 0.9 + direction.x * 0.03, currentMotion.y, currentMotion.z * 0.9 + direction.z * 0.03);
        }

        // Puff attack: when player is within 4 blocks, push them away
        if (this.puffCooldown > 0) {
            --this.puffCooldown;
        }
        if (this.puffCooldown <= 0 && target != null && this.distanceTo(target) <= 4.0) {
            AABB puffArea = this.getBoundingBox().inflate(4.0);
            java.util.List<LivingEntity> nearby = ((ServerLevel)this.level()).getEntitiesOfClass(LivingEntity.class, puffArea);
            for (LivingEntity entity : nearby) {
                if (entity == this) continue;
                Vec3 pushDir = entity.position().subtract(this.position()).normalize().scale(1.5);
                entity.push(pushDir.x, 0.4, pushDir.z);
            }
            ((ServerLevel)this.level()).sendParticles((ParticleOptions) ParticleTypes.CLOUD,
                    this.getX(), this.getY(), this.getZ(), 12, 1.0, 0.5, 1.0, 0.1);
            this.level().playSound(null, this.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.HOSTILE, 1.0f, 1.2f);
            this.puffCooldown = 200;
        }

        // Fireball attack
        if (this.fireballCooldown > 0) {
            --this.fireballCooldown;
        }
        if (this.fireballCooldown <= 0 && target != null && this.distanceTo(target) <= 32.0) {
            Vec3 delta = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            SmallFireball fireball = new SmallFireball((Level)this.level(), (LivingEntity)this, delta);
            fireball.setPos(this.getX(), this.getEyeY(), this.getZ());
            this.level().addFreshEntity(fireball);
            this.attackAnimationState.start(this.tickCount);
            this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
            this.fireballCooldown = 60;
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putInt("FireballCooldown", this.fireballCooldown);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.fireballCooldown = input.getIntOr("FireballCooldown", 60);
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
