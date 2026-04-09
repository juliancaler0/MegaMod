package com.ultra.megamod.feature.toggles;

import com.ultra.megamod.MegaMod;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

public class FeatureToggleManager {
    private static FeatureToggleManager INSTANCE;
    private static final String FILE_NAME = "megamod_feature_toggles.dat";
    private final Map<String, Boolean> toggles = new LinkedHashMap<>();
    private final Map<String, Integer> numericSettings = new LinkedHashMap<>();
    private boolean dirty = false;

    public record FeatureDefinition(String id, String name, String category, String description) {}

    public static final List<FeatureDefinition> ALL_FEATURES = List.of(
        new FeatureDefinition("path_sprinting", "Path Sprinting", "Vanilla Refresh", "Sprint faster on path blocks"),
        new FeatureDefinition("player_sitting", "Player Sitting", "Vanilla Refresh", "Sit on stairs and slabs"),
        new FeatureDefinition("equipable_banners", "Equipable Banners", "Vanilla Refresh", "Wear banners on your head"),
        new FeatureDefinition("totem_void", "Totem in Void", "Vanilla Refresh", "Totem saves you from void death"),
        new FeatureDefinition("head_drops", "Player Head Drops", "Vanilla Refresh", "Players drop heads on death"),
        new FeatureDefinition("wither_head_drops", "Wither Head Drops", "Vanilla Refresh", "Better wither skull drops"),
        new FeatureDefinition("death_sound", "Improved Death Sound", "Vanilla Refresh", "Enhanced death sound effects"),
        new FeatureDefinition("global_death_sound", "Global Death Sound", "Vanilla Refresh", "Everyone hears deaths"),
        new FeatureDefinition("low_health_sound", "Low Health Warning", "Vanilla Refresh", "Sound warning at low HP"),
        new FeatureDefinition("recovery_coords", "Recovery Coordinates", "Vanilla Refresh", "Show death coordinates"),
        new FeatureDefinition("mob_health_display", "Mob Health Display", "Vanilla Refresh", "See mob health bars"),
        new FeatureDefinition("baby_zombies", "Improved Baby Zombies", "Vanilla Refresh", "Better baby zombie behavior"),
        new FeatureDefinition("trimmed_piglins", "Trimmed Piglins", "Vanilla Refresh", "Piglins spawn with armor trims"),
        new FeatureDefinition("echo_shard_silence", "Echo Shard Silence", "Vanilla Refresh", "Echo shards muffle nearby sounds"),
        new FeatureDefinition("spectator_ghost", "Spectator Ghost", "Vanilla Refresh", "Ghost effect in spectator"),
        new FeatureDefinition("homing_xp", "Homing XP Orbs", "Vanilla Refresh", "XP orbs fly toward players"),
        new FeatureDefinition("crops_xp", "Crops XP", "Vanilla Refresh", "Earn XP from harvesting crops"),
        new FeatureDefinition("craft_sounds", "Craft Sounds", "Vanilla Refresh", "Sound effects when crafting"),
        new FeatureDefinition("jukebox_override", "Jukebox Music Override", "Vanilla Refresh", "Enhanced jukebox behavior"),
        new FeatureDefinition("day_counter", "Day Counter", "Vanilla Refresh", "Track days survived"),
        new FeatureDefinition("major_subtitles", "Major Event Subtitles", "Vanilla Refresh", "Subtitles for big events"),
        new FeatureDefinition("biome_subtitles", "Biome Discovery", "Vanilla Refresh", "Notification on new biomes"),
        new FeatureDefinition("readable_clocks", "Readable Clocks", "Vanilla Refresh", "Clocks show actual time"),
        new FeatureDefinition("loyal_tridents", "Extra Loyal Tridents", "Vanilla Refresh", "Tridents return faster"),
        new FeatureDefinition("armor_stands", "Better Armor Stands", "Vanilla Refresh", "Poseable armor stands"),
        new FeatureDefinition("drop_ladder", "Drop Ladder", "Vanilla Refresh", "Ladders place downward"),
        new FeatureDefinition("lodestones", "Better Lodestones", "Vanilla Refresh", "Enhanced lodestone compass"),
        new FeatureDefinition("party_cake", "Party Cake", "Vanilla Refresh", "Cake triggers celebration"),
        new FeatureDefinition("invis_frames", "Invisible Frames", "Vanilla Refresh", "Make item frames invisible"),
        new FeatureDefinition("griefing_rules", "Griefing Gamerules", "Vanilla Refresh", "Per-mob griefing control"),
        new FeatureDefinition("level_up_effects", "Level-Up Effects", "Vanilla Refresh", "Visual effects on level up"),
        new FeatureDefinition("water_splash", "Water Splash Effects", "Vanilla Refresh", "Enhanced water splashes"),
        new FeatureDefinition("elytra_flight", "Elytra Creative Flight", "Vanilla Refresh", "Creative flight with elytra"),
        new FeatureDefinition("villager_refresh", "Villager Trade Refresh", "Vanilla Refresh", "Refresh villager trades"),
        new FeatureDefinition("block_animations", "Block Animations", "Vanilla Refresh", "Improved block animations"),
        new FeatureDefinition("economy", "Economy System", "Economy", "MegaCoin wallet and bank"),
        new FeatureDefinition("megashop", "MegaShop", "Economy", "Daily rotating item shop"),
        new FeatureDefinition("relics", "Relic System", "Combat", "Accessory relics with abilities"),
        new FeatureDefinition("rpg_weapons", "RPG Weapons", "Combat", "Weapons with combat skills"),
        new FeatureDefinition("skill_trees", "Skill Trees", "Combat", "5 progression skill trees"),
        new FeatureDefinition("museum", "Museum System", "Exploration", "Collectible museum building"),
        new FeatureDefinition("dungeons", "Dungeon System", "Exploration", "Roguelike dungeon instances"),
        new FeatureDefinition("admin_computer", "Admin Computer", "Admin", "Computer block admin panel"),
        new FeatureDefinition("multiplayer", "Multiplayer Features", "Multiplayer", "Join/exit sounds, stats, tab display, gamerules"),
        new FeatureDefinition("furniture", "Furniture System", "Decoration", "27 decorative office/home blocks"),
        new FeatureDefinition("death_recovery", "Death Recovery", "Vanilla Refresh", "Gravestones at death location with item recovery"),
        new FeatureDefinition("sorting_system", "Sorting System", "QoL", "One-click container sorting with profiles"),
        new FeatureDefinition("backpacks", "Backpack System", "QoL", "Travelers backpacks with tiered storage and upgrades"),
        new FeatureDefinition("pocket_dimensions", "Pocket Dimensions", "Exploration", "Shared museum/dungeon dimension infrastructure"),
        new FeatureDefinition("skill_item_locks", "Skill Item Locks", "Admin", "Lock items/enchants behind skill branch specialization"),
        new FeatureDefinition("admin_skill_lock_bypass", "Admin Lock Bypass", "Admin", "Admin players bypass all skill item locks"),
        new FeatureDefinition("admin_dungeon_bypass", "Admin Dungeon Bypass", "Admin", "Admin players bypass all dungeon restrictions (pearls, sleep, commands, interactions)"),
        new FeatureDefinition("skill_tree_bypass", "Skill Tree Bypass", "Admin", "Admins can unlock any node regardless of branch limits"),
        new FeatureDefinition("dungeon_loot_glow", "Dungeon Loot Glow", "Admin", "Altars and loot chests glow with outlines through walls in dungeons"),
        new FeatureDefinition("dungeon_block_protection", "Dungeon Block Protection", "Admin", "Prevent non-admin players from placing blocks in dungeons"),
        new FeatureDefinition("museum_block_protection", "Museum Block Protection", "Admin", "Prevent non-admin players from breaking/placing in museum"),
        new FeatureDefinition("admin_modules", "Admin Modules", "Admin", "100+ admin utility modules (KillAura, ESP, Flight, etc.)"),
        new FeatureDefinition("baritone_bots", "Baritone Bots", "Admin", "Server-side bot pathfinding and AI control"),
        new FeatureDefinition("minigames", "Minigames", "Multiplayer", "Computer minigames with score tracking"),
        new FeatureDefinition("audit_log", "Audit Log", "Admin", "Player activity logging (joins, commands, deaths)"),
        new FeatureDefinition("moderation_system", "Moderation", "Admin", "Mutes, bans, warnings, action log"),
        new FeatureDefinition("discovery", "Discovery System", "Exploration", "Encyclopedia discovery tracking per player"),
        new FeatureDefinition("citizens", "Citizen System", "Colony", "Hireable worker and recruit NPCs with AI jobs"),
        new FeatureDefinition("citizen_factions", "Factions & Diplomacy", "Colony", "Player factions with ally/enemy relations and treaties"),
        new FeatureDefinition("citizen_territory", "Territory & Siege", "Colony", "Chunk claiming, permissions, and siege warfare"),
        new FeatureDefinition("ambient_sounds", "Ambient Sounds", "Vanilla Refresh", "Biome-specific environmental audio — birds, wind, cave drips"),
        new FeatureDefinition("resource_dimension", "Resource Dimension", "Exploration", "Craftable key to enter a fresh overworld that resets every 24 hours"),
        new FeatureDefinition("mob_variants", "Mob Variants", "Combat", "Elite and Champion mobs spawn in the world with modifiers"),
        new FeatureDefinition("bounty_hunting", "Bounty Hunting", "Combat", "Hunt named mobs for MegaCoin rewards"),
        new FeatureDefinition("arena", "Arena System", "Combat", "PvE wave arena and PvP combat with ELO ranking"),
        new FeatureDefinition("corruption", "Corruption Spread", "World Events", "Hostile corruption zones that spread and can be purged"),
        new FeatureDefinition("marketplace", "Marketplace", "Economy", "Player-to-player WTS/WTB marketplace with trading terminals"),
        new FeatureDefinition("alchemy", "Alchemy System", "Combat", "Custom potion brewing with reagent grinding and cauldron mechanics"),
        new FeatureDefinition("tree_felling", "Tree Felling", "Farming", "Chop whole trees with an axe (requires Crop Master or Herbalist T3+)"),
        new FeatureDefinition("world_loot", "World Loot Drops", "Exploration", "Relics and weapons drop from mobs, fishing, and structure chests"),
        new FeatureDefinition("builder_admin_bypass", "Builder Admin Bypass", "Admin", "Admin builders skip material requirements — place blocks without needing items in the town chest")
    );

