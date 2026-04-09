package com.ultra.megamod.feature.scheduler;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@EventBusSubscriber(modid = MegaMod.MODID)
public class CommandScheduler {
    private static CommandScheduler INSTANCE;
    private static final String FILE_NAME = "megamod_scheduler.dat";
    private static final int MAX_LOG = 100;

    private final List<ScheduledTask> tasks = new ArrayList<>();
    private final List<ExecutionLog> log = new ArrayList<>();
    private boolean dirty = false;
    private int saveCounter = 0;
    private static final int SAVE_INTERVAL_TICKS = 600; // Auto-save every 30 seconds

    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    public static class ScheduledTask {
        public String id;
        public String name;
        public String command;
        public long intervalMs;
        public boolean repeat;
        public boolean active;
        public long nextRunTime; // System.currentTimeMillis() based
        public int runCount;

        public ScheduledTask(String id, String name, String command, long intervalMs, boolean repeat) {
            this.id = id;
            this.name = name;
            this.command = command;
            this.intervalMs = intervalMs;
            this.repeat = repeat;
            this.active = true;
            this.nextRunTime = System.currentTimeMillis() + intervalMs;
            this.runCount = 0;
        }

        public ScheduledTask() {
            // For deserialization
        }

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", id);
            tag.putString("name", name);
            tag.putString("command", command);
            tag.putLong("intervalMs", intervalMs);
            tag.putBoolean("repeat", repeat);
            tag.putBoolean("active", active);
            tag.putLong("nextRunTime", nextRunTime);
            tag.putInt("runCount", runCount);
            return tag;
        }

