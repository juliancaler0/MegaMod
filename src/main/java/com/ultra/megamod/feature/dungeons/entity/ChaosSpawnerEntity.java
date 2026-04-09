package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * ChaosSpawner gatekeeper — faithful port of DNL's ChaosSpawner boss.
 * Stationary floating boss with weighted attack selection, 2 phases,
 * ghost bullet patterns, AoE push, and vanilla mob summoning.
 * Players must defeat it before the DungeonAltar appears.
 */
public class ChaosSpawnerEntity extends Monster {

    public enum State { SLEEPING, AWAKENING, IDLE, PUSH, SHOOT_SINGLE, SHOOT_BURST, SUMMON, DEAD }

    // --- DNL-matching stats ---
    private static final float BASE_HP = 300.0f;
    private static final float BASE_DAMAGE = 20.0f;
    private static final double WAKE_RANGE = 15.0;
    private static final int AWAKENING_TICKS = 160; // 8 seconds
    private static final double FOLLOW_DISTANCE = 35.0;

    private State state = State.SLEEPING;
    private int phase = 0; // 0=not started, 1=phase1, 2=phase2
    private int awakeningTimer = 0;
    private int attackTick = 0;
    private int contactDamageCooldown = 0;
    private int deathAnimTicks = 0;
    private float difficultyMultiplier = 1.0f;
    private String dungeonInstanceId = "";
    private BlockPos spawnPoint = BlockPos.ZERO;
    private String bulletPattern = "";

    private final ServerBossEvent bossEvent;

    // --- Summon positions (DNL pattern: ring around boss) ---
    private static final BlockPos[] SUMMON_OFFSETS = {
            new BlockPos(5, 1, 0), new BlockPos(-5, 1, 0),
            new BlockPos(0, 1, 5), new BlockPos(0, 1, -5),
            new BlockPos(5, 1, 2), new BlockPos(-5, 1, 2),
            new BlockPos(2, 1, 5), new BlockPos(2, 1, -5),
            new BlockPos(5, 1, -2), new BlockPos(-5, 1, -2),
            new BlockPos(-2, 1, 5), new BlockPos(-2, 1, -5)
    };

    public ChaosSpawnerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.bossEvent = new ServerBossEvent(
                Component.literal("Chaos Spawner").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setPlayBossMusic(true);
        this.bossEvent.setDarkenScreen(true);
    }

