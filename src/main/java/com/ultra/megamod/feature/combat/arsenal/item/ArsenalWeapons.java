package com.ultra.megamod.feature.combat.arsenal.item;

import com.ultra.megamod.feature.combat.arsenal.ArsenalMod;
import com.ultra.megamod.feature.combat.arsenal.spell.ArsenalSpells;
import com.ultra.megamod.feature.combat.arsenal.spell.ArsenalSpellGroups;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Rarity;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainers;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapons;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArsenalWeapons {
    public static final ArrayList<Weapon.Entry> entries = new ArrayList<>();

    private static Weapon.Entry add(Weapon.Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final float TIER_5_SPELL_POWER = 8;

    // MARK: Claymores

    public static final Weapon.Entry unique_claymore_1 = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_claymore_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.MAGMA_BLOCK))
            .translatedName("Cataclysm's Edge")
            .withAdditionalSpell(ArsenalSpells.exploding_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_claymore_2 = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_claymore_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Champion's Greatsword")
            .withAdditionalSpell(ArsenalSpells.radiance_melee.id().toString())
            .lootTheme(Loot.Theme.DIVINE.toString()));
    public static final Weapon.Entry unique_claymore_sw = add(Weapons.claymore(ArsenalMod.NAMESPACE, "unique_claymore_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Apolyon, the Soul-Render")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.CLAYMORE_DOUBLE_AXE.location().toString())
            .withAdditionalSpell(ArsenalSpells.rampaging_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Damage Staves

    public static final Weapon.Entry unique_staff_damage_1 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK), List.of(SpellSchools.ARCANE.id, SpellSchools.FROST.id))
            .translatedName("Nexus Key")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.cooldown_shot_spell.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_staff_damage_2 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK), List.of(SpellSchools.ARCANE.id, SpellSchools.FIRE.id))
            .translatedName("Antonidas's Staff of Rapt Concentration")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FIRE.location().toString())
            .withAdditionalSpell(ArsenalSpells.chain_reaction_spell.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));
    public static final Weapon.Entry unique_staff_damage_3 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), List.of(SpellSchools.ARCANE.id, SpellSchools.FIRE.id))
            .translatedName("Draconic Battle Staff")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FIRE.location().toString())
            .withAdditionalSpell(ArsenalSpells.flame_cloud_spell.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_staff_damage_4 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_4", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), List.of(SpellSchools.FIRE.id, SpellSchools.FROST.id))
            .translatedName("Gargoyle's Bite")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_FIRE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.leeching_spell.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_staff_damage_5 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_5", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK), List.of(SpellSchools.ARCANE.id, SpellSchools.FROST.id))
            .translatedName("Mage Lord Cane")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.shockwave_area_spell.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_staff_damage_6 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_6", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), List.of(SpellSchools.ARCANE.id, SpellSchools.FROST.id))
            .translatedName("Endless Winter")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.frost_cloud_spell.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_staff_damage_sw = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.DIAMOND), List.of(SpellSchools.ARCANE.id, SpellSchools.FIRE.id, SpellSchools.FROST.id))
            .translatedName("Grand Magister's Staff of Torrents")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FIRE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.rampaging_spell.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Healing Staves

    public static final Weapon.Entry unique_staff_heal_1 = add(Weapons.healingStaff(ArsenalMod.NAMESPACE, "unique_staff_heal_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .attribute(AttributeModifier.bonus(SpellSchools.ARCANE.id, TIER_5_SPELL_POWER))
            .translatedName("Crystalline Life-Staff")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_HEALING.location().toString())
            .withAdditionalSpell(ArsenalSpells.radiance_spell.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));
    public static final Weapon.Entry unique_staff_heal_2 = add(Weapons.healingStaff(ArsenalMod.NAMESPACE, "unique_staff_heal_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Staff of Immaculate Recovery")
            .spellContainer(SpellContainers.forMagicWeapon().withSpell("megamod:holy_shock"))
            .withAdditionalSpell(ArsenalSpells.guardian_heal.id().toString())
            .lootTheme(Loot.Theme.DIVINE.toString()));
    public static final Weapon.Entry unique_staff_heal_sw = add(Weapons.healingStaff(ArsenalMod.NAMESPACE, "unique_staff_heal_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Golden Staff of the Sin'dorei")
            .spellContainer(SpellContainers.forMagicWeapon().withSpell("megamod:holy_shock"))
            .withAdditionalSpell(ArsenalSpells.cooldown_heal.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Spears

    public static final Weapon.Entry unique_spear_1 = add(Weapons.spearWithSkill(ArsenalMod.NAMESPACE, "unique_spear_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Sonic Spear")
            .withAdditionalSpell(ArsenalSpells.slowing_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_spear_2 = add(Weapons.spearWithSkill(ArsenalMod.NAMESPACE, "unique_spear_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Spear of the Damned")
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.DIVINE.toString()));
    public static final Weapon.Entry unique_spear_sw = add(Weapons.spearWithSkill(ArsenalMod.NAMESPACE, "unique_spear_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Mounting Vengeance")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.SPEAR_GLAIVE.location().toString())
            .withAdditionalSpell(ArsenalSpells.leeching_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Daggers

    public static final Weapon.Entry unique_dagger_1 = add(Weapons.daggerWithSkill(ArsenalMod.NAMESPACE, "unique_dagger_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.PRISMARINE))
            .translatedName("Frost Fang")
            .withAdditionalSpell(ArsenalSpells.slowing_melee.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_dagger_2 = add(Weapons.daggerWithSkill(ArsenalMod.NAMESPACE, "unique_dagger_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Demonic Shiv")
            .withAdditionalSpell(ArsenalSpells.leeching_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_dagger_sw = add(Weapons.daggerWithSkill(ArsenalMod.NAMESPACE, "unique_dagger_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Crux of the Apocalypse")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.DAGGER_SICKLE.location().toString())
            .withAdditionalSpell(ArsenalSpells.sundering_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Sickles

    public static final Weapon.Entry unique_sickle_1 = add(Weapons.sickleWithSkill(ArsenalMod.NAMESPACE, "unique_sickle_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Toxic Sickle")
            .withAdditionalSpell(ArsenalSpells.poison_cloud_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_sickle_2 = add(Weapons.sickleWithSkill(ArsenalMod.NAMESPACE, "unique_sickle_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.MAGMA_BLOCK))
            .translatedName("Infernal Harvester")
            .withAdditionalSpell(ArsenalSpells.exploding_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_sickle_sw = add(Weapons.sickleWithSkill(ArsenalMod.NAMESPACE, "unique_sickle_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Thalassian Sickle")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.SICKLE_AXE.location().toString())
            .withAdditionalSpell(ArsenalSpells.swirling_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Longsword

    public static final Weapon.Entry unique_longsword_sw = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_longsword_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_INGOT))
            .translatedName("Dragonscale-Encrusted Longblade")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.ONE_HANDED_SLASHER.location().toString())
            .withAdditionalSpell(ArsenalSpells.sundering_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Double Axes

    public static final Weapon.Entry unique_double_axe_1 = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_double_axe_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Dual-blade Butcher")
            .withAdditionalSpell(ArsenalSpells.leeching_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_double_axe_2 = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_double_axe_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Arcanite Reaper")
            .withAdditionalSpell(ArsenalSpells.wither_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_double_axe_sw = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_double_axe_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_INGOT))
            .translatedName("Sunreaver War Axe")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.DOUBLE_AXE_HAMMER.location().toString())
            .withAdditionalSpell(ArsenalSpells.rampaging_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Glaives

    public static final Weapon.Entry unique_glaive_1 = add(Weapons.glaiveWithSkill(ArsenalMod.NAMESPACE, "unique_glaive_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Hellreaver")
            .withAdditionalSpell(ArsenalSpells.flame_cloud_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_glaive_2 = add(Weapons.glaiveWithSkill(ArsenalMod.NAMESPACE, "unique_glaive_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .translatedName("Crystalforge Glaive")
            .withAdditionalSpell(ArsenalSpells.shockwave_melee.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));
    public static final Weapon.Entry unique_glaive_sw = add(Weapons.glaiveWithSkill(ArsenalMod.NAMESPACE, "unique_glaive_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Shivering Felspine")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.GLAIVE_DOUBLE_AXE.location().toString())
            .withAdditionalSpell(ArsenalSpells.swirling_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Hammers

    public static final Weapon.Entry unique_hammer_1 = add(Weapons.hammerWithSkill(ArsenalMod.NAMESPACE, "unique_hammer_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .translatedName("Hammer of Destiny")
            .withAdditionalSpell(ArsenalSpells.shockwave_melee.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));
    public static final Weapon.Entry unique_hammer_2 = add(Weapons.hammerWithSkill(ArsenalMod.NAMESPACE, "unique_hammer_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Blackhand")
            .withAdditionalSpell(ArsenalSpells.exploding_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_hammer_sw = add(Weapons.hammerWithSkill(ArsenalMod.NAMESPACE, "unique_hammer_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Hammer of Sanctification")
            .withAdditionalSpell(ArsenalSpells.radiance_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Maces

    public static final Weapon.Entry unique_mace_1 = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_mace_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Bonecracker")
            .withAdditionalSpell(ArsenalSpells.sundering_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_mace_2 = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_mace_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Stormherald")
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_mace_sw = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_mace_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Archon's Scepter")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.MACE_SWORD.location().toString())
            .withAdditionalSpell(ArsenalSpells.guarding_strike_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));

    // MARK: Nightmare _3 variants (matching their _1/_2 counterpart spells)

    public static final Weapon.Entry unique_claymore_3 = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_claymore_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Northwind Greatsword")
            .withAdditionalSpell(ArsenalSpells.swirling_melee.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_dagger_3 = add(Weapons.daggerWithSkill(ArsenalMod.NAMESPACE, "unique_dagger_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .translatedName("Whispersting")
            .withAdditionalSpell(ArsenalSpells.poison_cloud_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_double_axe_3 = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_double_axe_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.MAGMA_BLOCK))
            .translatedName("Cindercleaver")
            .withAdditionalSpell(ArsenalSpells.exploding_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_glaive_3 = add(Weapons.glaiveWithSkill(ArsenalMod.NAMESPACE, "unique_glaive_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .translatedName("Moonreaver Glaive")
            .withAdditionalSpell(ArsenalSpells.slowing_melee.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));
    public static final Weapon.Entry unique_hammer_3 = add(Weapons.hammerWithSkill(ArsenalMod.NAMESPACE, "unique_hammer_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Frostfall Maul")
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_mace_3 = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_mace_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Radiant Morningstar")
            .withAdditionalSpell(ArsenalSpells.radiance_melee.id().toString())
            .lootTheme(Loot.Theme.DIVINE.toString()));
    public static final Weapon.Entry unique_sickle_3 = add(Weapons.sickleWithSkill(ArsenalMod.NAMESPACE, "unique_sickle_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Frostbite Reaper")
            .withAdditionalSpell(ArsenalSpells.slowing_melee.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_spear_3 = add(Weapons.spearWithSkill(ArsenalMod.NAMESPACE, "unique_spear_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Thunderlance")
            .withAdditionalSpell(ArsenalSpells.shockwave_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_staff_damage_8 = add(Weapons.damageStaff(ArsenalMod.NAMESPACE, "unique_staff_damage_8", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK), List.of(SpellSchools.ARCANE.id, SpellSchools.FROST.id))
            .translatedName("Stormcaller's Rod")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.chain_reaction_spell.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_staff_heal_3 = add(Weapons.healingStaff(ArsenalMod.NAMESPACE, "unique_staff_heal_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Living Wood Staff")
            .spellContainer(SpellContainers.forMagicWeapon().withSpell("megamod:holy_shock"))
            .withAdditionalSpell(ArsenalSpells.radiance_spell.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));

    // MARK: Longswords (sword base type + spell container)

    public static final Weapon.Entry unique_longsword_1 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_longsword_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Hearthguard Blade")
            .withAdditionalSpell(ArsenalSpells.guarding_strike_melee.id().toString())
            .lootTheme(Loot.Theme.DIVINE.toString()));
    public static final Weapon.Entry unique_longsword_2 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_longsword_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Nightfall Edge")
            .withAdditionalSpell(ArsenalSpells.wither_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_longsword_3 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_longsword_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .translatedName("Duskblade")
            .withAdditionalSpell(ArsenalSpells.leeching_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));

    // MARK: Whips (use sword base + swipe skill)

    public static final Weapon.Entry unique_whip_1 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_whip_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Serpent's Lash")
            .withAdditionalSpell(ArsenalSpells.poison_cloud_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_whip_2 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_whip_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.MAGMA_BLOCK))
            .translatedName("Flamelash")
            .withAdditionalSpell(ArsenalSpells.flame_cloud_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));
    public static final Weapon.Entry unique_whip_sw = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_whip_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Thornwhip of the Verdant Warden")
            .spellContainer(SpellContainers.forMeleeWeapon())
            .withSpellChoices(ArsenalSpellGroups.ONE_HANDED_SLASHER.location().toString())
            .withAdditionalSpell(ArsenalSpells.swirling_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));
    public static final Weapon.Entry unique_whip_3 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_whip_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Thunderlash")
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));

    // MARK: Arsenal Wands

    public static final Weapon.Entry unique_wand_1 = add(Weapons.damageWand(ArsenalMod.NAMESPACE, "unique_wand_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK), List.of(SpellSchools.ARCANE.id))
            .translatedName("Arcane Focus")
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(com.ultra.megamod.feature.combat.wizards.content.WizardSpells.arcane_bolt.id()))
            .withAdditionalSpell(ArsenalSpells.cooldown_shot_spell.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));
    public static final Weapon.Entry unique_wand_2 = add(Weapons.damageWand(ArsenalMod.NAMESPACE, "unique_wand_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), List.of(SpellSchools.FROST.id))
            .translatedName("Frostfinger")
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(com.ultra.megamod.feature.combat.wizards.content.WizardSpells.frost_shard.id()))
            .withAdditionalSpell(ArsenalSpells.frost_cloud_spell.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_wand_sw = add(Weapons.damageWand(ArsenalMod.NAMESPACE, "unique_wand_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK), List.of(SpellSchools.ARCANE.id, SpellSchools.FIRE.id, SpellSchools.FROST.id))
            .translatedName("Star Conduit")
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices(ArsenalSpellGroups.STAFF_ARCANE_FIRE_FROST.location().toString())
            .withAdditionalSpell(ArsenalSpells.rampaging_spell.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));
    public static final Weapon.Entry unique_wand_3 = add(Weapons.damageWand(ArsenalMod.NAMESPACE, "unique_wand_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP), List.of(SpellSchools.ARCANE.id))
            .translatedName("Voidwand")
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(com.ultra.megamod.feature.combat.wizards.content.WizardSpells.arcane_bolt.id()))
            .withAdditionalSpell(ArsenalSpells.leeching_spell.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));

    // MARK: Katanas (claymore base + flurry skill)

    public static final Weapon.Entry unique_katana_1 = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_katana_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Windcutter")
            .withAdditionalSpell(ArsenalSpells.swirling_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_katana_2 = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_katana_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Shadowmoon Blade")
            .withAdditionalSpell(ArsenalSpells.wither_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_katana_sw = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_katana_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Ashenblade of the Eternal Dusk")
            .spellContainer(SpellContainers.forMeleeWeapon())
            .withSpellChoices(ArsenalSpellGroups.TWO_HANDED_SLASHER.location().toString())
            .withAdditionalSpell(ArsenalSpells.rampaging_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));
    public static final Weapon.Entry unique_katana_3 = add(Weapons.claymoreWithSkill(ArsenalMod.NAMESPACE, "unique_katana_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.MAGMA_BLOCK))
            .translatedName("Crimson Edge")
            .withAdditionalSpell(ArsenalSpells.leeching_melee.id().toString())
            .lootTheme(Loot.Theme.FIERY.toString()));

    // MARK: Rapiers (sword base + swift_strikes skill)

    public static final Weapon.Entry unique_rapier_1 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_rapier_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Duelist's Sting")
            .withAdditionalSpell(ArsenalSpells.sundering_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_rapier_2 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_rapier_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Venomfang Foil")
            .withAdditionalSpell(ArsenalSpells.poison_cloud_melee.id().toString())
            .lootTheme(Loot.Theme.EVIL.toString()));
    public static final Weapon.Entry unique_rapier_sw = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_rapier_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Silvered Estoc of the Court")
            .spellContainer(SpellContainers.forMeleeWeapon())
            .withSpellChoices(ArsenalSpellGroups.ONE_HANDED_SLASHER.location().toString())
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));
    public static final Weapon.Entry unique_rapier_3 = add(Weapons.swordWithSkill(ArsenalMod.NAMESPACE, "unique_rapier_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.AMETHYST_BLOCK))
            .translatedName("Mithril Foil")
            .withAdditionalSpell(ArsenalSpells.slowing_melee.id().toString())
            .lootTheme(Loot.Theme.CRYSTAL.toString()));

    // MARK: Greatshields (mace base + smash skill)

    public static final Weapon.Entry unique_greatshield_1 = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_greatshield_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Ironwall")
            .withAdditionalSpell(ArsenalSpells.guarding_strike_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_greatshield_2 = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_greatshield_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Aegis of Dawn")
            .withAdditionalSpell(ArsenalSpells.radiance_melee.id().toString())
            .lootTheme(Loot.Theme.DIVINE.toString()));
    public static final Weapon.Entry unique_greatshield_sw = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_greatshield_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Titan's Bulwark")
            .spellContainer(SpellContainers.forMeleeWeapon())
            .withSpellChoices(ArsenalSpellGroups.MACE_SWORD.location().toString())
            .withAdditionalSpell(ArsenalSpells.guarding_strike_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));
    public static final Weapon.Entry unique_greatshield_3 = add(Weapons.maceWithSkill(ArsenalMod.NAMESPACE, "unique_greatshield_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Stoneguard Aegis")
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));

    // MARK: Throwing Axes (double_axe base + whirlwind skill)

    public static final Weapon.Entry unique_throwing_axe_1 = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_throwing_axe_1", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Stormhatchet")
            .withAdditionalSpell(ArsenalSpells.stunning_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));
    public static final Weapon.Entry unique_throwing_axe_2 = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_throwing_axe_2", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.NETHERITE_SCRAP))
            .translatedName("Frostbite Hatchet")
            .withAdditionalSpell(ArsenalSpells.slowing_melee.id().toString())
            .lootTheme(Loot.Theme.FROSTY.toString()));
    public static final Weapon.Entry unique_throwing_axe_sw = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_throwing_axe_sw", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.GOLD_BLOCK))
            .translatedName("Galeforce Tomahawk")
            .spellContainer(SpellContainers.forMeleeWeapon())
            .withSpellChoices(ArsenalSpellGroups.DOUBLE_AXE_HAMMER.location().toString())
            .withAdditionalSpell(ArsenalSpells.rampaging_melee.id().toString())
            .lootTheme(Loot.Theme.ELVEN.toString()));
    public static final Weapon.Entry unique_throwing_axe_3 = add(Weapons.doubleAxeWithSkill(ArsenalMod.NAMESPACE, "unique_throwing_axe_3", Equipment.Tier.TIER_5, () -> Ingredient.of(Items.IRON_BLOCK))
            .translatedName("Tempest Hatchet")
            .withAdditionalSpell(ArsenalSpells.shockwave_melee.id().toString())
            .lootTheme(Loot.Theme.GENERIC.toString()));

    static {
        entries.forEach(entry -> entry.rarity = Rarity.RARE);
    }

    public static void register(Map<String, WeaponConfig> configs) {
        Weapon.register(configs, entries, Group.KEY);
    }
}
