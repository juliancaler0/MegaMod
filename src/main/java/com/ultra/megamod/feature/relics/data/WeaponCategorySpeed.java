package com.ultra.megamod.feature.relics.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps a weapon's registry name to its BetterCombat-style base attack speed
 * AND its weapon category (used to look up weapon_attributes JSON for animation).
 *
 * <p>Vanilla default attack speed is 4.0 (0.25s = 5 ticks). BetterCombat weapons
 * are slower — e.g. a claymore should be ~0.9 attack speed (22 ticks between
 * swings), giving swings the proper heavy feel.</p>
 *
 * <p>Minecraft's ATTACK_SPEED attribute defaults to 4.0 and item modifiers are
 * ADD_VALUE deltas. A sword with target 1.6 applies {@code 1.6 - 4.0 = -2.4}.</p>
 *
 * <p>Recognition order: longest keyword wins (so "heavy_crossbow" beats "crossbow"),
 * then lexical match. Unique name mappings (e.g., "vampiric_tome" → staff) are
 * resolved first for custom relic weapons.</p>
 */
public final class WeaponCategorySpeed {
    private WeaponCategorySpeed() {}

    /** Vanilla default attack speed. Item modifiers are applied as deltas from this value. */
    public static final double VANILLA_DEFAULT = 4.0;

    /** Fallback category + speed when no match found. Sword-tier 1H. */
    public static final String DEFAULT_CATEGORY = "sword";
    private static final double DEFAULT_SPEED = 1.6;

    /**
     * Hand-curated mapping of specific relic/custom item paths to categories.
     * These override keyword matching. Insertion order matters for readability
     * but lookups are exact-match.
     */
    private static final Map<String, String> SPECIFIC = new LinkedHashMap<>();
    static {
        // Relic legendary weapons — category determined by lore/mechanics, not name
        SPECIFIC.put("vampiric_tome",       "staff");    // spell-casting tome, 2H
        SPECIFIC.put("static_seeker",       "sword");    // lightning 1H blade
        SPECIFIC.put("battledancer",        "sword");    // dual-wield-capable, fast 1H
        SPECIFIC.put("ebonchill",           "staff");    // frost 2H stave
        SPECIFIC.put("lightbinder",         "staff");    // holy 2H stave
        SPECIFIC.put("crescent_blade",      "dagger");   // scimitar/curved 1H fast
        SPECIFIC.put("ghost_fang",          "dagger");   // spectral 1H fast
        SPECIFIC.put("terra_warhammer",     "hammer");   // earth 2H heavy
        SPECIFIC.put("soka_singing_blade",  "claymore"); // dimensional rift 2H
        SPECIFIC.put("voidreaver",          "claymore"); // void 2H greatsword
        SPECIFIC.put("solaris",             "hammer");   // holy fire 2H blunt
        SPECIFIC.put("stormfury",           "double_axe"); // lightning 2H axe
        SPECIFIC.put("briarthorn",          "spear");    // nature polearm
        SPECIFIC.put("abyssal_trident",     "spear");    // trident/polearm
        SPECIFIC.put("pyroclast",           "hammer");   // fire 2H blunt
        SPECIFIC.put("whisperwind",         "longbow");  // wind ranged
        SPECIFIC.put("soulchain",           "dagger");   // chain/whip (fast reach)
        SPECIFIC.put("shadow_glaive",       "glaive");   // 2H polearm slash
    }

