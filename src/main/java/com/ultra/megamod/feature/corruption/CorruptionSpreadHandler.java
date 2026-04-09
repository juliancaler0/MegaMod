package com.ultra.megamod.feature.corruption;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.ClaimData;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.corruption.CorruptionManager.CorruptionZone;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

public class CorruptionSpreadHandler {

    // Spread check interval (every 200 ticks = 10 seconds)
    private static final long SPREAD_CHECK_INTERVAL = 200;
    // Colony pushback every 12000 ticks (10 minutes)
    private static final long PUSHBACK_INTERVAL = 12000;
    // Natural spawn check every 72000 ticks (1 MC day cycle)
    private static final long NATURAL_SPAWN_INTERVAL = 72000;
    // 10% chance for natural spawn per check
    private static final double NATURAL_SPAWN_CHANCE = 0.10;
    // Min/max blocks from a player for natural spawn
    private static final int NATURAL_SPAWN_MIN_DISTANCE = 200;
    private static final int NATURAL_SPAWN_MAX_DISTANCE = 500;
    // Never spawn within 128 blocks of claimed territory
    private static final int CLAIM_EXCLUSION_RADIUS = 128;
    // Never spawn within 64 blocks of existing corruption
    private static final int ZONE_EXCLUSION_RADIUS = 64;
    // 5% chance per tick cycle to spawn child zone from tier 3+ parent
    private static final double CHILD_SPAWN_CHANCE = 0.05;
    // Minimum citizen count in area for pushback
    private static final int PUSHBACK_MIN_CITIZENS = 3;

    private static long lastSpreadCheckTick = 0;
    private static long lastPushbackTick = 0;
    private static long lastNaturalSpawnTick = 0;

    private static final Random RANDOM = new Random();

    /**
     * Called each server tick from CorruptionEvents.
     */
    public static void tick(ServerLevel level) {
        long currentTick = level.getServer().getTickCount();
        CorruptionManager cm = CorruptionManager.get(level);

        // Spread logic check (every 200 ticks)
        if (currentTick - lastSpreadCheckTick >= SPREAD_CHECK_INTERVAL) {
            lastSpreadCheckTick = currentTick;
            if (cm.isSpreadEnabled()) {
                processSpread(level, currentTick, cm);
                processChildZoneSpawn(level, currentTick, cm);
            }
        }

        // Colony pushback
        if (currentTick - lastPushbackTick >= PUSHBACK_INTERVAL) {
            lastPushbackTick = currentTick;
            processColonyPushback(level, cm);
        }

        // Natural spawning
        if (currentTick - lastNaturalSpawnTick >= NATURAL_SPAWN_INTERVAL) {
            lastNaturalSpawnTick = currentTick;
            processNaturalSpawn(level, currentTick, cm);
        }
    }

    /**
     * Expand corruption zones based on their tier and spread interval.
     * Each zone grows by 1 block every (600 / tier) ticks.
     * Tier 1: every 12000 ticks (10 min), Tier 4: every 3000 ticks (2.5 min).
     * Colony claimed chunks push back: reduce effective radius by 8 blocks per claimed chunk in zone.
     * Military recruits further reduce by 2 per recruit.
     * Zone cannot spread into chunks with >= 3 military recruits.
     */
    private static void processSpread(ServerLevel level, long currentTick, CorruptionManager cm) {
        ClaimManager claimManager = ClaimManager.get(level);

        for (CorruptionZone zone : cm.getActiveZones()) {
            if (zone.radius >= zone.maxRadius) continue;

            // Check if enough time has passed for this zone to spread
            long elapsed = currentTick - zone.lastSpreadTick;
            if (elapsed < zone.getSpreadInterval()) continue;

            // Check if expansion is blocked by claims
            if (isExpansionBlocked(zone, claimManager, level)) continue;

            cm.expandZone(zone.zoneId);
            zone.lastSpreadTick = currentTick;

            // Increase corruption level
            zone.corruptionLevel = Math.min(100, zone.corruptionLevel + 1);
        }
    }

    /**
     * Check if all frontier chunks around the zone are defended by factions with >= 3 military recruits.
     */
    private static boolean isExpansionBlocked(CorruptionZone zone, ClaimManager claimManager, ServerLevel level) {
        int nextRadius = zone.radius + 1;
        int blocked = 0;
        int frontier = 0;

        // Sample frontier points at the edge of the expansion
        for (int angle = 0; angle < 16; angle++) {
            double rad = angle * Math.PI * 2.0 / 16.0;
            int checkX = (int)(zone.centerX + Math.cos(rad) * nextRadius);
            int checkZ = (int)(zone.centerZ + Math.sin(rad) * nextRadius);
            int chunkX = checkX >> 4;
            int chunkZ = checkZ >> 4;

            frontier++;
            if (claimManager.isChunkClaimed(chunkX, chunkZ)) {
                // Check if the faction has military recruits
                String factionId = claimManager.getFactionAtChunk(chunkX, chunkZ);
                if (factionId != null) {
                    int recruits = countFactionMilitaryRecruits(level, factionId);
                    if (recruits >= PUSHBACK_MIN_CITIZENS) {
                        blocked++;
                    }
                }
            }
        }

        // Blocked only if ALL frontier samples are defended
        return frontier > 0 && blocked >= frontier;
    }

