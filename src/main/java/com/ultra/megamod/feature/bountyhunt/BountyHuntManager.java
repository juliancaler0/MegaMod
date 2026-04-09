package com.ultra.megamod.feature.bountyhunt;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the Bounty Hunting system: daily rotating mob bounties that players can accept and complete.
 * Static singleton with NbtIo persistence.
 */
public class BountyHuntManager {

    private static final String FILE_NAME = "megamod_bounty_hunts.dat";
    private static boolean loaded = false;
    private static boolean dirty = false;

    private static final long EXPIRY_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final int MAX_ACTIVE_BOUNTIES = 3;

    // Per-player active bounties
    private static final Map<UUID, List<ActiveBounty>> playerBounties = new ConcurrentHashMap<>();

    // Current day's available bounties (regenerated daily)
    private static List<BountyDefinition> dailyBounties = Collections.synchronizedList(new ArrayList<>());
    private static volatile int lastGeneratedDay = -1;

    // ---- Data Records ----

    public record BountyDefinition(int id, String mobType, String mobDisplayName, String biomeHint, int reward) {}

    public static class ActiveBounty {
        public int bountyId;
        public String targetName;
        public boolean completed;
        public long acceptedTime;

        public ActiveBounty(int bountyId, String targetName, boolean completed, long acceptedTime) {
            this.bountyId = bountyId;
            this.targetName = targetName;
            this.completed = completed;
            this.acceptedTime = acceptedTime;
        }
    }

    // ---- Mob Pool ----

    private static final List<BountyDefinition> MOB_POOL = new ArrayList<>();
    static {
        int id = 1;
        MOB_POOL.add(new BountyDefinition(id++, "zombie",            "Zombie",            "Overworld",   50));
        MOB_POOL.add(new BountyDefinition(id++, "skeleton",          "Skeleton",          "Overworld",   55));
        MOB_POOL.add(new BountyDefinition(id++, "creeper",           "Creeper",           "Overworld",   60));
        MOB_POOL.add(new BountyDefinition(id++, "spider",            "Spider",            "Overworld",   50));
        MOB_POOL.add(new BountyDefinition(id++, "enderman",          "Enderman",          "Overworld",   120));
        MOB_POOL.add(new BountyDefinition(id++, "witch",             "Witch",             "Dark Forest", 150));
        MOB_POOL.add(new BountyDefinition(id++, "blaze",             "Blaze",             "Nether",      175));
        MOB_POOL.add(new BountyDefinition(id++, "wither_skeleton",   "Wither Skeleton",   "Nether",      200));
        MOB_POOL.add(new BountyDefinition(id++, "pillager",          "Pillager",          "Overworld",   80));
        MOB_POOL.add(new BountyDefinition(id++, "vindicator",        "Vindicator",        "Overworld",   120));
        MOB_POOL.add(new BountyDefinition(id++, "phantom",           "Phantom",           "Overworld",   100));
        MOB_POOL.add(new BountyDefinition(id++, "guardian",          "Guardian",          "Ocean",       160));
    }

    // ---- Daily Rotation ----

    /**
     * Generates daily bounties deterministically based on the day number.
     * Selects 5-8 bounties from the mob pool with varied rewards.
     */
    public static void generateDailyBounties() {
        int today = getCurrentDay();
        if (today == lastGeneratedDay && !dailyBounties.isEmpty()) {
            return; // Already generated for today
        }

        Random rng = new Random(today * 31L + 7919L); // Deterministic seed from day
        int count = 5 + rng.nextInt(4); // 5-8 bounties

        List<BountyDefinition> pool = new ArrayList<>(MOB_POOL);
        Collections.shuffle(pool, rng);

        dailyBounties.clear();
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            BountyDefinition base = pool.get(i);
            // Apply daily reward variance: 80%-130% of base reward
            double multiplier = 0.8 + rng.nextDouble() * 0.5;
            int adjustedReward = Math.max(10, (int) (base.reward * multiplier));
            dailyBounties.add(new BountyDefinition(
                    base.id, base.mobType, base.mobDisplayName, base.biomeHint, adjustedReward
            ));
        }

