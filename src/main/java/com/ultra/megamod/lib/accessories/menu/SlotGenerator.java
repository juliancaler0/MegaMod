package com.ultra.megamod.lib.accessories.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.function.Consumer;

/**
 * Stub for OWO's SlotGenerator.
 * Generates player inventory slots at the specified position.
 */
public class SlotGenerator {
    private final Consumer<Slot> addSlot;
    private final int startX;
    private final int startY;

    private SlotGenerator(Consumer<Slot> addSlot, int startX, int startY) {
        this.addSlot = addSlot;
        this.startX = startX;
        this.startY = startY;
    }

    public static SlotGenerator begin(Consumer<Slot> addSlot, int startX, int startY) {
        return new SlotGenerator(addSlot, startX, startY);
    }

    public SlotGenerator playerInventory(Inventory inventory) {
        // Add 27 main inventory slots (rows 1-3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot.accept(new Slot(inventory, col + (row + 1) * 9, startX + col * 18, startY + row * 18));
            }
        }

        // Add 9 hotbar slots
        for (int col = 0; col < 9; col++) {
            addSlot.accept(new Slot(inventory, col, startX + col * 18, startY + 58));
        }

        return this;
    }
}
