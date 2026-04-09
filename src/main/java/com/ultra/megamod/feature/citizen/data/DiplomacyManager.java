package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.util.AsyncSaveHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Manages diplomacy relations between colonies (factions).
 * <p>
 * Relations are stored as a map of sorted faction-pair keys to DiplomacyStatus.
 * Persists to {@code world/data/megamod_diplomacy.dat}.
 */
public final class DiplomacyManager {

    private static final String SAVE_FILE = "megamod_diplomacy.dat";
    private static DiplomacyManager INSTANCE;

    private final Map<String, DiplomacyStatus> relations = new HashMap<>();
    private boolean dirty = false;

    private DiplomacyManager() {}

    public static DiplomacyManager get(@NotNull ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new DiplomacyManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ==================== Relations ====================

    private static String makeKey(String f1, String f2) {
        return f1.compareTo(f2) <= 0 ? f1 + ":" + f2 : f2 + ":" + f1;
    }

    @NotNull
    public DiplomacyStatus getRelation(@NotNull String faction1, @NotNull String faction2) {
        if (faction1.equals(faction2)) return DiplomacyStatus.NEUTRAL;
        return relations.getOrDefault(makeKey(faction1, faction2), DiplomacyStatus.NEUTRAL);
    }

    public void setRelation(@NotNull String faction1, @NotNull String faction2, @NotNull DiplomacyStatus status) {
        if (faction1.equals(faction2)) return;
        relations.put(makeKey(faction1, faction2), status);
        dirty = true;
    }

    public void removeRelation(@NotNull String faction1, @NotNull String faction2) {
        relations.remove(makeKey(faction1, faction2));
        dirty = true;
    }

    /**
     * Removes all relations involving a faction (used when deleting a colony).
     */
    public void removeFaction(@NotNull String factionId) {
        relations.entrySet().removeIf(entry -> entry.getKey().contains(factionId));
        dirty = true;
    }

    // ==================== Persistence ====================

    public void saveToDisk(@NotNull ServerLevel level) {
        try {
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (Map.Entry<String, DiplomacyStatus> entry : relations.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("key", entry.getKey());
                tag.putString("status", entry.getValue().name());
                list.add(tag);
            }
            root.put("relations", list);

            File dataDir = new File(level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File saveFile = new File(dataDir, SAVE_FILE);

            final File fSave = saveFile;
            final CompoundTag fRoot = root;
            try {
                AsyncSaveHelper.saveAsync(() -> {
                    try { NbtIo.writeCompressed(fRoot, fSave.toPath()); }
                    catch (Exception e) { MegaMod.LOGGER.error("Failed to save diplomacy", e); }
                });
            } catch (Exception e) {
                NbtIo.writeCompressed(root, saveFile.toPath());
            }
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save diplomacy data", e);
        }
    }

    public void loadFromDisk(@NotNull ServerLevel level) {
        relations.clear();
        try {
            File dataDir = new File(level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            File saveFile = new File(dataDir, SAVE_FILE);
            if (!saveFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed(saveFile.toPath(),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root == null) return;

            if (root.contains("relations")) {
                ListTag list = root.getListOrEmpty("relations");
                for (int i = 0; i < list.size(); i++) {
                    if (!(list.get(i) instanceof CompoundTag tag)) continue;
                    String key = tag.getStringOr("key", "");
                    String statusStr = tag.getStringOr("status", "NEUTRAL");
                    if (!key.isEmpty()) {
                        relations.put(key, DiplomacyStatus.fromString(statusStr));
                    }
                }
            }
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load diplomacy data", e);
        }
    }
}
