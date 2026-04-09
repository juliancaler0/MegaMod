package com.ultra.megamod.feature.prestige;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages purchasable prestige rewards using Marks of Mastery.
 * Handles titles, particle effects, death messages, and permanent bonuses.
 */
public class PrestigeRewardManager {
    private static PrestigeRewardManager INSTANCE;
    private static final String FILE_NAME = "megamod_prestige_rewards.dat";

    // Per-player purchased rewards
    private final Map<UUID, Set<String>> purchasedRewards = new HashMap<>();
    // Per-player active selections (which title/particle/death_msg is active)
    // e.g. {uuid -> {"title" -> "champion", "particle" -> "flame_aura", "death_msg" -> "dramatic"}}
    private final Map<UUID, Map<String, String>> activeSelections = new HashMap<>();
    private boolean dirty = false;

    // ── Reward Record ──

    public record PrestigeReward(String displayName, int cost, String category, String description) {}

    // ── Reward Catalogs ──

    public static final Map<String, PrestigeReward> TITLES = Map.ofEntries(
        Map.entry("champion", new PrestigeReward("Champion", 50, "title", "Prestigious fighter title")),
        Map.entry("legend", new PrestigeReward("Legend", 100, "title", "Legendary status title")),
        Map.entry("mythic_hero", new PrestigeReward("Mythic Hero", 200, "title", "Completed mythic challenges")),
        Map.entry("eternal_one", new PrestigeReward("The Eternal One", 500, "title", "Master of all systems")),
        Map.entry("dungeon_master", new PrestigeReward("Dungeon Master", 150, "title", "Conquered all dungeon tiers")),
        Map.entry("colony_lord", new PrestigeReward("Colony Lord", 120, "title", "Built a thriving colony")),
        Map.entry("fortune_seeker", new PrestigeReward("Fortune Seeker", 80, "title", "Casino high roller")),
        Map.entry("shadow_walker", new PrestigeReward("Shadow Walker", 100, "title", "Explored the void")),
        Map.entry("arcane_sage", new PrestigeReward("Arcane Sage", 130, "title", "Mastered arcane arts")),
        Map.entry("iron_will", new PrestigeReward("Iron Will", 90, "title", "Survived the impossible"))
    );

    public static final Map<String, PrestigeReward> PARTICLES = Map.ofEntries(
        Map.entry("flame_aura", new PrestigeReward("Flame Aura", 75, "particle", "Ambient flame particles")),
        Map.entry("enchant_aura", new PrestigeReward("Enchant Aura", 60, "particle", "Enchantment table particles")),
        Map.entry("end_rod_trail", new PrestigeReward("End Rod Trail", 80, "particle", "Glowing end rod particles")),
        Map.entry("soul_fire", new PrestigeReward("Soul Fire", 100, "particle", "Blue soul flame particles")),
        Map.entry("totem_burst", new PrestigeReward("Totem Radiance", 120, "particle", "Totem of undying particles")),
        Map.entry("cherry_blossom", new PrestigeReward("Cherry Blossom", 90, "particle", "Falling cherry petals")),
        Map.entry("void_tendrils", new PrestigeReward("Void Tendrils", 150, "particle", "Dark portal particles")),
        Map.entry("heart_trail", new PrestigeReward("Heart Trail", 70, "particle", "Floating hearts"))
    );

    public static final Map<String, PrestigeReward> DEATH_MESSAGES = Map.ofEntries(
        Map.entry("dramatic", new PrestigeReward("Dramatic Death", 40, "death_msg", "X has fallen in battle!")),
        Map.entry("heroic", new PrestigeReward("Heroic Death", 50, "death_msg", "X made the ultimate sacrifice!")),
        Map.entry("comedic", new PrestigeReward("Comedic Death", 35, "death_msg", "X forgot how to live.")),
        Map.entry("mysterious", new PrestigeReward("Mysterious Death", 45, "death_msg", "X vanished into the void...")),
        Map.entry("legendary", new PrestigeReward("Legendary Death", 80, "death_msg", "The legend of X has ended... for now.")),
        Map.entry("ominous", new PrestigeReward("Ominous Death", 60, "death_msg", "X was claimed by darkness."))
    );

