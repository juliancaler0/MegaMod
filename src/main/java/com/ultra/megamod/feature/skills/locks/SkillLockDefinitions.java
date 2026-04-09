package com.ultra.megamod.feature.skills.locks;

import com.ultra.megamod.feature.skills.SkillBranch;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All item use-lock and enchantment generation-lock definitions.
 *
 * USE LOCK: Player can hold/obtain the item but cannot use/equip it without spec.
 * GENERATION LOCK: Enchantment won't appear on enchanting tables or loot for the player.
 *   - Exclusive: Only the listed branches can generate it (Enchanter cannot bypass).
 *   - Standard: Enchanter is a valid second path (max level only).
 */
public final class SkillLockDefinitions {

    private SkillLockDefinitions() {}

    // ==================== Records ====================

    /** Use lock: item matched by registry path pattern -> required branch A or B. */
    public record UseLock(String category, Set<String> itemPatterns, SkillBranch branchA, SkillBranch branchB) {}

    /** Enchant generation lock: enchant ID -> min locked level, branches, exclusive flag. */
    public record EnchantLock(String enchantId, int minLockedLevel, SkillBranch branchA, SkillBranch branchB, boolean exclusive) {
        /** Single-branch exclusive lock (no second path). */
        public EnchantLock(String enchantId, int minLockedLevel, SkillBranch branchA, boolean exclusive) {
            this(enchantId, minLockedLevel, branchA, null, exclusive);
        }
    }

    // ==================== Use Locks ====================

