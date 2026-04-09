package com.ultra.megamod.feature.alchemy.block;

import com.ultra.megamod.feature.alchemy.AlchemyRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Alchemy Shelf. 54 shelf slots (6 rows of 9) + 36 player slots.
 * Only allows potions and reagents in the shelf slots.
 */
public class AlchemyShelfMenu extends AbstractContainerMenu {

    public static final int SHELF_SIZE = AlchemyShelfBlockEntity.SIZE; // 54
    public static final int ROWS = 6;
    public static final int COLS = 9;

    private final Container shelfContainer;

    // Server constructor
    public AlchemyShelfMenu(int containerId, Inventory playerInv, Container shelf) {
        super(AlchemyRegistry.ALCHEMY_SHELF_MENU.get(), containerId);
        this.shelfContainer = shelf;
        shelf.startOpen(playerInv.player);

        // Shelf slots: 6 rows of 9
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                this.addSlot(new AlchemyShelfSlot(shelf, col + row * COLS, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (3 rows of 9)
        int playerInvY = 18 + ROWS * 18 + 14;
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
    public AlchemyShelfMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, new SimpleContainer(SHELF_SIZE));
        buf.readBlockPos();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index < SHELF_SIZE) {
                // Moving from shelf to player inventory
                if (!this.moveItemStackTo(slotStack, SHELF_SIZE, SHELF_SIZE + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player to shelf — only if allowed
                if (AlchemyShelfBlockEntity.isAllowedItem(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, SHELF_SIZE, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return shelfContainer.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        shelfContainer.stopOpen(player);
    }

    /**
     * Custom slot that only accepts potions and reagents.
     */
    private static class AlchemyShelfSlot extends Slot {
        public AlchemyShelfSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return AlchemyShelfBlockEntity.isAllowedItem(stack);
        }
    }
}
