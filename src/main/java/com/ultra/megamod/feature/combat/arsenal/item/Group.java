package com.ultra.megamod.feature.combat.arsenal.item;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Group {
    public static final Identifier GROUP_ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "arsenal");
    public static final String translationKey = "itemGroup." + GROUP_ID.getNamespace() + "." + GROUP_ID.getPath();
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, GROUP_ID);

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MegaMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARSENAL_TAB = CREATIVE_TABS.register(
            "arsenal",
            () -> CreativeModeTab.builder()
                    .icon(() -> {
                        if (!ArsenalWeapons.entries.isEmpty() && ArsenalWeapons.entries.get(0).item() != null) {
                            return new ItemStack(ArsenalWeapons.entries.get(0).item());
                        }
                        return ItemStack.EMPTY;
                    })
                    .title(Component.translatable(translationKey))
                    .build()
    );

    public static void init(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