    public static final Map<String, PrestigeReward> BONUSES = Map.ofEntries(
        Map.entry("coin_bonus_5", new PrestigeReward("+5% Coin Drops", 100, "bonus", "Permanent 5% more MegaCoins")),
        Map.entry("coin_bonus_10", new PrestigeReward("+10% Coin Drops", 250, "bonus", "Permanent 10% more MegaCoins")),
        Map.entry("xp_bonus_5", new PrestigeReward("+5% XP Gain", 100, "bonus", "Permanent 5% more experience")),
        Map.entry("xp_bonus_10", new PrestigeReward("+10% XP Gain", 250, "bonus", "Permanent 10% more experience")),
        Map.entry("relic_xp_bonus", new PrestigeReward("+10% Relic XP", 150, "bonus", "Relics gain 10% more XP")),
        Map.entry("loot_luck", new PrestigeReward("+5% Loot Quality", 200, "bonus", "Better dungeon loot quality"))
    );

    // ── Class-Specific Rewards ──
    // Each class gets exclusive titles and bonuses purchasable only by that class.

    public static final Map<String, PrestigeReward> CLASS_TITLES = Map.ofEntries(
        // Paladin
        Map.entry("paladin_crusader", new PrestigeReward("Holy Crusader", 120, "class_title", "Paladin exclusive title")),
        Map.entry("paladin_guardian", new PrestigeReward("Divine Guardian", 200, "class_title", "Paladin exclusive title")),
        Map.entry("paladin_saint", new PrestigeReward("Saint", 400, "class_title", "Paladin legendary title")),
        // Warrior
        Map.entry("warrior_gladiator", new PrestigeReward("Gladiator", 120, "class_title", "Warrior exclusive title")),
        Map.entry("warrior_berserker", new PrestigeReward("Berserker Lord", 200, "class_title", "Warrior exclusive title")),
        Map.entry("warrior_warlord", new PrestigeReward("Warlord", 400, "class_title", "Warrior legendary title")),
        // Wizard
        Map.entry("wizard_archmage", new PrestigeReward("Archmage", 120, "class_title", "Wizard exclusive title")),
        Map.entry("wizard_sorcerer", new PrestigeReward("Grand Sorcerer", 200, "class_title", "Wizard exclusive title")),
        Map.entry("wizard_magus", new PrestigeReward("Magus Supreme", 400, "class_title", "Wizard legendary title")),
        // Rogue
        Map.entry("rogue_phantom", new PrestigeReward("Phantom", 120, "class_title", "Rogue exclusive title")),
        Map.entry("rogue_shadowmaster", new PrestigeReward("Shadowmaster", 200, "class_title", "Rogue exclusive title")),
        Map.entry("rogue_voidwalker", new PrestigeReward("Voidwalker", 400, "class_title", "Rogue legendary title")),
        // Ranger
        Map.entry("ranger_warden", new PrestigeReward("Warden", 120, "class_title", "Ranger exclusive title")),
        Map.entry("ranger_beastmaster", new PrestigeReward("Beastmaster", 200, "class_title", "Ranger exclusive title")),
        Map.entry("ranger_apex", new PrestigeReward("Apex Predator", 400, "class_title", "Ranger legendary title"))
    );

