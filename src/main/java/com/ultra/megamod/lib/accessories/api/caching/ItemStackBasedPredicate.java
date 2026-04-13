package com.ultra.megamod.lib.accessories.api.caching;

import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.impl.caching.EquipmentLookupCache;
// REMOVED: OWO Scary annotation
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

/**
 * A custom implementation of {@link Predicate} for use with {@link EquipmentLookupCache} requireing
 * the implementation of {@link #isEqual} and {@link #hashCode} to have proper equality
 */
public abstract class ItemStackBasedPredicate implements Predicate<ItemStack> {

    private static final String UNKNOWN_PREDICATE = "UNKNOWN";

    private final String name;

    protected ItemStackBasedPredicate(String name) {
        this.name = name;
    }

    //--

    public static ItemStackBasedPredicate ofClass(Class<? extends ItemLike> clazz) {
        return ofClass(UNKNOWN_PREDICATE, clazz);
    }

    public static ItemStackBasedPredicate ofClass(String name, Class<? extends ItemLike> clazz) {
        return new ItemLikeClassPredicate(name, clazz);
    }

    public static ItemStackBasedPredicate ofItem(Item item) {
        return ofItem(UNKNOWN_PREDICATE, item);
    }

    public static ItemStackBasedPredicate ofItem(String name, Item item) {
        return new ItemPredicate(name, item);
    }

    public static ItemStackBasedPredicate ofComponents(DataComponentType<?>... dataComponentTypes) {
        return ofComponents(UNKNOWN_PREDICATE, dataComponentTypes);
    }

    public static ItemStackBasedPredicate ofComponents(String name, DataComponentType<?>... dataComponentTypes) {
        return new DataComponentsPredicate(name, dataComponentTypes);
    }

    /**
     * <strong>WARNING</strong>: it is recommended to either make a custom implementation of {@link ItemStackBasedPredicate}
     * to have the ability to cache any results for use within {@link AccessoriesCapability}. Reason behind this issue is
     * the fact that {@link Predicate}
     */
    @ApiStatus.Experimental
    public static ItemStackBasedPredicate ofPredicate(Predicate<ItemStack> predicate) {
        return ofPredicate(UNKNOWN_PREDICATE, predicate);
    }

    /**
     * <strong>WARNING</strong>: it is recommended to either make a custom implementation of {@link ItemStackBasedPredicate}
     * to have the ability to cache any results for use within {@link AccessoriesCapability}
     */
    @ApiStatus.Experimental
    public static ItemStackBasedPredicate ofPredicate(String name, Predicate<ItemStack> predicate) {
        if (predicate instanceof ItemStackBasedPredicate itemStackBasedPredicate) {
            return itemStackBasedPredicate;
        }

        return new ItemStackPredicate(name, predicate);
    }

    //--

    public final String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Name: " + this.name + ", " + extraStringData();
    }

    public abstract String extraStringData();

    @Override
    public abstract boolean test(ItemStack stack);

    @Override
    public abstract int hashCode();

    protected abstract boolean isEqual(Object other);

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!this.getClass().isInstance(other)) return false;

        return this.isEqual(other);
    }
}
