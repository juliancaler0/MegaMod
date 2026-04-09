package com.ultra.megamod.feature.alchemy;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Per-player alchemy state: discovered recipes, brew counts, alchemy level.
 */
public class AlchemyManager {
    private static AlchemyManager INSTANCE;
    private static final String FILE_NAME = "megamod_alchemy.dat";

    private final Map<UUID, PlayerAlchemyData> playerData = new HashMap<>();
    private final Set<String> disabledRecipes = new HashSet<>();
    private boolean dirty = false;

    public static AlchemyManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new AlchemyManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void markDirty() {
        dirty = true;
    }

    private PlayerAlchemyData getOrCreate(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerAlchemyData());
    }

    // ==================== Public API ====================

    public void discoverRecipe(UUID player, String recipeId) {
        getOrCreate(player).discoveredRecipes.add(recipeId);
        markDirty();
    }

    public boolean hasDiscovered(UUID player, String recipeId) {
        return getOrCreate(player).discoveredRecipes.contains(recipeId);
    }

    public Set<String> getDiscoveredRecipes(UUID player) {
        return Collections.unmodifiableSet(getOrCreate(player).discoveredRecipes);
    }

    public void incrementBrewCount(UUID player, String potionId) {
        PlayerAlchemyData data = getOrCreate(player);
        data.brewCount.merge(potionId, 1, Integer::sum);
        markDirty();
    }

    public int getBrewCount(UUID player, String potionId) {
        return getOrCreate(player).brewCount.getOrDefault(potionId, 0);
    }

    public Map<String, Integer> getAllBrewCounts(UUID player) {
        return Collections.unmodifiableMap(getOrCreate(player).brewCount);
    }

    /**
     * Alchemy level derived from total brews (0-50).
     * Every 5 total brews = 1 level, up to 50.
     */
    public int getAlchemyLevel(UUID player) {
        PlayerAlchemyData data = getOrCreate(player);
        int totalBrews = 0;
        for (int count : data.brewCount.values()) {
            totalBrews += count;
        }
        return Math.min(50, totalBrews / 5);
    }

    public int getTotalBrews(UUID player) {
        int total = 0;
        for (int count : getOrCreate(player).brewCount.values()) {
            total += count;
        }
        return total;
    }

    // ==================== Admin API ====================

    public boolean isRecipeDisabled(String recipeId) {
        return disabledRecipes.contains(recipeId);
    }

    public boolean toggleRecipe(String recipeId) {
        boolean nowEnabled;
        if (disabledRecipes.contains(recipeId)) {
            disabledRecipes.remove(recipeId);
            nowEnabled = true;
        } else {
            disabledRecipes.add(recipeId);
            nowEnabled = false;
        }
        markDirty();
        return nowEnabled;
    }

    public void discoverAllRecipes(UUID player) {
        PlayerAlchemyData data = getOrCreate(player);
        for (AlchemyRecipeRegistry.BrewingRecipe recipe : AlchemyRecipeRegistry.getAllBrewingRecipes()) {
            data.discoveredRecipes.add(recipe.id());
        }
        for (AlchemyRecipeRegistry.GrindingRecipe recipe : AlchemyRecipeRegistry.getAllGrindingRecipes()) {
            data.discoveredRecipes.add(recipe.id());
        }
        markDirty();
    }

    public Map<UUID, ?> getAllPlayerData() {
        return Collections.unmodifiableMap(playerData);
    }

    // ==================== Persistence ====================

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
            File dataDir = worldDir.resolve("data").toFile();
            if (!dataDir.exists()) dataDir.mkdirs();

            File file = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();

            for (Map.Entry<UUID, PlayerAlchemyData> entry : playerData.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                PlayerAlchemyData data = entry.getValue();

                // Discovered recipes
                CompoundTag recipesTag = new CompoundTag();
                int idx = 0;
                for (String recipe : data.discoveredRecipes) {
                    recipesTag.putString("r" + idx, recipe);
                    idx++;
                }
                recipesTag.putInt("count", idx);
                playerTag.put("Discovered", recipesTag);

                // Brew counts
                CompoundTag brewTag = new CompoundTag();
                for (Map.Entry<String, Integer> brewEntry : data.brewCount.entrySet()) {
                    brewTag.putInt(brewEntry.getKey(), brewEntry.getValue());
                }
                playerTag.put("BrewCounts", brewTag);

                root.put(entry.getKey().toString(), playerTag);
            }

            // Save disabled recipes
            CompoundTag disabledTag = new CompoundTag();
            int dIdx = 0;
            for (String rid : disabledRecipes) {
                disabledTag.putString("d" + dIdx, rid);
                dIdx++;
            }
            disabledTag.putInt("count", dIdx);
            root.put("_disabled", disabledTag);

            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save alchemy data", e);
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
            File file = worldDir.resolve("data").resolve(FILE_NAME).toFile();
            if (!file.exists()) return;

            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());

            // Load disabled recipes
            CompoundTag disabledTag = root.getCompoundOrEmpty("_disabled");
            int dCount = disabledTag.getIntOr("count", 0);
            for (int i = 0; i < dCount; i++) {
                String rid = disabledTag.getStringOr("d" + i, "");
                if (!rid.isEmpty()) disabledRecipes.add(rid);
            }

            for (String key : root.keySet()) {
                if (key.startsWith("_")) continue; // Skip metadata keys
                try {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag playerTag = root.getCompoundOrEmpty(key);
                    PlayerAlchemyData data = new PlayerAlchemyData();

                    // Load discovered recipes
                    CompoundTag recipesTag = playerTag.getCompoundOrEmpty("Discovered");
                    int count = recipesTag.getIntOr("count", 0);
                    for (int i = 0; i < count; i++) {
                        String recipe = recipesTag.getStringOr("r" + i, "");
                        if (!recipe.isEmpty()) {
                            data.discoveredRecipes.add(recipe);
                        }
                    }

                    // Load brew counts
                    CompoundTag brewTag = playerTag.getCompoundOrEmpty("BrewCounts");
                    for (String brewKey : brewTag.keySet()) {
                        int brewCount = brewTag.getIntOr(brewKey, 0);
                        if (brewCount > 0) {
                            data.brewCount.put(brewKey, brewCount);
                        }
                    }

                    playerData.put(uuid, data);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load alchemy data", e);
        }
    }

    // ==================== Inner Class ====================

    private static class PlayerAlchemyData {
        final Set<String> discoveredRecipes = new HashSet<>();
        final Map<String, Integer> brewCount = new HashMap<>();
    }
}
