package com.ultra.megamod.feature.combat.archers.item;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Quivers - simplified for NeoForge (no BundleAPI dependency).
 * Creates simple quiver items that are registered with the item registry.
 */
public class Quivers {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArchersMod.ID);
    public static final List<Entry> entries = new ArrayList<>();

    public record Entry(Identifier id, int capacity, Supplier<Item> itemSupplier) {
        public Item item() { return itemSupplier.get(); }
    }

    private static Entry entry(String name, int capacity, @Nullable Rarity rarity) {
        var settings = new Item.Properties().stacksTo(1);
        if (rarity != null) {
            settings.rarity(rarity);
        }
        final var finalSettings = settings;
        var id = Identifier.fromNamespaceAndPath(ArchersMod.ID, name);
        var holder = ITEMS.register(name, () -> new Item(finalSettings));
        var entry = new Entry(id, capacity, holder);
        entries.add(entry);
        return entry;
    }

    public static void register(IEventBus modEventBus) {
        entry("small_quiver", 4, null);
        entry("medium_quiver", 8, null);
        entry("large_quiver", 12, Rarity.UNCOMMON);

        ITEMS.register(modEventBus);
    }
}
