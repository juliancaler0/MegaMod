package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.weapons.RpgArmorItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Class armor sets for the RPG combat system.
 * Each set has 4 pieces (head, chest, legs, boots).
 * Uses the Equippable component for slot assignment and rendering.
 * Armor and bonus stats are rolled via ArmorStatRoller on first inventory tick.
 * Set bonuses come from EquipmentSetManager.
 */
public class ClassArmorRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ─── Custom equipment asset keys (point to megamod equipment JSONs + textures) ───
    private static ResourceKey<EquipmentAsset> customAsset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MegaMod.MODID, name));
    }

    // Wizard Robes
    private static final ResourceKey<EquipmentAsset> WIZARD_ROBE_ASSET = customAsset("wizard_robe");
    private static final ResourceKey<EquipmentAsset> ARCANE_ROBE_ASSET = customAsset("arcane_robe");
    private static final ResourceKey<EquipmentAsset> FIRE_ROBE_ASSET = customAsset("fire_robe");
    private static final ResourceKey<EquipmentAsset> FROST_ROBE_ASSET = customAsset("frost_robe");
    private static final ResourceKey<EquipmentAsset> NETHERITE_ARCANE_ROBE_ASSET = customAsset("netherite_arcane_robe");
    private static final ResourceKey<EquipmentAsset> NETHERITE_FIRE_ROBE_ASSET = customAsset("netherite_fire_robe");
    private static final ResourceKey<EquipmentAsset> NETHERITE_FROST_ROBE_ASSET = customAsset("netherite_frost_robe");

    // Paladin Armor
    private static final ResourceKey<EquipmentAsset> PALADIN_ARMOR_ASSET = customAsset("paladin_armor");
    private static final ResourceKey<EquipmentAsset> CRUSADER_ARMOR_ASSET = customAsset("crusader_armor");
    private static final ResourceKey<EquipmentAsset> NETHERITE_CRUSADER_ARMOR_ASSET = customAsset("netherite_crusader_armor");

    // Priest Robes
    private static final ResourceKey<EquipmentAsset> PRIEST_ROBE_ASSET = customAsset("priest_robe");
    private static final ResourceKey<EquipmentAsset> PRIOR_ROBE_ASSET = customAsset("prior_robe");
    private static final ResourceKey<EquipmentAsset> NETHERITE_PRIOR_ROBE_ASSET = customAsset("netherite_prior_robe");

    // Rogue Armor
    private static final ResourceKey<EquipmentAsset> ROGUE_ARMOR_ASSET = customAsset("rogue_armor");
    private static final ResourceKey<EquipmentAsset> ASSASSIN_ARMOR_ASSET = customAsset("assassin_armor");
    private static final ResourceKey<EquipmentAsset> NETHERITE_ASSASSIN_ARMOR_ASSET = customAsset("netherite_assassin_armor");

    // Warrior Armor
    private static final ResourceKey<EquipmentAsset> WARRIOR_ARMOR_ASSET = customAsset("warrior_armor");
    private static final ResourceKey<EquipmentAsset> BERSERKER_ARMOR_ASSET = customAsset("berserker_armor");
    private static final ResourceKey<EquipmentAsset> NETHERITE_BERSERKER_ARMOR_ASSET = customAsset("netherite_berserker_armor");

    // Archer Armor
    private static final ResourceKey<EquipmentAsset> ARCHER_ARMOR_ASSET = customAsset("archer_armor");
    private static final ResourceKey<EquipmentAsset> RANGER_ARMOR_ASSET = customAsset("ranger_armor");
    private static final ResourceKey<EquipmentAsset> NETHERITE_RANGER_ARMOR_ASSET = customAsset("netherite_ranger_armor");

    // ─── Armor weight classes ───
    // Armor values: [head, chest, legs, boots]
    // Progression: T1 < T2 < T3 within each weight class.
    // Robes are leather-weight (spell-focused), Plate is heavy (warrior-focused),
    // Medium is balanced (rogue/archer-focused).

    // Robes — starter 7 total (leather), T2 chain-tier 11, T3 iron+ 14
    private static final int[] ROBE_T1 = {1, 3, 2, 1};     // 7 total (leather)
    private static final int[] ROBE_T2 = {2, 4, 3, 2};     // 11 total (chain+)
    private static final int[] ROBE_T3 = {2, 5, 4, 3};     // 14 total (iron+, netherite tier)

    // Plate — starter 15 total (iron), T2 diamond 20, T3 netherite+ 24
    private static final int[] PLATE_T1 = {2, 6, 5, 2};    // 15 total (iron)
    private static final int[] PLATE_T2 = {3, 8, 6, 3};    // 20 total (diamond)
    private static final int[] PLATE_T3 = {4, 9, 7, 4};    // 24 total (netherite+)

    // Medium — starter 11 total (chain), T2 iron+ 17, T3 diamond+ 20
    private static final int[] MEDIUM_T1 = {2, 4, 3, 2};   // 11 total (chain)
    private static final int[] MEDIUM_T2 = {3, 6, 5, 3};   // 17 total (iron+)
    private static final int[] MEDIUM_T3 = {3, 7, 6, 4};   // 20 total (diamond+)

    // T3 adds real netherite-tier toughness on top of the higher armor values
    private static final double T3_ROBE_TOUGHNESS = 1.0;
    private static final double T3_MEDIUM_TOUGHNESS = 1.5;
    private static final double T3_PLATE_TOUGHNESS = 2.0;

    // ─── Slot index constants ───
    private static final int HEAD = 0;
    private static final int CHEST = 1;
    private static final int LEGS = 2;
    private static final int BOOTS = 3;

    // ─── Helper to register RPG armor with rolled stats ───
    private static DeferredItem<RpgArmorItem> rpgArmor(String name, EquipmentSlot slot,
                                                        ResourceKey<EquipmentAsset> asset,
                                                        int[] armorValues, double toughness) {
        int slotIndex = switch (slot) {
            case HEAD -> HEAD;
            case CHEST -> CHEST;
            case LEGS -> LEGS;
            case FEET -> BOOTS;
            default -> 0;
        };
        double baseArmor = armorValues[slotIndex];
        EquipmentSlotGroup group = switch (slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ARMOR;
        };
        Identifier armorModId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor." + name);
        Identifier toughModId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor_toughness." + name);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
            .add(Attributes.ARMOR,
                new AttributeModifier(armorModId, baseArmor, AttributeModifier.Operation.ADD_VALUE),
                group);
        if (toughness > 0) {
            attrBuilder.add(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(toughModId, toughness, AttributeModifier.Operation.ADD_VALUE),
                group);
        }
        ItemAttributeModifiers defaultAttrs = attrBuilder.build();
        return ITEMS.registerItem(name,
            props -> new RpgArmorItem(baseArmor, toughness, slot, (Item.Properties) props),
            () -> new Item.Properties().stacksTo(1)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(slot).setAsset(asset).build())
                .component(DataComponents.ATTRIBUTE_MODIFIERS, defaultAttrs));
    }

    // ═══════════════════════════════════════════════════════════════
    // WIZARD ROBES (7 sets) — Leather-weight, spell-focused
    // ═══════════════════════════════════════════════════════════════

    // --- Wizard Robe (starter, T1 robe) ---
    public static final DeferredItem<RpgArmorItem> WIZARD_ROBE_HEAD = rpgArmor("wizard_robe_head", EquipmentSlot.HEAD, WIZARD_ROBE_ASSET, ROBE_T1, 0);
    public static final DeferredItem<RpgArmorItem> WIZARD_ROBE_CHEST = rpgArmor("wizard_robe_chest", EquipmentSlot.CHEST, WIZARD_ROBE_ASSET, ROBE_T1, 0);
    public static final DeferredItem<RpgArmorItem> WIZARD_ROBE_LEGS = rpgArmor("wizard_robe_legs", EquipmentSlot.LEGS, WIZARD_ROBE_ASSET, ROBE_T1, 0);
    public static final DeferredItem<RpgArmorItem> WIZARD_ROBE_BOOTS = rpgArmor("wizard_robe_boots", EquipmentSlot.FEET, WIZARD_ROBE_ASSET, ROBE_T1, 0);

    // --- Arcane Robe (T2 robe) ---
    public static final DeferredItem<RpgArmorItem> ARCANE_ROBE_HEAD = rpgArmor("arcane_robe_head", EquipmentSlot.HEAD, ARCANE_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> ARCANE_ROBE_CHEST = rpgArmor("arcane_robe_chest", EquipmentSlot.CHEST, ARCANE_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> ARCANE_ROBE_LEGS = rpgArmor("arcane_robe_legs", EquipmentSlot.LEGS, ARCANE_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> ARCANE_ROBE_BOOTS = rpgArmor("arcane_robe_boots", EquipmentSlot.FEET, ARCANE_ROBE_ASSET, ROBE_T2, 0);

    // --- Fire Robe (T2 robe) ---
    public static final DeferredItem<RpgArmorItem> FIRE_ROBE_HEAD = rpgArmor("fire_robe_head", EquipmentSlot.HEAD, FIRE_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> FIRE_ROBE_CHEST = rpgArmor("fire_robe_chest", EquipmentSlot.CHEST, FIRE_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> FIRE_ROBE_LEGS = rpgArmor("fire_robe_legs", EquipmentSlot.LEGS, FIRE_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> FIRE_ROBE_BOOTS = rpgArmor("fire_robe_boots", EquipmentSlot.FEET, FIRE_ROBE_ASSET, ROBE_T2, 0);

    // --- Frost Robe (T2 robe) ---
    public static final DeferredItem<RpgArmorItem> FROST_ROBE_HEAD = rpgArmor("frost_robe_head", EquipmentSlot.HEAD, FROST_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> FROST_ROBE_CHEST = rpgArmor("frost_robe_chest", EquipmentSlot.CHEST, FROST_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> FROST_ROBE_LEGS = rpgArmor("frost_robe_legs", EquipmentSlot.LEGS, FROST_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> FROST_ROBE_BOOTS = rpgArmor("frost_robe_boots", EquipmentSlot.FEET, FROST_ROBE_ASSET, ROBE_T2, 0);

    // --- Netherite Arcane Robe (T3 robe) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_ARCANE_ROBE_HEAD = rpgArmor("netherite_arcane_robe_head", EquipmentSlot.HEAD, NETHERITE_ARCANE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_ARCANE_ROBE_CHEST = rpgArmor("netherite_arcane_robe_chest", EquipmentSlot.CHEST, NETHERITE_ARCANE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_ARCANE_ROBE_LEGS = rpgArmor("netherite_arcane_robe_legs", EquipmentSlot.LEGS, NETHERITE_ARCANE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_ARCANE_ROBE_BOOTS = rpgArmor("netherite_arcane_robe_boots", EquipmentSlot.FEET, NETHERITE_ARCANE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);

    // --- Netherite Fire Robe (T3 robe) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_FIRE_ROBE_HEAD = rpgArmor("netherite_fire_robe_head", EquipmentSlot.HEAD, NETHERITE_FIRE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_FIRE_ROBE_CHEST = rpgArmor("netherite_fire_robe_chest", EquipmentSlot.CHEST, NETHERITE_FIRE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_FIRE_ROBE_LEGS = rpgArmor("netherite_fire_robe_legs", EquipmentSlot.LEGS, NETHERITE_FIRE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_FIRE_ROBE_BOOTS = rpgArmor("netherite_fire_robe_boots", EquipmentSlot.FEET, NETHERITE_FIRE_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);

    // --- Netherite Frost Robe (T3 robe) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_FROST_ROBE_HEAD = rpgArmor("netherite_frost_robe_head", EquipmentSlot.HEAD, NETHERITE_FROST_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_FROST_ROBE_CHEST = rpgArmor("netherite_frost_robe_chest", EquipmentSlot.CHEST, NETHERITE_FROST_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_FROST_ROBE_LEGS = rpgArmor("netherite_frost_robe_legs", EquipmentSlot.LEGS, NETHERITE_FROST_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_FROST_ROBE_BOOTS = rpgArmor("netherite_frost_robe_boots", EquipmentSlot.FEET, NETHERITE_FROST_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);

    // ═══════════════════════════════════════════════════════════════
    // PALADIN ARMOR (3 sets) — Heavy plate, holy warrior
    // ═══════════════════════════════════════════════════════════════

    // --- Paladin Armor (starter, T1 plate) ---
    public static final DeferredItem<RpgArmorItem> PALADIN_ARMOR_HEAD = rpgArmor("paladin_armor_head", EquipmentSlot.HEAD, PALADIN_ARMOR_ASSET, PLATE_T1, 0);
    public static final DeferredItem<RpgArmorItem> PALADIN_ARMOR_CHEST = rpgArmor("paladin_armor_chest", EquipmentSlot.CHEST, PALADIN_ARMOR_ASSET, PLATE_T1, 0);
    public static final DeferredItem<RpgArmorItem> PALADIN_ARMOR_LEGS = rpgArmor("paladin_armor_legs", EquipmentSlot.LEGS, PALADIN_ARMOR_ASSET, PLATE_T1, 0);
    public static final DeferredItem<RpgArmorItem> PALADIN_ARMOR_BOOTS = rpgArmor("paladin_armor_boots", EquipmentSlot.FEET, PALADIN_ARMOR_ASSET, PLATE_T1, 0);

    // --- Crusader Armor (T2 plate) ---
    public static final DeferredItem<RpgArmorItem> CRUSADER_ARMOR_HEAD = rpgArmor("crusader_armor_head", EquipmentSlot.HEAD, CRUSADER_ARMOR_ASSET, PLATE_T2, 0);
    public static final DeferredItem<RpgArmorItem> CRUSADER_ARMOR_CHEST = rpgArmor("crusader_armor_chest", EquipmentSlot.CHEST, CRUSADER_ARMOR_ASSET, PLATE_T2, 0);
    public static final DeferredItem<RpgArmorItem> CRUSADER_ARMOR_LEGS = rpgArmor("crusader_armor_legs", EquipmentSlot.LEGS, CRUSADER_ARMOR_ASSET, PLATE_T2, 0);
    public static final DeferredItem<RpgArmorItem> CRUSADER_ARMOR_BOOTS = rpgArmor("crusader_armor_boots", EquipmentSlot.FEET, CRUSADER_ARMOR_ASSET, PLATE_T2, 0);

    // --- Netherite Crusader Armor (T3 plate) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_CRUSADER_ARMOR_HEAD = rpgArmor("netherite_crusader_armor_head", EquipmentSlot.HEAD, NETHERITE_CRUSADER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_CRUSADER_ARMOR_CHEST = rpgArmor("netherite_crusader_armor_chest", EquipmentSlot.CHEST, NETHERITE_CRUSADER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_CRUSADER_ARMOR_LEGS = rpgArmor("netherite_crusader_armor_legs", EquipmentSlot.LEGS, NETHERITE_CRUSADER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_CRUSADER_ARMOR_BOOTS = rpgArmor("netherite_crusader_armor_boots", EquipmentSlot.FEET, NETHERITE_CRUSADER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);

    // ═══════════════════════════════════════════════════════════════
    // PRIEST ROBES (3 sets) — Light-weight, healing focused
    // ═══════════════════════════════════════════════════════════════

    // --- Priest Robe (starter, T1 robe) ---
    public static final DeferredItem<RpgArmorItem> PRIEST_ROBE_HEAD = rpgArmor("priest_robe_head", EquipmentSlot.HEAD, PRIEST_ROBE_ASSET, ROBE_T1, 0);
    public static final DeferredItem<RpgArmorItem> PRIEST_ROBE_CHEST = rpgArmor("priest_robe_chest", EquipmentSlot.CHEST, PRIEST_ROBE_ASSET, ROBE_T1, 0);
    public static final DeferredItem<RpgArmorItem> PRIEST_ROBE_LEGS = rpgArmor("priest_robe_legs", EquipmentSlot.LEGS, PRIEST_ROBE_ASSET, ROBE_T1, 0);
    public static final DeferredItem<RpgArmorItem> PRIEST_ROBE_BOOTS = rpgArmor("priest_robe_boots", EquipmentSlot.FEET, PRIEST_ROBE_ASSET, ROBE_T1, 0);

    // --- Prior Robe (T2 robe) ---
    public static final DeferredItem<RpgArmorItem> PRIOR_ROBE_HEAD = rpgArmor("prior_robe_head", EquipmentSlot.HEAD, PRIOR_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> PRIOR_ROBE_CHEST = rpgArmor("prior_robe_chest", EquipmentSlot.CHEST, PRIOR_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> PRIOR_ROBE_LEGS = rpgArmor("prior_robe_legs", EquipmentSlot.LEGS, PRIOR_ROBE_ASSET, ROBE_T2, 0);
    public static final DeferredItem<RpgArmorItem> PRIOR_ROBE_BOOTS = rpgArmor("prior_robe_boots", EquipmentSlot.FEET, PRIOR_ROBE_ASSET, ROBE_T2, 0);

    // --- Netherite Prior Robe (T3 robe) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_PRIOR_ROBE_HEAD = rpgArmor("netherite_prior_robe_head", EquipmentSlot.HEAD, NETHERITE_PRIOR_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_PRIOR_ROBE_CHEST = rpgArmor("netherite_prior_robe_chest", EquipmentSlot.CHEST, NETHERITE_PRIOR_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_PRIOR_ROBE_LEGS = rpgArmor("netherite_prior_robe_legs", EquipmentSlot.LEGS, NETHERITE_PRIOR_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_PRIOR_ROBE_BOOTS = rpgArmor("netherite_prior_robe_boots", EquipmentSlot.FEET, NETHERITE_PRIOR_ROBE_ASSET, ROBE_T3, T3_ROBE_TOUGHNESS);

    // ═══════════════════════════════════════════════════════════════
    // ROGUE ARMOR (3 sets) — Light leather, agility focused
    // ═══════════════════════════════════════════════════════════════

    // --- Rogue Armor (starter, T1 medium) ---
    public static final DeferredItem<RpgArmorItem> ROGUE_ARMOR_HEAD = rpgArmor("rogue_armor_head", EquipmentSlot.HEAD, ROGUE_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ROGUE_ARMOR_CHEST = rpgArmor("rogue_armor_chest", EquipmentSlot.CHEST, ROGUE_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ROGUE_ARMOR_LEGS = rpgArmor("rogue_armor_legs", EquipmentSlot.LEGS, ROGUE_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ROGUE_ARMOR_BOOTS = rpgArmor("rogue_armor_boots", EquipmentSlot.FEET, ROGUE_ARMOR_ASSET, MEDIUM_T1, 0);

    // --- Assassin Armor (T2 medium) ---
    public static final DeferredItem<RpgArmorItem> ASSASSIN_ARMOR_HEAD = rpgArmor("assassin_armor_head", EquipmentSlot.HEAD, ASSASSIN_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> ASSASSIN_ARMOR_CHEST = rpgArmor("assassin_armor_chest", EquipmentSlot.CHEST, ASSASSIN_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> ASSASSIN_ARMOR_LEGS = rpgArmor("assassin_armor_legs", EquipmentSlot.LEGS, ASSASSIN_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> ASSASSIN_ARMOR_BOOTS = rpgArmor("assassin_armor_boots", EquipmentSlot.FEET, ASSASSIN_ARMOR_ASSET, MEDIUM_T2, 0);

    // --- Netherite Assassin Armor (T3 medium) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_ARMOR_HEAD = rpgArmor("netherite_assassin_armor_head", EquipmentSlot.HEAD, NETHERITE_ASSASSIN_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_ARMOR_CHEST = rpgArmor("netherite_assassin_armor_chest", EquipmentSlot.CHEST, NETHERITE_ASSASSIN_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_ARMOR_LEGS = rpgArmor("netherite_assassin_armor_legs", EquipmentSlot.LEGS, NETHERITE_ASSASSIN_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_ASSASSIN_ARMOR_BOOTS = rpgArmor("netherite_assassin_armor_boots", EquipmentSlot.FEET, NETHERITE_ASSASSIN_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);

    // ═══════════════════════════════════════════════════════════════
    // WARRIOR ARMOR (3 sets) — Heavy plate, melee focused
    // ═══════════════════════════════════════════════════════════════

    // --- Warrior Armor (starter, T1 plate) ---
    public static final DeferredItem<RpgArmorItem> WARRIOR_ARMOR_HEAD = rpgArmor("warrior_armor_head", EquipmentSlot.HEAD, WARRIOR_ARMOR_ASSET, PLATE_T1, 0);
    public static final DeferredItem<RpgArmorItem> WARRIOR_ARMOR_CHEST = rpgArmor("warrior_armor_chest", EquipmentSlot.CHEST, WARRIOR_ARMOR_ASSET, PLATE_T1, 0);
    public static final DeferredItem<RpgArmorItem> WARRIOR_ARMOR_LEGS = rpgArmor("warrior_armor_legs", EquipmentSlot.LEGS, WARRIOR_ARMOR_ASSET, PLATE_T1, 0);
    public static final DeferredItem<RpgArmorItem> WARRIOR_ARMOR_BOOTS = rpgArmor("warrior_armor_boots", EquipmentSlot.FEET, WARRIOR_ARMOR_ASSET, PLATE_T1, 0);

    // --- Berserker Armor (T2 plate) ---
    public static final DeferredItem<RpgArmorItem> BERSERKER_ARMOR_HEAD = rpgArmor("berserker_armor_head", EquipmentSlot.HEAD, BERSERKER_ARMOR_ASSET, PLATE_T2, 0);
    public static final DeferredItem<RpgArmorItem> BERSERKER_ARMOR_CHEST = rpgArmor("berserker_armor_chest", EquipmentSlot.CHEST, BERSERKER_ARMOR_ASSET, PLATE_T2, 0);
    public static final DeferredItem<RpgArmorItem> BERSERKER_ARMOR_LEGS = rpgArmor("berserker_armor_legs", EquipmentSlot.LEGS, BERSERKER_ARMOR_ASSET, PLATE_T2, 0);
    public static final DeferredItem<RpgArmorItem> BERSERKER_ARMOR_BOOTS = rpgArmor("berserker_armor_boots", EquipmentSlot.FEET, BERSERKER_ARMOR_ASSET, PLATE_T2, 0);

    // --- Netherite Berserker Armor (T3 plate) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_ARMOR_HEAD = rpgArmor("netherite_berserker_armor_head", EquipmentSlot.HEAD, NETHERITE_BERSERKER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_ARMOR_CHEST = rpgArmor("netherite_berserker_armor_chest", EquipmentSlot.CHEST, NETHERITE_BERSERKER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_ARMOR_LEGS = rpgArmor("netherite_berserker_armor_legs", EquipmentSlot.LEGS, NETHERITE_BERSERKER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_BERSERKER_ARMOR_BOOTS = rpgArmor("netherite_berserker_armor_boots", EquipmentSlot.FEET, NETHERITE_BERSERKER_ARMOR_ASSET, PLATE_T3, T3_PLATE_TOUGHNESS);

    // ═══════════════════════════════════════════════════════════════
    // ARCHER ARMOR (3 sets) — Light leather, ranged focused
    // ═══════════════════════════════════════════════════════════════

    // --- Archer Armor (starter, T1 medium) ---
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_HEAD = rpgArmor("archer_armor_head", EquipmentSlot.HEAD, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_CHEST = rpgArmor("archer_armor_chest", EquipmentSlot.CHEST, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_LEGS = rpgArmor("archer_armor_legs", EquipmentSlot.LEGS, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_BOOTS = rpgArmor("archer_armor_boots", EquipmentSlot.FEET, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);

    // --- Ranger Armor (T2 medium) ---
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_HEAD = rpgArmor("ranger_armor_head", EquipmentSlot.HEAD, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_CHEST = rpgArmor("ranger_armor_chest", EquipmentSlot.CHEST, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_LEGS = rpgArmor("ranger_armor_legs", EquipmentSlot.LEGS, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_BOOTS = rpgArmor("ranger_armor_boots", EquipmentSlot.FEET, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);

    // --- Netherite Ranger Armor (T3 medium) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_HEAD = rpgArmor("netherite_ranger_armor_head", EquipmentSlot.HEAD, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_CHEST = rpgArmor("netherite_ranger_armor_chest", EquipmentSlot.CHEST, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_LEGS = rpgArmor("netherite_ranger_armor_legs", EquipmentSlot.LEGS, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_BOOTS = rpgArmor("netherite_ranger_armor_boots", EquipmentSlot.FEET, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static java.util.List<net.minecraft.world.item.Item> getTier1Items() {
        return java.util.List.of(
            WIZARD_ROBE_HEAD.get(), WIZARD_ROBE_CHEST.get(), WIZARD_ROBE_LEGS.get(), WIZARD_ROBE_BOOTS.get(),
            PALADIN_ARMOR_HEAD.get(), PALADIN_ARMOR_CHEST.get(), PALADIN_ARMOR_LEGS.get(), PALADIN_ARMOR_BOOTS.get(),
            PRIEST_ROBE_HEAD.get(), PRIEST_ROBE_CHEST.get(), PRIEST_ROBE_LEGS.get(), PRIEST_ROBE_BOOTS.get(),
            ROGUE_ARMOR_HEAD.get(), ROGUE_ARMOR_CHEST.get(), ROGUE_ARMOR_LEGS.get(), ROGUE_ARMOR_BOOTS.get(),
            WARRIOR_ARMOR_HEAD.get(), WARRIOR_ARMOR_CHEST.get(), WARRIOR_ARMOR_LEGS.get(), WARRIOR_ARMOR_BOOTS.get(),
            ARCHER_ARMOR_HEAD.get(), ARCHER_ARMOR_CHEST.get(), ARCHER_ARMOR_LEGS.get(), ARCHER_ARMOR_BOOTS.get()
        );
    }

    public static java.util.List<net.minecraft.world.item.Item> getTier2Items() {
        return java.util.List.of(
            ARCANE_ROBE_HEAD.get(), ARCANE_ROBE_CHEST.get(), ARCANE_ROBE_LEGS.get(), ARCANE_ROBE_BOOTS.get(),
            FIRE_ROBE_HEAD.get(), FIRE_ROBE_CHEST.get(), FIRE_ROBE_LEGS.get(), FIRE_ROBE_BOOTS.get(),
            FROST_ROBE_HEAD.get(), FROST_ROBE_CHEST.get(), FROST_ROBE_LEGS.get(), FROST_ROBE_BOOTS.get(),
            CRUSADER_ARMOR_HEAD.get(), CRUSADER_ARMOR_CHEST.get(), CRUSADER_ARMOR_LEGS.get(), CRUSADER_ARMOR_BOOTS.get(),
            PRIOR_ROBE_HEAD.get(), PRIOR_ROBE_CHEST.get(), PRIOR_ROBE_LEGS.get(), PRIOR_ROBE_BOOTS.get(),
            ASSASSIN_ARMOR_HEAD.get(), ASSASSIN_ARMOR_CHEST.get(), ASSASSIN_ARMOR_LEGS.get(), ASSASSIN_ARMOR_BOOTS.get(),
            BERSERKER_ARMOR_HEAD.get(), BERSERKER_ARMOR_CHEST.get(), BERSERKER_ARMOR_LEGS.get(), BERSERKER_ARMOR_BOOTS.get(),
            RANGER_ARMOR_HEAD.get(), RANGER_ARMOR_CHEST.get(), RANGER_ARMOR_LEGS.get(), RANGER_ARMOR_BOOTS.get()
        );
    }

    public static java.util.List<net.minecraft.world.item.Item> getTier3Items() {
        return java.util.List.of(
            NETHERITE_ARCANE_ROBE_HEAD.get(), NETHERITE_ARCANE_ROBE_CHEST.get(), NETHERITE_ARCANE_ROBE_LEGS.get(), NETHERITE_ARCANE_ROBE_BOOTS.get(),
            NETHERITE_FIRE_ROBE_HEAD.get(), NETHERITE_FIRE_ROBE_CHEST.get(), NETHERITE_FIRE_ROBE_LEGS.get(), NETHERITE_FIRE_ROBE_BOOTS.get(),
            NETHERITE_FROST_ROBE_HEAD.get(), NETHERITE_FROST_ROBE_CHEST.get(), NETHERITE_FROST_ROBE_LEGS.get(), NETHERITE_FROST_ROBE_BOOTS.get(),
            NETHERITE_CRUSADER_ARMOR_HEAD.get(), NETHERITE_CRUSADER_ARMOR_CHEST.get(), NETHERITE_CRUSADER_ARMOR_LEGS.get(), NETHERITE_CRUSADER_ARMOR_BOOTS.get(),
            NETHERITE_PRIOR_ROBE_HEAD.get(), NETHERITE_PRIOR_ROBE_CHEST.get(), NETHERITE_PRIOR_ROBE_LEGS.get(), NETHERITE_PRIOR_ROBE_BOOTS.get(),
            NETHERITE_ASSASSIN_ARMOR_HEAD.get(), NETHERITE_ASSASSIN_ARMOR_CHEST.get(), NETHERITE_ASSASSIN_ARMOR_LEGS.get(), NETHERITE_ASSASSIN_ARMOR_BOOTS.get(),
            NETHERITE_BERSERKER_ARMOR_HEAD.get(), NETHERITE_BERSERKER_ARMOR_CHEST.get(), NETHERITE_BERSERKER_ARMOR_LEGS.get(), NETHERITE_BERSERKER_ARMOR_BOOTS.get(),
            NETHERITE_RANGER_ARMOR_HEAD.get(), NETHERITE_RANGER_ARMOR_CHEST.get(), NETHERITE_RANGER_ARMOR_LEGS.get(), NETHERITE_RANGER_ARMOR_BOOTS.get()
        );
    }
}
