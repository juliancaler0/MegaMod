package com.ultra.megamod.feature.skills.prestige;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.skills.SkillTreeType;
import java.io.File;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class PrestigeManager {
    private static PrestigeManager INSTANCE;
    private static final String FILE_NAME = "megamod_prestige.dat";
    private static final int MAX_PRESTIGE = 5;
    private static final double BONUS_PER_PRESTIGE = 0.05; // 5% per prestige level

    private final Map<UUID, Map<SkillTreeType, Integer>> prestigeLevels = new HashMap<>();
    private boolean dirty = false;

    public static PrestigeManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new PrestigeManager();
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

    private Map<SkillTreeType, Integer> getOrCreate(UUID playerId) {
        return this.prestigeLevels.computeIfAbsent(playerId, k -> {
            Map<SkillTreeType, Integer> map = new EnumMap<>(SkillTreeType.class);
            for (SkillTreeType t : SkillTreeType.values()) {
                map.put(t, 0);
            }
            return map;
        });
    }

    public int getPrestigeLevel(UUID playerId, SkillTreeType tree) {
        return getOrCreate(playerId).getOrDefault(tree, 0);
    }

    public double getPrestigeBonus(UUID playerId, SkillTreeType tree) {
        return getPrestigeLevel(playerId, tree) * BONUS_PER_PRESTIGE;
    }

    public boolean prestige(UUID playerId, SkillTreeType tree) {
        Map<SkillTreeType, Integer> levels = getOrCreate(playerId);
        int current = levels.getOrDefault(tree, 0);
        if (current >= MAX_PRESTIGE) {
            return false;
        }
        levels.put(tree, current + 1);
        this.markDirty();
        return true;
    }

    public boolean decrementPrestige(UUID playerId, SkillTreeType tree) {
        Map<SkillTreeType, Integer> levels = getOrCreate(playerId);
        int current = levels.getOrDefault(tree, 0);
        if (current <= 0) {
            return false;
        }
        levels.put(tree, current - 1);
        this.markDirty();
        return true;
    }

    /**
     * Returns true if the player has prestige 3+ in the given tree,
     * unlocking a 3rd branch specialization slot.
     */
    public boolean hasThirdBranchUnlock(UUID playerId, SkillTreeType tree) {
        return getPrestigeLevel(playerId, tree) >= 3;
    }

    /**
     * Returns the total prestige level across all trees (for display/cosmetic purposes).
     */
    public int getTotalPrestige(UUID playerId) {
        Map<SkillTreeType, Integer> levels = getOrCreate(playerId);
        int total = 0;
        for (int v : levels.values()) {
            total += v;
        }
        return total;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pTag = players.getCompoundOrEmpty(key);
                    Map<SkillTreeType, Integer> levels = new EnumMap<>(SkillTreeType.class);
                    for (SkillTreeType type : SkillTreeType.values()) {
                        levels.put(type, pTag.getIntOr(type.name().toLowerCase(), 0));
                    }
                    this.prestigeLevels.put(uuid, levels);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load prestige data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, Map<SkillTreeType, Integer>> entry : this.prestigeLevels.entrySet()) {
                CompoundTag pTag = new CompoundTag();
                for (Map.Entry<SkillTreeType, Integer> treeEntry : entry.getValue().entrySet()) {
                    pTag.putInt(treeEntry.getKey().name().toLowerCase(), treeEntry.getValue());
                }
                players.put(entry.getKey().toString(), (Tag) pTag);
            }
            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            this.dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save prestige data", e);
        }
    }
}
