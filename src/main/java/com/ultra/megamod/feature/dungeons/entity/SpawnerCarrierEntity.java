package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;

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

public class SpawnerCarrierEntity extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState spawnAnimationState = new AnimationState();

    private static final float BASE_HP = 50.0f;
    private static final float BASE_DAMAGE = 4.0f;

    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int spawnCooldown = 200;
    private int totalSpawned = 0;
    private static final int MAX_SPAWNS = 8;

    public SpawnerCarrierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createSpawnerCarrierAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.6));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 12.0f));
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
        this.getAttribute(Attributes.ARMOR).setBaseValue(6.0 + (double) this.tierLevel);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(2.0 + (double) this.tierLevel * 0.5);
        tier.applyMobEffects(this);
    }

    public static SpawnerCarrierEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        SpawnerCarrierEntity carrier = new SpawnerCarrierEntity(DungeonEntityRegistry.SPAWNER_CARRIER.get(), (Level) level);
        carrier.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        carrier.setYRot(level.getRandom().nextFloat() * 360.0f);
        carrier.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) carrier);
        return carrier;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // Drop 2-3 experience bottles
        int count = 2 + this.getRandom().nextInt(2);
        this.spawnAtLocation(level, new ItemStack(Items.EXPERIENCE_BOTTLE, count));
    }

    @Override
    public void die(DamageSource source) {
        // Play explosion sound + particles on death
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
        }
        super.die(source);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }

        // Spawn mob logic
        if (this.spawnCooldown > 0) {
            --this.spawnCooldown;
        }
        if (this.spawnCooldown <= 0 && this.totalSpawned < MAX_SPAWNS) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 4.0;

            // Spawn Baby Zombies or Cave Spiders (like a real spawner)
            net.minecraft.world.entity.Mob spawnedMob;
            if (this.getRandom().nextBoolean()) {
                // Baby Zombie
                spawnedMob = (net.minecraft.world.entity.Mob) EntityType.ZOMBIE.create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                if (spawnedMob != null) {
                    spawnedMob.setBaby(true);
                    spawnedMob.setPersistenceRequired();
                }
            } else {
                // Cave Spider
                spawnedMob = (net.minecraft.world.entity.Mob) EntityType.CAVE_SPIDER.create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
                if (spawnedMob != null) {
                    spawnedMob.setPersistenceRequired();
                }
            }
            if (spawnedMob != null) {
                spawnedMob.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                spawnedMob.setYRot(this.getRandom().nextFloat() * 360.0f);
                // Scale HP/damage with tier
                float mult = DungeonTier.fromLevel(this.tierLevel).getDifficultyMultiplier();
                if (spawnedMob.getAttribute(Attributes.MAX_HEALTH) != null) {
                    double baseHP = spawnedMob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
                    spawnedMob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * mult);
                    spawnedMob.setHealth((float) (baseHP * mult));
                }
                if (spawnedMob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                    double baseDmg = spawnedMob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
                    spawnedMob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * mult);
                }
                serverLevel.addFreshEntity((Entity) spawnedMob);
            }

            this.spawnCooldown = 200;
            this.totalSpawned++;
            this.spawnAnimationState.start(this.tickCount);

            // Play spawner sound effect
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.HOSTILE, 0.8f, 1.2f);
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ANVIL_LAND;
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
        output.putInt("SpawnCooldown", this.spawnCooldown);
        output.putInt("TotalSpawned", this.totalSpawned);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.spawnCooldown = input.getIntOr("SpawnCooldown", 200);
        this.totalSpawned = input.getIntOr("TotalSpawned", 0);
        float scaledHP = BASE_HP * this.difficultyMultiplier;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
        float scaledDamage = BASE_DAMAGE * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(6.0 + (double) this.tierLevel);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(2.0 + (double) this.tierLevel * 0.5);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
