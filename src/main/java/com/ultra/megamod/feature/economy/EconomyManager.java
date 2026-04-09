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
package com.ultra.megamod.feature.economy;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class EconomyManager {
    private static volatile EconomyManager INSTANCE;
    private static final String FILE_NAME = "megamod_economy.dat";
    private final Map<UUID, int[]> playerData = new ConcurrentHashMap<>();
    private final List<AuditEntry> auditLog = new ArrayList<AuditEntry>();
    private static final int MAX_AUDIT_ENTRIES = 100;
    private boolean dirty = false;

    public record AuditEntry(long timestamp, String playerName, String type, int amount, String description) {}

    public void addAuditEntry(String playerName, String type, int amount, String description) {
        this.auditLog.add(new AuditEntry(System.currentTimeMillis(), playerName, type, amount, description));
        while (this.auditLog.size() > MAX_AUDIT_ENTRIES) {
            this.auditLog.remove(0);
        }
    }

    public List<AuditEntry> getAuditLog() {
        return Collections.unmodifiableList(this.auditLog);
    }

    public static EconomyManager get(ServerLevel level) {
        EconomyManager inst = INSTANCE;
        if (inst == null) {
            synchronized (EconomyManager.class) {
                inst = INSTANCE;
                if (inst == null) {
                    inst = new EconomyManager();
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

    private void loadFromDisk(ServerLevel level) {
        File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
        File dataDir = new File(saveDir, "data");
        File dataFile = new File(dataDir, FILE_NAME);
        File backupFile = new File(dataDir, FILE_NAME + ".bak");

        // Try main file first, fall back to backup if corrupted (e.g. crash mid-write)
        if (tryLoadFile(dataFile)) return;
        if (backupFile.exists()) {
            MegaMod.LOGGER.warn("Economy main file missing or corrupt, loading from backup");
            if (tryLoadFile(backupFile)) return;
        }
        if (dataFile.exists() || backupFile.exists()) {
            MegaMod.LOGGER.error("Failed to load economy data from both main and backup files!");
        }
    }

    private boolean tryLoadFile(File file) {
        if (!file.exists() || file.length() == 0) return false;
        try {
            CompoundTag root = NbtIo.readCompressed((Path) file.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
            CompoundTag players = root.getCompoundOrEmpty("players");
            for (String key : players.keySet()) {
                UUID uuid = UUID.fromString(key);
                CompoundTag pData = players.getCompoundOrEmpty(key);
                this.playerData.put(uuid, new int[]{pData.getIntOr("wallet", 0), pData.getIntOr("bank", 0)});
            }
            MegaMod.LOGGER.info("Loaded economy data for {} players from {}", this.playerData.size(), file.getName());
            return true;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load economy file {}: {}", file.getName(), e.getMessage());
            return false;
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
            File tempFile = new File(dataDir, FILE_NAME + ".tmp");
            File backupFile = new File(dataDir, FILE_NAME + ".bak");

            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, int[]> entry : this.playerData.entrySet()) {
                CompoundTag pData = new CompoundTag();
                pData.putInt("wallet", entry.getValue()[0]);
                pData.putInt("bank", entry.getValue()[1]);
                players.put(entry.getKey().toString(), (Tag)pData);
            }
            root.put("players", (Tag)players);

            // Atomic write: write to temp, backup old, rename temp to main.
            // If the server crashes mid-write, only the .tmp is corrupted — the
            // main file (or .bak) remains intact for recovery.
            NbtIo.writeCompressed((CompoundTag)root, (Path)tempFile.toPath());
            if (dataFile.exists()) {
                if (backupFile.exists()) backupFile.delete();
                dataFile.renameTo(backupFile);
            }
            tempFile.renameTo(dataFile);
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save economy data", e);
        }
    }

    private void markDirty() {
        this.dirty = true;
    }

    /**
     * Returns true if this player has ever been tracked by the economy system.
     * Used to distinguish genuinely new players from returning players whose
     * wallet/bank happen to be 0.
     */
    public boolean isKnownPlayer(UUID playerId) {
        return this.playerData.containsKey(playerId);
    }

    private int[] getOrCreate(UUID playerId) {
        return this.playerData.computeIfAbsent(playerId, id -> new int[]{0, 0});
    }

    public int getWallet(UUID playerId) {
        return this.getOrCreate(playerId)[0];
    }

    public int getBank(UUID playerId) {
        return this.getOrCreate(playerId)[1];
    }

    public void setWallet(UUID playerId, int amount) {
        this.getOrCreate(playerId)[0] = Math.max(0, amount);
        this.markDirty();
    }

    public void setBank(UUID playerId, int amount) {
        this.getOrCreate(playerId)[1] = Math.max(0, amount);
        this.markDirty();
    }

    /** Optional callback for systems that want to track coin earnings (e.g., challenges). */
    public static java.util.function.BiConsumer<java.util.UUID, Integer> onCoinsEarned;
    /** Optional callback for systems that want to track coin spending (e.g., challenges). */
    public static java.util.function.BiConsumer<java.util.UUID, Integer> onCoinsSpent;

    public void addWallet(UUID playerId, int amount) {
        int[] data = this.getOrCreate(playerId);
        synchronized (data) {
            data[0] = Math.max(0, data[0] + amount);
        }
        this.markDirty();
        if (amount > 0 && onCoinsEarned != null) {
            onCoinsEarned.accept(playerId, amount);
        }
        if (amount > 0) {
            EconomyAnalytics.recordTransaction(EconomyAnalytics.EARN, amount);
        }
    }

    public boolean spendWallet(UUID playerId, int amount) {
        int[] data = this.getOrCreate(playerId);
        synchronized (data) {
            if (data[0] >= amount) {
                data[0] = data[0] - amount;
                this.markDirty();
                EconomyAnalytics.recordTransaction(EconomyAnalytics.SPEND, amount);
                if (onCoinsSpent != null) {
                    onCoinsSpent.accept(playerId, amount);
                }
                return true;
            }
        }
        return false;
    }

    public boolean transferToBank(UUID playerId, int amount) {
        int[] data = this.getOrCreate(playerId);
        synchronized (data) {
            if (amount > 0 && data[0] >= amount) {
                data[0] = data[0] - amount;
                data[1] = data[1] + amount;
                this.markDirty();
                EconomyAnalytics.recordTransaction(EconomyAnalytics.TRANSFER, amount);
                return true;
            }
        }
        return false;
    }

    public boolean transferToWallet(UUID playerId, int amount) {
        int[] data = this.getOrCreate(playerId);
        synchronized (data) {
            if (amount > 0 && data[1] >= amount) {
                data[1] = data[1] - amount;
                data[0] = data[0] + amount;
                this.markDirty();
                EconomyAnalytics.recordTransaction(EconomyAnalytics.TRANSFER, amount);
                return true;
            }
        }
        return false;
    }

    public Map<UUID, int[]> getAllPlayerData() {
        return Collections.unmodifiableMap(this.playerData);
    }

    public int getTotalWallets() {
        int total = 0;
        for (int[] data : this.playerData.values()) {
            total += data[0];
        }
        return total;
    }

    public int getTotalBanks() {
        int total = 0;
        for (int[] data : this.playerData.values()) {
            total += data[1];
        }
        return total;
    }

    public int getPlayerCount() {
        return this.playerData.size();
    }
}

