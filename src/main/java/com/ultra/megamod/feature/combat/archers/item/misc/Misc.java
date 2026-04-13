package com.ultra.megamod.feature.combat.archers.item.misc;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.feature.combat.archers.item.Quivers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Misc {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArchersMod.ID);

    public static final Supplier<Item> autoFireHookItem = ITEMS.register("auto_fire_hook",
            () -> new AutoFireHookItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        AutoFireHook.itemSupplier = autoFireHookItem;
        ITEMS.register(modEventBus);
        Quivers.register(modEventBus);
    }
}