    public static final List<UseLock> USE_LOCKS = List.of(
        // --- Melee Weapons ---
        new UseLock("Swords", Set.of("diamond_sword", "netherite_sword", "unique_longsword_sw"),
                SkillBranch.BLADE_MASTERY, SkillBranch.TACTICIAN),
        new UseLock("Daggers", Set.of("unique_dagger_1", "unique_dagger_2", "unique_dagger_sw", "naga_fang_dagger", "ghost_fang"),
                SkillBranch.BLADE_MASTERY, SkillBranch.TACTICIAN),
        new UseLock("Claymores", Set.of("unique_claymore_1", "unique_claymore_2", "unique_claymore_sw"),
                SkillBranch.BLADE_MASTERY, SkillBranch.BERSERKER),
        new UseLock("Axes", Set.of("diamond_axe", "netherite_axe", "unique_double_axe_1", "unique_double_axe_2", "unique_double_axe_sw", "wrought_axe"),
                SkillBranch.BERSERKER, SkillBranch.BLADE_MASTERY),
        new UseLock("Hammers", Set.of("terra_warhammer", "unique_hammer_1", "unique_hammer_2", "unique_hammer_sw"),
                SkillBranch.BERSERKER, SkillBranch.SHIELD_WALL),
        new UseLock("Maces", Set.of("mace", "unique_mace_1", "unique_mace_2", "unique_mace_sw", "earthrend_gauntlet"),
                SkillBranch.BERSERKER, SkillBranch.SHIELD_WALL),
        new UseLock("Glaives", Set.of("unique_glaive_1", "unique_glaive_2", "unique_glaive_sw", "crescent_blade", "battledancer"),
                SkillBranch.TACTICIAN, SkillBranch.BLADE_MASTERY),
        new UseLock("Sickles", Set.of("unique_sickle_1", "unique_sickle_2", "unique_sickle_sw"),
                SkillBranch.TACTICIAN, SkillBranch.BERSERKER),

        // --- Ranged Weapons ---
        new UseLock("Longbows", Set.of("unique_longbow_1", "unique_longbow_2", "unique_longbow_sw"),
                SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Heavy Crossbows", Set.of("unique_heavy_crossbow_1", "unique_heavy_crossbow_2", "unique_heavy_crossbow_sw"),
                SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Spears", Set.of("unique_spear_1", "unique_spear_2", "unique_spear_sw", "trident"),
                SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Thrown", Set.of("blowgun", "dart", "spore_sack", "blazing_flask"),
                SkillBranch.RANGED_PRECISION, SkillBranch.EXPLORER),
        new UseLock("Tipped Arrows", Set.of("tipped_arrow", "cerulean_arrow", "crystal_arrow"),
                SkillBranch.RANGED_PRECISION, SkillBranch.BOTANIST),

        // --- Magic Weapons ---
        new UseLock("Damage Staves", Set.of("unique_staff_damage_1", "unique_staff_damage_2", "unique_staff_damage_3",
                "unique_staff_damage_4", "unique_staff_damage_5", "unique_staff_damage_6", "unique_staff_damage_sw",
                "static_seeker", "ebonchill"),
                SkillBranch.SPELL_BLADE, SkillBranch.MANA_WEAVER),
        new UseLock("Healing Staves", Set.of("unique_staff_heal_1", "unique_staff_heal_2", "unique_staff_heal_sw", "lightbinder"),
                SkillBranch.MANA_WEAVER, SkillBranch.SUMMONER),
        new UseLock("Tomes & Scepters", Set.of("vampiric_tome", "scepter_of_chaos"),
                SkillBranch.SPELL_BLADE, SkillBranch.RELIC_LORE),

        // --- Shields ---
        new UseLock("Unique Shields", Set.of("unique_shield_1", "unique_shield_2", "unique_shield_sw"),
                SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),

        // --- Armor ---
        new UseLock("Netherite Helmet", Set.of("netherite_helmet"),
                SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),
        new UseLock("Netherite Chestplate", Set.of("netherite_chestplate"),
                SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),
        new UseLock("Netherite Leggings", Set.of("netherite_leggings"),
                SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),
        new UseLock("Netherite Boots", Set.of("netherite_boots"),
                SkillBranch.ENDURANCE, SkillBranch.NAVIGATOR),
        new UseLock("Turtle Shell", Set.of("turtle_helmet"),
                SkillBranch.ENDURANCE, SkillBranch.EXPLORER),
        new UseLock("Geomancer Armor", Set.of("geomancer_helmet", "geomancer_chestplate", "geomancer_leggings", "geomancer_boots"),
                SkillBranch.DUNGEONEER, SkillBranch.SHIELD_WALL),
        new UseLock("Horse Armor", Set.of("diamond_horse_armor", "golden_horse_armor"),
                SkillBranch.ANIMAL_HANDLER, SkillBranch.NAVIGATOR),

        // --- Equipment & Relics ---
        new UseLock("Crowns", Set.of("lunar_crown", "solar_crown"),
                SkillBranch.RELIC_LORE, SkillBranch.SUMMONER),
        new UseLock("Masks", Set.of("umvuthana_mask_fear", "umvuthana_mask_fury", "umvuthana_mask_faith",
                "umvuthana_mask_rage", "umvuthana_mask_misery", "umvuthana_mask_bliss", "sol_visage"),
                SkillBranch.RELIC_LORE, SkillBranch.DUNGEONEER),
        new UseLock("Movement Relics", Set.of("roller_skates", "ice_skates", "elytra_booster"),
                SkillBranch.NAVIGATOR, SkillBranch.EXPLORER),
        new UseLock("Elytra", Set.of("elytra"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),
        new UseLock("Firework Rockets", Set.of("firework_rocket"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),
        new UseLock("Wolf Armor", Set.of("wolf_armor"),
                SkillBranch.ANIMAL_HANDLER, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Saddle", Set.of("saddle"),
                SkillBranch.ANIMAL_HANDLER, SkillBranch.NAVIGATOR),
        new UseLock("Totem of Undying", Set.of("totem_of_undying"),
                SkillBranch.ENDURANCE, SkillBranch.BERSERKER),
        new UseLock("Spyglass", Set.of("spyglass"),
                SkillBranch.RANGED_PRECISION, SkillBranch.EXPLORER),
        new UseLock("Ender Chest", Set.of("ender_chest"),
                SkillBranch.EXPLORER, SkillBranch.MANA_WEAVER),
        new UseLock("Shulker Boxes", Set.of("shulker_box", "white_shulker_box", "orange_shulker_box", "magenta_shulker_box",
                "light_blue_shulker_box", "yellow_shulker_box", "lime_shulker_box", "pink_shulker_box",
                "gray_shulker_box", "light_gray_shulker_box", "cyan_shulker_box", "purple_shulker_box",
                "blue_shulker_box", "brown_shulker_box", "green_shulker_box", "red_shulker_box", "black_shulker_box"),
                SkillBranch.DUNGEONEER, SkillBranch.EXPLORER),
        new UseLock("Lodestone", Set.of("lodestone"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),
        new UseLock("Recovery Compass", Set.of("recovery_compass"),
                SkillBranch.EXPLORER, SkillBranch.ENDURANCE),
        new UseLock("Name Tag", Set.of("name_tag"),
                SkillBranch.ANIMAL_HANDLER, SkillBranch.EXPLORER),
        new UseLock("Beacon", Set.of("beacon"),
                SkillBranch.ENCHANTER, SkillBranch.SUMMONER),
        new UseLock("Conduit", Set.of("conduit"),
                SkillBranch.EXPLORER, SkillBranch.MANA_WEAVER),
        new UseLock("Respawn Anchor", Set.of("respawn_anchor"),
                SkillBranch.EXPLORER, SkillBranch.DUNGEONEER),
        new UseLock("End Crystal", Set.of("end_crystal"),
                SkillBranch.MANA_WEAVER, SkillBranch.BERSERKER),
        new UseLock("Goat Horn", Set.of("goat_horn"),
                SkillBranch.EXPLORER, SkillBranch.ANIMAL_HANDLER),

        // --- Tools ---
        new UseLock("Netherite Pickaxe", Set.of("netherite_pickaxe"),
                SkillBranch.EFFICIENT_MINING, SkillBranch.SMELTER),
        new UseLock("Netherite Shovel", Set.of("netherite_shovel"),
                SkillBranch.EFFICIENT_MINING, SkillBranch.SMELTER),
        new UseLock("Netherite Axe (Tool)", Set.of("netherite_axe"),
                SkillBranch.EFFICIENT_MINING, SkillBranch.SMELTER),
        new UseLock("Netherite Hoe", Set.of("netherite_hoe"),
                SkillBranch.CROP_MASTER, SkillBranch.EFFICIENT_MINING),

        // --- Consumables ---
        new UseLock("Golden Apple", Set.of("golden_apple"),
                SkillBranch.COOK, SkillBranch.ENDURANCE),
        new UseLock("Enchanted Golden Apple", Set.of("enchanted_golden_apple"),
                SkillBranch.COOK, SkillBranch.RELIC_LORE),
        new UseLock("Suspicious Stew", Set.of("suspicious_stew"),
                SkillBranch.COOK, SkillBranch.BOTANIST),
        new UseLock("Ender Pearl", Set.of("ender_pearl"),
                SkillBranch.EXPLORER, SkillBranch.MANA_WEAVER),
        new UseLock("Chorus Fruit", Set.of("chorus_fruit"),
                SkillBranch.EXPLORER, SkillBranch.MANA_WEAVER),

        // --- Dungeon Items ---
        new UseLock("Soul Anchor", Set.of("soul_anchor"),
                SkillBranch.DUNGEONEER, SkillBranch.ENDURANCE),

        // --- Dungeon Unique Gear ---
        new UseLock("Ice Crystal", Set.of("ice_crystal"),
                SkillBranch.DUNGEONEER, SkillBranch.SPELL_BLADE),
        new UseLock("Life Stealer", Set.of("life_stealer"),
                SkillBranch.DUNGEONEER, SkillBranch.BERSERKER),
        new UseLock("Wrought Helm", Set.of("wrought_helm"),
                SkillBranch.DUNGEONEER, SkillBranch.SHIELD_WALL),
        new UseLock("Glowing Jelly", Set.of("glowing_jelly"),
                SkillBranch.DUNGEONEER, SkillBranch.BOTANIST),
        new UseLock("Naga Fang Dagger", Set.of("naga_fang_dagger"),
                SkillBranch.DUNGEONEER, SkillBranch.BLADE_MASTERY),
        new UseLock("Wrought Axe", Set.of("wrought_axe"),
                SkillBranch.DUNGEONEER, SkillBranch.BERSERKER),

        // --- New Relics (Wave 2) ---
        new UseLock("Face Relics", Set.of("wardens_visor"), SkillBranch.RELIC_LORE, SkillBranch.DUNGEONEER),
        new UseLock("Face Relics", Set.of("verdant_mask"), SkillBranch.RELIC_LORE, SkillBranch.BOTANIST),
        new UseLock("Face Relics", Set.of("frostweave_veil"), SkillBranch.RELIC_LORE, SkillBranch.SPELL_BLADE),
        new UseLock("Head Relics", Set.of("stormcaller_circlet"), SkillBranch.RELIC_LORE, SkillBranch.SPELL_BLADE),
        new UseLock("Head Relics", Set.of("ashen_diadem"), SkillBranch.RELIC_LORE, SkillBranch.SUMMONER),
        new UseLock("Head Relics", Set.of("wraith_crown"), SkillBranch.RELIC_LORE, SkillBranch.MANA_WEAVER),
        new UseLock("Hand Relics", Set.of("arcane_gauntlet"), SkillBranch.SPELL_BLADE, SkillBranch.MANA_WEAVER),
        new UseLock("Hand Relics", Set.of("iron_fist"), SkillBranch.BERSERKER, SkillBranch.BLADE_MASTERY),
        new UseLock("Hand Relics", Set.of("plague_grasp"), SkillBranch.SPELL_BLADE, SkillBranch.BOTANIST),
        new UseLock("Hand Relics", Set.of("sunforged_bracer"), SkillBranch.MANA_WEAVER, SkillBranch.SUMMONER),
        new UseLock("Ring Relics", Set.of("stormband"), SkillBranch.SPELL_BLADE, SkillBranch.RANGED_PRECISION),
        new UseLock("Ring Relics", Set.of("gravestone_ring"), SkillBranch.RELIC_LORE, SkillBranch.ENDURANCE),
        new UseLock("Ring Relics", Set.of("verdant_signet"), SkillBranch.BOTANIST, SkillBranch.MANA_WEAVER),
        new UseLock("Back Relics", Set.of("phoenix_mantle"), SkillBranch.SUMMONER, SkillBranch.ENDURANCE),
        new UseLock("Back Relics", Set.of("windrunner_cloak"), SkillBranch.NAVIGATOR, SkillBranch.EXPLORER),
        new UseLock("Back Relics", Set.of("abyssal_cape"), SkillBranch.SPELL_BLADE, SkillBranch.EXPLORER),
        new UseLock("Belt Relics", Set.of("alchemists_sash"), SkillBranch.BOTANIST, SkillBranch.ENCHANTER),
        new UseLock("Belt Relics", Set.of("guardians_girdle"), SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),
        new UseLock("Belt Relics", Set.of("serpent_belt"), SkillBranch.TACTICIAN, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Necklace Relics", Set.of("frostfire_pendant"), SkillBranch.SPELL_BLADE, SkillBranch.MANA_WEAVER),
        new UseLock("Necklace Relics", Set.of("tidekeeper_amulet"), SkillBranch.EXPLORER, SkillBranch.MANA_WEAVER),
        new UseLock("Necklace Relics", Set.of("bloodstone_choker"), SkillBranch.BERSERKER, SkillBranch.RELIC_LORE),
        new UseLock("Hand Relics", Set.of("thornweave_glove"), SkillBranch.BOTANIST, SkillBranch.TACTICIAN),
        new UseLock("Hand Relics", Set.of("chrono_glove"), SkillBranch.MANA_WEAVER, SkillBranch.ENCHANTER),
        new UseLock("Feet Relics", Set.of("stormstrider_boots"), SkillBranch.SPELL_BLADE, SkillBranch.NAVIGATOR),
        new UseLock("Feet Relics", Set.of("sandwalker_treads"), SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),
        new UseLock("Ring Relics", Set.of("emberstone_band"), SkillBranch.SPELL_BLADE, SkillBranch.ENDURANCE),
        new UseLock("Usable Relics", Set.of("void_lantern"), SkillBranch.MANA_WEAVER, SkillBranch.DUNGEONEER),
        new UseLock("Usable Relics", Set.of("thunderhorn"), SkillBranch.BERSERKER, SkillBranch.TACTICIAN),
        new UseLock("Usable Relics", Set.of("mending_chalice"), SkillBranch.MANA_WEAVER, SkillBranch.SUMMONER),

        // --- New Weapons (Wave 2) ---
        new UseLock("Shadow Weapons", Set.of("voidreaver"), SkillBranch.SPELL_BLADE, SkillBranch.TACTICIAN),
        new UseLock("Holy Weapons", Set.of("solaris"), SkillBranch.MANA_WEAVER, SkillBranch.SHIELD_WALL),
        new UseLock("Lightning Weapons", Set.of("stormfury"), SkillBranch.SPELL_BLADE, SkillBranch.BLADE_MASTERY),
        new UseLock("Nature Weapons", Set.of("briarthorn"), SkillBranch.SPELL_BLADE, SkillBranch.BOTANIST),
        new UseLock("Water Weapons", Set.of("abyssal_trident"), SkillBranch.SPELL_BLADE, SkillBranch.RANGED_PRECISION),
        new UseLock("Fire Weapons", Set.of("pyroclast"), SkillBranch.BERSERKER, SkillBranch.SPELL_BLADE),
        new UseLock("Arcane Weapons", Set.of("whisperwind"), SkillBranch.RANGED_PRECISION, SkillBranch.MANA_WEAVER),
        new UseLock("Shadow Weapons", Set.of("soulchain"), SkillBranch.SPELL_BLADE, SkillBranch.BERSERKER),
        new UseLock("Whips", Set.of("unique_whip_1", "unique_whip_2", "unique_whip_sw"), SkillBranch.TACTICIAN, SkillBranch.BLADE_MASTERY),
        new UseLock("Wands", Set.of("unique_wand_1", "unique_wand_2", "unique_wand_sw"), SkillBranch.SPELL_BLADE, SkillBranch.MANA_WEAVER),
        new UseLock("Katanas", Set.of("unique_katana_1", "unique_katana_2", "unique_katana_sw"), SkillBranch.BLADE_MASTERY, SkillBranch.TACTICIAN),
        new UseLock("Greatshields", Set.of("unique_greatshield_1", "unique_greatshield_2", "unique_greatshield_sw"), SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),
        new UseLock("Throwing Axes", Set.of("unique_throwing_axe_1", "unique_throwing_axe_2", "unique_throwing_axe_sw"), SkillBranch.RANGED_PRECISION, SkillBranch.BERSERKER),
        new UseLock("Rapiers", Set.of("unique_rapier_1", "unique_rapier_2", "unique_rapier_sw"), SkillBranch.TACTICIAN, SkillBranch.BLADE_MASTERY),
        new UseLock("Longswords", Set.of("unique_longsword_1", "unique_longsword_2"), SkillBranch.BLADE_MASTERY, SkillBranch.TACTICIAN),
        new UseLock("Claymores", Set.of("unique_claymore_3"), SkillBranch.BLADE_MASTERY, SkillBranch.BERSERKER),
        new UseLock("Daggers", Set.of("unique_dagger_3"), SkillBranch.BLADE_MASTERY, SkillBranch.TACTICIAN),
        new UseLock("Double Axes", Set.of("unique_double_axe_3"), SkillBranch.BERSERKER, SkillBranch.BLADE_MASTERY),
        new UseLock("Glaives", Set.of("unique_glaive_3"), SkillBranch.TACTICIAN, SkillBranch.BLADE_MASTERY),
        new UseLock("Hammers", Set.of("unique_hammer_3"), SkillBranch.BERSERKER, SkillBranch.SHIELD_WALL),
        new UseLock("Maces", Set.of("unique_mace_3"), SkillBranch.BERSERKER, SkillBranch.SHIELD_WALL),
        new UseLock("Sickles", Set.of("unique_sickle_3"), SkillBranch.TACTICIAN, SkillBranch.BERSERKER),
        new UseLock("Spears", Set.of("unique_spear_3"), SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Longbows", Set.of("unique_longbow_3"), SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Heavy Crossbows", Set.of("unique_heavy_crossbow_3"), SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        new UseLock("Damage Staves", Set.of("unique_staff_damage_8"), SkillBranch.SPELL_BLADE, SkillBranch.MANA_WEAVER),
        new UseLock("Healing Staves", Set.of("unique_staff_heal_3"), SkillBranch.MANA_WEAVER, SkillBranch.SUMMONER),
        new UseLock("Shields", Set.of("unique_shield_3"), SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),

        // --- Class Weapons (Paladin / Warrior) ---
        new UseLock("Class Claymores", Set.of("stone_claymore", "golden_claymore", "iron_claymore",
                "diamond_claymore", "netherite_claymore"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),
        new UseLock("Class Great Hammers", Set.of("wooden_great_hammer", "stone_great_hammer",
                "golden_great_hammer", "iron_great_hammer", "diamond_great_hammer", "netherite_great_hammer"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),
        new UseLock("Class Maces", Set.of("iron_mace", "golden_mace", "diamond_mace", "netherite_mace"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),
        new UseLock("Class Holy Wands", Set.of("acolyte_wand", "holy_wand", "diamond_holy_wand", "netherite_holy_wand"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),
        new UseLock("Class Holy Staves", Set.of("holy_staff", "diamond_holy_staff", "netherite_holy_staff"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),
        new UseLock("Class Kite Shields", Set.of("iron_kite_shield", "golden_kite_shield",
                "diamond_kite_shield", "netherite_kite_shield"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),

        // --- Class Weapons (Wizard) ---
        new UseLock("Class Wands", Set.of("wand_novice", "wand_arcane", "wand_fire", "wand_frost",
                "wand_netherite_arcane", "wand_netherite_fire", "wand_netherite_frost"),
                SkillBranch.WIZARD, null),
        new UseLock("Class Staves", Set.of("staff_wizard", "staff_arcane", "staff_fire", "staff_frost",
                "staff_netherite_arcane", "staff_netherite_fire", "staff_netherite_frost"),
                SkillBranch.WIZARD, null),

        // --- Class Weapons (Rogue) ---
        new UseLock("Class Daggers", Set.of("flint_dagger", "iron_dagger", "golden_dagger",
                "diamond_dagger", "netherite_dagger"),
                SkillBranch.ROGUE, SkillBranch.WARRIOR),
        new UseLock("Class Sickles", Set.of("golden_sickle", "iron_sickle", "diamond_sickle", "netherite_sickle"),
                SkillBranch.ROGUE, SkillBranch.WARRIOR),

        // --- Class Weapons (Warrior) ---
        new UseLock("Class Double Axes", Set.of("stone_double_axe", "golden_double_axe", "iron_double_axe",
                "diamond_double_axe", "netherite_double_axe"),
                SkillBranch.WARRIOR, SkillBranch.ROGUE),
        new UseLock("Class Glaives", Set.of("golden_glaive", "iron_glaive", "diamond_glaive", "netherite_glaive"),
                SkillBranch.WARRIOR, SkillBranch.ROGUE),

        // --- Class Weapons (Ranger) ---
        new UseLock("Class Spears", Set.of("flint_spear", "golden_spear", "iron_spear",
                "diamond_spear", "netherite_spear"),
                SkillBranch.RANGER, null),
        new UseLock("Class Bows", Set.of("composite_longbow", "mechanic_shortbow", "royal_longbow",
                "netherite_shortbow", "netherite_longbow"),
                SkillBranch.RANGER, null),
        new UseLock("Class Crossbows", Set.of("rapid_crossbow", "heavy_crossbow",
                "netherite_rapid_crossbow", "netherite_heavy_crossbow"),
                SkillBranch.RANGER, null),

        // --- Class Armor (Wizard) ---
        new UseLock("Wizard Robes", Set.of("wizard_robe_head", "wizard_robe_chest", "wizard_robe_legs", "wizard_robe_boots",
                "arcane_robe_head", "arcane_robe_chest", "arcane_robe_legs", "arcane_robe_boots",
                "fire_robe_head", "fire_robe_chest", "fire_robe_legs", "fire_robe_boots",
                "frost_robe_head", "frost_robe_chest", "frost_robe_legs", "frost_robe_boots",
                "netherite_arcane_robe_head", "netherite_arcane_robe_chest", "netherite_arcane_robe_legs", "netherite_arcane_robe_boots",
                "netherite_fire_robe_head", "netherite_fire_robe_chest", "netherite_fire_robe_legs", "netherite_fire_robe_boots",
                "netherite_frost_robe_head", "netherite_frost_robe_chest", "netherite_frost_robe_legs", "netherite_frost_robe_boots"),
                SkillBranch.WIZARD, null),

        // --- Class Armor (Paladin / Warrior) ---
        new UseLock("Paladin Armor", Set.of("paladin_armor_head", "paladin_armor_chest", "paladin_armor_legs", "paladin_armor_boots",
                "crusader_armor_head", "crusader_armor_chest", "crusader_armor_legs", "crusader_armor_boots",
                "netherite_crusader_armor_head", "netherite_crusader_armor_chest", "netherite_crusader_armor_legs", "netherite_crusader_armor_boots"),
                SkillBranch.PALADIN, SkillBranch.WARRIOR),

        // --- Class Armor (Priest / Paladin) ---
        new UseLock("Priest Robes", Set.of("priest_robe_head", "priest_robe_chest", "priest_robe_legs", "priest_robe_boots",
                "prior_robe_head", "prior_robe_chest", "prior_robe_legs", "prior_robe_boots",
                "netherite_prior_robe_head", "netherite_prior_robe_chest", "netherite_prior_robe_legs", "netherite_prior_robe_boots"),
                SkillBranch.PALADIN, null),

        // --- Class Armor (Rogue) ---
        new UseLock("Rogue Armor", Set.of("rogue_armor_head", "rogue_armor_chest", "rogue_armor_legs", "rogue_armor_boots",
                "assassin_armor_head", "assassin_armor_chest", "assassin_armor_legs", "assassin_armor_boots",
                "netherite_assassin_armor_head", "netherite_assassin_armor_chest", "netherite_assassin_armor_legs", "netherite_assassin_armor_boots"),
                SkillBranch.ROGUE, SkillBranch.WARRIOR),

        // --- Class Armor (Warrior) ---
        new UseLock("Warrior Armor", Set.of("warrior_armor_head", "warrior_armor_chest", "warrior_armor_legs", "warrior_armor_boots",
                "berserker_armor_head", "berserker_armor_chest", "berserker_armor_legs", "berserker_armor_boots",
                "netherite_berserker_armor_head", "netherite_berserker_armor_chest", "netherite_berserker_armor_legs", "netherite_berserker_armor_boots"),
                SkillBranch.WARRIOR, SkillBranch.ROGUE),

        // --- Class Armor (Ranger) ---
        new UseLock("Archer Armor", Set.of("archer_armor_head", "archer_armor_chest", "archer_armor_legs", "archer_armor_boots",
                "ranger_armor_head", "ranger_armor_chest", "ranger_armor_legs", "ranger_armor_boots",
                "netherite_ranger_armor_head", "netherite_ranger_armor_chest", "netherite_ranger_armor_legs", "netherite_ranger_armor_boots"),
                SkillBranch.RANGER, null),

        // --- Resource Dimension ---
        new UseLock("Resource Dimension Key", Set.of("resource_dimension_key"),
                SkillBranch.ORE_FINDER, SkillBranch.EFFICIENT_MINING),

        // --- Backpacks (all non-standard variants) ---
        new UseLock("Backpacks", Set.of(
                "netherite_backpack", "diamond_backpack", "gold_backpack", "iron_backpack",
                "emerald_backpack", "lapis_backpack", "redstone_backpack", "coal_backpack",
                "quartz_backpack", "bookshelf_backpack", "sandstone_backpack", "snow_backpack",
                "sponge_backpack", "cake_backpack", "cactus_backpack", "hay_backpack",
                "melon_backpack", "pumpkin_backpack", "creeper_backpack", "dragon_backpack",
                "enderman_backpack", "blaze_backpack", "ghast_backpack", "magma_cube_backpack",
                "skeleton_backpack", "spider_backpack", "wither_backpack", "warden_backpack",
                "bat_backpack", "bee_backpack", "wolf_backpack", "fox_backpack",
                "ocelot_backpack", "horse_backpack", "cow_backpack", "pig_backpack",
                "sheep_backpack", "chicken_backpack", "squid_backpack", "villager_backpack",
                "iron_golem_backpack"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),

        // --- Backpack Tier Upgrades (diamond+) ---
        new UseLock("Backpack Tier Upgrades", Set.of(
                "diamond_tier_upgrade", "netherite_tier_upgrade"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),

        // --- Backpack Feature Upgrades (all) ---
        new UseLock("Backpack Feature Upgrades", Set.of(
                "crafting_upgrade", "furnace_upgrade", "smoker_upgrade", "blast_furnace_upgrade",
                "tanks_upgrade", "pickup_upgrade", "magnet_upgrade", "jukebox_upgrade",
                "void_upgrade", "feeding_upgrade", "refill_upgrade"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR)
    );

    // ==================== Craft Locks ====================
    // Items that cannot be crafted without the required skill branch.
    // Also hidden from the recipe book if the player lacks the skill.

    public record CraftLock(String category, Set<String> itemPatterns, SkillBranch branchA, SkillBranch branchB) {}

    public static final List<CraftLock> CRAFT_LOCKS = List.of(
        // Non-standard backpack variants cannot be crafted without EXPLORER/NAVIGATOR
        new CraftLock("Backpacks", Set.of(
                "netherite_backpack", "diamond_backpack", "gold_backpack", "iron_backpack",
                "emerald_backpack", "lapis_backpack", "redstone_backpack", "coal_backpack",
                "quartz_backpack", "bookshelf_backpack", "sandstone_backpack", "snow_backpack",
                "sponge_backpack", "cake_backpack", "cactus_backpack", "hay_backpack",
                "melon_backpack", "pumpkin_backpack", "creeper_backpack", "dragon_backpack",
                "enderman_backpack", "blaze_backpack", "ghast_backpack", "magma_cube_backpack",
                "skeleton_backpack", "spider_backpack", "wither_backpack", "warden_backpack",
                "bat_backpack", "bee_backpack", "wolf_backpack", "fox_backpack",
                "ocelot_backpack", "horse_backpack", "cow_backpack", "pig_backpack",
                "sheep_backpack", "chicken_backpack", "squid_backpack", "villager_backpack",
                "iron_golem_backpack"),
                SkillBranch.EXPLORER, SkillBranch.NAVIGATOR),
        new CraftLock("Soul Anchor", Set.of("soul_anchor"),
                SkillBranch.DUNGEONEER, SkillBranch.ENDURANCE)
    );

    // ==================== Enchantment Generation Locks ====================

    // Exclusive: only the listed branches can generate these. Enchanter CANNOT bypass.
    // Standard: Enchanter IS a valid second path. Only max level locked.

    public static final List<EnchantLock> ENCHANT_LOCKS = List.of(
        // --- Exclusive Generation Locks (all levels, no Enchanter bypass) ---
        new EnchantLock("sharpness",       5, SkillBranch.BLADE_MASTERY, null, true),   // single-branch exclusive at V
        new EnchantLock("power",           5, SkillBranch.RANGED_PRECISION, null, true), // single-branch exclusive at V
        new EnchantLock("infinity",        1, SkillBranch.RANGED_PRECISION, null, true), // single-branch exclusive
        new EnchantLock("fortune",         1, SkillBranch.GEM_CUTTER, SkillBranch.ORE_FINDER, true),
        new EnchantLock("efficiency",      3, SkillBranch.EFFICIENT_MINING, SkillBranch.ORE_FINDER, true),
        new EnchantLock("silk_touch",      1, SkillBranch.ORE_FINDER, SkillBranch.EFFICIENT_MINING, true),
        new EnchantLock("looting",         1, SkillBranch.TACTICIAN, SkillBranch.HUNTER_INSTINCT, true),
        new EnchantLock("luck_of_the_sea", 1, SkillBranch.FISHERMAN, null, true),       // single-branch exclusive
        new EnchantLock("lure",            1, SkillBranch.FISHERMAN, null, true),        // single-branch exclusive
        new EnchantLock("mending",         1, SkillBranch.ENCHANTER, SkillBranch.MANA_WEAVER, true),

        // --- Standard Generation Locks (max level only, Enchanter is second path) ---
        new EnchantLock("smite",               5, SkillBranch.BLADE_MASTERY, SkillBranch.ENCHANTER, false),
        new EnchantLock("bane_of_arthropods",  5, SkillBranch.HUNTER_INSTINCT, SkillBranch.ENCHANTER, false),
        new EnchantLock("punch",               2, SkillBranch.RANGED_PRECISION, SkillBranch.ENCHANTER, false),
        new EnchantLock("protection",          4, SkillBranch.SHIELD_WALL, SkillBranch.ENCHANTER, false),
        new EnchantLock("thorns",              3, SkillBranch.SHIELD_WALL, SkillBranch.ENCHANTER, false),
        new EnchantLock("fire_aspect",         2, SkillBranch.SPELL_BLADE, SkillBranch.ENCHANTER, false),
        new EnchantLock("flame",               1, SkillBranch.SPELL_BLADE, SkillBranch.ENCHANTER, false),
        new EnchantLock("channeling",          1, SkillBranch.SPELL_BLADE, SkillBranch.ENCHANTER, false),
        new EnchantLock("sweeping_edge",       3, SkillBranch.BLADE_MASTERY, SkillBranch.ENCHANTER, false),
        new EnchantLock("feather_falling",     4, SkillBranch.TUNNEL_RAT, SkillBranch.ENCHANTER, false),
        new EnchantLock("depth_strider",       3, SkillBranch.NAVIGATOR, SkillBranch.ENCHANTER, false),
        new EnchantLock("frost_walker",        2, SkillBranch.NAVIGATOR, SkillBranch.ENCHANTER, false),
        new EnchantLock("respiration",         3, SkillBranch.EXPLORER, SkillBranch.ENCHANTER, false),
        new EnchantLock("aqua_affinity",       1, SkillBranch.EXPLORER, SkillBranch.ENCHANTER, false),
        new EnchantLock("riptide",             3, SkillBranch.NAVIGATOR, SkillBranch.ENCHANTER, false),
        new EnchantLock("loyalty",             3, SkillBranch.RANGED_PRECISION, SkillBranch.ENCHANTER, false),
        new EnchantLock("unbreaking",          3, SkillBranch.EFFICIENT_MINING, SkillBranch.ENCHANTER, false)
    );

    // ==================== Splash/Lingering/Strong Potion Locks ====================
    // These are handled separately since they match by potion type, not item ID.

    /** Item patterns that indicate a splash potion. */
    public static final UseLock SPLASH_POTIONS = new UseLock("Splash Potions",
            Set.of("splash_potion"), SkillBranch.BOTANIST, SkillBranch.MANA_WEAVER);

    /** Item patterns that indicate a lingering potion. */
    public static final UseLock LINGERING_POTIONS = new UseLock("Lingering Potions",
            Set.of("lingering_potion"), SkillBranch.BOTANIST, SkillBranch.MANA_WEAVER);

    // ==================== Enchanted Vanilla Weapons (use-locked if enchanted) ====================
    // Vanilla bow/crossbow/shield are free when unenchanted, but locked when enchanted.

    public static final Map<String, UseLock> ENCHANTED_VANILLA_LOCKS = Map.of(
        "bow", new UseLock("Enchanted Bow", Set.of("bow"),
                SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        "crossbow", new UseLock("Enchanted Crossbow", Set.of("crossbow"),
                SkillBranch.RANGED_PRECISION, SkillBranch.HUNTER_INSTINCT),
        "shield", new UseLock("Enchanted Shield", Set.of("shield"),
                SkillBranch.SHIELD_WALL, SkillBranch.ENDURANCE),
        "fishing_rod", new UseLock("Enchanted Fishing Rod", Set.of("fishing_rod"),
                SkillBranch.FISHERMAN, SkillBranch.EXPLORER)
    );

    // ==================== Smithing Table Lock ====================
    public static final UseLock SMITHING_NETHERITE = new UseLock("Netherite Upgrade",
            Set.of("netherite_upgrade_smithing_template"), SkillBranch.SMELTER, SkillBranch.EFFICIENT_MINING);
}
