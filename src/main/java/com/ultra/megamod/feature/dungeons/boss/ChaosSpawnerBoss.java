package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.GhostBulletEntity;
import com.ultra.megamod.feature.dungeons.entity.MinionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ChaosSpawnerBoss extends DungeonBossEntity {
    private static final float BASE_HP = 300.0f;
    private static final float BASE_DAMAGE = 10.0f;
    private int minionSummonCooldown = 0;
    private int projectileCooldown = 0;

    public ChaosSpawnerBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createChaosSpawnerAttributes() {
        return DungeonBossEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.22)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new AvoidSpikeBlockGoal((PathfinderMob) this));
        this.goalSelector.addGoal(5, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 24.0f));
        this.goalSelector.addGoal(6, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    @Override
    public int getMaxPhases() {
        return 4;
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
        return Component.literal("The Chaos Spawner")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        switch (newPhase) {
            case 2 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.5f, 0.6f);
            }
            case 3 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.5f);
            }
            case 4 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0f, 0.5f);
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
            case 1 -> performGhostBullet(serverLevel, target);
            case 2 -> performSummonWave(serverLevel, target);
            case 3 -> performVoidPulse(serverLevel, target);
            case 4 -> performChaosBarrage(serverLevel, target);
        }
    }

    private void performGhostBullet(ServerLevel level, LivingEntity target) {
        // Spawn 3 GhostBulletEntity projectiles with slight spread
        for (int i = 0; i < 3; i++) {
            GhostBulletEntity.shoot(this.level(), this, target);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.5f, 0.8f);
    }

    private void performSummonWave(ServerLevel level, LivingEntity target) {
        int count = 3 + this.getRandom().nextInt(2);
        this.summonMinions(level, count);
        // Also do a ghost bullet if target is close
        if (this.distanceToSqr((Entity) target) < 100.0) {
            this.performGhostBullet(level, target);
        }
    }

    private void performVoidPulse(ServerLevel level, LivingEntity target) {
        float damage = 8.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().magic();
        AABB aoe = this.getBoundingBox().inflate(8.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
        for (Player player : players) {
            player.hurt(source, damage);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, true));
            // Knockback away from boss
            Vec3 knockback = player.position().subtract(this.position()).normalize().scale(2.0);
            player.push(knockback.x, 0.3, knockback.z);
        }
        // Expanding ring of reverse portal particles
        for (int ring = 1; ring <= 8; ring++) {
            int particlesInRing = ring * 6;
            for (int i = 0; i < particlesInRing; i++) {
                double angle = Math.PI * 2 * (double) i / particlesInRing;
                double px = this.getX() + Math.cos(angle) * ring;
                double pz = this.getZ() + Math.sin(angle) * ring;
                level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                        px, this.getY() + 1.0, pz, 1, 0.0, 0.1, 0.0, 0.02);
            }
        }
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0f, 0.5f);
    }

    private void performChaosBarrage(ServerLevel level, LivingEntity target) {
        // Ghost bullet + void pulse combined
        this.performGhostBullet(level, target);
        this.performVoidPulse(level, target);
        // Also summon 2 minions if cooldown allows
        if (this.minionSummonCooldown <= 0) {
            this.summonMinions(level, 2);
            this.minionSummonCooldown = 80;
        }
    }

    private void summonMinions(ServerLevel level, int count) {
        for (int i = 0; i < count; i++) {
            MinionEntity minion = new MinionEntity(DungeonEntityRegistry.MINION.get(), (Level) level);
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 6.0;
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 6.0;
            minion.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
            minion.setYRot(level.getRandom().nextFloat() * 360.0f);
            minion.setParentBossUUID(this.getUUID());
            minion.setDifficultyMultiplier(this.difficultyMultiplier);
            level.addFreshEntity((Entity) minion);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 0.8f);
        this.minionSummonCooldown = 60;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();

        // Manage cooldowns
        if (this.minionSummonCooldown > 0) --this.minionSummonCooldown;
        if (this.projectileCooldown > 0) --this.projectileCooldown;

        // Float toward target — hover 3 blocks above
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 toTarget = target.position().add(0, 3.0, 0).subtract(this.position());
            Vec3 direction = toTarget.normalize();
            double speed = 0.10;
            double vy = (target.getY() + 3.0 - this.getY()) * 0.05;
            // Steer up over solid blocks in our path
            BlockPos ahead = BlockPos.containing(
                this.getX() + direction.x * 1.5, this.getY(), this.getZ() + direction.z * 1.5);
            if (this.level().getBlockState(ahead).isSolid()) {
                vy = 0.15;
            }
            this.setDeltaMovement(direction.x * speed, vy, direction.z * speed);
        }

        // Ambient reverse portal particles every 3 ticks
        if (this.tickCount % 3 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.4, 0.6, 0.4, 0.01);
        }

        // Phase 3+ extra end rod particles every 30 ticks
        if (this.currentPhase >= 3 && this.tickCount % 30 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1.5, this.getZ(), 8, 1.0, 1.0, 1.0, 0.05);
        }
    }
}
