/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.storage.LevelResource
 */
package com.ultra.megamod.feature.dimensions;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class PocketManager {
    private static PocketManager INSTANCE;
    private static final String FILE_NAME = "megamod_pockets.dat";
    private static final int POCKET_SPACING = 1000;
    private static final int POCKET_Y = 64;
    private final Map<UUID, BlockPos> museumPockets = new HashMap<UUID, BlockPos>();
    private final Map<String, BlockPos> dungeonPockets = new HashMap<String, BlockPos>();
    private final Map<String, BlockPos> tradingPockets = new HashMap<String, BlockPos>();
    private int nextPocketIndex = 0;
    private boolean dirty = false;

    public static PocketManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new PocketManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public BlockPos getOrCreateMuseumPocket(UUID playerId) {
        BlockPos existing = this.museumPockets.get(playerId);
        if (existing != null) {
            return existing;
        }
        BlockPos origin = this.allocateNextPocket();
        this.museumPockets.put(playerId, origin);
        this.markDirty();
        return origin;
    }

    public BlockPos getMuseumPocket(UUID playerId) {
        return this.museumPockets.get(playerId);
    }

    public BlockPos allocateDungeonPocket(String instanceId) {
        BlockPos existing = this.dungeonPockets.get(instanceId);
        if (existing != null) {
            return existing;
        }
        BlockPos origin = this.allocateNextPocket();
        this.dungeonPockets.put(instanceId, origin);
        this.markDirty();
        return origin;
    }

    public BlockPos getDungeonPocket(String instanceId) {
        return this.dungeonPockets.get(instanceId);
    }

    public void freeDungeonPocket(String instanceId) {
        if (this.dungeonPockets.remove(instanceId) != null) {
            this.markDirty();
        }
    }

    /**
     * Gets or creates a shared trading pocket for two players.
     * The key is the two UUIDs sorted and joined, ensuring both players map to the same room.
     */
    public BlockPos getOrCreateTradingPocket(UUID player1, UUID player2) {
        String key = makeTradingKey(player1, player2);
        BlockPos existing = this.tradingPockets.get(key);
        if (existing != null) {
            return existing;
        }
        BlockPos origin = this.allocateNextPocket();
        this.tradingPockets.put(key, origin);
        this.markDirty();
        return origin;
    }

    public BlockPos getTradingPocket(UUID player1, UUID player2) {
        return this.tradingPockets.get(makeTradingKey(player1, player2));
    }

    public void freeTradingPocket(UUID player1, UUID player2) {
        if (this.tradingPockets.remove(makeTradingKey(player1, player2)) != null) {
            this.markDirty();
        }
    }

    private static String makeTradingKey(UUID a, UUID b) {
        String sa = a.toString();
        String sb = b.toString();
        return sa.compareTo(sb) < 0 ? sa + "_" + sb : sb + "_" + sa;
    }

    private BlockPos allocateNextPocket() {
        BlockPos pos = new BlockPos(this.nextPocketIndex * 1000, 64, 0);
        ++this.nextPocketIndex;
        this.markDirty();
        return pos;
    }

    private void markDirty() {
        this.dirty = true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                BlockPos pos;
                CompoundTag posTag;
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                this.nextPocketIndex = root.getIntOr("nextPocketIndex", 0);
                CompoundTag museumTag = root.getCompoundOrEmpty("museumPockets");
                for (String key : museumTag.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    posTag = museumTag.getCompoundOrEmpty(key);
                    pos = new BlockPos(posTag.getIntOr("x", 0), posTag.getIntOr("y", 64), posTag.getIntOr("z", 0));
                    this.museumPockets.put(uuid, pos);
                }
                CompoundTag dungeonTag = root.getCompoundOrEmpty("dungeonPockets");
                for (String key : dungeonTag.keySet()) {
                    posTag = dungeonTag.getCompoundOrEmpty(key);
                    pos = new BlockPos(posTag.getIntOr("x", 0), posTag.getIntOr("y", 64), posTag.getIntOr("z", 0));
                    this.dungeonPockets.put(key, pos);
                }
                CompoundTag tradingTag = root.getCompoundOrEmpty("tradingPockets");
                for (String key : tradingTag.keySet()) {
                    posTag = tradingTag.getCompoundOrEmpty(key);
                    pos = new BlockPos(posTag.getIntOr("x", 0), posTag.getIntOr("y", 64), posTag.getIntOr("z", 0));
                    this.tradingPockets.put(key, pos);
                }
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load pocket dimension data", e);
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
            root.putInt("nextPocketIndex", this.nextPocketIndex);
            CompoundTag museumTag = new CompoundTag();
            for (Map.Entry<UUID, BlockPos> entry : this.museumPockets.entrySet()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", entry.getValue().getX());
                posTag.putInt("y", entry.getValue().getY());
                posTag.putInt("z", entry.getValue().getZ());
                museumTag.put(entry.getKey().toString(), (Tag)posTag);
            }
            root.put("museumPockets", (Tag)museumTag);
            CompoundTag dungeonTag = new CompoundTag();
            for (Map.Entry<String, BlockPos> entry : this.dungeonPockets.entrySet()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", entry.getValue().getX());
                posTag.putInt("y", entry.getValue().getY());
                posTag.putInt("z", entry.getValue().getZ());
                dungeonTag.put(entry.getKey(), (Tag)posTag);
            }
            root.put("dungeonPockets", (Tag)dungeonTag);
            CompoundTag tradingTag = new CompoundTag();
            for (Map.Entry<String, BlockPos> entry : this.tradingPockets.entrySet()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", entry.getValue().getX());
                posTag.putInt("y", entry.getValue().getY());
                posTag.putInt("z", entry.getValue().getZ());
                tradingTag.put(entry.getKey(), (Tag)posTag);
            }
            root.put("tradingPockets", (Tag)tradingTag);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save pocket dimension data", e);
        }
    }
}

