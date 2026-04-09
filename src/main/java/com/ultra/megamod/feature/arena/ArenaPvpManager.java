package com.ultra.megamod.feature.arena;

import com.ultra.megamod.MegaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * ELO rating and PvP matchmaking for the Arena system.
 */
public class ArenaPvpManager {

    private static final int ELO_K = 32;
    private static final int MATCH_ELO_RANGE = 200;
    private static final int ROUND_DURATION_SEC = 60;
    private static final int ROUNDS_TO_WIN = 2; // Best of 3

    private static final List<UUID> pvpQueue = new ArrayList<>();

    /**
     * Add a player to the PvP queue.
     */
    public static boolean queueForMatch(UUID playerId) {
        if (pvpQueue.contains(playerId)) return false;
        pvpQueue.add(playerId);
        return true;
    }

    /**
     * Remove a player from the PvP queue.
     */
    public static void dequeueFromMatch(UUID playerId) {
        pvpQueue.remove(playerId);
    }

    /**
     * Check if a player is in the PvP queue.
     */
    public static boolean isQueued(UUID playerId) {
        return pvpQueue.contains(playerId);
    }

    /**
     * Attempt to match two players with similar ELO ratings.
     * Called periodically from ArenaEvents tick handler.
     */
    public static void tryMatchmaking(ServerLevel overworld) {
        if (pvpQueue.size() < 2) return;

        ArenaManager arenaManager = ArenaManager.get(overworld);

        // Try to pair players within ELO range
        for (int i = 0; i < pvpQueue.size(); i++) {
            UUID p1Uuid = pvpQueue.get(i);
            ArenaManager.ArenaStats stats1 = arenaManager.getOrCreateStats(p1Uuid);
            ServerPlayer p1 = overworld.getServer().getPlayerList().getPlayer(p1Uuid);
            if (p1 == null) {
                pvpQueue.remove(i);
                i--;
                continue;
            }

            for (int j = i + 1; j < pvpQueue.size(); j++) {
                UUID p2Uuid = pvpQueue.get(j);
                ArenaManager.ArenaStats stats2 = arenaManager.getOrCreateStats(p2Uuid);
                ServerPlayer p2 = overworld.getServer().getPlayerList().getPlayer(p2Uuid);
                if (p2 == null) {
                    pvpQueue.remove(j);
                    j--;
                    continue;
                }

                int eloDiff = Math.abs(stats1.eloRating - stats2.eloRating);
                if (eloDiff <= MATCH_ELO_RANGE) {
                    // Match found
                    pvpQueue.remove(j);
                    pvpQueue.remove(i);

                    String id = arenaManager.createPvpArena(p1, p2, overworld);
                    if (id != null) {
                        p1.sendSystemMessage(Component.literal("PvP match found! Fighting " + p2.getGameProfile().name())
                                .withStyle(ChatFormatting.RED));
                        p2.sendSystemMessage(Component.literal("PvP match found! Fighting " + p1.getGameProfile().name())
                                .withStyle(ChatFormatting.RED));
                        MegaMod.LOGGER.info("PvP match created: {} vs {}", p1.getGameProfile().name(), p2.getGameProfile().name());
                    }
                    return;
                }
            }
        }
    }

    /**
     * Calculate new ELO ratings after a PvP match.
     * Returns int[] {newWinnerElo, newLoserElo}.
     */
    public static int[] calculateNewRatings(int winnerElo, int loserElo) {
        double expectedWinner = 1.0 / (1.0 + Math.pow(10.0, (loserElo - winnerElo) / 400.0));
        double expectedLoser = 1.0 / (1.0 + Math.pow(10.0, (winnerElo - loserElo) / 400.0));

        int newWinner = (int) Math.round(winnerElo + ELO_K * (1.0 - expectedWinner));
        int newLoser = (int) Math.round(loserElo + ELO_K * (0.0 - expectedLoser));

        // Floor at 100
        newLoser = Math.max(100, newLoser);

        return new int[]{newWinner, newLoser};
    }

