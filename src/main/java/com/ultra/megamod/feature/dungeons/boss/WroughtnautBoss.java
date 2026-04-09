package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
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

public class WroughtnautBoss extends DungeonBossEntity {
    private static final float BASE_HP = 350.0f;
    private static final float BASE_DAMAGE = 14.0f;

    public WroughtnautBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createWroughtnautAttributes() {
        return DungeonBossEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 350.0)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
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
        return Component.literal("The Wroughtnaut")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        switch (newPhase) {
            case 2 -> {
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 2.0f, 0.6f);
                serverLevel.sendParticles((ParticleOptions) ParticleTypes.LAVA,
                        this.getX(), this.getY() + 1.0, this.getZ(), 15, 1.0, 0.5, 1.0, 0.1);
            }
            case 3 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0, false, true));
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.5f);
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
            case 1 -> performAxeSlam(serverLevel, target);
            case 2 -> performCharge(serverLevel, target);
            case 3 -> performBerserkStrike(serverLevel, target);
        }
    }

    private void performAxeSlam(ServerLevel level, LivingEntity target) {
        float damage = 14.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().mobAttack((LivingEntity) this);
        AABB aoe = this.getBoundingBox().inflate(5.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
        for (Player player : players) {
            player.hurt(source, damage);
            player.push(0.0, 1.0, 0.0);
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true));
        }
        // Explosion particles at foot level
        level.sendParticles((ParticleOptions) ParticleTypes.EXPLOSION,
                this.getX(), this.getY() + 0.5, this.getZ(), 5, 2.0, 0.2, 2.0, 0.0);
        level.playSound(null, this.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 2.0f, 0.7f);
    }

    private void performCharge(ServerLevel level, LivingEntity target) {
        Vec3 direction = target.position().subtract(this.position()).normalize();
        // Check for dangerous drops in the charge path — fall back to slam if pit ahead
        BlockPos aheadPos = BlockPos.containing(
            this.getX() + direction.x * 3.0, this.getY(), this.getZ() + direction.z * 3.0);
        if (isDangerousDrop(aheadPos)) {
            // Fall back to axe slam instead
            performAxeSlam(level, target);
            return;
        }
        this.setDeltaMovement(direction.x * 1.5, 0.1, direction.z * 1.5);
        // Damage entities near charge path
        AABB chargePath = this.getBoundingBox().inflate(2.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, chargePath);
        float damage = 10.0f * this.getDamageMultiplier();
        for (Player player : players) {
            player.hurt(this.damageSources().mobAttack((LivingEntity) this), damage);
        }
        // Flame particles along path
        for (int i = 0; i < 10; i++) {
            double t = (double) i / 10.0;
            double px = this.getX() + direction.x * t * 3.0;
            double pz = this.getZ() + direction.z * t * 3.0;
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    px, this.getY() + 0.5, pz, 2, 0.1, 0.1, 0.1, 0.02);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.5f, 0.7f);
    }

    private void performBerserkStrike(ServerLevel level, LivingEntity target) {
        // Teleport behind target — avoid spike blocks
        Vec3 lookVec = target.getViewVector(1.0f).normalize();
        Vec3 teleportPos = target.position().subtract(lookVec.scale(2.0));
        BlockPos teleportBlock = BlockPos.containing(teleportPos);
        if (isDangerousDrop(teleportBlock)) {
            BlockPos safePos = findSafePosition(teleportBlock);
            teleportPos = Vec3.atCenterOf(safePos);
        }
        this.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        float damage = 18.0f * this.getDamageMultiplier();
        DamageSource source = this.damageSources().mobAttack((LivingEntity) this);
        target.hurt(source, damage);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0, false, true));
        }
        // Lava + flame particles at strike location
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA,
                this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0f, 0.7f);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Directional damage: front hits deal only 25% damage
        if (source.getEntity() instanceof LivingEntity attacker) {
            Vec3 facing = this.getViewVector(1.0f).normalize();
            Vec3 toAttacker = attacker.position().subtract(this.position()).normalize();
            double dot = facing.dot(new Vec3(toAttacker.x, 0, toAttacker.z).normalize());
            if (dot > 0.5) {
                // Attacker is in front — reduce damage to 25%
                amount *= 0.25f;
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();
        // Ambient flame particles
        if (this.tickCount % 10 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.3, 0.5, 0.3, 0.01);
        }
        // Phase 2+: set nearby air blocks to fire occasionally
        if (this.currentPhase >= 2 && this.tickCount % 40 == 0) {
            BlockPos center = this.blockPosition();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dz * dz > 9) continue;
                    BlockPos pos = center.offset(dx, 0, dz);
                    if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
