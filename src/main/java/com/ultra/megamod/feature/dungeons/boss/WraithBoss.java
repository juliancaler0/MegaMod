/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.MinionEntity;
import java.util.List;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WraithBoss
extends DungeonBossEntity {
    private static final float BASE_HP = 200.0f;
    private static final float BASE_DAMAGE = 8.0f;
    private int minionSummonCooldown = 0;

    public WraithBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createWraithAttributes() {
        return DungeonBossEntity.createBossAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.ATTACK_DAMAGE, 8.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.FLYING_SPEED, 0.22).add(Attributes.ARMOR, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new AvoidSpikeBlockGoal((PathfinderMob)this));
        this.goalSelector.addGoal(5, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 24.0f));
        this.goalSelector.addGoal(6, (Goal)new RandomLookAroundGoal((Mob)this));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    @Override
    public int getMaxPhases() {
        return 3;
    }

    @Override
    public float getBaseMaxHealth() {
        return 200.0f;
    }

    @Override
    public float getBaseDamage() {
        return 8.0f;
    }

    @Override
    public Component getBossDisplayName() {
        return Component.literal((String)"The Wraith").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD});
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel)this.level();
        switch (newPhase) {
            case 2: {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.5f, 0.6f);
                break;
            }
            case 3: {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 2, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 2.0f, 0.5f);
            }
        }
    }

    @Override
    public void performAttack(int phase) {
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)this.level();
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        switch (phase) {
            case 1: {
                this.performMagicBolt(serverLevel, target);
                break;
            }
            case 2: {
                this.performShadowPulse(serverLevel);
                break;
            }
            case 3: {
                this.performTeleportStrike(serverLevel, target);
            }
        }
    }

    private void performMagicBolt(ServerLevel level, LivingEntity target) {
        float damage = 8.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().magic();
        target.hurt(source, damage);
        if (target instanceof LivingEntity) {
            LivingEntity living = target;
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
        }
        Vec3 start = this.position().add(0.0, 1.5, 0.0);
        Vec3 end = target.position().add(0.0, 1.0, 0.0);
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        for (int i = 0; i < (int)distance * 3; ++i) {
            double t = (double)i / (distance * 3.0);
            double px = start.x + direction.x * distance * t;
            double py = start.y + direction.y * distance * t;
            double pz = start.z + direction.z * distance * t;
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 1, 0.05, 0.05, 0.05, 0.0);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    private void performShadowPulse(ServerLevel level) {
        float damage = 6.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().magic();
        AABB aoe = this.getBoundingBox().inflate(6.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
        for (Player player : players) {
            player.hurt(source, damage);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, true));
            Vec3 knockback = player.position().subtract(this.position()).normalize().scale(1.5);
            player.push(knockback.x, 0.3, knockback.z);
        }
        for (int i = 0; i < 40; ++i) {
            double angle = Math.PI * 2 * (double)i / 40.0;
            for (int r = 1; r <= 6; ++r) {
                double px = this.getX() + Math.cos(angle) * (double)r;
                double pz = this.getZ() + Math.sin(angle) * (double)r;
                level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, px, this.getY() + 0.5, pz, 1, 0.0, 0.1, 0.0, 0.0);
            }
        }
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.8f, 0.5f);
    }

    private void performTeleportStrike(ServerLevel level, LivingEntity target) {
        Vec3 lookVec = target.getViewVector(1.0f).normalize();
        Vec3 teleportPos = target.position().subtract(lookVec.scale(2.0));
        // Avoid teleporting into a pit
        BlockPos teleportBlock = BlockPos.containing(teleportPos);
        if (isDangerousDrop(teleportBlock)) {
            BlockPos safePos = findSafePosition(teleportBlock);
            teleportPos = Vec3.atCenterOf(safePos);
        }
        this.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        float damage = 12.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().mobAttack((LivingEntity)this);
        target.hurt(source, damage);
        if (target instanceof LivingEntity) {
            LivingEntity living = target;
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, true));
        }
        level.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.5, 1.0, 0.5, 0.1);
        level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.7f);
        if (this.minionSummonCooldown <= 0) {
            this.summonMinions(level, 2);
            this.minionSummonCooldown = 100;
        }
    }

    private void summonMinions(ServerLevel level, int count) {
        for (int i = 0; i < count; ++i) {
            MinionEntity minion = new MinionEntity(DungeonEntityRegistry.MINION.get(), (Level)level);
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 4.0;
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 4.0;
            minion.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
            minion.setYRot(level.getRandom().nextFloat() * 360.0f);
            minion.setParentBossUUID(this.getUUID());
            minion.setDifficultyMultiplier(this.difficultyMultiplier);
            level.addFreshEntity((Entity)minion);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    @Override
    public void tick() {
        LivingEntity target;
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.minionSummonCooldown > 0) {
            --this.minionSummonCooldown;
        }
        if ((target = this.getTarget()) != null && target.isAlive()) {
            Vec3 toTarget = target.position().add(0, 2.0, 0).subtract(this.position());
            Vec3 direction = toTarget.normalize();
            double speed = 0.12;
            double vy = (target.getY() + 2.0 - this.getY()) * 0.05;
            // Steer up over solid blocks in our path
            BlockPos ahead = BlockPos.containing(
                this.getX() + direction.x * 1.5, this.getY(), this.getZ() + direction.z * 1.5);
            if (this.level().getBlockState(ahead).isSolid()) {
                vy = 0.15;
            }
            this.setDeltaMovement(direction.x * speed, vy, direction.z * speed);
        }
        if (this.tickCount % 5 == 0) {
            ServerLevel level = (ServerLevel)this.level();
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.3, 0.5, 0.3, 0.01);
        }
    }
}

