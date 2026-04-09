package com.ultra.megamod.feature.arena;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.NewGamePlusManager;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Boss Rush sub-mode. Spawns all 8 dungeon bosses sequentially.
 * Access gate: player must have completed all bosses on INFERNAL tier.
 */
public class BossRushManager {

    /**
     * Boss order for Boss Rush mode, using registered dungeon boss entity types.
     */
    private static final String[] BOSS_IDS = {
            "wraith", "ossukage", "dungeon_keeper", "frostmaw",
            "wroughtnaut", "umvuthi", "chaos_spawner", "sculptor"
    };

    private static final String[] BOSS_NAMES = {
            "The Wraith", "Ossukage", "Dungeon Keeper", "Frostmaw",
            "Ferrous Wroughtnaut", "Umvuthi", "Chaos Spawner", "The Sculptor"
    };

    private static final long INTERMISSION_MS = 5000; // 5 seconds between bosses

    /**
     * Check if a player has access to Boss Rush mode.
     */
    public static boolean hasAccess(UUID playerId, ServerLevel overworld) {
        return NewGamePlusManager.get(overworld).hasCompletedAllBossesInTier(playerId, "INFERNAL");
    }

    /**
     * Spawn the next boss in the Boss Rush sequence.
     */
    @SuppressWarnings("unchecked")
    public static void spawnNextBoss(ServerLevel level, ArenaManager.ArenaInstance instance) {
        if (instance.currentBossIndex >= BOSS_IDS.length) {
            // All bosses defeated — run complete
            return;
        }

        Supplier<? extends EntityType<?>> bossTypeSupplier = getBossTypeSupplier(instance.currentBossIndex);
        if (bossTypeSupplier == null) {
            MegaMod.LOGGER.warn("Boss Rush: unknown boss index {}", instance.currentBossIndex);
            return;
        }

        EntityType<?> entityType = bossTypeSupplier.get();
        Mob boss = (Mob) entityType.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (boss == null) {
            MegaMod.LOGGER.warn("Boss Rush: failed to create boss {}", BOSS_IDS[instance.currentBossIndex]);
            return;
        }

        BlockPos spawnPos = instance.origin.offset(15, 1, 15);
        boss.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        boss.setPersistenceRequired();

        level.addFreshEntity(boss);
        instance.mobsAlive = 1;

        // Notify players
        String bossName = BOSS_NAMES[instance.currentBossIndex];
        for (UUID playerId : instance.players) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                player.sendSystemMessage(Component.literal("Boss " + (instance.currentBossIndex + 1) + "/8: " + bossName + "!")
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }

        MegaMod.LOGGER.info("Boss Rush: spawned {} (boss {}/{})", bossName, instance.currentBossIndex + 1, BOSS_IDS.length);
    }

    /**
     * Check Boss Rush progress. Called from ArenaEvents tick.
     */
    public static void checkBossRushProgress(ServerLevel pocketLevel, ArenaManager.ArenaInstance instance, ArenaManager arenaManager) {
        if (instance.mode != ArenaManager.ArenaMode.BOSS_RUSH) return;
        if (instance.state != ArenaManager.ArenaState.ACTIVE) return;

        long now = System.currentTimeMillis();

        // If in intermission, wait for it to end
        if (instance.intermissionEnd > 0) {
            if (now >= instance.intermissionEnd) {
                instance.intermissionEnd = 0;
                instance.currentBossIndex++;

                if (instance.currentBossIndex >= BOSS_IDS.length) {
                    // All 8 bosses defeated — Boss Rush complete
                    long totalTime = now - instance.startTime;

                    // Record time on leaderboard
                    ServerLevel overworld = pocketLevel.getServer().overworld();
                    BossRushLeaderboard leaderboard = BossRushLeaderboard.get(overworld);
                    for (UUID playerId : instance.players) {
                        leaderboard.recordTime(playerId, totalTime);
                        ArenaManager.ArenaStats stats = arenaManager.getOrCreateStats(playerId);
                        if (stats.bestBossRushTime == 0 || totalTime < stats.bestBossRushTime) {
                            stats.bestBossRushTime = totalTime;
                        }
                    }
                    leaderboard.saveToDisk(overworld);

                    arenaManager.endArena(instance.instanceId, true, pocketLevel);

                    // Notify players of completion time
                    String timeStr = formatTime(totalTime);
                    for (UUID playerId : instance.players) {
                        ServerPlayer player = pocketLevel.getServer().getPlayerList().getPlayer(playerId);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal("Boss Rush complete! Time: " + timeStr)
                                    .withStyle(ChatFormatting.GREEN));
                        }
                    }
                    return;
                }

                // Spawn next boss
                spawnNextBoss(pocketLevel, instance);
            }
            return;
        }

        // Check if current boss is dead
        if (instance.mobsAlive <= 0 && instance.currentBossIndex < BOSS_IDS.length) {
            // Boss defeated — start intermission
            instance.intermissionEnd = now + INTERMISSION_MS;

            String bossName = BOSS_NAMES[instance.currentBossIndex];
            for (UUID playerId : instance.players) {
                ServerPlayer player = pocketLevel.getServer().getPlayerList().getPlayer(playerId);
                if (player != null) {
                    player.sendSystemMessage(Component.literal(bossName + " defeated! Next boss in 5 seconds...")
                            .withStyle(ChatFormatting.GOLD));
                    // Heal player between bosses
                    player.setHealth(player.getMaxHealth());
                }
            }
        }
    }

    /**
     * Get the registered EntityType supplier for a boss by index.
     */
    @SuppressWarnings("unchecked")
    private static Supplier<? extends EntityType<?>> getBossTypeSupplier(int index) {
        switch (index) {
            case 0: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.WRAITH_BOSS;
            case 1: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.OSSUKAGE_BOSS;
            case 2: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.DUNGEON_KEEPER;
            case 3: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.FROSTMAW_BOSS;
            case 4: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.WROUGHTNAUT_BOSS;
            case 5: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.UMVUTHI_BOSS;
            case 6: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.CHAOS_SPAWNER_BOSS;
            case 7: return (Supplier<? extends EntityType<?>>) (Supplier<?>) DungeonEntityRegistry.SCULPTOR_BOSS;
            default: return null;
        }
    }

    /**
     * Format milliseconds as "Xm Ys" or "Xh Ym Zs".
     */
    public static String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}
