package com.ultra.megamod.feature.combat.relics.compat;

import com.ultra.megamod.feature.combat.relics.item.RelicFactory;
import com.ultra.megamod.lib.accessories.api.components.AccessoriesDataComponents;
import com.ultra.megamod.lib.accessories.api.components.AccessoryItemAttributeModifiers;

/**
 * Ported 1:1 from Relics-1.21.1's net.relics_rpgs.compat.AccessoriesHelper.
 * Replaces the default RelicFactory with one that stores attribute modifiers
 * as AccessoryItemAttributeModifiers components (slot-aware) instead of vanilla
 * ItemAttributeModifiers, and wraps items as RelicAccessoriesItem so the slot
 * system picks them up.
 */
public class AccessoriesHelper {
    public static void registerFactory() {
        RelicFactory.factory = args -> {
            var settings = args.settings();
            if (args.attributes() != null) {
                var builder = AccessoryItemAttributeModifiers.builder();
                for (var entry : args.attributes().modifiers()) {
                    builder = builder.addForSlot(entry.attribute(), entry.modifier(), "charm", true);
                }
                settings = settings.component(AccessoriesDataComponents.ATTRIBUTES.get(), builder.build());
            }
            return new RelicAccessoriesItem(settings);
        };
    }
}
