package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for the colony rack — 27-slot container.
 */
public class TileEntityRack extends BlockEntity implements Container {
    public static final int SIZE = 27;
    private final ItemStack[] items = new ItemStack[SIZE];

    public TileEntityRack(BlockPos pos, BlockState state) {
        super(ColonyBlockRegistry.RACK_BE.get(), pos, state);
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
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
        return slot >= 0 && slot < SIZE ? items[slot] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SIZE || items[slot].isEmpty()) return ItemStack.EMPTY;
        ItemStack result = items[slot].split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        ItemStack stack = items[slot];
        items[slot] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SIZE) {
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
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < SIZE; i++) {
            if (!items[i].isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items[i]);
                items[i] = ItemStack.EMPTY;
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.ValueOutputList list = output.childrenList("Items");
        for (int i = 0; i < SIZE; i++) {
            if (!items[i].isEmpty()) {
                ValueOutput slotOutput = list.addChild();
                slotOutput.putInt("Slot", i);
                slotOutput.store("Item", ItemStack.CODEC, items[i]);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        clearContent();
        ValueInput.ValueInputList list = input.childrenListOrEmpty("Items");
        for (ValueInput slotInput : list) {
            int slot = slotInput.getIntOr("Slot", -1);
            if (slot >= 0 && slot < SIZE) {
                ItemStack stack = slotInput.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
                items[slot] = stack;
            }
        }
    }
}
