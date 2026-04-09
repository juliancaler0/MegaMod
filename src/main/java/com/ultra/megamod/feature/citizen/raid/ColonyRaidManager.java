package com.ultra.megamod.feature.citizen.raid;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.FactionData;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.citizen.data.FactionStatsManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.prestige.MasteryMarkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

/**
 * Periodically raids colony territories with dungeon-themed mobs.
 * Raid difficulty scales with colony size (citizen count).
 * Multi-wave raids with warning phase, citizen reactions, and scaled rewards.
 * Defending rewards MegaCoins + Marks of Mastery for perfect defenses.
 * Raids happen every 3-5 MC days.
 *
 * Enhanced with culture-based raider system: 6 cultures x 3 tiers = 18 raider types.
 * Uses RaidEvent for culture raids and RaidDifficultyScaler for dynamic difficulty.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ColonyRaidManager {

    private static final int MIN_RAID_INTERVAL = 60000;  // ~3 MC days
    private static final int MAX_RAID_INTERVAL = 100000;  // ~5 MC days
    private static final int MIN_CITIZENS_FOR_RAID = 7;
    private static final int WARNING_TICKS = 600;         // 30 seconds warning phase
    private static final int RAID_DURATION = 6000;        // 5 minutes per wave
    private static final double WAVE_CLEAR_THRESHOLD = 0.80; // 80% killed to advance wave

    private static final Map<String, RaidState> activeRaids = new HashMap<>();
    private static final Map<String, ServerBossEvent> raidBossBars = new HashMap<>();
    private static long nextRaidCheck = 0;
    private static final Random random = new Random();

    // --- Culture-based raid system ---
    private static int raidDifficulty = RaidDifficultyScaler.INITIAL_DIFFICULTY;
    private static int nightsSinceLastRaid = 0;
    private static long lastDayChecked = -1;
    private static int nextRaidId = 1;
    private static final List<RaidEvent> activeCultureRaids = new ArrayList<>();
    private static final Map<Integer, ServerBossEvent> cultureRaidBossBars = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long time = overworld.getGameTime();

        // Track night cycle for culture raids
        trackNightCycle(overworld);

        // Check for scheduled raids (from /mc colony raid tonight)
        if (scheduledRaidCenter != null && scheduledRaidTick > 0 && time >= scheduledRaidTick) {
            startCultureRaid(overworld, scheduledRaidCenter, RaiderCulture.BARBARIAN);
            scheduledRaidCenter = null;
            scheduledRaidTick = -1;
        }

        // Check for new raids periodically
        if (time >= nextRaidCheck) {
            nextRaidCheck = time + 2400; // check every 2 minutes
            checkForRaids(overworld);
        }

        // Tick active legacy raids (vanilla mobs)
        if (!activeRaids.isEmpty()) {
            var iterator = activeRaids.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                RaidState raid = entry.getValue();

                if (raid.inWarningPhase) {
                    tickWarningPhase(overworld, entry.getKey(), raid);
                } else {
                    tickActiveRaid(overworld, entry.getKey(), raid, iterator);
                }
            }
        }

        // Tick active culture-based raids (raider entities)
        tickCultureRaids(overworld);
    }

    private static void tickWarningPhase(ServerLevel level, String factionId, RaidState raid) {
        raid.warningTicksRemaining--;

        // Play warning horn every 5 seconds during warning phase
        if (raid.warningTicksRemaining % 100 == 0 && raid.warningTicksRemaining > 0) {
            playRaidHornForFaction(level, factionId, raid.center);
        }

        // Update boss bar during warning phase to show countdown
        ServerBossEvent bossBar = raidBossBars.get(factionId);
        if (bossBar != null) {
            float warningProgress = (float) raid.warningTicksRemaining / WARNING_TICKS;
            bossBar.setProgress(warningProgress);
            bossBar.setName(Component.literal("Raid Incoming... " + (raid.warningTicksRemaining / 20) + "s")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        }

        if (raid.warningTicksRemaining <= 0) {
            // Warning phase over - start the actual raid
            raid.inWarningPhase = false;

            // Notify citizens to react
            notifyCitizensOfRaid(level, raid.center, raid.leaderId);

            // Spawn wave 1
            spawnRaidWave(level, raid.center, raid.waveSizes[0], raid.difficulty, 1);
            raid.mobsRemainingInWave = raid.waveSizes[0];

            // Update boss bar for active raid
            if (bossBar != null) {
                bossBar.setName(Component.literal("Colony Under Attack! Wave 1/" + raid.maxWaves)
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                bossBar.setProgress(1.0f);
                bossBar.setColor(BossEvent.BossBarColor.RED);
            }

            notifyFaction(level, factionId,
                Component.literal("Raiders are attacking! Wave 1/" + raid.maxWaves + "!")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    private static void tickActiveRaid(ServerLevel level, String factionId, RaidState raid,
                                        Iterator<Map.Entry<String, RaidState>> iterator) {
        raid.ticksRemaining--;

        // Check wave progress every 2 seconds (40 ticks)
        if (raid.ticksRemaining % 40 == 0) {
            int survivorsInWave = countSurvivingRaiders(level, raid.center);
            raid.mobsRemainingInWave = survivorsInWave;
            int totalSpawnedThisWave = raid.waveSizes[raid.currentWave - 1];
            int killed = totalSpawnedThisWave - survivorsInWave;
            double killRatio = totalSpawnedThisWave > 0 ? (double) killed / totalSpawnedThisWave : 1.0;

            // Update boss bar progress
            ServerBossEvent bossBar = raidBossBars.get(factionId);
            if (bossBar != null) {
                float progress = survivorsInWave > 0
                        ? (float) survivorsInWave / totalSpawnedThisWave
                        : 0.0f;
                bossBar.setProgress(Math.max(0.0f, Math.min(1.0f, progress)));
                bossBar.setName(Component.literal("Colony Under Attack! Wave " + raid.currentWave + "/" + raid.maxWaves
                        + " - " + survivorsInWave + " raiders remaining")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }

            // Check if current wave is cleared enough to advance (80%+ killed)
            if (killRatio >= WAVE_CLEAR_THRESHOLD && raid.currentWave < raid.maxWaves) {
                raid.currentWave++;
                raid.totalMobsKilled += killed;

                // Clean up stragglers from previous wave
                cleanupRaiders(level, raid.center);

                // Spawn next wave
                spawnRaidWave(level, raid.center, raid.waveSizes[raid.currentWave - 1], raid.difficulty, raid.currentWave);
                raid.mobsRemainingInWave = raid.waveSizes[raid.currentWave - 1];

                // Reset timer for the new wave
                raid.ticksRemaining = RAID_DURATION;

                // Update boss bar for new wave
                if (bossBar != null) {
                    bossBar.setProgress(1.0f);
                    BossEvent.BossBarColor color = raid.currentWave == raid.maxWaves
                            ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.RED;
                    bossBar.setColor(color);
                }

                // Play horn sound for new wave
                playRaidHornForFaction(level, factionId, raid.center);

                String waveMsg = raid.currentWave == raid.maxWaves
                    ? "Final wave incoming! Wave " + raid.currentWave + "/" + raid.maxWaves + "!"
                    : "Wave " + raid.currentWave + "/" + raid.maxWaves + " incoming!";
                notifyFaction(level, factionId,
                    Component.literal(waveMsg).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }

            // Check if all mobs in the final wave are dead (raid fully cleared)
            if (raid.currentWave == raid.maxWaves && survivorsInWave == 0) {
                raid.totalMobsKilled += totalSpawnedThisWave;
                endRaid(level, factionId, raid);
                iterator.remove();
                return;
            }
        }

        if (raid.ticksRemaining <= 0) {
            // Time ran out on this wave
            int survivors = countSurvivingRaiders(level, raid.center);
            int totalThisWave = raid.waveSizes[raid.currentWave - 1];
            raid.totalMobsKilled += (totalThisWave - survivors);
            endRaid(level, factionId, raid);
            iterator.remove();
        }
    }

    private static void checkForRaids(ServerLevel level) {
        FactionManager factions = FactionManager.get(level);
        CitizenManager citizens = CitizenManager.get(level);

        for (FactionData faction : factions.getAllFactions()) {
            String factionId = faction.getFactionId();
            if (activeRaids.containsKey(factionId)) continue;

            UUID leaderId = faction.getLeaderUuid();
            if (leaderId == null) continue;
            int citizenCount = citizens.getCitizenCount(leaderId);
            if (citizenCount < MIN_CITIZENS_FOR_RAID) continue;

            if (random.nextFloat() > 0.10f) continue;

            // Use leader's position as raid center
            ServerPlayer leader = level.getServer().getPlayerList().getPlayer(leaderId);
            if (leader == null) continue;
            BlockPos raidCenter = leader.blockPosition();

            startRaid(level, factionId, leaderId, raidCenter, citizenCount);
        }
    }

    private static void startRaid(ServerLevel level, String factionId, UUID leaderId, BlockPos center, int citizenCount) {
        FactionData factionData = FactionManager.get(level).getPlayerFactionData(leaderId);
        String factionName = factionData != null ? factionData.getDisplayName() : factionId;

        // Calculate raid parameters
        int baseWaveSize = Math.min(20, 3 + citizenCount / 5);
        int difficulty = Math.min(14, 1 + citizenCount / 5);

        // Determine wave count based on difficulty
        // Difficulty 1-5: 1 wave, 6-10: 2 waves, 11-14: 3 waves
        int maxWaves;
        if (difficulty >= 11) {
            maxWaves = 3; // High difficulty: 3 waves including boss wave
        } else if (difficulty >= 6) {
            maxWaves = 2; // Mid difficulty: 2 waves
        } else {
            maxWaves = 1; // Low difficulty: 1 wave
        }

        // Calculate wave sizes: wave 1 = base, wave 2 = base+2, wave 3 (boss) = smaller + boss
        int[] waveSizes = new int[maxWaves];
        waveSizes[0] = baseWaveSize;
        if (maxWaves >= 2) {
            waveSizes[1] = baseWaveSize + 2;
        }
        if (maxWaves >= 3) {
            waveSizes[2] = Math.max(3, baseWaveSize / 2) + 1; // smaller wave + boss mob
        }

        int totalMobs = 0;
        for (int ws : waveSizes) totalMobs += ws;

        // Create raid state in warning phase
        RaidState raid = new RaidState(center, RAID_DURATION, totalMobs, difficulty, leaderId,
            maxWaves, waveSizes);

        activeRaids.put(factionId, raid);

        // Create raid boss bar for this faction
        ServerBossEvent bossBar = new ServerBossEvent(
                Component.literal("Colony Under Attack!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.NOTCHED_10);
        bossBar.setProgress(1.0f);
        raidBossBars.put(factionId, bossBar);

        // Add all faction players to the boss bar
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            String playerFaction = FactionManager.get(level).getPlayerFaction(player.getUUID());
            if (factionId.equals(playerFaction)) {
                bossBar.addPlayer(player);
            }
        }

        // Send warning message
        notifyFaction(level, factionId,
            Component.literal("\u26A0 RAID WARNING! \u26A0")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        notifyFaction(level, factionId,
            Component.literal("Raiders approaching your colony near " +
                center.getX() + ", " + center.getY() + ", " + center.getZ() + "!")
                .withStyle(ChatFormatting.YELLOW));
        notifyFaction(level, factionId,
            Component.literal("Attack begins in 30 seconds! Prepare your defenses! ("
                + maxWaves + " waves, difficulty " + difficulty + ")")
                .withStyle(ChatFormatting.GOLD));

        // Play raid horn sound
        playRaidHornForFaction(level, factionId, center);

        MegaMod.LOGGER.info("Colony raid started for faction {} at {} with {} total mobs across {} waves (difficulty {})",
            factionName, center, totalMobs, maxWaves, difficulty);
    }

    private static void spawnRaidWave(ServerLevel level, BlockPos center, int count, int difficulty, int waveNumber) {
        for (int i = 0; i < count; i++) {
            int dx = random.nextInt(31) - 15;
            int dz = random.nextInt(31) - 15;
            BlockPos spawnPos = center.offset(dx, 0, dz);
            // Find surface
            spawnPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);

            EntityType<? extends Mob> mobType;
            boolean isBoss = false;

            // Wave 3 last mob is always a boss
            if (waveNumber == 3 && i == count - 1) {
                mobType = pickBossMob(difficulty);
                isBoss = true;
            } else {
                mobType = pickRaidMobForWave(difficulty, waveNumber, random);
            }

            Mob mob = (Mob) mobType.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (mob == null) continue;
            mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

            // Scale mob stats by difficulty
            float hpMult = 1.0f + (difficulty - 1) * 0.5f;
            float dmgMult = 1.0f + (difficulty - 1) * 0.3f;

            // Boss mobs get extra HP
            if (isBoss) {
                hpMult *= 3.0f;
                dmgMult *= 1.5f;
            }

            var hp = mob.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) hp.setBaseValue(hp.getBaseValue() * hpMult);
            var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * dmgMult);
            mob.setHealth(mob.getMaxHealth());

            mob.addTag("colony_raider");
            if (isBoss) {
                mob.addTag("colony_raid_boss");
                mob.setCustomName(Component.literal("Raid Boss").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                mob.setCustomNameVisible(true);
            }

            // Enable door breaking for raiders at difficulty 5+
            if (RaidDifficultyScaler.shouldBreakDoors(difficulty)) {
                if (mob instanceof net.minecraft.world.entity.monster.zombie.Zombie zombie) {
                    zombie.setCanBreakDoors(true);
                }
            }

            level.addFreshEntity(mob);
        }
    }

    /**
     * Pick mob type based on wave number:
     * Wave 1: Basic mobs (zombies, skeletons, spiders)
     * Wave 2: Mid-tier mobs (pillagers, vindicators, witches)
     * Wave 3: Escorts for boss (tough mobs)
     * At difficulty 3+, add wither skeletons and piglins
     */
    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> pickRaidMobForWave(int difficulty, int waveNumber, Random rng) {
        if (waveNumber == 1) {
            // Wave 1: Basic mobs
            return switch (rng.nextInt(3)) {
                case 0 -> EntityType.ZOMBIE;
                case 1 -> EntityType.SKELETON;
                default -> EntityType.SPIDER;
            };
        } else if (waveNumber == 2) {
            // Wave 2: Mid-tier mobs
            if (difficulty >= 3) {
                // Add wither skeletons and piglins at high difficulty
                return switch (rng.nextInt(6)) {
                    case 0 -> EntityType.PILLAGER;
                    case 1 -> EntityType.VINDICATOR;
                    case 2 -> EntityType.WITCH;
                    case 3 -> EntityType.WITHER_SKELETON;
                    case 4 -> EntityType.PIGLIN_BRUTE;
                    default -> EntityType.VINDICATOR;
                };
            }
            return switch (rng.nextInt(4)) {
                case 0 -> EntityType.PILLAGER;
                case 1 -> EntityType.VINDICATOR;
                case 2 -> EntityType.WITCH;
                default -> EntityType.PILLAGER;
            };
        } else {
            // Wave 3: Tough escort mobs
            if (difficulty >= 4) {
                return switch (rng.nextInt(5)) {
                    case 0 -> EntityType.VINDICATOR;
                    case 1 -> EntityType.WITHER_SKELETON;
                    case 2 -> EntityType.PIGLIN_BRUTE;
                    case 3 -> EntityType.EVOKER;
                    default -> EntityType.WITCH;
                };
            }
            return switch (rng.nextInt(4)) {
                case 0 -> EntityType.VINDICATOR;
                case 1 -> EntityType.EVOKER;
                case 2 -> EntityType.WITCH;
                default -> EntityType.WITHER_SKELETON;
            };
        }
    }

    /**
     * Pick boss mob for wave 3.
     * Difficulty 4 = Ravager (mini-boss with extra HP).
     * Difficulty 3 = Ravager or Evoker.
     * Lower difficulties shouldn't reach wave 3, but fallback to Ravager.
     */
    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> pickBossMob(int difficulty) {
        if (difficulty >= 4) {
            return EntityType.RAVAGER; // Mini-boss with extra HP applied in spawn method
        } else if (difficulty >= 3) {
            return random.nextBoolean() ? EntityType.RAVAGER : EntityType.EVOKER;
        }
        return EntityType.RAVAGER;
    }

    /**
     * Notify citizen entities in the raid area to react.
     * Workers flee to bed positions; recruits engage raiders.
     */
    private static void notifyCitizensOfRaid(ServerLevel level, BlockPos raidCenter, UUID ownerId) {
        AABB raidArea = new AABB(
            raidCenter.getX() - 80, raidCenter.getY() - 50, raidCenter.getZ() - 80,
            raidCenter.getX() + 80, raidCenter.getY() + 50, raidCenter.getZ() + 80);

        for (MCEntityCitizen citizen : level.getEntitiesOfClass(MCEntityCitizen.class, raidArea)) {
            // Tag citizen as in a raid for AI checks
            citizen.addTag("in_raid");
            // MCEntityCitizen handles raid behavior through its job handler
        }
    }

    /**
     * Clear raid tags from citizens after raid ends.
     */
    private static void clearCitizenRaidTags(ServerLevel level, BlockPos raidCenter, UUID ownerId) {
        AABB raidArea = new AABB(
            raidCenter.getX() - 80, raidCenter.getY() - 50, raidCenter.getZ() - 80,
            raidCenter.getX() + 80, raidCenter.getY() + 50, raidCenter.getZ() + 80);

        for (MCEntityCitizen citizen : level.getEntitiesOfClass(MCEntityCitizen.class, raidArea)) {
            citizen.removeTag("in_raid");
        }
    }

    private static void endRaid(ServerLevel level, String factionId, RaidState raid) {
        // Remove the boss bar
        ServerBossEvent bossBar = raidBossBars.remove(factionId);
        if (bossBar != null) {
            bossBar.removeAllPlayers();
        }

        // Count surviving raiders within 50 blocks of raid center
        int survivingRaiders = countSurvivingRaiders(level, raid.center);

        // Clean up all remaining raiders
        cleanupRaiders(level, raid.center);

        // Calculate total kills across all waves
        int totalKills = raid.totalMobsKilled + (raid.waveSizes[raid.currentWave - 1] - survivingRaiders);
        int wavesCompleted = raid.currentWave;

        // Scale reward based on waves completed and difficulty
        double rewardMultiplier;
        if (wavesCompleted >= raid.maxWaves) {
            rewardMultiplier = 1.0; // Full reward for all waves
        } else if (wavesCompleted >= 2) {
            rewardMultiplier = 0.6; // 60% for 2 waves
        } else {
            rewardMultiplier = 0.3; // 30% for 1 wave
        }
        // Reward scales with difficulty: higher difficulty = more coins + XP
        int coinReward = (int) (totalKills * 15 * raid.difficulty * rewardMultiplier);
        int xpReward = (int) (totalKills * 5 * raid.difficulty * rewardMultiplier);

        // Record faction stats
        FactionStatsManager factionStats = FactionStatsManager.get(level);
        factionStats.addKills(factionId, totalKills);
        boolean perfectDefense = survivingRaiders == 0 && wavesCompleted >= raid.maxWaves;
        if (perfectDefense) {
            factionStats.recordRaidDefended(factionId);
        } else {
            factionStats.recordRaidFailed(factionId);
        }
        factionStats.saveToDisk(level);

        // Award defender coins + XP
        EconomyManager eco = EconomyManager.get(level);
        eco.addWallet(raid.leaderId, coinReward);

        // Award XP to the leader
        ServerPlayer leader = level.getServer().getPlayerList().getPlayer(raid.leaderId);
        if (leader != null) {
            leader.giveExperiencePoints(xpReward);
        }

        // Award Marks of Mastery for perfect defense (0 survivors, all waves)
        if (perfectDefense) {
            if (leader != null) {
                ServerLevel overworld = level.getServer().overworld();
                MasteryMarkManager.get(overworld).awardMilestone(leader,
                    "raid_defend_" + factionId, 10, "Defended colony from raid");
            }
        }

        // Build result message
        String resultMsg;
        if (perfectDefense) {
            resultMsg = "\u2694 Raid defeated! All " + raid.maxWaves + " waves eliminated! Perfect defense!";
        } else if (survivingRaiders == 0) {
            resultMsg = "\u2694 Raid ended. Cleared " + wavesCompleted + "/" + raid.maxWaves + " waves.";
        } else {
            resultMsg = "\u2694 Raid ended. " + survivingRaiders + " raiders escaped. ("
                + wavesCompleted + "/" + raid.maxWaves + " waves)";
        }

        // Notify faction members
        notifyFaction(level, factionId,
            Component.literal(resultMsg).withStyle(perfectDefense ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        notifyFaction(level, factionId,
            Component.literal("+" + coinReward + " MegaCoins, +" + xpReward + " XP (raid defense reward"
                + (rewardMultiplier < 1.0 ? ", " + (int)(rewardMultiplier * 100) + "% wave bonus" : ", full bonus")
                + ")").withStyle(ChatFormatting.GOLD));

        // Clear raid tags from citizens
        clearCitizenRaidTags(level, raid.center, raid.leaderId);
    }

    // ---- Helper Methods ----

    private static int countSurvivingRaiders(ServerLevel level, BlockPos center) {
        int count = 0;
        AABB raidArea = new AABB(
            center.getX() - 50, center.getY() - 50, center.getZ() - 50,
            center.getX() + 50, center.getY() + 50, center.getZ() + 50);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, raidArea)) {
            if (mob.getTags().contains("colony_raider") && mob.isAlive()) {
                count++;
            }
        }
        return count;
    }

    private static void cleanupRaiders(ServerLevel level, BlockPos center) {
        AABB raidArea = new AABB(
            center.getX() - 50, center.getY() - 50, center.getZ() - 50,
            center.getX() + 50, center.getY() + 50, center.getZ() + 50);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, raidArea)) {
            if (mob.getTags().contains("colony_raider") && mob.isAlive()) {
                mob.discard();
            }
        }
    }

    private static void notifyFaction(ServerLevel level, String factionId, Component message) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            String playerFaction = FactionManager.get(level).getPlayerFaction(player.getUUID());
            if (factionId.equals(playerFaction)) {
                player.sendSystemMessage(message);
            }
        }
    }

    private static void playRaidHornForFaction(ServerLevel level, String factionId, BlockPos center) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            String playerFaction = FactionManager.get(level).getPlayerFaction(player.getUUID());
            if (factionId.equals(playerFaction)) {
                level.playSound(null, player.blockPosition(), (net.minecraft.sounds.SoundEvent) SoundEvents.RAID_HORN.value(),
                    SoundSource.HOSTILE, 2.0f, 1.0f);
            }
        }
    }

    public static boolean isRaidActive(String factionId) {
        return activeRaids.containsKey(factionId);
    }

    // ===========================================================================
    // Culture-Based Raid System (Phase 5)
    // ===========================================================================

    /**
     * Start a culture-based raid using the new raider entity system.
     * This creates a RaidEvent with culture-specific raiders instead of vanilla mobs.
     *
     * @param level        the server level
     * @param colonyCenter the center position of the colony being raided
     * @param culture      the raider culture to use
     * @return the created RaidEvent, or null if creation failed
     */
    public static RaidEvent startCultureRaid(ServerLevel level, BlockPos colonyCenter, RaiderCulture culture) {
        int raiderCount = RaidDifficultyScaler.calculateRaiderCount(raidDifficulty,
                getCitizenCountNearby(level, colonyCenter));
        int[] waveSizes = RaidDifficultyScaler.calculateWaves(raidDifficulty, raiderCount);

        int raidId = nextRaidId++;
        RaidEvent event = new RaidEvent(raidId, culture, colonyCenter, raidDifficulty,
                waveSizes.length, waveSizes, level.getGameTime());

        activeCultureRaids.add(event);

        // Create boss bar for culture raid
        ServerBossEvent cultureBar = new ServerBossEvent(
                Component.literal(culture.getDisplayName() + " Raid Incoming!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.NOTCHED_10);
        cultureBar.setProgress(1.0f);
        cultureRaidBossBars.put(raidId, cultureBar);

        // Play horn and notify nearby players
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.blockPosition().closerThan(colonyCenter, 100)) {
                player.sendSystemMessage(
                        Component.literal("\u26A0 " + culture.getDisplayName() + " RAID WARNING! \u26A0")
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                player.sendSystemMessage(
                        Component.literal(culture.getDisplayName() + " raiders approaching near " +
                                colonyCenter.getX() + ", " + colonyCenter.getY() + ", " + colonyCenter.getZ() + "!")
                                .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(
                        Component.literal("Attack begins in 30 seconds! (" + waveSizes.length +
                                " waves, difficulty " + raidDifficulty + ")")
                                .withStyle(ChatFormatting.GOLD));
                level.playSound(null, player.blockPosition(),
                        (net.minecraft.sounds.SoundEvent) SoundEvents.RAID_HORN.value(),
                        SoundSource.HOSTILE, 2.0f, 1.0f);
                cultureBar.addPlayer(player);
            }
        }

        MegaMod.LOGGER.info("Culture raid started: {} (id={}) at {} with {} raiders across {} waves (difficulty {})",
                culture.getDisplayName(), raidId, colonyCenter, raiderCount, waveSizes.length, raidDifficulty);

        nightsSinceLastRaid = 0;
        return event;
    }

    /**
     * Schedules a raid to start after the given number of ticks.
     * Used by the /mc colony raid tonight command.
     */
    public static void scheduleRaid(ServerLevel level, BlockPos center, long delayTicks) {
        long targetTick = level.getGameTime() + delayTicks;
        // Store as a pending scheduled raid using a simple approach:
        // We'll piggyback on the nextRaidCheck mechanism
        scheduledRaidCenter = center;
        scheduledRaidTick = targetTick;
        MegaMod.LOGGER.info("Raid scheduled at {} for tick {} (in {} ticks)",
            center, targetTick, delayTicks);
    }

    // Scheduled raid fields (for /mc colony raid tonight)
    private static BlockPos scheduledRaidCenter = null;
    private static long scheduledRaidTick = -1;

    /** Returns true if a raid is scheduled or will happen tonight (used by Ancient Tome glow). */
    public static boolean isRaidScheduledTonight() {
        return scheduledRaidCenter != null || !activeCultureRaids.isEmpty() || nightsSinceLastRaid >= 3;
    }

    /**
     * Tick all active culture-based raids. Should be called each server tick.
     */
    public static void tickCultureRaids(ServerLevel level) {
        if (activeCultureRaids.isEmpty()) return;

        var iterator = activeCultureRaids.iterator();
        while (iterator.hasNext()) {
            RaidEvent event = iterator.next();
            boolean finished = event.tick(level);

            // Update boss bar for this culture raid
            ServerBossEvent cBar = cultureRaidBossBars.get(event.getRaidId());
            if (cBar != null && !finished) {
                if (event.getStatus() == RaidEvent.EventStatus.ACTIVE) {
                    int alive = event.getTotalRaiders() - event.getKilledRaiders();
                    float progress = event.getTotalRaiders() > 0
                            ? (float) Math.max(0, alive) / event.getTotalRaiders()
                            : 0.0f;
                    cBar.setProgress(Math.max(0.0f, Math.min(1.0f, progress)));
                    cBar.setName(Component.literal(event.getCulture().getDisplayName() + " Raid - Wave "
                            + event.getCurrentWave() + "/" + event.getMaxWaves()
                            + " - " + Math.max(0, alive) + " raiders")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                }
            }

            if (finished) {
                onCultureRaidEnd(level, event);
                iterator.remove();
            }
        }
    }

    /**
     * Handle cleanup and rewards when a culture raid ends.
     */
    private static void onCultureRaidEnd(ServerLevel level, RaidEvent event) {
        // Remove boss bar
        ServerBossEvent cBar = cultureRaidBossBars.remove(event.getRaidId());
        if (cBar != null) {
            cBar.removeAllPlayers();
        }

        boolean allKilled = event.getKilledRaiders() >= event.getTotalRaiders();

        // Adjust difficulty based on outcome (0 deaths = harder next time)
        int effectiveCitizenDeaths = allKilled ? 0 : 2;
        raidDifficulty = RaidDifficultyScaler.adjustDifficulty(raidDifficulty, effectiveCitizenDeaths);

        // Reward nearby players — scales with difficulty
        int coinReward = event.getKilledRaiders() * 15 * event.getDifficulty();
        int xpReward = event.getKilledRaiders() * 5 * event.getDifficulty();
        // Bonus for all waves completed
        if (allKilled) {
            coinReward = (int) (coinReward * 1.5);
            xpReward = (int) (xpReward * 1.5);
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.blockPosition().closerThan(event.getTargetCenter(), 100)) {
                String msg;
                if (allKilled) {
                    msg = "\u2694 " + event.getCulture().getDisplayName() + " raid defeated! All waves eliminated!";
                } else {
                    msg = "\u2694 " + event.getCulture().getDisplayName() + " raid ended. "
                            + event.getKilledRaiders() + "/" + event.getTotalRaiders() + " raiders eliminated.";
                }
                player.sendSystemMessage(Component.literal(msg)
                        .withStyle(allKilled ? ChatFormatting.GREEN : ChatFormatting.YELLOW));

                EconomyManager eco = EconomyManager.get(level);
                eco.addWallet(player.getUUID(), coinReward);
                player.giveExperiencePoints(xpReward);
                player.sendSystemMessage(Component.literal("+" + coinReward + " MegaCoins, +" + xpReward + " XP (raid defense)")
                        .withStyle(ChatFormatting.GOLD));

                // Mastery mark for perfect defense
                if (allKilled) {
                    ServerLevel overworld = level.getServer().overworld();
                    MasteryMarkManager.get(overworld).awardMilestone(player,
                            "culture_raid_defend_" + event.getRaidId(), 10,
                            "Defeated " + event.getCulture().getDisplayName() + " raid");
                }
            }
        }

        MegaMod.LOGGER.info("Culture raid {} ({}) ended. Killed: {}/{}. New difficulty: {}",
                event.getRaidId(), event.getCulture().getDisplayName(),
                event.getKilledRaiders(), event.getTotalRaiders(), raidDifficulty);
    }

    /**
     * Track nights passed for culture raid scheduling.
     */
    public static void trackNightCycle(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000L;
        if (lastDayChecked < 0) {
            lastDayChecked = currentDay;
            return;
        }
        if (currentDay > lastDayChecked) {
            nightsSinceLastRaid += (int) (currentDay - lastDayChecked);
            lastDayChecked = currentDay;
        }
    }

    /**
     * Get the current number of nights since last culture raid.
     */
    public static int getNightsSinceLastRaid() {
        return nightsSinceLastRaid;
    }

    /**
     * Get the current raid difficulty level.
     */
    public static int getRaidDifficulty() {
        return raidDifficulty;
    }

    /**
     * Set the raid difficulty level (for admin commands).
     */
    public static void setRaidDifficulty(int difficulty) {
        raidDifficulty = Math.max(RaidDifficultyScaler.MIN_DIFFICULTY,
                Math.min(RaidDifficultyScaler.MAX_DIFFICULTY, difficulty));
    }

    /**
     * Get all active culture-based raids.
     */
    public static List<RaidEvent> getActiveCultureRaids() {
        return Collections.unmodifiableList(activeCultureRaids);
    }

    /**
     * Check if any culture-based raid is active.
     */
    public static boolean hasActiveCultureRaid() {
        return !activeCultureRaids.isEmpty();
    }

    /**
     * Count citizens near a position (for culture raids that don't use faction IDs).
     */
    private static int getCitizenCountNearby(ServerLevel level, BlockPos center) {
        AABB area = new AABB(
                center.getX() - 80, center.getY() - 50, center.getZ() - 80,
                center.getX() + 80, center.getY() + 50, center.getZ() + 80);
        return level.getEntitiesOfClass(MCEntityCitizen.class, area).size();
    }

    // ---- Raid State ----

    private static class RaidState {
        final BlockPos center;
        int ticksRemaining;
        final int totalMobs;
        final int difficulty;
        final UUID leaderId;

        // Multi-wave tracking
        int currentWave;
        final int maxWaves;
        final int[] waveSizes;
        int mobsRemainingInWave;
        int totalMobsKilled;

        // Warning phase
        boolean inWarningPhase;
        int warningTicksRemaining;

        RaidState(BlockPos center, int ticksRemaining, int totalMobs, int difficulty, UUID leaderId,
                  int maxWaves, int[] waveSizes) {
            this.center = center;
            this.ticksRemaining = ticksRemaining;
            this.totalMobs = totalMobs;
            this.difficulty = difficulty;
            this.leaderId = leaderId;

            this.currentWave = 1;
            this.maxWaves = maxWaves;
            this.waveSizes = waveSizes;
            this.mobsRemainingInWave = 0;
            this.totalMobsKilled = 0;

            // Start in warning phase
            this.inWarningPhase = true;
            this.warningTicksRemaining = WARNING_TICKS;
        }
    }
}
