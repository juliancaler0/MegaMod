package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes.Attack;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.HitboxShape;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.SwingDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Static registry mapping item registry names to their {@link WeaponAttributes}.
 * All definitions are hardcoded since we control every weapon in the mod.
 * <p>
 * Weapon categories and their combat behavior:
 * <ul>
 *   <li><b>Swords</b> - 3-hit horizontal combo, balanced speed/damage</li>
 *   <li><b>Claymores</b> - 3-hit two-handed combo, high damage with slow recovery</li>
 *   <li><b>Axes</b> - 2-hit horizontal chop combo, short range</li>
 *   <li><b>Double Axes</b> - 2-hit two-handed combo ending in spin, wide arcs</li>
 *   <li><b>Daggers</b> - Fast 3-hit combo with heavy stab finisher, short range</li>
 *   <li><b>Maces</b> - 3-hit combo (swing/swing/slam) with heavy overhead finisher</li>
 *   <li><b>Great Hammers</b> - Single forward slam, highest damage per hit</li>
 *   <li><b>Spears</b> - Single stab, long range, two-handed</li>
 *   <li><b>Glaives</b> - 3-hit polearm combo (chop/chop/sweep), two-handed</li>
 *   <li><b>Sickles</b> - Fast 2-hit slash combo, short range</li>
 *   <li><b>Wands</b> - Single direct hit, narrow angle, short range</li>
 *   <li><b>Staves</b> - 3-hit swing/swing/slam, two-handed</li>
 * </ul>
 */
public final class WeaponAttributeRegistry {

