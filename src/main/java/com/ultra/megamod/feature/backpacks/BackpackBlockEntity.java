package com.ultra.megamod.feature.backpacks;

import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the placed backpack block.
 * Stores backpack contents, variant, and tier in-memory.
 * When broken, converts back to a BackpackItem with all data preserved.
 */
public class BackpackBlockEntity extends BlockEntity implements Container {
    private ItemStack[] items;
    private ItemStack[] toolItems;
    private BackpackVariant variant = BackpackVariant.STANDARD;
    private BackpackTier tier = BackpackTier.LEATHER;
    private String ownerName = "";

    // Original item data (CustomData from the placed item) to preserve upgrades, etc.
    private CompoundTag originalItemData = new CompoundTag();


    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackRegistry.BACKPACK_BE.get(), pos, state);
        this.items = new ItemStack[tier.getStorageSlots()];
        for (int i = 0; i < items.length; i++) items[i] = ItemStack.EMPTY;
        this.toolItems = new ItemStack[tier.getToolSlots()];
        for (int i = 0; i < toolItems.length; i++) toolItems[i] = ItemStack.EMPTY;
    }

    /**
     * Load block entity data from a placed BackpackItem stack.
     */
    public void fromItemStack(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem bpItem)) return;

        this.variant = bpItem.getVariant();
        this.tier = bpItem.getTier(stack);

        // Resize items array to match tier
        this.items = new ItemStack[tier.getStorageSlots()];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }

        // Preserve the full custom data from the item
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            this.originalItemData = customData.copyTag();
        }

        // Load stored items from the item's custom data
        SimpleContainer loaded = BackpackMenu.loadFromItemStack(stack, tier);
        for (int i = 0; i < loaded.getContainerSize() && i < items.length; i++) {
            items[i] = loaded.getItem(i);
        }

        // Resize and load tool items
        this.toolItems = new ItemStack[tier.getToolSlots()];
        for (int i = 0; i < toolItems.length; i++) toolItems[i] = ItemStack.EMPTY;
        SimpleContainer loadedTools = BackpackMenu.loadToolsFromItemStack(stack, tier);
        for (int i = 0; i < loadedTools.getContainerSize() && i < toolItems.length; i++) {
            toolItems[i] = loadedTools.getItem(i);
        }

        setChanged();
    }

    /**
     * Convert block entity back to a BackpackItem with all contents and data preserved.
     */
    public ItemStack toItemStack() {
        // Get the registered BackpackItem for this variant
        var deferredItem = BackpackRegistry.getItem(variant);
        if (deferredItem == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(deferredItem.get());

        // Restore original custom data (tier, upgrades, etc.)
        CompoundTag tag = originalItemData.copy();
        tag.putString("BackpackTier", tier.name());

        // Save current inventory into the item
        ListTag itemList = new ListTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putString("id", BuiltInRegistries.ITEM.getKey(items[i].getItem()).toString());
                itemTag.putInt("count", items[i].getCount());
                itemList.add(itemTag);
            }
        }
        tag.put("BackpackItems", itemList);

        // Save tool items
        ListTag toolList = new ListTag();
        for (int i = 0; i < toolItems.length; i++) {
            if (!toolItems[i].isEmpty()) {
                CompoundTag toolTag = new CompoundTag();
                toolTag.putInt("Slot", i);
                toolTag.putString("id", BuiltInRegistries.ITEM.getKey(toolItems[i].getItem()).toString());
                toolTag.putInt("count", toolItems[i].getCount());
                toolList.add(toolTag);
            }
        }
        tag.put("BackpackTools", toolList);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return stack;
    }

    /**
     * Create a SimpleContainer view of the current items (for opening BackpackMenu).
     */
    public SimpleContainer toContainer() {
        SimpleContainer container = new SimpleContainer(items.length);
        for (int i = 0; i < items.length; i++) {
            container.setItem(i, items[i].copy());
        }
        return container;
    }

    /**
     * Save items from a container back into this block entity (after menu closes).
     */
    public void fromContainer(Container container) {
        int size = Math.min(container.getContainerSize(), items.length);
        for (int i = 0; i < size; i++) {
            items[i] = container.getItem(i).copy();
        }
        setChanged();
    }

    public SimpleContainer toToolContainer() {
        SimpleContainer container = new SimpleContainer(toolItems.length);
        for (int i = 0; i < toolItems.length; i++) {
            container.setItem(i, toolItems[i].copy());
        }
        return container;
    }

    public void fromToolContainer(Container container) {
        int size = Math.min(container.getContainerSize(), toolItems.length);
        for (int i = 0; i < size; i++) {
            toolItems[i] = container.getItem(i).copy();
        }
        setChanged();
    }

    public BackpackVariant getVariant() { return variant; }
    public BackpackTier getTier() { return tier; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String name) { this.ownerName = name; }

    // ============ Container interface ============

    @Override
    public int getContainerSize() {
        return items.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.length ? items[slot] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= items.length || items[slot].isEmpty()) return ItemStack.EMPTY;
        ItemStack result = items[slot].split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= items.length) return ItemStack.EMPTY;
        ItemStack stack = items[slot];
        items[slot] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.length) {
            items[slot] = stack;
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items[i]);
                items[i] = ItemStack.EMPTY;
            }
        }
    }
}
