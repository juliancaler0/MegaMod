package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GrottolEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState fleeAnimationState = new AnimationState();

    private static final float BASE_HP = 25.0f;
    private static final float BASE_DAMAGE = 3.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private boolean fleeing = false;
    private int burrowTicks = 0;
    private boolean burrowing = false;

    public GrottolEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public boolean isFleeing() {
        return this.fleeing;
    }

    public static AttributeSupplier.Builder createGrottolAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new AvoidEntityGoal<>((PathfinderMob) this, Player.class, 12.0f, 1.0, 1.5));
        this.goalSelector.addGoal(2, (Goal) new PanicGoal((PathfinderMob) this, 1.5));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 1.0));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        // No target selector goals - passive until hit
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.fleeing) {
            this.fleeing = true;
            this.fleeAnimationState.start(this.tickCount);
            this.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, false));
            level.playSound(null, this.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.0f, 1.5f);
            // Burrow escape: sink into ground and teleport away
            if (!this.burrowing) {
                this.burrowing = true;
                this.burrowTicks = 16;
                this.setInvisible(true);
                this.setInvulnerable(true);
            }
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(8.0 + (double) this.tierLevel);
        tier.applyMobEffects(this);
    }

    public static GrottolEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        GrottolEntity grottol = new GrottolEntity(DungeonEntityRegistry.GROTTOL.get(), (Level) level);
        grottol.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        grottol.setYRot(level.getRandom().nextFloat() * 360.0f);
        grottol.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) grottol);
        return grottol;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int bonusCount = Math.max(0, this.tierLevel - 1);
        if (this.getRandom().nextFloat() < 0.6f) {
            this.spawnAtLocation(level, new ItemStack(Items.DIAMOND, 1 + bonusCount));
        }
        if (this.getRandom().nextFloat() < 0.3f) {
            this.spawnAtLocation(level, new ItemStack(Items.GOLD_INGOT, 1 + bonusCount));
        }
        if (this.getRandom().nextFloat() < 0.1f) {
            this.spawnAtLocation(level, new ItemStack(Items.EMERALD, 1 + bonusCount));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
        // Burrow escape logic
        if (this.burrowing && this.burrowTicks > 0) {
            --this.burrowTicks;
            // Stone particles while burrowing
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX(), this.getY(), this.getZ(), 3, 0.3, 0.1, 0.3, 0.02);
            }
            if (this.burrowTicks == 0) {
                // Teleport 8-12 blocks away
                double angle = this.getRandom().nextDouble() * Math.PI * 2;
                double dist = 8.0 + this.getRandom().nextDouble() * 4.0;
                double newX = this.getX() + Math.cos(angle) * dist;
                double newZ = this.getZ() + Math.sin(angle) * dist;
                this.teleportTo(newX, this.getY(), newZ);
                this.setInvisible(false);
                this.setInvulnerable(false);
                this.burrowing = false;
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            this.getX(), this.getY(), this.getZ(), 10, 0.5, 0.3, 0.5, 0.05);
                    sl.playSound(null, this.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.STONE_STEP;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_BREAK;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putBoolean("Fleeing", this.fleeing);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.fleeing = input.getBooleanOr("Fleeing", false);
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(8.0 + (double) this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