    /**
     * Handle a PvP round result. Checks if one player killed the other.
     */
    public static void handlePvpKill(UUID killerUuid, UUID victimUuid, ServerLevel overworld) {
        ArenaManager arenaManager = ArenaManager.get(overworld);
        ArenaManager.ArenaInstance instance = arenaManager.getInstanceForPlayer(killerUuid);
        if (instance == null || instance.mode != ArenaManager.ArenaMode.PVP) return;

        // Determine which player index the killer is
        int killerIdx = instance.players.indexOf(killerUuid);
        if (killerIdx < 0 || killerIdx > 1) return;

        instance.roundScores[killerIdx]++;

        ServerPlayer killer = overworld.getServer().getPlayerList().getPlayer(killerUuid);
        ServerPlayer victim = overworld.getServer().getPlayerList().getPlayer(victimUuid);

        String roundMsg = "Round " + instance.currentRound + " won by " +
                (killer != null ? killer.getGameProfile().name() : "Player " + (killerIdx + 1)) +
                "! Score: " + instance.roundScores[0] + " - " + instance.roundScores[1];

        // Notify both players
        for (UUID pid : instance.players) {
            ServerPlayer p = overworld.getServer().getPlayerList().getPlayer(pid);
            if (p != null) {
                p.sendSystemMessage(Component.literal(roundMsg).withStyle(ChatFormatting.YELLOW));
            }
        }

        // Check for match winner (best of 3)
        if (instance.roundScores[killerIdx] >= ROUNDS_TO_WIN) {
            // Match over - killer wins
            UUID winnerUuid = killerUuid;
            UUID loserUuid = instance.players.get(1 - killerIdx);

            ArenaManager.ArenaStats winnerStats = arenaManager.getOrCreateStats(winnerUuid);
            ArenaManager.ArenaStats loserStats = arenaManager.getOrCreateStats(loserUuid);

            int[] newRatings = calculateNewRatings(winnerStats.eloRating, loserStats.eloRating);
            winnerStats.eloRating = newRatings[0];
            loserStats.eloRating = newRatings[1];
            winnerStats.pvpWins++;
            loserStats.pvpLosses++;

            ServerLevel pocketLevel = overworld.getServer().getLevel(
                    com.ultra.megamod.feature.dimensions.MegaModDimensions.DUNGEON);
            if (pocketLevel != null) {
                arenaManager.endArena(instance.instanceId, true, pocketLevel);
            }

            // Award coins to winner
            com.ultra.megamod.feature.economy.EconomyManager eco =
                    com.ultra.megamod.feature.economy.EconomyManager.get(overworld);
            eco.addWallet(winnerUuid, 150);

            if (killer != null) {
                killer.sendSystemMessage(Component.literal("You won the PvP match! +150 MC, ELO: " + newRatings[0])
                        .withStyle(ChatFormatting.GREEN));
            }
            ServerPlayer loser = overworld.getServer().getPlayerList().getPlayer(loserUuid);
            if (loser != null) {
                loser.sendSystemMessage(Component.literal("You lost the PvP match. ELO: " + newRatings[1])
                        .withStyle(ChatFormatting.RED));
            }
        } else {
            // Next round
            instance.currentRound++;
            instance.roundStartTime = System.currentTimeMillis();

            // Respawn victim and reset positions
            if (victim != null) {
                victim.setHealth(victim.getMaxHealth());
                BlockPos spawn2 = instance.origin.offset(10, 1, 18);
                victim.teleportTo((ServerLevel) victim.level(),
                        spawn2.getX() + 0.5, spawn2.getY(), spawn2.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 0.0f, false);
            }
            if (killer != null) {
                killer.setHealth(killer.getMaxHealth());
                BlockPos spawn1 = instance.origin.offset(10, 1, 2);
                killer.teleportTo((ServerLevel) killer.level(),
                        spawn1.getX() + 0.5, spawn1.getY(), spawn1.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 0.0f, false);
            }
        }
    }

    /**
     * Check PvP round timer. If 60s elapsed with no kill, the round is a draw (no score change).
     */
    public static void checkRoundTimers(ServerLevel overworld) {
        ArenaManager arenaManager = ArenaManager.get(overworld);
        long now = System.currentTimeMillis();

        Iterator<ArenaManager.ArenaInstance> it = new ArrayList<>(arenaManager.getActiveInstances().values()).iterator();
        while (it.hasNext()) {
            ArenaManager.ArenaInstance instance = it.next();
            if (instance.mode != ArenaManager.ArenaMode.PVP || instance.state != ArenaManager.ArenaState.ACTIVE) continue;

            long elapsed = (now - instance.roundStartTime) / 1000;
            if (elapsed >= ROUND_DURATION_SEC) {
                // Round timed out — draw, advance to next round
                instance.currentRound++;
                instance.roundStartTime = now;

                for (UUID pid : instance.players) {
                    ServerPlayer p = overworld.getServer().getPlayerList().getPlayer(pid);
                    if (p != null) {
                        p.sendSystemMessage(Component.literal("Round timed out! Score: " +
                                instance.roundScores[0] + " - " + instance.roundScores[1])
                                .withStyle(ChatFormatting.YELLOW));

                        // Heal players for next round
                        p.setHealth(p.getMaxHealth());
                    }
                }

                // If all 3 rounds are over and no winner, end in draw
                if (instance.currentRound > 3) {
                    ServerLevel pocketLevel = overworld.getServer().getLevel(
                            com.ultra.megamod.feature.dimensions.MegaModDimensions.DUNGEON);
                    if (pocketLevel != null) {
                        arenaManager.endArena(instance.instanceId, false, pocketLevel);
                    }

                    for (UUID pid : instance.players) {
                        ServerPlayer p = overworld.getServer().getPlayerList().getPlayer(pid);
                        if (p != null) {
                            p.sendSystemMessage(Component.literal("PvP match ended in a draw!")
                                    .withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }
        }
    }

    /**
     * Clean up queue on server stop.
     */
    public static void clearQueue() {
        pvpQueue.clear();
    }

    /**
     * Get the queue size.
     */
    public static int getQueueSize() {
        return pvpQueue.size();
    }
}
