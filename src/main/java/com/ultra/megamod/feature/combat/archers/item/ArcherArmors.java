package com.ultra.megamod.feature.combat.archers.item;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.feature.combat.archers.item.armor.ArcherArmor;
import com.ultra.megamod.feature.combat.archers.content.ArcherSounds;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.spellengine.api.config.ArmorSetConfig;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Armor;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArcherArmors {

    private static ResourceKey<EquipmentAsset> createAssetId(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(ArchersMod.ID, name));
    }

    public static ArmorMaterial material(String name,
                                                  int protectionHead, int protectionChest, int protectionLegs, int protectionFeet,
                                                  int enchantability) {
        return new ArmorMaterial(
                15, // durability
                Map.of(ArmorType.HELMET, protectionHead,
                       ArmorType.CHESTPLATE, protectionChest,
                       ArmorType.LEGGINGS, protectionLegs,
                       ArmorType.BOOTS, protectionFeet),
                enchantability,
                ArcherSounds.ARCHER_ARMOR_EQUIP.entry(),
                0, 0,
                ItemTags.REPAIRS_LEATHER_ARMOR,
                createAssetId(name)
        );
    }

    public static ArmorMaterial material_t1 = material(
            "archer_armor",
            2, 3, 3, 2,
            9);

    public static ArmorMaterial material_t2 = material(
            "ranger_armor",
            2, 3, 3, 2,
            10);

    public static ArmorMaterial material_t3 = material(
            "netherite_ranger_armor",
            2, 3, 3, 2,
            15);


    public static final ArrayList<Armor.Entry> entries = new ArrayList<>();
    private static Armor.Entry create(ArmorMaterial material, Identifier id, int durability, Armor.Set.ItemFactory factory, ArmorSetConfig defaults, int tier) {
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

    private static AttributeModifier damageMultiplier(float value) {
        return new AttributeModifier(
                EntityAttributes_RangedWeapon.DAMAGE.id.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static AttributeModifier hasteMultiplier(float value) {
        return new AttributeModifier(
                EntityAttributes_RangedWeapon.HASTE.id.toString(),
                value,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public static final float damage_T1 = 0.05F;
    public static final float haste_T2 = 0.03F;
    public static final float damage_T2 = 0.08F;
    public static final float haste_T3 = 0.04F;
    public static final float damage_T3 = 0.09F;

    public static final Armor.Set archerArmorSet_T1 = create(
            material_t1,
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "archer_armor"),
            15,
            ArcherArmor::archer,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(damage_T1)),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(damage_T1)),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(damage_T1)),
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(damage_T1))
            ),
            1)
            .armorSet();

    public static final Armor.Set archerArmorSet_T2 = create(
            material_t2,
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "ranger_armor"),
            25,
            ArcherArmor::ranger,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(damage_T2))
                            .add(hasteMultiplier(haste_T2)),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(damage_T2))
                            .add(hasteMultiplier(haste_T2)),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(damage_T2))
                            .add(hasteMultiplier(haste_T2)),
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(damage_T2))
                            .add(hasteMultiplier(haste_T2))
            ),
            2)
            .armorSet();

    public static final Armor.Set archerArmorSet_T3 = create(
            material_t3,
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "netherite_ranger_armor"),
            35,
            ArcherArmor::ranger,
            ArmorSetConfig.with(
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(damage_T3))
                            .add(hasteMultiplier(haste_T3)),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(damage_T3))
                            .add(hasteMultiplier(haste_T3)),
                    new ArmorSetConfig.Piece(3)
                            .add(damageMultiplier(damage_T3))
                            .add(hasteMultiplier(haste_T3)),
                    new ArmorSetConfig.Piece(2)
                            .add(damageMultiplier(damage_T3))
                            .add(hasteMultiplier(haste_T3))
            ),
            3)
            .armorSet();

    public static void register(IEventBus modEventBus, Map<String, ArmorSetConfig> configs) {
        Armor.register(configs, entries, Group.KEY);
    }
}
