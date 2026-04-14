package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.weapons.RpgBowItem;
import com.ultra.megamod.feature.relics.weapons.RpgCrossbowItem;
import com.ultra.megamod.feature.relics.weapons.RpgShieldItem;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Class-tier weapons for the RPG combat system.
 * These are the standard progression weapons players earn/loot as they level.
 * Legendary/unique weapons remain in RelicRegistry.
 *
 * <p><b>SpellEngine migration:</b> Wands, staves, claymores, great hammers, maces,
 * holy wands, holy staves, and kite shields are now registered through the
 * SpellEngine factory pipeline (see {@code WizardsMod.registerItems} and
 * {@code PaladinsMod.registerItems}) so they carry {@code SpellContainer}
 * components and participate in right-click casting, passive triggers, and
 * equipment-set MODIFIER spells. Their entries have been removed from this
 * registry to avoid duplicate item registrations. Legacy
 * {@code ClassWeaponRegistry.XYZ} field references have been replaced with
 * {@link #item(String)} lookups below so callers keep compiling.</p>
 *
 * <p>Rogue weapons (daggers, sickles, glaives, double axes) and Archer/Ranger
 * weapons (bows, crossbows, spears) continue to register via plain
 * {@link RpgWeaponItem} / {@link RpgBowItem} / {@link RpgCrossbowItem} until
 * their respective class-mod SpellEngine ports complete.</p>
 */
public class ClassWeaponRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ═══════════════════════════════════════════════════════════════
    // DAGGERS (Rogue) — fast, low damage  [legacy path — awaits rogues SpellEngine port]
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
    // SICKLES (Rogue)
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
    // DOUBLE AXES (Warrior)
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
    // GLAIVES (Warrior)
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
    // SPEARS (Ranger)
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
    // BOWS (Ranger)
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
    // CROSSBOWS (Ranger)
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
    // SpellEngine-registered items (legacy field-alias shims)
    //
    // These IDs are now registered by WizardsMod / PaladinsMod via the
    // SpellEngine factory pipeline (with SpellContainer components) — the
    // previous RpgWeaponItem / RpgShieldItem entries were removed to avoid
    // duplicate registration crashes. Downstream callers still reference
    // {@code ClassWeaponRegistry.XYZ} fields; those are now lazy lookups
    // into {@code BuiltInRegistries.ITEM}, wrapped in a small Lookup helper
    // that mimics {@code DeferredItem}'s {@code .get()} surface so existing
    // call sites keep compiling without churn.
    // ═══════════════════════════════════════════════════════════════

    /** Wraps a {@link BuiltInRegistries#ITEM} lookup so existing {@code .get()} call sites keep working. */
    public static final class Lookup {
        private final String path;
        private Item cached;
        private Lookup(String path) { this.path = path; }
        public Item get() {
            if (cached == null) {
                cached = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(MegaMod.MODID, path));
            }
            return cached;
        }
        public String path() { return path; }
    }

    private static Lookup item(String path) { return new Lookup(path); }

    // Wizard wands (registered by WizardsMod.registerItems via WizardWeapons)
    public static final Lookup WAND_NOVICE = item("wand_novice");
    public static final Lookup WAND_ARCANE = item("wand_arcane");
    public static final Lookup WAND_FIRE = item("wand_fire");
    public static final Lookup WAND_FROST = item("wand_frost");
    public static final Lookup WAND_NETHERITE_ARCANE = item("wand_netherite_arcane");
    public static final Lookup WAND_NETHERITE_FIRE = item("wand_netherite_fire");
    public static final Lookup WAND_NETHERITE_FROST = item("wand_netherite_frost");

    // Wizard staves
    public static final Lookup STAFF_WIZARD = item("staff_wizard");
    public static final Lookup STAFF_ARCANE = item("staff_arcane");
    public static final Lookup STAFF_FIRE = item("staff_fire");
    public static final Lookup STAFF_FROST = item("staff_frost");
    public static final Lookup STAFF_NETHERITE_ARCANE = item("staff_netherite_arcane");
    public static final Lookup STAFF_NETHERITE_FIRE = item("staff_netherite_fire");
    public static final Lookup STAFF_NETHERITE_FROST = item("staff_netherite_frost");

    // Paladin claymores
    public static final Lookup STONE_CLAYMORE = item("stone_claymore");
    public static final Lookup IRON_CLAYMORE = item("iron_claymore");
    public static final Lookup GOLDEN_CLAYMORE = item("golden_claymore");
    public static final Lookup DIAMOND_CLAYMORE = item("diamond_claymore");
    public static final Lookup NETHERITE_CLAYMORE = item("netherite_claymore");

    // Paladin great hammers
    public static final Lookup WOODEN_GREAT_HAMMER = item("wooden_great_hammer");
    public static final Lookup STONE_GREAT_HAMMER = item("stone_great_hammer");
    public static final Lookup IRON_GREAT_HAMMER = item("iron_great_hammer");
    public static final Lookup GOLDEN_GREAT_HAMMER = item("golden_great_hammer");
    public static final Lookup DIAMOND_GREAT_HAMMER = item("diamond_great_hammer");
    public static final Lookup NETHERITE_GREAT_HAMMER = item("netherite_great_hammer");

    // Paladin maces
    public static final Lookup IRON_MACE = item("iron_mace");
    public static final Lookup GOLDEN_MACE = item("golden_mace");
    public static final Lookup DIAMOND_MACE = item("diamond_mace");
    public static final Lookup NETHERITE_MACE = item("netherite_mace");

    // Paladin healing wands
    public static final Lookup ACOLYTE_WAND = item("acolyte_wand");
    public static final Lookup HOLY_WAND = item("holy_wand");
    public static final Lookup DIAMOND_HOLY_WAND = item("diamond_holy_wand");
    public static final Lookup NETHERITE_HOLY_WAND = item("netherite_holy_wand");

    // Paladin healing staves
    public static final Lookup HOLY_STAFF = item("holy_staff");
    public static final Lookup DIAMOND_HOLY_STAFF = item("diamond_holy_staff");
    public static final Lookup NETHERITE_HOLY_STAFF = item("netherite_holy_staff");

    // Paladin kite shields
    public static final Lookup IRON_KITE_SHIELD = item("iron_kite_shield");
    public static final Lookup GOLDEN_KITE_SHIELD = item("golden_kite_shield");
    public static final Lookup DIAMOND_KITE_SHIELD = item("diamond_kite_shield");
    public static final Lookup NETHERITE_KITE_SHIELD = item("netherite_kite_shield");

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // ─── Loot tier helpers ───

    /** Null-tolerant builder so the list doesn't contain AIR entries if SpellEngine items haven't fully loaded yet. */
    private static void addItem(List<Item> out, Item item) {
        if (item != null && item != net.minecraft.world.item.Items.AIR) out.add(item);
    }

    public static List<Item> getNormalTierItems() {
        List<Item> out = new ArrayList<>();
        addItem(out, STONE_CLAYMORE.get());
        addItem(out, GOLDEN_CLAYMORE.get());
        addItem(out, WOODEN_GREAT_HAMMER.get());
        addItem(out, STONE_GREAT_HAMMER.get());
        addItem(out, GOLDEN_GREAT_HAMMER.get());
        addItem(out, STONE_DOUBLE_AXE.get());
        addItem(out, GOLDEN_DOUBLE_AXE.get());
        addItem(out, FLINT_DAGGER.get());
        addItem(out, GOLDEN_DAGGER.get());
        addItem(out, GOLDEN_SICKLE.get());
        addItem(out, GOLDEN_MACE.get());
        addItem(out, GOLDEN_GLAIVE.get());
        addItem(out, GOLDEN_SPEAR.get());
        addItem(out, FLINT_SPEAR.get());
        addItem(out, WAND_NOVICE.get());
        addItem(out, ACOLYTE_WAND.get());
        addItem(out, IRON_CLAYMORE.get());
        addItem(out, IRON_GREAT_HAMMER.get());
        addItem(out, IRON_MACE.get());
        addItem(out, IRON_DAGGER.get());
        addItem(out, IRON_SICKLE.get());
        addItem(out, IRON_DOUBLE_AXE.get());
        addItem(out, IRON_GLAIVE.get());
        addItem(out, IRON_SPEAR.get());
        addItem(out, STAFF_WIZARD.get());
        addItem(out, HOLY_WAND.get());
        addItem(out, HOLY_STAFF.get());
        addItem(out, COMPOSITE_LONGBOW.get());
        addItem(out, IRON_KITE_SHIELD.get());
        addItem(out, GOLDEN_KITE_SHIELD.get());
        return out;
    }

    public static List<Item> getHardTierItems() {
        List<Item> out = new ArrayList<>();
        addItem(out, DIAMOND_CLAYMORE.get());
        addItem(out, DIAMOND_GREAT_HAMMER.get());
        addItem(out, DIAMOND_MACE.get());
        addItem(out, DIAMOND_DAGGER.get());
        addItem(out, DIAMOND_SICKLE.get());
        addItem(out, DIAMOND_DOUBLE_AXE.get());
        addItem(out, DIAMOND_GLAIVE.get());
        addItem(out, DIAMOND_SPEAR.get());
        addItem(out, DIAMOND_HOLY_WAND.get());
        addItem(out, DIAMOND_HOLY_STAFF.get());
        addItem(out, WAND_ARCANE.get());
        addItem(out, WAND_FIRE.get());
        addItem(out, WAND_FROST.get());
        addItem(out, STAFF_ARCANE.get());
        addItem(out, STAFF_FIRE.get());
        addItem(out, STAFF_FROST.get());
        addItem(out, MECHANIC_SHORTBOW.get());
        addItem(out, ROYAL_LONGBOW.get());
        addItem(out, RAPID_CROSSBOW.get());
        addItem(out, HEAVY_CROSSBOW.get());
        addItem(out, DIAMOND_KITE_SHIELD.get());
        return out;
    }

    public static List<Item> getNightmareTierItems() {
        List<Item> out = new ArrayList<>();
        addItem(out, NETHERITE_CLAYMORE.get());
        addItem(out, NETHERITE_GREAT_HAMMER.get());
        addItem(out, NETHERITE_MACE.get());
        addItem(out, NETHERITE_DAGGER.get());
        addItem(out, NETHERITE_SICKLE.get());
        addItem(out, NETHERITE_DOUBLE_AXE.get());
        addItem(out, NETHERITE_GLAIVE.get());
        addItem(out, NETHERITE_SPEAR.get());
        addItem(out, NETHERITE_HOLY_WAND.get());
        addItem(out, NETHERITE_HOLY_STAFF.get());
        addItem(out, WAND_NETHERITE_ARCANE.get());
        addItem(out, WAND_NETHERITE_FIRE.get());
        addItem(out, WAND_NETHERITE_FROST.get());
        addItem(out, STAFF_NETHERITE_ARCANE.get());
        addItem(out, STAFF_NETHERITE_FIRE.get());
        addItem(out, STAFF_NETHERITE_FROST.get());
        addItem(out, NETHERITE_SHORTBOW.get());
        addItem(out, NETHERITE_LONGBOW.get());
        addItem(out, NETHERITE_RAPID_CROSSBOW.get());
        addItem(out, NETHERITE_HEAVY_CROSSBOW.get());
        addItem(out, NETHERITE_KITE_SHIELD.get());
        return out;
    }
}
