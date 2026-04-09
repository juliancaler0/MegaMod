package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.entity.CrystallineBeamEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.entity.HollowEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DungeonKeeperBoss extends DungeonBossEntity {
    private static final float BASE_HP = 250.0f;
    private static final float BASE_DAMAGE = 10.0f;
    private int barrageCooldown = 0;
    private int hollowSummonCooldown = 0;

    public DungeonKeeperBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal) new FloatGoal((Mob) this));
        this.goalSelector.addGoal(1, (Goal) new AvoidSpikeBlockGoal((PathfinderMob) this));
        this.goalSelector.addGoal(2, (Goal) new MeleeAttackGoal((PathfinderMob) this, 1.0, true));
        this.goalSelector.addGoal(5, (Goal) new WaterAvoidingRandomStrollGoal((PathfinderMob) this, 0.8));
        this.goalSelector.addGoal(6, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 16.0f));
        this.goalSelector.addGoal(7, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, true));
    }

    public static AttributeSupplier.Builder createKeeperAttributes() {
        return DungeonBossEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
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
        return Component.literal("The Dungeon Keeper")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD);
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        switch (newPhase) {
            case 2 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 2.0f, 0.6f);
            }
            case 3 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0f, 0.5f);
                // Summon initial Hollows on entering phase 3
                this.summonHollows(serverLevel, 2);
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
            case 1 -> performMeleeSwing(serverLevel, target);
            case 2 -> performBarrage(serverLevel, target);
            case 3 -> performCombinedAssault(serverLevel, target);
        }
    }

    private void performMeleeSwing(ServerLevel level, LivingEntity target) {
        double dist = this.distanceToSqr((Entity) target);
        if (dist > 12.0) return;
        float damage = BASE_DAMAGE * this.getDamageMultiplier();
        DamageSource source = this.damageSources().mobAttack(this);
        target.hurt(source, damage);
        Vec3 knockback = target.position().subtract(this.position()).normalize().scale(1.2);
        target.push(knockback.x, 0.3, knockback.z);
        level.sendParticles((ParticleOptions) ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + 1.0, target.getZ(), 1, 0.5, 0.3, 0.5, 0.0);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5f, 0.7f);
    }

    private void performBarrage(ServerLevel level, LivingEntity target) {
        if (this.barrageCooldown > 0) {
            // Fall back to melee if barrage on cooldown
            performMeleeSwing(level, target);
            return;
        }
        // Fire 3 Crystalline Beam projectiles in a spread
        Vec3 baseDir = target.position().add(0, target.getBbHeight() / 2.0, 0)
                .subtract(this.position().add(0, this.getBbHeight() / 2.0, 0)).normalize();
        for (int i = -1; i <= 1; i++) {
            double spread = i * 0.3;
            Vec3 spreadDir = baseDir.add(spread, 0, spread).normalize();
            CrystallineBeamEntity beam = new CrystallineBeamEntity(level, this, target);
            beam.setDeltaMovement(spreadDir.scale(0.5));
            level.addFreshEntity(beam);
        }
        this.barrageCooldown = 40;
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5f, 1.0f);
    }

    private void performCombinedAssault(ServerLevel level, LivingEntity target) {
        double dist = this.distanceToSqr((Entity) target);
        if (dist < 9.0) {
            // Close range: melee + knockback
            performMeleeSwing(level, target);
        }
        // Always fire barrage in phase 3
        if (this.barrageCooldown <= 0) {
            performBarrage(level, target);
        }
        // Summon Hollows periodically
        if (this.hollowSummonCooldown <= 0) {
            this.summonHollows(level, 2);
            this.hollowSummonCooldown = 200;
        }
    }

    private void summonHollows(ServerLevel level, int count) {
        for (int i = 0; i < count; i++) {
            HollowEntity hollow = new HollowEntity(DungeonEntityRegistry.HOLLOW.get(), (Level) level);
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 6.0;
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 6.0;
            hollow.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
            hollow.setYRot(level.getRandom().nextFloat() * 360.0f);
            // Scale hollows to match dungeon tier
            if (this.difficultyMultiplier > 1.0f) {
                float scaledHP = 15.0f * this.difficultyMultiplier * 0.5f;
                hollow.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
                hollow.setHealth(scaledHP);
            }
            level.addFreshEntity((Entity) hollow);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();
        if (this.barrageCooldown > 0) --this.barrageCooldown;
        if (this.hollowSummonCooldown > 0) --this.hollowSummonCooldown;

        // Phase 3 ambient particles
        if (this.currentPhase == 3 && this.tickCount % 5 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.5, 1.0, 0.5, 0.02);
        }
        // Phase 2+ ambient particles
        if (this.currentPhase >= 2 && this.tickCount % 10 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 0.5, this.getZ(), 5, 0.8, 0.5, 0.8, 0.01);
        }
    }
}
