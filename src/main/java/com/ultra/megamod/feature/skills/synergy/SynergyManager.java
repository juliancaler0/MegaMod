package com.ultra.megamod.feature.skills.synergy;

import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cross-Branch Synergy system.
 *
 * When a player specialises in specific pairs of branches (both at tier 3+),
 * they receive a synergy bonus that cannot be obtained any other way.
 */
public class SynergyManager {

    public record Synergy(String id, String displayName, String description,
                          SkillBranch branch1, SkillBranch branch2,
                          Map<String, Double> bonuses) {}

    private static final List<Synergy> ALL_SYNERGIES = List.of(
        new Synergy("bloodblade", "Bloodblade",
            "Lifesteal healing increased by 50%",
            SkillBranch.BLADE_MASTERY, SkillBranch.BERSERKER,
            Map.of("lifesteal_efficiency", 50.0)),

        new Synergy("sharpshooter", "Sharpshooter",
            "First hit on a new target always critically strikes",
            SkillBranch.RANGED_PRECISION, SkillBranch.TACTICIAN,
            Map.of("first_hit_crit", 1.0)),

        new Synergy("prospectors_rush", "Prospector's Rush",
            "Mining ores grants brief Haste I",
            SkillBranch.EFFICIENT_MINING, SkillBranch.ORE_FINDER,
            Map.of("ore_mining_haste", 1.0)),

        new Synergy("arcane_flow", "Arcane Flow",
            "Ability kills reduce other cooldowns by 2 seconds",
            SkillBranch.RELIC_LORE, SkillBranch.MANA_WEAVER,
            Map.of("kill_cooldown_reduction", 2.0)),

        new Synergy("adventurer", "Adventurer",
            "10% faster movement speed in dungeons",
            SkillBranch.EXPLORER, SkillBranch.DUNGEONEER,
            Map.of("dungeon_speed", 10.0)),

        new Synergy("undying", "Undying",
            "Heal 1 HP per second while below 30% health",
            SkillBranch.BERSERKER, SkillBranch.ENDURANCE,
            Map.of("heal_below_threshold", 1.0)),

        new Synergy("iron_fortress", "Iron Fortress",
            "+5 armor when below 50% HP",
            SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE,
            Map.of("armor_boost_low_hp", 5.0)),

        new Synergy("gourmet", "Gourmet",
            "Food-related buffs last 50% longer",
            SkillBranch.COOK, SkillBranch.BOTANIST,
            Map.of("food_buff_duration", 50.0)),

        new Synergy("arcane_swordsman", "Arcane Swordsman",
            "Melee kills have 20% chance to reduce ability cooldowns by 1s",
            SkillBranch.SPELL_BLADE, SkillBranch.BLADE_MASTERY,
            Map.of("melee_kill_cooldown_chance", 20.0)),

        new Synergy("treasure_alchemist", "Treasure Alchemist",
            "Dungeon loot has higher quality tier",
            SkillBranch.SMELTER, SkillBranch.DUNGEONEER,
            Map.of("dungeon_loot_quality", 1.0)),

        new Synergy("spirit_conductor", "Spirit Conductor",
            "Spectral Familiar deals double damage",
            SkillBranch.MANA_WEAVER, SkillBranch.SUMMONER,
            Map.of("familiar_damage_bonus", 100.0)),

        new Synergy("hawk_eye", "Hawk Eye",
            "+15% ranged damage when outdoors",
            SkillBranch.RANGED_PRECISION, SkillBranch.EXPLORER,
            Map.of("outdoor_ranged_bonus", 15.0)),

        new Synergy("natures_harmony", "Nature's Harmony",
            "Crops and animals grow 25% faster nearby",
            SkillBranch.CROP_MASTER, SkillBranch.ANIMAL_HANDLER,
            Map.of("growth_speed_bonus", 25.0)),

        new Synergy("fortunes_favor", "Fortune's Favor",
            "10% chance to double rare drops",
            SkillBranch.GEM_CUTTER, SkillBranch.FISHERMAN,
            Map.of("double_rare_drops", 10.0)),

        new Synergy("calculated_hunter", "Calculated Hunter",
            "Consecutive kills within 10s grant stacking +5% damage, max 25%",
            SkillBranch.TACTICIAN, SkillBranch.HUNTER_INSTINCT,
            Map.of("kill_streak_bonus", 5.0)),

        new Synergy("underground_express", "Underground Express",
            "+15% movement speed below Y=50",
            SkillBranch.NAVIGATOR, SkillBranch.TUNNEL_RAT,
            Map.of("underground_speed", 15.0)),

        // === NEW SYNERGIES ===

        new Synergy("lethal_combatant", "Lethal Combatant",
            "Melee critical hits restore 1 heart",
            SkillBranch.BLADE_MASTERY, SkillBranch.TACTICIAN,
            Map.of("crit_heal", 2.0)),

        new Synergy("blood_fortress", "Blood Fortress",
            "Blocking attacks heals 2% of max HP per hit blocked",
            SkillBranch.SHIELD_WALL, SkillBranch.BERSERKER,
            Map.of("block_heal_percent", 2.0)),

        new Synergy("farm_to_table", "Farm to Table",
            "Eating grants 2 extra hunger and Saturation I for 3s",
            SkillBranch.CROP_MASTER, SkillBranch.COOK,
            Map.of("crop_cook_bonus", 1.0)),

        new Synergy("runic_arsenal", "Runic Arsenal",
            "Enchanted weapons deal +15% elemental damage",
            SkillBranch.ENCHANTER, SkillBranch.SPELL_BLADE,
            Map.of("enchanted_elemental_bonus", 15.0)),

        new Synergy("mother_lode", "Mother Lode",
            "8% chance mining ore drops a bonus random gem",
            SkillBranch.ORE_FINDER, SkillBranch.GEM_CUTTER,
            Map.of("bonus_gem_chance", 8.0)),

        new Synergy("ironfoot", "Ironfoot",
            "+3 armor and +5% movement speed",
            SkillBranch.ENDURANCE, SkillBranch.NAVIGATOR,
            Map.of("armor", 3.0, "movement_speed", 5.0)),

        new Synergy("apex_explorer", "Apex Explorer",
            "+20% damage to mob types you haven't killed this session",
            SkillBranch.EXPLORER, SkillBranch.HUNTER_INSTINCT,
            Map.of("new_mob_damage", 20.0))
    );

