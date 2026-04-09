package com.ultra.megamod.feature.dimensions.resource;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ResourceDimensionRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "megamod");

    public static final DeferredItem<ResourceDimensionKeyItem> RESOURCE_DIMENSION_KEY = ITEMS.registerItem(
            "resource_dimension_key",
            props -> new ResourceDimensionKeyItem((Item.Properties) props),
            () -> new Item.Properties().stacksTo(1)
    );

    public static final Supplier<CreativeModeTab> RESOURCE_TAB = CREATIVE_MODE_TABS.register("megamod_resource_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("MegaMod - Resource Dimension"))
                    .icon(() -> new ItemStack((ItemLike) Items.DIAMOND))
                    .displayItems((parameters, output) -> {
                        output.accept((ItemLike) RESOURCE_DIMENSION_KEY.get());
                    })
                    .build()
    );

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