    public static final Map<String, PrestigeReward> CLASS_BONUSES = Map.ofEntries(
        // Paladin
        Map.entry("paladin_heal_bonus", new PrestigeReward("+10% Heal Power", 150, "class_bonus", "Paladin: healing spells 10% stronger")),
        Map.entry("paladin_shield_bonus", new PrestigeReward("+5% Block Reduction", 200, "class_bonus", "Paladin: shield blocks reduce 5% more damage")),
        // Warrior
        Map.entry("warrior_melee_bonus", new PrestigeReward("+10% Melee Damage", 150, "class_bonus", "Warrior: melee attacks deal 10% more damage")),
        Map.entry("warrior_cleave_bonus", new PrestigeReward("+15% Cleave Range", 200, "class_bonus", "Warrior: sweeping attacks hit wider")),
        // Wizard
        Map.entry("wizard_spell_bonus", new PrestigeReward("+10% Spell Damage", 150, "class_bonus", "Wizard: spell damage increased by 10%")),
        Map.entry("wizard_cdr_bonus", new PrestigeReward("-5% Spell Cooldowns", 200, "class_bonus", "Wizard: spell cooldowns reduced by 5%")),
        // Rogue
        Map.entry("rogue_crit_bonus", new PrestigeReward("+10% Crit Chance", 150, "class_bonus", "Rogue: critical hit chance increased by 10%")),
        Map.entry("rogue_stealth_bonus", new PrestigeReward("+3s Stealth Duration", 200, "class_bonus", "Rogue: vanish lasts 3 seconds longer")),
        // Ranger
        Map.entry("ranger_range_bonus", new PrestigeReward("+15% Arrow Range", 150, "class_bonus", "Ranger: projectiles travel 15% farther")),
        Map.entry("ranger_nature_bonus", new PrestigeReward("+10% Nature Spell Power", 200, "class_bonus", "Ranger: nature spells 10% stronger"))
    );

    /**
     * Maps class-specific reward IDs to the required PlayerClass.
     */
    private static final Map<String, String> CLASS_REWARD_REQUIREMENTS = new LinkedHashMap<>();
    static {
        // Paladin
        CLASS_REWARD_REQUIREMENTS.put("paladin_crusader", "PALADIN");
        CLASS_REWARD_REQUIREMENTS.put("paladin_guardian", "PALADIN");
        CLASS_REWARD_REQUIREMENTS.put("paladin_saint", "PALADIN");
        CLASS_REWARD_REQUIREMENTS.put("paladin_heal_bonus", "PALADIN");
        CLASS_REWARD_REQUIREMENTS.put("paladin_shield_bonus", "PALADIN");
        // Warrior
        CLASS_REWARD_REQUIREMENTS.put("warrior_gladiator", "WARRIOR");
        CLASS_REWARD_REQUIREMENTS.put("warrior_berserker", "WARRIOR");
        CLASS_REWARD_REQUIREMENTS.put("warrior_warlord", "WARRIOR");
        CLASS_REWARD_REQUIREMENTS.put("warrior_melee_bonus", "WARRIOR");
        CLASS_REWARD_REQUIREMENTS.put("warrior_cleave_bonus", "WARRIOR");
        // Wizard
        CLASS_REWARD_REQUIREMENTS.put("wizard_archmage", "WIZARD");
        CLASS_REWARD_REQUIREMENTS.put("wizard_sorcerer", "WIZARD");
        CLASS_REWARD_REQUIREMENTS.put("wizard_magus", "WIZARD");
        CLASS_REWARD_REQUIREMENTS.put("wizard_spell_bonus", "WIZARD");
        CLASS_REWARD_REQUIREMENTS.put("wizard_cdr_bonus", "WIZARD");
        // Rogue
        CLASS_REWARD_REQUIREMENTS.put("rogue_phantom", "ROGUE");
        CLASS_REWARD_REQUIREMENTS.put("rogue_shadowmaster", "ROGUE");
        CLASS_REWARD_REQUIREMENTS.put("rogue_voidwalker", "ROGUE");
        CLASS_REWARD_REQUIREMENTS.put("rogue_crit_bonus", "ROGUE");
        CLASS_REWARD_REQUIREMENTS.put("rogue_stealth_bonus", "ROGUE");
        // Ranger
        CLASS_REWARD_REQUIREMENTS.put("ranger_warden", "RANGER");
        CLASS_REWARD_REQUIREMENTS.put("ranger_beastmaster", "RANGER");
        CLASS_REWARD_REQUIREMENTS.put("ranger_apex", "RANGER");
        CLASS_REWARD_REQUIREMENTS.put("ranger_range_bonus", "RANGER");
        CLASS_REWARD_REQUIREMENTS.put("ranger_nature_bonus", "RANGER");
    }