    private static final Map<String, FeatureDefinition> FEATURE_MAP = new LinkedHashMap<>();
    static {
        for (FeatureDefinition def : ALL_FEATURES) {
            FEATURE_MAP.put(def.id(), def);
        }
    }

    public static FeatureToggleManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new FeatureToggleManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void init(ServerLevel level) {
        get(level);
    }

    public static void reset() {
        INSTANCE = null;
    }

    /**
     * Check if a feature is enabled. Call this from each feature's event handler.
     * Example usage in any @EventBusSubscriber feature:
     *   if (level instanceof ServerLevel sl && !FeatureToggleManager.get(sl).isEnabled("path_sprinting")) return;
     */
    // Features that default to OFF instead of ON
    private static final java.util.Set<String> DEFAULT_OFF = java.util.Set.of("dungeon_loot_glow", "mob_variants");

    public boolean isEnabled(String featureId) {
        return toggles.getOrDefault(featureId, !DEFAULT_OFF.contains(featureId));
    }

    public void setEnabled(String featureId, boolean enabled) {
        if (FEATURE_MAP.containsKey(featureId)) {
            toggles.put(featureId, enabled);
            dirty = true;
        }
    }

    // ── Numeric sub-settings ─────────────────────────────────────────
    // Defaults for numeric settings (key -> default value)
    private static final Map<String, Integer> NUMERIC_DEFAULTS = Map.of(
            "builder_speed_multiplier", 1
    );
    // Min/max for numeric settings
    private static final Map<String, int[]> NUMERIC_RANGES = Map.of(
            "builder_speed_multiplier", new int[]{1, 50}
    );

