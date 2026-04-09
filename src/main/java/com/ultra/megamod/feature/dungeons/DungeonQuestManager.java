/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.storage.LevelResource
 */
package com.ultra.megamod.feature.dungeons;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class DungeonQuestManager {
    private static DungeonQuestManager INSTANCE;
    private static final String FILE_NAME = "megamod_dungeon_quests.dat";
    private final Map<String, PlayerQuestData> playerData = new HashMap<String, PlayerQuestData>();
    private boolean dirty = false;

    public static DungeonQuestManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new DungeonQuestManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public boolean hasUnlockedTier(UUID playerUUID, DungeonTier tier) {
        if (tier == DungeonTier.NORMAL) {
            return true;
        }
        DungeonTier[] tiers = DungeonTier.values();
        int index = tier.ordinal();
        if (index <= 0) {
            return true;
        }
        DungeonTier previousTier = tiers[index - 1];
        return this.getClearCount(playerUUID, previousTier) > 0;
    }

    public void recordClear(UUID playerUUID, DungeonTier tier) {
        PlayerQuestData data = this.getOrCreate(playerUUID);
        data.clearCounts.merge(tier, 1, Integer::sum);
        ++data.totalClears;
        ++data.consecutiveClears;
        this.markDirty();
        MegaMod.LOGGER.info("Player {} cleared {} dungeon (total: {}, streak: {})", new Object[]{playerUUID.toString().substring(0, 8), tier.getDisplayName(), data.totalClears, data.consecutiveClears});
    }

    public void recordFailure(UUID playerUUID) {
        PlayerQuestData data = this.getOrCreate(playerUUID);
        data.consecutiveClears = 0;
        this.markDirty();
    }

    public int getClearCount(UUID playerUUID, DungeonTier tier) {
        PlayerQuestData data = this.playerData.get(playerUUID.toString());
        if (data == null) {
            return 0;
        }
        return data.clearCounts.getOrDefault((Object)tier, 0);
    }

    public int getTotalClears(UUID playerUUID) {
        PlayerQuestData data = this.playerData.get(playerUUID.toString());
        return data == null ? 0 : data.totalClears;
    }

    public int getConsecutiveClears(UUID playerUUID) {
        PlayerQuestData data = this.playerData.get(playerUUID.toString());
        return data == null ? 0 : data.consecutiveClears;
    }

    private PlayerQuestData getOrCreate(UUID playerUUID) {
        return this.playerData.computeIfAbsent(playerUUID.toString(), k -> new PlayerQuestData());
    }

    private void markDirty() {
        this.dirty = true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                CompoundTag playersTag = root.getCompoundOrEmpty("players");
                for (String uuid : playersTag.keySet()) {
                    CompoundTag playerTag = playersTag.getCompoundOrEmpty(uuid);
                    PlayerQuestData data = new PlayerQuestData();
                    CompoundTag clearsTag = playerTag.getCompoundOrEmpty("clearCounts");
                    for (DungeonTier tier : DungeonTier.values()) {
                        data.clearCounts.put(tier, clearsTag.getIntOr(tier.name(), 0));
                    }
                    data.totalClears = playerTag.getIntOr("totalClears", 0);
                    data.consecutiveClears = playerTag.getIntOr("consecutiveClears", 0);
                    this.playerData.put(uuid, data);
                }
                MegaMod.LOGGER.info("Loaded dungeon quest data for {} players", (Object)this.playerData.size());
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load dungeon quest data", (Throwable)e);
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
            CompoundTag playersTag = new CompoundTag();
            for (Map.Entry<String, PlayerQuestData> entry : this.playerData.entrySet()) {
                PlayerQuestData data = entry.getValue();
                CompoundTag playerTag = new CompoundTag();
                CompoundTag clearsTag = new CompoundTag();
                for (Map.Entry<DungeonTier, Integer> clearEntry : data.clearCounts.entrySet()) {
                    clearsTag.putInt(clearEntry.getKey().name(), clearEntry.getValue().intValue());
                }
                playerTag.put("clearCounts", (Tag)clearsTag);
                playerTag.putInt("totalClears", data.totalClears);
                playerTag.putInt("consecutiveClears", data.consecutiveClears);
                playersTag.put(entry.getKey(), (Tag)playerTag);
            }
            root.put("players", (Tag)playersTag);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save dungeon quest data", (Throwable)e);
        }
    }

    public static class PlayerQuestData {
        public final Map<DungeonTier, Integer> clearCounts = new HashMap<DungeonTier, Integer>();
        public int consecutiveClears = 0;
        public int totalClears = 0;

        public PlayerQuestData() {
            for (DungeonTier tier : DungeonTier.values()) {
                this.clearCounts.put(tier, 0);
            }
        }
    }
}