    public static AttributeSupplier.Builder createGatekeeperAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HP)
                .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FLYING_SPEED, 0.23)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_DISTANCE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, (Goal) new LookAtPlayerGoal((Mob) this, Player.class, 30.0f));
        this.goalSelector.addGoal(8, (Goal) new RandomLookAroundGoal((Mob) this));
        this.targetSelector.addGoal(1, (Goal) new HurtByTargetGoal((PathfinderMob) this, DungeonBossEntity.class));
        // mustSee=false — the gatekeeper is surrounded by cage blocks and can't see through them
        this.targetSelector.addGoal(2, (Goal) new NearestAttackableTargetGoal((Mob) this, Player.class, false));
    }

    // --- Stationary: no gravity, no movement (DNL behavior) ---

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public Vec3 getDeltaMovement() { return Vec3.ZERO; }

    // --- Setters/Getters ---

    public void setState(State state) { this.state = state; }
    public State getState() { return this.state; }

    public void setDifficultyMultiplier(float mult) {
        this.difficultyMultiplier = mult;
        float scaledHP = BASE_HP * mult;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        this.setHealth(scaledHP);
        float scaledDamage = BASE_DAMAGE * mult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) scaledDamage);
    }

    public void setDungeonInstanceId(String instanceId) { this.dungeonInstanceId = instanceId; }
    public void setSpawnPoint(BlockPos pos) { this.spawnPoint = pos; }
    public float getAttackDamage() { return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); }

    // --- Invulnerability during SLEEPING / AWAKENING ---

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.state == State.SLEEPING || this.state == State.AWAKENING) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    // --- Boss bar visibility ---

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
        if (this.phase == 0) {
            this.bossEvent.setVisible(false);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    // --- Main tick ---

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level();

        if (this.state == State.SLEEPING) {
            tickSleeping(level);
            return;
        }

        if (this.state == State.AWAKENING) {
            tickAwakening(level);
            return;
        }

        if (this.state == State.DEAD) {
            this.deathAnimTicks++;
            if (this.deathAnimTicks >= 160) {
                this.remove(RemovalReason.KILLED);
            }
            return;
        }

        // Active combat (IDLE or attacking)
        if (this.phase > 0 && this.state != State.DEAD) {
            // Smoothly rotate body toward target (like normal mobs)
            LivingEntity target = this.getTarget();
            if (target != null) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
                // Smooth rotation — turn up to 10 degrees per tick
                float currentYaw = this.getYRot();
                float diff = net.minecraft.util.Mth.wrapDegrees(targetYaw - currentYaw);
                float step = net.minecraft.util.Mth.clamp(diff, -10.0f, 10.0f);
                float newYaw = currentYaw + step;
                this.setYRot(newYaw);
                this.yBodyRot = newYaw;
                this.yHeadRot = newYaw;
            }

            tickAbilitySelection(level);
            tickContactDamage(level);
            tickPhaseUpdate();
            tickAttack(level);
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    // --- SLEEPING ---

    private void tickSleeping(ServerLevel level) {
        if (this.tickCount % 10 == 0) {
            level.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.3, 0.5, 0.3, 0.01);
        }
        AABB wakeBox = this.getBoundingBox().inflate(WAKE_RANGE);
        List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, wakeBox);
        if (!nearby.isEmpty()) {
            this.state = State.AWAKENING;
            this.awakeningTimer = AWAKENING_TICKS;
            for (ServerPlayer player : nearby) {
                player.sendSystemMessage(Component.literal("The Chaos Spawner stirs...")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
            level.playSound(null, this.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 3.0f, 1.0f);
        }
    }

    // --- AWAKENING (160 ticks / 8 seconds, matching DNL) ---

    private void tickAwakening(ServerLevel level) {
        --this.awakeningTimer;

        // Chain break sound at tick 99
        if (this.awakeningTimer == 99) {
            level.playSound(null, this.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 3.0f, 1.0f);
        }
        // Laughter at tick 60
        if (this.awakeningTimer == 60) {
            level.playSound(null, this.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 3.0f, 0.5f);
        }

        // Intensifying particles
        if (this.tickCount % 3 == 0) {
            int count = (AWAKENING_TICKS - this.awakeningTimer) / 10 + 1;
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2 * (double) i / count + this.tickCount * 0.1;
                double px = this.getX() + Math.cos(angle) * 1.5;
                double pz = this.getZ() + Math.sin(angle) * 1.5;
                level.sendParticles((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME,
                        px, this.getY() + 1.0, pz, 1, 0.0, 0.1, 0.0, 0.01);
            }
        }

        // Boss bar progress during awakening
        this.bossEvent.setVisible(true);
        this.bossEvent.setProgress(1.0f - (float) this.awakeningTimer / AWAKENING_TICKS);

        if (this.awakeningTimer <= 0) {
            this.phase = 1;
            this.state = State.IDLE;
            level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 0.7f);

            // Burst particles
            for (int i = 0; i < 50; i++) {
                level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                        this.getRandomX(0.9), this.getRandomY(), this.getRandomZ(0.9),
                        1, 0.0, 0.0, 0.0, 0.0);
            }

            // Clear cage barrier blocks around the spawner so players can reach it
            clearCageBlocks(level);

            AABB range = this.getBoundingBox().inflate(FOLLOW_DISTANCE);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, range)) {
                player.sendSystemMessage(Component.literal("The Chaos Spawner awakens!")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            }
        }
    }

    /**
     * Destroy the chaos_spawner barrier blocks surrounding the entity so players can engage.
     * Scans a 5-block radius and removes any dungeonnowloading:chaos_spawner_barrier_* blocks.
     */
    private void clearCageBlocks(ServerLevel level) {
        BlockPos center = this.blockPosition();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    net.minecraft.world.level.block.Block block = level.getBlockState(pos).getBlock();
                    String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
                    if (id.startsWith("dungeonnowloading:chaos_spawner_barrier")) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
        level.playSound(null, center, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0f, 0.5f);
    }

    /**
     * Destroy all chaos_spawner structural frame blocks (edges, vertices, base blocks)
     * so the boss spawned from the altar can move freely. Called on ChaosSpawner death.
     * Scans a 7-block radius and removes any dungeonnowloading:chaos_spawner* blocks.
     */
    private void clearFrameBlocks(ServerLevel level) {
        BlockPos center = this.spawnPoint.equals(BlockPos.ZERO) ? this.blockPosition() : this.spawnPoint;
        int cleared = 0;
        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = -7; dy <= 7; dy++) {
                for (int dz = -7; dz <= 7; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    net.minecraft.world.level.block.Block block = level.getBlockState(pos).getBlock();
                    String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
                    if (id.startsWith("dungeonnowloading:chaos_spawner")) {
                        level.destroyBlock(pos, false);
                        cleared++;
                    }
                }
            }
        }
        if (cleared > 0) {
            level.playSound(null, center, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0f, 0.3f);
        }
    }

    // --- Phase update (Phase 2 at 50% HP, matching DNL) ---

    private void tickPhaseUpdate() {
        if (this.phase == 1 && this.getHealth() < this.getMaxHealth() * 0.5f) {
            this.phase = 2;
            if (this.level() instanceof ServerLevel level) {
                level.playSound(null, this.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.4f);
                level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                        this.getX(), this.getY() + 1.0, this.getZ(), 30, 2.0, 2.0, 2.0, 0.1);
                AABB range = this.getBoundingBox().inflate(FOLLOW_DISTANCE);
                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, range)) {
                    player.sendSystemMessage(Component.literal("The Chaos Spawner enters Phase 2!")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                }
            }
        }
    }

    // --- Weighted attack selection (DNL weights: Push=3, Single=3, Burst=2, Summon=1) ---

    private void tickAbilitySelection(ServerLevel level) {
        if (this.getTarget() == null) return;
        if (this.attackTick > 0) {
            --this.attackTick;
            return;
        }
        if (this.state != State.IDLE) return;

        // Weighted random attack selection
        AABB aabb = new AABB(this.blockPosition()).inflate(10);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, aabb);
        boolean playersClose = !nearbyPlayers.isEmpty();

        int totalWeight;
        int roll;
        if (playersClose) {
            // Push=3, Single=3, Burst=2, Summon=1 (total=9)
            totalWeight = 9;
            roll = this.getRandom().nextInt(totalWeight);
            if (roll < 3) {
                this.state = State.PUSH;
            } else if (roll < 6) {
                this.state = State.SHOOT_SINGLE;
            } else if (roll < 8) {
                this.state = State.SHOOT_BURST;
            } else {
                this.state = State.SUMMON;
            }
        } else {
            // No players close: Single=3, Burst=2, Summon=1 (total=6)
            totalWeight = 6;
            roll = this.getRandom().nextInt(totalWeight);
            if (roll < 3) {
                this.state = State.SHOOT_SINGLE;
            } else if (roll < 5) {
                this.state = State.SHOOT_BURST;
            } else {
                this.state = State.SUMMON;
            }
        }
    }

    // --- Execute current attack ---

    private void tickAttack(ServerLevel level) {
        switch (this.state) {
            case PUSH -> tickPushAttack(level);
            case SHOOT_SINGLE -> tickShootSingle(level);
            case SHOOT_BURST -> tickShootBurst(level);
            case SUMMON -> tickSummon(level);
            default -> {
                // Ambient particles in IDLE
                if (this.tickCount % 3 == 0) {
                    ParticleOptions p = this.phase >= 2
                            ? (ParticleOptions) ParticleTypes.FLAME
                            : (ParticleOptions) ParticleTypes.REVERSE_PORTAL;
                    level.sendParticles(p, this.getX(), this.getY() + 1.0, this.getZ(),
                            2, 0.4, 0.6, 0.4, 0.01);
                }
            }
        }
    }

    // --- PUSH attack (DNL: 8-block AoE, 12x knockback, 90% ATK, breaks shields) ---

    private void tickPushAttack(ServerLevel level) {
        if (this.attackTick == 0) {
            this.attackTick = 100;
        }
        if (this.attackTick == 66) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0f, 1.0f);
            level.sendParticles((ParticleOptions) ParticleTypes.POOF,
                    this.getX(), this.getY(), this.getZ(), 50, 3.0, 0.0, 3.0, 0.0);

            AABB aoe = new AABB(this.blockPosition()).inflate(8);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aoe);
            for (LivingEntity target : targets) {
                if (target == this) continue;
                double knockback = 12.0;
                double x = target.getX() - this.getX();
                double z = target.getZ() - this.getZ();
                double dist = Math.max(x * x + z * z, 0.001);

                if (target instanceof Player player) {
                    float damage;
                    if (player.isBlocking()) {
                        // Break shield — apply 5-second shield cooldown
                        player.getCooldowns().addCooldown(player.getUseItem(), 100);
                        player.stopUsingItem();
                        damage = this.getAttackDamage() * 0.45f;
                    } else {
                        damage = this.getAttackDamage() * 0.9f;
                    }
                    player.push(x / dist * knockback, 0.2, z / dist * knockback);
                    player.hurt(this.damageSources().mobAttack(this), damage);
                } else {
                    target.push(x / dist * knockback, 0.2, z / dist * knockback);
                    target.hurt(this.damageSources().noAggroMobAttack(this), this.getAttackDamage() * 0.9f);
                }
            }
        }
        if (this.attackTick <= 1) {
            stopAttacking(60);
        }
    }

    // --- GHOST BULLET SINGLE (DNL: Phase 1=Single/Arc, Phase 2=Rapid/StrongArc) ---

    private void tickShootSingle(ServerLevel level) {
        if (this.attackTick == 0) {
            this.attackTick = 126;
            if (this.phase == 1) {
                this.bulletPattern = this.getRandom().nextBoolean() ? "Single" : "Arc";
            } else {
                this.bulletPattern = this.getRandom().nextBoolean() ? "Rapid" : "StrongArc";
            }
        }

        LivingEntity target = this.getTarget();
        if (target == null || target.distanceTo(this) > FOLLOW_DISTANCE) {
            if (this.attackTick <= 1) stopAttacking(60);
            return;
        }

        switch (this.bulletPattern) {
            case "Single" -> {
                // 5 single shots at ticks 100, 80, 60, 40, 20
                for (int i = 0; i < 5; i++) {
                    if (this.attackTick == 100 - i * 20) shootDirected(level, 0.0f);
                }
            }
            case "Arc" -> {
                // 3 waves of 3-spread shots at ticks 100, 70, 40
                for (int i = 0; i < 3; i++) {
                    if (this.attackTick == 100 - i * 30) {
                        shootDirected(level, 0.0f);
                        shootDirected(level, -10.0f);
                        shootDirected(level, 10.0f);
                    }
                }
            }
            case "Rapid" -> {
                // 3 bursts of 5 rapid shots
                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 5; i++) {
                        if (this.attackTick == (100 - 30 * j) - i * 4) shootDirected(level, 0.0f);
                    }
                }
            }
            case "StrongArc" -> {
                // 3 waves of 5-spread shots
                for (int i = 0; i < 3; i++) {
                    if (this.attackTick == 100 - i * 30) {
                        shootDirected(level, 0.0f);
                        shootDirected(level, -10.0f);
                        shootDirected(level, 10.0f);
                        shootDirected(level, -20.0f);
                        shootDirected(level, 20.0f);
                    }
                }
            }
        }
        if (this.attackTick <= 1) stopAttacking(60);
    }

    // --- GHOST BULLET BURST (DNL: Phase 1=8 bullets circle, Phase 2=16 bullets circle) ---

    private void tickShootBurst(ServerLevel level) {
        if (this.attackTick == 0) {
            this.attackTick = 126;
        }
        int bulletsPerWave = this.phase >= 2 ? 16 : 8;
        float angleStep = this.phase >= 2 ? 22.5f : 45.0f;

        // 3 waves at ticks 100, 70, 40 — alternating offset
        for (int i = 0; i < 3; i++) {
            if (this.attackTick == 100 - i * 30) {
                float baseOffset = (i % 2 == 0) ? 0.0f : (this.phase >= 2 ? 11.5f : 22.5f);
                shootBurstRing(level, bulletsPerWave, angleStep, baseOffset);
            }
        }
        if (this.attackTick <= 1) stopAttacking(60);
    }

    // --- SUMMON MOBS (DNL: vanilla mobs, Phase 2 adds diamond/invisible/jockey variants) ---

    private void tickSummon(ServerLevel level) {
        // Check mob cap first
        List<Monster> nearbyMobs = level.getEntitiesOfClass(Monster.class,
                this.getBoundingBox().inflate(FOLLOW_DISTANCE / 2));
        int maxSummon = Math.min(12, 4 + (int)(this.difficultyMultiplier * 2));
        if (nearbyMobs.size() >= maxSummon) {
            stopAttacking(0);
            return;
        }

        if (this.attackTick == 0) {
            this.attackTick = 100;
        }

        if (this.attackTick == 100) {
            level.playSound(null, this.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 1.0f);
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 20, 3.0, 3.0, 3.0, 0.0);

            int summonCount = Math.max(0, maxSummon - nearbyMobs.size());
            summonCount = Math.min(summonCount, SUMMON_OFFSETS.length);

            for (int i = 0; i < summonCount; i++) {
                BlockPos summonPos = this.blockPosition().offset(SUMMON_OFFSETS[i]);
                level.sendParticles((ParticleOptions) ParticleTypes.CLOUD,
                        summonPos.getX(), summonPos.getY() + 1, summonPos.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
                spawnSummonedMob(level, summonPos);
            }
        }
        if (this.attackTick <= 1) stopAttacking(60);
    }

    private void spawnSummonedMob(ServerLevel level, BlockPos pos) {
        int roll;
        if (this.phase == 1) {
            // Phase 1: Zombie(3), Skeleton(2), Spider(2) = 7
            roll = this.getRandom().nextInt(7);
            if (roll < 3) spawnVanillaMob(level, EntityType.ZOMBIE, pos, false, false);
            else if (roll < 5) spawnVanillaMob(level, EntityType.SKELETON, pos, false, false);
            else spawnVanillaMob(level, EntityType.SPIDER, pos, false, false);
        } else {
            // Phase 2: adds diamond/invisible/jockey variants
            roll = this.getRandom().nextInt(29);
            if (roll < 9) spawnVanillaMob(level, EntityType.ZOMBIE, pos, false, false);
            else if (roll < 15) spawnVanillaMob(level, EntityType.SKELETON, pos, false, false);
            else if (roll < 21) spawnVanillaMob(level, EntityType.SPIDER, pos, false, false);
            else if (roll < 23) spawnDiamondZombie(level, pos);
            else if (roll < 25) spawnDiamondSkeleton(level, pos);
            else if (roll < 27) spawnInvisibleSpider(level, pos);
            else if (roll < 28) spawnSpiderJockey(level, pos);
            else spawnBabyZombie(level, pos);
        }
    }

    // --- Helper: spawn vanilla mob with no drops ---

    private void spawnVanillaMob(ServerLevel level, EntityType<? extends Mob> type, BlockPos pos,
                                  boolean noDrop, boolean unused) {
        Mob mob = (Mob) type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        setNoDrops(mob);
        level.addFreshEntity(mob);
    }

    private void spawnDiamondZombie(ServerLevel level, BlockPos pos) {
        Mob mob = (Mob) EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        setNoDrops(mob);
        level.addFreshEntity(mob);
    }

    private void spawnDiamondSkeleton(ServerLevel level, BlockPos pos) {
        Mob mob = (Mob) EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        setNoDrops(mob);
        level.addFreshEntity(mob);
    }

    private void spawnInvisibleSpider(ServerLevel level, BlockPos pos) {
        Mob mob = (Mob) EntityType.SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        ((LivingEntity) mob).addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1));
        level.addFreshEntity(mob);
    }

    private void spawnSpiderJockey(ServerLevel level, BlockPos pos) {
        Mob spider = (Mob) EntityType.SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED);
        Mob skeleton = (Mob) EntityType.SKELETON.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (spider == null || skeleton == null) return;
        spider.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        skeleton.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        skeleton.startRiding(spider);
        setNoDrops(skeleton);
        level.addFreshEntity(spider);
        level.addFreshEntity(skeleton);
    }

    private void spawnBabyZombie(ServerLevel level, BlockPos pos) {
        Mob mob = (Mob) EntityType.ZOMBIE.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (mob == null) return;
        mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        mob.setBaby(true);
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
        mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
        setNoDrops(mob);
        level.addFreshEntity(mob);
    }

    private void setNoDrops(Mob mob) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0f);
        }
    }

    // --- Contact damage (DNL: hurts touching players every 20 ticks) ---

    private void tickContactDamage(ServerLevel level) {
        if (this.contactDamageCooldown > 0) {
            --this.contactDamageCooldown;
            return;
        }
        this.contactDamageCooldown = 20;
        AABB aabb = new AABB(this.blockPosition()).inflate(2);
        List<Player> touching = level.getEntitiesOfClass(Player.class, aabb);
        for (Player player : touching) {
            this.doHurtTarget(level, player);
        }
    }

    // --- Shooting helpers ---

    private void shootDirected(ServerLevel level, float angleOffset) {
        Vec3 view = this.getViewVector(1.0f);
        if (angleOffset != 0.0f) {
            view = view.yRot((float) Math.toRadians(angleOffset));
        }
        double d0 = view.x * 2.0;
        double d1 = view.y * 2.0;
        double d2 = view.z * 2.0;
        GhostBulletEntity bullet = new GhostBulletEntity(DungeonEntityRegistry.GHOST_BULLET.get(), this.level());
        bullet.setOwner(this);
        bullet.setPos(this.getX() + d0, this.getY(0.5) + d1, this.getZ() + d2);
        bullet.setDeltaMovement(d0 * 0.2, d1 * 0.2, d2 * 0.2);
        this.level().addFreshEntity(bullet);
        level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 3.0f,
                1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2f);
    }

    private void shootBurstRing(ServerLevel level, int count, float angleStep, float baseOffset) {
        float offsetRad = (float) Math.toRadians(baseOffset);
        Vec3 view = this.getViewVector(1.0f).yRot(offsetRad);
        level.playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 3.0f,
                1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2f);
        for (int i = 0; i < count; i++) {
            Vec3 dir = view.yRot((float) Math.toRadians(angleStep) * i);
            GhostBulletEntity bullet = new GhostBulletEntity(DungeonEntityRegistry.GHOST_BULLET.get(), this.level());
            bullet.setOwner(this);
            bullet.setPos(this.getX() + dir.x, this.getY(0.5) + dir.y, this.getZ() + dir.z);
            bullet.setDeltaMovement(dir.x * 0.2, dir.y * 0.2, dir.z * 0.2);
            this.level().addFreshEntity(bullet);
        }
    }

    private void stopAttacking(int cooldown) {
        this.state = State.IDLE;
        this.attackTick = cooldown;
        this.bulletPattern = "";
    }

    // --- Death (DNL: 160-tick death animation, then spawn altar) ---

    @Override
    public void die(DamageSource source) {
        if (this.state == State.DEAD) return;
        super.die(source);
        this.state = State.DEAD;
        this.deathAnimTicks = 0;
        if (this.level().isClientSide()) return;

        ServerLevel level = (ServerLevel) this.level();
        this.bossEvent.removeAllPlayers();

        // Clear the chaos spawner frame/cage structural blocks so the boss can move freely
        clearFrameBlocks(level);

        // Spawn the altar at spawn position
        BlockPos altarPos = this.spawnPoint.equals(BlockPos.ZERO) ? this.blockPosition() : this.spawnPoint;
        level.setBlock(altarPos, DungeonEntityRegistry.DUNGEON_ALTAR_BLOCK.get().defaultBlockState(), 3);

        // Award XP (DNL gives 500 base)
        int xpAmount = (int) (500 * this.difficultyMultiplier);
        ExperienceOrb.award(level, this.position(), xpAmount);

        // Notify all nearby players
        AABB range = this.getBoundingBox().inflate(FOLLOW_DISTANCE);
        List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, range);
        for (ServerPlayer player : nearby) {
            player.sendSystemMessage(Component.literal("The Chaos Spawner has been defeated! An altar has appeared...")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }

        // Notify DungeonManager
        if (!this.dungeonInstanceId.isEmpty()) {
            DungeonManager manager = DungeonManager.get(level.getServer().overworld());
            manager.chaosSpawnerDefeated(this.dungeonInstanceId);
            DungeonManager.DungeonInstance inst = manager.getInstance(this.dungeonInstanceId);
            if (inst != null) {
                inst.bossChestPositions.clear();
                int chestCount = inst.tier.getBossChestCount();
                for (int i = 0; i < chestCount; i++) {
                    int offsetX = (i - chestCount / 2) * 3;
                    inst.bossChestPositions.add(new BlockPos(altarPos.getX() + offsetX, altarPos.getY(), altarPos.getZ() + 3));
                }
            }
        }

        level.playSound(null, this.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 2.0f, 0.8f);
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(), 60, 2.0, 2.0, 2.0, 0.15);
    }

    // --- Persistence ---

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("GatekeeperState", this.state.name());
        output.putInt("Phase", this.phase);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putString("DungeonInstanceId", this.dungeonInstanceId);
        output.putInt("SpawnPointX", this.spawnPoint.getX());
        output.putInt("SpawnPointY", this.spawnPoint.getY());
        output.putInt("SpawnPointZ", this.spawnPoint.getZ());
        output.putInt("AwakeningTimer", this.awakeningTimer);
        output.putInt("AttackTick", this.attackTick);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stateName = input.getStringOr("GatekeeperState", "SLEEPING");
        try {
            this.state = State.valueOf(stateName);
        } catch (IllegalArgumentException e) {
            this.state = State.SLEEPING;
        }
        this.phase = input.getIntOr("Phase", 0);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.dungeonInstanceId = input.getStringOr("DungeonInstanceId", "");
        this.spawnPoint = new BlockPos(
                input.getIntOr("SpawnPointX", 0),
                input.getIntOr("SpawnPointY", 0),
                input.getIntOr("SpawnPointZ", 0));
        this.awakeningTimer = input.getIntOr("AwakeningTimer", 0);
        this.attackTick = input.getIntOr("AttackTick", 0);

        // Re-scale HP after load
        if (this.difficultyMultiplier != 1.0f) {
            float scaledHP = BASE_HP * this.difficultyMultiplier;
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) scaledHP);
        }

        // Reset to sleeping if phase was never started
        if (this.phase == 0 && this.state == State.IDLE) {
            this.state = State.SLEEPING;
        }
    }
}
