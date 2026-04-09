/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.skills.SkillTreeType;

public enum SkillBranch {
    BLADE_MASTERY(SkillTreeType.COMBAT, "Blade Mastery", "Increase melee damage with bladed weapons"),
    RANGED_PRECISION(SkillTreeType.COMBAT, "Ranged Precision", "Improve critical strike chance with all attacks"),
    SHIELD_WALL(SkillTreeType.COMBAT, "Shield Wall", "Enhance armor and evasion capabilities"),
    BERSERKER(SkillTreeType.COMBAT, "Berserker", "Drain life from enemies and attack faster"),
    TACTICIAN(SkillTreeType.COMBAT, "Tactician", "Amplify critical hit damage"),
    ORE_FINDER(SkillTreeType.MINING, "Ore Finder", "Gain more mining XP from excavation"),
    EFFICIENT_MINING(SkillTreeType.MINING, "Efficient Mining", "Break blocks faster"),
    GEM_CUTTER(SkillTreeType.MINING, "Gem Cutter", "Increase loot fortune from mining"),
    TUNNEL_RAT(SkillTreeType.MINING, "Tunnel Rat", "Reduce fall damage while underground"),
    SMELTER(SkillTreeType.MINING, "Smelter", "Earn more MegaCoins from ores"),
    CROP_MASTER(SkillTreeType.FARMING, "Crop Master", "Gain more farming XP from harvests"),
    ANIMAL_HANDLER(SkillTreeType.FARMING, "Rancher", "Bonus XP and improved animal breeding"),
    BOTANIST(SkillTreeType.FARMING, "Herbalist", "Improve hunger efficiency and foraging"),
    COOK(SkillTreeType.FARMING, "Cook", "Regenerate health passively over time"),
    FISHERMAN(SkillTreeType.FARMING, "Master Fisherman", "Improve loot fortune and fishing rewards"),
    RELIC_LORE(SkillTreeType.ARCANE, "Relic Lore", "Boost the power of equipped relic abilities"),
    ENCHANTER(SkillTreeType.ARCANE, "Enchanter", "Gain more arcane XP from magical activities"),
    MANA_WEAVER(SkillTreeType.ARCANE, "Mana Weaver", "Reduce cooldowns on relic abilities"),
    SPELL_BLADE(SkillTreeType.ARCANE, "Spell Blade", "Increase magic damage output"),
    SUMMONER(SkillTreeType.ARCANE, "Summoner", "Amplify overall ability power"),
    EXPLORER(SkillTreeType.SURVIVAL, "Explorer", "Gain more XP from exploration and discovery"),
    ENDURANCE(SkillTreeType.SURVIVAL, "Endurance", "Increase maximum health"),
    HUNTER_INSTINCT(SkillTreeType.SURVIVAL, "Hunter Instinct", "Gain bonus combat XP from kills"),
    NAVIGATOR(SkillTreeType.SURVIVAL, "Navigator", "Move faster on foot"),
    DUNGEONEER(SkillTreeType.SURVIVAL, "Dungeoneer", "Increase loot drops and dungeon prowess"),

    // --- Class Archetype Branches (Phase 4 Combat Overhaul) ---
    PALADIN(SkillTreeType.COMBAT, "Paladin", "Holy damage, healing, and divine shields"),
    WARRIOR(SkillTreeType.COMBAT, "Warrior", "Berserker melee DPS, war cries, and charges"),
    WIZARD(SkillTreeType.ARCANE, "Wizard", "Spell casting, elemental magic, and arcane mastery"),
    ROGUE(SkillTreeType.SURVIVAL, "Rogue", "Evasion, stealth, speed, and dirty tricks"),
    RANGER(SkillTreeType.SURVIVAL, "Ranger", "Ranged damage, traps, and wilderness expertise");

    private final SkillTreeType treeType;
    private final String displayName;
    private final String description;

    private SkillBranch(SkillTreeType treeType, String displayName, String description) {
        this.treeType = treeType;
        this.displayName = displayName;
        this.description = description;
    }

    public SkillTreeType getTreeType() {
        return this.treeType;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }
}

