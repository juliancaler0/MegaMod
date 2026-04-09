package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillTreeDefinitions {
    private static List<SkillNode> ALL_NODES;
    private static Map<String, SkillNode> NODE_BY_ID;
    private static Map<SkillTreeType, List<SkillNode>> NODES_BY_TREE;

    private SkillTreeDefinitions() {
    }

    public static List<SkillNode> getAllNodes() {
        if (ALL_NODES == null) {
            ALL_NODES = SkillTreeDefinitions.buildAllNodes();
            buildCaches();
        }
        return ALL_NODES;
    }

    private static void buildCaches() {
        NODE_BY_ID = new HashMap<>(ALL_NODES.size());
        NODES_BY_TREE = new EnumMap<>(SkillTreeType.class);
        for (SkillTreeType type : SkillTreeType.values()) {
            NODES_BY_TREE.put(type, new ArrayList<>());
        }
        for (SkillNode node : ALL_NODES) {
            NODE_BY_ID.put(node.id(), node);
            NODES_BY_TREE.get(node.branch().getTreeType()).add(node);
        }
        for (SkillTreeType type : SkillTreeType.values()) {
            NODES_BY_TREE.put(type, Collections.unmodifiableList(NODES_BY_TREE.get(type)));
        }
    }

    public static List<SkillNode> getNodesForTree(SkillTreeType type) {
        getAllNodes(); // ensure caches built
        List<SkillNode> cached = NODES_BY_TREE.get(type);
        return cached != null ? cached : Collections.emptyList();
    }

    public static SkillNode getNodeById(String id) {
        getAllNodes(); // ensure caches built
        return NODE_BY_ID.get(id);
    }

    private static List<SkillNode> buildAllNodes() {
        ArrayList<SkillNode> nodes = new ArrayList<SkillNode>(130);

        // ===================================================================
        // COMBAT TREE
        // ===================================================================

        // --- Blade Mastery (total: +10.5 attack_damage, +6 armor_shred from T3) ---
        nodes.add(new SkillNode("blade_mastery_1", SkillBranch.BLADE_MASTERY, 1, 1,
            "Sharpened Edge",
            "Increase attack damage by 1.5.",
            List.of(), Map.of("attack_damage", 1.5)));
        nodes.add(new SkillNode("blade_mastery_2", SkillBranch.BLADE_MASTERY, 2, 2,
            "Honed Blade",
            "Increase attack damage by 2. Bonus: 15% chance attacks don't consume sword durability.",
            List.of("blade_mastery_1"), Map.of("attack_damage", 2.0)));
        nodes.add(new SkillNode("blade_mastery_3", SkillBranch.BLADE_MASTERY, 3, 3,
            "Master Swordsman",
            "Increase attack damage by 3 and armor shred by 3. Bonus: 30% chance attacks don't consume sword durability.",
            List.of("blade_mastery_2"), Map.of("attack_damage", 3.0, "armor_shred", 3.0)));
        nodes.add(new SkillNode("blade_mastery_4", SkillBranch.BLADE_MASTERY, 4, 5,
            "Legendary Blade",
            "Increase attack damage by 4 and armor shred by 3. Capstone: Executioner \u2014 Instantly kill non-boss mobs below 15% HP.",
            List.of("blade_mastery_3"), Map.of("attack_damage", 4.0, "armor_shred", 3.0)));

        // --- Ranged Precision (total: +25% critical_chance, +8% stun_chance from T3) ---
        nodes.add(new SkillNode("ranged_precision_1", SkillBranch.RANGED_PRECISION, 1, 1,
            "Steady Aim",
            "Increase critical chance by 3%.",
            List.of(), Map.of("critical_chance", 3.0)));
        nodes.add(new SkillNode("ranged_precision_2", SkillBranch.RANGED_PRECISION, 2, 2,
            "Eagle Eye",
            "Increase critical chance by 5%. Bonus: Speed I after firing a bow for repositioning.",
            List.of("ranged_precision_1"), Map.of("critical_chance", 5.0)));
        nodes.add(new SkillNode("ranged_precision_3", SkillBranch.RANGED_PRECISION, 3, 3,
            "Sniper Focus",
            "Increase critical chance by 7% and stun chance by 4%. Bonus: Longer Speed boost after firing a bow.",
            List.of("ranged_precision_2"), Map.of("critical_chance", 7.0, "stun_chance", 4.0)));
        nodes.add(new SkillNode("ranged_precision_4", SkillBranch.RANGED_PRECISION, 4, 5,
            "Perfect Shot",
            "Increase critical chance by 10% and stun chance by 4%. Capstone: Deadeye \u2014 +50% projectile damage while sneaking.",
            List.of("ranged_precision_3"), Map.of("critical_chance", 10.0, "stun_chance", 4.0)));

        // --- Shield Wall (total: +17 armor, +5 thorns_damage from T3) ---
        nodes.add(new SkillNode("shield_wall_1", SkillBranch.SHIELD_WALL, 1, 1,
            "Reinforced Guard",
            "Increase armor by 2.",
            List.of(), Map.of("armor", 2.0)));
        nodes.add(new SkillNode("shield_wall_2", SkillBranch.SHIELD_WALL, 2, 2,
            "Iron Bulwark",
            "Increase armor by 3. Bonus: Blocking grants Strength I for 1s. 20% chance shield doesn't lose durability.",
            List.of("shield_wall_1"), Map.of("armor", 3.0)));
        nodes.add(new SkillNode("shield_wall_3", SkillBranch.SHIELD_WALL, 3, 3,
            "Fortress Stance",
            "Increase armor by 4 and thorns damage by 2. Bonus: 40% chance shield doesn't lose durability.",
            List.of("shield_wall_2"), Map.of("armor", 4.0, "thorns_damage", 2.0)));
        nodes.add(new SkillNode("shield_wall_4", SkillBranch.SHIELD_WALL, 4, 5,
            "Unbreakable Wall",
            "Increase armor by 8 and thorns damage by 3. Capstone: Fortress \u2014 Reflect 30% of blocked damage and apply Slowness to attacker.",
            List.of("shield_wall_3"), Map.of("armor", 8.0, "thorns_damage", 3.0)));

        // --- Berserker (total: +23% lifesteal, +25% combo_speed, +5 attack_damage from T4) ---
        // combo_speed is core to Berserker from T2 (original); attack_damage is the new secondary from T4
        nodes.add(new SkillNode("berserker_1", SkillBranch.BERSERKER, 1, 1,
            "Blood Thirst",
            "Gain 2% lifesteal on attacks.",
            List.of(), Map.of("lifesteal", 2.0)));
        nodes.add(new SkillNode("berserker_2", SkillBranch.BERSERKER, 2, 2,
            "Frenzy",
            "Gain 3% lifesteal and 3% attack speed. Bonus: Strength I when below 50% HP.",
            List.of("berserker_1"), Map.of("lifesteal", 3.0, "combo_speed", 3.0)));
        nodes.add(new SkillNode("berserker_3", SkillBranch.BERSERKER, 3, 3,
            "Bloodlust",
            "Gain 5% lifesteal and 5% attack speed. Bonus: Strength II when below 50% HP.",
            List.of("berserker_2"), Map.of("lifesteal", 5.0, "combo_speed", 5.0)));
        nodes.add(new SkillNode("berserker_4", SkillBranch.BERSERKER, 4, 5,
            "Undying Rage",
            "Gain 5% lifesteal, 7% attack speed, and 2 attack damage. Capstone: Undying Rage \u2014 Cheat death once per 5 min, gaining Resistance + Strength for 3s.",
            List.of("berserker_3"), Map.of("lifesteal", 5.0, "combo_speed", 7.0, "attack_damage", 2.0)));

        // --- Tactician (total: +75% critical_damage, +9 attack_damage from T3) ---
        nodes.add(new SkillNode("tactician_1", SkillBranch.TACTICIAN, 1, 1,
            "Exploit Weakness",
            "Increase critical damage by 5%.",
            List.of(), Map.of("critical_damage", 5.0)));
        nodes.add(new SkillNode("tactician_2", SkillBranch.TACTICIAN, 2, 2,
            "Calculated Strike",
            "Increase critical damage by 10%. Bonus: 20% chance crits don't consume weapon durability.",
            List.of("tactician_1"), Map.of("critical_damage", 10.0)));
        nodes.add(new SkillNode("tactician_3", SkillBranch.TACTICIAN, 3, 3,
            "Precision Blow",
            "Increase critical damage by 15% and attack damage by 3. Bonus: 40% chance crits don't consume weapon durability.",
            List.of("tactician_2"), Map.of("critical_damage", 15.0, "attack_damage", 3.0)));
        nodes.add(new SkillNode("tactician_4", SkillBranch.TACTICIAN, 4, 5,
            "Lethal Strategist",
            "Increase critical damage by 20% and attack damage by 3. Capstone: Exploit Weakness \u2014 Every 3rd consecutive hit on the same target within 10s deals double damage.",
            List.of("tactician_3"), Map.of("critical_damage", 20.0, "attack_damage", 3.0)));

        // ===================================================================
        // MINING TREE
        // ===================================================================

        // --- Ore Finder (total: +50% mining_xp_bonus, +8 vein_sense from T3) ---
        nodes.add(new SkillNode("ore_finder_1", SkillBranch.ORE_FINDER, 1, 1,
            "Prospector",
            "Increase mining XP gained by 5%.",
            List.of(), Map.of("mining_xp_bonus", 5.0)));
        nodes.add(new SkillNode("ore_finder_2", SkillBranch.ORE_FINDER, 2, 2,
            "Vein Seeker",
            "Increase mining XP gained by 10%. Bonus: Mining ore produces shimmer particles near hidden ores.",
            List.of("ore_finder_1"), Map.of("mining_xp_bonus", 10.0)));
        nodes.add(new SkillNode("ore_finder_3", SkillBranch.ORE_FINDER, 3, 3,
            "Ore Whisperer",
            "Increase mining XP gained by 15% and vein sense by 4. Bonus: Magnet effect pulls items while underground.",
            List.of("ore_finder_2"), Map.of("mining_xp_bonus", 15.0, "vein_sense", 4.0)));
        nodes.add(new SkillNode("ore_finder_4", SkillBranch.ORE_FINDER, 4, 5,
            "Master Prospector",
            "Increase mining XP gained by 20% and vein sense by 4. Capstone: Vein Pulse \u2014 15% chance on ore break to detect nearby ores, grant Haste, and bonus mining XP.",
            List.of("ore_finder_3"), Map.of("mining_xp_bonus", 20.0, "vein_sense", 4.0)));

        // --- Efficient Mining (total: +60% mining_speed_bonus, +1.0 excavation_reach from T3) ---
        nodes.add(new SkillNode("efficient_mining_1", SkillBranch.EFFICIENT_MINING, 1, 1,
            "Quick Pickaxe",
            "Increase mining speed by 8%.",
            List.of(), Map.of("mining_speed_bonus", 8.0)));
        nodes.add(new SkillNode("efficient_mining_2", SkillBranch.EFFICIENT_MINING, 2, 2,
            "Power Strikes",
            "Increase mining speed by 12%. Bonus: 15% chance mining doesn't consume pickaxe durability.",
            List.of("efficient_mining_1"), Map.of("mining_speed_bonus", 12.0)));
        nodes.add(new SkillNode("efficient_mining_3", SkillBranch.EFFICIENT_MINING, 3, 3,
            "Rapid Excavation",
            "Increase mining speed by 18% and excavation reach by 0.5. Bonus: 30% chance mining doesn't consume pickaxe durability.",
            List.of("efficient_mining_2"), Map.of("mining_speed_bonus", 18.0, "excavation_reach", 0.5)));
        nodes.add(new SkillNode("efficient_mining_4", SkillBranch.EFFICIENT_MINING, 4, 5,
            "Tunneling Machine",
            "Increase mining speed by 22% and excavation reach by 0.5. Capstone: Shatter Strike \u2014 10% chance on ore break to vein-mine up to 8 connected ores.",
            List.of("efficient_mining_3"), Map.of("mining_speed_bonus", 22.0, "excavation_reach", 0.5)));

        // --- Gem Cutter (total: +25% loot_fortune, +10% brilliance from T3) ---
        nodes.add(new SkillNode("gem_cutter_1", SkillBranch.GEM_CUTTER, 1, 1,
            "Careful Extraction",
            "Increase loot fortune by 3%.",
            List.of(), Map.of("loot_fortune", 3.0)));
        nodes.add(new SkillNode("gem_cutter_2", SkillBranch.GEM_CUTTER, 2, 2,
            "Precise Cuts",
            "Increase loot fortune by 5%. Bonus: Gem ores chime and sparkle when mined.",
            List.of("gem_cutter_1"), Map.of("loot_fortune", 5.0)));
        nodes.add(new SkillNode("gem_cutter_3", SkillBranch.GEM_CUTTER, 3, 3,
            "Expert Lapidary",
            "Increase loot fortune by 7% and brilliance by 5%. Bonus: 5% chance any ore drops double items.",
            List.of("gem_cutter_2"), Map.of("loot_fortune", 7.0, "brilliance", 5.0)));
        nodes.add(new SkillNode("gem_cutter_4", SkillBranch.GEM_CUTTER, 4, 5,
            "Master Gem Cutter",
            "Increase loot fortune by 10% and brilliance by 5%. Capstone: Perfect Cut \u2014 5% chance for an extra diamond or emerald drop.",
            List.of("gem_cutter_3"), Map.of("loot_fortune", 10.0, "brilliance", 5.0)));

        // --- Tunnel Rat (total: +60% fall_damage_reduction, +1.0 jump_height_bonus from T3) ---
        nodes.add(new SkillNode("tunnel_rat_1", SkillBranch.TUNNEL_RAT, 1, 1,
            "Soft Landing",
            "Reduce fall damage by 10%.",
            List.of(), Map.of("fall_damage_reduction", 10.0)));
        nodes.add(new SkillNode("tunnel_rat_2", SkillBranch.TUNNEL_RAT, 2, 2,
            "Nimble Descent",
            "Reduce fall damage by 15%. Bonus: No knockback while sneaking. Faster climbing.",
            List.of("tunnel_rat_1"), Map.of("fall_damage_reduction", 15.0)));
        nodes.add(new SkillNode("tunnel_rat_3", SkillBranch.TUNNEL_RAT, 3, 3,
            "Controlled Fall",
            "Reduce fall damage by 15% and increase jump height by 0.5. Bonus: Night Vision underground. Haste I when mining in darkness.",
            List.of("tunnel_rat_2"), Map.of("fall_damage_reduction", 15.0, "jump_height_bonus", 0.5)));
        nodes.add(new SkillNode("tunnel_rat_4", SkillBranch.TUNNEL_RAT, 4, 5,
            "Feather Feet",
            "Reduce fall damage by 20% and increase jump height by 0.5. Capstone: Earthen Shield \u2014 Immune to fall damage; big falls create a damaging shockwave.",
            List.of("tunnel_rat_3"), Map.of("fall_damage_reduction", 20.0, "jump_height_bonus", 0.5)));

        // --- Smelter (total: +50% megacoin_bonus, +16% ice_resistance_bonus from T3) ---
        nodes.add(new SkillNode("smelter_1", SkillBranch.SMELTER, 1, 1,
            "Ore Appraiser",
            "Increase MegaCoin bonus from ores by 5%.",
            List.of(), Map.of("megacoin_bonus", 5.0)));
        nodes.add(new SkillNode("smelter_2", SkillBranch.SMELTER, 2, 2,
            "Refined Smelting",
            "Increase MegaCoin bonus from ores by 10%. Bonus: Nearby furnaces smelt 2x faster.",
            List.of("smelter_1"), Map.of("megacoin_bonus", 10.0)));
        nodes.add(new SkillNode("smelter_3", SkillBranch.SMELTER, 3, 3,
            "Expert Metallurgist",
            "Increase MegaCoin bonus from ores by 15% and ice resistance by 8%. Bonus: Nearby furnaces smelt 3x faster.",
            List.of("smelter_2"), Map.of("megacoin_bonus", 15.0, "ice_resistance_bonus", 8.0)));
        nodes.add(new SkillNode("smelter_4", SkillBranch.SMELTER, 4, 5,
            "Master Smelter",
            "Increase MegaCoin bonus from ores by 20% and ice resistance by 8%. Capstone: Auto-Smelt \u2014 20% chance for a bonus smelted ingot on ore break.",
            List.of("smelter_3"), Map.of("megacoin_bonus", 20.0, "ice_resistance_bonus", 8.0)));

        // ===================================================================
        // FARMING TREE
        // ===================================================================

        // --- Crop Master (total: +50% farming_xp_bonus, +16% poison_resistance_bonus from T3) ---
        nodes.add(new SkillNode("crop_master_1", SkillBranch.CROP_MASTER, 1, 1,
            "Green Thumb",
            "Increase farming XP gained by 5%.",
            List.of(), Map.of("farming_xp_bonus", 5.0)));
        nodes.add(new SkillNode("crop_master_2", SkillBranch.CROP_MASTER, 2, 2,
            "Fertile Hands",
            "Increase farming XP gained by 10%. Bonus: Auto-replant mature crops. Bone meal advances crops 1 extra stage.",
            List.of("crop_master_1"), Map.of("farming_xp_bonus", 10.0)));
        nodes.add(new SkillNode("crop_master_3", SkillBranch.CROP_MASTER, 3, 3,
            "Harvest Lord",
            "Increase farming XP gained by 15% and poison resistance by 8%. Bonus: Growth aura (3 crops/10s). Bone meal advances crops 2 extra stages.",
            List.of("crop_master_2"), Map.of("farming_xp_bonus", 15.0, "poison_resistance_bonus", 8.0)));
        nodes.add(new SkillNode("crop_master_4", SkillBranch.CROP_MASTER, 4, 5,
            "Master Agronomist",
            "Increase farming XP gained by 20% and poison resistance by 8%. Capstone: Golden Harvest \u2014 15% chance on crop break to grow a nearby crop instantly.",
            List.of("crop_master_3"), Map.of("farming_xp_bonus", 20.0, "poison_resistance_bonus", 8.0)));

        // --- Rancher / Animal Handler (total: +25% xp_bonus, +10% beast_affinity from T3) ---
        nodes.add(new SkillNode("animal_handler_1", SkillBranch.ANIMAL_HANDLER, 1, 1,
            "Gentle Touch",
            "Increase general XP by 3% and reduce animal breeding cooldown.",
            List.of(), Map.of("xp_bonus", 3.0)));
        nodes.add(new SkillNode("animal_handler_2", SkillBranch.ANIMAL_HANDLER, 2, 2,
            "Beast Whisperer",
            "Increase general XP by 5%. Bonus: Tamed animals take 25% less damage near you. Extra animal drops.",
            List.of("animal_handler_1"), Map.of("xp_bonus", 5.0)));
        nodes.add(new SkillNode("animal_handler_3", SkillBranch.ANIMAL_HANDLER, 3, 3,
            "Herding Expert",
            "Increase general XP by 7% and beast affinity by 5%. Bonus: Kills grant your tamed animals Speed II. Extra animal drops.",
            List.of("animal_handler_2"), Map.of("xp_bonus", 7.0, "beast_affinity", 5.0)));
        nodes.add(new SkillNode("animal_handler_4", SkillBranch.ANIMAL_HANDLER, 4, 5,
            "Master Rancher",
            "Increase general XP by 10% and beast affinity by 5%. Capstone: Beast Bond \u2014 Tamed animals gain Resistance; 50% chance for twins when breeding.",
            List.of("animal_handler_3"), Map.of("xp_bonus", 10.0, "beast_affinity", 5.0)));

        // --- Herbalist / Botanist (total: +30% hunger_efficiency, +6 poison_damage_bonus from T3) ---
        nodes.add(new SkillNode("botanist_1", SkillBranch.BOTANIST, 1, 1,
            "Herbal Knowledge",
            "Increase hunger efficiency by 5%.",
            List.of(), Map.of("hunger_efficiency", 5.0)));
        nodes.add(new SkillNode("botanist_2", SkillBranch.BOTANIST, 2, 2,
            "Forager",
            "Increase hunger efficiency by 7%. Bonus: 20% chance for cross-pollination seed drops on harvest.",
            List.of("botanist_1"), Map.of("hunger_efficiency", 7.0)));
        nodes.add(new SkillNode("botanist_3", SkillBranch.BOTANIST, 3, 3,
            "Plant Sage",
            "Increase hunger efficiency by 8% and poison damage by 3. Bonus: Eating food grants Regeneration I for 3s.",
            List.of("botanist_2"), Map.of("hunger_efficiency", 8.0, "poison_damage_bonus", 3.0)));
        nodes.add(new SkillNode("botanist_4", SkillBranch.BOTANIST, 4, 5,
            "Master Herbalist",
            "Increase hunger efficiency by 10% and poison damage by 3. Capstone: Nature's Bounty \u2014 +1 hunger on food eat; 10% chance for a random 30s buff.",
            List.of("botanist_3"), Map.of("hunger_efficiency", 10.0, "poison_damage_bonus", 3.0)));

        // --- Cook (total: +7.25 health_regen_bonus, +9 max_health from T3) ---
        nodes.add(new SkillNode("cook_1", SkillBranch.COOK, 1, 1,
            "Hearty Meals",
            "Increase health regen by 0.5 per 5s.",
            List.of(), Map.of("health_regen_bonus", 0.5)));
        nodes.add(new SkillNode("cook_2", SkillBranch.COOK, 2, 2,
            "Nutritious Recipes",
            "Increase health regen by 0.75 per 5s. Bonus: Eating food restores 1 extra hunger. Nearby smokers cook 2x faster.",
            List.of("cook_1"), Map.of("health_regen_bonus", 0.75)));
        nodes.add(new SkillNode("cook_3", SkillBranch.COOK, 3, 3,
            "Gourmet Chef",
            "Increase health regen by 1.0 per 5s and max health by 3. Bonus: Eating food grants Absorption I for 3s. Nearby smokers cook 3x faster.",
            List.of("cook_2"), Map.of("health_regen_bonus", 1.0, "max_health", 3.0)));
        nodes.add(new SkillNode("cook_4", SkillBranch.COOK, 4, 5,
            "Master Chef",
            "Increase health regen by 2.0 per 5s and max health by 3. Capstone: Master Chef \u2014 25% chance for an extra food item when crafting food.",
            List.of("cook_3"), Map.of("health_regen_bonus", 2.0, "max_health", 3.0)));

        // --- Master Fisherman (total: +25% loot_fortune, +16% swim_speed_bonus from T3) ---
        nodes.add(new SkillNode("fisherman_1", SkillBranch.FISHERMAN, 1, 1,
            "Patient Angler",
            "Increase loot fortune by 3%.",
            List.of(), Map.of("loot_fortune", 3.0)));
        nodes.add(new SkillNode("fisherman_2", SkillBranch.FISHERMAN, 2, 2,
            "River Expert",
            "Increase loot fortune by 5%. Bonus: Rain doubles catch rate and drops.",
            List.of("fisherman_1"), Map.of("loot_fortune", 5.0)));
        nodes.add(new SkillNode("fisherman_3", SkillBranch.FISHERMAN, 3, 3,
            "Deep Sea Fisher",
            "Increase loot fortune by 7% and swim speed by 8%. Bonus: Conduit Power while fishing in oceans. Extra fishing XP.",
            List.of("fisherman_2"), Map.of("loot_fortune", 7.0, "swim_speed_bonus", 8.0)));
        nodes.add(new SkillNode("fisherman_4", SkillBranch.FISHERMAN, 4, 5,
            "Master Fisherman",
            "Increase loot fortune by 10% and swim speed by 8%. Capstone: Treasure Sense \u2014 Bonus treasure on catches; 5% chance for a sunken treasure haul.",
            List.of("fisherman_3"), Map.of("loot_fortune", 10.0, "swim_speed_bonus", 8.0)));

        // ===================================================================
        // ARCANE TREE
        // ===================================================================

        // --- Relic Lore (total: +100% ability_power, +12% cooldown_reduction from T3) ---
        nodes.add(new SkillNode("relic_lore_1", SkillBranch.RELIC_LORE, 1, 1,
            "Ancient Texts",
            "Increase ability power by 5%.",
            List.of(), Map.of("ability_power", 5.0)));
        nodes.add(new SkillNode("relic_lore_2", SkillBranch.RELIC_LORE, 2, 2,
            "Forgotten Knowledge",
            "Increase ability power by 10%. Bonus: Killing mobs has a 15% chance to drop Arcane XP orbs.",
            List.of("relic_lore_1"), Map.of("ability_power", 10.0)));
        nodes.add(new SkillNode("relic_lore_3", SkillBranch.RELIC_LORE, 3, 3,
            "Arcane Mastery",
            "Increase ability power by 15% and cooldown reduction by 4%. Bonus: Taking damage below 30% HP grants a brief magic shield (Absorption I, 3s, 60s CD).",
            List.of("relic_lore_2"), Map.of("ability_power", 15.0, "cooldown_reduction", 4.0)));
        nodes.add(new SkillNode("relic_lore_4", SkillBranch.RELIC_LORE, 4, 5,
            "Relic Sage",
            "Increase ability power by 30% and cooldown reduction by 4%. Capstone: Arcane Resonance \u2014 +25% magic damage.",
            List.of("relic_lore_3"), Map.of("ability_power", 30.0, "cooldown_reduction", 4.0)));

        // --- Enchanter (total: +75% arcane_xp_bonus, +23% ability_power from T3) ---
        nodes.add(new SkillNode("enchanter_1", SkillBranch.ENCHANTER, 1, 1,
            "Runic Studies",
            "Increase arcane XP gained by 5%.",
            List.of(), Map.of("arcane_xp_bonus", 5.0)));
        nodes.add(new SkillNode("enchanter_2", SkillBranch.ENCHANTER, 2, 2,
            "Glyph Reader",
            "Increase arcane XP gained by 10%. Bonus: 10% XP cost reduction at enchanting tables.",
            List.of("enchanter_1"), Map.of("arcane_xp_bonus", 10.0)));
        nodes.add(new SkillNode("enchanter_3", SkillBranch.ENCHANTER, 3, 3,
            "Mystic Scholar",
            "Increase arcane XP gained by 15% and ability power by 7%. Bonus: 20% XP cost reduction + Luck I near enchanting tables.",
            List.of("enchanter_2"), Map.of("arcane_xp_bonus", 15.0, "ability_power", 7.0)));
        nodes.add(new SkillNode("enchanter_4", SkillBranch.ENCHANTER, 4, 5,
            "Grand Enchanter",
            "Increase arcane XP gained by 20% and ability power by 8%. Capstone: Enchantment Savant \u2014 +1 enchant level at enchanting tables.",
            List.of("enchanter_3"), Map.of("arcane_xp_bonus", 20.0, "ability_power", 8.0)));

        // --- Mana Weaver (total: +53% cooldown_reduction, +22% ability_power from T3) ---
        nodes.add(new SkillNode("mana_weaver_1", SkillBranch.MANA_WEAVER, 1, 1,
            "Quick Recovery",
            "Reduce ability cooldowns by 5%.",
            List.of(), Map.of("cooldown_reduction", 5.0)));
        nodes.add(new SkillNode("mana_weaver_2", SkillBranch.MANA_WEAVER, 2, 2,
            "Flowing Energy",
            "Reduce ability cooldowns by 7%. Bonus: Regen I for 3s after using a relic ability (10s CD).",
            List.of("mana_weaver_1"), Map.of("cooldown_reduction", 7.0)));
        nodes.add(new SkillNode("mana_weaver_3", SkillBranch.MANA_WEAVER, 3, 3,
            "Mana Surge",
            "Reduce ability cooldowns by 10% and increase ability power by 7%. Bonus: XP orbs restore a small amount of hunger.",
            List.of("mana_weaver_2"), Map.of("cooldown_reduction", 10.0, "ability_power", 7.0)));
        nodes.add(new SkillNode("mana_weaver_4", SkillBranch.MANA_WEAVER, 4, 5,
            "Mana Conduit",
            "Reduce ability cooldowns by 13% and increase ability power by 7%. Capstone: Mana Surge \u2014 3 magic hits in 10s grants Strength I + Speed I for 3s.",
            List.of("mana_weaver_3"), Map.of("cooldown_reduction", 13.0, "ability_power", 7.0)));

        // --- Spell Blade (total: +14 fire, +13 lightning, +13 shadow, +7 ice from T3) ---
        // All 3 elements from T1 for balanced feel; ice_damage completes the quartet at T3
        nodes.add(new SkillNode("spell_blade_1", SkillBranch.SPELL_BLADE, 1, 1,
            "Arcane Infusion",
            "Increase fire damage by 2, lightning by 1, and shadow by 1.",
            List.of(), Map.of("fire_damage_bonus", 2.0, "lightning_damage_bonus", 1.0, "shadow_damage_bonus", 1.0)));
        nodes.add(new SkillNode("spell_blade_2", SkillBranch.SPELL_BLADE, 2, 2,
            "Elemental Edge",
            "Increase fire damage by 3, lightning by 3, and shadow by 2. Bonus: Melee hits have a 10% chance to leave a burning trail (Fire Aspect I, 2s).",
            List.of("spell_blade_1"), Map.of("fire_damage_bonus", 3.0, "lightning_damage_bonus", 3.0, "shadow_damage_bonus", 2.0)));
        nodes.add(new SkillNode("spell_blade_3", SkillBranch.SPELL_BLADE, 3, 3,
            "Storm Blade",
            "Increase fire damage by 4, lightning by 4, shadow by 4, and ice damage by 3. Bonus: Weapons shimmer with elemental particles while held.",
            List.of("spell_blade_2"), Map.of("fire_damage_bonus", 4.0, "lightning_damage_bonus", 4.0, "shadow_damage_bonus", 4.0, "ice_damage_bonus", 3.0)));
        nodes.add(new SkillNode("spell_blade_4", SkillBranch.SPELL_BLADE, 4, 5,
            "Arcane Warrior",
            "Increase fire damage by 5, lightning by 5, shadow by 6, and ice damage by 4. Capstone: Elemental Mastery \u2014 Every 3rd melee hit cycles fire, lightning, ice, or shadow effects.",
            List.of("spell_blade_3"), Map.of("fire_damage_bonus", 5.0, "lightning_damage_bonus", 5.0, "shadow_damage_bonus", 6.0, "ice_damage_bonus", 4.0)));

        // --- Summoner (total: +55% ability_power, +30% spell_range, +12% cooldown_reduction from T3) ---
        // spell_range is core to Summoner from T3 (original); cooldown_reduction is the new secondary
        nodes.add(new SkillNode("summoner_1", SkillBranch.SUMMONER, 1, 1,
            "Minor Conjuration",
            "Increase ability power by 3%.",
            List.of(), Map.of("ability_power", 3.0)));
        nodes.add(new SkillNode("summoner_2", SkillBranch.SUMMONER, 2, 2,
            "Spirit Bond",
            "Increase ability power by 7%. Bonus: Tamed animals near you regenerate health slowly.",
            List.of("summoner_1"), Map.of("ability_power", 7.0)));
        nodes.add(new SkillNode("summoner_3", SkillBranch.SUMMONER, 3, 3,
            "Ethereal Link",
            "Increase ability power by 10%, spell range by 5%, and cooldown reduction by 4%.",
            List.of("summoner_2"), Map.of("ability_power", 10.0, "spell_range", 5.0, "cooldown_reduction", 4.0)));
        nodes.add(new SkillNode("summoner_4", SkillBranch.SUMMONER, 4, 5,
            "Grand Summoner",
            "Increase ability power by 15%, spell range by 10%, and cooldown reduction by 4%. Capstone: Spectral Familiar \u2014 Auto-attacks nearest mob for 6 damage every 2s while in combat. Spell range extends familiar reach.",
            List.of("summoner_3"), Map.of("ability_power", 15.0, "spell_range", 10.0, "cooldown_reduction", 4.0)));

        // ===================================================================
        // SURVIVAL TREE
        // ===================================================================

        // --- Explorer (total: +50% survival_xp_bonus, +8% movement_speed, +16% lightning_resistance from T3) ---
        // movement_speed is core to Explorer from T1; lightning_resistance is the new secondary
        nodes.add(new SkillNode("explorer_1", SkillBranch.EXPLORER, 1, 1,
            "Wanderer",
            "Increase survival XP gained by 5% and movement speed by 1%.",
            List.of(), Map.of("survival_xp_bonus", 5.0, "movement_speed", 1.0)));
        nodes.add(new SkillNode("explorer_2", SkillBranch.EXPLORER, 2, 2,
            "Pathfinder",
            "Increase survival XP gained by 10% and movement speed by 2%. Bonus: Reduced hunger while sprinting. +0.5 block reach.",
            List.of("explorer_1"), Map.of("survival_xp_bonus", 10.0, "movement_speed", 2.0)));
        nodes.add(new SkillNode("explorer_3", SkillBranch.EXPLORER, 3, 3,
            "Trailblazer",
            "Increase survival XP gained by 15%, movement speed by 2%, and lightning resistance by 8%. Bonus: +1 block reach. Unlocks all backpack variants, upgrades, and tier crafting.",
            List.of("explorer_2"), Map.of("survival_xp_bonus", 15.0, "movement_speed", 2.0, "lightning_resistance_bonus", 8.0)));
        nodes.add(new SkillNode("explorer_4", SkillBranch.EXPLORER, 4, 5,
            "Master Explorer",
            "Increase survival XP gained by 20%, movement speed by 3%, and lightning resistance by 8%. Capstone: Cartographer \u2014 Gain Speed I for 10s when entering a new biome.",
            List.of("explorer_3"), Map.of("survival_xp_bonus", 20.0, "movement_speed", 3.0, "lightning_resistance_bonus", 8.0)));

        // --- Endurance (total: +14 max_health, +16% shadow_resistance from T3) ---
        nodes.add(new SkillNode("endurance_1", SkillBranch.ENDURANCE, 1, 1,
            "Tough Skin",
            "Increase max health by 2.",
            List.of(), Map.of("max_health", 2.0)));
        nodes.add(new SkillNode("endurance_2", SkillBranch.ENDURANCE, 2, 2,
            "Iron Constitution",
            "Increase max health by 3. Bonus: 15% slower hunger depletion.",
            List.of("endurance_1"), Map.of("max_health", 3.0)));
        nodes.add(new SkillNode("endurance_3", SkillBranch.ENDURANCE, 3, 3,
            "Unyielding Body",
            "Increase max health by 4 and shadow resistance by 8%. Bonus: 30% slower hunger depletion.",
            List.of("endurance_2"), Map.of("max_health", 4.0, "shadow_resistance_bonus", 8.0)));
        nodes.add(new SkillNode("endurance_4", SkillBranch.ENDURANCE, 4, 5,
            "Immortal Vigor",
            "Increase max health by 5 and shadow resistance by 8%. Capstone: Second Wind \u2014 Below 20% HP, gain Regen II + Resistance I (5 min cooldown).",
            List.of("endurance_3"), Map.of("max_health", 5.0, "shadow_resistance_bonus", 8.0)));

        // --- Hunter Instinct (total: +35% combat_xp_bonus, +8 prey_sense from T3) ---
        nodes.add(new SkillNode("hunter_instinct_1", SkillBranch.HUNTER_INSTINCT, 1, 1,
            "Predator Sense",
            "Increase combat XP gained by 5%.",
            List.of(), Map.of("combat_xp_bonus", 5.0)));
        nodes.add(new SkillNode("hunter_instinct_2", SkillBranch.HUNTER_INSTINCT, 2, 2,
            "Keen Tracker",
            "Increase combat XP gained by 7%. Bonus: Nearby hostile mobs glow briefly every 30s.",
            List.of("hunter_instinct_1"), Map.of("combat_xp_bonus", 7.0)));
        nodes.add(new SkillNode("hunter_instinct_3", SkillBranch.HUNTER_INSTINCT, 3, 3,
            "Apex Predator",
            "Increase combat XP gained by 10% and prey sense by 4. Bonus: Killing a mob grants Speed I for 2s.",
            List.of("hunter_instinct_2"), Map.of("combat_xp_bonus", 10.0, "prey_sense", 4.0)));
        nodes.add(new SkillNode("hunter_instinct_4", SkillBranch.HUNTER_INSTINCT, 4, 5,
            "Master Hunter",
            "Increase combat XP gained by 13% and prey sense by 4. Capstone: Predator's Mark \u2014 Killing a mob marks the nearest same-type mob with Glowing; deal +30% damage to marked targets.",
            List.of("hunter_instinct_3"), Map.of("combat_xp_bonus", 13.0, "prey_sense", 4.0)));

        // --- Navigator (total: +23% movement_speed, +15% dodge_chance from T3) ---
        nodes.add(new SkillNode("navigator_1", SkillBranch.NAVIGATOR, 1, 1,
            "Swift Stride",
            "Increase movement speed by 2%.",
            List.of(), Map.of("movement_speed", 2.0)));
        nodes.add(new SkillNode("navigator_2", SkillBranch.NAVIGATOR, 2, 2,
            "Fleet Footed",
            "Increase movement speed by 3%. Bonus: Dolphin's Grace in water. No speed penalty on soul sand. 15% faster boats.",
            List.of("navigator_1"), Map.of("movement_speed", 3.0)));
        nodes.add(new SkillNode("navigator_3", SkillBranch.NAVIGATOR, 3, 3,
            "Wind Runner",
            "Increase movement speed by 5% and dodge chance by 5%. Bonus: 30% faster boats. Sprinting 5s+ grants Jump Boost I. Unlocks all backpack variants, upgrades, and tier crafting.",
            List.of("navigator_2"), Map.of("movement_speed", 5.0, "dodge_chance", 5.0)));
        nodes.add(new SkillNode("navigator_4", SkillBranch.NAVIGATOR, 4, 5,
            "Master Navigator",
            "Increase movement speed by 5% and dodge chance by 5%. Capstone: Windwalker \u2014 Reduced sprint exhaustion; Speed II burst every 30s of sprinting.",
            List.of("navigator_3"), Map.of("movement_speed", 5.0, "dodge_chance", 5.0)));

        // --- Dungeoneer (total: +30% loot_fortune, +35% sell_bonus, +11 armor from T3) ---
        // sell_bonus is core to Dungeoneer from T3 (original); armor is the new secondary
        nodes.add(new SkillNode("dungeoneer_1", SkillBranch.DUNGEONEER, 1, 1,
            "Scavenger",
            "Increase loot fortune by 3%.",
            List.of(), Map.of("loot_fortune", 3.0)));
        nodes.add(new SkillNode("dungeoneer_2", SkillBranch.DUNGEONEER, 2, 2,
            "Delver",
            "Increase loot fortune by 5%. Bonus: +25% extra drops from dungeon mobs.",
            List.of("dungeoneer_1"), Map.of("loot_fortune", 5.0)));
        nodes.add(new SkillNode("dungeoneer_3", SkillBranch.DUNGEONEER, 3, 3,
            "Treasure Seeker",
            "Increase loot fortune by 5%, sell bonus by 8%, and armor by 3. Bonus: +50% extra drops from dungeon mobs.",
            List.of("dungeoneer_2"), Map.of("loot_fortune", 5.0, "sell_bonus", 8.0, "armor", 3.0)));
        nodes.add(new SkillNode("dungeoneer_4", SkillBranch.DUNGEONEER, 4, 5,
            "Master Dungeoneer",
            "Increase loot fortune by 7%, sell bonus by 12%, and armor by 4. Capstone: Delver's Fortune \u2014 1-3 bonus loot items on dungeon boss kills.",
            List.of("dungeoneer_3"), Map.of("loot_fortune", 7.0, "sell_bonus", 12.0, "armor", 4.0)));

        // ===================================================================
        // TIER 5 ULTIMATE NODES
        // Cost 7 pts each. Full branch (T1-T5) = 18 pts. Enhanced capstone.
        // ===================================================================

        // Combat T5
        nodes.add(new SkillNode("blade_mastery_5", SkillBranch.BLADE_MASTERY, 5, 7,
            "Godslayer",
            "Increase attack damage by 6 and armor shred by 4. Enhanced Capstone: Executioner now executes below 20% HP.",
            List.of("blade_mastery_4"), Map.of("attack_damage", 6.0, "armor_shred", 4.0)));
        nodes.add(new SkillNode("ranged_precision_5", SkillBranch.RANGED_PRECISION, 5, 7,
            "Marksman's Eye",
            "Increase critical chance by 15% and stun chance by 5%. Enhanced Capstone: Deadeye now deals +75% projectile damage.",
            List.of("ranged_precision_4"), Map.of("critical_chance", 15.0, "stun_chance", 5.0)));
        nodes.add(new SkillNode("shield_wall_5", SkillBranch.SHIELD_WALL, 5, 7,
            "Aegis",
            "Increase armor by 10 and thorns damage by 4. Enhanced Capstone: Fortress reflects 50% and applies Weakness.",
            List.of("shield_wall_4"), Map.of("armor", 10.0, "thorns_damage", 4.0)));
        nodes.add(new SkillNode("berserker_5", SkillBranch.BERSERKER, 5, 7,
            "Warlord",
            "Gain 8% lifesteal, 10% attack speed, and 3 attack damage. Enhanced Capstone: Undying Rage cooldown reduced to 3 min.",
            List.of("berserker_4"), Map.of("lifesteal", 8.0, "combo_speed", 10.0, "attack_damage", 3.0)));
        nodes.add(new SkillNode("tactician_5", SkillBranch.TACTICIAN, 5, 7,
            "Grandmaster",
            "Increase critical damage by 25% and attack damage by 3. Enhanced Capstone: Exploit Weakness triggers every 2nd hit.",
            List.of("tactician_4"), Map.of("critical_damage", 25.0, "attack_damage", 3.0)));

        // Mining T5
        nodes.add(new SkillNode("ore_finder_5", SkillBranch.ORE_FINDER, 5, 7,
            "Deep Sight",
            "Increase mining XP gained by 25% and vein sense by 6. Enhanced Capstone: Vein Pulse always triggers with 30s cooldown.",
            List.of("ore_finder_4"), Map.of("mining_xp_bonus", 25.0, "vein_sense", 6.0)));
        nodes.add(new SkillNode("efficient_mining_5", SkillBranch.EFFICIENT_MINING, 5, 7,
            "Earthshaker",
            "Increase mining speed by 30% and excavation reach by 0.8. Enhanced Capstone: Shatter Strike 25% chance, up to 16 blocks.",
            List.of("efficient_mining_4"), Map.of("mining_speed_bonus", 30.0, "excavation_reach", 0.8)));
        nodes.add(new SkillNode("gem_cutter_5", SkillBranch.GEM_CUTTER, 5, 7,
            "Diamond Soul",
            "Increase loot fortune by 15% and brilliance by 7%. Enhanced Capstone: Perfect Cut 15% chance, works on all precious ores.",
            List.of("gem_cutter_4"), Map.of("loot_fortune", 15.0, "brilliance", 7.0)));
        nodes.add(new SkillNode("tunnel_rat_5", SkillBranch.TUNNEL_RAT, 5, 7,
            "Mole King",
            "Reduce fall damage by 25% and increase jump height by 0.8. Enhanced Capstone: Earthen Shield shockwave radius and damage doubled.",
            List.of("tunnel_rat_4"), Map.of("fall_damage_reduction", 25.0, "jump_height_bonus", 0.8)));
        nodes.add(new SkillNode("smelter_5", SkillBranch.SMELTER, 5, 7,
            "Forge Master",
            "Increase MegaCoin bonus from ores by 25% and ice resistance by 10%. Enhanced Capstone: Auto-Smelt 40% chance.",
            List.of("smelter_4"), Map.of("megacoin_bonus", 25.0, "ice_resistance_bonus", 10.0)));

        // Farming T5
        nodes.add(new SkillNode("crop_master_5", SkillBranch.CROP_MASTER, 5, 7,
            "Harvest Deity",
            "Increase farming XP gained by 25% and poison resistance by 10%. Enhanced Capstone: Golden Harvest 30% chance, grows 2 nearby crops.",
            List.of("crop_master_4"), Map.of("farming_xp_bonus", 25.0, "poison_resistance_bonus", 10.0)));
        nodes.add(new SkillNode("animal_handler_5", SkillBranch.ANIMAL_HANDLER, 5, 7,
            "Beastlord",
            "Increase general XP by 15% and beast affinity by 7%. Enhanced Capstone: Beast Bond 75% twin chance, tamed animals also get Strength.",
            List.of("animal_handler_4"), Map.of("xp_bonus", 15.0, "beast_affinity", 7.0)));
        nodes.add(new SkillNode("botanist_5", SkillBranch.BOTANIST, 5, 7,
            "Life Sage",
            "Increase hunger efficiency by 15% and poison damage by 4. Enhanced Capstone: Nature's Bounty 25% buff chance, buffs last 60s.",
            List.of("botanist_4"), Map.of("hunger_efficiency", 15.0, "poison_damage_bonus", 4.0)));
        nodes.add(new SkillNode("cook_5", SkillBranch.COOK, 5, 7,
            "Grand Chef",
            "Increase health regen by 3.0 per 5s and max health by 3. Enhanced Capstone: Master Chef 50% chance for extra food.",
            List.of("cook_4"), Map.of("health_regen_bonus", 3.0, "max_health", 3.0)));
        nodes.add(new SkillNode("fisherman_5", SkillBranch.FISHERMAN, 5, 7,
            "Sea Legend",
            "Increase loot fortune by 15% and swim speed by 10%. Enhanced Capstone: Treasure Sense 15% sunken treasure chance.",
            List.of("fisherman_4"), Map.of("loot_fortune", 15.0, "swim_speed_bonus", 10.0)));

        // Arcane T5
        nodes.add(new SkillNode("relic_lore_5", SkillBranch.RELIC_LORE, 5, 7,
            "Archmage",
            "Increase ability power by 40% and cooldown reduction by 4%. Enhanced Capstone: Arcane Resonance now grants +40% magic damage.",
            List.of("relic_lore_4"), Map.of("ability_power", 40.0, "cooldown_reduction", 4.0)));
        nodes.add(new SkillNode("enchanter_5", SkillBranch.ENCHANTER, 5, 7,
            "Runeforger",
            "Increase arcane XP gained by 25% and ability power by 8%. Enhanced Capstone: Enchantment Savant grants +2 enchant levels.",
            List.of("enchanter_4"), Map.of("arcane_xp_bonus", 25.0, "ability_power", 8.0)));
        nodes.add(new SkillNode("mana_weaver_5", SkillBranch.MANA_WEAVER, 5, 7,
            "Infinity Weaver",
            "Reduce ability cooldowns by 18% and increase ability power by 8%. Enhanced Capstone: Mana Surge triggers after 2 hits, lasts 5s.",
            List.of("mana_weaver_4"), Map.of("cooldown_reduction", 18.0, "ability_power", 8.0)));
        nodes.add(new SkillNode("spell_blade_5", SkillBranch.SPELL_BLADE, 5, 7,
            "Elemental Lord",
            "Increase fire damage by 7, lightning by 8, shadow by 9, and ice damage by 5. Enhanced Capstone: Elemental Mastery triggers every 2nd hit.",
            List.of("spell_blade_4"), Map.of("fire_damage_bonus", 7.0, "lightning_damage_bonus", 8.0, "shadow_damage_bonus", 9.0, "ice_damage_bonus", 5.0)));
        nodes.add(new SkillNode("summoner_5", SkillBranch.SUMMONER, 5, 7,
            "Archsummoner",
            "Increase ability power by 20%, spell range by 15%, and cooldown reduction by 4%. Enhanced Capstone: Familiar deals 10 damage every 1.5s and chains to a second target for 60% damage.",
            List.of("summoner_4"), Map.of("ability_power", 20.0, "spell_range", 15.0, "cooldown_reduction", 4.0)));

        // Survival T5
        nodes.add(new SkillNode("explorer_5", SkillBranch.EXPLORER, 5, 7,
            "World Walker",
            "Increase survival XP gained by 25%, movement speed by 5%, and lightning resistance by 10%. Enhanced Capstone: Cartographer grants Speed II for 15s.",
            List.of("explorer_4"), Map.of("survival_xp_bonus", 25.0, "movement_speed", 5.0, "lightning_resistance_bonus", 10.0)));
        nodes.add(new SkillNode("endurance_5", SkillBranch.ENDURANCE, 5, 7,
            "Immortal",
            "Increase max health by 8 and shadow resistance by 10%. Enhanced Capstone: Second Wind also cleanses all negative effects.",
            List.of("endurance_4"), Map.of("max_health", 8.0, "shadow_resistance_bonus", 10.0)));
        nodes.add(new SkillNode("hunter_instinct_5", SkillBranch.HUNTER_INSTINCT, 5, 7,
            "Alpha Predator",
            "Increase combat XP gained by 18% and prey sense by 6. Enhanced Capstone: Predator's Mark deals +50% bonus damage.",
            List.of("hunter_instinct_4"), Map.of("combat_xp_bonus", 18.0, "prey_sense", 6.0)));
        nodes.add(new SkillNode("navigator_5", SkillBranch.NAVIGATOR, 5, 7,
            "Windrunner",
            "Increase movement speed by 8% and dodge chance by 5%. Enhanced Capstone: Windwalker burst every 15s instead of 30s.",
            List.of("navigator_4"), Map.of("movement_speed", 8.0, "dodge_chance", 5.0)));
        nodes.add(new SkillNode("dungeoneer_5", SkillBranch.DUNGEONEER, 5, 7,
            "Dungeon Lord",
            "Increase loot fortune by 10%, sell bonus by 15%, and armor by 4. Enhanced Capstone: Delver's Fortune drops 2-5 bonus items.",
            List.of("dungeoneer_4"), Map.of("loot_fortune", 10.0, "sell_bonus", 15.0, "armor", 4.0)));

        // ===================================================================
        // CLASS BRANCHES — Visible to players with the matching class.
        // These provide class-specific perks and gate spell tier access.
        // ===================================================================

        // --- Paladin (Combat Tree) ---
        nodes.add(new SkillNode("paladin_1", SkillBranch.PALADIN, 1, 1,
            "Holy Devotion",
            "Increase healing power by 5% and armor by 2. Unlocks Tier 1 paladin spells.",
            List.of(), Map.of("healing_power", 5.0, "armor", 2.0)));
        nodes.add(new SkillNode("paladin_2", SkillBranch.PALADIN, 2, 2,
            "Sacred Shield",
            "Divine Protection duration +3s. Increase armor by 4. Unlocks Tier 2 paladin spells.",
            List.of("paladin_1"), Map.of("armor", 4.0, "holy_damage_bonus", 5.0)));
        nodes.add(new SkillNode("paladin_3", SkillBranch.PALADIN, 3, 3,
            "Crusader's Zeal",
            "Hammer and Claymore attacks deal +15% holy bonus damage. Unlocks Tier 3 paladin spells.",
            List.of("paladin_2"), Map.of("holy_damage_bonus", 15.0, "attack_damage", 2.0)));
        nodes.add(new SkillNode("paladin_4", SkillBranch.PALADIN, 4, 5,
            "Divine Champion",
            "Capstone: Party members within 8 blocks gain +10% damage aura. Unlocks Tier 4 spells.",
            List.of("paladin_3"), Map.of("healing_power", 10.0, "ability_power", 10.0)));
        nodes.add(new SkillNode("paladin_5", SkillBranch.PALADIN, 5, 7,
            "Hand of Light",
            "Ultimate: All healing spells also cleanse negative status effects from targets.",
            List.of("paladin_4"), Map.of("healing_power", 15.0, "max_health", 4.0)));

        // --- Warrior (Combat Tree) ---
        nodes.add(new SkillNode("warrior_1", SkillBranch.WARRIOR, 1, 1,
            "Battle Hardened",
            "Increase max health by 3 and lifesteal by 2%. Unlocks Tier 1 warrior skills.",
            List.of(), Map.of("max_health", 3.0, "lifesteal", 2.0)));
        nodes.add(new SkillNode("warrior_2", SkillBranch.WARRIOR, 2, 2,
            "Raging Strikes",
            "Increase attack speed by 8% and attack damage by 2. Unlocks Tier 2 warrior skills.",
            List.of("warrior_1"), Map.of("combo_speed", 8.0, "attack_damage", 2.0)));
        nodes.add(new SkillNode("warrior_3", SkillBranch.WARRIOR, 3, 3,
            "Warlord's Fury",
            "Shout also heals 15% max HP. Increase armor shred by 5. Unlocks Tier 3 warrior skills.",
            List.of("warrior_2"), Map.of("armor_shred", 5.0, "lifesteal", 5.0)));
        nodes.add(new SkillNode("warrior_4", SkillBranch.WARRIOR, 4, 5,
            "Unstoppable Force",
            "Capstone: Charge stuns targets for 2s. +5 attack damage. Unlocks Tier 4 skills.",
            List.of("warrior_3"), Map.of("attack_damage", 5.0, "stun_chance", 10.0)));
        nodes.add(new SkillNode("warrior_5", SkillBranch.WARRIOR, 5, 7,
            "Berserker Rage",
            "Ultimate: Below 30% HP, gain +40% damage and +20% lifesteal.",
            List.of("warrior_4"), Map.of("critical_damage", 20.0, "lifesteal", 10.0)));

        // --- Wizard (Arcane Tree) ---
        nodes.add(new SkillNode("wizard_1", SkillBranch.WIZARD, 1, 1,
            "Arcane Affinity",
            "Increase spell haste by 5% and mana efficiency by 3%. Unlocks Tier 1 wizard spells.",
            List.of(), Map.of("spell_haste", 5.0, "mana_efficiency", 3.0)));
        nodes.add(new SkillNode("wizard_2", SkillBranch.WIZARD, 2, 2,
            "School Focus",
            "Increase elemental spell damage by 12%. Unlocks Tier 2 wizard spells.",
            List.of("wizard_1"), Map.of("ability_power", 12.0)));
        nodes.add(new SkillNode("wizard_3", SkillBranch.WIZARD, 3, 3,
            "Rune Mastery",
            "T2 spells have 30% chance to not consume runes. Unlocks Tier 3 wizard spells.",
            List.of("wizard_2"), Map.of("cooldown_reduction", 8.0, "mana_efficiency", 5.0)));
        nodes.add(new SkillNode("wizard_4", SkillBranch.WIZARD, 4, 5,
            "Archmage",
            "Capstone: T2 spells are rune-free, T3 spells cost 1 rune instead of 2. Unlocks T4.",
            List.of("wizard_3"), Map.of("ability_power", 15.0, "spell_haste", 8.0)));
        nodes.add(new SkillNode("wizard_5", SkillBranch.WIZARD, 5, 7,
            "Spell Weaving",
            "Ultimate: Channeled spells tick 30% faster. All spell damage +10%.",
            List.of("wizard_4"), Map.of("ability_power", 10.0, "spell_range", 3.0)));

        // --- Rogue (Survival Tree) ---
        nodes.add(new SkillNode("rogue_1", SkillBranch.ROGUE, 1, 1,
            "Shadow Arts",
            "Increase dodge chance by 5% and critical chance by 3%. Unlocks Tier 1 rogue skills.",
            List.of(), Map.of("dodge_chance", 5.0, "critical_chance", 3.0)));
        nodes.add(new SkillNode("rogue_2", SkillBranch.ROGUE, 2, 2,
            "Blade Venom",
            "Daggers and sickles apply Poison on critical hits. +5% crit chance.",
            List.of("rogue_1"), Map.of("critical_chance", 5.0, "poison_damage_bonus", 8.0)));
        nodes.add(new SkillNode("rogue_3", SkillBranch.ROGUE, 3, 3,
            "Phantom Strike",
            "Shadow Step cooldown reduced by 30%. Unlocks Tier 3 rogue skills.",
            List.of("rogue_2"), Map.of("cooldown_reduction", 10.0, "dodge_chance", 5.0)));
        nodes.add(new SkillNode("rogue_4", SkillBranch.ROGUE, 4, 5,
            "Death Mark",
            "Capstone: After using Vanish, next attack deals 3x damage. Unlocks T4 skills.",
            List.of("rogue_3"), Map.of("critical_damage", 25.0, "attack_damage", 3.0)));
        nodes.add(new SkillNode("rogue_5", SkillBranch.ROGUE, 5, 7,
            "Ghost Walk",
            "Ultimate: Stealth no longer reduces movement speed. +10% dodge, +15% crit damage.",
            List.of("rogue_4"), Map.of("dodge_chance", 10.0, "critical_damage", 15.0)));

        // --- Ranger (Survival Tree) ---
        nodes.add(new SkillNode("ranger_1", SkillBranch.RANGER, 1, 1,
            "Keen Eye",
            "Increase ranged damage by 5% and critical chance by 3%. Unlocks Tier 1 ranger skills.",
            List.of(), Map.of("ranged_damage", 5.0, "critical_chance", 3.0)));
        nodes.add(new SkillNode("ranger_2", SkillBranch.RANGER, 2, 2,
            "Quick Draw",
            "Bow draw speed increased by 15%. +5% ranged damage.",
            List.of("ranger_1"), Map.of("ranged_damage", 5.0, "combo_speed", 5.0)));
        nodes.add(new SkillNode("ranger_3", SkillBranch.RANGER, 3, 3,
            "Nature's Ally",
            "Entangling Roots duration +50%. +5% ranged damage. Unlocks Tier 3 ranger skills.",
            List.of("ranger_2"), Map.of("ranged_damage", 5.0, "ability_power", 5.0)));
        nodes.add(new SkillNode("ranger_4", SkillBranch.RANGER, 4, 5,
            "Eagle Eye",
            "Capstone: Arrows gain minor homing within 15\u00b0 cone. Unlocks T4 ranger skills.",
            List.of("ranger_3"), Map.of("ranged_damage", 10.0, "critical_chance", 5.0)));
        nodes.add(new SkillNode("ranger_5", SkillBranch.RANGER, 5, 7,
            "Volley Master",
            "Ultimate: Barrage fires 5 arrows instead of 3. +10% ranged damage.",
            List.of("ranger_4"), Map.of("ranged_damage", 10.0, "critical_damage", 10.0)));

        return nodes;
    }
}
