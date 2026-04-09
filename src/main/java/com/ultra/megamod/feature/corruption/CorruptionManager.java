package com.ultra.megamod.feature.corruption;

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
import java.util.concurrent.CopyOnWriteArrayList;

public class CorruptionManager {
    private static CorruptionManager INSTANCE;
    private static final String FILE_NAME = "megamod_corruption.dat";
    private final List<CorruptionZone> zones = new CopyOnWriteArrayList<>();
    private int nextId = 1;
    private boolean dirty = false;
    private boolean spreadEnabled = true;
    private int maxActiveZones = 8;

    // Lifetime stats
    private int totalZonesCreated = 0;
    private int totalZonesDestroyed = 0;
    private int totalPurgesCompleted = 0;
    private int totalPurgesFailed = 0;

    public static class CorruptionZone {
        public int zoneId;
        public long centerX;         // world coordinates (block pos X)
        public long centerZ;         // world coordinates (block pos Z)
        public int radius;           // current radius in blocks (starts at 16, max 128)
        public int maxRadius;        // maximum allowed spread (64-128 based on tier)
        public int tier;             // 1-4 (determines mob difficulty and spread speed)
        public long createdTick;
        public long lastSpreadTick;
        public boolean active;       // can be deactivated by purge
        public int corruptionLevel;  // 0-100, increases over time, determines visual intensity
        public String sourceType;    // "natural", "dungeon_failure", "admin", "event"

        public CorruptionZone() {}

        public CorruptionZone(int zoneId, long centerX, long centerZ, int tier, String sourceType, long createdTick) {
            this.zoneId = zoneId;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = 16;
            this.tier = Math.max(1, Math.min(4, tier));
            this.maxRadius = computeMaxRadius(this.tier);
            this.createdTick = createdTick;
            this.lastSpreadTick = createdTick;
            this.active = true;
            this.corruptionLevel = 10;
            this.sourceType = sourceType;
        }

        private static int computeMaxRadius(int tier) {
            return switch (tier) {
                case 1 -> 64;
                case 2 -> 80;
                case 3 -> 96;
                case 4 -> 128;
                default -> 64;
            };
        }

