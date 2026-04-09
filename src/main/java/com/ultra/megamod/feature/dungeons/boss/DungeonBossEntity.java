/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.AnimationState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.storage.ValueInput
 *  net.minecraft.world.level.storage.ValueOutput
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.furniture.QuestTracker;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.dimensions.PocketBuilder;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.boss.BossBarHandler;
import com.ultra.megamod.feature.dungeons.boss.BossPhaseManager;
import com.ultra.megamod.feature.dungeons.boss.FrostmawBoss;
import com.ultra.megamod.feature.dungeons.boss.WroughtnautBoss;
import com.ultra.megamod.feature.dungeons.boss.UmvuthiBoss;
import com.ultra.megamod.feature.dungeons.boss.ChaosSpawnerBoss;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import com.ultra.megamod.feature.dungeons.network.BossMusicPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class DungeonBossEntity
extends Monster {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    protected final ServerBossEvent bossEvent;
    protected int currentPhase = 1;
    protected float difficultyMultiplier = 1.0f;
    protected int attackCooldown = 0;
    protected int invulnerabilityTicks = 0;
    protected String dungeonInstanceId = "";
    protected BlockPos bossRoomCenter = BlockPos.ZERO;
    protected boolean phaseTransitioning = false;
    private static final int BOSS_BAR_RANGE = 100;

    protected DungeonBossEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setPlayBossMusic(true);
        this.setPersistenceRequired();
    }

    public abstract int getMaxPhases();

    public abstract void onPhaseTransition(int var1);

    public abstract void performAttack(int var1);

    public abstract float getBaseMaxHealth();

    /**
     * Returns the boss ID for New Game+ tracking.
     * Derived from class name: WraithBoss -> "wraith", ChaosSpawnerBoss -> "chaos_spawner", etc.
     */
    public String getBossId() {
        String className = this.getClass().getSimpleName();
        // Remove "Boss" suffix
        String name = className.replace("Boss", "");
        // Convert CamelCase to snake_case
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) sb.append('_');
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    public abstract float getBaseDamage();

    public abstract Component getBossDisplayName();

    public static AttributeSupplier.Builder createBossAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.ATTACK_DAMAGE, 8.0).add(Attributes.ARMOR, 4.0).add(Attributes.FOLLOW_RANGE, 64.0).add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new AvoidSpikeBlockGoal((PathfinderMob)this));
        this.goalSelector.addGoal(4, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.0, false));
        this.goalSelector.addGoal(5, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 0.8));
        this.goalSelector.addGoal(6, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 16.0f));
        this.goalSelector.addGoal(7, (Goal)new RandomLookAroundGoal((Mob)this));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, true));
    }

    /**
     * Returns the softened damage multiplier for attack calculations.
     * Uses diminishing scaling so high-tier bosses stay dangerous without instant-killing.
     */
    protected float getDamageMultiplier() {
        return 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
    }

    public void setDifficultyMultiplier(float mult) {
        this.difficultyMultiplier = mult;
        float scaledMaxHealth = this.getBaseMaxHealth() * mult;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)scaledMaxHealth);
        this.setHealth(scaledMaxHealth);
        // Damage uses diminishing scaling — stays dangerous but not instant-kill
        // Normal=1.0x, Hard=1.25x, Nightmare=1.75x, Infernal=2.5x, Mythic=3.5x, Eternal=5.5x
        float damageMult = 1.0f + (mult - 1.0f) * 0.5f;
        float scaledDamage = this.getBaseDamage() * damageMult;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double)scaledDamage);
        // Higher tiers get more armor and speed (Nightmare+ now included)
        if (mult >= 6.0f) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(12.0);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.34);
        } else if (mult >= 4.0f) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.32);
        } else if (mult >= 2.5f) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(6.0);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.30);
        }
    }

    public void setDungeonInstanceId(String instanceId) {
        this.dungeonInstanceId = instanceId;
    }

    public String getDungeonInstanceId() {
        return this.dungeonInstanceId;
    }

    public void setBossRoomCenter(BlockPos center) {
        this.bossRoomCenter = center;
    }

    public BlockPos getBossRoomCenter() {
        return this.bossRoomCenter;
    }

    public int getCurrentPhase() {
        return this.currentPhase;
    }

    /**
     * Returns true if stepping to this position would cause a fall greater than 2 blocks.
     * Spikes are at the bottom of pits, so avoiding long drops keeps bosses safe.
     */
    protected boolean isDangerousDrop(BlockPos feetPos) {
        return AvoidSpikeBlockGoal.isDangerousDrop(this.level(), feetPos);
    }

    /**
     * Find a safe position near the given position, avoiding dangerous drops.
     * Searches in expanding rings around the target.
     */
    protected BlockPos findSafePosition(BlockPos target) {
        if (!isDangerousDrop(target)) return target;
        for (int r = 1; r <= 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    BlockPos candidate = target.offset(dx, 0, dz);
                    if (!isDangerousDrop(candidate) && !this.level().getBlockState(candidate).isSolid()) {
                        return candidate;
                    }
                }
            }
        }
        return target;
    }

    /**
     * Break any iron bars overlapping the boss's bounding box.
     * Bosses are powerful enough to smash through iron bar obstacles.
     */
    private void breakNearbyIronBars(ServerLevel level) {
        AABB box = this.getBoundingBox().inflate(0.2);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.ceil(box.maxX);
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.ceil(box.maxY);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.ceil(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    if (level.getBlockState(pos).getBlock() instanceof IronBarsBlock) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
            return;
        }
        // Safety net: push boss away from ledges/pits every 5 ticks (only when idle, not in combat)
        if (!this.isNoGravity() && this.getTarget() == null && this.tickCount % 5 == 0 && isDangerousDrop(this.blockPosition())) {
            BlockPos safePos = findSafePosition(this.blockPosition());
            if (!safePos.equals(this.blockPosition())) {
                Vec3 pushDir = Vec3.atCenterOf(safePos).subtract(this.position()).normalize().scale(0.4);
                this.push(pushDir.x, 0.1, pushDir.z);
            }
        }
        // Bosses smash through iron bars — prevents getting stuck
        if (this.tickCount % 3 == 0) {
            breakNearbyIronBars((ServerLevel) this.level());
        }
        if (this.invulnerabilityTicks > 0) {
            --this.invulnerabilityTicks;
            if (this.invulnerabilityTicks == 0) {
                this.phaseTransitioning = false;
            }
        }
        if (this.attackCooldown > 0) {
            --this.attackCooldown;
        }
        BossPhaseManager.checkPhaseTransition(this);
        if (!this.isDeadOrDying()) {
            this.updateBossBar();
        }
        if (this.attackCooldown <= 0 && !this.phaseTransitioning && this.getTarget() != null && this.getTarget().isAlive()) {
            boolean inRangedRange;
            double distSq = this.distanceToSqr((Entity)this.getTarget());
            boolean inMeleeRange = distSq < 9.0;
            boolean bl = inRangedRange = distSq < 400.0;
            if (inMeleeRange || inRangedRange) {
                this.performAttack(this.currentPhase);
                this.attackAnimationState.start(this.tickCount);
                int baseCooldown = 40 - this.currentPhase * 5;
                this.attackCooldown = Math.max(15, baseCooldown);
            }
        }
    }

    private void updateBossBar() {
        ServerLevel serverLevel = (ServerLevel)this.level();
        float healthFraction = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(healthFraction);
        BossBarHandler.updateBar(this);
        AABB range = this.getBoundingBox().inflate(100.0);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, range);
        for (ServerPlayer player : nearbyPlayers) {
            this.bossEvent.addPlayer(player);
        }
        for (ServerPlayer player : List.copyOf(this.bossEvent.getPlayers())) {
            // Remove if too far, dead, or no longer in the dungeon dimension
            if (this.distanceToSqr((Entity)player) > 10000.0
                    || !player.isAlive()
                    || !player.level().dimension().equals(this.level().dimension())) {
                this.bossEvent.removePlayer(player);
            }
        }
    }

    /** Remove a specific player from this boss's bar (called on death/logout) */
    public void removeBossBarPlayer(ServerPlayer player) {
        this.bossEvent.removePlayer(player);
        PacketDistributor.sendToPlayer(player, new BossMusicPayload(this.getId(), false));
    }

    void setPhase(int newPhase) {
        if (newPhase != this.currentPhase && newPhase <= this.getMaxPhases()) {
            this.currentPhase = newPhase;
            this.phaseTransitioning = true;
            this.invulnerabilityTicks = 20;
            this.onPhaseTransition(newPhase);
            ServerLevel serverLevel = (ServerLevel)this.level();
            AABB range = this.getBoundingBox().inflate(100.0);
            List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, range);
            for (ServerPlayer player : nearbyPlayers) {
                player.sendSystemMessage((Component)Component.literal((String)(this.getBossDisplayName().getString() + " enters Phase " + newPhase + "!")).withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}));
            }
            BossPhaseManager.spawnPhaseParticles(serverLevel, this.position());
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.2f);
        }
    }

    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.phaseTransitioning && this.invulnerabilityTicks > 0) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    protected void tickDeath() {
        super.tickDeath();
    }

    public void die(DamageSource source) {
        super.die(source);
        this.deathAnimationState.start(this.tickCount);
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)this.level();
        if (!this.dungeonInstanceId.isEmpty()) {
            DungeonManager manager = DungeonManager.get(serverLevel.getServer().overworld());
            manager.completeDungeon(this.dungeonInstanceId);
            manager.placeBossLootChests(this.dungeonInstanceId, serverLevel);

            // Pay out Quest Board rewards to all players in this dungeon
            DungeonManager.DungeonInstance instance = manager.getInstance(this.dungeonInstanceId);
            if (instance != null) {
                ServerLevel overworld = serverLevel.getServer().overworld();
                QuestTracker questTracker = QuestTracker.get(overworld);
                EconomyManager eco = EconomyManager.get(overworld);
                int dungeonTier = instance.tier.ordinal();

                // Check owner + all party members
                java.util.Set<java.util.UUID> allPlayers = new java.util.HashSet<>(instance.partyPlayers);
                allPlayers.add(instance.playerUUID);

                for (java.util.UUID playerUuid : allPlayers) {
                    int coins = questTracker.completeDungeonForPlayer(playerUuid, dungeonTier);
                    if (coins > 0) {
                        eco.addWallet(playerUuid, coins);
                        ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);
                        if (sp != null) {
                            sp.sendSystemMessage(Component.literal(
                                "\u00A7a\u00A7l\u2605 \u00A7eQuest Complete! \u00A77Earned \u00A7a+" + coins + " MC\u00A77!"));
                            eco.addAuditEntry(sp.getGameProfile().name(), "quest_board_complete",
                                coins, "Dungeon " + instance.tier.name() + " cleared");
                            // Quest bonus: 40% chance to also receive a rolled item reward
                            double luck = com.ultra.megamod.feature.loot.WorldLootIntegration.getLuck(sp);
                            double questItemChance = 0.40 + luck * 0.02;
                            if (serverLevel.random.nextDouble() < questItemChance) {
                                net.minecraft.world.item.ItemStack questReward = com.ultra.megamod.feature.loot.WorldLootIntegration.generateQuestReward(
                                        instance.tier, serverLevel.random);
                                if (!sp.getInventory().add(questReward)) {
                                    sp.spawnAtLocation(serverLevel, questReward);
                                }
                                sp.sendSystemMessage(Component.literal(
                                        "\u00A76\u00A7l\u2605 \u00A7eQuest bonus: " + questReward.getHoverName().getString() + "!"));
                            }
                        }
                    }
                }
                questTracker.saveToDisk(overworld);

                // Record boss defeat for New Game+ progression
                String bossId = getBossId();
                if (bossId != null) {
                    com.ultra.megamod.feature.dungeons.NewGamePlusManager ngp = com.ultra.megamod.feature.dungeons.NewGamePlusManager.get(overworld);
                    for (java.util.UUID playerUuid : allPlayers) {
                        ngp.recordBossDefeat(playerUuid, bossId, instance.tier.name());
                    }
                    ngp.saveToDisk(overworld);
                }

                // Challenge hook: dungeon_clear for all players in this dungeon
                for (java.util.UUID playerUuid : allPlayers) {
                    ServerPlayer sp2 = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);
                    if (sp2 != null) {
                        com.ultra.megamod.feature.skills.challenges.SkillChallenges.addProgress(sp2, "dungeon_clear", 1);
                    }
                }

                // Record leaderboard time
                if (instance.startTimeMs > 0) {
                    long clearTime = System.currentTimeMillis() - instance.startTimeMs;
                    com.ultra.megamod.feature.dungeons.DungeonLeaderboardManager lb = com.ultra.megamod.feature.dungeons.DungeonLeaderboardManager.get(overworld);
                    int partySize = allPlayers.size();
                    for (java.util.UUID playerUuid : allPlayers) {
                        ServerPlayer sp3 = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);
                        String name = sp3 != null ? sp3.getGameProfile().name() : "Unknown";
                        lb.submitTime(playerUuid, name, instance.tier, partySize, clearTime);
                    }
                    lb.saveToDisk(overworld);
                }

                // Record analytics for admin Dungeon Analytics tab
                String bossDisplayName = this.getDisplayName().getString();
                long nowMs = System.currentTimeMillis();
                for (java.util.UUID playerUuid : allPlayers) {
                    ServerPlayer sp4 = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);
                    String pName = sp4 != null ? sp4.getGameProfile().name() : "Unknown";
                    com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.recordRun(
                            pName, playerUuid.toString(),
                            instance.tier.getDisplayName(), instance.theme.getDisplayName(),
                            "Completed", instance.startTimeMs, nowMs, bossDisplayName);
                    com.ultra.megamod.feature.computer.network.handlers.DungeonAnalyticsHandler.recordBossKill(
                            pName, bossDisplayName, instance.tier.getDisplayName());
                }
            }

            instance = manager.getInstance(this.dungeonInstanceId);
            if (instance != null && !instance.bossChestPositions.isEmpty() && serverLevel.getRandom().nextFloat() < 0.15f) {
                ItemStack trophy = null;
                if (this instanceof WraithBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.WRAITH_TROPHY_ITEM.get());
                } else if (this instanceof OssukageBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.OSSUKAGE_TROPHY_ITEM.get());
                } else if (this instanceof DungeonKeeperBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.DUNGEON_KEEPER_TROPHY_ITEM.get());
                } else if (this instanceof FrostmawBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.FROSTMAW_TROPHY_ITEM.get());
                } else if (this instanceof WroughtnautBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.WROUGHTNAUT_TROPHY_ITEM.get());
                } else if (this instanceof UmvuthiBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.UMVUTHI_TROPHY_ITEM.get());
                } else if (this instanceof ChaosSpawnerBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.CHAOS_SPAWNER_TROPHY_ITEM.get());
                } else if (this instanceof SculptorBoss) {
                    trophy = new ItemStack((ItemLike)DungeonExclusiveItems.SCULPTOR_TROPHY_ITEM.get());
                }
                if (trophy != null) {
                    BlockPos trophyChestPos = instance.bossChestPositions.get(0);
                    BlockEntity be = serverLevel.getBlockEntity(trophyChestPos);
                    if (be instanceof ChestBlockEntity chest) {
                        for (int slot = 0; slot < chest.getContainerSize(); ++slot) {
                            if (!chest.getItem(slot).isEmpty()) continue;
                            chest.setItem(slot, trophy);
                            break;
                        }
                    }
                }
            }
        }
        serverLevel.playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 2.0f, 0.8f);
        int xpAmount = (int)(500.0f * this.difficultyMultiplier);
        ExperienceOrb.award((ServerLevel)serverLevel, (Vec3)this.position(), (int)xpAmount);
        BlockPos portalPos = this.bossRoomCenter.equals((Object)BlockPos.ZERO) ? this.blockPosition() : this.bossRoomCenter;
        PocketBuilder.placePortalBlock(serverLevel, portalPos);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Block fogWallBlock = (Block)DungeonEntityRegistry.FOG_WALL_BLOCK.get();
        int searchRadius = 30;
        for (int dx = -searchRadius; dx <= searchRadius; ++dx) {
            for (int dy = -5; dy <= 15; ++dy) {
                for (int dz = -searchRadius; dz <= searchRadius; ++dz) {
                    mutablePos.set(portalPos.getX() + dx, portalPos.getY() + dy, portalPos.getZ() + dz);
                    if (!serverLevel.getBlockState((BlockPos)mutablePos).is(fogWallBlock)) continue;
                    serverLevel.setBlock((BlockPos)mutablePos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        AABB range = this.getBoundingBox().inflate(100.0);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, range);
        for (ServerPlayer player : nearbyPlayers) {
            player.sendSystemMessage((Component)Component.literal((String)(this.getBossDisplayName().getString() + " has been defeated!")).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}));
            player.sendSystemMessage((Component)Component.literal((String)"A return portal has appeared!").withStyle(ChatFormatting.GREEN));
        }
        this.bossEvent.removeAllPlayers();
    }

    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        // Skip boss bar and music for showcase (NoAI) entities
        if (this.isNoAi()) return;
        this.bossEvent.setName(this.getBossDisplayName());
        this.bossEvent.addPlayer(player);
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new BossMusicPayload(this.getId(), true), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.isNoAi()) return;
        this.bossEvent.removePlayer(player);
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new BossMusicPayload(this.getId(), false), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("BossPhase", this.currentPhase);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putString("DungeonInstanceId", this.dungeonInstanceId);
        output.putInt("BossRoomX", this.bossRoomCenter.getX());
        output.putInt("BossRoomY", this.bossRoomCenter.getY());
        output.putInt("BossRoomZ", this.bossRoomCenter.getZ());
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.currentPhase = input.getIntOr("BossPhase", 1);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.dungeonInstanceId = input.getStringOr("DungeonInstanceId", "");
        this.bossRoomCenter = new BlockPos(input.getIntOr("BossRoomX", 0), input.getIntOr("BossRoomY", 0), input.getIntOr("BossRoomZ", 0));
        if (this.difficultyMultiplier != 1.0f) {
            setDifficultyMultiplier(this.difficultyMultiplier);
        }
        this.bossEvent.setName(this.getBossDisplayName());
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }
}

