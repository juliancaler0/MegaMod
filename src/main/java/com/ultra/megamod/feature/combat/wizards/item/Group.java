package com.ultra.megamod.feature.combat.wizards.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.wizards.item.armor.Armors;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative mode tab for the Wizards content port.
 * Mirrors the Paladins {@code Group} class.
 */
public class Group {
    public static final Identifier GROUP_ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "wizards");
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, GROUP_ID);

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MegaMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WIZARDS_TAB = CREATIVE_TABS.register(
            "wizards",
            () -> CreativeModeTab.builder()
                    .icon(() -> {
                        var set = Armors.wizardRobeSet;
                        if (set != null && set.head != null) {
                            return new ItemStack(set.head);
                        }
                        return ItemStack.EMPTY;
                    })
                    .title(Component.translatable("itemGroup.megamod.wizards"))
                    .build()
    );

    public static void init(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
