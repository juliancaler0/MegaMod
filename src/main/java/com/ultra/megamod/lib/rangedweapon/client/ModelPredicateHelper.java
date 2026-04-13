package com.ultra.megamod.lib.rangedweapon.client;

import com.ultra.megamod.lib.rangedweapon.api.CustomCrossbow;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.world.item.Item;

/**
 * In MC 1.21.11, the old ModelPredicateProviderRegistry / ItemProperties system was removed.
 * Item model predicates (pull, pulling, charged, etc.) are now defined via
 * JSON item model definitions using range_dispatch in assets/namespace/items/*.json.
 *
 * These methods are retained for API compatibility but are no-ops.
 * Custom bows/crossbows should define their item model predicates via JSON.
 */
public class ModelPredicateHelper {
    public static void registerBowModelPredicates(Item bow) {
        // No-op: In 1.21.11, bow pull predicates are handled via JSON item model definitions
        // (range_dispatch in assets/namespace/items/<bow>.json)
    }

    public static void registerCrossbowModelPredicates(CustomCrossbow crossbow) {
        // No-op: In 1.21.11, crossbow predicates are handled via JSON item model definitions
        // (range_dispatch in assets/namespace/items/<crossbow>.json)
    }
}
