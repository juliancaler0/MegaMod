package com.ultra.megamod.feature.computer.admin;

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
import java.util.*;

/**
 * Tracks player deaths with location, inventory snapshot, and cause.
 */
public class DeathLogManager {

    private static DeathLogManager INSTANCE;
    private static final String FILE_NAME = "megamod_death_log.dat";
    private static final int MAX_ENTRIES = 200;

    private final List<DeathEntry> entries = new ArrayList<>();
    private boolean dirty = false;

    public record DeathEntry(
            UUID playerUuid, String playerName,
            double x, double y, double z, String dimension,
            String cause, long timestamp,
            List<DeathItem> items
    ) {}

    public record DeathItem(String itemId, String itemName, int count, int slot) {}

    public static DeathLogManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new DeathLogManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() { INSTANCE = null; }

    public void addDeath(UUID uuid, String name, double x, double y, double z,
                         String dimension, String cause, List<DeathItem> items) {
        entries.add(new DeathEntry(uuid, name, x, y, z, dimension, cause,
                System.currentTimeMillis(), items));
        while (entries.size() > MAX_ENTRIES) entries.remove(0);
        dirty = true;
    }

    public List<DeathEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<DeathEntry> getRecentEntries(int count) {
        int start = Math.max(0, entries.size() - count);
        return entries.subList(start, entries.size());
    }

    public DeathEntry getEntry(int index) {
        if (index < 0 || index >= entries.size()) return null;
        return entries.get(index);
    }

    // --- Persistence ---

    public void loadFromDisk(ServerLevel level) {
        entries.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
            ListTag list = root.getListOrEmpty("deaths");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompoundOrEmpty(i);
                String uuidStr = tag.getStringOr("uuid", "");
                if (uuidStr.isEmpty()) continue;

                UUID uuid;
                try { uuid = UUID.fromString(uuidStr); } catch (Exception e) { continue; }

                List<DeathItem> items = new ArrayList<>();
                ListTag itemsTag = tag.getListOrEmpty("items");
                for (int j = 0; j < itemsTag.size(); j++) {
                    CompoundTag it = itemsTag.getCompoundOrEmpty(j);
                    items.add(new DeathItem(
                            it.getStringOr("id", ""),
                            it.getStringOr("name", ""),
                            it.getIntOr("count", 1),
                            it.getIntOr("slot", 0)
                    ));
                }

                entries.add(new DeathEntry(uuid, tag.getStringOr("name", ""),
                        tag.getDoubleOr("x", 0), tag.getDoubleOr("y", 0), tag.getDoubleOr("z", 0),
                        tag.getStringOr("dim", ""), tag.getStringOr("cause", ""),
                        tag.getLongOr("time", 0L), items));
            }
        } catch (Exception e) { MegaMod.LOGGER.error("Failed to load death log data", e); }
        dirty = false;
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (DeathEntry e : entries) {
                CompoundTag tag = new CompoundTag();
                tag.putString("uuid", e.playerUuid.toString());
                tag.putString("name", e.playerName);
                tag.putDouble("x", e.x);
                tag.putDouble("y", e.y);
                tag.putDouble("z", e.z);
                tag.putString("dim", e.dimension);
                tag.putString("cause", e.cause);
                tag.putLong("time", e.timestamp);

                ListTag itemsTag = new ListTag();
                for (DeathItem item : e.items) {
                    CompoundTag it = new CompoundTag();
                    it.putString("id", item.itemId);
                    it.putString("name", item.itemName);
                    it.putInt("count", item.count);
                    it.putInt("slot", item.slot);
                    itemsTag.add((Tag) it);
                }
                tag.put("items", (Tag) itemsTag);
                list.add((Tag) tag);
            }
            root.put("deaths", (Tag) list);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) { MegaMod.LOGGER.error("Failed to save death log data", e); }
    }
}
