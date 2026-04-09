package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.weapons.RpgBowItem;
import com.ultra.megamod.feature.relics.weapons.RpgCrossbowItem;
import com.ultra.megamod.feature.relics.weapons.RpgShieldItem;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Class-tier weapons for the RPG combat system.
 * These are the standard progression weapons players earn/loot as they level.
 * Legendary/unique weapons remain in RelicRegistry.
 */
public class ClassWeaponRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ═══════════════════════════════════════════════════════════════
    // CLAYMORES (Paladin / Warrior) — slow, high damage two-handers
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> STONE_CLAYMORE = ITEMS.registerItem("stone_claymore",
        props -> new RpgWeaponItem("Stone Claymore", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> GOLDEN_CLAYMORE = ITEMS.registerItem("golden_claymore",
        props -> new RpgWeaponItem("Golden Claymore", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_CLAYMORE = ITEMS.registerItem("iron_claymore",
        props -> new RpgWeaponItem("Iron Claymore", 6.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_CLAYMORE = ITEMS.registerItem("diamond_claymore",
        props -> new RpgWeaponItem("Diamond Claymore", 8.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_CLAYMORE = ITEMS.registerItem("netherite_claymore",
        props -> new RpgWeaponItem("Netherite Claymore", 10.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // GREAT HAMMERS (Paladin) — slowest, highest damage two-handers
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> WOODEN_GREAT_HAMMER = ITEMS.registerItem("wooden_great_hammer",
        props -> new RpgWeaponItem("Wooden Great Hammer", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STONE_GREAT_HAMMER = ITEMS.registerItem("stone_great_hammer",
        props -> new RpgWeaponItem("Stone Great Hammer", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> GOLDEN_GREAT_HAMMER = ITEMS.registerItem("golden_great_hammer",
        props -> new RpgWeaponItem("Golden Great Hammer", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_GREAT_HAMMER = ITEMS.registerItem("iron_great_hammer",
        props -> new RpgWeaponItem("Iron Great Hammer", 7.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_GREAT_HAMMER = ITEMS.registerItem("diamond_great_hammer",
        props -> new RpgWeaponItem("Diamond Great Hammer", 9.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_GREAT_HAMMER = ITEMS.registerItem("netherite_great_hammer",
        props -> new RpgWeaponItem("Netherite Great Hammer", 11.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // MACES (Paladin) — one-handed blunt weapons
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> IRON_MACE = ITEMS.registerItem("iron_mace",
        props -> new RpgWeaponItem("Iron Mace", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> GOLDEN_MACE = ITEMS.registerItem("golden_mace",
        props -> new RpgWeaponItem("Golden Mace", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_MACE = ITEMS.registerItem("diamond_mace",
        props -> new RpgWeaponItem("Diamond Mace", 7.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_MACE = ITEMS.registerItem("netherite_mace",
        props -> new RpgWeaponItem("Netherite Mace", 9.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // DAGGERS (Rogue) — fast, low damage
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> FLINT_DAGGER = ITEMS.registerItem("flint_dagger",
        props -> new RpgWeaponItem("Flint Dagger", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_DAGGER = ITEMS.registerItem("iron_dagger",
        props -> new RpgWeaponItem("Iron Dagger", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> GOLDEN_DAGGER = ITEMS.registerItem("golden_dagger",
        props -> new RpgWeaponItem("Golden Dagger", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_DAGGER = ITEMS.registerItem("diamond_dagger",
        props -> new RpgWeaponItem("Diamond Dagger", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_DAGGER = ITEMS.registerItem("netherite_dagger",
        props -> new RpgWeaponItem("Netherite Dagger", 6.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // SICKLES (Rogue) — curved blades, moderate speed
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> GOLDEN_SICKLE = ITEMS.registerItem("golden_sickle",
        props -> new RpgWeaponItem("Golden Sickle", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_SICKLE = ITEMS.registerItem("iron_sickle",
        props -> new RpgWeaponItem("Iron Sickle", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_SICKLE = ITEMS.registerItem("diamond_sickle",
        props -> new RpgWeaponItem("Diamond Sickle", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_SICKLE = ITEMS.registerItem("netherite_sickle",
        props -> new RpgWeaponItem("Netherite Sickle", 6.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // DOUBLE AXES (Warrior) — heavy two-handed axes
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> STONE_DOUBLE_AXE = ITEMS.registerItem("stone_double_axe",
        props -> new RpgWeaponItem("Stone Double Axe", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> GOLDEN_DOUBLE_AXE = ITEMS.registerItem("golden_double_axe",
        props -> new RpgWeaponItem("Golden Double Axe", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_DOUBLE_AXE = ITEMS.registerItem("iron_double_axe",
        props -> new RpgWeaponItem("Iron Double Axe", 7.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_DOUBLE_AXE = ITEMS.registerItem("diamond_double_axe",
        props -> new RpgWeaponItem("Diamond Double Axe", 9.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_DOUBLE_AXE = ITEMS.registerItem("netherite_double_axe",
        props -> new RpgWeaponItem("Netherite Double Axe", 11.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // GLAIVES (Warrior) — polearm slashing weapons
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> GOLDEN_GLAIVE = ITEMS.registerItem("golden_glaive",
        props -> new RpgWeaponItem("Golden Glaive", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_GLAIVE = ITEMS.registerItem("iron_glaive",
        props -> new RpgWeaponItem("Iron Glaive", 6.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_GLAIVE = ITEMS.registerItem("diamond_glaive",
        props -> new RpgWeaponItem("Diamond Glaive", 8.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_GLAIVE = ITEMS.registerItem("netherite_glaive",
        props -> new RpgWeaponItem("Netherite Glaive", 10.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // SPEARS (Ranger) — polearm thrusting weapons
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> FLINT_SPEAR = ITEMS.registerItem("flint_spear",
        props -> new RpgWeaponItem("Flint Spear", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> GOLDEN_SPEAR = ITEMS.registerItem("golden_spear",
        props -> new RpgWeaponItem("Golden Spear", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> IRON_SPEAR = ITEMS.registerItem("iron_spear",
        props -> new RpgWeaponItem("Iron Spear", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_SPEAR = ITEMS.registerItem("diamond_spear",
        props -> new RpgWeaponItem("Diamond Spear", 7.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_SPEAR = ITEMS.registerItem("netherite_spear",
        props -> new RpgWeaponItem("Netherite Spear", 9.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // WANDS (Wizard) — one-handed spell catalysts
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> WAND_NOVICE = ITEMS.registerItem("wand_novice",
        props -> new RpgWeaponItem("Novice Wand", 2.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> WAND_ARCANE = ITEMS.registerItem("wand_arcane",
        props -> new RpgWeaponItem("Arcane Wand", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> WAND_FIRE = ITEMS.registerItem("wand_fire",
        props -> new RpgWeaponItem("Fire Wand", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> WAND_FROST = ITEMS.registerItem("wand_frost",
        props -> new RpgWeaponItem("Frost Wand", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> WAND_NETHERITE_ARCANE = ITEMS.registerItem("wand_netherite_arcane",
        props -> new RpgWeaponItem("Netherite Arcane Wand", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> WAND_NETHERITE_FIRE = ITEMS.registerItem("wand_netherite_fire",
        props -> new RpgWeaponItem("Netherite Fire Wand", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> WAND_NETHERITE_FROST = ITEMS.registerItem("wand_netherite_frost",
        props -> new RpgWeaponItem("Netherite Frost Wand", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // STAVES (Wizard) — two-handed spell catalysts, higher power
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> STAFF_WIZARD = ITEMS.registerItem("staff_wizard",
        props -> new RpgWeaponItem("Wizard Staff", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STAFF_ARCANE = ITEMS.registerItem("staff_arcane",
        props -> new RpgWeaponItem("Arcane Staff", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STAFF_FIRE = ITEMS.registerItem("staff_fire",
        props -> new RpgWeaponItem("Fire Staff", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STAFF_FROST = ITEMS.registerItem("staff_frost",
        props -> new RpgWeaponItem("Frost Staff", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STAFF_NETHERITE_ARCANE = ITEMS.registerItem("staff_netherite_arcane",
        props -> new RpgWeaponItem("Netherite Arcane Staff", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STAFF_NETHERITE_FIRE = ITEMS.registerItem("staff_netherite_fire",
        props -> new RpgWeaponItem("Netherite Fire Staff", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> STAFF_NETHERITE_FROST = ITEMS.registerItem("staff_netherite_frost",
        props -> new RpgWeaponItem("Netherite Frost Staff", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // HEALING WANDS (Paladin) — one-handed healing catalysts
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> ACOLYTE_WAND = ITEMS.registerItem("acolyte_wand",
        props -> new RpgWeaponItem("Acolyte Wand", 2.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> HOLY_WAND = ITEMS.registerItem("holy_wand",
        props -> new RpgWeaponItem("Holy Wand", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_HOLY_WAND = ITEMS.registerItem("diamond_holy_wand",
        props -> new RpgWeaponItem("Diamond Holy Wand", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_HOLY_WAND = ITEMS.registerItem("netherite_holy_wand",
        props -> new RpgWeaponItem("Netherite Holy Wand", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // HEALING STAVES (Paladin) — two-handed healing catalysts
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgWeaponItem> HOLY_STAFF = ITEMS.registerItem("holy_staff",
        props -> new RpgWeaponItem("Holy Staff", 3.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> DIAMOND_HOLY_STAFF = ITEMS.registerItem("diamond_holy_staff",
        props -> new RpgWeaponItem("Diamond Holy Staff", 4.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgWeaponItem> NETHERITE_HOLY_STAFF = ITEMS.registerItem("netherite_holy_staff",
        props -> new RpgWeaponItem("Netherite Holy Staff", 5.0f, (Item.Properties) props, List.of()),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // BOWS (Ranger) — standard ranged weapons, rolled rarity + bonus stats
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgBowItem> COMPOSITE_LONGBOW = ITEMS.registerItem("composite_longbow",
        props -> new RpgBowItem("Composite Longbow", 4.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgBowItem> MECHANIC_SHORTBOW = ITEMS.registerItem("mechanic_shortbow",
        props -> new RpgBowItem("Mechanic Shortbow", 5.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgBowItem> ROYAL_LONGBOW = ITEMS.registerItem("royal_longbow",
        props -> new RpgBowItem("Royal Longbow", 6.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgBowItem> NETHERITE_SHORTBOW = ITEMS.registerItem("netherite_shortbow",
        props -> new RpgBowItem("Netherite Shortbow", 7.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgBowItem> NETHERITE_LONGBOW = ITEMS.registerItem("netherite_longbow",
        props -> new RpgBowItem("Netherite Longbow", 8.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // CROSSBOWS (Ranger) — charged ranged weapons, rolled rarity + bonus stats
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgCrossbowItem> RAPID_CROSSBOW = ITEMS.registerItem("rapid_crossbow",
        props -> new RpgCrossbowItem("Rapid Crossbow", 5.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgCrossbowItem> HEAVY_CROSSBOW = ITEMS.registerItem("heavy_crossbow",
        props -> new RpgCrossbowItem("Heavy Crossbow", 7.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgCrossbowItem> NETHERITE_RAPID_CROSSBOW = ITEMS.registerItem("netherite_rapid_crossbow",
        props -> new RpgCrossbowItem("Netherite Rapid Crossbow", 6.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<RpgCrossbowItem> NETHERITE_HEAVY_CROSSBOW = ITEMS.registerItem("netherite_heavy_crossbow",
        props -> new RpgCrossbowItem("Netherite Heavy Crossbow", 9.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // KITE SHIELDS (Paladin) — one-handed defensive items, rolled rarity + bonus stats
    // ═══════════════════════════════════════════════════════════════
    public static final DeferredItem<RpgShieldItem> IRON_KITE_SHIELD = ITEMS.registerItem("iron_kite_shield",
        props -> new RpgShieldItem("Iron Kite Shield", 2.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1).durability(336));
    public static final DeferredItem<RpgShieldItem> GOLDEN_KITE_SHIELD = ITEMS.registerItem("golden_kite_shield",
        props -> new RpgShieldItem("Golden Kite Shield", 1.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1).durability(112));
    public static final DeferredItem<RpgShieldItem> DIAMOND_KITE_SHIELD = ITEMS.registerItem("diamond_kite_shield",
        props -> new RpgShieldItem("Diamond Kite Shield", 3.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1).durability(528));
    public static final DeferredItem<RpgShieldItem> NETHERITE_KITE_SHIELD = ITEMS.registerItem("netherite_kite_shield",
        props -> new RpgShieldItem("Netherite Kite Shield", 4.0f, (Item.Properties) props),
        () -> new Item.Properties().stacksTo(1).durability(672));

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // ─── Loot tier helpers ───

    public static java.util.List<Item> getNormalTierItems() {
        return java.util.List.of(
            STONE_CLAYMORE.get(), GOLDEN_CLAYMORE.get(), WOODEN_GREAT_HAMMER.get(),
            STONE_GREAT_HAMMER.get(), GOLDEN_GREAT_HAMMER.get(), STONE_DOUBLE_AXE.get(),
            GOLDEN_DOUBLE_AXE.get(), FLINT_DAGGER.get(), GOLDEN_DAGGER.get(),
            GOLDEN_SICKLE.get(), GOLDEN_MACE.get(), GOLDEN_GLAIVE.get(),
            GOLDEN_SPEAR.get(), FLINT_SPEAR.get(), WAND_NOVICE.get(), ACOLYTE_WAND.get(),
            IRON_CLAYMORE.get(), IRON_GREAT_HAMMER.get(), IRON_MACE.get(),
            IRON_DAGGER.get(), IRON_SICKLE.get(), IRON_DOUBLE_AXE.get(), IRON_GLAIVE.get(),
            IRON_SPEAR.get(), STAFF_WIZARD.get(), HOLY_WAND.get(), HOLY_STAFF.get(),
            COMPOSITE_LONGBOW.get(),
            IRON_KITE_SHIELD.get(), GOLDEN_KITE_SHIELD.get()
        );
    }

    public static java.util.List<Item> getHardTierItems() {
        return java.util.List.of(
            DIAMOND_CLAYMORE.get(), DIAMOND_GREAT_HAMMER.get(), DIAMOND_MACE.get(),
            DIAMOND_DAGGER.get(), DIAMOND_SICKLE.get(), DIAMOND_DOUBLE_AXE.get(), DIAMOND_GLAIVE.get(),
            DIAMOND_SPEAR.get(), DIAMOND_HOLY_WAND.get(), DIAMOND_HOLY_STAFF.get(),
            WAND_ARCANE.get(), WAND_FIRE.get(), WAND_FROST.get(),
            STAFF_ARCANE.get(), STAFF_FIRE.get(), STAFF_FROST.get(),
            MECHANIC_SHORTBOW.get(), ROYAL_LONGBOW.get(), RAPID_CROSSBOW.get(), HEAVY_CROSSBOW.get(),
            DIAMOND_KITE_SHIELD.get()
        );
    }

    public static java.util.List<Item> getNightmareTierItems() {
        return java.util.List.of(
            NETHERITE_CLAYMORE.get(), NETHERITE_GREAT_HAMMER.get(), NETHERITE_MACE.get(),
            NETHERITE_DAGGER.get(), NETHERITE_SICKLE.get(), NETHERITE_DOUBLE_AXE.get(), NETHERITE_GLAIVE.get(),
            NETHERITE_SPEAR.get(), NETHERITE_HOLY_WAND.get(), NETHERITE_HOLY_STAFF.get(),
            WAND_NETHERITE_ARCANE.get(), WAND_NETHERITE_FIRE.get(), WAND_NETHERITE_FROST.get(),
            STAFF_NETHERITE_ARCANE.get(), STAFF_NETHERITE_FIRE.get(), STAFF_NETHERITE_FROST.get(),
            NETHERITE_SHORTBOW.get(), NETHERITE_LONGBOW.get(),
            NETHERITE_RAPID_CROSSBOW.get(), NETHERITE_HEAVY_CROSSBOW.get(),
            NETHERITE_KITE_SHIELD.get()
        );
    }
}
