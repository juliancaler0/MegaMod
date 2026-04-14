package com.ultra.megamod.feature.combat.rogues.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.rogues.item.armor.Armors;
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
 * Creative tab + Group key for Rogues & Warriors items.
 * Mirrors the Paladins {@link com.ultra.megamod.feature.combat.paladins.item.Group} layout.
 */
public class Group {
    public static final Identifier GROUP_ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "rogues");
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, GROUP_ID);

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MegaMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ROGUES_TAB = CREATIVE_TABS.register(
            "rogues",
            () -> CreativeModeTab.builder()
                    .icon(() -> {
                        var set = Armors.assassinArmorSet;
                        if (set != null && set.head != null) {
                            return new ItemStack(set.head);
                        }
                        return ItemStack.EMPTY;
                    })
                    .title(Component.translatable("itemGroup.megamod.rogues"))
                    .build()
    );

    public static void init(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
