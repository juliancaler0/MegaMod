package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.*;

/**
 * Tracks 30+ colony statistics per day.
 * Uses {@code Map<String, Map<Integer, Integer>>} — stat name -> (day -> count).
 * <p>
 * Per-faction instance; use {@link #get(ServerLevel, String)} with the faction ID.
 */
public class ColonyStatisticsManager {

    // factionId -> manager instance
    private static final Map<String, ColonyStatisticsManager> INSTANCES = new HashMap<>();

    private static final String FILE_PREFIX = "megamod_colony_stats_";

    // Stat name constants
    public static final String TREE_CUT = "tree_cut";
    public static final String DEATH = "death";
    public static final String BIRTH = "birth";
    public static final String ORES_MINED = "ores_mined";
    public static final String BLOCKS_MINED = "blocks_mined";
    public static final String BLOCKS_PLACED = "blocks_placed";
    public static final String MOBS_KILLED = "mobs_killed";
    public static final String ITEMS_DELIVERED = "items_delivered";
    public static final String ITEMS_CRAFTED = "items_crafted";
    public static final String FOOD_SERVED = "food_served";
    public static final String CITIZENS_HEALED = "citizens_healed";
    public static final String CROPS_HARVESTED = "crops_harvested";
    public static final String FISH_CAUGHT = "fish_caught";
    public static final String BUILD_BUILT = "build_built";
    public static final String BUILD_UPGRADED = "build_upgraded";
    public static final String ARROWS_FIRED = "arrows_fired";
    public static final String VISITORS_RECRUITED = "visitors_recruited";
    public static final String COINS_EARNED = "coins_earned";
    public static final String COINS_SPENT = "coins_spent";
    public static final String RAIDS_SURVIVED = "raids_survived";
    public static final String RAIDS_FAILED = "raids_failed";
    public static final String RESEARCH_COMPLETED = "research_completed";
    public static final String ANIMALS_BRED = "animals_bred";
    public static final String ANIMALS_SLAUGHTERED = "animals_slaughtered";
    public static final String TOOLS_CRAFTED = "tools_crafted";
    public static final String WEAPONS_CRAFTED = "weapons_crafted";
    public static final String ARMOR_CRAFTED = "armor_crafted";
    public static final String POTIONS_BREWED = "potions_brewed";
    public static final String MEALS_COOKED = "meals_cooked";
    public static final String CITIZEN_HAPPINESS_TOTAL = "citizen_happiness_total";
    public static final String SIEGE_WON = "siege_won";
    public static final String SIEGE_LOST = "siege_lost";

    private final String factionId;
    // stat name -> (day number -> count)
    private final Map<String, Map<Integer, Integer>> stats = new LinkedHashMap<>();
    private boolean dirty = false;
    private int currentDay = 0;

    private ColonyStatisticsManager(String factionId) {
        this.factionId = factionId;
    }

    /**
     * Get the statistics manager for a given faction. Creates a new one if needed.
     */
    public static ColonyStatisticsManager get(ServerLevel level, String factionId) {
        ColonyStatisticsManager manager = INSTANCES.get(factionId);
        if (manager == null) {
            manager = new ColonyStatisticsManager(factionId);
            manager.loadFromDisk(level);
            INSTANCES.put(factionId, manager);
        }
        return manager;
    }

    /**
     * Reset all instances (call on server stop).
     */
    public static void resetAll() {
        INSTANCES.clear();
    }

    /**
     * Save all faction instances to disk.
     */
    public static void saveAll(ServerLevel level) {
        for (ColonyStatisticsManager manager : INSTANCES.values()) {
            manager.saveToDisk(level);
        }
    }