        lastGeneratedDay = today;
    }

    /**
     * Returns the current day number (days since epoch).
     */
    private static int getCurrentDay() {
        return (int) (System.currentTimeMillis() / (24 * 60 * 60 * 1000L));
    }

    // ---- Public API ----

    /**
     * Gets today's available bounties.
     */
    public static List<BountyDefinition> getAvailableBounties() {
        generateDailyBounties();
        return Collections.unmodifiableList(dailyBounties);
    }

    /**
     * Gets a player's active bounties.
     */
    public static List<ActiveBounty> getActiveBounties(UUID playerId) {
        return playerBounties.getOrDefault(playerId, Collections.emptyList());
    }

    /**
     * Accept a bounty by bounty definition ID.
     * @return null on success, or an error message string.
     */
    public static String acceptBounty(UUID playerId, int bountyId) {
        generateDailyBounties();

        // Find the bounty definition in today's pool
        BountyDefinition def = null;
        for (BountyDefinition bd : dailyBounties) {
            if (bd.id == bountyId) {
                def = bd;
                break;
            }
        }
        if (def == null) {
            return "Bounty not found in today's list.";
        }

        List<ActiveBounty> actives = playerBounties.computeIfAbsent(playerId, k -> new ArrayList<>());

        // Clean expired
        cleanExpiredBounties(playerId);

        // Check max active
        long activeCount = actives.stream().filter(b -> !b.completed).count();
        if (activeCount >= MAX_ACTIVE_BOUNTIES) {
            return "You already have " + MAX_ACTIVE_BOUNTIES + " active bounties.";
        }

        // Check if already has this bounty active
        for (ActiveBounty ab : actives) {
            if (ab.bountyId == bountyId && !ab.completed) {
                return "You already have this bounty active.";
            }
        }

        // Create active bounty with a themed target name
        String targetName = generateTargetName(def, playerId);
        ActiveBounty active = new ActiveBounty(bountyId, targetName, false, System.currentTimeMillis());
        actives.add(active);

        dirty = true;
        return null; // Success
    }

    /**
     * Complete a bounty for a player. Returns the coin reward, or 0 if not found.
     */
    public static int completeBounty(UUID playerId, int bountyId) {
        List<ActiveBounty> actives = playerBounties.get(playerId);
        if (actives == null) return 0;

        for (ActiveBounty ab : actives) {
            if (ab.bountyId == bountyId && !ab.completed) {
                ab.completed = true;
                dirty = true;

                // Find reward from definition
                for (BountyDefinition bd : MOB_POOL) {
                    if (bd.id == bountyId) {
                        // Use base reward (the daily variance was only for display)
                        // Look in today's daily list for the adjusted reward
                        for (BountyDefinition daily : dailyBounties) {
                            if (daily.id == bountyId) {
                                return daily.reward;
                            }
                        }
                        return bd.reward; // Fallback to base reward
                    }
                }
                return 0;
            }
        }
        return 0;
    }

    /**
     * Abandon an active bounty.
     * @return null on success, or an error message string.
     */
    public static String abandonBounty(UUID playerId, int bountyId) {
        List<ActiveBounty> actives = playerBounties.get(playerId);
        if (actives == null) return "No active bounties found.";

        Iterator<ActiveBounty> it = actives.iterator();
        while (it.hasNext()) {
            ActiveBounty ab = it.next();
            if (ab.bountyId == bountyId && !ab.completed) {
                it.remove();
                dirty = true;
                return null; // Success
            }
        }
        return "Bounty not found or already completed.";
    }

    /**
     * Finds the BountyDefinition for a given bountyId from the mob pool.
     */
    public static BountyDefinition getDefinition(int bountyId) {
        for (BountyDefinition bd : MOB_POOL) {
            if (bd.id == bountyId) return bd;
        }
        return null;
    }

    /**
     * Checks if ANY online player has an active bounty targeting the given mob type.
     * Returns a list of (playerId, ActiveBounty) pairs.
     */
    public static List<Map.Entry<UUID, ActiveBounty>> findPlayersWithBountyForMob(String mobType) {
        List<Map.Entry<UUID, ActiveBounty>> results = new ArrayList<>();
        for (Map.Entry<UUID, List<ActiveBounty>> entry : playerBounties.entrySet()) {
            for (ActiveBounty ab : entry.getValue()) {
                if (ab.completed) continue;
                BountyDefinition def = getDefinition(ab.bountyId);
                if (def != null && def.mobType.equals(mobType)) {
                    results.add(Map.entry(entry.getKey(), ab));
                }
            }
        }
        return results;
    }

    // ---- Helpers ----

    private static String generateTargetName(BountyDefinition def, UUID playerId) {
        String[] prefixes = {
                "Shadow", "Rogue", "Vile", "Cursed", "Dark", "Twisted",
                "Savage", "Wicked", "Fell", "Dread", "Dire", "Grim"
        };
        Random rng = new Random(playerId.hashCode() + def.id * 17L + System.currentTimeMillis() / 60000);
        String prefix = prefixes[rng.nextInt(prefixes.length)];
        return "Bounty: " + prefix + " " + def.mobDisplayName;
    }

    private static void cleanExpiredBounties(UUID playerId) {
        List<ActiveBounty> actives = playerBounties.get(playerId);
        if (actives == null) return;

        long now = System.currentTimeMillis();
        boolean changed = false;
        Iterator<ActiveBounty> it = actives.iterator();
        while (it.hasNext()) {
            ActiveBounty ab = it.next();
            if (!ab.completed && (now - ab.acceptedTime) > EXPIRY_MS) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            dirty = true;
        }
    }

    /**
     * Called periodically to clean expired bounties across all players.
     */
    public static void cleanAllExpired() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (List<ActiveBounty> actives : playerBounties.values()) {
            Iterator<ActiveBounty> it = actives.iterator();
            while (it.hasNext()) {
                ActiveBounty ab = it.next();
                // Remove completed bounties older than 1 hour and expired uncompleted
                if (ab.completed && (now - ab.acceptedTime) > 60 * 60 * 1000L) {
                    it.remove();
                    changed = true;
                } else if (!ab.completed && (now - ab.acceptedTime) > EXPIRY_MS) {
                    it.remove();
                    changed = true;
                }
            }
        }
        if (changed) {
            dirty = true;
        }
    }

    /**
     * Daily rotation check: regenerate bounties if day has changed.
     */
    public static void checkDailyRotation() {
        int today = getCurrentDay();
        if (today != lastGeneratedDay) {
            generateDailyBounties();
        }
    }

    // ---- Persistence ----

    public static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
            loaded = true;
        }
    }

    public static void loadFromDisk(ServerLevel level) {
        playerBounties.clear();
        dailyBounties.clear();
        lastGeneratedDay = -1;

        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                lastGeneratedDay = root.getIntOr("lastGeneratedDay", -1);

                // Load daily bounties
                ListTag dailyList = root.getListOrEmpty("dailyBounties");
                for (int i = 0; i < dailyList.size(); i++) {
                    CompoundTag tag = dailyList.getCompoundOrEmpty(i);
                    dailyBounties.add(new BountyDefinition(
                            tag.getIntOr("id", 0),
                            tag.getStringOr("mobType", ""),
                            tag.getStringOr("mobDisplayName", ""),
                            tag.getStringOr("biomeHint", ""),
                            tag.getIntOr("reward", 0)
                    ));
                }

                // Load player bounties
                ListTag playersTag = root.getListOrEmpty("playerBounties");
                for (int i = 0; i < playersTag.size(); i++) {
                    CompoundTag playerTag = playersTag.getCompoundOrEmpty(i);
                    String uuidStr = playerTag.getStringOr("uuid", "");
                    if (uuidStr.isEmpty()) continue;

                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    List<ActiveBounty> actives = new ArrayList<>();
                    ListTag bountiesList = playerTag.getListOrEmpty("bounties");
                    for (int j = 0; j < bountiesList.size(); j++) {
                        CompoundTag bTag = bountiesList.getCompoundOrEmpty(j);
                        actives.add(new ActiveBounty(
                                bTag.getIntOr("bountyId", 0),
                                bTag.getStringOr("targetName", ""),
                                bTag.getBooleanOr("completed", false),
                                bTag.getLongOr("acceptedTime", 0L)
                        ));
                    }
                    if (!actives.isEmpty()) {
                        playerBounties.put(uuid, actives);
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load bounty hunt data", e);
        }
        dirty = false;
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putInt("lastGeneratedDay", lastGeneratedDay);

            // Save daily bounties
            ListTag dailyList = new ListTag();
            for (BountyDefinition bd : dailyBounties) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("id", bd.id);
                tag.putString("mobType", bd.mobType);
                tag.putString("mobDisplayName", bd.mobDisplayName);
                tag.putString("biomeHint", bd.biomeHint);
                tag.putInt("reward", bd.reward);
                dailyList.add((Tag) tag);
            }
            root.put("dailyBounties", (Tag) dailyList);

            // Save player bounties
            ListTag playersTag = new ListTag();
            for (Map.Entry<UUID, List<ActiveBounty>> entry : playerBounties.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putString("uuid", entry.getKey().toString());

                ListTag bountiesList = new ListTag();
                for (ActiveBounty ab : entry.getValue()) {
                    CompoundTag bTag = new CompoundTag();
                    bTag.putInt("bountyId", ab.bountyId);
                    bTag.putString("targetName", ab.targetName);
                    bTag.putBoolean("completed", ab.completed);
                    bTag.putLong("acceptedTime", ab.acceptedTime);
                    bountiesList.add((Tag) bTag);
                }
                playerTag.put("bounties", (Tag) bountiesList);
                playersTag.add((Tag) playerTag);
            }
            root.put("playerBounties", (Tag) playersTag);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save bounty hunt data", e);
        }
    }

    public static boolean isDirty() {
        return dirty;
    }

    public static void reset() {
        playerBounties.clear();
        dailyBounties.clear();
        lastGeneratedDay = -1;
        dirty = false;
        loaded = false;
    }
}