    /**
     * Returns the required class name for a class-specific reward, or null if unrestricted.
     */
    public static String getClassRequirement(String rewardId) {
        return CLASS_REWARD_REQUIREMENTS.get(rewardId);
    }

    // ── Singleton ──

    public static PrestigeRewardManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new PrestigeRewardManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ── Purchase / Ownership ──

    /**
     * Attempt to purchase a reward. Deducts Marks of Mastery via MasteryMarkManager.
     * Returns null on success, or an error message string on failure.
     */
    public String purchaseReward(net.minecraft.server.level.ServerPlayer player, String rewardId) {
        PrestigeReward reward = findReward(rewardId);
        if (reward == null) return "Unknown reward.";

        UUID uuid = player.getUUID();
        if (hasPurchased(uuid, rewardId)) return "Already purchased!";

        // Check class requirement for class-specific rewards
        String requiredClass = getClassRequirement(rewardId);
        if (requiredClass != null) {
            try {
                ServerLevel overworld = player.level().getServer().overworld();
                com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass playerClass =
                    com.ultra.megamod.feature.combat.PlayerClassManager.get(overworld).getPlayerClass(uuid);
                if (!playerClass.name().equals(requiredClass)) {
                    return "This reward requires the " + requiredClass + " class!";
                }
            } catch (Exception e) {
                return "Cannot verify class requirement.";
            }
        }

        ServerLevel overworld = player.level().getServer().overworld();
        MasteryMarkManager marks = MasteryMarkManager.get(overworld);
        if (!marks.spendMarks(uuid, reward.cost())) {
            return "Not enough Marks! Need " + reward.cost() + ", have " + marks.getMarks(uuid) + ".";
        }
        marks.saveToDisk(overworld);

        purchasedRewards.computeIfAbsent(uuid, k -> new HashSet<>()).add(rewardId);
        dirty = true;
        return null; // success
    }

    public boolean hasPurchased(UUID playerId, String rewardId) {
        Set<String> set = purchasedRewards.get(playerId);
        return set != null && set.contains(rewardId);
    }

    // ── Active Selections ──

    public void setActive(UUID playerId, String category, String rewardId) {
        activeSelections.computeIfAbsent(playerId, k -> new HashMap<>()).put(category, rewardId);
        dirty = true;
    }

    /**
     * Clear the active selection for a category. Passing empty rewardId deactivates.
     */
    public void clearActive(UUID playerId, String category) {
        Map<String, String> selections = activeSelections.get(playerId);
        if (selections != null) {
            selections.remove(category);
            dirty = true;
        }
    }

    public String getActive(UUID playerId, String category) {
        Map<String, String> selections = activeSelections.get(playerId);
        if (selections == null) return "";
        return selections.getOrDefault(category, "");
    }

    // ── Convenience Getters ──

    public String getActiveTitle(UUID playerId) {
        String rewardId = getActive(playerId, "title");
        if (rewardId.isEmpty()) return "";
        PrestigeReward reward = TITLES.get(rewardId);
        if (reward == null) reward = CLASS_TITLES.get(rewardId);
        return reward != null ? reward.displayName() : "";
    }

    public String getActiveParticleId(UUID playerId) {
        return getActive(playerId, "particle");
    }

    public String getActiveDeathMessage(UUID playerId) {
        String rewardId = getActive(playerId, "death_msg");
        if (rewardId.isEmpty()) return "";
        PrestigeReward reward = DEATH_MESSAGES.get(rewardId);
        return reward != null ? reward.description() : "";
    }

