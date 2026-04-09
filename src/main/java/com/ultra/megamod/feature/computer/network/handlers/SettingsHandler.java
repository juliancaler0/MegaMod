package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsHandler {

    private static final Map<UUID, Map<String, Boolean>> playerSettings = new HashMap<>();
    private static boolean dirty = false;
    private static boolean loaded = false;
    private static final String FILE_NAME = "megamod_player_settings.dat";

    // Setting keys with display names (insertion-ordered)
    private static final LinkedHashMap<String, String> SETTING_NAMES = new LinkedHashMap<>();
    static {
        // HUD Elements
        SETTING_NAMES.put("hud_mob_health", "Mob Health Display");
        SETTING_NAMES.put("hud_readable_clock", "Readable Clock");
        SETTING_NAMES.put("hud_combat_text", "Combat Text");
        SETTING_NAMES.put("hud_skill_bar", "Skill XP Bar");
        SETTING_NAMES.put("hud_ability_bar", "Ability Cooldown Bar");
        // Visual Effects
        SETTING_NAMES.put("fx_level_up", "Level-Up Effects");
        SETTING_NAMES.put("fx_water_splash", "Water Splash Effects");
        SETTING_NAMES.put("fx_block_animations", "Block Animations");
        // Gameplay
        SETTING_NAMES.put("gp_homing_xp", "Homing XP Orbs");
        SETTING_NAMES.put("gp_path_sprint", "Path Sprinting");
        SETTING_NAMES.put("gp_death_sound", "Death Sounds");
        SETTING_NAMES.put("gp_craft_sounds", "Craft Sounds");
        SETTING_NAMES.put("gp_low_health", "Low Health Warning");
        // Notifications
        SETTING_NAMES.put("notif_join_exit", "Join/Exit Sounds");
        SETTING_NAMES.put("notif_day_counter", "Day Announcements");
        SETTING_NAMES.put("notif_biome_discovery", "Biome Discovery Titles");
        // Skills
        SETTING_NAMES.put("skill_badge", "Skill Chat Badge");
        SETTING_NAMES.put("skill_particles", "Skill Particle Aura");
        SETTING_NAMES.put("hud_waypoint_beacons", "Waypoint Beacons");
        // HUD additions
        SETTING_NAMES.put("hud_party_health", "Party Health Bars");
        SETTING_NAMES.put("hud_quest_tracker", "Quest/Bounty Tracker");
        SETTING_NAMES.put("hud_kill_combo", "Kill Combo Counter");
        SETTING_NAMES.put("hud_loot_log", "Loot Pickup Log");
        SETTING_NAMES.put("hud_status_effects", "Status Effect Bar");
        // FX additions
        SETTING_NAMES.put("fx_screen_shake", "Screen Shake");
        SETTING_NAMES.put("fx_low_hp_vignette", "Low Health Vignette");
    }

    // Categories with their setting keys (insertion-ordered)
    private static final LinkedHashMap<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("HUD Elements", List.of("hud_mob_health", "hud_readable_clock", "hud_combat_text", "hud_skill_bar", "hud_ability_bar", "hud_waypoint_beacons", "hud_party_health", "hud_quest_tracker", "hud_kill_combo", "hud_loot_log", "hud_status_effects"));
        CATEGORIES.put("Visual Effects", List.of("fx_level_up", "fx_water_splash", "fx_block_animations", "fx_screen_shake", "fx_low_hp_vignette"));
        CATEGORIES.put("Gameplay", List.of("gp_homing_xp", "gp_path_sprint", "gp_death_sound", "gp_craft_sounds", "gp_low_health"));
        CATEGORIES.put("Notifications", List.of("notif_join_exit", "notif_day_counter", "notif_biome_discovery"));
    }

    /**
     * Handles settings actions from the computer screen.
     * Returns true if the action was handled, false otherwise.
     */
    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "settings_request": {
                ensureLoaded(level);
                sendSettingsData(player, eco);
                return true;
            }
            case "settings_toggle": {
                ensureLoaded(level);
                handleToggle(player, jsonData, level, eco);
                return true;
            }
            case "settings_reset": {
                ensureLoaded(level);
                handleReset(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    /**
     * Checks whether a specific setting is enabled for a player.
     * Returns true by default if the setting has never been set.
     */
    public static boolean isEnabled(UUID playerUuid, String settingKey) {
        Map<String, Boolean> settings = playerSettings.get(playerUuid);
        if (settings == null) {
            return true;
        }
        return settings.getOrDefault(settingKey, true);
    }

    /**
     * Programmatically toggle a setting for a player (used by commands).
     * Does not require a ServerLevel for save — will be saved on next server tick/stop.
     */
    public static void toggleSetting(UUID playerUuid, String settingKey) {
        Map<String, Boolean> settings = playerSettings.computeIfAbsent(playerUuid, k -> new java.util.HashMap<>());
        boolean currentValue = settings.getOrDefault(settingKey, true);
        settings.put(settingKey, !currentValue);
        dirty = true;
    }

    private static void handleToggle(ServerPlayer player, String settingKey, ServerLevel level, EconomyManager eco) {
        if (settingKey == null || settingKey.isEmpty() || !SETTING_NAMES.containsKey(settingKey)) {
            sendResult(player, false, "Unknown setting: " + settingKey, eco);
            return;
        }

        UUID playerId = player.getUUID();
        Map<String, Boolean> settings = playerSettings.computeIfAbsent(playerId, k -> new HashMap<>());
        boolean currentValue = settings.getOrDefault(settingKey, true);
        settings.put(settingKey, !currentValue);
        dirty = true;
        saveToDisk(level);

        // Send back the full updated settings
        sendSettingsData(player, eco);
    }

    private static void handleReset(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        playerSettings.remove(playerId);
        dirty = true;
        saveToDisk(level);

        sendResult(player, true, "All settings reset to defaults.", eco);
        sendSettingsData(player, eco);
    }

    private static void sendSettingsData(ServerPlayer player, EconomyManager eco) {
        UUID playerId = player.getUUID();
        Map<String, Boolean> settings = playerSettings.getOrDefault(playerId, new HashMap<>());

        JsonObject root = new JsonObject();
        JsonArray categoriesArr = new JsonArray();

        for (Map.Entry<String, List<String>> catEntry : CATEGORIES.entrySet()) {
            JsonObject catObj = new JsonObject();
            catObj.addProperty("name", catEntry.getKey());
            JsonArray settingsArr = new JsonArray();

            for (String key : catEntry.getValue()) {
                JsonObject settingObj = new JsonObject();
                settingObj.addProperty("key", key);
                settingObj.addProperty("name", SETTING_NAMES.getOrDefault(key, key));
                settingObj.addProperty("enabled", settings.getOrDefault(key, true));
                settingsArr.add(settingObj);
            }

            catObj.add("settings", settingsArr);
            categoriesArr.add(catObj);
        }

        root.add("categories", categoriesArr);
        sendResponse(player, "settings_data", root.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "settings_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    // --- NbtIo Persistence ---

    private static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
            loaded = true;
        }
    }

    public static void loadFromDisk(ServerLevel level) {
        playerSettings.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    CompoundTag playerTag = players.getCompoundOrEmpty(key);
                    Map<String, Boolean> settings = new HashMap<>();
                    for (String settingKey : playerTag.keySet()) {
                        settings.put(settingKey, playerTag.getBooleanOr(settingKey, true));
                    }
                    playerSettings.put(uuid, settings);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load settings data", e);
        }
        dirty = false;
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();

            for (Map.Entry<UUID, Map<String, Boolean>> entry : playerSettings.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                for (Map.Entry<String, Boolean> setting : entry.getValue().entrySet()) {
                    playerTag.putBoolean(setting.getKey(), setting.getValue());
                }
                players.put(entry.getKey().toString(), (Tag) playerTag);
            }

            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save settings data", e);
        }
    }

    public static void reset() {
        playerSettings.clear();
        dirty = false;
        loaded = false;
    }
}