    /**
     * Set the current day (typically from server's day count).
     */
    public void setCurrentDay(int day) {
        this.currentDay = day;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    /**
     * Increment a statistic by 1 for the current day.
     */
    public void increment(String stat) {
        increment(stat, 1);
    }

    /**
     * Increment a statistic by a given amount for the current day.
     */
    public void increment(String stat, int amount) {
        Map<Integer, Integer> dayMap = stats.computeIfAbsent(stat, k -> new LinkedHashMap<>());
        dayMap.merge(currentDay, amount, Integer::sum);
        dirty = true;
    }

    /**
     * Get the count for a statistic on the current day.
     */
    public int getToday(String stat) {
        return getForDay(stat, currentDay);
    }

    /**
     * Get the total count for a statistic across all days.
     */
    public int getTotal(String stat) {
        Map<Integer, Integer> dayMap = stats.get(stat);
        if (dayMap == null) return 0;
        int total = 0;
        for (int count : dayMap.values()) {
            total += count;
        }
        return total;
    }

    /**
     * Get the count for a statistic on a specific day.
     */
    public int getForDay(String stat, int day) {
        Map<Integer, Integer> dayMap = stats.get(stat);
        if (dayMap == null) return 0;
        return dayMap.getOrDefault(day, 0);
    }

    /**
     * Get all days that have data for a given stat.
     */
    public Set<Integer> getDaysWithData(String stat) {
        Map<Integer, Integer> dayMap = stats.get(stat);
        if (dayMap == null) return Collections.emptySet();
        return Collections.unmodifiableSet(dayMap.keySet());
    }

    /**
     * Get all tracked stat names.
     */
    public Set<String> getAllStatNames() {
        return Collections.unmodifiableSet(stats.keySet());
    }

    /**
     * Get average per day for a given stat.
     */
    public double getAverage(String stat) {
        Map<Integer, Integer> dayMap = stats.get(stat);
        if (dayMap == null || dayMap.isEmpty()) return 0.0;
        int total = 0;
        for (int count : dayMap.values()) {
            total += count;
        }
        return (double) total / dayMap.size();
    }

    // --- NBT Persistence ---

    public CompoundTag toNbt() {
        CompoundTag root = new CompoundTag();
        root.putString("factionId", factionId);
        root.putInt("currentDay", currentDay);

        CompoundTag statsTag = new CompoundTag();
        for (Map.Entry<String, Map<Integer, Integer>> entry : stats.entrySet()) {
            CompoundTag dayTag = new CompoundTag();
            for (Map.Entry<Integer, Integer> dayEntry : entry.getValue().entrySet()) {
                dayTag.putInt(String.valueOf(dayEntry.getKey()), dayEntry.getValue());
            }
            statsTag.put(entry.getKey(), dayTag);
        }
        root.put("stats", statsTag);
        return root;
    }

    public void fromNbt(CompoundTag root) {
        this.currentDay = root.getIntOr("currentDay", 0);
        stats.clear();

        CompoundTag statsTag = root.getCompoundOrEmpty("stats");
        for (String statName : statsTag.keySet()) {
            CompoundTag dayTag = statsTag.getCompoundOrEmpty(statName);
            Map<Integer, Integer> dayMap = new LinkedHashMap<>();
            for (String dayKey : dayTag.keySet()) {
                try {
                    int day = Integer.parseInt(dayKey);
                    int count = dayTag.getIntOr(dayKey, 0);
                    dayMap.put(day, count);
                } catch (NumberFormatException ignored) {}
            }
            stats.put(statName, dayMap);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        dirty = false;
        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            dir.toFile().mkdirs();
            CompoundTag root = toNbt();
            NbtIo.writeCompressed(root, dir.resolve(FILE_PREFIX + factionId + ".dat"));
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to save colony statistics for faction {}: {}", factionId, e.getMessage());
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            Path file = level.getServer().getWorldPath(LevelResource.ROOT)
                    .resolve("data").resolve(FILE_PREFIX + factionId + ".dat");
            if (!file.toFile().exists()) return;
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            fromNbt(root);
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to load colony statistics for faction {}: {}", factionId, e.getMessage());
        }
    }
}
