package com.ultra.megamod.feature.moderation;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class ModerationManager {
    private static ModerationManager INSTANCE;
    private static final String FILE_NAME = "megamod_moderation.dat";
    private static final int MAX_LOG_ENTRIES = 500;

    private final Map<String, TimedBan> timedBans = new LinkedHashMap<>();
    private final Map<String, MuteData> mutes = new LinkedHashMap<>();
    private final Map<String, WarnData> warnings = new LinkedHashMap<>();
    private final List<ActionLog> actionLog = new ArrayList<>();
    private boolean dirty = false;
    private int tickCounter = 0;

    public record TimedBan(String playerName, String uuid, String reason, long banTime, long expireTime, String bannedBy) {}
    public record MuteData(String playerName, String uuid, String reason, long muteTime, long expireTime) {}
    public record WarnData(String playerName, String uuid, List<Warning> warnings) {}
    public record Warning(String reason, long timestamp, String warnedBy) {}
    public record ActionLog(long timestamp, String actionType, String adminName, String targetName, String details) {}

    public static ModerationManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ModerationManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void init(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ModerationManager();
            INSTANCE.loadFromDisk(level);
        }
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ---- Mute/Unmute ----

    public void mutePlayer(String name, String uuid, String reason, long durationMs) {
        long now = System.currentTimeMillis();
        long expire = durationMs <= 0 ? 0 : now + durationMs;
        mutes.put(name.toLowerCase(), new MuteData(name, uuid, reason, now, expire));
        markDirty();
    }

    public void unmutePlayer(String name) {
        mutes.remove(name.toLowerCase());
        markDirty();
    }

    public boolean isMuted(String name) {
        MuteData data = mutes.get(name.toLowerCase());
        if (data == null) return false;
        if (data.expireTime > 0 && System.currentTimeMillis() > data.expireTime) {
            mutes.remove(name.toLowerCase());
            markDirty();
            return false;
        }
        return true;
    }

    public List<MuteData> getActiveMutes() {
        long now = System.currentTimeMillis();
        List<MuteData> active = new ArrayList<>();
        Iterator<Map.Entry<String, MuteData>> it = mutes.entrySet().iterator();
        while (it.hasNext()) {
            MuteData data = it.next().getValue();
            if (data.expireTime > 0 && now > data.expireTime) {
                it.remove();
                dirty = true;
            } else {
                active.add(data);
            }
        }
        return active;
    }

    // ---- Timed Bans ----

    public void addTimedBan(String name, String uuid, String reason, long durationMs, String adminName) {
        long now = System.currentTimeMillis();
        long expire = durationMs <= 0 ? 0 : now + durationMs;
        timedBans.put(name.toLowerCase(), new TimedBan(name, uuid, reason, now, expire, adminName));
        markDirty();
    }

    public void removeTimedBan(String name) {
        timedBans.remove(name.toLowerCase());
        markDirty();
    }

    public List<TimedBan> getActiveBans() {
        long now = System.currentTimeMillis();
        List<TimedBan> active = new ArrayList<>();
        Iterator<Map.Entry<String, TimedBan>> it = timedBans.entrySet().iterator();
        while (it.hasNext()) {
            TimedBan ban = it.next().getValue();
            if (ban.expireTime > 0 && now > ban.expireTime) {
                it.remove();
                dirty = true;
            } else {
                active.add(ban);
            }
        }
        return active;
    }

    public void checkExpiredBans(MinecraftServer server) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, TimedBan>> it = timedBans.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            TimedBan ban = it.next().getValue();
            if (ban.expireTime > 0 && now > ban.expireTime) {
                // Auto-unban via vanilla pardon
                try {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "pardon " + ban.playerName);
                } catch (Exception e) {
                    // Silently ignore if already unbanned
                }
                logAction("AUTO_UNBAN", "System", ban.playerName, "Timed ban expired");
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
    }

    // ---- Warnings ----

    public void warnPlayer(String name, String uuid, String reason, String adminName) {
        String key = name.toLowerCase();
        WarnData existing = warnings.get(key);
        List<Warning> list;
        if (existing != null) {
            list = new ArrayList<>(existing.warnings());
        } else {
            list = new ArrayList<>();
        }
        list.add(new Warning(reason, System.currentTimeMillis(), adminName));
        warnings.put(key, new WarnData(name, uuid, list));
        markDirty();
    }

    public void clearWarnings(String name) {
        warnings.remove(name.toLowerCase());
        markDirty();
    }

    public Map<String, WarnData> getAllWarnings() {
        return Collections.unmodifiableMap(warnings);
    }

    public WarnData getWarnings(String name) {
        return warnings.get(name.toLowerCase());
    }

    // ---- Action Log ----

    public void logAction(String type, String admin, String target, String details) {
        actionLog.add(new ActionLog(System.currentTimeMillis(), type, admin, target, details));
        while (actionLog.size() > MAX_LOG_ENTRIES) {
            actionLog.remove(0);
        }
        markDirty();
    }

    public List<ActionLog> getActionLog() {
        return Collections.unmodifiableList(actionLog);
    }

    // ---- Tick ----

    public void tick(MinecraftServer server) {
        tickCounter++;
        // Check every 20 ticks (once per second)
        if (tickCounter % 20 == 0) {
            checkExpiredBans(server);
            checkExpiredMutes();
        }
        // Auto-save every 6000 ticks (5 minutes)
        if (tickCounter % 6000 == 0 && dirty) {
            ServerLevel level = server.overworld();
            if (level != null) {
                saveToDisk(level);
            }
        }
    }

    private void checkExpiredMutes() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        Iterator<Map.Entry<String, MuteData>> it = mutes.entrySet().iterator();
        while (it.hasNext()) {
            MuteData data = it.next().getValue();
            if (data.expireTime > 0 && now > data.expireTime) {
                logAction("AUTO_UNMUTE", "System", data.playerName, "Timed mute expired");
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
    }

    // ---- Persistence ----

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

            // Load timed bans
            CompoundTag bansTag = root.getCompoundOrEmpty("timedBans");
            for (String key : bansTag.keySet()) {
                CompoundTag b = bansTag.getCompoundOrEmpty(key);
                timedBans.put(key, new TimedBan(
                    b.getStringOr("name", key),
                    b.getStringOr("uuid", ""),
                    b.getStringOr("reason", ""),
                    b.getLongOr("banTime", 0L),
                    b.getLongOr("expireTime", 0L),
                    b.getStringOr("bannedBy", "")
                ));
            }

            // Load mutes
            CompoundTag mutesTag = root.getCompoundOrEmpty("mutes");
            for (String key : mutesTag.keySet()) {
                CompoundTag m = mutesTag.getCompoundOrEmpty(key);
                mutes.put(key, new MuteData(
                    m.getStringOr("name", key),
                    m.getStringOr("uuid", ""),
                    m.getStringOr("reason", ""),
                    m.getLongOr("muteTime", 0L),
                    m.getLongOr("expireTime", 0L)
                ));
            }

            // Load warnings
            CompoundTag warnsTag = root.getCompoundOrEmpty("warnings");
            for (String key : warnsTag.keySet()) {
                CompoundTag w = warnsTag.getCompoundOrEmpty(key);
                String wName = w.getStringOr("name", key);
                String wUuid = w.getStringOr("uuid", "");
                ListTag listTag = w.getListOrEmpty("list");
                List<Warning> warnList = new ArrayList<>();
                for (int i = 0; i < listTag.size(); i++) {
                    if (listTag.get(i) instanceof CompoundTag wt) {
                        warnList.add(new Warning(
                            wt.getStringOr("reason", ""),
                            wt.getLongOr("timestamp", 0L),
                            wt.getStringOr("warnedBy", "")
                        ));
                    }
                }
                warnings.put(key, new WarnData(wName, wUuid, warnList));
            }

            // Load action log
            ListTag logTag = root.getListOrEmpty("actionLog");
            for (int i = 0; i < logTag.size(); i++) {
                if (logTag.get(i) instanceof CompoundTag lt) {
                    actionLog.add(new ActionLog(
                        lt.getLongOr("timestamp", 0L),
                        lt.getStringOr("type", ""),
                        lt.getStringOr("admin", ""),
                        lt.getStringOr("target", ""),
                        lt.getStringOr("details", "")
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load moderation data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();

            // Save timed bans
            CompoundTag bansTag = new CompoundTag();
            for (Map.Entry<String, TimedBan> entry : timedBans.entrySet()) {
                CompoundTag b = new CompoundTag();
                TimedBan ban = entry.getValue();
                b.putString("name", ban.playerName);
                b.putString("uuid", ban.uuid);
                b.putString("reason", ban.reason);
                b.putLong("banTime", ban.banTime);
                b.putLong("expireTime", ban.expireTime);
                b.putString("bannedBy", ban.bannedBy);
                bansTag.put(entry.getKey(), (Tag) b);
            }
            root.put("timedBans", (Tag) bansTag);

            // Save mutes
            CompoundTag mutesTag = new CompoundTag();
            for (Map.Entry<String, MuteData> entry : mutes.entrySet()) {
                CompoundTag m = new CompoundTag();
                MuteData mute = entry.getValue();
                m.putString("name", mute.playerName);
                m.putString("uuid", mute.uuid);
                m.putString("reason", mute.reason);
                m.putLong("muteTime", mute.muteTime);
                m.putLong("expireTime", mute.expireTime);
                mutesTag.put(entry.getKey(), (Tag) m);
            }
            root.put("mutes", (Tag) mutesTag);

            // Save warnings
            CompoundTag warnsTag = new CompoundTag();
            for (Map.Entry<String, WarnData> entry : warnings.entrySet()) {
                CompoundTag w = new CompoundTag();
                WarnData wd = entry.getValue();
                w.putString("name", wd.playerName);
                w.putString("uuid", wd.uuid);
                ListTag listTag = new ListTag();
                for (Warning warn : wd.warnings) {
                    CompoundTag wt = new CompoundTag();
                    wt.putString("reason", warn.reason);
                    wt.putLong("timestamp", warn.timestamp);
                    wt.putString("warnedBy", warn.warnedBy);
                    listTag.add((Tag) wt);
                }
                w.put("list", (Tag) listTag);
                warnsTag.put(entry.getKey(), (Tag) w);
            }
            root.put("warnings", (Tag) warnsTag);

            // Save action log
            ListTag logTag = new ListTag();
            for (ActionLog log : actionLog) {
                CompoundTag lt = new CompoundTag();
                lt.putLong("timestamp", log.timestamp);
                lt.putString("type", log.actionType);
                lt.putString("admin", log.adminName);
                lt.putString("target", log.targetName);
                lt.putString("details", log.details);
                logTag.add((Tag) lt);
            }
            root.put("actionLog", (Tag) logTag);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save moderation data", e);
        }
    }

    private void markDirty() {
        this.dirty = true;
    }

    // ---- Utility ----

    public static String formatDuration(long ms) {
        if (ms <= 0) return "Permanent";
        long seconds = ms / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        return days + "d";
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(timestamp));
    }
}
