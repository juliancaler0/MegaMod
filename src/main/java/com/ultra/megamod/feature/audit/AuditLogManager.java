package com.ultra.megamod.feature.audit;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class AuditLogManager {
    private static AuditLogManager INSTANCE;
    private static final String FILE_NAME = "megamod_audit_log.dat";
    private static final int MAX_ENTRIES = 1000;

    private final List<AuditEntry> entries = new ArrayList<>();
    private boolean dirty = false;

    public enum EventType {
        LOGIN_LOGOUT,
        COMMAND_USED,
        PLAYER_DEATH,
        SUSPICIOUS_ITEM,
        RAPID_KILLS,
        DIMENSION_ENTER,
        PERMISSION_DENIED,
        CASINO_ADMIN
    }

    public record AuditEntry(long timestamp, String playerName, String playerUUID, EventType eventType, String description) {}

    public static AuditLogManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new AuditLogManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public void log(String playerName, String playerUUID, EventType type, String description) {
        entries.add(new AuditEntry(System.currentTimeMillis(), playerName, playerUUID, type, description));
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
        dirty = true;
    }

    public List<AuditEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<AuditEntry> getEntriesByType(EventType type) {
        return entries.stream()
            .filter(e -> e.eventType() == type)
            .collect(Collectors.toUnmodifiableList());
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                ListTag list = root.getListOrEmpty("entries");
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i).orElse(new CompoundTag());
                    try {
                        entries.add(new AuditEntry(
                            tag.getLongOr("time", 0L),
                            tag.getStringOr("player", "Unknown"),
                            tag.getStringOr("uuid", ""),
                            EventType.valueOf(tag.getStringOr("type", "LOGIN_LOGOUT")),
                            tag.getStringOr("desc", "")
                        ));
                    } catch (Exception e) {
                        // Skip malformed entries
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load audit log data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (AuditEntry entry : entries) {
                CompoundTag tag = new CompoundTag();
                tag.putLong("time", entry.timestamp());
                tag.putString("player", entry.playerName());
                tag.putString("uuid", entry.playerUUID());
                tag.putString("type", entry.eventType().name());
                tag.putString("desc", entry.description());
                list.add((Tag) tag);
            }
            root.put("entries", (Tag) list);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save audit log data", e);
        }
    }
}