        public static ScheduledTask fromNbt(CompoundTag tag) {
            ScheduledTask task = new ScheduledTask();
            task.id = tag.getStringOr("id", "sched_0");
            task.name = tag.getStringOr("name", "Unknown");
            task.command = tag.getStringOr("command", "");
            task.intervalMs = tag.getLongOr("intervalMs", 300000L);
            task.repeat = tag.getBooleanOr("repeat", true);
            task.active = tag.getBooleanOr("active", true);
            task.nextRunTime = tag.getLongOr("nextRunTime", System.currentTimeMillis() + task.intervalMs);
            task.runCount = tag.getIntOr("runCount", 0);

            // If the saved nextRunTime is in the past, reschedule from now
            if (task.active && task.nextRunTime < System.currentTimeMillis()) {
                task.nextRunTime = System.currentTimeMillis() + task.intervalMs;
            }

            return task;
        }
    }

    public record ExecutionLog(long timestamp, String name, String command, String result) {}

    public static CommandScheduler get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new CommandScheduler();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void init(ServerLevel level) {
        get(level);
    }

    public static void reset() {
        if (INSTANCE != null) {
            INSTANCE.tasks.clear();
            INSTANCE.log.clear();
        }
        INSTANCE = null;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (INSTANCE == null) return;
        INSTANCE.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (INSTANCE != null) {
            INSTANCE.saveToDisk(event.getServer().overworld());
        }
        reset();
    }

    public void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        for (int i = 0; i < tasks.size(); i++) {
            ScheduledTask task = tasks.get(i);
            if (!task.active) continue;
            if (now < task.nextRunTime) continue;

            // Execute the scheduled command
            String result = executeScheduledCommand(server, task.command);

            task.runCount++;
            task.nextRunTime = now + task.intervalMs;

            // Add to execution log (most recent first)
            log.add(0, new ExecutionLog(now, task.name, task.command, result));
            if (log.size() > MAX_LOG) {
                log.remove(log.size() - 1);
            }

            // If not repeating, deactivate after execution
            if (!task.repeat) {
                task.active = false;
            }

            dirty = true;
        }

        // Periodic auto-save
        saveCounter++;
        if (saveCounter >= SAVE_INTERVAL_TICKS && dirty) {
            saveCounter = 0;
            ServerLevel overworld = server.overworld();
            if (overworld != null) {
                saveToDisk(overworld);
            }
        }
    }

    private String executeScheduledCommand(MinecraftServer server, String command) {
        try {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput(), command);
            return "OK";
        } catch (Exception e) {
            String msg = e.getMessage();
            return "Error: " + (msg != null ? msg : e.getClass().getSimpleName());
        }
    }

    public String createTask(String name, String command, long intervalMs, boolean repeat) {
        String id = "sched_" + ID_COUNTER.incrementAndGet();
        ScheduledTask task = new ScheduledTask(id, name, command, intervalMs, repeat);
        tasks.add(task);
        dirty = true;
        return id;
    }

    public void deleteTask(String id) {
        tasks.removeIf(t -> t.id.equals(id));
        dirty = true;
    }

    public void pauseTask(String id) {
        for (ScheduledTask task : tasks) {
            if (task.id.equals(id)) {
                task.active = false;
                dirty = true;
                return;
            }
        }
    }

    public void resumeTask(String id) {
        for (ScheduledTask task : tasks) {
            if (task.id.equals(id)) {
                task.active = true;
                // Reschedule from now
                task.nextRunTime = System.currentTimeMillis() + task.intervalMs;
                dirty = true;
                return;
            }
        }
    }

    public void runNow(String id, MinecraftServer server) {
        for (ScheduledTask task : tasks) {
            if (task.id.equals(id)) {
                String result = executeScheduledCommand(server, task.command);
                task.runCount++;
                // Reset the timer
                task.nextRunTime = System.currentTimeMillis() + task.intervalMs;

                log.add(0, new ExecutionLog(System.currentTimeMillis(), task.name, task.command, result));
                if (log.size() > MAX_LOG) {
                    log.remove(log.size() - 1);
                }

                dirty = true;
                return;
            }
        }
    }

    public List<ScheduledTask> getTasks() {
        return tasks;
    }

    public List<ExecutionLog> getLog() {
        return log;
    }

    // ---- NbtIo persistence ----

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) {
                return;
            }
            CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

            // Load tasks
            tasks.clear();
            long maxId = 0;
            if (root.contains("tasks")) {
                ListTag taskList = root.getListOrEmpty("tasks");
                for (int i = 0; i < taskList.size(); i++) {
                    CompoundTag taskTag = taskList.getCompoundOrEmpty(i);
                    ScheduledTask task = ScheduledTask.fromNbt(taskTag);
                    tasks.add(task);
                    // Track max ID to avoid collisions
                    try {
                        String numPart = task.id.replace("sched_", "");
                        long num = Long.parseLong(numPart);
                        if (num > maxId) maxId = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            ID_COUNTER.set(maxId);

            // Load execution log
            log.clear();
            if (root.contains("log")) {
                ListTag logList = root.getListOrEmpty("log");
                for (int i = 0; i < logList.size(); i++) {
                    CompoundTag logTag = logList.getCompoundOrEmpty(i);
                    long timestamp = logTag.getLongOr("timestamp", 0);
                    String name = logTag.getStringOr("name", "Unknown");
                    String command = logTag.getStringOr("command", "");
                    String result = logTag.getStringOr("result", "?");
                    log.add(new ExecutionLog(timestamp, name, command, result));
                }
            }

            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load CommandScheduler data", e);
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

            // Save tasks
            ListTag taskList = new ListTag();
            for (ScheduledTask task : tasks) {
                taskList.add((Tag) task.toNbt());
            }
            root.put("tasks", (Tag) taskList);

            // Save execution log (limited to MAX_LOG)
            ListTag logList = new ListTag();
            int logLimit = Math.min(log.size(), MAX_LOG);
            for (int i = 0; i < logLimit; i++) {
                ExecutionLog entry = log.get(i);
                CompoundTag logTag = new CompoundTag();
                logTag.putLong("timestamp", entry.timestamp());
                logTag.putString("name", entry.name());
                logTag.putString("command", entry.command());
                logTag.putString("result", entry.result());
                logList.add((Tag) logTag);
            }
            root.put("log", (Tag) logList);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save CommandScheduler data", e);
        }
    }
}