    /**
     * Returns a cumulative multiplier for the given bonus type.
     * For example, if player has both coin_bonus_5 and coin_bonus_10 purchased,
     * getBonusMultiplier(uuid, "coin") returns 1.15 (5% + 10%).
     */
    public double getBonusMultiplier(UUID playerId, String bonusType) {
        double multiplier = 1.0;
        Set<String> purchased = purchasedRewards.get(playerId);
        if (purchased == null) return multiplier;

        switch (bonusType) {
            case "coin" -> {
                if (purchased.contains("coin_bonus_5")) multiplier += 0.05;
                if (purchased.contains("coin_bonus_10")) multiplier += 0.10;
            }
            case "xp" -> {
                if (purchased.contains("xp_bonus_5")) multiplier += 0.05;
                if (purchased.contains("xp_bonus_10")) multiplier += 0.10;
            }
            case "relic_xp" -> {
                if (purchased.contains("relic_xp_bonus")) multiplier += 0.10;
            }
            case "loot" -> {
                if (purchased.contains("loot_luck")) multiplier += 0.05;
            }
            // Class-specific bonuses
            case "heal_power" -> {
                if (purchased.contains("paladin_heal_bonus")) multiplier += 0.10;
            }
            case "shield_block" -> {
                if (purchased.contains("paladin_shield_bonus")) multiplier += 0.05;
            }
            case "melee_damage" -> {
                if (purchased.contains("warrior_melee_bonus")) multiplier += 0.10;
            }
            case "cleave_range" -> {
                if (purchased.contains("warrior_cleave_bonus")) multiplier += 0.15;
            }
            case "spell_damage" -> {
                if (purchased.contains("wizard_spell_bonus")) multiplier += 0.10;
            }
            case "spell_cdr" -> {
                if (purchased.contains("wizard_cdr_bonus")) multiplier -= 0.05;
            }
            case "crit_chance" -> {
                if (purchased.contains("rogue_crit_bonus")) multiplier += 0.10;
            }
            case "arrow_range" -> {
                if (purchased.contains("ranger_range_bonus")) multiplier += 0.15;
            }
            case "nature_spell" -> {
                if (purchased.contains("ranger_nature_bonus")) multiplier += 0.10;
            }
        }
        return multiplier;
    }

    // ── Catalog Helpers ──

    /**
     * Returns a combined map of all reward categories keyed by reward ID.
     */
    public Map<String, PrestigeReward> getAllRewards() {
        Map<String, PrestigeReward> all = new LinkedHashMap<>();
        all.putAll(TITLES);
        all.putAll(PARTICLES);
        all.putAll(DEATH_MESSAGES);
        all.putAll(BONUSES);
        all.putAll(CLASS_TITLES);
        all.putAll(CLASS_BONUSES);
        return all;
    }

    public Set<String> getPurchasedRewards(UUID playerId) {
        return purchasedRewards.getOrDefault(playerId, Set.of());
    }

    /**
     * Find a reward by ID across all categories.
     */
    private static PrestigeReward findReward(String rewardId) {
        PrestigeReward r = TITLES.get(rewardId);
        if (r != null) return r;
        r = PARTICLES.get(rewardId);
        if (r != null) return r;
        r = DEATH_MESSAGES.get(rewardId);
        if (r != null) return r;
        r = BONUSES.get(rewardId);
        if (r != null) return r;
        r = CLASS_TITLES.get(rewardId);
        if (r != null) return r;
        r = CLASS_BONUSES.get(rewardId);
        return r;
    }

    // ── JSON Serialization (for network sync) ──

