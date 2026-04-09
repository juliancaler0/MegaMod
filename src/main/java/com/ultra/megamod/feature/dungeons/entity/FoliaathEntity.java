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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FoliaathEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState emergeAnimationState = new AnimationState();

    private static final float BASE_HP = 30.0f;
    private static final float BASE_DAMAGE = 9.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private boolean dormant = true;

    public FoliaathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public boolean isDormant() {
        return this.dormant;
    }

    public static AttributeSupplier.Builder createFoliaathAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.0, false));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 6.0f));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    private void wakeUp() {
        if (this.dormant) {
            this.dormant = false;
            this.setNoAi(false);
            this.setInvisible(false);
            if (this.level().isClientSide()) {
                this.emergeAnimationState.start(this.tickCount);
            }
            if (!this.level().isClientSide()) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 1.0f, 1.2f);
                // Spore burst particles on wake
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles((ParticleOptions) ParticleTypes.SPORE_BLOSSOM_AIR,
                            this.getX(), this.getY() + 0.5, this.getZ(), 20, 1.0, 0.5, 1.0, 0.05);
                    sl.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + 0.5, this.getZ(), 10, 0.8, 0.4, 0.8, 0.02);
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Apply Poison I for 60 ticks on hit
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Wake up on damage
        wakeUp();
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + (double) this.tierLevel);
        tier.applyMobEffects(this);
    }

    public static FoliaathEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        FoliaathEntity foliaath = new FoliaathEntity(DungeonEntityRegistry.FOLIAATH.get(), (Level) level);
        foliaath.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        foliaath.setYRot(level.getRandom().nextFloat() * 360.0f);
        foliaath.applyDungeonScaling(tier);
        // Start dormant with AI disabled and invisible
        foliaath.setNoAi(true);
        foliaath.setInvisible(true);
        level.addFreshEntity((Entity) foliaath);
        return foliaath;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getRandom().nextFloat() < 0.5f) {
            this.spawnAtLocation(level, new ItemStack(Items.VINE, 1 + this.getRandom().nextInt(3)));
        }
        if (this.getRandom().nextFloat() < 0.25f) {
            this.spawnAtLocation(level, new ItemStack(Items.POISONOUS_POTATO));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (!this.dormant) {
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
            return;
        }

        // Server-side: check for nearby players to wake up
        if (this.dormant) {
            Player nearest = this.level().getNearestPlayer(this, 6.0);
            if (nearest != null) {
                wakeUp();
            }
        }
    }

    protected SoundEvent getAmbientSound() {
        return this.dormant ? null : SoundEvents.GRASS_STEP;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SWEET_BERRY_BUSH_BREAK;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SWEET_BERRY_BUSH_BREAK;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putBoolean("Dormant", this.dormant);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.dormant = input.getBooleanOr("Dormant", true);
        if (this.dormant) {
            this.setNoAi(true);
        }
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(2.0 + (double) this.tierLevel);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
