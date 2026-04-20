package com.ultra.megamod.feature.combat.relics.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Ported 1:1 from Relics-1.21.1's RelicFactory.
 * Default factory creates a vanilla Item; replaced at init time by AccessoriesHelper
 * to wrap items as RelicAccessoriesItem with AccessoryItemAttributeModifiers.
 */
public class RelicFactory {
    public record ItemArgs(Item.Properties settings, @Nullable ItemAttributeModifiers attributes) { }

    public static Function<ItemArgs, Item> factory = args -> {
        var settings = args.settings;
        if (args.attributes != null) {
            settings = settings.attributes(args.attributes);
        }
        return new Item(settings);
    };

    public static Function<ItemArgs, Item> getFactory() { return factory; }
}
