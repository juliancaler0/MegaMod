package com.ultra.megamod.feature.combat.paladins.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.PaladinsMod;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSpells;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainers;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapons;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

public class PaladinWeapons {
    private static final String NAMESPACE = MegaMod.MODID;
    public static final ArrayList<Weapon.Entry> entries = new ArrayList<>();

    private static Weapon.Entry add(Weapon.Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static Supplier<Ingredient> ingredient(String idString, boolean requirement, Item fallback) {
        var id = Identifier.parse(idString);
        if (requirement) {
            return () -> Ingredient.of(fallback);
        } else {
            return () -> {
                // In MegaMod, always use fallback since we don't have these compat mods
                return Ingredient.of(fallback);
            };
        }
    }

    private static final String AETHER = "aether";
    private static final String BETTER_END = "betterend";
    private static final String BETTER_NETHER = "betternether";

    // MARK: Claymores

    public static final Weapon.Entry stone_claymore = add(Weapons.claymoreWithSkill(
            NAMESPACE, "stone_claymore", Equipment.Tier.TIER_0, () -> Ingredient.of(Items.COBBLESTONE)));
    public static final Weapon.Entry iron_claymore = add(Weapons.claymoreWithSkill(
            NAMESPACE, "iron_claymore", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_claymore = add(Weapons.claymoreWithSkill(
            NAMESPACE, "golden_claymore", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_claymore = add(Weapons.claymoreWithSkill(
            NAMESPACE, "diamond_claymore", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_claymore = add(Weapons.claymoreWithSkill(
            NAMESPACE, "netherite_claymore", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Hammers

    public static final Weapon.Entry wooden_great_hammer = add(Weapons.hammerWithSkill(
            NAMESPACE, "wooden_great_hammer", Equipment.Tier.WOODEN, () -> Ingredient.of(Items.OAK_PLANKS)));
    public static final Weapon.Entry stone_great_hammer = add(Weapons.hammerWithSkill(
            NAMESPACE, "stone_great_hammer", Equipment.Tier.TIER_0, () -> Ingredient.of(Items.COBBLESTONE)));
    public static final Weapon.Entry iron_great_hammer = add(Weapons.hammerWithSkill(
            NAMESPACE, "iron_great_hammer", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_great_hammer = add(Weapons.hammerWithSkill(
            NAMESPACE, "golden_great_hammer", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_great_hammer = add(Weapons.hammerWithSkill(
            NAMESPACE, "diamond_great_hammer", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_great_hammer = add(Weapons.hammerWithSkill(
            NAMESPACE, "netherite_great_hammer", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Maces

    public static final Weapon.Entry iron_mace = add(Weapons.maceWithSkill(
            NAMESPACE, "iron_mace", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.IRON_INGOT)));
    public static final Weapon.Entry golden_mace = add(Weapons.maceWithSkill(
            NAMESPACE, "golden_mace", Equipment.Tier.GOLDEN, () -> Ingredient.of(Items.GOLD_INGOT)));
    public static final Weapon.Entry diamond_mace = add(Weapons.maceWithSkill(
            NAMESPACE, "diamond_mace", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND)));
    public static final Weapon.Entry netherite_mace = add(Weapons.maceWithSkill(
            NAMESPACE, "netherite_mace", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT)));

    // MARK: Wands

    public static final Weapon.Entry acolyte_wand = add(Weapons.healingWand(
            NAMESPACE, "acolyte_wand", Equipment.Tier.TIER_0, () -> Ingredient.of(Items.STICK))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HEAL.id())));
    public static final Weapon.Entry holy_wand = add(Weapons.healingWand(
            NAMESPACE, "holy_wand", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.GOLD_INGOT))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HEAL.id())));
    public static final Weapon.Entry diamond_holy_wand = add(Weapons.healingWand(
            NAMESPACE, "diamond_holy_wand", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HEAL.id())));
    public static final Weapon.Entry netherite_holy_wand = add(Weapons.healingWand(
            NAMESPACE, "netherite_holy_wand", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HEAL.id())));

    // MARK: Staves

    public static final Weapon.Entry holy_staff = add(Weapons.healingStaff(
            NAMESPACE, "holy_staff", Equipment.Tier.TIER_1, () -> Ingredient.of(Items.GOLD_INGOT))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HOLY_SHOCK.id())));
    public static final Weapon.Entry diamond_holy_staff = add(Weapons.healingStaff(
            NAMESPACE, "diamond_holy_staff", Equipment.Tier.TIER_2, () -> Ingredient.of(Items.DIAMOND))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HOLY_SHOCK.id())));
    public static final Weapon.Entry netherite_holy_staff = add(Weapons.healingStaff(
            NAMESPACE, "netherite_holy_staff", Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HOLY_SHOCK.id())));

    // MARK: Register

    public static void register(Map<String, WeaponConfig> configs) {
        // Add compat mod weapons (always included since ignore_items_required_mods = true)
        var repair = ingredient("betternether:nether_ruby", false, Items.NETHERITE_INGOT);
        add(Weapons.healingStaff(NAMESPACE, "ruby_holy_staff", Equipment.Tier.TIER_4, repair)
                .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HOLY_SHOCK.id())));
        add(Weapons.claymoreWithSkill(NAMESPACE, "ruby_claymore", Equipment.Tier.TIER_4, repair));
        add(Weapons.hammerWithSkill(NAMESPACE, "ruby_great_hammer", Equipment.Tier.TIER_4, repair));
        add(Weapons.maceWithSkill(NAMESPACE, "ruby_mace", Equipment.Tier.TIER_4, repair));

        var endRepair = ingredient("betterend:aeternium_ingot", false, Items.NETHERITE_INGOT);
        add(Weapons.claymoreWithSkill(NAMESPACE, "aeternium_claymore", Equipment.Tier.TIER_4, endRepair));
        add(Weapons.hammerWithSkill(NAMESPACE, "aeternium_great_hammer", Equipment.Tier.TIER_4, endRepair));
        add(Weapons.maceWithSkill(NAMESPACE, "aeternium_mace", Equipment.Tier.TIER_4, endRepair));

        var aetherRepair = ingredient("aether:ambrosium_shard", false, Items.NETHERITE_INGOT);
        add(Weapons.healingStaff(NAMESPACE, "aether_holy_staff", Equipment.Tier.TIER_4, aetherRepair)
                .spellContainer(SpellContainers.forMagicWeapon().withSpellId(PaladinSpells.HOLY_SHOCK.id()))
                .loot(Equipment.LootProperties.of("aether")));
        add(Weapons.claymoreWithSkill(NAMESPACE, "aether_claymore", Equipment.Tier.TIER_4, aetherRepair)
                .loot(Equipment.LootProperties.of("aether")));
        add(Weapons.hammerWithSkill(NAMESPACE, "aether_great_hammer", Equipment.Tier.TIER_4, aetherRepair)
                .loot(Equipment.LootProperties.of("aether")));
        add(Weapons.maceWithSkill(NAMESPACE, "aether_mace", Equipment.Tier.TIER_4, aetherRepair)
                .loot(Equipment.LootProperties.of("aether")));

        Weapon.register(configs, entries, Group.KEY);
    }

    public static void init(IEventBus modEventBus) {
        register(PaladinsMod.itemConfig.weapons);
    }
}
