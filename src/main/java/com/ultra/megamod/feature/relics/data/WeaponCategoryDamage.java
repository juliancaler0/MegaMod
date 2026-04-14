package com.ultra.megamod.feature.relics.data;

import java.util.Map;

/**
 * Target base attack damage per weapon category so all categories share a similar
 * DPS curve at tier 5 (arsenal uniques, common rarity).
 *
 * <p>Design: {@code DPS_target = 10 + (1.6 - speed) × 2}. This gives slower weapons
 * a modest DPS edge (~15% at hammer speed 0.8) as compensation for commitment +
 * harder-to-land swings, while keeping fast weapons viable since they land more
 * hits per opening and proc passives more often.</p>
 *
 * <p>Formula: {@code baseDamage = DPS_target / speed}. Rounded to integer, min 3.</p>
 *
 * <p>Final per-swing damage = {@code baseDamage × rarity.damageMultiplier + rolled_bonus}.</p>
 */
public final class WeaponCategoryDamage {
    private WeaponCategoryDamage() {}

    /** Fallback when category isn't recognised. Sword-tier (speed 1.6 → DPS 10). */
    private static final float DEFAULT = 6f;

    // Pre-computed base damages per category (DPS = 10 + (1.6 - speed) × 2, rounded)
    //   Hammer     0.8 spd × 14 dmg = 11.2 DPS
    //   Claymore   0.9 spd × 13 dmg = 11.7 DPS
    //   Double Axe 1.0 spd × 11 dmg = 11.0 DPS
    //   Glaive     1.1 spd × 10 dmg = 11.0 DPS
    //   Mace       1.2 spd ×  9 dmg = 10.8 DPS
    //   Spear      1.3 spd ×  8 dmg = 10.4 DPS
    //   Sickle     1.4 spd ×  7 dmg =  9.8 DPS
    //   Katana     1.5 spd ×  7 dmg = 10.5 DPS
    //   Longsword  1.5 spd ×  7 dmg = 10.5 DPS
    //   Sword      1.6 spd ×  6 dmg =  9.6 DPS
    //   Throw Axe  1.6 spd ×  6 dmg =  9.6 DPS
    //   Rapier     1.8 spd ×  5 dmg =  9.0 DPS
    //   Dagger     2.4 spd ×  4 dmg =  9.6 DPS
    //   Staff      1.6 spd ×  4 dmg =  6.4 DPS (casting weapon, spell-focused)
    //   Wand       2.0 spd ×  3 dmg =  6.0 DPS (casting weapon)
    //   Shield     1.6 spd ×  2 dmg =  3.2 DPS (defensive)
    //   Longbow    1.5 spd ×  8 dmg = 12.0 DPS (ranged — higher since bow pull mechanics)
    //   Crossbow   1.2 spd ×  9 dmg = 10.8 DPS
    //   Heavy Xbow 1.0 spd × 10 dmg = 10.0 DPS
    private static final Map<String, Float> CATEGORY_DAMAGE = Map.ofEntries(
            Map.entry("hammer",         14f),
            Map.entry("claymore",       13f),
            Map.entry("greatsword",     13f),
            Map.entry("double_axe",     11f),
            Map.entry("glaive",         10f),
            Map.entry("mace",            9f),
            Map.entry("spear",           8f),
            Map.entry("sickle",          7f),
            Map.entry("katana",          7f),
            Map.entry("longsword",       7f),
            Map.entry("sword",           6f),
            Map.entry("throwing_axe",    6f),
            Map.entry("rapier",          5f),
            Map.entry("dagger",          4f),
            Map.entry("staff",           4f),
            Map.entry("wand",            3f),
            Map.entry("shield",          2f),
            Map.entry("longbow",         8f),
            Map.entry("crossbow",        9f),
            Map.entry("heavy_crossbow", 10f)
    );

    /** Returns the category-balanced base damage for the given item registry name. */
    public static float getBaseDamage(String itemRegistryName) {
        String category = WeaponCategorySpeed.getCategory(itemRegistryName);
        return CATEGORY_DAMAGE.getOrDefault(category, DEFAULT);
    }
}