        /**
         * Returns the approximate number of chunks affected by this zone.
         */
        public int getAffectedChunkCount() {
            if (radius <= 0) return 0;
            int chunkRadius = (radius + 15) / 16;
            int count = 0;
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    double distBlocks = Math.sqrt(dx * dx + dz * dz) * 16;
                    if (distBlocks <= radius) {
                        count++;
                    }
                }
            }
            return Math.max(1, count);
        }

        /**
         * Returns whether the given block position is within this zone's radius.
         */
        public boolean containsBlock(long blockX, long blockZ) {
            if (!active || radius <= 0) return false;
            long dx = blockX - centerX;
            long dz = blockZ - centerZ;
            return dx * dx + dz * dz <= (long) radius * radius;
        }

        /**
         * Returns whether the given chunk is at least partially within this zone's radius.
         */
        public boolean containsChunk(int chunkX, int chunkZ) {
            if (!active || radius <= 0) return false;
            // Nearest block in the chunk to the zone center
            long nearX = Math.max((long) chunkX * 16, Math.min(centerX, (long) chunkX * 16 + 15));
            long nearZ = Math.max((long) chunkZ * 16, Math.min(centerZ, (long) chunkZ * 16 + 15));
            long dx = nearX - centerX;
            long dz = nearZ - centerZ;
            return dx * dx + dz * dz <= (long) radius * radius;
        }

        /**
         * Get the spread interval in ticks based on tier.
         * Tier 1 = 12000 (10 min), Tier 2 = 6000 (5 min), Tier 3 = 3600 (3 min), Tier 4 = 3000 (2.5 min)
         */
        public int getSpreadInterval() {
            return switch (tier) {
                case 1 -> 12000;
                case 2 -> 6000;
                case 3 -> 3600;
                case 4 -> 3000;
                default -> 12000;
            };
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("zoneId", zoneId);
            tag.putLong("centerX", centerX);
            tag.putLong("centerZ", centerZ);
            tag.putInt("radius", radius);
            tag.putInt("maxRadius", maxRadius);
            tag.putInt("tier", tier);
            tag.putLong("createdTick", createdTick);
            tag.putLong("lastSpreadTick", lastSpreadTick);
            tag.putBoolean("active", active);
            tag.putInt("corruptionLevel", corruptionLevel);
            tag.putString("sourceType", sourceType);
            return tag;
        }

        public static CorruptionZone load(CompoundTag tag) {
            CorruptionZone zone = new CorruptionZone();
            zone.zoneId = tag.getIntOr("zoneId", 0);
            zone.centerX = tag.getLongOr("centerX", 0L);
            zone.centerZ = tag.getLongOr("centerZ", 0L);
            zone.radius = tag.getIntOr("radius", 16);
            zone.maxRadius = tag.getIntOr("maxRadius", 64);
            zone.tier = tag.getIntOr("tier", 1);
            zone.createdTick = tag.getLongOr("createdTick", 0L);
            zone.lastSpreadTick = tag.getLongOr("lastSpreadTick", 0L);
            zone.active = tag.getBooleanOr("active", true);
            zone.corruptionLevel = tag.getIntOr("corruptionLevel", 10);
            zone.sourceType = tag.getStringOr("sourceType", "natural");
            return zone;
        }
    }

    // ---- Singleton Access ----

    public static CorruptionManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new CorruptionManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ---- Zone Creation ----

    /**
     * Create a new corruption zone at the given block coordinates.
     */
    public CorruptionZone createZone(long blockX, long blockZ, int tier, String sourceType, long currentTick) {
        int id = nextId++;
        CorruptionZone zone = new CorruptionZone(id, blockX, blockZ, tier, sourceType, currentTick);
        zones.add(zone);
        totalZonesCreated++;
        markDirty();
        return zone;
    }

    /**
     * Remove a zone entirely by ID.
     */
    public boolean removeZone(int zoneId) {
        Iterator<CorruptionZone> it = zones.iterator();
        while (it.hasNext()) {
            CorruptionZone zone = it.next();
            if (zone.zoneId == zoneId) {
                it.remove();
                totalZonesDestroyed++;
                markDirty();
                return true;
            }
        }
        return false;
    }

    // ---- Zone Queries ----

    /**
     * Check if a block position is in any active corruption zone.
     */
    public boolean isCorrupted(long blockX, long blockZ) {
        for (CorruptionZone zone : zones) {
            if (zone.active && zone.containsBlock(blockX, blockZ)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a block position is in any active corruption zone (BlockPos variant).
     */
    public boolean isCorrupted(net.minecraft.core.BlockPos pos) {
        return isCorrupted(pos.getX(), pos.getZ());
    }

    /**
     * Get the zone at a given block position (highest tier if overlapping).
     */
    public CorruptionZone getZoneAt(long blockX, long blockZ) {
        CorruptionZone best = null;
        for (CorruptionZone zone : zones) {
            if (zone.active && zone.containsBlock(blockX, blockZ)) {
                if (best == null || zone.tier > best.tier) {
                    best = zone;
                }
            }
        }
        return best;
    }

    /**
     * Get the zone at a given block position (BlockPos variant).
     */
    public CorruptionZone getZoneAt(net.minecraft.core.BlockPos pos) {
        return getZoneAt(pos.getX(), pos.getZ());
    }

    /**
     * Get zones within range of a block position.
     */
    public List<CorruptionZone> getZonesInRange(long blockX, long blockZ, int range) {
        List<CorruptionZone> nearby = new ArrayList<>();
        for (CorruptionZone zone : zones) {
            if (!zone.active) continue;
            long dx = zone.centerX - blockX;
            long dz = zone.centerZ - blockZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist <= range + zone.radius) {
                nearby.add(zone);
            }
        }
        return nearby;
    }

    /**
     * Get zones within range of a block position (BlockPos variant).
     */
    public List<CorruptionZone> getZonesInRange(net.minecraft.core.BlockPos pos, int range) {
        return getZonesInRange(pos.getX(), pos.getZ(), range);
    }

    /**
     * Get a zone by its ID.
     */
    public CorruptionZone getZone(int zoneId) {
        for (CorruptionZone zone : zones) {
            if (zone.zoneId == zoneId) return zone;
        }
        return null;
    }

    /**
     * Get all zones (active and inactive).
     */
    public List<CorruptionZone> getAllZones() {
        return Collections.unmodifiableList(new ArrayList<>(zones));
    }

    /**
     * Get all active zones.
     */
    public List<CorruptionZone> getActiveZones() {
        List<CorruptionZone> active = new ArrayList<>();
        for (CorruptionZone zone : zones) {
            if (zone.active) active.add(zone);
        }
        return Collections.unmodifiableList(active);
    }

    /**
     * Get total number of active zones.
     */
    public int getActiveZoneCount() {
        int count = 0;
        for (CorruptionZone zone : zones) {
            if (zone.active) count++;
        }
        return count;
    }

    /**
     * Total zone count including inactive.
     */
    public int getZoneCount() {
        return zones.size();
    }

    /**
     * Get total corrupted chunks across all active zones (approximate).
     */
    public int getTotalCorruptedChunks() {
        int total = 0;
        for (CorruptionZone zone : zones) {
            if (zone.active) total += zone.getAffectedChunkCount();
        }
        return total;
    }

    /**
     * Get the highest tier affecting the given block position, or 0 if not corrupted.
     */
    public int getTierAt(long blockX, long blockZ) {
        int maxTier = 0;
        for (CorruptionZone zone : zones) {
            if (zone.active && zone.containsBlock(blockX, blockZ)) {
                maxTier = Math.max(maxTier, zone.tier);
            }
        }
        return maxTier;
    }

    /**
     * Backwards-compatible: get corruption strength at chunk (maps tier to strength scale).
     */
    public int getCorruptionStrength(long chunkX, long chunkZ, String dimensionId) {
        long blockX = chunkX * 16 + 8;
        long blockZ = chunkZ * 16 + 8;
        return getTierAt(blockX, blockZ);
    }

    /**
     * Backwards-compatible: check if chunk is corrupted.
     */
    public boolean isChunkCorrupted(long chunkX, long chunkZ, String dimensionId) {
        // Check center of chunk
        long blockX = chunkX * 16 + 8;
        long blockZ = chunkZ * 16 + 8;
        return isCorrupted(blockX, blockZ);
    }

    // ---- Zone Mutations ----

    /**
     * Expand a zone's radius by 1 block (up to maxRadius).
     */
    public void expandZone(int zoneId) {
        CorruptionZone zone = getZone(zoneId);
        if (zone == null || !zone.active) return;
        if (zone.radius < zone.maxRadius) {
            zone.radius++;
            // Increase corruption level over time
            zone.corruptionLevel = Math.min(100, zone.corruptionLevel + 1);
            markDirty();
        }
    }

    /**
     * Shrink a zone's radius. Returns true if zone was destroyed (radius hit 0).
     */
    public boolean shrinkZone(int zoneId, int amount) {
        CorruptionZone zone = getZone(zoneId);
        if (zone == null || !zone.active) return false;
        zone.radius = Math.max(0, zone.radius - amount);
        if (zone.radius <= 0) {
            zone.active = false;
            zones.remove(zone);
            totalZonesDestroyed++;
            markDirty();
            return true;
        }
        markDirty();
        return false;
    }

    /**
     * Set zone tier (1-4).
     */
    public void setZoneTier(int zoneId, int tier) {
        CorruptionZone zone = getZone(zoneId);
        if (zone == null) return;
        zone.tier = Math.max(1, Math.min(4, tier));
        zone.maxRadius = CorruptionZone.computeMaxRadius(zone.tier);
        markDirty();
    }

    /**
     * Set zone radius directly.
     */
    public void setZoneRadius(int zoneId, int radius) {
        CorruptionZone zone = getZone(zoneId);
        if (zone == null) return;
        zone.radius = Math.max(0, Math.min(radius, zone.maxRadius));
        if (zone.radius <= 0) {
            zone.active = false;
            zones.remove(zone);
            totalZonesDestroyed++;
        }
        markDirty();
    }

    /**
     * Remove all corruption zones.
     */
    public void clearAll() {
        totalZonesDestroyed += zones.size();
        zones.clear();
        markDirty();
    }

    // ---- Spread Toggle ----

    public boolean isSpreadEnabled() { return spreadEnabled; }
    public void setSpreadEnabled(boolean enabled) { spreadEnabled = enabled; markDirty(); }

    public int getMaxActiveZones() { return maxActiveZones; }
    public void setMaxActiveZones(int max) { maxActiveZones = Math.max(1, max); markDirty(); }

    // ---- Stats ----

    public int getTotalZonesCreated() { return totalZonesCreated; }
    public int getTotalZonesDestroyed() { return totalZonesDestroyed; }
    public int getTotalPurgesCompleted() { return totalPurgesCompleted; }
    public int getTotalPurgesFailed() { return totalPurgesFailed; }

    public void incrementPurgesCompleted() { totalPurgesCompleted++; markDirty(); }
    public void incrementPurgesFailed() { totalPurgesFailed++; markDirty(); }

    // ---- Persistence ----

    private void markDirty() { dirty = true; }

    public boolean isDirty() { return dirty; }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                nextId = root.getIntOr("nextId", 1);
                totalZonesCreated = root.getIntOr("totalZonesCreated", 0);
                totalZonesDestroyed = root.getIntOr("totalZonesDestroyed", 0);
                totalPurgesCompleted = root.getIntOr("totalPurgesCompleted", 0);
                totalPurgesFailed = root.getIntOr("totalPurgesFailed", 0);
                spreadEnabled = root.getBooleanOr("spreadEnabled", true);
                maxActiveZones = root.getIntOr("maxActiveZones", 8);
                ListTag zoneList = root.getListOrEmpty("zones");
                for (int i = 0; i < zoneList.size(); i++) {
                    CompoundTag zoneTag = zoneList.getCompoundOrEmpty(i);
                    CorruptionZone zone = CorruptionZone.load(zoneTag);
                    if (zone.zoneId >= nextId) nextId = zone.zoneId + 1;
                    zones.add(zone);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load corruption data", e);
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
            root.putInt("nextId", nextId);
            root.putInt("totalZonesCreated", totalZonesCreated);
            root.putInt("totalZonesDestroyed", totalZonesDestroyed);
            root.putInt("totalPurgesCompleted", totalPurgesCompleted);
            root.putInt("totalPurgesFailed", totalPurgesFailed);
            root.putBoolean("spreadEnabled", spreadEnabled);
            root.putInt("maxActiveZones", maxActiveZones);
            ListTag zoneList = new ListTag();
            for (CorruptionZone zone : zones) {
                zoneList.add(zone.save());
            }
            root.put("zones", (Tag) zoneList);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save corruption data", e);
        }
    }
}
