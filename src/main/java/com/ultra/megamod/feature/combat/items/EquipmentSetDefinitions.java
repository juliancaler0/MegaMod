package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static definitions of all 22 equipment sets and their tiered bonuses.
 * Separated from EquipmentSetManager for cleanliness and easy tuning.
 *
 * Bonus values are absolute attribute amounts (not percentages in the modifier sense).
 * The attributes themselves interpret these as percentage-like values where relevant
 * (e.g. CRITICAL_CHANCE of 5.0 = 5% crit chance, ARCANE_POWER of 15.0 = +15 spell power).
 */
public final class EquipmentSetDefinitions {

    private EquipmentSetDefinitions() {}

    /** Registers all 22 equipment sets. Called once during EquipmentSetManager init. */
    public static List<EquipmentSetManager.EquipmentSet> buildAllSets() {
        List<EquipmentSetManager.EquipmentSet> sets = new ArrayList<>();

        // ═══════════════════════════════════════════════════════════════
        // WIZARD ROBES (7 sets)
        // ═══════════════════════════════════════════════════════════════

        sets.add(new EquipmentSetManager.EquipmentSet("wizard_robe", "Wizard Robe",
            Set.of("megamod:wizard_robe_head", "megamod:wizard_robe_chest", "megamod:wizard_robe_legs", "megamod:wizard_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.ARCANE_POWER, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.ARCANE_POWER, 10.0, MegaModAttributes.MANA_EFFICIENCY, 3.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("arcane_robe", "Arcane Robe",
            Set.of("megamod:arcane_robe_head", "megamod:arcane_robe_chest", "megamod:arcane_robe_legs", "megamod:arcane_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.ARCANE_POWER, 15.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.ARCANE_POWER, 25.0, MegaModAttributes.SPELL_HASTE, 2.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.ARCANE_POWER, 35.0, MegaModAttributes.SPELL_HASTE, 4.0, MegaModAttributes.CRITICAL_CHANCE, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("fire_robe", "Fire Robe",
            Set.of("megamod:fire_robe_head", "megamod:fire_robe_chest", "megamod:fire_robe_legs", "megamod:fire_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.FIRE_DAMAGE_BONUS, 15.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.FIRE_DAMAGE_BONUS, 25.0, MegaModAttributes.SPELL_HASTE, 2.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.FIRE_DAMAGE_BONUS, 35.0, MegaModAttributes.SPELL_HASTE, 4.0, MegaModAttributes.CRITICAL_DAMAGE, 20.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("frost_robe", "Frost Robe",
            Set.of("megamod:frost_robe_head", "megamod:frost_robe_chest", "megamod:frost_robe_legs", "megamod:frost_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.ICE_DAMAGE_BONUS, 15.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.ICE_DAMAGE_BONUS, 25.0, MegaModAttributes.SPELL_HASTE, 2.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.ICE_DAMAGE_BONUS, 35.0, MegaModAttributes.SPELL_HASTE, 4.0, MegaModAttributes.COOLDOWN_REDUCTION, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_arcane_robe", "Netherite Arcane Robe",
            Set.of("megamod:netherite_arcane_robe_head", "megamod:netherite_arcane_robe_chest", "megamod:netherite_arcane_robe_legs", "megamod:netherite_arcane_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.ARCANE_POWER, 25.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.ARCANE_POWER, 40.0, MegaModAttributes.SPELL_HASTE, 4.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.ARCANE_POWER, 55.0, MegaModAttributes.SPELL_HASTE, 6.0, MegaModAttributes.CRITICAL_CHANCE, 8.0, MegaModAttributes.COOLDOWN_REDUCTION, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_fire_robe", "Netherite Fire Robe",
            Set.of("megamod:netherite_fire_robe_head", "megamod:netherite_fire_robe_chest", "megamod:netherite_fire_robe_legs", "megamod:netherite_fire_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.FIRE_DAMAGE_BONUS, 25.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.FIRE_DAMAGE_BONUS, 40.0, MegaModAttributes.SPELL_HASTE, 4.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.FIRE_DAMAGE_BONUS, 55.0, MegaModAttributes.SPELL_HASTE, 6.0, MegaModAttributes.CRITICAL_DAMAGE, 30.0, MegaModAttributes.LIFESTEAL, 3.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_frost_robe", "Netherite Frost Robe",
            Set.of("megamod:netherite_frost_robe_head", "megamod:netherite_frost_robe_chest", "megamod:netherite_frost_robe_legs", "megamod:netherite_frost_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.ICE_DAMAGE_BONUS, 25.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.ICE_DAMAGE_BONUS, 40.0, MegaModAttributes.SPELL_HASTE, 4.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.ICE_DAMAGE_BONUS, 55.0, MegaModAttributes.SPELL_HASTE, 6.0, MegaModAttributes.COOLDOWN_REDUCTION, 10.0, MegaModAttributes.MANA_EFFICIENCY, 5.0))
            )));

        // ═══════════════════════════════════════════════════════════════
        // PALADIN ARMOR (3 sets)
        // ═══════════════════════════════════════════════════════════════

        sets.add(new EquipmentSetManager.EquipmentSet("paladin_armor", "Paladin Armor",
            Set.of("megamod:paladin_armor_head", "megamod:paladin_armor_chest", "megamod:paladin_armor_legs", "megamod:paladin_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.HEALING_POWER, 10.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.HEALING_POWER, 20.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 10.0, MegaModAttributes.HEALTH_REGEN_BONUS, 2.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("crusader_armor", "Crusader Armor",
            Set.of("megamod:crusader_armor_head", "megamod:crusader_armor_chest", "megamod:crusader_armor_legs", "megamod:crusader_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.HEALING_POWER, 20.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 10.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.HEALING_POWER, 30.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 15.0, MegaModAttributes.COOLDOWN_REDUCTION, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.HEALING_POWER, 40.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 20.0, MegaModAttributes.COOLDOWN_REDUCTION, 8.0, MegaModAttributes.THORNS_DAMAGE, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_crusader_armor", "Netherite Crusader Armor",
            Set.of("megamod:netherite_crusader_armor_head", "megamod:netherite_crusader_armor_chest", "megamod:netherite_crusader_armor_legs", "megamod:netherite_crusader_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.HEALING_POWER, 30.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 15.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.HEALING_POWER, 45.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 25.0, MegaModAttributes.COOLDOWN_REDUCTION, 8.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.HEALING_POWER, 60.0, MegaModAttributes.HOLY_DAMAGE_BONUS, 35.0, MegaModAttributes.COOLDOWN_REDUCTION, 12.0, MegaModAttributes.THORNS_DAMAGE, 8.0, MegaModAttributes.HEALTH_REGEN_BONUS, 4.0))
            )));

        // ═══════════════════════════════════════════════════════════════
        // PRIEST ROBES (3 sets)
        // ═══════════════════════════════════════════════════════════════

        sets.add(new EquipmentSetManager.EquipmentSet("priest_robe", "Priest Robe",
            Set.of("megamod:priest_robe_head", "megamod:priest_robe_chest", "megamod:priest_robe_legs", "megamod:priest_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.HEALING_POWER, 15.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.HEALING_POWER, 30.0, MegaModAttributes.MANA_EFFICIENCY, 5.0, MegaModAttributes.HEALTH_REGEN_BONUS, 2.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("prior_robe", "Prior Robe",
            Set.of("megamod:prior_robe_head", "megamod:prior_robe_chest", "megamod:prior_robe_legs", "megamod:prior_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.HEALING_POWER, 25.0, MegaModAttributes.SPELL_HASTE, 2.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.HEALING_POWER, 35.0, MegaModAttributes.SPELL_HASTE, 4.0, MegaModAttributes.MANA_EFFICIENCY, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.HEALING_POWER, 50.0, MegaModAttributes.SPELL_HASTE, 6.0, MegaModAttributes.MANA_EFFICIENCY, 8.0, MegaModAttributes.COOLDOWN_REDUCTION, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_prior_robe", "Netherite Prior Robe",
            Set.of("megamod:netherite_prior_robe_head", "megamod:netherite_prior_robe_chest", "megamod:netherite_prior_robe_legs", "megamod:netherite_prior_robe_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.HEALING_POWER, 35.0, MegaModAttributes.SPELL_HASTE, 3.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.HEALING_POWER, 50.0, MegaModAttributes.SPELL_HASTE, 6.0, MegaModAttributes.MANA_EFFICIENCY, 8.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.HEALING_POWER, 70.0, MegaModAttributes.SPELL_HASTE, 8.0, MegaModAttributes.MANA_EFFICIENCY, 12.0, MegaModAttributes.COOLDOWN_REDUCTION, 8.0, MegaModAttributes.HEALTH_REGEN_BONUS, 3.0))
            )));

        // ═══════════════════════════════════════════════════════════════
        // ROGUE ARMOR (3 sets)
        // ═══════════════════════════════════════════════════════════════

        sets.add(new EquipmentSetManager.EquipmentSet("rogue_armor", "Rogue Armor",
            Set.of("megamod:rogue_armor_head", "megamod:rogue_armor_chest", "megamod:rogue_armor_legs", "megamod:rogue_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.CRITICAL_CHANCE, 5.0, MegaModAttributes.DODGE_CHANCE, 3.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.CRITICAL_CHANCE, 10.0, MegaModAttributes.DODGE_CHANCE, 6.0, MegaModAttributes.COMBO_SPEED, 10.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("assassin_armor", "Assassin Armor",
            Set.of("megamod:assassin_armor_head", "megamod:assassin_armor_chest", "megamod:assassin_armor_legs", "megamod:assassin_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.CRITICAL_CHANCE, 8.0, MegaModAttributes.CRITICAL_DAMAGE, 15.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.CRITICAL_CHANCE, 12.0, MegaModAttributes.CRITICAL_DAMAGE, 25.0, MegaModAttributes.DODGE_CHANCE, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.CRITICAL_CHANCE, 15.0, MegaModAttributes.CRITICAL_DAMAGE, 40.0, MegaModAttributes.DODGE_CHANCE, 8.0, MegaModAttributes.COMBO_SPEED, 15.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_assassin_armor", "Netherite Assassin Armor",
            Set.of("megamod:netherite_assassin_armor_head", "megamod:netherite_assassin_armor_chest", "megamod:netherite_assassin_armor_legs", "megamod:netherite_assassin_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.CRITICAL_CHANCE, 12.0, MegaModAttributes.CRITICAL_DAMAGE, 25.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.CRITICAL_CHANCE, 18.0, MegaModAttributes.CRITICAL_DAMAGE, 40.0, MegaModAttributes.DODGE_CHANCE, 8.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.CRITICAL_CHANCE, 22.0, MegaModAttributes.CRITICAL_DAMAGE, 60.0, MegaModAttributes.DODGE_CHANCE, 12.0, MegaModAttributes.COMBO_SPEED, 20.0, MegaModAttributes.ARMOR_SHRED, 5.0))
            )));

        // ═══════════════════════════════════════════════════════════════
        // WARRIOR ARMOR (3 sets)
        // ═══════════════════════════════════════════════════════════════

        sets.add(new EquipmentSetManager.EquipmentSet("warrior_armor", "Warrior Armor",
            Set.of("megamod:warrior_armor_head", "megamod:warrior_armor_chest", "megamod:warrior_armor_legs", "megamod:warrior_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 10.0, MegaModAttributes.STUN_CHANCE, 3.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 20.0, MegaModAttributes.STUN_CHANCE, 5.0, MegaModAttributes.LIFESTEAL, 3.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("berserker_armor", "Berserker Armor",
            Set.of("megamod:berserker_armor_head", "megamod:berserker_armor_chest", "megamod:berserker_armor_legs", "megamod:berserker_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 20.0, MegaModAttributes.LIFESTEAL, 3.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 30.0, MegaModAttributes.LIFESTEAL, 5.0, MegaModAttributes.STUN_CHANCE, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 45.0, MegaModAttributes.LIFESTEAL, 7.0, MegaModAttributes.STUN_CHANCE, 8.0, MegaModAttributes.ARMOR_SHRED, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_berserker_armor", "Netherite Berserker Armor",
            Set.of("megamod:netherite_berserker_armor_head", "megamod:netherite_berserker_armor_chest", "megamod:netherite_berserker_armor_legs", "megamod:netherite_berserker_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 30.0, MegaModAttributes.LIFESTEAL, 5.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 50.0, MegaModAttributes.LIFESTEAL, 8.0, MegaModAttributes.STUN_CHANCE, 8.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.CRITICAL_DAMAGE, 70.0, MegaModAttributes.LIFESTEAL, 10.0, MegaModAttributes.STUN_CHANCE, 12.0, MegaModAttributes.ARMOR_SHRED, 8.0, MegaModAttributes.THORNS_DAMAGE, 5.0))
            )));

        // ═══════════════════════════════════════════════════════════════
        // ARCHER ARMOR (3 sets)
        // ═══════════════════════════════════════════════════════════════

        sets.add(new EquipmentSetManager.EquipmentSet("archer_armor", "Archer Armor",
            Set.of("megamod:archer_armor_head", "megamod:archer_armor_chest", "megamod:archer_armor_legs", "megamod:archer_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.RANGED_DAMAGE, 10.0, MegaModAttributes.DODGE_CHANCE, 3.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.RANGED_DAMAGE, 20.0, MegaModAttributes.DODGE_CHANCE, 5.0, MegaModAttributes.CRITICAL_CHANCE, 5.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("ranger_armor", "Ranger Armor",
            Set.of("megamod:ranger_armor_head", "megamod:ranger_armor_chest", "megamod:ranger_armor_legs", "megamod:ranger_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.RANGED_DAMAGE, 20.0, MegaModAttributes.CRITICAL_CHANCE, 5.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.RANGED_DAMAGE, 30.0, MegaModAttributes.CRITICAL_CHANCE, 8.0, MegaModAttributes.DODGE_CHANCE, 5.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.RANGED_DAMAGE, 40.0, MegaModAttributes.CRITICAL_CHANCE, 12.0, MegaModAttributes.DODGE_CHANCE, 8.0, MegaModAttributes.COMBO_SPEED, 10.0))
            )));

        sets.add(new EquipmentSetManager.EquipmentSet("netherite_ranger_armor", "Netherite Ranger Armor",
            Set.of("megamod:netherite_ranger_armor_head", "megamod:netherite_ranger_armor_chest", "megamod:netherite_ranger_armor_legs", "megamod:netherite_ranger_armor_feet"),
            List.of(
                new EquipmentSetManager.SetBonus(2, Map.of(MegaModAttributes.RANGED_DAMAGE, 30.0, MegaModAttributes.CRITICAL_CHANCE, 8.0)),
                new EquipmentSetManager.SetBonus(3, Map.of(MegaModAttributes.RANGED_DAMAGE, 45.0, MegaModAttributes.CRITICAL_CHANCE, 12.0, MegaModAttributes.DODGE_CHANCE, 8.0)),
                new EquipmentSetManager.SetBonus(4, Map.of(MegaModAttributes.RANGED_DAMAGE, 60.0, MegaModAttributes.CRITICAL_CHANCE, 16.0, MegaModAttributes.DODGE_CHANCE, 12.0, MegaModAttributes.COMBO_SPEED, 15.0, MegaModAttributes.CRITICAL_DAMAGE, 20.0))
            )));

        return sets;
    }
}
