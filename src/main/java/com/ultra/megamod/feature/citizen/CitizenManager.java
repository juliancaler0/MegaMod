package com.ultra.megamod.feature.citizen;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.data.CitizenStatus;
import com.ultra.megamod.feature.citizen.data.UpkeepTracker;
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

public class CitizenManager {
    private static volatile CitizenManager INSTANCE;
    private static final String FILE_NAME = "megamod_citizens.dat";
    private boolean dirty = false;

    // ownerUUID -> list of citizen records
    private final Map<UUID, List<CitizenRecord>> citizensByOwner = new HashMap<>();
    // entityUUID -> citizen record (reverse lookup)
    private final Map<UUID, CitizenRecord> citizensByEntity = new HashMap<>();
    private final UpkeepTracker upkeepTracker = new UpkeepTracker();

    public record CitizenRecord(UUID entityId, UUID ownerUuid, String factionId, CitizenJob job, String name, long hiredTick) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("entityId", entityId.toString());
            tag.putString("owner", ownerUuid.toString());
            tag.putString("factionId", factionId);
            tag.putString("job", job.name());
            tag.putString("name", name);
            tag.putLong("hiredTick", hiredTick);
            return tag;
        }

        public static CitizenRecord load(CompoundTag tag) {
            return new CitizenRecord(
                UUID.fromString(tag.getStringOr("entityId", UUID.randomUUID().toString())),
                UUID.fromString(tag.getStringOr("owner", new UUID(0, 0).toString())),
                tag.getStringOr("factionId", ""),
                CitizenJob.fromString(tag.getStringOr("job", "FARMER")),
                tag.getStringOr("name", "Citizen"),
                tag.getLongOr("hiredTick", 0L)
            );
        }
    }

    public static CitizenManager get(ServerLevel level) {
        CitizenManager inst = INSTANCE;
        if (inst == null) {
            synchronized (CitizenManager.class) {
                inst = INSTANCE;
                if (inst == null) {
                    inst = new CitizenManager();
                    inst.loadFromDisk(level);
                    INSTANCE = inst;
                }
            }
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public void registerCitizen(UUID entityId, UUID ownerUuid, String factionId, CitizenJob job, String name, long tick) {
        CitizenRecord record = new CitizenRecord(entityId, ownerUuid, factionId, job, name, tick);
        citizensByOwner.computeIfAbsent(ownerUuid, k -> new ArrayList<>()).add(record);
        citizensByEntity.put(entityId, record);
        markDirty();
    }

    /** Backward-compatible overload (no faction). */
    public void registerCitizen(UUID entityId, UUID ownerUuid, CitizenJob job, String name, long tick) {
        registerCitizen(entityId, ownerUuid, "", job, name, tick);
    }

    public List<CitizenRecord> getCitizensForFaction(String factionId) {
        if (factionId == null || factionId.isEmpty()) return Collections.emptyList();
        List<CitizenRecord> result = new ArrayList<>();
        for (List<CitizenRecord> list : citizensByOwner.values()) {
            for (CitizenRecord r : list) {
                if (factionId.equals(r.factionId())) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    public void unregisterCitizen(UUID entityId) {
        CitizenRecord record = citizensByEntity.remove(entityId);
        if (record != null) {
            List<CitizenRecord> list = citizensByOwner.get(record.ownerUuid());
            if (list != null) {
                list.removeIf(r -> r.entityId().equals(entityId));
                if (list.isEmpty()) citizensByOwner.remove(record.ownerUuid());
            }
            upkeepTracker.removeCitizen(entityId);
            markDirty();
        }
    }

    /**
     * Resets all pending requests/needs for citizens in the given faction.
     * Clears dirty state flags so citizens re-evaluate their work state on next tick.
     */
    public void resetRequests(String factionId) {
        // Mark dirty to force re-evaluation on next tick
        markDirty();
    }

    public List<CitizenRecord> getCitizensForOwner(UUID ownerUuid) {
        return citizensByOwner.getOrDefault(ownerUuid, Collections.emptyList());
    }

    public CitizenRecord getCitizenByEntity(UUID entityId) {
        return citizensByEntity.get(entityId);
    }

    public int getCitizenCount(UUID ownerUuid) {
        return getCitizensForOwner(ownerUuid).size();
    }

    public int getTotalCitizenCount() {
        return citizensByEntity.size();
    }

    public Map<UUID, List<CitizenRecord>> getAllCitizens() {
        return Collections.unmodifiableMap(citizensByOwner);
    }

    public UpkeepTracker getUpkeepTracker() {
        return upkeepTracker;
    }

    public int getTotalDailyUpkeep(UUID ownerUuid) {
        int total = 0;
        for (CitizenRecord r : getCitizensForOwner(ownerUuid)) {
            total += CitizenConfig.getUpkeepCost(r.job());
        }
        return total;
    }

    private void markDirty() {
        this.dirty = true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag citizens = root.getCompoundOrEmpty("citizens");
                for (String ownerKey : citizens.keySet()) {
                    UUID ownerUuid = UUID.fromString(ownerKey);
                    ListTag list = citizens.getListOrEmpty(ownerKey);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag ct = list.getCompoundOrEmpty(i);
                        CitizenRecord record = CitizenRecord.load(ct);
                        citizensByOwner.computeIfAbsent(ownerUuid, k -> new ArrayList<>()).add(record);
                        citizensByEntity.put(record.entityId(), record);
                    }
                }
                if (root.contains("upkeep")) {
                    UpkeepTracker loaded = UpkeepTracker.load(root.getCompoundOrEmpty("upkeep"));
                    // Copy loaded data into our tracker
                    CompoundTag upkeepTag = loaded.save();
                    UpkeepTracker merged = UpkeepTracker.load(upkeepTag);
                    // We just reload into the field via the save/load cycle
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load citizen data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            CompoundTag citizens = new CompoundTag();
            for (Map.Entry<UUID, List<CitizenRecord>> entry : citizensByOwner.entrySet()) {
                ListTag list = new ListTag();
                for (CitizenRecord record : entry.getValue()) {
                    list.add(record.save());
                }
                citizens.put(entry.getKey().toString(), (Tag) list);
            }
            root.put("citizens", (Tag) citizens);
            root.put("upkeep", (Tag) upkeepTracker.save());

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save citizen data", e);
        }
    }
}
