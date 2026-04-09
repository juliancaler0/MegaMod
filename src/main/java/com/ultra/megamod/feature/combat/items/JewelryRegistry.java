package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.weapons.RpgJewelryItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Jewelry items for the RPG combat system.
 * Rings and necklaces that go in accessory slots and provide attribute bonuses.
 */
public class JewelryRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ═══════════════════════════════════════════════════════════════
    // RAW GEM ITEMS — crafting materials for gem jewelry
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> RUBY = ITEMS.registerSimpleItem("ruby");
    public static final DeferredItem<Item> TOPAZ = ITEMS.registerSimpleItem("topaz");
    public static final DeferredItem<Item> CITRINE = ITEMS.registerSimpleItem("citrine");
    public static final DeferredItem<Item> JADE = ITEMS.registerSimpleItem("jade");
    public static final DeferredItem<Item> SAPPHIRE = ITEMS.registerSimpleItem("sapphire");
    public static final DeferredItem<Item> TANZANITE = ITEMS.registerSimpleItem("tanzanite");

    // ═══════════════════════════════════════════════════════════════
    // BASIC RINGS (Tier 0) — simple metal bands
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> COPPER_RING = ITEMS.registerItem("copper_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ARMOR, "copper_ring.armor", 0.5, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> IRON_RING = ITEMS.registerItem("iron_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ARMOR, "iron_ring.armor", 1.0, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> GOLD_RING = ITEMS.registerItem("gold_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // GEM RINGS (Tier 2) — gemstone-set rings, 4% bonuses
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> RUBY_RING = ITEMS.registerItem("ruby_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_DAMAGE, "ruby_ring.attack_damage", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> TOPAZ_RING = ITEMS.registerItem("topaz_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "topaz_ring.arcane_power", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "topaz_ring.fire_damage_bonus", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> CITRINE_RING = ITEMS.registerItem("citrine_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.HEALING_POWER, "citrine_ring.healing_power", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.LIGHTNING_DAMAGE_BONUS, "citrine_ring.lightning_damage_bonus", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> JADE_RING = ITEMS.registerItem("jade_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(MegaModAttributes.RANGED_DAMAGE, "jade_ring.ranged_damage", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> SAPPHIRE_RING = ITEMS.registerItem("sapphire_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.MAX_HEALTH, "sapphire_ring.max_health", 2.0, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> TANZANITE_RING = ITEMS.registerItem("tanzanite_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "tanzanite_ring.ice_damage_bonus", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SOUL_POWER, "tanzanite_ring.soul_power", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    // ═══════════════════════════════════════════════════════════════
    // GEM NECKLACES (Tier 2) — gemstone pendants, same bonuses as rings
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> RUBY_NECKLACE = ITEMS.registerItem("ruby_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_DAMAGE, "ruby_necklace.attack_damage", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> TOPAZ_NECKLACE = ITEMS.registerItem("topaz_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "topaz_necklace.arcane_power", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "topaz_necklace.fire_damage_bonus", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> CITRINE_NECKLACE = ITEMS.registerItem("citrine_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.HEALING_POWER, "citrine_necklace.healing_power", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.LIGHTNING_DAMAGE_BONUS, "citrine_necklace.lightning_damage_bonus", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> JADE_NECKLACE = ITEMS.registerItem("jade_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(MegaModAttributes.RANGED_DAMAGE, "jade_necklace.ranged_damage", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> SAPPHIRE_NECKLACE = ITEMS.registerItem("sapphire_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.MAX_HEALTH, "sapphire_necklace.max_health", 2.0, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> TANZANITE_NECKLACE = ITEMS.registerItem("tanzanite_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "tanzanite_necklace.ice_damage_bonus", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SOUL_POWER, "tanzanite_necklace.soul_power", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    // ═══════════════════════════════════════════════════════════════
    // NETHERITE GEM RINGS (Tier 3) — netherite-framed gemstone rings, 8% bonuses
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> NETHERITE_RUBY_RING = ITEMS.registerItem("netherite_ruby_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_DAMAGE, "netherite_ruby_ring.attack_damage", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> NETHERITE_TOPAZ_RING = ITEMS.registerItem("netherite_topaz_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "netherite_topaz_ring.arcane_power", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "netherite_topaz_ring.fire_damage_bonus", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> NETHERITE_CITRINE_RING = ITEMS.registerItem("netherite_citrine_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.HEALING_POWER, "netherite_citrine_ring.healing_power", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.LIGHTNING_DAMAGE_BONUS, "netherite_citrine_ring.lightning_damage_bonus", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> NETHERITE_JADE_RING = ITEMS.registerItem("netherite_jade_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(MegaModAttributes.RANGED_DAMAGE, "netherite_jade_ring.ranged_damage", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> NETHERITE_SAPPHIRE_RING = ITEMS.registerItem("netherite_sapphire_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.MAX_HEALTH, "netherite_sapphire_ring.max_health", 4.0, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> NETHERITE_TANZANITE_RING = ITEMS.registerItem("netherite_tanzanite_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "netherite_tanzanite_ring.ice_damage_bonus", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SOUL_POWER, "netherite_tanzanite_ring.soul_power", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    // ═══════════════════════════════════════════════════════════════
    // NETHERITE GEM NECKLACES (Tier 3) — netherite-framed pendants, same as rings
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> NETHERITE_RUBY_NECKLACE = ITEMS.registerItem("netherite_ruby_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_DAMAGE, "netherite_ruby_necklace.attack_damage", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> NETHERITE_TOPAZ_NECKLACE = ITEMS.registerItem("netherite_topaz_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "netherite_topaz_necklace.arcane_power", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "netherite_topaz_necklace.fire_damage_bonus", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> NETHERITE_CITRINE_NECKLACE = ITEMS.registerItem("netherite_citrine_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.HEALING_POWER, "netherite_citrine_necklace.healing_power", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.LIGHTNING_DAMAGE_BONUS, "netherite_citrine_necklace.lightning_damage_bonus", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> NETHERITE_JADE_NECKLACE = ITEMS.registerItem("netherite_jade_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(MegaModAttributes.RANGED_DAMAGE, "netherite_jade_necklace.ranged_damage", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> NETHERITE_SAPPHIRE_NECKLACE = ITEMS.registerItem("netherite_sapphire_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.MAX_HEALTH, "netherite_sapphire_necklace.max_health", 4.0, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> NETHERITE_TANZANITE_NECKLACE = ITEMS.registerItem("netherite_tanzanite_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "netherite_tanzanite_necklace.ice_damage_bonus", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SOUL_POWER, "netherite_tanzanite_necklace.soul_power", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    // ═══════════════════════════════════════════════════════════════
    // SPECIAL JEWELRY (Tier 1) — unique high-value pieces
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> EMERALD_NECKLACE = ITEMS.registerItem("emerald_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.LUCK, "emerald_necklace.luck", 1.0, AttributeModifier.Operation.ADD_VALUE))
        ));

    public static final DeferredItem<Item> DIAMOND_NECKLACE = ITEMS.registerItem("diamond_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_SPEED, "diamond_necklace.attack_speed", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> DIAMOND_RING = ITEMS.registerItem("diamond_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_SPEED, "diamond_ring.attack_speed", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    // ═══════════════════════════════════════════════════════════════
    // UNIQUE JEWELRY (Tier 4) — powerful endgame pieces
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<Item> UNIQUE_ATTACK_RING = ITEMS.registerItem("unique_attack_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_DAMAGE, "unique_attack_ring.attack_damage", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> UNIQUE_ATTACK_NECKLACE = ITEMS.registerItem("unique_attack_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(Attributes.ATTACK_DAMAGE, "unique_attack_necklace.attack_damage", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> UNIQUE_DEX_RING = ITEMS.registerItem("unique_dex_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(Attributes.ATTACK_SPEED, "unique_dex_ring.attack_speed", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(Attributes.MOVEMENT_SPEED, "unique_dex_ring.movement_speed", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_DEX_NECKLACE = ITEMS.registerItem("unique_dex_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(Attributes.ATTACK_SPEED, "unique_dex_necklace.attack_speed", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(Attributes.MOVEMENT_SPEED, "unique_dex_necklace.movement_speed", 0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_TANK_RING = ITEMS.registerItem("unique_tank_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(Attributes.MAX_HEALTH, "unique_tank_ring.max_health", 6.0, AttributeModifier.Operation.ADD_VALUE),
                new ModEntry(Attributes.ARMOR, "unique_tank_ring.armor", 4.0, AttributeModifier.Operation.ADD_VALUE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_TANK_NECKLACE = ITEMS.registerItem("unique_tank_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(Attributes.MAX_HEALTH, "unique_tank_necklace.max_health", 6.0, AttributeModifier.Operation.ADD_VALUE),
                new ModEntry(Attributes.ARMOR, "unique_tank_necklace.armor", 4.0, AttributeModifier.Operation.ADD_VALUE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_ARCHER_RING = ITEMS.registerItem("unique_archer_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(MegaModAttributes.RANGED_DAMAGE, "unique_archer_ring.ranged_damage", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> UNIQUE_ARCHER_NECKLACE = ITEMS.registerItem("unique_archer_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(new ModEntry(MegaModAttributes.RANGED_DAMAGE, "unique_archer_necklace.ranged_damage", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
        ));

    public static final DeferredItem<Item> UNIQUE_ARCANE_RING = ITEMS.registerItem("unique_arcane_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "unique_arcane_ring.arcane_power", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SPELL_HASTE, "unique_arcane_ring.spell_haste", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_ARCANE_NECKLACE = ITEMS.registerItem("unique_arcane_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "unique_arcane_necklace.arcane_power", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SPELL_HASTE, "unique_arcane_necklace.spell_haste", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_FIRE_RING = ITEMS.registerItem("unique_fire_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "unique_fire_ring.fire_damage_bonus", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.CRITICAL_CHANCE, "unique_fire_ring.critical_chance", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_FIRE_NECKLACE = ITEMS.registerItem("unique_fire_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "unique_fire_necklace.fire_damage_bonus", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.CRITICAL_CHANCE, "unique_fire_necklace.critical_chance", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_FROST_RING = ITEMS.registerItem("unique_frost_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "unique_frost_ring.ice_damage_bonus", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.CRITICAL_DAMAGE, "unique_frost_ring.critical_damage", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_FROST_NECKLACE = ITEMS.registerItem("unique_frost_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "unique_frost_necklace.ice_damage_bonus", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.CRITICAL_DAMAGE, "unique_frost_necklace.critical_damage", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_HEALING_RING = ITEMS.registerItem("unique_healing_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.HEALING_POWER, "unique_healing_ring.healing_power", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SPELL_HASTE, "unique_healing_ring.spell_haste", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_HEALING_NECKLACE = ITEMS.registerItem("unique_healing_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.HEALING_POWER, "unique_healing_necklace.healing_power", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.SPELL_HASTE, "unique_healing_necklace.spell_haste", 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_SPELL_RING = ITEMS.registerItem("unique_spell_ring",
        props -> new RpgJewelryItem(props, AccessorySlotType.RING_LEFT),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "unique_spell_ring.arcane_power", 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "unique_spell_ring.fire_damage_bonus", 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "unique_spell_ring.ice_damage_bonus", 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    public static final DeferredItem<Item> UNIQUE_SPELL_NECKLACE = ITEMS.registerItem("unique_spell_necklace",
        props -> new RpgJewelryItem(props, AccessorySlotType.NECKLACE),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildModifiers(
                new ModEntry(MegaModAttributes.ARCANE_POWER, "unique_spell_necklace.arcane_power", 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.FIRE_DAMAGE_BONUS, "unique_spell_necklace.fire_damage_bonus", 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                new ModEntry(MegaModAttributes.ICE_DAMAGE_BONUS, "unique_spell_necklace.ice_damage_bonus", 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            )
        ));

    // ═══════════════════════════════════════════════════════════════
    // REGISTRATION
    // ═══════════════════════════════════════════════════════════════

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // ═══════════════════════════════════════════════════════════════
    // TIER HELPERS — used by loot, shop, and crafting systems
    // ═══════════════════════════════════════════════════════════════

    public static java.util.List<net.minecraft.world.item.Item> getRawGemItems() {
        return java.util.List.of(
            RUBY.get(), TOPAZ.get(), CITRINE.get(),
            JADE.get(), SAPPHIRE.get(), TANZANITE.get()
        );
    }

    public static java.util.List<net.minecraft.world.item.Item> getBasicItems() {
        return java.util.List.of(
            COPPER_RING.get(), IRON_RING.get(), GOLD_RING.get(),
            EMERALD_NECKLACE.get(), DIAMOND_RING.get(), DIAMOND_NECKLACE.get()
        );
    }

    public static java.util.List<net.minecraft.world.item.Item> getGemItems() {
        return java.util.List.of(
            RUBY_RING.get(), TOPAZ_RING.get(), CITRINE_RING.get(),
            JADE_RING.get(), SAPPHIRE_RING.get(), TANZANITE_RING.get(),
            RUBY_NECKLACE.get(), TOPAZ_NECKLACE.get(), CITRINE_NECKLACE.get(),
            JADE_NECKLACE.get(), SAPPHIRE_NECKLACE.get(), TANZANITE_NECKLACE.get()
        );
    }

    public static java.util.List<net.minecraft.world.item.Item> getNetheriteItems() {
        return java.util.List.of(
            NETHERITE_RUBY_RING.get(), NETHERITE_TOPAZ_RING.get(), NETHERITE_CITRINE_RING.get(),
            NETHERITE_JADE_RING.get(), NETHERITE_SAPPHIRE_RING.get(), NETHERITE_TANZANITE_RING.get(),
            NETHERITE_RUBY_NECKLACE.get(), NETHERITE_TOPAZ_NECKLACE.get(), NETHERITE_CITRINE_NECKLACE.get(),
            NETHERITE_JADE_NECKLACE.get(), NETHERITE_SAPPHIRE_NECKLACE.get(), NETHERITE_TANZANITE_NECKLACE.get()
        );
    }

    public static java.util.List<net.minecraft.world.item.Item> getUniqueItems() {
        return java.util.List.of(
            UNIQUE_ATTACK_RING.get(), UNIQUE_ATTACK_NECKLACE.get(),
            UNIQUE_DEX_RING.get(), UNIQUE_DEX_NECKLACE.get(),
            UNIQUE_TANK_RING.get(), UNIQUE_TANK_NECKLACE.get(),
            UNIQUE_ARCHER_RING.get(), UNIQUE_ARCHER_NECKLACE.get(),
            UNIQUE_ARCANE_RING.get(), UNIQUE_ARCANE_NECKLACE.get(),
            UNIQUE_FIRE_RING.get(), UNIQUE_FIRE_NECKLACE.get(),
            UNIQUE_FROST_RING.get(), UNIQUE_FROST_NECKLACE.get(),
            UNIQUE_HEALING_RING.get(), UNIQUE_HEALING_NECKLACE.get(),
            UNIQUE_SPELL_RING.get(), UNIQUE_SPELL_NECKLACE.get()
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTRIBUTE MODIFIER BUILDER HELPERS
    // ═══════════════════════════════════════════════════════════════

    /** A single attribute modifier entry for the builder. */
    private record ModEntry(
        Holder<Attribute> attribute,
        String idSuffix,
        double amount,
        AttributeModifier.Operation operation
    ) {}

    /**
     * Builds an {@link ItemAttributeModifiers} from one or more modifier entries.
     * Each modifier uses {@link EquipmentSlotGroup#ANY} since jewelry works in accessory slots.
     */
    private static ItemAttributeModifiers buildModifiers(ModEntry... entries) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ModEntry entry : entries) {
            AttributeModifier modifier = new AttributeModifier(
                Identifier.fromNamespaceAndPath("megamod", "jewelry." + entry.idSuffix()),
                entry.amount(),
                entry.operation()
            );
            builder.add(entry.attribute(), modifier, EquipmentSlotGroup.ANY);
        }
        return builder.build();
    }
}
