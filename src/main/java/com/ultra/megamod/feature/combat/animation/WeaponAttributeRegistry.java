package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes.Attack;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.Condition;
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
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 120, 0.5, SwingDirection.SLASH_RIGHT, "one_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 120, 0.5, SwingDirection.SLASH_LEFT, "one_handed_slash_horizontal_left", null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.STAB, "one_handed_stab", null)
            }
    );

    /**
     * Claymores: 3-hit two-handed combo (wide slash right, stab, overhead slam).
     * Source-parity: uses {@code two_handed_stab_left} (not right) for attack 2.
     */
    private static final WeaponAttributes CLAYMORE = new WeaponAttributes(
            0.25, true, "claymore", "pose_two_handed_sword",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.75, 150, 0.5, SwingDirection.SLASH_RIGHT, "two_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.STAB, "two_handed_stab_left", null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 1.25, 150, 0.5, SwingDirection.SLASH_DOWN, "two_handed_slam", null)
            }
    );

    /**
     * Axes (vanilla one-handed): 2-hit horizontal chop combo.
     * Slightly shorter range than default (-0.25).
     */
    private static final WeaponAttributes AXE = new WeaponAttributes(
            -0.25, false, "axe",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 90, 0.5, SwingDirection.SLASH_RIGHT, "one_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 90, 0.5, SwingDirection.SLASH_LEFT, "one_handed_slash_horizontal_left", null)
            }
    );

    /**
     * Double Axes: 2-hit two-handed combo (wide slash + full spin).
     * Wide arcs (180/360 angles), two-handed.
     */
    private static final WeaponAttributes DOUBLE_AXE = new WeaponAttributes(
            0.0, true, "double_axe",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 180, 0.5, SwingDirection.SLASH_RIGHT, "two_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 360, 0.5, SwingDirection.SPIN, "two_handed_spin", null)
            }
    );

    /**
     * Daggers: Source-parity 3-hit combo — 2 horizontal slashes + conditional dual-stab finisher.
     * The 3rd attack only unlocks when dual-wielding daggers (main hand).
     */
    private static final WeaponAttributes DAGGER = new WeaponAttributes(
            -0.5, false, "dagger",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 150, 0.5, SwingDirection.SLASH_RIGHT, "one_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 150, 0.5, SwingDirection.SLASH_LEFT, "one_handed_slash_horizontal_left", null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.4, 150, 0.5, SwingDirection.STAB, "dual_handed_stab",
                            new Condition[]{ Condition.DUAL_WIELDING_SAME_CATEGORY, Condition.MAIN_HAND_ONLY })
            }
    );

    /**
     * Maces: Source-parity 3-hit combo (slash right, slash left, overhead slam).
     */
    private static final WeaponAttributes MACE = new WeaponAttributes(
            -0.25, false, "mace",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_RIGHT, "one_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_LEFT, "one_handed_slash_horizontal_left", null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 1.4, 90, 0.5, SwingDirection.SLASH_DOWN, "one_handed_slam", null)
            }
    );

    /**
     * Great Hammers (source "hammer"): Source-parity single forward slam, two-handed.
     * Uses {@code two_handed_slam} animation (not {@code two_handed_slam_heavy}).
     */
    private static final WeaponAttributes GREAT_HAMMER = new WeaponAttributes(
            0.0, true, "hammer", "pose_two_handed_heavy",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.SLASH_DOWN, "two_handed_slam", null)
            }
    );

    /**
     * Spears: Source-parity single stab, long range, two-handed.
     * Uses {@code two_handed_stab_right} animation.
     */
    private static final WeaponAttributes SPEAR = new WeaponAttributes(
            1.0, true, "spear", "pose_two_handed_polearm",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.STAB, "two_handed_stab_right", null)
            }
    );

    /**
     * Glaives: Source-parity 3-hit polearm combo (chop, chop, wide sweep).
     */
    private static final WeaponAttributes GLAIVE = new WeaponAttributes(
            0.5, true, "glaive", "pose_two_handed_polearm",
            new Attack[]{
                    new Attack(HitboxShape.VERTICAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_RIGHT, "two_handed_slash_vertical_right", null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 0.8, 150, 0.5, SwingDirection.SLASH_LEFT, "two_handed_slash_vertical_left", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.4, 180, 0.5, SwingDirection.SLASH_RIGHT, "two_handed_slash_horizontal_right", null)
            }
    );

    /**
     * Sickles: Source-parity 4-hit combo — 2 horizontal slashes + 2 conditional
     * dual-wielding cross/uncross attacks.
     */
    private static final WeaponAttributes SICKLE = new WeaponAttributes(
            -0.25, false, "sickle",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 120, 0.5, SwingDirection.SLASH_RIGHT, "one_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.9, 120, 0.5, SwingDirection.SLASH_LEFT, "one_handed_slash_horizontal_left", null),
                    new Attack(HitboxShape.FORWARD_BOX, 1.2, 0, 0.5, SwingDirection.SLASH_RIGHT, "dual_handed_slash_cross",
                            new Condition[]{ Condition.DUAL_WIELDING_SAME_CATEGORY, Condition.MAIN_HAND_ONLY }),
                    new Attack(HitboxShape.FORWARD_BOX, 1.3, 0, 0.5, SwingDirection.SLASH_LEFT, "dual_handed_slash_uncross",
                            new Condition[]{ Condition.DUAL_WIELDING_SAME_CATEGORY, Condition.OFF_HAND_ONLY })
            }
    );

    /**
     * Wands: Source-parity single stab cast (uses {@code one_handed_stab} animation).
     */
    private static final WeaponAttributes WAND = new WeaponAttributes(
            -0.5, false, "wand",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.STAB, "one_handed_stab", null)
            }
    );

    /**
     * Staves: 3-hit combo (swing, swing, overhead slam).
     * Two-handed spell catalyst, escalating damage (0.8/1.0/1.2).
     */
    private static final WeaponAttributes STAFF = new WeaponAttributes(
            0.25, true, "staff",
            new Attack[]{
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 0.8, 160, 0.5, SwingDirection.SLASH_RIGHT, "two_handed_slash_horizontal_right", null),
                    new Attack(HitboxShape.HORIZONTAL_PLANE, 1.0, 160, 0.5, SwingDirection.SLASH_LEFT, "two_handed_slash_horizontal_left", null),
                    new Attack(HitboxShape.VERTICAL_PLANE, 1.2, 120, 0.5, SwingDirection.SLASH_DOWN, "two_handed_slam", null)
            }
    );

    /**
     * Trident: Source-parity one-handed spear variant (extends SPEAR, but category "trident").
     */
    private static final WeaponAttributes TRIDENT = new WeaponAttributes(
            0.25, false, "trident",
            new Attack[]{
                    new Attack(HitboxShape.FORWARD_BOX, 1.0, 0, 0.5, SwingDirection.STAB, "one_handed_stab", null)
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
