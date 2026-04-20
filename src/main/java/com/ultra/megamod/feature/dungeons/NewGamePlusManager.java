package com.ultra.megamod.feature.dungeons;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Tracks per-player boss defeats across tiers for New Game+ progression.
 * Beating all 8 bosses on a tier (plus prestige requirements) unlocks the next NG+ tier.
 */
public class NewGamePlusManager {
    private static NewGamePlusManager INSTANCE;
    private static final String FILE_NAME = "megamod_newgameplus.dat";

    /**
     * The 8 dungeon boss IDs that must be defeated per tier.
     */
    private static final Set<String> ALL_BOSS_IDS = Set.of(
            "wraith", "ossukage", "dungeon_keeper", "frostmaw",
            "wroughtnaut", "umvuthi", "chaos_spawner", "sculptor"
    );

    /**
     * Per-player boss defeats per tier.
     * Outer key = player UUID, middle key = tier name (NORMAL/HARD/etc), inner set = boss IDs defeated.
     */
    private final Map<UUID, Map<String, Set<String>>> bossDefeats = new HashMap<>();
    private boolean dirty = false;

    public static NewGamePlusManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new NewGamePlusManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void markDirty() {
        this.dirty = true;
    }

    /**
     * Record a boss defeat for a player in a specific tier.
     */
    public void recordBossDefeat(UUID playerId, String bossId, String tierName) {
        Map<String, Set<String>> tierMap = bossDefeats.computeIfAbsent(playerId, k -> new HashMap<>());
        Set<String> bosses = tierMap.computeIfAbsent(tierName.toUpperCase(), k -> new HashSet<>());
        if (bosses.add(bossId.toLowerCase())) {
            markDirty();
            MegaMod.LOGGER.info("NG+ recorded boss defeat: player={}, boss={}, tier={}",
                    playerId.toString().substring(0, 8), bossId, tierName);
        }
    }

    /**
     * Check if a player has defeated all 8 bosses in the given tier.
     */
    public boolean hasCompletedAllBossesInTier(UUID playerId, String tierName) {
        Map<String, Set<String>> tierMap = bossDefeats.get(playerId);
        if (tierMap == null) return false;
        Set<String> bosses = tierMap.get(tierName.toUpperCase());
        if (bosses == null) return false;
        return bosses.containsAll(ALL_BOSS_IDS);
    }

    /**
     * Get the set of boss IDs defeated by a player in a given tier. Returns empty set if none.
     */
    public Set<String> getDefeatedBosses(UUID playerId, String tierName) {
        Map<String, Set<String>> tierMap = bossDefeats.get(playerId);
        if (tierMap == null) return Set.of();
        Set<String> bosses = tierMap.get(tierName.toUpperCase());
        return bosses != null ? Set.copyOf(bosses) : Set.of();
    }

    /// Mythic tier access: all Infernal bosses + 2 total prestige (was 5/25 in legacy 5-tree
    /// system, now 2/10 to stay proportional to the 2-category cap).
    public boolean canAccessMythic(UUID playerId, ServerLevel level) {
        if (!hasCompletedAllBossesInTier(playerId, "INFERNAL")) return false;
        int totalPrestige = PrestigeManager.get(level).getTotalPrestige(playerId);
        return totalPrestige >= 2;
    }

    /// Eternal tier access: all Mythic bosses + 6 total prestige (was 15/25, now 6/10).
    public boolean canAccessEternal(UUID playerId, ServerLevel level) {
        if (!hasCompletedAllBossesInTier(playerId, "MYTHIC")) return false;
        int totalPrestige = PrestigeManager.get(level).getTotalPrestige(playerId);
        return totalPrestige >= 6;
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            CompoundTag playersTag = new CompoundTag();

            for (Map.Entry<UUID, Map<String, Set<String>>> playerEntry : bossDefeats.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                for (Map.Entry<String, Set<String>> tierEntry : playerEntry.getValue().entrySet()) {
                    CompoundTag tierTag = new CompoundTag();
                    int idx = 0;
                    for (String bossId : tierEntry.getValue()) {
                        tierTag.putString("boss_" + idx, bossId);
                        idx++;
                    }
                    tierTag.putInt("count", idx);
                    playerTag.put(tierEntry.getKey(), (Tag) tierTag);
                }
                playersTag.put(playerEntry.getKey().toString(), (Tag) playerTag);
            }

            root.put("players", (Tag) playersTag);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            this.dirty = false;
            MegaMod.LOGGER.info("Saved NG+ data for {} players", bossDefeats.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save NG+ data", (Throwable) e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag playersTag = root.getCompoundOrEmpty("players");

                for (String uuidStr : playersTag.keySet()) {
                    UUID uuid = UUID.fromString(uuidStr);
                    CompoundTag playerTag = playersTag.getCompoundOrEmpty(uuidStr);
                    Map<String, Set<String>> tierMap = new HashMap<>();

                    for (String tierName : playerTag.keySet()) {
                        CompoundTag tierTag = playerTag.getCompoundOrEmpty(tierName);
                        int count = tierTag.getIntOr("count", 0);
                        Set<String> bosses = new HashSet<>();
                        for (int i = 0; i < count; i++) {
                            String bossId = tierTag.getStringOr("boss_" + i, "");
                            if (!bossId.isEmpty()) {
                                bosses.add(bossId);
                            }
                        }
                        tierMap.put(tierName, bosses);
                    }

                    bossDefeats.put(uuid, tierMap);
                }
                MegaMod.LOGGER.info("Loaded NG+ data for {} players", bossDefeats.size());
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load NG+ data", (Throwable) e);
        }
    }
}
