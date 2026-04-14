package com.ultra.megamod.feature.combat.wizards.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.wizards.WizardsMod;
import com.ultra.megamod.feature.combat.wizards.content.WizardSpells;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainers;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapon;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Weapons;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Wand and staff registrations for the Wizards content port.
 * Ported from {@code net.wizards.item.WizardWeapons}, adapted for NeoForge 1.21.11
 * via SpellEngine's {@link Weapons} factories.
 */
public class WizardWeapons {
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
            // In MegaMod we always fall back since we don't ship BetterEnd/BetterNether/Aether items
            return () -> Ingredient.of(fallback);
        }
    }

    private static final String AETHER = "aether";
    private static final String BETTER_END = "betterend";
    private static final String BETTER_NETHER = "betternether";

    // ─── Wands ───
    public static final Weapon.Entry wand_novice = add(Weapons.damageWand(
            NAMESPACE, "wand_novice",
            Equipment.Tier.TIER_0, () -> Ingredient.of(Items.STICK),
            List.of(SpellSchools.FIRE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.fire_scorch.id()))
    );
    public static final Weapon.Entry wand_arcane = add(Weapons.damageWand(
            NAMESPACE, "wand_arcane",
            Equipment.Tier.TIER_2, () -> Ingredient.of(Items.GOLD_INGOT),
            List.of(SpellSchools.ARCANE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.arcane_bolt.id()))
    );
    public static final Weapon.Entry wand_fire = add(Weapons.damageWand(
            NAMESPACE, "wand_fire",
            Equipment.Tier.TIER_2, () -> Ingredient.of(Items.GOLD_INGOT),
            List.of(SpellSchools.FIRE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.fireball.id()))
    );
    public static final Weapon.Entry wand_frost = add(Weapons.damageWand(
            NAMESPACE, "wand_frost",
            Equipment.Tier.TIER_2, () -> Ingredient.of(Items.IRON_INGOT),
            List.of(SpellSchools.FROST.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.frost_shard.id()))
    );

    public static final Weapon.Entry wand_netherite_arcane = add(Weapons.damageWand(
            NAMESPACE, "wand_netherite_arcane",
            Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(SpellSchools.ARCANE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.arcane_bolt.id()))
    );
    public static final Weapon.Entry wand_netherite_fire = add(Weapons.damageWand(
            NAMESPACE, "wand_netherite_fire",
            Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(SpellSchools.FIRE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.fireball.id()))
    );
    public static final Weapon.Entry wand_netherite_frost = add(Weapons.damageWand(
            NAMESPACE, "wand_netherite_frost",
            Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(SpellSchools.FROST.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.frost_shard.id()))
    );

    // ─── Staves ───
    public static final Weapon.Entry staff_wizard = add(Weapons.damageStaff(
            NAMESPACE, "staff_wizard",
            Equipment.Tier.TIER_1, () -> Ingredient.of(Items.STICK),
            List.of(SpellSchools.ARCANE.id, SpellSchools.FIRE.id, SpellSchools.FROST.id))
            .spellContainer(SpellContainers.forMagicWeapon())
            .withSpellChoices("megamod:weapon/wizard_staff")
    );
    public static final Weapon.Entry staff_arcane = add(Weapons.damageStaff(
            NAMESPACE, "staff_arcane",
            Equipment.Tier.TIER_2, () -> Ingredient.of(Items.GOLD_INGOT),
            List.of(SpellSchools.ARCANE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.arcane_blast.id()))
    );
    public static final Weapon.Entry staff_fire = add(Weapons.damageStaff(
            NAMESPACE, "staff_fire",
            Equipment.Tier.TIER_2, () -> Ingredient.of(Items.GOLD_INGOT),
            List.of(SpellSchools.FIRE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.fire_blast.id()))
    );
    public static final Weapon.Entry staff_frost = add(Weapons.damageStaff(
            NAMESPACE, "staff_frost",
            Equipment.Tier.TIER_2, () -> Ingredient.of(Items.IRON_INGOT),
            List.of(SpellSchools.FROST.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.frostbolt.id()))
    );

    public static final Weapon.Entry staff_netherite_arcane = add(Weapons.damageStaff(
            NAMESPACE, "staff_netherite_arcane",
            Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(SpellSchools.ARCANE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.arcane_blast.id()))
    );
    public static final Weapon.Entry staff_netherite_fire = add(Weapons.damageStaff(
            NAMESPACE, "staff_netherite_fire",
            Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(SpellSchools.FIRE.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.fire_blast.id()))
    );
    public static final Weapon.Entry staff_netherite_frost = add(Weapons.damageStaff(
            NAMESPACE, "staff_netherite_frost",
            Equipment.Tier.TIER_3, () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(SpellSchools.FROST.id))
            .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.frostbolt.id()))
    );

    // ─── Register ───
    public static void register(Map<String, WeaponConfig> configs) {
        // Compat-mod wizard staves — always added when ignore_items_required_mods is true.
        if (WizardsMod.tweaksConfig.ignore_items_required_mods) {
            var nether = ingredient("betternether:nether_ruby", false, Items.NETHERITE_INGOT);
            add(Weapons.damageStaff(NAMESPACE, "staff_ruby_fire",
                    Equipment.Tier.TIER_4, nether, List.of(SpellSchools.FIRE.id))
                    .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.fire_blast.id())));

            var end = ingredient("betterend:aeternium_ingot", false, Items.NETHERITE_INGOT);
            add(Weapons.damageStaff(NAMESPACE, "staff_crystal_arcane",
                    Equipment.Tier.TIER_4, end, List.of(SpellSchools.ARCANE.id))
                    .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.arcane_blast.id())));
            add(Weapons.damageStaff(NAMESPACE, "staff_smaragdant_frost",
                    Equipment.Tier.TIER_4, end, List.of(SpellSchools.FROST.id))
                    .spellContainer(SpellContainers.forMagicWeapon().withSpellId(WizardSpells.frostbolt.id())));

            var aether = ingredient("aether:ambrosium_shard", false, Items.NETHERITE_INGOT);
            add(Weapons.damageStaff(NAMESPACE, "aether_wizard_staff",
                    Equipment.Tier.TIER_4, aether,
                    List.of(SpellSchools.ARCANE.id, SpellSchools.FIRE.id, SpellSchools.FROST.id))
                    .loot(Equipment.LootProperties.of("aether"))
                    .spellContainer(SpellContainers.forMagicWeapon())
                    .withSpellChoices("megamod:weapon/wizard_staff"));
        }

        Weapon.register(configs, entries, Group.KEY);
    }

    public static void init(IEventBus modEventBus) {
        register(WizardsMod.itemConfig.weapons);
    }
}