    /**
     * Count military recruits for a faction.
     */
    private static int countFactionMilitaryRecruits(ServerLevel level, String factionId) {
        FactionManager fm = FactionManager.get(level);
        CitizenManager citizenManager = CitizenManager.get(level);
        var factionData = fm.getFaction(factionId);
        if (factionData == null) return 0;

        int count = 0;
        for (UUID memberUuid : factionData.getMembers()) {
            List<CitizenManager.CitizenRecord> citizens = citizenManager.getCitizensForOwner(memberUuid);
            if (citizens != null) {
                for (CitizenManager.CitizenRecord citizen : citizens) {
                    String job = citizen.job() != null ? citizen.job().name() : "";
                    if (job.contains("RECRUIT") || job.contains("SHIELDMAN") ||
                            job.contains("BOWMAN") || job.contains("KNIGHT") || job.contains("GUARD")) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Colony pushback: claimed chunks adjacent to corruption reduce zone radius.
     * Each claimed chunk reduces by 8 blocks, each military recruit by 2 more.
     */
    private static void processColonyPushback(ServerLevel level, CorruptionManager cm) {
        if (cm.getActiveZoneCount() == 0) return;

        ClaimManager claimManager = ClaimManager.get(level);
        FactionManager factionManager = FactionManager.get(level);
        CitizenManager citizenManager = CitizenManager.get(level);

        for (ClaimData claim : claimManager.getAllClaims()) {
            String factionId = claim.getOwnerFactionId();
            if (factionId == null || factionId.isEmpty()) continue;

            int citizenCount = countFactionCitizens(citizenManager, factionManager, factionId);
            if (citizenCount < PUSHBACK_MIN_CITIZENS) continue;

            int recruits = countFactionMilitaryRecruits(level, factionId);

            // Find corruption zones adjacent to this faction's claims
            Set<Integer> processedZones = new HashSet<>();
            for (long[] chunk : claim.getClaimedChunks()) {
                int cx = (int) chunk[0];
                int cz = (int) chunk[1];

                int[][] adjacentOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] offset : adjacentOffsets) {
                    int adjX = cx + offset[0];
                    int adjZ = cz + offset[1];
                    long blockX = adjX * 16L + 8;
                    long blockZ = adjZ * 16L + 8;

                    for (CorruptionZone zone : cm.getActiveZones()) {
                        if (processedZones.contains(zone.zoneId)) continue;
                        if (zone.containsBlock(blockX, blockZ)) {
                            processedZones.add(zone.zoneId);

                            // Calculate pushback: 8 per claimed chunk overlapping + 2 per recruit
                            int claimedChunksInZone = countClaimedChunksInZone(zone, claim);
                            int pushback = claimedChunksInZone * 8 + recruits * 2;

                            boolean destroyed = cm.shrinkZone(zone.zoneId, pushback);
                            notifyFactionMembers(level, factionId, factionManager, destroyed, zone);
                        }
                    }
                }
            }
        }
    }

    /**
     * Count how many of a faction's claimed chunks overlap with a corruption zone.
     */
    private static int countClaimedChunksInZone(CorruptionZone zone, ClaimData claim) {
        int count = 0;
        for (long[] chunk : claim.getClaimedChunks()) {
            if (zone.containsChunk((int) chunk[0], (int) chunk[1])) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count total citizens belonging to a faction.
     */
    private static int countFactionCitizens(CitizenManager citizenManager, FactionManager factionManager, String factionId) {
        var factionData = factionManager.getFaction(factionId);
        if (factionData == null) return 0;

        int count = 0;
        for (UUID memberUuid : factionData.getMembers()) {
            List<CitizenManager.CitizenRecord> citizens = citizenManager.getCitizensForOwner(memberUuid);
            if (citizens != null) {
                count += citizens.size();
            }
        }
        return count;
    }

    /**
     * Notify faction members about corruption pushback.
     */
    private static void notifyFactionMembers(ServerLevel level, String factionId, FactionManager factionManager,
                                              boolean destroyed, CorruptionZone zone) {
        var factionData = factionManager.getFaction(factionId);
        if (factionData == null) return;

        Component message;
        if (destroyed) {
            message = Component.literal("[Corruption] ")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Your colony has purged the darkness! Zone #" + zone.zoneId + " destroyed!")
                            .withStyle(ChatFormatting.GREEN));
        } else {
            message = Component.literal("[Corruption] ")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("Your colony's presence is pushing back the darkness!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        for (UUID memberUuid : factionData.getMembers()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(memberUuid);
            if (player != null) {
                player.sendSystemMessage(message);
            }
        }
    }

    /**
     * Random chance (5% per tick cycle) to spawn a new tier-1 zone within 256 blocks of existing tier-3+ zone.
     */
    private static void processChildZoneSpawn(ServerLevel level, long currentTick, CorruptionManager cm) {
        if (cm.getActiveZoneCount() >= cm.getMaxActiveZones()) return;

        for (CorruptionZone parent : cm.getActiveZones()) {
            if (parent.tier < 3) continue;
            if (RANDOM.nextDouble() >= CHILD_SPAWN_CHANCE) continue;

            // Pick random position within 256 blocks
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            int dist = 64 + RANDOM.nextInt(192); // 64-256 blocks away
            long newX = parent.centerX + (long)(Math.cos(angle) * dist);
            long newZ = parent.centerZ + (long)(Math.sin(angle) * dist);

            // Check exclusion: no existing zone within 64 blocks
            if (!cm.getZonesInRange(newX, newZ, ZONE_EXCLUSION_RADIUS).isEmpty()) continue;

            // Check claim exclusion
            ClaimManager claimManager = ClaimManager.get(level);
            int chunkX = (int)(newX >> 4);
            int chunkZ = (int)(newZ >> 4);
            if (claimManager.isChunkClaimed(chunkX, chunkZ)) continue;

            cm.createZone(newX, newZ, 1, "natural", currentTick);
            MegaMod.LOGGER.info("Child corruption zone spawned near parent #{} at ({}, {})", parent.zoneId, newX, newZ);
            break; // Only spawn one per tick cycle
        }
    }

    /**
     * Natural spawn: every 72000 ticks (1 MC day), 10% chance to spawn tier-1 zone.
     * Max 8 active zones. Spawns 200-500 blocks from a random online player.
     */
    private static void processNaturalSpawn(ServerLevel level, long currentTick, CorruptionManager cm) {
        if (RANDOM.nextDouble() >= NATURAL_SPAWN_CHANCE) return;
        if (cm.getActiveZoneCount() >= cm.getMaxActiveZones()) return;

        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        ServerPlayer target = players.get(RANDOM.nextInt(players.size()));
        if (!(target.level() instanceof ServerLevel sl) || !sl.dimension().equals(level.dimension())) return;

        // Generate position at random distance
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        int distance = NATURAL_SPAWN_MIN_DISTANCE + RANDOM.nextInt(NATURAL_SPAWN_MAX_DISTANCE - NATURAL_SPAWN_MIN_DISTANCE);
        long blockX = (long)(target.getX() + Math.cos(angle) * distance);
        long blockZ = (long)(target.getZ() + Math.sin(angle) * distance);

        // Check exclusions
        if (!cm.getZonesInRange(blockX, blockZ, ZONE_EXCLUSION_RADIUS).isEmpty()) return;

        // Check claim exclusion
        ClaimManager claimManager = ClaimManager.get(level);
        for (ClaimData claim : claimManager.getAllClaims()) {
            for (long[] c : claim.getClaimedChunks()) {
                long claimBlockX = c[0] * 16 + 8;
                long claimBlockZ = c[1] * 16 + 8;
                long dx = claimBlockX - blockX;
                long dz = claimBlockZ - blockZ;
                if (dx * dx + dz * dz <= (long) CLAIM_EXCLUSION_RADIUS * CLAIM_EXCLUSION_RADIUS) {
                    return; // Too close to a colony
                }
            }
        }

        // Create the zone
        CorruptionZone zone = cm.createZone(blockX, blockZ, 1, "natural", currentTick);

        // Get biome name for broadcast
        BlockPos biomeCheckPos = new BlockPos((int) blockX, 64, (int) blockZ);
        String biomeName = getBiomeName(level, biomeCheckPos);

        // Broadcast
        Component broadcast = Component.literal("[Corruption] ")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal("A dark corruption has appeared near " + biomeName + "!")
                        .withStyle(ChatFormatting.RED));

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(broadcast);
        }

        MegaMod.LOGGER.info("Natural corruption zone #{} spawned at ({}, {}) near biome {}",
                zone.zoneId, blockX, blockZ, biomeName);
    }

    /**
     * Get a readable biome name from coordinates.
     */
    private static String getBiomeName(ServerLevel level, BlockPos pos) {
        try {
            Holder<Biome> biomeHolder = level.getBiome(pos);
            String biomeKey = biomeHolder.unwrapKey()
                    .map(key -> key.identifier().getPath())
                    .orElse("unknown");
            return formatBiomeName(biomeKey);
        } catch (Exception e) {
            return "the wilderness";
        }
    }

    /**
     * Convert snake_case biome ID to "Title Case".
     */
    private static String formatBiomeName(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) return "Unknown";
        String[] parts = snakeCase.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Reset tick trackers -- called on server start.
     */
    public static void resetTicks(long currentTick) {
        lastSpreadCheckTick = currentTick;
        lastPushbackTick = currentTick;
        lastNaturalSpawnTick = currentTick;
    }
}
