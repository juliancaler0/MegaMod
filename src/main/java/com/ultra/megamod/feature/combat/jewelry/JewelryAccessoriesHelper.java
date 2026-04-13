package com.ultra.megamod.feature.combat.jewelry;

import com.ultra.megamod.lib.accessories.api.components.AccessoriesDataComponents;
import com.ultra.megamod.lib.accessories.api.components.AccessoryItemAttributeModifiers;

/**
 * Helper that registers the Accessories-compatible factory for jewelry items.
 * Ported 1:1 from the Jewelry mod reference's AccessoriesHelper.
 *
 * When Accessories is available, jewelry items are created as JewelryAccessoriesItem
 * instances with their attribute modifiers stored as AccessoryItemAttributeModifiers
 * components, ensuring proper slot-specific behavior.
 */
public class JewelryAccessoriesHelper {

    /**
     * Registers the factory that creates JewelryAccessoriesItem instances.
     * This replaces the default VanillaJewelryItem factory with one that
     * stores attribute modifiers as Accessories components for proper slot assignment.
     */
    public static void registerFactory() {
        JewelryItemFactory.factory = args -> {
            var settings = args.settings();
            var attributes = args.attributes();
            var slot = args.slot() != null ? args.slot() : "ring";

            if (attributes != null) {
                var builder = AccessoryItemAttributeModifiers.builder();
                for (var bonus : attributes.modifiers()) {
                    builder = builder.addForSlot(bonus.attribute(), bonus.modifier(), slot, true);
                }
                settings = settings.component(AccessoriesDataComponents.ATTRIBUTES.get(), builder.build());
            }

            return new JewelryAccessoriesItem(
                    settings,
                    args.lore(),
                    () -> JewelrySounds.getEquipSound());
        };
    }
}
