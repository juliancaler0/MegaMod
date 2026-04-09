package com.ultra.megamod.feature.alchemy.block;

import com.ultra.megamod.feature.alchemy.AlchemyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Alchemy Shelf. 54 slots (double chest size).
 * Only accepts potions and reagents from the megamod alchemy system.
 */
public class AlchemyShelfBlockEntity extends BlockEntity implements Container {
    public static final int SIZE = 54; // double chest
    private final ItemStack[] items = new ItemStack[SIZE];

    public AlchemyShelfBlockEntity(BlockPos pos, BlockState state) {
        super(AlchemyRegistry.ALCHEMY_SHELF_BE.get(), pos, state);
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    /**
     * Check if an item is allowed in this shelf (potions or reagents only).
     */
    public static boolean isAllowedItem(ItemStack stack) {
        if (stack.isEmpty()) return true;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return itemId.startsWith("megamod:potion_") || itemId.startsWith("megamod:reagent_");
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
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isAllowedItem(stack);
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
}