    private static final Map<Identifier, WeaponAttributes> REGISTRY = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════
    // WEAPON ARCHETYPE DEFINITIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Swords: 3-hit combo (slash right, slash left, stab).
     * Balanced all-around melee weapon.
     */
    private static final WeaponAttributes SWORD = new WeaponAttributes(
            0.0, false, "sword",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 120, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 120, 0.5, SwingDirection.SLASH_LEFT, null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.STAB, null)
            }
    );

    /**
     * Claymores: 3-hit two-handed combo (wide slash right, stab, overhead slam).
     * High damage, wide arcs, two-handed.
     */
    private static final WeaponAttributes CLAYMORE = new WeaponAttributes(
            0.25, true, "claymore",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.75, 150, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 60, 0.5, SwingDirection.STAB, null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 1.25, 150, 0.5, SwingDirection.SLASH_DOWN, null)
            }
    );

    /**
     * Axes (vanilla one-handed): 2-hit horizontal chop combo.
     * Slightly shorter range than default (-0.25).
     */
    private static final WeaponAttributes AXE = new WeaponAttributes(
            -0.25, false, "axe",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 90, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 90, 0.5, SwingDirection.SLASH_LEFT, null)
            }
    );

    /**
     * Double Axes: 2-hit two-handed combo (wide slash + full spin).
     * Wide arcs (180/360 angles), two-handed.
     */
    private static final WeaponAttributes DOUBLE_AXE = new WeaponAttributes(
            0.0, true, "double_axe",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 180, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 360, 0.5, SwingDirection.SPIN, null)
            }
    );

    /**
     * Daggers: Fast 3-hit combo (slash, slash, heavy stab finisher).
     * Short range (-0.5), increasing damage on finisher (0.9/0.9/1.4).
     */
    private static final WeaponAttributes DAGGER = new WeaponAttributes(
            -0.5, false, "dagger",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 80, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 80, 0.5, SwingDirection.SLASH_LEFT, null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.4, 80, 0.5, SwingDirection.STAB, null)
            }
    );

    /**
     * Maces: 3-hit combo (swing, swing, overhead slam).
     * Light openers (0.8) into heavy finisher (1.4).
     */
    private static final WeaponAttributes MACE = new WeaponAttributes(
            0.0, false, "mace",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_LEFT, null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 1.4, 90, 0.5, SwingDirection.SLASH_DOWN, null)
            }
    );

    /**
     * Great Hammers: Single forward slam, two-handed.
     * Slowest weapon type, full damage in one hit.
     */
    private static final WeaponAttributes GREAT_HAMMER = new WeaponAttributes(
            0.0, true, "great_hammer",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.SLASH_DOWN, null)
            }
    );

    /**
     * Spears: Single stab, long range, two-handed.
     * Narrow hitbox but extended reach.
     */
    private static final WeaponAttributes SPEAR = new WeaponAttributes(
            1.0, true, "spear",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 30, 0.5, SwingDirection.STAB, null)
            }
    );

    /**
     * Glaives: 3-hit polearm combo (chop, chop, wide sweep).
     * Extended range (+0.5), light openers (0.8) into heavy finisher (1.4), two-handed.
     */
    private static final WeaponAttributes GLAIVE = new WeaponAttributes(
            0.5, true, "glaive",
            new Attack[]{
                    new Attack(HitboxShape.VERTICAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_LEFT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.4, 180, 0.5, SwingDirection.SLASH_RIGHT, null)
            }
    );

    /**
     * Sickles: Fast 2-hit slash combo.
     * Short range (-0.25), consistent damage (0.9/0.9).
     */
    private static final WeaponAttributes SICKLE = new WeaponAttributes(
            -0.25, false, "sickle",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 90, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 90, 0.5, SwingDirection.SLASH_LEFT, null)
            }
    );

    /**
     * Wands: Single direct hit cast.
     * Short range (-0.5), narrow cone, one-handed spell catalyst.
     */
    private static final WeaponAttributes WAND = new WeaponAttributes(
            -0.5, false, "wand",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 30, 0.5, SwingDirection.STAB, null)
            }
    );

    /**
     * Staves: 3-hit combo (swing, swing, overhead slam).
     * Two-handed spell catalyst, escalating damage (0.8/1.0/1.2).
     */
    private static final WeaponAttributes STAFF = new WeaponAttributes(
            0.25, true, "staff",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.8, 160, 0.5, SwingDirection.SLASH_RIGHT, null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 160, 0.5, SwingDirection.SLASH_LEFT, null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 1.2, 120, 0.5, SwingDirection.SLASH_DOWN, null)
            }
    );

    /**
     * Trident: One-handed spear variant with shorter range (+0.25).
     */
    private static final WeaponAttributes TRIDENT = new WeaponAttributes(
            0.25, false, "spear",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 30, 0.5, SwingDirection.STAB, null)
            }
    );

    // ═══════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    static {
        // --- Vanilla Swords ---
        register("minecraft", "wooden_sword", SWORD);
        register("minecraft", "stone_sword", SWORD);
        register("minecraft", "iron_sword", SWORD);
        register("minecraft", "golden_sword", SWORD);
        register("minecraft", "diamond_sword", SWORD);
        register("minecraft", "netherite_sword", SWORD);

        // --- Vanilla Axes ---
        register("minecraft", "wooden_axe", AXE);
        register("minecraft", "stone_axe", AXE);
        register("minecraft", "iron_axe", AXE);
        register("minecraft", "golden_axe", AXE);
        register("minecraft", "diamond_axe", AXE);
        register("minecraft", "netherite_axe", AXE);

        // --- Vanilla Trident ---
        register("minecraft", "trident", TRIDENT);

        // --- Vanilla Mace (1.21) ---
        register("minecraft", "mace", MACE);

        // --- MegaMod Claymores ---
        register("megamod", "stone_claymore", CLAYMORE);
        register("megamod", "iron_claymore", CLAYMORE);
        register("megamod", "golden_claymore", CLAYMORE);
        register("megamod", "diamond_claymore", CLAYMORE);
        register("megamod", "netherite_claymore", CLAYMORE);

        // --- MegaMod Great Hammers ---
        register("megamod", "wooden_great_hammer", GREAT_HAMMER);
        register("megamod", "stone_great_hammer", GREAT_HAMMER);
        register("megamod", "iron_great_hammer", GREAT_HAMMER);
        register("megamod", "golden_great_hammer", GREAT_HAMMER);
        register("megamod", "diamond_great_hammer", GREAT_HAMMER);
        register("megamod", "netherite_great_hammer", GREAT_HAMMER);

        // --- MegaMod Maces ---
        register("megamod", "iron_mace", MACE);
        register("megamod", "golden_mace", MACE);
        register("megamod", "diamond_mace", MACE);
        register("megamod", "netherite_mace", MACE);

        // --- MegaMod Daggers ---
        register("megamod", "flint_dagger", DAGGER);
        register("megamod", "iron_dagger", DAGGER);
        register("megamod", "golden_dagger", DAGGER);
        register("megamod", "diamond_dagger", DAGGER);
        register("megamod", "netherite_dagger", DAGGER);

        // --- MegaMod Sickles ---
        register("megamod", "iron_sickle", SICKLE);
        register("megamod", "golden_sickle", SICKLE);
        register("megamod", "diamond_sickle", SICKLE);
        register("megamod", "netherite_sickle", SICKLE);

        // --- MegaMod Double Axes ---
        register("megamod", "stone_double_axe", DOUBLE_AXE);
        register("megamod", "iron_double_axe", DOUBLE_AXE);
        register("megamod", "golden_double_axe", DOUBLE_AXE);
        register("megamod", "diamond_double_axe", DOUBLE_AXE);
        register("megamod", "netherite_double_axe", DOUBLE_AXE);

        // --- MegaMod Glaives ---
        register("megamod", "iron_glaive", GLAIVE);
        register("megamod", "golden_glaive", GLAIVE);
        register("megamod", "diamond_glaive", GLAIVE);
        register("megamod", "netherite_glaive", GLAIVE);

        // --- MegaMod Spears ---
        register("megamod", "flint_spear", SPEAR);
        register("megamod", "iron_spear", SPEAR);
        register("megamod", "golden_spear", SPEAR);
        register("megamod", "diamond_spear", SPEAR);
        register("megamod", "netherite_spear", SPEAR);

        // --- MegaMod Wands (offensive) ---
        register("megamod", "wand_novice", WAND);
        register("megamod", "wand_arcane", WAND);
        register("megamod", "wand_fire", WAND);
        register("megamod", "wand_frost", WAND);
        register("megamod", "wand_netherite_arcane", WAND);
        register("megamod", "wand_netherite_fire", WAND);
        register("megamod", "wand_netherite_frost", WAND);

        // --- MegaMod Wands (healing) ---
        register("megamod", "acolyte_wand", WAND);
        register("megamod", "holy_wand", WAND);
        register("megamod", "diamond_holy_wand", WAND);
        register("megamod", "netherite_holy_wand", WAND);

        // --- MegaMod Staves (offensive) ---
        register("megamod", "staff_wizard", STAFF);
        register("megamod", "staff_arcane", STAFF);
        register("megamod", "staff_fire", STAFF);
        register("megamod", "staff_frost", STAFF);
        register("megamod", "staff_netherite_arcane", STAFF);
        register("megamod", "staff_netherite_fire", STAFF);
        register("megamod", "staff_netherite_frost", STAFF);

        // --- MegaMod Staves (healing) ---
        register("megamod", "holy_staff", STAFF);
        register("megamod", "diamond_holy_staff", STAFF);
        register("megamod", "netherite_holy_staff", STAFF);
    }

    private WeaponAttributeRegistry() {}

    // ═══════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Looks up weapon attributes by ItemStack. Returns null if the item has no
     * registered weapon attributes (e.g. non-weapon items, bows, crossbows).
     */
    @Nullable
    public static WeaponAttributes getAttributes(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        return getAttributes(stack.getItem());
    }

    /**
     * Looks up weapon attributes by Item. Returns null if not registered.
     */
    @Nullable
    public static WeaponAttributes getAttributes(Item item) {
        if (item == null) return null;
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        // Check JSON overrides first (data pack weapon_attributes)
        var jsonAttrs = com.ultra.megamod.feature.combat.animation.logic.WeaponAttributesLoader.getAttributes(key.toString());
        if (jsonAttrs != null) return jsonAttrs;
        // Fall back to hardcoded registry
        return REGISTRY.get(key);
    }

    /**
     * Checks whether an item has registered weapon attributes.
     */
    public static boolean hasAttributes(ItemStack stack) {
        return getAttributes(stack) != null;
    }

    /**
     * Checks whether an item has registered weapon attributes.
     */
    public static boolean hasAttributes(Item item) {
        return getAttributes(item) != null;
    }

    /**
     * Registers weapon attributes for an item by namespace and path.
     * Can be called to add attributes for items from other mods or
     * for dynamically registered MegaMod items.
     */
    public static void register(String namespace, String path, WeaponAttributes attributes) {
        REGISTRY.put(Identifier.fromNamespaceAndPath(namespace, path), attributes);
    }

    /**
     * Registers weapon attributes for an item directly.
     * The item must already be registered in the item registry.
     */
    public static void register(Item item, WeaponAttributes attributes) {
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        REGISTRY.put(key, attributes);
    }

    /**
     * Returns an unmodifiable view of all registered weapon attributes.
     */
    public static Map<Identifier, WeaponAttributes> getAll() {
        return Map.copyOf(REGISTRY);
    }

    /**
     * Returns the archetype attributes for a weapon category name.
     * Useful for assigning attributes to dynamically created weapons.
     */
    @Nullable
    public static WeaponAttributes getArchetype(String category) {
        return switch (category) {
            case "sword" -> SWORD;
            case "claymore" -> CLAYMORE;
            case "axe" -> AXE;
            case "double_axe" -> DOUBLE_AXE;
            case "dagger" -> DAGGER;
            case "mace" -> MACE;
            case "great_hammer" -> GREAT_HAMMER;
            case "spear" -> SPEAR;
            case "glaive" -> GLAIVE;
            case "sickle" -> SICKLE;
            case "wand" -> WAND;
            case "staff" -> STAFF;
            default -> null;
        };
    }
}