    /**
     * Keyword → category map for pattern matching. Longer keys are checked first
     * to avoid false matches (e.g., "heavy_crossbow" before "crossbow").
     */
    private static final Map<String, String> KEYWORD = new LinkedHashMap<>();
    static {
        // Ranged (check before "bow"/"crossbow")
        KEYWORD.put("heavy_crossbow",    "heavy_crossbow");
        KEYWORD.put("throwing_axe",      "throwing_axe");
        KEYWORD.put("longbow",           "longbow");
        KEYWORD.put("shortbow",          "longbow");
        KEYWORD.put("crossbow",          "crossbow");
        // 2H heavies (check before generic "axe", "hammer")
        KEYWORD.put("double_axe",        "double_axe");
        KEYWORD.put("great_hammer",      "hammer");
        KEYWORD.put("warhammer",         "hammer");
        KEYWORD.put("greatshield",       "shield");
        KEYWORD.put("greatsword",        "claymore");
        KEYWORD.put("longsword",         "longsword");
        KEYWORD.put("claymore",          "claymore");
        KEYWORD.put("hammer",            "hammer");
        KEYWORD.put("glaive",            "glaive");
        KEYWORD.put("halberd",           "glaive");
        KEYWORD.put("scythe",            "sickle");
        KEYWORD.put("sickle",            "sickle");
        KEYWORD.put("spear",             "spear");
        KEYWORD.put("lance",             "spear");
        KEYWORD.put("trident",           "spear");
        // 1H
        KEYWORD.put("katana",            "katana");
        KEYWORD.put("rapier",            "rapier");
        KEYWORD.put("cutlass",           "sword");
        KEYWORD.put("mace",              "mace");
        KEYWORD.put("dagger",            "dagger");
        KEYWORD.put("knife",             "dagger");
        KEYWORD.put("shiv",              "dagger");
        KEYWORD.put("fang",              "dagger");
        KEYWORD.put("sword",             "sword");
        KEYWORD.put("blade",             "sword");
        KEYWORD.put("whip",              "dagger");
        // Casting
        KEYWORD.put("battlestaff",       "staff");
        KEYWORD.put("staff_damage",      "staff");
        KEYWORD.put("staff_heal",        "staff");
        KEYWORD.put("staff",             "staff");
        KEYWORD.put("tome",              "staff");
        KEYWORD.put("wand",              "wand");
        // Defensive
        KEYWORD.put("shield",            "shield");
        // Misc
        KEYWORD.put("axe",               "sword");  // axes use sword-like slash animations when no category
    }

    /**
     * Target ATTACK_SPEED values per category (what the weapon's final value should equal).
     * Lower = slower = heavier feel.
     */
    private static final Map<String, Double> CATEGORY_SPEED = Map.ofEntries(
            Map.entry("claymore",        0.9),
            Map.entry("greatsword",      0.9),
            Map.entry("hammer",          0.8),
            Map.entry("double_axe",      1.0),
            Map.entry("glaive",          1.1),
            Map.entry("mace",            1.2),
            Map.entry("spear",           1.3),
            Map.entry("sickle",          1.4),
            Map.entry("katana",          1.5),
            Map.entry("longsword",       1.5),
            Map.entry("sword",           1.6),
            Map.entry("rapier",          1.8),
            Map.entry("throwing_axe",    1.6),
            Map.entry("dagger",          2.4),
            // Casting — brisk swings; spell haste handles casting speed separately
            Map.entry("staff",           1.6),
            Map.entry("wand",            2.0),
            // Ranged — attack_speed doesn't apply to bow pull, keep near vanilla
            Map.entry("longbow",         1.5),
            Map.entry("crossbow",        1.2),
            Map.entry("heavy_crossbow",  1.0),
            Map.entry("shield",          1.6) // shield bash
    );

    /**
     * Resolve the category for a given item registry name by checking (1) specific
     * overrides, (2) longest keyword match, (3) fallback.
     */
    public static String getCategory(String itemRegistryName) {
        if (itemRegistryName == null) return DEFAULT_CATEGORY;
        int colon = itemRegistryName.indexOf(':');
        String path = colon >= 0 ? itemRegistryName.substring(colon + 1) : itemRegistryName;

        String direct = SPECIFIC.get(path);
        if (direct != null) return direct;

        String best = null;
        int bestLen = 0;
        for (var entry : KEYWORD.entrySet()) {
            if (path.contains(entry.getKey()) && entry.getKey().length() > bestLen) {
                best = entry.getValue();
                bestLen = entry.getKey().length();
            }
        }
        return best != null ? best : DEFAULT_CATEGORY;
    }

    /**
     * Target ATTACK_SPEED (weapon's final value) for the given item.
     */
    public static double getTargetAttackSpeed(String itemRegistryName) {
        String category = getCategory(itemRegistryName);
        return CATEGORY_SPEED.getOrDefault(category, DEFAULT_SPEED);
    }

    /**
     * Convert a target attack speed into the modifier delta to add to vanilla's 4.0.
     */
    public static double toModifierDelta(double targetAttackSpeed) {
        return targetAttackSpeed - VANILLA_DEFAULT;
    }
}
