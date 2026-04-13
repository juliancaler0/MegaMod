package com.ultra.megamod.feature.combat.jewelry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Factory for creating jewelry items, ported 1:1 from the Jewelry mod reference.
 *
 * The default factory creates VanillaJewelryItem instances.
 * When Accessories is loaded, JewelryAccessoriesHelper replaces the factory
 * to create JewelryAccessoriesItem instances with proper slot-aware attribute components.
 */
public class JewelryItemFactory {

    public record ItemArgs(
            Item.Properties settings,
            @Nullable ItemAttributeModifiers attributes,
            @Nullable String lore,
            @Nullable String slot
    ) {}

    /**
     * The current factory function. Default creates vanilla items;
     * may be replaced by JewelryAccessoriesHelper for Accessories integration.
     */
    public static Function<ItemArgs, Item> factory = args -> {
        var settings = args.settings;
        if (args.attributes != null) {
            settings.attributes(args.attributes);
        }
        return new VanillaJewelryItem(settings, args.lore);
    };

    public static Function<ItemArgs, Item> getFactory() {
        return factory;
    }
}
