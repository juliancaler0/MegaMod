package com.ultra.megamod.feature.combat.archers.item.misc;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.feature.combat.archers.component.ArcherComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class AutoFireHook {
    public static final Identifier id = Identifier.fromNamespaceAndPath(ArchersMod.ID, "auto_fire_hook");
    // Deferred — resolved after registry initialization
    static Supplier<Item> itemSupplier;
    public static final TagKey<Item> AFH_ATTACHABLE = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "auto_fire_hook_attachables"));

    public static Item item() {
        return itemSupplier.get();
    }

    public static boolean isApplied(ItemStack itemStack) {
        var component = itemStack.get(ArcherComponents.AUTO_FIRE);
        if (component == null) { return false; }
        return component;
    }

    public static void apply(ItemStack itemStack) {
        itemStack.set(ArcherComponents.AUTO_FIRE, true);
    }

    public static void remove(ItemStack itemStack) {
        itemStack.remove(ArcherComponents.AUTO_FIRE);
    }

    public static boolean isApplicable(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) { return false; }
        return (itemStack.getItem() instanceof CrossbowItem || itemStack.is(AFH_ATTACHABLE))
                && !isApplied(itemStack);
    }
}
