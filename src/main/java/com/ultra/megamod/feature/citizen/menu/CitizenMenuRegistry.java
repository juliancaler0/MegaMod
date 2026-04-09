package com.ultra.megamod.feature.citizen.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Old citizen menu registry - menus removed during MCEntityCitizen transition.
 * Kept as stub to avoid breaking CitizenRegistry.init() references.
 */
public class CitizenMenuRegistry {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            (ResourceKey) Registries.MENU, "megamod");

    public static void init(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