    /**
     * Serialize a player's reward state to JSON for the computer app.
     */
    public String toJson(UUID playerId) {
        JsonObject root = new JsonObject();

        // All rewards by category
        JsonArray titlesArr = rewardCatalogToJson(TITLES, playerId);
        JsonArray particlesArr = rewardCatalogToJson(PARTICLES, playerId);
        JsonArray deathMsgsArr = rewardCatalogToJson(DEATH_MESSAGES, playerId);
        JsonArray bonusesArr = rewardCatalogToJson(BONUSES, playerId);
        JsonArray classTitlesArr = rewardCatalogToJson(CLASS_TITLES, playerId);
        JsonArray classBonusesArr = rewardCatalogToJson(CLASS_BONUSES, playerId);

        root.add("titles", titlesArr);
        root.add("particles", particlesArr);
        root.add("death_messages", deathMsgsArr);
        root.add("bonuses", bonusesArr);
        root.add("class_titles", classTitlesArr);
        root.add("class_bonuses", classBonusesArr);

        // Active selections
        JsonObject active = new JsonObject();
        active.addProperty("title", getActive(playerId, "title"));
        active.addProperty("particle", getActive(playerId, "particle"));
        active.addProperty("death_msg", getActive(playerId, "death_msg"));
        root.add("active", active);

        // Player marks balance
        // Note: caller should add marks separately since we don't have ServerLevel here

        return root.toString();
    }

    private JsonArray rewardCatalogToJson(Map<String, PrestigeReward> catalog, UUID playerId) {
        JsonArray arr = new JsonArray();
        for (var entry : catalog.entrySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", entry.getKey());
            obj.addProperty("name", entry.getValue().displayName());
            obj.addProperty("cost", entry.getValue().cost());
            obj.addProperty("category", entry.getValue().category());
            obj.addProperty("description", entry.getValue().description());
            obj.addProperty("purchased", hasPurchased(playerId, entry.getKey()));
            arr.add(obj);
        }
        return arr;
    }

    // ── Persistence ──

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        dirty = false;
        try {
            Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
            dir.toFile().mkdirs();

            CompoundTag root = new CompoundTag();

            // Save purchased rewards
            CompoundTag purchasedTag = new CompoundTag();
            for (var entry : purchasedRewards.entrySet()) {
                StringBuilder sb = new StringBuilder();
                for (String id : entry.getValue()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(id);
                }
                purchasedTag.putString(entry.getKey().toString(), sb.toString());
            }
            root.put("purchased", purchasedTag);

            // Save active selections
            CompoundTag activeTag = new CompoundTag();
            for (var entry : activeSelections.entrySet()) {
                CompoundTag playerActive = new CompoundTag();
                for (var sel : entry.getValue().entrySet()) {
                    playerActive.putString(sel.getKey(), sel.getValue());
                }
                activeTag.put(entry.getKey().toString(), playerActive);
            }
            root.put("active", activeTag);

            NbtIo.writeCompressed(root, dir.resolve(FILE_NAME));
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to save prestige rewards: {}", e.getMessage());
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            Path file = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
            if (!file.toFile().exists()) return;
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());

            // Load purchased rewards
            CompoundTag purchasedTag = root.getCompoundOrEmpty("purchased");
            for (String key : purchasedTag.keySet()) {
                try {
                    UUID id = UUID.fromString(key);
                    String csv = purchasedTag.getStringOr(key, "");
                    if (!csv.isEmpty()) {
                        Set<String> set = new HashSet<>();
                        for (String rewardId : csv.split(",")) {
                            if (!rewardId.isEmpty()) set.add(rewardId);
                        }
                        purchasedRewards.put(id, set);
                    }
                } catch (Exception ignored) {}
            }

            // Load active selections
            CompoundTag activeTag = root.getCompoundOrEmpty("active");
            for (String key : activeTag.keySet()) {
                try {
                    UUID id = UUID.fromString(key);
                    CompoundTag playerActive = activeTag.getCompoundOrEmpty(key);
                    Map<String, String> selections = new HashMap<>();
                    for (String category : playerActive.keySet()) {
                        String rewardId = playerActive.getStringOr(category, "");
                        if (!rewardId.isEmpty()) {
                            selections.put(category, rewardId);
                        }
                    }
                    if (!selections.isEmpty()) {
                        activeSelections.put(id, selections);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to load prestige rewards: {}", e.getMessage());
        }
    }
}
