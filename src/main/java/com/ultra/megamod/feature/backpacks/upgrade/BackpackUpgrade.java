package com.ultra.megamod.feature.backpacks.upgrade;

import com.ultra.megamod.feature.backpacks.BackpackTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for all backpack upgrades.
 * Each upgrade (crafting, furnace, magnet, tanks, etc.) extends this.
 * Upgrades are instantiated when an UpgradeItem is placed in an upgrade slot,
 * and destroyed when the item is removed.
 */
public abstract class BackpackUpgrade {

    protected boolean active = true;
    protected boolean tabOpen = false;

    /** Unique ID (e.g., "crafting", "furnace", "magnet") */
    public abstract String getId();

    /** Display name for UI */
    public abstract String getDisplayName();

    /** Number of additional slots this upgrade adds to the menu (e.g., 10 for crafting grid+result) */
    public int getSlotCount() { return 0; }

    /** Create the slots this upgrade needs. Called when the upgrade is installed. */
    public List<Slot> createSlots(SimpleContainer container, int baseX, int baseY) {
        return Collections.emptyList();
    }

    /** Create the container for this upgrade's slots. */
    public SimpleContainer createContainer() {
        int slots = getSlotCount();
        return slots > 0 ? new SimpleContainer(slots) : null;
    }

    /** Tab width/height when open. Return {width, height}. */
    public int getTabWidth() { return 24; }
    public int getTabHeight() { return 24; }

    /** Icon UV coordinates in icons.png for the tab icon. Return {u, v}. */
    public int getIconU() { return 0; }
    public int getIconV() { return 0; }

    /** Whether this upgrade is currently active. */
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Whether the tab is currently open (expanded). */
    public boolean isTabOpen() { return tabOpen; }
    public void setTabOpen(boolean open) { this.tabOpen = open; }

    /** Called when the upgrade is first installed. */
    public void onInstalled(ItemStack backpackStack, BackpackTier tier) {}

    /** Called when the upgrade is removed. Return items to drop. */
    public List<ItemStack> onRemoved() { return Collections.emptyList(); }

    /** Save upgrade-specific data to a CompoundTag. */
    public void saveToTag(CompoundTag tag) {
        tag.putBoolean("Active", active);
        tag.putBoolean("TabOpen", tabOpen);
    }

    /** Load upgrade-specific data from a CompoundTag. */
    public void loadFromTag(CompoundTag tag) {
        this.active = tag.getBooleanOr("Active", true);
        this.tabOpen = tag.getBooleanOr("TabOpen", false);
    }
}
