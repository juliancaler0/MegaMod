package com.ultra.megamod.feature.citizen;

import com.ultra.megamod.feature.skills.SkillManager;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;

/**
 * Static utility that queries the player's skill tree to compute
 * buffs applied to their owned colony citizens.
 *
 * Every method is null-safe: if the SkillManager is unavailable or
 * the owner has no data, defaults are returned.
 *
 * Node IDs reference SkillTreeDefinitions (e.g. "efficient_mining_2").
 */
public final class CitizenSkillBonuses {

    private CitizenSkillBonuses() {}

    // =====================================================================
    //  Mining Tree -> Miner Citizens
    // =====================================================================

    /**
     * Mining speed multiplier (1.0 = normal).
     * efficient_mining_2 -> 1.15 (15% faster)
     * efficient_mining_3 -> 1.30 (30% faster)
     * efficient_mining_4 -> 1.50 (capstone, 50% faster)
     * efficient_mining_5 -> 1.75 (T5, 75% faster)
     */
    public static double getMiningSpeedMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "efficient_mining_5")) return 1.75;
        if (has(level, ownerUUID, "efficient_mining_4")) return 1.50;
        if (has(level, ownerUUID, "efficient_mining_3")) return 1.30;
        if (has(level, ownerUUID, "efficient_mining_2")) return 1.15;
        return 1.0;
    }

    /**
     * Whether miner citizens should prioritize ore blocks over stone.
     * Requires ore_finder_2+ (Vein Seeker).
     */
    public static boolean shouldPrioritizeOres(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "ore_finder_2");
    }

    /**
     * Chance for vein-mining connected ores (0.0 default).
     * efficient_mining_4 (Shatter Strike capstone) -> 0.10
     * efficient_mining_5 (enhanced capstone)        -> 0.25
     */
    public static double getVeinMineChance(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "efficient_mining_5")) return 0.25;
        if (has(level, ownerUUID, "efficient_mining_4")) return 0.10;
        return 0.0;
    }

    /**
     * Fortune bonus for diamond/emerald drops (0.0 default).
     * gem_cutter_2 (Precise Cuts)    -> 0.10
     * gem_cutter_3 (Expert Lapidary) -> 0.15
     * gem_cutter_4 (Perfect Cut)     -> 0.20
     * gem_cutter_5 (Diamond Soul)    -> 0.30
     */
    public static double getOreFortune(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "gem_cutter_5")) return 0.30;
        if (has(level, ownerUUID, "gem_cutter_4")) return 0.20;
        if (has(level, ownerUUID, "gem_cutter_3")) return 0.15;
        if (has(level, ownerUUID, "gem_cutter_2")) return 0.10;
        return 0.0;
    }

    /**
     * Whether miner citizens are immune to fall damage.
     * Requires any tunnel_rat tier (tunnel_rat_1+).
     */
    public static boolean hasMinerFallImmunity(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "tunnel_rat_1");
    }

    // =====================================================================
    //  Farming Tree -> Farmer / Animal Farmer / Fisherman Citizens
    // =====================================================================

    /**
     * Farming cycle speed multiplier (1.0 = normal, lower fraction = faster).
     * crop_master_2 (Fertile Hands) -> 1.50 (50% more productive per cycle)
     * crop_master_3 (Harvest Lord)  -> 1.75
     * crop_master_4 (Master Agron.) -> 2.00
     * crop_master_5 (Harvest Deity) -> 2.50
     *
     * Values > 1.0 mean more items per harvest cycle; the AI multiplies
     * base output by this.
     */
    public static double getFarmingSpeedMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "crop_master_5")) return 2.50;
        if (has(level, ownerUUID, "crop_master_4")) return 2.00;
        if (has(level, ownerUUID, "crop_master_3")) return 1.75;
        if (has(level, ownerUUID, "crop_master_2")) return 1.50;
        return 1.0;
    }

    /**
     * Chance for growth aura tick on nearby crops (0.0 default).
     * crop_master_3 (Harvest Lord, growth aura) -> 0.15
     * crop_master_4 (Golden Harvest capstone)   -> 0.25
     * crop_master_5 (enhanced capstone)         -> 0.35
     */
    public static double getGrowthAuraChance(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "crop_master_5")) return 0.35;
        if (has(level, ownerUUID, "crop_master_4")) return 0.25;
        if (has(level, ownerUUID, "crop_master_3")) return 0.15;
        return 0.0;
    }

    /**
     * Bonus harvest items per crop break (0 default).
     * crop_master_4 (Golden Harvest capstone) -> +1
     * crop_master_5 (enhanced capstone)       -> +2
     */
    public static int getBonusHarvestCount(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "crop_master_5")) return 2;
        if (has(level, ownerUUID, "crop_master_4")) return 1;
        return 0;
    }

    /**
     * Breeding cooldown multiplier for animal farmer citizens (1.0 = normal).
     * animal_handler_2 (Beast Whisperer)          -> 0.85 (15% faster)
     * animal_handler_3 (Herding Expert)           -> 0.70 (30% faster)
     * animal_handler_4 (Master Rancher capstone)  -> 0.55 (45% faster)
     * animal_handler_5 (Beastlord)                -> 0.40 (60% faster)
     */
    public static double getBreedingTickMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "animal_handler_5")) return 0.40;
        if (has(level, ownerUUID, "animal_handler_4")) return 0.55;
        if (has(level, ownerUUID, "animal_handler_3")) return 0.70;
        if (has(level, ownerUUID, "animal_handler_2")) return 0.85;
        return 1.0;
    }

    /**
     * Twin chance on breed (0.0 default).
     * animal_handler_4 (Beast Bond capstone)     -> 0.20 (originally described as 50% for player, tuned for colony)
     * animal_handler_5 (enhanced capstone)       -> 0.35
     */
    public static double getTwinChance(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "animal_handler_5")) return 0.35;
        if (has(level, ownerUUID, "animal_handler_4")) return 0.20;
        return 0.0;
    }

    /**
     * Whether animals near the colony take reduced damage (25% less).
     * Requires animal_handler_2+ (Beast Whisperer).
     */
    public static boolean hasAnimalDamageReduction(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "animal_handler_2");
    }

    /**
     * Fisherman citizen catch timer multiplier (1.0 = normal, lower = faster).
     * fisherman_2 (River Expert)                 -> 0.85
     * fisherman_3 (Deep Sea Fisher)              -> 0.70
     * fisherman_4 (Master Treasure Hunter)       -> 0.55
     * fisherman_5 (Sea Legend)                   -> 0.40
     */
    public static double getFishingTimerMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "fisherman_5")) return 0.40;
        if (has(level, ownerUUID, "fisherman_4")) return 0.55;
        if (has(level, ownerUUID, "fisherman_3")) return 0.70;
        if (has(level, ownerUUID, "fisherman_2")) return 0.85;
        return 1.0;
    }

    /**
     * Fisherman treasure rate bonus (0.0 default).
     * fisherman_2 (River Expert)                 -> 0.10
     * fisherman_3 (Deep Sea Fisher)              -> 0.15
     * fisherman_4 (Treasure Sense capstone)      -> 0.25
     * fisherman_5 (Sea Legend enhanced)          -> 0.35
     */
    public static double getFishingTreasureBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "fisherman_5")) return 0.35;
        if (has(level, ownerUUID, "fisherman_4")) return 0.25;
        if (has(level, ownerUUID, "fisherman_3")) return 0.15;
        if (has(level, ownerUUID, "fisherman_2")) return 0.10;
        return 0.0;
    }

    // =====================================================================
    //  Combat Tree -> Recruit Citizens
    // =====================================================================

    /**
     * Melee damage bonus for recruit citizens (0 default).
     * blade_mastery_2 (Honed Blade)      -> +2
     * blade_mastery_3 (Master Swordsman) -> +4
     * blade_mastery_4 (Legendary Blade)  -> +6
     * blade_mastery_5 (Godslayer)        -> +8
     */
    public static double getMeleeDamageBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "blade_mastery_5")) return 8.0;
        if (has(level, ownerUUID, "blade_mastery_4")) return 6.0;
        if (has(level, ownerUUID, "blade_mastery_3")) return 4.0;
        if (has(level, ownerUUID, "blade_mastery_2")) return 2.0;
        return 0.0;
    }

    /**
     * Execute chance: instant kill on non-boss mobs below 15% HP (0.0 default).
     * blade_mastery_4 (Executioner capstone)  -> 0.10 (10% chance per hit)
     * blade_mastery_5 (enhanced, below 20%)   -> 0.15
     */
    public static double getExecuteChance(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "blade_mastery_5")) return 0.15;
        if (has(level, ownerUUID, "blade_mastery_4")) return 0.10;
        return 0.0;
    }

    /**
     * Ranged accuracy improvement factor (1.0 = normal, lower = tighter spread).
     * ranged_precision_2 (Eagle Eye)      -> 0.80 (20% tighter)
     * ranged_precision_3 (Sniper Focus)   -> 0.65 (35% tighter)
     * ranged_precision_4 (Perfect Shot)   -> 0.50
     * ranged_precision_5 (Marksman's Eye) -> 0.35
     */
    public static double getRangedAccuracyMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "ranged_precision_5")) return 0.35;
        if (has(level, ownerUUID, "ranged_precision_4")) return 0.50;
        if (has(level, ownerUUID, "ranged_precision_3")) return 0.65;
        if (has(level, ownerUUID, "ranged_precision_2")) return 0.80;
        return 1.0;
    }

    /**
     * Ranged damage multiplier (1.0 default).
     * ranged_precision_3 (Sniper Focus)   -> 1.25
     * ranged_precision_4 (Deadeye)        -> 1.50
     * ranged_precision_5 (enhanced)       -> 1.75
     */
    public static double getRangedDamageMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "ranged_precision_5")) return 1.75;
        if (has(level, ownerUUID, "ranged_precision_4")) return 1.50;
        if (has(level, ownerUUID, "ranged_precision_3")) return 1.25;
        return 1.0;
    }

    /**
     * Ranged critical hit chance (0.0 default).
     * ranged_precision_4 (Perfect Shot capstone)  -> 0.15
     * ranged_precision_5 (Marksman's Eye)         -> 0.25
     */
    public static double getRangedCritChance(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "ranged_precision_5")) return 0.25;
        if (has(level, ownerUUID, "ranged_precision_4")) return 0.15;
        return 0.0;
    }

    /**
     * Bonus armor for shieldman recruits (0 default).
     * shield_wall_2 (Iron Bulwark)        -> +4
     * shield_wall_3 (Fortress Stance)     -> +8
     * shield_wall_4 (Unbreakable Wall)    -> +12
     * shield_wall_5 (Aegis)               -> +16
     */
    public static double getShieldmanArmorBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "shield_wall_5")) return 16.0;
        if (has(level, ownerUUID, "shield_wall_4")) return 12.0;
        if (has(level, ownerUUID, "shield_wall_3")) return 8.0;
        if (has(level, ownerUUID, "shield_wall_2")) return 4.0;
        return 0.0;
    }

    /**
     * Formation armor bonus for ALL recruits (0 default).
     * shield_wall_4 (Fortress capstone) -> +2
     * shield_wall_5 (Aegis enhanced)    -> +4
     */
    public static double getFormationArmorBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "shield_wall_5")) return 4.0;
        if (has(level, ownerUUID, "shield_wall_4")) return 2.0;
        return 0.0;
    }

    /**
     * Lifesteal fraction (0.0 default): fraction of damage dealt healed back.
     * berserker_2 (Frenzy)         -> 0.05
     * berserker_3 (Bloodlust)      -> 0.10
     * berserker_4 (Undying Rage)   -> 0.15
     * berserker_5 (Warlord)        -> 0.20
     */
    public static double getLifestealFraction(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "berserker_5")) return 0.20;
        if (has(level, ownerUUID, "berserker_4")) return 0.15;
        if (has(level, ownerUUID, "berserker_3")) return 0.10;
        if (has(level, ownerUUID, "berserker_2")) return 0.05;
        return 0.0;
    }

    /**
     * Whether recruits get an attack speed boost when below 30% HP.
     * Requires berserker_3+ (Bloodlust).
     */
    public static boolean hasLowHpAttackSpeedBoost(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "berserker_3");
    }

    /**
     * Whether recruits can cheat death once per MC day
     * (survive a killing blow at 1 HP with brief Resistance + Strength).
     * Requires berserker_4+ (Undying Rage capstone).
     */
    public static boolean hasCheatDeath(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "berserker_4");
    }

    /**
     * Recruit XP multiplier (1.0 default).
     * tactician_2 (Calculated Strike) -> 1.50
     * tactician_3 (Precision Blow)    -> 2.00
     * tactician_4 (Lethal Strategist) -> 2.50
     * tactician_5 (Grandmaster)       -> 3.00
     */
    public static double getRecruitXpMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "tactician_5")) return 3.00;
        if (has(level, ownerUUID, "tactician_4")) return 2.50;
        if (has(level, ownerUUID, "tactician_3")) return 2.00;
        if (has(level, ownerUUID, "tactician_2")) return 1.50;
        return 1.0;
    }

    // =====================================================================
    //  Survival Tree -> All Citizens
    // =====================================================================

    /**
     * Citizen movement speed bonus (added to base speed attribute, 0.0 default).
     * explorer_2 (Pathfinder)      -> 0.05
     * explorer_3 (Trailblazer)     -> 0.10
     * explorer_4 (Master Explorer) -> 0.15
     * explorer_5 (World Walker)    -> 0.20
     */
    public static double getCitizenSpeedBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "explorer_5")) return 0.20;
        if (has(level, ownerUUID, "explorer_4")) return 0.15;
        if (has(level, ownerUUID, "explorer_3")) return 0.10;
        if (has(level, ownerUUID, "explorer_2")) return 0.05;
        return 0.0;
    }

    /**
     * Citizen max health bonus (added to base 20 HP, 0 default).
     * endurance_2 (Iron Constitution)  -> +5
     * endurance_3 (Unyielding Body)    -> +10
     * endurance_4 (Immortal Vigor)     -> +15
     * endurance_5 (Immortal)           -> +20
     */
    public static double getCitizenMaxHealthBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "endurance_5")) return 20.0;
        if (has(level, ownerUUID, "endurance_4")) return 15.0;
        if (has(level, ownerUUID, "endurance_3")) return 10.0;
        if (has(level, ownerUUID, "endurance_2")) return 5.0;
        return 0.0;
    }

    /**
     * Whether citizens auto-heal 1 HP per 60 ticks (3 seconds) while out of combat.
     * Requires endurance_4+ (Immortal Vigor, Second Wind capstone).
     */
    public static boolean hasCitizenAutoHeal(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "endurance_4");
    }

    /**
     * Damage multiplier vs hostile mobs for recruit citizens (1.0 default).
     * hunter_instinct_2 (Keen Tracker)   -> 1.15
     * hunter_instinct_3 (Apex Predator)  -> 1.25
     * hunter_instinct_4 (Master Hunter)  -> 1.40
     * hunter_instinct_5 (Alpha Predator) -> 1.50
     */
    public static double getHostileDamageMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "hunter_instinct_5")) return 1.50;
        if (has(level, ownerUUID, "hunter_instinct_4")) return 1.40;
        if (has(level, ownerUUID, "hunter_instinct_3")) return 1.25;
        if (has(level, ownerUUID, "hunter_instinct_2")) return 1.15;
        return 1.0;
    }

    /**
     * Whether citizens ignore water/soul sand slowdown.
     * Requires navigator_2+ (Fleet Footed).
     */
    public static boolean hasTerrainSpeedImmunity(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "navigator_2");
    }

    /**
     * Fisherman boat speed multiplier (1.0 default).
     * navigator_2 (Fleet Footed, 15% faster boats) -> 1.15
     * navigator_3 (Wind Runner, 30% faster boats)  -> 1.30
     * navigator_4 (Master Navigator)               -> 1.45
     * navigator_5 (Windrunner)                     -> 1.60
     */
    public static double getBoatSpeedMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "navigator_5")) return 1.60;
        if (has(level, ownerUUID, "navigator_4")) return 1.45;
        if (has(level, ownerUUID, "navigator_3")) return 1.30;
        if (has(level, ownerUUID, "navigator_2")) return 1.15;
        return 1.0;
    }

    // =====================================================================
    //  Arcane Tree -> Citizen Utility
    // =====================================================================

    /**
     * Tool durability consumption multiplier (1.0 = normal, lower = slower wear).
     * enchanter_2 (Glyph Reader)    -> 0.85 (15% slower)
     * enchanter_3 (Mystic Scholar)  -> 0.70 (30% slower)
     * enchanter_4 (Grand Enchanter) -> 0.55
     * enchanter_5 (Runeforger)      -> 0.40
     */
    public static double getToolDurabilityMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "enchanter_5")) return 0.40;
        if (has(level, ownerUUID, "enchanter_4")) return 0.55;
        if (has(level, ownerUUID, "enchanter_3")) return 0.70;
        if (has(level, ownerUUID, "enchanter_2")) return 0.85;
        return 1.0;
    }

    /**
     * Whether worker tools auto-repair (+1 durability per minute).
     * Requires enchanter_4+ (Grand Enchanter, Enchantment Savant capstone).
     */
    public static boolean hasToolAutoRepair(ServerLevel level, UUID ownerUUID) {
        return has(level, ownerUUID, "enchanter_4");
    }

    /**
     * Max citizen bonus (added to colony's base citizen cap, 0 default).
     * summoner_2 (Spirit Bond)      -> +5
     * summoner_3 (Ethereal Link)    -> +10
     * summoner_4 (Grand Summoner)   -> +15
     * summoner_5 (Archsummoner)     -> +20
     */
    public static int getMaxCitizenBonus(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "summoner_5")) return 20;
        if (has(level, ownerUUID, "summoner_4")) return 15;
        if (has(level, ownerUUID, "summoner_3")) return 10;
        if (has(level, ownerUUID, "summoner_2")) return 5;
        return 0;
    }

    /**
     * Hire cost multiplier for new citizens (1.0 = normal, lower = cheaper).
     * summoner_3 (Ethereal Link)    -> 0.90
     * summoner_4 (Grand Summoner capstone) -> 0.80
     * summoner_5 (Archsummoner)     -> 0.65
     */
    public static double getHireCostMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "summoner_5")) return 0.65;
        if (has(level, ownerUUID, "summoner_4")) return 0.80;
        if (has(level, ownerUUID, "summoner_3")) return 0.90;
        return 1.0;
    }

    /**
     * Global upkeep multiplier for all citizens (1.0 = normal, lower = cheaper).
     * mana_weaver_2 (Flowing Energy)  -> 0.95
     * mana_weaver_3 (Mana Surge)      -> 0.90
     * mana_weaver_4 (Mana Conduit)    -> 0.85
     * mana_weaver_5 (Infinity Weaver) -> 0.75
     */
    public static double getUpkeepMultiplier(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "mana_weaver_5")) return 0.75;
        if (has(level, ownerUUID, "mana_weaver_4")) return 0.85;
        if (has(level, ownerUUID, "mana_weaver_3")) return 0.90;
        if (has(level, ownerUUID, "mana_weaver_2")) return 0.95;
        return 1.0;
    }

    /**
     * Hunger depletion multiplier for citizens (1.0 = normal, lower = slower drain).
     * Combines herbalist (botanist) and cook branch nodes multiplicatively.
     *
     * botanist_3 (Plant Sage, regen on eat)       -> 0.80 (20% slower depletion)
     * botanist_4 (Master Herbalist capstone)      -> 0.70
     * botanist_5 (Life Sage)                      -> 0.60
     *
     * cook_4 (Master Chef capstone)               -> 0.50 (50% slower depletion)
     * cook_5 (Grand Chef)                         -> 0.40
     *
     * Both stacks multiplicatively: e.g. botanist_3 + cook_4 = 0.80 * 0.50 = 0.40
     */
    public static double getHungerDepletionMultiplier(ServerLevel level, UUID ownerUUID) {
        double herbalist = 1.0;
        if (has(level, ownerUUID, "botanist_5")) {
            herbalist = 0.60;
        } else if (has(level, ownerUUID, "botanist_4")) {
            herbalist = 0.70;
        } else if (has(level, ownerUUID, "botanist_3")) {
            herbalist = 0.80;
        }

        double cook = 1.0;
        if (has(level, ownerUUID, "cook_5")) {
            cook = 0.40;
        } else if (has(level, ownerUUID, "cook_4")) {
            cook = 0.50;
        }

        return herbalist * cook;
    }

    /**
     * Bonus hunger restored per food item eaten by a citizen (0 default).
     * cook_2 (Nutritious Recipes, +1 extra hunger) -> +2
     * cook_3 (Gourmet Chef)                        -> +3
     * cook_4 (Master Chef capstone)                -> +4
     * cook_5 (Grand Chef)                          -> +5
     */
    public static int getBonusFoodHunger(ServerLevel level, UUID ownerUUID) {
        if (has(level, ownerUUID, "cook_5")) return 5;
        if (has(level, ownerUUID, "cook_4")) return 4;
        if (has(level, ownerUUID, "cook_3")) return 3;
        if (has(level, ownerUUID, "cook_2")) return 2;
        return 0;
    }

    // =====================================================================
    //  Helper
    // =====================================================================

    /**
     * Null-safe check whether the colony owner has unlocked a given skill node.
     */
    private static boolean has(ServerLevel level, UUID owner, String nodeId) {
        if (level == null || owner == null) return false;
        try {
            return SkillManager.get(level).isNodeUnlocked(owner, nodeId);
        } catch (Exception e) {
            return false;
        }
    }
}
