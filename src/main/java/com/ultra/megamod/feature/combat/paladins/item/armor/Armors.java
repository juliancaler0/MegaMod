package com.ultra.megamod.feature.combat.paladins.item.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.PaladinsMod;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSounds;
import com.ultra.megamod.feature.combat.paladins.item.Group;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            String name, int protectionHead, int protectionChest, int protectionLegs, int protectionFeet,
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

    private static final Identifier ATTACK_DAMAGE_ID = Identifier.withDefaultNamespace("generic.attack_damage");
    private static final Identifier ARMOR_TOUGHNESS_ID = Identifier.withDefaultNamespace("generic.armor_toughness");

    private static AttributeModifier damageMultiplier(float value) {
        return new AttributeModifier(
                ATTACK_DAMAGE_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifier toughnessBonus(float value) {
        return new AttributeModifier(
                ARMOR_TOUGHNESS_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
    }

    public static ArmorMaterial paladin_armor = material(
            "paladin_armor",
            2, 6, 5, 2,
            9,
            PaladinSounds.paladin_armor_equip.entry(), ItemTags.REPAIRS_IRON_ARMOR);

    public static ArmorMaterial crusader_armor = material(
            "crusader_armor",
            3, 8, 6, 3,
            10,
            PaladinSounds.paladin_armor_equip.entry(), ItemTags.REPAIRS_GOLD_ARMOR);

    public static ArmorMaterial netherite_crusader_armor = material(
            "netherite_crusader_armor",
            3, 8, 6, 3,
            15,
            PaladinSounds.paladin_armor_equip.entry(), ItemTags.REPAIRS_NETHERITE_ARMOR);

    public static ArmorMaterial priest_robe = material(
            "priest_robe",
            1, 3, 2, 1,
            9,
            PaladinSounds.priest_robe_equip.entry(), ItemTags.REPAIRS_LEATHER_ARMOR);

    public static ArmorMaterial prior_robe = material(
            "prior_robe",
            1, 3, 2, 1,
            10,
            PaladinSounds.priest_robe_equip.entry(), ItemTags.REPAIRS_GOLD_ARMOR);

    public static ArmorMaterial netherite_prior_robe = material(
            "netherite_prior_robe",
            1, 3, 2, 1,
            15,
            PaladinSounds.priest_robe_equip.entry(), ItemTags.REPAIRS_NETHERITE_ARMOR);

    private static final float paladin_t1_spell_power = 0.5F;
    private static final float paladin_t2_spell_power = 1F;
    private static final float paladin_t3_spell_power = 1F;
    private static final float paladin_t3_toughness = 1F;

    public static final Armor.Set paladinArmorSet_t1 = create(
            paladin_armor,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "paladin_armor"),
            15,
            PaladinArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t1_spell_power)),
                    new ArmorSetConfig.Piece(6)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t1_spell_power)),
                    new ArmorSetConfig.Piece(5)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t1_spell_power)),
                    new ArmorSetConfig.Piece(2)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t1_spell_power))
            ), 1)
            .armorSet();

    public static final Armor.Set paladinArmorSet_t2 = create(
            crusader_armor,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "crusader_armor"),
            25,
            PaladinArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(3)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t2_spell_power)),
                    new ArmorSetConfig.Piece(8)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t2_spell_power)),
                    new ArmorSetConfig.Piece(6)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t2_spell_power)),
                    new ArmorSetConfig.Piece(3)
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t2_spell_power))
            ), 2)
            .armorSet();

    public static final Armor.Set paladinArmorSet_t3 = create(
            netherite_crusader_armor,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_crusader_armor"),
            37,
            PaladinArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(3)
                            .add(toughnessBonus(paladin_t3_toughness))
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t3_spell_power)),
                    new ArmorSetConfig.Piece(8)
                            .add(toughnessBonus(paladin_t3_toughness))
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t3_spell_power)),
                    new ArmorSetConfig.Piece(6)
                            .add(toughnessBonus(paladin_t3_toughness))
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t3_spell_power)),
                    new ArmorSetConfig.Piece(3)
                            .add(toughnessBonus(paladin_t3_toughness))
                            .addAll(AttributeModifier.bonuses(List.of(SpellSchools.HEALING.id), paladin_t3_spell_power))
            ), 3)
            .armorSet();

    private static final float priest_t1_spell_power = 0.2F;
    private static final float priest_t2_spell_power = 0.25F;
    private static final float priest_t2_haste = 0.03F;
    private static final float priest_t3_spell_power = 0.3F;
    private static final float priest_t3_haste = 0.04F;

    public static final Armor.Set priestArmorSet_t1 = create(
            priest_robe,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "priest_robe"),
            10,
            PriestArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .add(AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t1_spell_power)),
                    new ArmorSetConfig.Piece(3)
                            .add(AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t1_spell_power)),
                    new ArmorSetConfig.Piece(2)
                            .add(AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t1_spell_power)),
                    new ArmorSetConfig.Piece(1)
                            .add(AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t1_spell_power))
            ), 1)
            .armorSet();

    public static final Armor.Set priestArmorSet_t2 = create(
            prior_robe,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "prior_robe"),
            20,
            PriestArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t2_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t2_haste)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t2_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t2_haste)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t2_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t2_haste)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t2_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t2_haste)
                            ))
            ), 2)
            .armorSet();

    public static final Armor.Set priestArmorSet_t3 = create(
            netherite_prior_robe,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_prior_robe"),
            30,
            PriestArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t3_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t3_haste)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t3_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t3_haste)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t3_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t3_haste)
                            )),
                    new ArmorSetConfig.Piece(1)
                            .addAll(List.of(
                                    AttributeModifier.multiply(SpellSchools.HEALING.id, priest_t3_spell_power),
                                    AttributeModifier.multiply(SpellPowerMechanics.HASTE.id, priest_t3_haste)
                            ))
            ), 3)
            .armorSet();

    public static void register(Map<String, ArmorSetConfig> configs) {
        Armor.register(configs, entries, Group.KEY);
    }

    public static void init(IEventBus modEventBus) {
        register(PaladinsMod.itemConfig.armor_sets);
    }
}