    public int getNumericSetting(String key) {
        return numericSettings.getOrDefault(key, NUMERIC_DEFAULTS.getOrDefault(key, 1));
    }

    public void setNumericSetting(String key, int value) {
        int[] range = NUMERIC_RANGES.get(key);
        if (range != null) {
            value = Math.max(range[0], Math.min(range[1], value));
        }
        numericSettings.put(key, value);
        dirty = true;
    }

    public Map<String, Integer> getAllNumericSettings() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String key : NUMERIC_DEFAULTS.keySet()) {
            result.put(key, getNumericSetting(key));
        }
        return result;
    }

    public void enableAll() {
        for (FeatureDefinition def : ALL_FEATURES) {
            toggles.put(def.id(), true);
        }
        dirty = true;
    }

    public void disableAll() {
        for (FeatureDefinition def : ALL_FEATURES) {
            toggles.put(def.id(), false);
        }
        dirty = true;
    }

    public void resetDefaults() {
        toggles.clear();
        for (FeatureDefinition def : ALL_FEATURES) {
            toggles.put(def.id(), !DEFAULT_OFF.contains(def.id()));
        }
        dirty = true;
    }

    public Map<String, Boolean> getAllToggles() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (FeatureDefinition def : ALL_FEATURES) {
            result.put(def.id(), isEnabled(def.id()));
        }
        return Collections.unmodifiableMap(result);
    }

    public int getEnabledCount() {
        int count = 0;
        for (FeatureDefinition def : ALL_FEATURES) {
            if (isEnabled(def.id())) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return ALL_FEATURES.size();
    }

    public static FeatureDefinition getDefinition(String featureId) {
        return FEATURE_MAP.get(featureId);
    }

    public static List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        for (FeatureDefinition def : ALL_FEATURES) {
            if (!cats.contains(def.category())) {
                cats.add(def.category());
            }
        }
        return cats;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag toggleTag = root.getCompoundOrEmpty("toggles");
                for (String key : toggleTag.keySet()) {
                    toggles.put(key, toggleTag.getBooleanOr(key, true));
                }
                CompoundTag numTag = root.getCompoundOrEmpty("numeric");
                for (String key : numTag.keySet()) {
                    numericSettings.put(key, numTag.getIntOr(key, NUMERIC_DEFAULTS.getOrDefault(key, 1)));
                }
            }
            // Ensure all known features have an entry (respects DEFAULT_OFF)
            for (FeatureDefinition def : ALL_FEATURES) {
                if (!toggles.containsKey(def.id())) {
                    toggles.put(def.id(), !DEFAULT_OFF.contains(def.id()));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load feature toggle data", e);
            // On load failure, default everything to enabled (respects DEFAULT_OFF)
            for (FeatureDefinition def : ALL_FEATURES) {
                toggles.putIfAbsent(def.id(), !DEFAULT_OFF.contains(def.id()));
            }
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) {
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
            CompoundTag toggleTag = new CompoundTag();
            for (Map.Entry<String, Boolean> entry : toggles.entrySet()) {
                toggleTag.putBoolean(entry.getKey(), entry.getValue());
            }
            root.put("toggles", (Tag) toggleTag);
            CompoundTag numTag = new CompoundTag();
            for (Map.Entry<String, Integer> entry : numericSettings.entrySet()) {
                numTag.putInt(entry.getKey(), entry.getValue());
            }
            root.put("numeric", (Tag) numTag);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save feature toggle data", e);
        }
    }
}
