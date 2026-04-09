package com.ultra.megamod.feature.sorting;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;

import java.util.Comparator;

public enum SortAlgorithm {
    NAME("Name", Comparator.comparing(s -> s.getHoverName().getString())),
    ID("ID", Comparator.comparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString())),
    CATEGORY("Category", Comparator.comparingInt(SortAlgorithm::getCategoryOrdinal)
        .thenComparing(s -> s.getHoverName().getString())),
    COUNT("Stack Size", Comparator.<ItemStack, Integer>comparing(ItemStack::getCount).reversed()
        .thenComparing(s -> s.getHoverName().getString())),
    RARITY("Rarity", Comparator.<ItemStack, Integer>comparing(s -> s.getRarity().ordinal()).reversed()
        .thenComparing(s -> s.getHoverName().getString()));

    private final String displayName;
    private final Comparator<ItemStack> comparator;

    SortAlgorithm(String displayName, Comparator<ItemStack> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public String getDisplayName() { return displayName; }
    public Comparator<ItemStack> getComparator() { return comparator; }

    private static int getCategoryOrdinal(ItemStack stack) {
        Item item = stack.getItem();
        // Use registry path for categorization since item subclasses changed in 1.21.11
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        if (path.contains("sword") || path.contains("axe") || path.contains("trident")) return 0;
        if (path.contains("helmet") || path.contains("chestplate") || path.contains("leggings")
            || path.contains("boots") || path.contains("shield")) return 1;
        if (path.contains("pickaxe") || path.contains("shovel") || path.contains("hoe")) return 2;
        if (item instanceof BlockItem) return 3;
        if (stack.has(DataComponents.FOOD)) return 4;
        if (path.contains("bow") || path.contains("crossbow") || path.contains("arrow")) return 5;
        if (path.contains("potion") || path.contains("splash") || path.contains("lingering")) return 6;
        return 7;
    }

    public static SortAlgorithm fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NAME;
        }
    }
}
