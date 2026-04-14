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
    // Rogue/Warrior weapons (DAGGERS, SICKLES, DOUBLE AXES, GLAIVES) — now
    // registered by RoguesMod.registerItems via SpellEngine Weapons factory
    // with weapon-skill SpellContainer components. Legacy ClassWeaponRegistry
    // field references fall through to {@link Lookup} shims further below.
    // ═══════════════════════════════════════════════════════════════

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

    // Rogue daggers (registered by RoguesMod.registerItems via RogueWeapons)
    public static final Lookup FLINT_DAGGER = item("flint_dagger");
    public static final Lookup IRON_DAGGER = item("iron_dagger");
    public static final Lookup GOLDEN_DAGGER = item("golden_dagger");
    public static final Lookup DIAMOND_DAGGER = item("diamond_dagger");
    public static final Lookup NETHERITE_DAGGER = item("netherite_dagger");

    // Rogue sickles
    public static final Lookup IRON_SICKLE = item("iron_sickle");
    public static final Lookup GOLDEN_SICKLE = item("golden_sickle");
    public static final Lookup DIAMOND_SICKLE = item("diamond_sickle");
    public static final Lookup NETHERITE_SICKLE = item("netherite_sickle");

    // Warrior double axes
    public static final Lookup STONE_DOUBLE_AXE = item("stone_double_axe");
    public static final Lookup IRON_DOUBLE_AXE = item("iron_double_axe");
    public static final Lookup GOLDEN_DOUBLE_AXE = item("golden_double_axe");
    public static final Lookup DIAMOND_DOUBLE_AXE = item("diamond_double_axe");
    public static final Lookup NETHERITE_DOUBLE_AXE = item("netherite_double_axe");

    // Warrior glaives
    public static final Lookup IRON_GLAIVE = item("iron_glaive");
    public static final Lookup GOLDEN_GLAIVE = item("golden_glaive");
    public static final Lookup DIAMOND_GLAIVE = item("diamond_glaive");
    public static final Lookup NETHERITE_GLAIVE = item("netherite_glaive");

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
