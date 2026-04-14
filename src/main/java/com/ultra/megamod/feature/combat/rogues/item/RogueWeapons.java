package com.ultra.megamod.feature.combat.rogues.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.RoguesMod;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapons;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.Map;

/**
 * Rogue & Warrior weapons — ported from {@code net.rogues.item.RogueWeapons}.
 *
 * <p>All rogue/warrior weapons are registered through SpellEngine's
 * {@link Weapons} factory pipeline so they carry {@code SpellContainer}
 * components and participate in weapon-skill right-click casting (FAN_OF_KNIVES
 * for daggers, SWIPE for sickles, HEAVY_STRIKE for double axes, THRUST for
 * glaives — see {@code WeaponSkills} constants).</p>
 *
 * <p>Additional rogue/warrior class spells (slice_and_dice, shadow_step, vanish,
 * shock_powder, shattering_throw, shout, charge, whirlwind) live under
 * {@code data/megamod/spell/} and are bound to items via spellbooks / learn
 * recipes / spell assignment managers rather than pinned {@code withAdditionalSpell}
 * calls, matching the reference mod's design.</p>
 */
public class RogueWeapons {
    private static final String NAMESPACE = MegaMod.MODID;
    public static final ArrayList<Weapon.Entry> entries = new ArrayList<>();

    private static Weapon.Entry add(Weapon.Entry entry) {
        entries.add(entry);
        return entry;
    }

    // MARK: Daggers (5 tiers)

    public static final Weapon.Entry flint_dagger = add(Weapons.daggerWithSkill(
            NAMESPACE, "flint_dagger", Equipment.Tier.TIER_0, () -> Ingredient.of(Items.FLINT)));
    public static final Weapon.Entry iron_dagger = add(Weapons.daggerWithSkill(
            NAMESPACE, "iron_dagger", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_dagger = add(Weapons.daggerWithSkill(
            NAMESPACE, "golden_dagger", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_dagger = add(Weapons.daggerWithSkill(
            NAMESPACE, "diamond_dagger", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_dagger = add(Weapons.daggerWithSkill(
            NAMESPACE, "netherite_dagger", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Sickles (4 tiers)

    public static final Weapon.Entry iron_sickle = add(Weapons.sickleWithSkill(
            NAMESPACE, "iron_sickle", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_sickle = add(Weapons.sickleWithSkill(
            NAMESPACE, "golden_sickle", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_sickle = add(Weapons.sickleWithSkill(
            NAMESPACE, "diamond_sickle", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_sickle = add(Weapons.sickleWithSkill(
            NAMESPACE, "netherite_sickle", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Double Axes (5 tiers, warrior-side)

    public static final Weapon.Entry stone_double_axe = add(Weapons.doubleAxeWithSkill(
            NAMESPACE, "stone_double_axe", Equipment.Tier.TIER_0, () -> Ingredient.of(Items.COBBLESTONE)));
    public static final Weapon.Entry iron_double_axe = add(Weapons.doubleAxeWithSkill(
            NAMESPACE, "iron_double_axe", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_double_axe = add(Weapons.doubleAxeWithSkill(
            NAMESPACE, "golden_double_axe", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_double_axe = add(Weapons.doubleAxeWithSkill(
            NAMESPACE, "diamond_double_axe", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_double_axe = add(Weapons.doubleAxeWithSkill(
            NAMESPACE, "netherite_double_axe", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Glaives (4 tiers, warrior-side)

    public static final Weapon.Entry iron_glaive = add(Weapons.glaiveWithSkill(
            NAMESPACE, "iron_glaive", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_glaive = add(Weapons.glaiveWithSkill(
            NAMESPACE, "golden_glaive", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_glaive = add(Weapons.glaiveWithSkill(
            NAMESPACE, "diamond_glaive", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_glaive = add(Weapons.glaiveWithSkill(
            NAMESPACE, "netherite_glaive", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Register

    public static void register(Map<String, WeaponConfig> configs) {
        Weapon.register(configs, entries, Group.KEY);
    }

    public static void init(IEventBus modEventBus) {
        register(RoguesMod.itemConfig.weapons);
    }
}
