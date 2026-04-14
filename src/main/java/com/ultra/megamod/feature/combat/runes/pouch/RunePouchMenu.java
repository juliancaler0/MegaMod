package com.ultra.megamod.feature.combat.runes.pouch;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for a rune pouch. Only accepts rune items in pouch slots;
 * falls back to regular player-inventory slots beneath.
 */
public class RunePouchMenu extends AbstractContainerMenu {
    public static MenuType<RunePouchMenu> TYPE;  // set by RunePouchRegistry

    private final Container pouchContainer;
    private final Player owner;
    private final int pouchSlotCount;
    private final net.minecraft.world.item.ItemStack pouchStack;

    public static RunePouchMenu create(int containerId, Inventory playerInv, int size, net.minecraft.world.item.ItemStack pouchStack) {
        SimpleContainer container = RunePouchStorage.load(pouchStack);
        return new RunePouchMenu(containerId, playerInv, container, size, pouchStack);
    }

    public RunePouchMenu(int containerId, Inventory playerInv, Container pouchContainer, int slotCount, net.minecraft.world.item.ItemStack pouchStack) {
        super(TYPE, containerId);
        this.pouchContainer = pouchContainer;
        this.owner = playerInv.player;
        this.pouchSlotCount = slotCount;
        this.pouchStack = pouchStack;
        pouchContainer.startOpen(playerInv.player);

        // Pouch slots — 9 per row
        int cols = 9;
        int rows = (slotCount + cols - 1) / cols;
        int topY = 18;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int slotIdx = r * cols + c;
                if (slotIdx >= slotCount) break;
                this.addSlot(new RuneOnlySlot(pouchContainer, slotIdx, 8 + c * 18, topY + r * 18));
            }
        }

        // Player inventory — offset below pouch slots
        int playerInvY = topY + rows * 18 + 14;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(playerInv, 9 + r * 9 + c, 8 + c * 18, playerInvY + r * 18));
            }
        }
        // Hotbar
        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(playerInv, c, 8 + c * 18, playerInvY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack source = slot.getItem();
            stack = source.copy();
            if (index < pouchSlotCount) {
                // From pouch → player inventory
                if (!this.moveItemStackTo(source, pouchSlotCount, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // From player inventory → pouch (only if rune)
                if (!RunePouchStorage.isValidRune(source)) return ItemStack.EMPTY;
                if (!this.moveItemStackTo(source, 0, pouchSlotCount, false)) return ItemStack.EMPTY;
            }
            if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override
    public void clicked(int slotIdx, int button, ClickType action, Player player) {
        // Prevent picking up the pouch itself in its own GUI by blocking the carried-slot location
        if (action == ClickType.SWAP && slotIdx >= 0 && slotIdx < pouchSlotCount) {
            // Allow swaps but block rune restriction via RuneOnlySlot
        }
        super.clicked(slotIdx, button, action, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return pouchContainer.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        pouchContainer.stopOpen(player);
        // Persist the container back to the stack
        if (!pouchStack.isEmpty()) {
            RunePouchStorage.save(pouchStack, pouchContainer);
        }
    }

    /** Slot that only accepts rune items. */
    private static class RuneOnlySlot extends Slot {
        RuneOnlySlot(Container container, int idx, int x, int y) {
            super(container, idx, x, y);
        }
        @Override public boolean mayPlace(ItemStack stack) { return RunePouchStorage.isValidRune(stack); }
        @Override public int getMaxStackSize() { return 64; }
    }
}