    /**
     * Check whether a player has specialised in a branch (at least one
     * tier 3+ node unlocked).
     */
    public static boolean isBranchSpecialized(Set<String> unlockedNodes, SkillBranch branch) {
        for (String nodeId : unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node != null && node.branch() == branch && node.tier() >= 3) {
                return true;
            }
        }
        return false;
    }

    /** Return every synergy whose branch pair is fully specialised. */
    public static List<Synergy> getActiveSynergies(Set<String> unlockedNodes) {
        List<Synergy> active = new ArrayList<>();
        for (Synergy syn : ALL_SYNERGIES) {
            if (isBranchSpecialized(unlockedNodes, syn.branch1())
                    && isBranchSpecialized(unlockedNodes, syn.branch2())) {
                active.add(syn);
            }
        }
        return active;
    }

    /**
     * Aggregate all synergy bonuses for the given set of unlocked nodes.
     * Keys that appear in more than one active synergy are summed.
     */
    public static Map<String, Double> getSynergyBonuses(Set<String> unlockedNodes) {
        Map<String, Double> bonuses = new HashMap<>();
        for (Synergy syn : getActiveSynergies(unlockedNodes)) {
            for (var entry : syn.bonuses().entrySet()) {
                bonuses.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        return bonuses;
    }

    /** Return the full list of defined synergies (for UI display). */
    public static List<Synergy> getAllSynergies() {
        return ALL_SYNERGIES;
    }
}
