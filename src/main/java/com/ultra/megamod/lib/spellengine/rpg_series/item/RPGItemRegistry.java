package com.ultra.megamod.lib.spellengine.rpg_series.item;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Shared DeferredRegister for all RPG series items (weapons, armor, shields, ranged weapons).
 * Uses DeferredRegister.createItems() to ensure Item.Properties gets the ResourceKey
 * injected automatically, which is required in NeoForge 1.21.11.
 *
 * <p>In NeoForge 1.21.11, Item.Properties must have its ResourceKey set before the
 * Item constructor is called. Use {@link #registerItem(String, Function)} to get
 * pre-configured properties, or use {@link #register(String, Function)} to configure
 * your own properties but receive the ResourceKey to set on them.</p>
 */
public class RPGItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    /**
     * Register an item using a factory that receives pre-configured Item.Properties
     * with the ResourceKey already set. This is the preferred registration method.
     */
    public static Supplier<Item> registerItem(String name, Function<Item.Properties, ? extends Item> factory) {
        return ITEMS.registerItem(name, factory::apply);
    }

    /**
     * Register an item with a factory that receives the ResourceKey. The factory must
     * call {@code properties.setId(key)} on its Item.Properties before constructing the item.
     */
    public static Supplier<Item> register(String name, Function<ResourceKey<Item>, ? extends Item> factory) {
        var key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(MegaMod.MODID, name));
        return ITEMS.register(name, () -> factory.apply(key));
    }

    /**
     * Register an item with a simple supplier. The supplier must ensure the Item.Properties
     * has its ResourceKey set (via setId) before constructing the item.
     * Prefer {@link #registerItem(String, Function)} instead.
     */
    public static Supplier<Item> registerSimple(String name, Supplier<Item> supplier) {
        return ITEMS.register(name, supplier);
    }

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
