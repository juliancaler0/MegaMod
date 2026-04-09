package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import net.minecraft.core.BlockPos;
import com.ultra.megamod.feature.dungeons.entity.MinionEntity;
import com.ultra.megamod.feature.dungeons.entity.SolarBeamEntity;
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

public class UmvuthiBoss extends DungeonBossEntity {
    private static final float BASE_HP = 200.0f;
    private static final float BASE_DAMAGE = 8.0f;
    private int minionSummonCooldown = 0;

    public UmvuthiBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCustomName(this.getBossDisplayName());
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createUmvuthiAttributes() {
        return DungeonBossEntity.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
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
        return Component.literal("Umvuthi, the Mask Lord")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    @Override
    public void onPhaseTransition(int newPhase) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        switch (newPhase) {
            case 2 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                this.summonMinions(serverLevel, 2);
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 0.6f);
            }
            case 3 -> {
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 0, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 999999, 0, false, true));
                this.summonMinions(serverLevel, 2);
                serverLevel.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 2.0f, 0.5f);
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
            case 1 -> performStaffBlast(serverLevel, target);
            case 2 -> performMaskAbility(serverLevel, target);
            case 3 -> performMassSummon(serverLevel, target);
        }
    }

    private void performStaffBlast(ServerLevel level, LivingEntity target) {
        // Spawn SolarBeamEntity for visible beam effect
        SolarBeamEntity.shoot(this.level(), this, target);
        // Apply random debuff directly
        if (target instanceof LivingEntity living) {
            int debuffType = this.getRandom().nextInt(3);
            switch (debuffType) {
                case 0 -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
                case 1 -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, false, true));
                case 2 -> living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 0, false, true));
            }
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5f, 1.0f);
    }

    private void performMaskAbility(ServerLevel level, LivingEntity target) {
        int maskType = this.getRandom().nextInt(6);
        switch (maskType) {
            case 0 -> {
                // Heal self
                this.heal(10.0f);
            }
            case 1 -> {
                // Slowness to all nearby players
                AABB aoe = this.getBoundingBox().inflate(8.0);
                List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, true));
                }
            }
            case 2 -> {
                // Blindness to all nearby players
                AABB aoe = this.getBoundingBox().inflate(8.0);
                List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
                }
            }
            case 3 -> {
                // Wither to all nearby players
                AABB aoe = this.getBoundingBox().inflate(8.0);
                List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true));
                }
            }
            case 4 -> {
                // Mining Fatigue to all nearby players
                AABB aoe = this.getBoundingBox().inflate(8.0);
                List<Player> players = level.getEntitiesOfClass(Player.class, aoe);
                for (Player player : players) {
                    player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 1, false, true));
                }
            }
            case 5 -> {
                // Teleport to random position near boss room center — avoid pits
                double offsetX = (this.getRandom().nextDouble() - 0.5) * 16.0;
                double offsetZ = (this.getRandom().nextDouble() - 0.5) * 16.0;
                BlockPos teleportBlock = BlockPos.containing(
                        this.bossRoomCenter.getX() + offsetX,
                        this.bossRoomCenter.getY(),
                        this.bossRoomCenter.getZ() + offsetZ);
                if (isDangerousDrop(teleportBlock)) {
                    teleportBlock = findSafePosition(teleportBlock);
                }
                this.teleportTo(teleportBlock.getX() + 0.5,
                        teleportBlock.getY(),
                        teleportBlock.getZ() + 0.5);
            }
        }
        // Witch particles around boss
        level.sendParticles((ParticleOptions) ParticleTypes.WITCH,
                this.getX(), this.getY() + 1.5, this.getZ(), 15, 0.8, 0.5, 0.8, 0.05);
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.5f, 0.8f);
    }

    private void performMassSummon(ServerLevel level, LivingEntity target) {
        // Summon 1 minion + one of the other abilities
        if (this.minionSummonCooldown <= 0) {
            this.summonMinions(level, 1);
            this.minionSummonCooldown = 200; // ~10 seconds between summons
        }
        // Alternate between staff blast and mask ability
        if (this.tickCount % 2 == 0) {
            this.performStaffBlast(level, target);
        } else {
            this.performMaskAbility(level, target);
        }
    }

    private static final int MAX_MINIONS = 6;

    private void summonMinions(ServerLevel level, int count) {
        // Cap total minions to prevent exponential growth
        AABB searchBox = this.getBoundingBox().inflate(32.0);
        int currentMinions = level.getEntitiesOfClass(Monster.class, searchBox,
                e -> e != this && e.isAlive() && (
                    e instanceof com.ultra.megamod.feature.dungeons.entity.UmvuthanaEntity ||
                    e instanceof com.ultra.megamod.feature.dungeons.entity.UmvuthanaFollowerEntity ||
                    e instanceof com.ultra.megamod.feature.dungeons.entity.UmvuthanaRaptorEntity ||
                    e instanceof com.ultra.megamod.feature.dungeons.entity.UmvuthanaCraneEntity
                )).size();
        if (currentMinions >= MAX_MINIONS) return;
        count = Math.min(count, MAX_MINIONS - currentMinions);

        DungeonTier tier = DungeonTier.fromLevel((int)(this.difficultyMultiplier));
        for (int i = 0; i < count; i++) {
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 8.0 + (level.getRandom().nextDouble() < 0.5 ? -2.0 : 2.0);
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 8.0 + (level.getRandom().nextDouble() < 0.5 ? -2.0 : 2.0);
            BlockPos spawnPos = BlockPos.containing(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
            // Mix of Umvuthana variants: 50% warriors, 25% followers, 15% raptors, 10% cranes
            float roll = level.getRandom().nextFloat();
            if (roll < 0.10f) {
                com.ultra.megamod.feature.dungeons.entity.UmvuthanaCraneEntity.create(level, tier, spawnPos);
            } else if (roll < 0.25f) {
                com.ultra.megamod.feature.dungeons.entity.UmvuthanaRaptorEntity.create(level, tier, spawnPos);
            } else if (roll < 0.50f) {
                // Spawn follower with no leader (independent fighter)
                com.ultra.megamod.feature.dungeons.entity.UmvuthanaFollowerEntity follower =
                        new com.ultra.megamod.feature.dungeons.entity.UmvuthanaFollowerEntity(
                                DungeonEntityRegistry.UMVUTHANA_FOLLOWER.get(), (Level) level);
                follower.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                follower.setYRot(level.getRandom().nextFloat() * 360.0f);
                follower.applyDungeonScaling(tier);
                level.addFreshEntity((Entity) follower);
            } else {
                com.ultra.megamod.feature.dungeons.entity.UmvuthanaEntity.create(level, tier, spawnPos);
            }
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();
        if (this.minionSummonCooldown > 0) {
            --this.minionSummonCooldown;
        }
        // Ambient enchant particles
        if (this.tickCount % 5 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 1.5, this.getZ(), 3, 0.5, 0.8, 0.5, 0.01);
        }
    }
}
