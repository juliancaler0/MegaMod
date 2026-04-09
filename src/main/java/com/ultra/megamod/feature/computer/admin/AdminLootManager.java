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
 * Admin-editable custom loot tables. Defines extra drops per mob type.
 * These drops are ADDED to vanilla drops (not replacing).
 */
public class AdminLootManager {

    private static AdminLootManager INSTANCE;
    private static final String FILE_NAME = "megamod_custom_loot.dat";

    // mobTypeId -> list of drops
    private final Map<String, List<LootDrop>> customDrops = new LinkedHashMap<>();
    private boolean dirty = false;

    public record LootDrop(String itemId, int minCount, int maxCount, double chance) {}

    public static AdminLootManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new AdminLootManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() { INSTANCE = null; }

    public Map<String, List<LootDrop>> getAllDrops() {
        return Collections.unmodifiableMap(customDrops);
    }

    public List<LootDrop> getDropsForMob(String mobId) {
        return customDrops.getOrDefault(mobId, Collections.emptyList());
    }

    public String addDrop(String mobId, String itemId, int minCount, int maxCount, double chance) {
        if (mobId.isEmpty() || itemId.isEmpty()) return "Mob ID and Item ID required.";
        if (minCount < 1) minCount = 1;
        if (maxCount < minCount) maxCount = minCount;
        if (chance <= 0 || chance > 1.0) return "Chance must be between 0.01 and 1.0.";

        customDrops.computeIfAbsent(mobId, k -> new ArrayList<>())
                .add(new LootDrop(itemId, minCount, maxCount, chance));
        dirty = true;
        return null;
    }

    public boolean removeDrop(String mobId, int index) {
        List<LootDrop> drops = customDrops.get(mobId);
        if (drops == null || index < 0 || index >= drops.size()) return false;
        drops.remove(index);
        if (drops.isEmpty()) customDrops.remove(mobId);
        dirty = true;
        return true;
    }

    public boolean clearDropsForMob(String mobId) {
        if (customDrops.remove(mobId) != null) {
            dirty = true;
            return true;
        }
        return false;
    }

    // --- Persistence ---

    public void loadFromDisk(ServerLevel level) {
        customDrops.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
            CompoundTag mobsTag = root.getCompoundOrEmpty("mobs");
            for (String mobId : mobsTag.keySet()) {
                ListTag dropsList = mobsTag.getListOrEmpty(mobId);
                List<LootDrop> drops = new ArrayList<>();
                for (int i = 0; i < dropsList.size(); i++) {
                    CompoundTag dt = dropsList.getCompoundOrEmpty(i);
                    drops.add(new LootDrop(
                            dt.getStringOr("item", ""),
                            dt.getIntOr("min", 1),
                            dt.getIntOr("max", 1),
                            dt.getDoubleOr("chance", 1.0)
                    ));
                }
                if (!drops.isEmpty()) customDrops.put(mobId, drops);
            }
        } catch (Exception e) { MegaMod.LOGGER.error("Failed to load loot table data", e); }
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
            CompoundTag mobsTag = new CompoundTag();
            for (Map.Entry<String, List<LootDrop>> entry : customDrops.entrySet()) {
                ListTag dropsList = new ListTag();
                for (LootDrop d : entry.getValue()) {
                    CompoundTag dt = new CompoundTag();
                    dt.putString("item", d.itemId);
                    dt.putInt("min", d.minCount);
                    dt.putInt("max", d.maxCount);
                    dt.putDouble("chance", d.chance);
                    dropsList.add((Tag) dt);
                }
                mobsTag.put(entry.getKey(), (Tag) dropsList);
            }
            root.put("mobs", (Tag) mobsTag);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) { MegaMod.LOGGER.error("Failed to save loot table data", e); }
    }
}
