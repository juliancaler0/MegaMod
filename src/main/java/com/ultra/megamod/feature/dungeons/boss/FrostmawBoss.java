package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.IceBallEntity;
import com.ultra.megamod.feature.dungeons.entity.MinionEntity;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FrostmawBoss extends DungeonBossEntity {
    private static final float BASE_HP = 250.0f;
    private static final float BASE_DAMAGE = 10.0f;

    public FrostmawBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createFrostmawAttributes() {
        return DungeonBossEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
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
        return Component.literal("Frostmaw")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        switch (newPhase) {
            case 2 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.5f, 0.6f);
            }
            case 3 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 2.0f, 0.5f);
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
            case 1 -> performIceBreath(serverLevel, target);
            case 2 -> performIceBall(serverLevel, target);
            case 3 -> performBlizzard(serverLevel, target);
        }
    }

    private void performIceBreath(ServerLevel level, LivingEntity target) {
        float damage = 8.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().magic();
        Vec3 lookDir = this.getViewVector(1.0f).normalize();
        AABB coneArea = this.getBoundingBox().inflate(6.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, coneArea);
        for (Player player : players) {
            Vec3 toPlayer = player.position().subtract(this.position()).normalize();
            double dot = lookDir.dot(toPlayer);
            if (dot > 0.5 && this.distanceToSqr((Entity) player) < 36.0) {
                player.hurt(source, damage);
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 0, false, true));
            }
        }
        // Cone particle effect
        for (int i = 0; i < 30; i++) {
            double dist = 1.0 + (i / 5.0);
            double spread = dist * 0.3;
            double px = this.getX() + lookDir.x * dist + (this.getRandom().nextDouble() - 0.5) * spread;
            double py = this.getY() + 1.5 + (this.getRandom().nextDouble() - 0.5) * spread;
            double pz = this.getZ() + lookDir.z * dist + (this.getRandom().nextDouble() - 0.5) * spread;
            level.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, px, py, pz, 1, 0.05, 0.05, 0.05, 0.0);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5f, 0.8f);
    }

    private void performIceBall(ServerLevel level, LivingEntity target) {
        // Spawn IceBallEntity projectile instead of direct damage
        IceBallEntity.shoot(this.level(), this, target);
        level.playSound(null, this.blockPosition(), SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.HOSTILE, 1.5f, 0.8f);
    }

    private void performBlizzard(ServerLevel level, LivingEntity target) {
        float damage = 5.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().magic();
        AABB aoe = this.getBoundingBox().inflate(10.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
        for (Player player : players) {
            player.hurt(source, damage);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
        }
        // Massive snowflake sphere burst
        for (int i = 0; i < 50; i++) {
            double theta = Math.PI * 2 * this.getRandom().nextDouble();
            double phi = Math.acos(2.0 * this.getRandom().nextDouble() - 1.0);
            double r = 3.0 + this.getRandom().nextDouble() * 7.0;
            double px = this.getX() + r * Math.sin(phi) * Math.cos(theta);
            double py = this.getY() + 1.0 + r * Math.sin(phi) * Math.sin(theta);
            double pz = this.getZ() + r * Math.cos(phi);
            level.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, px, py, pz, 1, 0.1, 0.1, 0.1, 0.02);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.6f);
        // Freeze ground in 5-block radius
        BlockPos center = this.blockPosition();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (dx * dx + dz * dz > 25) continue;
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.PACKED_ICE.defaultBlockState(), 3);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();
        // Ambient snowflake particles
        if (this.tickCount % 5 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.5, this.getZ(), 3, 0.5, 1.0, 0.5, 0.01);
        }
    }
}
