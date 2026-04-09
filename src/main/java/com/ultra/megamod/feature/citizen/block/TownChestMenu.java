package com.ultra.megamod.feature.citizen.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Town Chest. Uses proxy slots for the visible chest rows
 * so that scrolling works with AbstractContainerScreen's rendering and click handling.
 * Only VISIBLE_ROWS * 9 chest slots exist in the menu at any time, mapped to the scroll position.
 */
public class TownChestMenu extends AbstractContainerMenu {

    public static final int CHEST_SIZE = TownChestBlockEntity.SIZE; // 324
    public static final int COLS = 9;
    public static final int VISIBLE_ROWS = 6;
    public static final int TOTAL_ROWS = CHEST_SIZE / COLS; // 36 rows
    public static final int VISIBLE_SLOTS = VISIBLE_ROWS * COLS; // 54

    private final Container chestContainer;
    private int scrollRow = 0;

    // Server constructor
    public TownChestMenu(int containerId, Inventory playerInv, Container chest) {
        super(TownChestRegistry.TOWN_CHEST_MENU.get(), containerId);
        this.chestContainer = chest;
        chest.startOpen(playerInv.player);

        // Add only VISIBLE_ROWS worth of proxy slots (54 slots) for the chest area
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int containerIndex = row * COLS + col; // initial: rows 0-5
                this.addSlot(new ScrollableSlot(chest, containerIndex, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (3 rows of 9) — positioned below the visible chest area
        int playerInvY = 18 + VISIBLE_ROWS * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        // Player hotbar
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    // Client constructor
    public TownChestMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, new SimpleContainer(CHEST_SIZE));
        buf.readBlockPos();
    }

    public void setScrollRow(int row) {
        int newRow = Math.max(0, Math.min(row, TOTAL_ROWS - VISIBLE_ROWS));
        if (newRow != this.scrollRow) {
            this.scrollRow = newRow;
            updateProxySlots();
        }
    }

    public int getScrollRow() {
        return scrollRow;
    }

    public int getMaxScrollRow() {
        return TOTAL_ROWS - VISIBLE_ROWS;
    }

    /**
     * Updates the proxy slots to point to the correct container indices based on scroll position.
     */
    private void updateProxySlots() {
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            int row = i / COLS;
            int actualRow = row + scrollRow;
            int col = i % COLS;
            int containerIndex = actualRow * COLS + col;
            Slot slot = this.slots.get(i);
            if (slot instanceof ScrollableSlot ss) {
                ss.setContainerIndex(containerIndex);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index < VISIBLE_SLOTS) {
                // From chest proxy slot -> player inventory
                if (!this.moveItemStackTo(slotStack, VISIBLE_SLOTS, VISIBLE_SLOTS + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory -> chest (try all 324 real slots via the container directly)
                ItemStack leftover = insertIntoChest(slotStack);
                if (leftover.getCount() == result.getCount()) {
                    return ItemStack.EMPTY; // nothing moved
                }
                slotStack.setCount(leftover.getCount());
            }
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    /**
     * Inserts a stack into the full chest container (all 324 slots), not just visible proxy slots.
     */
    private ItemStack insertIntoChest(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        // First merge with existing stacks
        for (int i = 0; i < CHEST_SIZE; i++) {
            ItemStack existing = chestContainer.getItem(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, stack.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    chestContainer.setChanged();
                    if (stack.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        // Then empty slots
        for (int i = 0; i < CHEST_SIZE; i++) {
            if (chestContainer.getItem(i).isEmpty()) {
                chestContainer.setItem(i, stack.copy());
                stack.setCount(0);
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return chestContainer.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        chestContainer.stopOpen(player);
    }

    /**
     * Returns the total number of items in the full chest (for display).
     */
    public int getUsedSlotCount() {
        int used = 0;
        for (int i = 0; i < CHEST_SIZE; i++) {
            if (!chestContainer.getItem(i).isEmpty()) used++;
        }
        return used;
    }

    /**
     * A slot that can change which container index it references.
     * Used for scrollable chest viewing - the slot position stays fixed,
     * but the backing data changes when the user scrolls.
     */
    public static class ScrollableSlot extends Slot {
        private int dynamicIndex;

        public ScrollableSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.dynamicIndex = index;
        }

        public void setContainerIndex(int index) {
            this.dynamicIndex = index;
        }

        @Override
        public ItemStack getItem() {
            return this.container.getItem(this.dynamicIndex);
        }

        @Override
        public void set(ItemStack stack) {
            this.container.setItem(this.dynamicIndex, stack);
            this.setChanged();
        }

        @Override
        public ItemStack remove(int amount) {
            return this.container.removeItem(this.dynamicIndex, amount);
        }

        @Override
        public boolean hasItem() {
            return !this.container.getItem(this.dynamicIndex).isEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return this.container.getMaxStackSize();
        }
    }
}
