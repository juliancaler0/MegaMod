package com.ultra.megamod.feature.combat.wizards.item.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.wizards.WizardsMod;
import com.ultra.megamod.feature.combat.wizards.content.WizardSounds;
import com.ultra.megamod.feature.combat.wizards.item.Group;
import com.ultra.megamod.lib.spellengine.api.config.ArmorSetConfig;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellpower.api.SpellPowerMechanics;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wizard robe armor sets — ported from {@code net.wizards.item.WizardArmors}.
 * Mirrors the Paladins {@link com.ultra.megamod.feature.combat.paladins.item.armor.Armors} layout
 * for the seven robe sets: wizard, arcane, fire, frost, netherite_arcane, netherite_fire, netherite_frost.
 */
public class Armors {
    public static final ArrayList<Armor.Entry> entries = new ArrayList<>();

    private static Armor.Entry create(ArmorMaterial material, Identifier id, int durability,
                                      Armor.Set.ItemFactory factory, ArmorSetConfig defaults, int tier) {
        var entry = Armor.Entry.create(
                material,
                id,
                durability,
                factory,
                defaults,
                Equipment.LootProperties.of(tier)
        );
        entries.add(entry);
        return entry;
    }

    private static ResourceKey<EquipmentAsset> createAssetId(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MegaMod.MODID, name));
    }

    public static ArmorMaterial material(
            String name,
            int protectionHead, int protectionChest, int protectionLegs, int protectionFeet,
            int enchantability, Holder<SoundEvent> equipSound, TagKey<Item> repairTag) {
        return new ArmorMaterial(
                15,
                Map.of(ArmorType.HELMET, protectionHead,
                       ArmorType.CHESTPLATE, protectionChest,
                       ArmorType.LEGGINGS, protectionLegs,
                       ArmorType.BOOTS, protectionFeet),
                enchantability, equipSound,
                0, 0,
                repairTag,
                createAssetId(name)
        );
    }

    public static ArmorMaterial material_wizard = material(
            "wizard_robe",
            1, 3, 2, 1,
            9,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.WOOL);

    public static ArmorMaterial material_arcane = material(
            "arcane_robe",
            1, 3, 2, 1,
            10,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.WOOL);

    public static ArmorMaterial material_fire = material(
            "fire_robe",
            1, 3, 2, 1,
            10,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.WOOL);

    public static ArmorMaterial material_frost = material(
            "frost_robe",
            1, 3, 2, 1,
            10,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.WOOL);

    public static ArmorMaterial material_netherite_arcane = material(
            "netherite_arcane_robe",
            1, 3, 2, 1,
            15,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.REPAIRS_NETHERITE_ARMOR);

    public static ArmorMaterial material_netherite_fire = material(
            "netherite_fire_robe",
            1, 3, 2, 1,
            15,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.REPAIRS_NETHERITE_ARMOR);

    public static ArmorMaterial material_netherite_frost = material(
            "netherite_frost_robe",
            1, 3, 2, 1,
            15,
            WizardSounds.WIZARD_ROBES_EQUIP.entry(), ItemTags.REPAIRS_NETHERITE_ARMOR);

    private static final float spell_power_t1 = 0.2F;
    private static final float spell_power_t2 = 0.25F;
    private static final float spell_power_t3 = 0.3F;

    private static final float haste_t2 = 0.02F;
    private static final float haste_t3 = 0.03F;

    private static final float crit_damage_t2 = 0.05F;
    private static final float crit_chance_t3 = 0.03F;

    private static final float crit_chance_t2 = 0.02F;
    private static final float crit_damage_t3 = 0.06F;

    public static final Armor.Set wizardRobeSet = create(
            material_wizard,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "wizard_robe"),
            10,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .add(AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t1)),
                    new ArmorSetConfig.Piece(3)
                            .add(AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t1)),
                    new ArmorSetConfig.Piece(2)
                            .add(AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t1)),
                    new ArmorSetConfig.Piece(1)
                            .add(AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t1))
                            .add(AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t1))
            ), 1)
            .armorSet();

    public static final Armor.Set arcaneRobeSet = create(
            material_arcane,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "arcane_robe"),
            20,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t2)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t2)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t2)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t2)
                            ))
            ), 2)
            .armorSet();

    public static final Armor.Set fireRobeSet = create(
            material_fire,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "fire_robe"),
            20,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t2)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t2)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t2)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t2)
                            ))
            ), 2)
            .armorSet();

    public static final Armor.Set frostRobeSet = create(
            material_frost,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "frost_robe"),
            20,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t2)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t2)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t2)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t2),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t2)
                            ))
            ), 2)
            .armorSet();

    public static final Armor.Set netheriteArcaneRobeSet = create(
            material_netherite_arcane,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_arcane_robe"),
            30,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t3)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t3)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t3)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.ARCANE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, haste_t3)
                            ))
            ), 3)
            .armorSet();

    public static final Armor.Set netheriteFireRobeSet = create(
            material_netherite_fire,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_fire_robe"),
            30,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t3)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t3)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t3)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FIRE.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_CHANCE.id, crit_chance_t3)
                            ))
            ), 3)
            .armorSet();

    public static final Armor.Set netheriteFrostRobeSet = create(
            material_netherite_frost,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_frost_robe"),
            30,
            WizardArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t3)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t3)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t3)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.FROST.id, spell_power_t3),
                                    AttributeModifier.multiply(SpellPowerMechanics.CRITICAL_DAMAGE.id, crit_damage_t3)
                            ))
            ), 3)
            .armorSet();

    public static void register(Map<String, ArmorSetConfig> configs) {
        Armor.register(configs, entries, Group.KEY);
    }

    public static void init(IEventBus modEventBus) {
        register(WizardsMod.itemConfig.armor_sets);
    }
}
