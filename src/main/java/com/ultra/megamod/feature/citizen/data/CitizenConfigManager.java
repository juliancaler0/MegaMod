package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;

public class CitizenConfigManager {
    private static CitizenConfigManager INSTANCE;
    private static final String FILE_NAME = "megamod_citizen_config.dat";

    public static CitizenConfigManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new CitizenConfigManager();
            INSTANCE.loadConfig(level);
        }
        return INSTANCE;
    }

    public static void reset() { INSTANCE = null; }

    public void loadConfig(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CitizenConfig.MAX_CITIZENS_PER_PLAYER = root.getIntOr("maxCitizens", 50);
                CitizenConfig.CHUNK_LOADING_ENABLED = root.getBooleanOr("chunkLoading", false);
                CitizenConfig.UPKEEP_INTERVAL_TICKS = root.getIntOr("upkeepInterval", 24000);
                CitizenConfig.UPKEEP_FAILURE_MODE = root.getStringOr("upkeepFailure", "idle");
                CitizenConfig.HUNGER_RATE = root.getFloatOr("hungerRate", 1.0f);
                CitizenConfig.CITIZEN_MAX_INVENTORY = root.getIntOr("maxInventory", 18);
                CitizenConfig.RECRUIT_XP_MULTIPLIER = root.getFloatOr("xpMultiplier", 1.0f);
                CitizenConfig.SIEGE_DURATION_TICKS = root.getIntOr("siegeDuration", 72000);
                CitizenConfig.SIEGE_HEALTH_DEFAULT = root.getIntOr("siegeHealth", 100);
                CitizenConfig.PATROL_SPAWN_ENABLED = root.getBooleanOr("patrolEnabled", true);
                CitizenConfig.PATROL_SPAWN_INTERVAL = root.getIntOr("patrolInterval", 12000);
                CitizenConfig.PATROL_SPAWN_CHANCE = root.getFloatOr("patrolChance", 0.3f);
                CitizenConfig.WORKERS_ALWAYS_WORK_IN_RAIN = root.getBooleanOr("workersAlwaysWorkInRain", false);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load citizen config data", e);
        }
    }

    public void saveConfig(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putInt("maxCitizens", CitizenConfig.MAX_CITIZENS_PER_PLAYER);
            root.putBoolean("chunkLoading", CitizenConfig.CHUNK_LOADING_ENABLED);
            root.putInt("upkeepInterval", CitizenConfig.UPKEEP_INTERVAL_TICKS);
            root.putString("upkeepFailure", CitizenConfig.UPKEEP_FAILURE_MODE);
            root.putFloat("hungerRate", CitizenConfig.HUNGER_RATE);
            root.putInt("maxInventory", CitizenConfig.CITIZEN_MAX_INVENTORY);
            root.putFloat("xpMultiplier", CitizenConfig.RECRUIT_XP_MULTIPLIER);
            root.putInt("siegeDuration", CitizenConfig.SIEGE_DURATION_TICKS);
            root.putInt("siegeHealth", CitizenConfig.SIEGE_HEALTH_DEFAULT);
            root.putBoolean("patrolEnabled", CitizenConfig.PATROL_SPAWN_ENABLED);
            root.putInt("patrolInterval", CitizenConfig.PATROL_SPAWN_INTERVAL);
            root.putFloat("patrolChance", CitizenConfig.PATROL_SPAWN_CHANCE);
            root.putBoolean("workersAlwaysWorkInRain", CitizenConfig.WORKERS_ALWAYS_WORK_IN_RAIN);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save citizen config data", e);
        }
    }
}
