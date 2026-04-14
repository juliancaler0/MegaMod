package com.ultra.megamod.feature.combat.rogues.item.armor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.RoguesMod;
import com.ultra.megamod.feature.combat.rogues.item.Group;
import com.ultra.megamod.lib.spellengine.api.config.ArmorSetConfig;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEngineAttributes;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
 * Rogue & Warrior armor sets — ported from {@code net.rogues.item.armor.RogueArmors}.
 *
 * <p>Six sets total × 4 pieces = 24 {@code _feet}-suffixed armor items registered
 * through SpellEngine's {@link Armor.Entry} factory so MODIFIER spell containers
 * (evasion, haste, damage multiplier, knockback, crit chance/damage, toughness)
 * feed the attribute resolver.</p>
 *
 * <ul>
 *   <li>Rogue T1 (leather-tier): rogue_armor</li>
 *   <li>Assassin T2 (medium): assassin_armor</li>
 *   <li>Netherite Assassin T3: netherite_assassin_armor</li>
 *   <li>Warrior T1 (iron-tier): warrior_armor</li>
 *   <li>Berserker T2 (diamond-tier): berserker_armor</li>
 *   <li>Netherite Berserker T3: netherite_berserker_armor</li>
 * </ul>
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

    // Materials — mirror Rogues ref values
    public static final ArmorMaterial material_rogue_t1 = material(
            "rogue_armor",
            1, 3, 3, 1,
            9,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR);

    public static final ArmorMaterial material_rogue_t2 = material(
            "assassin_armor",
            2, 4, 4, 2,
            10,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR);

    public static final ArmorMaterial material_rogue_t3 = material(
            "netherite_assassin_armor",
            2, 4, 4, 2,
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE, ItemTags.REPAIRS_NETHERITE_ARMOR);

    public static final ArmorMaterial material_warrior_t1 = material(
            "warrior_armor",
            2, 5, 4, 1,
            9,
            SoundEvents.ARMOR_EQUIP_IRON, ItemTags.REPAIRS_IRON_ARMOR);

    public static final ArmorMaterial material_warrior_t2 = material(
            "berserker_armor",
            3, 8, 6, 2,
            10,
            SoundEvents.ARMOR_EQUIP_IRON, ItemTags.REPAIRS_IRON_ARMOR);

    public static final ArmorMaterial material_warrior_t3 = material(
            "netherite_berserker_armor",
            3, 8, 6, 2,
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE, ItemTags.REPAIRS_NETHERITE_ARMOR);

    // Attribute modifier IDs — vanilla generic + crit strike
    private static final Identifier ATTACK_DAMAGE_ID = Identifier.withDefaultNamespace("generic.attack_damage");
    private static final Identifier ATTACK_SPEED_ID = Identifier.withDefaultNamespace("generic.attack_speed");
    private static final Identifier KNOCKBACK_ID = Identifier.withDefaultNamespace("generic.knockback_resistance");
    private static final Identifier ARMOR_TOUGHNESS_ID = Identifier.withDefaultNamespace("generic.armor_toughness");
    private static final String CRIT_MOD_ID = "critical_strike";
    private static final Identifier CRIT_CHANCE_ID = Identifier.fromNamespaceAndPath(CRIT_MOD_ID, "chance");
    private static final Identifier CRIT_DAMAGE_ID = Identifier.fromNamespaceAndPath(CRIT_MOD_ID, "damage");

    private static AttributeModifier damageMultiplier(float value) {
        return new AttributeModifier(
                ATTACK_DAMAGE_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifier hasteMultiplier(float value) {
        return new AttributeModifier(
                ATTACK_SPEED_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifier knockbackBonus(float value) {
        return new AttributeModifier(
                KNOCKBACK_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
    }

    private static AttributeModifier evasionBonus(float value) {
        return new AttributeModifier(
                SpellEngineAttributes.EVASION_CHANCE.id.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifier toughnessBonus(float value) {
        return new AttributeModifier(
                ARMOR_TOUGHNESS_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
    }

    private static AttributeModifier critChance(float value) {
        return new AttributeModifier(
                CRIT_CHANCE_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifier critDamage(float value) {
        return new AttributeModifier(
                CRIT_DAMAGE_ID.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    // Tier constants (from Rogues ref)
    public static final float rogue_t1_evasion = 0.03F;
    public static final float rogue_t1_haste = 0.04F;

    public static final float rogue_t2_evasion = 0.04F;
    public static final float rogue_t2_haste = 0.05F;
    public static final float rogue_t2_damage = 0.02F;
    public static final float rogue_t2_crit_chance = 0.02F;

    public static final float rogue_t3_evasion = 0.05F;
    public static final float rogue_t3_haste = 0.05F;
    public static final float rogue_t3_damage = 0.05F;
    public static final float rogue_t3_crit_chance = 0.025F;

    public static final float warrior_t1_damage = 0.04F;

    public static final float warrior_t2_damage = 0.05F;
    public static final float warrior_t2_knockback = 0.1F;
    public static final float warrior_t2_crit_damage = 0.04F;

    public static final float warrior_t3_damage = 0.05F;
    public static final float warrior_t3_toughness = 1F;
    public static final float warrior_t3_knockback = 0.1F;
    public static final float warrior_t3_crit_damage = 0.05F;

    // Armor sets
    public static final Armor.Set rogueArmorSet = create(
            material_rogue_t1,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "rogue_armor"),
            15,
            RogueArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(1)
                            .add(evasionBonus(rogue_t1_evasion))
                            .add(hasteMultiplier(rogue_t1_haste)),
                    new ArmorSetConfig.Piece(3)
                            .add(evasionBonus(rogue_t1_evasion))
                            .add(hasteMultiplier(rogue_t1_haste)),
                    new ArmorSetConfig.Piece(3)
                            .add(evasionBonus(rogue_t1_evasion))
                            .add(hasteMultiplier(rogue_t1_haste)),
                    new ArmorSetConfig.Piece(1)
                            .add(evasionBonus(rogue_t1_evasion))
                            .add(hasteMultiplier(rogue_t1_haste))
            ), 1)
            .armorSet();

    public static final Armor.Set assassinArmorSet = create(
            material_rogue_t2,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "assassin_armor"),
            25,
            RogueArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .add(evasionBonus(rogue_t2_evasion))
                            .add(hasteMultiplier(rogue_t2_haste))
                            .add(damageMultiplier(rogue_t2_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t2_evasion),
                                    hasteMultiplier(rogue_t2_haste),
                                    critChance(rogue_t2_crit_chance)
                            )),
                    new ArmorSetConfig.Piece(4)
                            .add(evasionBonus(rogue_t2_evasion))
                            .add(hasteMultiplier(rogue_t2_haste))
                            .add(damageMultiplier(rogue_t2_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t2_evasion),
                                    hasteMultiplier(rogue_t2_haste),
                                    critChance(rogue_t2_crit_chance)
                            )),
                    new ArmorSetConfig.Piece(4)
                            .add(evasionBonus(rogue_t2_evasion))
                            .add(hasteMultiplier(rogue_t2_haste))
                            .add(damageMultiplier(rogue_t2_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t2_evasion),
                                    hasteMultiplier(rogue_t2_haste),
                                    critChance(rogue_t2_crit_chance)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .add(evasionBonus(rogue_t2_evasion))
                            .add(hasteMultiplier(rogue_t2_haste))
                            .add(damageMultiplier(rogue_t2_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t2_evasion),
                                    hasteMultiplier(rogue_t2_haste),
                                    critChance(rogue_t2_crit_chance)
                            ))
            ), 2)
            .armorSet();

    public static final Armor.Set netheriteAssassinArmorSet = create(
            material_rogue_t3,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_assassin_armor"),
            37,
            RogueArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .add(evasionBonus(rogue_t3_evasion))
                            .add(hasteMultiplier(rogue_t3_haste))
                            .add(damageMultiplier(rogue_t3_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t3_evasion),
                                    hasteMultiplier(rogue_t3_haste),
                                    critChance(rogue_t3_crit_chance)
                            )),
                    new ArmorSetConfig.Piece(4)
                            .add(evasionBonus(rogue_t3_evasion))
                            .add(hasteMultiplier(rogue_t3_haste))
                            .add(damageMultiplier(rogue_t3_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t3_evasion),
                                    hasteMultiplier(rogue_t3_haste),
                                    critChance(rogue_t3_crit_chance)
                            )),
                    new ArmorSetConfig.Piece(4)
                            .add(evasionBonus(rogue_t3_evasion))
                            .add(hasteMultiplier(rogue_t3_haste))
                            .add(damageMultiplier(rogue_t3_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t3_evasion),
                                    hasteMultiplier(rogue_t3_haste),
                                    critChance(rogue_t3_crit_chance)
                            )),
                    new ArmorSetConfig.Piece(2)
                            .add(evasionBonus(rogue_t3_evasion))
                            .add(hasteMultiplier(rogue_t3_haste))
                            .add(damageMultiplier(rogue_t3_damage))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    evasionBonus(rogue_t3_evasion),
                                    hasteMultiplier(rogue_t3_haste),
                                    critChance(rogue_t3_crit_chance)
                            ))
            ), 3)
            .armorSet();

    public static final Armor.Set warriorArmorSet = create(
            material_warrior_t1,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "warrior_armor"),
            15,
            WarriorArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(warrior_t1_damage)),
                    new ArmorSetConfig.Piece(5)
                            .add(damageMultiplier(warrior_t1_damage)),
                    new ArmorSetConfig.Piece(4)
                            .add(damageMultiplier(warrior_t1_damage)),
                    new ArmorSetConfig.Piece(1)
                            .add(damageMultiplier(warrior_t1_damage))
            ), 1)
            .armorSet();

    public static final Armor.Set berserkerArmorSet = create(
            material_warrior_t2,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "berserker_armor"),
            25,
            WarriorArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(warrior_t2_damage))
                            .add(knockbackBonus(warrior_t2_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t2_damage),
                                    critDamage(warrior_t2_crit_damage)
                            )),
                    new ArmorSetConfig.Piece(8)
                            .add(damageMultiplier(warrior_t2_damage))
                            .add(knockbackBonus(warrior_t2_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t2_damage),
                                    critDamage(warrior_t2_crit_damage)
                            )),
                    new ArmorSetConfig.Piece(6)
                            .add(damageMultiplier(warrior_t2_damage))
                            .add(knockbackBonus(warrior_t2_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t2_damage),
                                    critDamage(warrior_t2_crit_damage)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(warrior_t2_damage))
                            .add(knockbackBonus(warrior_t2_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t2_damage),
                                    critDamage(warrior_t2_crit_damage)
                            ))
            ), 2)
            .armorSet();

    public static final Armor.Set netheriteBerserkerArmorSet = create(
            material_warrior_t3,
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "netherite_berserker_armor"),
            37,
            WarriorArmor::new,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(warrior_t3_damage))
                            .add(toughnessBonus(warrior_t3_toughness))
                            .add(knockbackBonus(warrior_t3_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t3_damage),
                                    toughnessBonus(warrior_t3_toughness),
                                    critDamage(warrior_t3_crit_damage)
                            )),
                    new ArmorSetConfig.Piece(8)
                            .add(damageMultiplier(warrior_t3_damage))
                            .add(toughnessBonus(warrior_t3_toughness))
                            .add(knockbackBonus(warrior_t3_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t3_damage),
                                    toughnessBonus(warrior_t3_toughness),
                                    critDamage(warrior_t3_crit_damage)
                            )),
                    new ArmorSetConfig.Piece(6)
                            .add(damageMultiplier(warrior_t3_damage))
                            .add(toughnessBonus(warrior_t3_toughness))
                            .add(knockbackBonus(warrior_t3_knockback))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t3_damage),
                                    toughnessBonus(warrior_t3_toughness),
                                    critDamage(warrior_t3_crit_damage)
                            )),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(warrior_t3_damage))
                            .add(toughnessBonus(warrior_t3_toughness))
                            .addConditional(CRIT_MOD_ID, List.of(
                                    damageMultiplier(warrior_t3_damage),
                                    toughnessBonus(warrior_t3_toughness),
                                    critDamage(warrior_t3_crit_damage)
                            ))
            ), 3)
            .armorSet();

    public static void register(Map<String, ArmorSetConfig> configs) {
        Armor.register(configs, entries, Group.KEY);
    }

    public static void init(IEventBus modEventBus) {
        register(RoguesMod.itemConfig.armor_sets);
    }
}
