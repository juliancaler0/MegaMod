package com.ultra.megamod.feature.schematic.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A single entry in a material list: one item type with count totals.
 */
public class MaterialListEntry implements Comparable<MaterialListEntry> {

    private final Item item;
    private final int countTotal;
    private int countAvailable;

    public MaterialListEntry(Item item, int countTotal) {
        this.item = item;
        this.countTotal = countTotal;
        this.countAvailable = 0;
    }

    public Item getItem() { return item; }
    public int getCountTotal() { return countTotal; }
    public int getCountAvailable() { return countAvailable; }
    public void setCountAvailable(int count) { this.countAvailable = count; }

    public int getCountMissing() {
        return Math.max(0, countTotal - countAvailable);
    }

    public boolean isComplete() {
        return countAvailable >= countTotal;
    }

    /**
     * Returns a representative ItemStack for display (count = total needed).
     */
    public ItemStack getDisplayStack() {
        return new ItemStack(item, Math.min(countTotal, 64));
    }

    @Override
    public int compareTo(MaterialListEntry other) {
        // Sort by missing count descending, then by name ascending
        int cmp = Integer.compare(other.getCountMissing(), this.getCountMissing());
        if (cmp != 0) return cmp;
        return item.toString().compareTo(other.item.toString());
    }
}
