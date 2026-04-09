package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SculptorBoss extends DungeonBossEntity {
    private static final float BASE_HP = 250.0f;
    private static final float BASE_DAMAGE = 12.0f;

    public SculptorBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createSculptorAttributes() {
        return DungeonBossEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new AvoidSpikeBlockGoal((PathfinderMob) this));
        this.goalSelector.addGoal(4, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.0, false));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 24.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    @Override
    public int getMaxPhases() {
        return 3;
    }

    @Override
    public float getBaseMaxHealth() {
        return BASE_HP;
    }

    @Override
    public float getBaseDamage() {
        return BASE_DAMAGE;
    }

    @Override
    public Component getBossDisplayName() {
        return Component.literal("The Sculptor")
                .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD);
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        switch (newPhase) {
            case 2 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 2.0f, 0.6f);
                // Stone burst particles
                serverLevel.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX(), this.getY() + 1.0, this.getZ(), 20, 1.5, 1.0, 1.5, 0.05);
            }
            case 3 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.5f);
            }
        }
    }

    @Override
    public void performAttack(int phase) {
        if (this.level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) this.level();
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        switch (phase) {
            case 1 -> performStonePillar(serverLevel, target);
            case 2 -> performBoulderThrow(serverLevel, target);
            case 3 -> performGroundFissure(serverLevel, target);
        }
    }

    private void performStonePillar(ServerLevel level, LivingEntity target) {
        BlockPos targetPos = target.blockPosition();
        // 3x3 stone pillars under player, 3 blocks high
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy < 3; dy++) {
                    BlockPos pos = targetPos.offset(dx, dy, dz);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                    }
                }
            }
        }
        // Damage and launch nearby
        float damage = 8.0f * this.getDamageMultiplier();
        AABB area = new AABB(targetPos).inflate(2.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.hurt(this.damageSources().mobAttack(this), damage);
            player.push(0, 0.8, 0);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true));
        }
        level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                targetPos.getX() + 0.5, targetPos.getY() + 1.5, targetPos.getZ() + 0.5, 15, 1.0, 1.0, 1.0, 0.05);
        level.playSound(null, targetPos, SoundEvents.STONE_PLACE, SoundSource.HOSTILE, 2.0f, 0.6f);
    }

    private void performBoulderThrow(ServerLevel level, LivingEntity target) {
        // Projectile-like damage (direct)
        float damage = 10.0f * this.getDamageMultiplier();
        target.hurt(this.damageSources().mobAttack(this), damage);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true));
        // Particle trail
        Vec3 start = this.position().add(0, 1.5, 0);
        Vec3 end = target.position().add(0, 1.0, 0);
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        for (int i = 0; i < (int) distance * 3; i++) {
            double t = (double) i / (distance * 3.0);
            double px = start.x + direction.x * distance * t;
            double py = start.y + direction.y * distance * t;
            double pz = start.z + direction.z * distance * t;
            level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 1, 0.1, 0.1, 0.1, 0.0);
        }
        // Earth spikes: AOE damage
        float spikeDmg = 6.0f * this.getDamageMultiplier();
        AABB spikeArea = this.getBoundingBox().inflate(6.0);
        for (Player player : level.getEntitiesOfClass(Player.class, spikeArea)) {
            player.hurt(this.damageSources().mobAttack(this), spikeDmg);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 1.5f, 0.7f);
    }

    private void performGroundFissure(ServerLevel level, LivingEntity target) {
        // Big AOE: 12-block radius damage
        float damage = 7.0f * this.getDamageMultiplier();
        AABB aoe = this.getBoundingBox().inflate(12.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
        for (Player player : players) {
            player.hurt(this.damageSources().mobAttack(this), damage);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 0, false, true));
        }
        // Fire line toward target
        Vec3 dir = target.position().subtract(this.position()).normalize();
        for (int i = 1; i <= 10; i++) {
            BlockPos linePos = this.blockPosition().offset((int) (dir.x * i), 0, (int) (dir.z * i));
            if (level.getBlockState(linePos).isAir() && !level.getBlockState(linePos.below()).isAir()) {
                level.setBlock(linePos, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
        // Massive particle burst
        for (int i = 0; i < 40; i++) {
            double theta = Math.PI * 2 * this.getRandom().nextDouble();
            double r = 2.0 + this.getRandom().nextDouble() * 10.0;
            double px = this.getX() + r * Math.cos(theta);
            double pz = this.getZ() + r * Math.sin(theta);
            level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE, px, this.getY() + 0.5, pz, 1, 0.1, 0.2, 0.1, 0.02);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.5f);
        // Also do stone pillar attack
        performStonePillar(level, target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();
        // Ambient stone particles
        if (this.tickCount % 8 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.3, 0.5, 0.3, 0.01);
        }
    }
}
