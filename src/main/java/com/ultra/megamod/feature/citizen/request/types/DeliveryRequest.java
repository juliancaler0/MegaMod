package com.ultra.megamod.feature.citizen.request.types;

import com.ultra.megamod.feature.citizen.request.IRequestable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Request for delivering a specific item to a building or citizen.
 */
public class DeliveryRequest implements IRequestable {

    private final ItemStack requestedItem;
    private final int count;

    public DeliveryRequest(ItemStack requestedItem, int count) {
        this.requestedItem = requestedItem.copy();
        this.count = count;
    }

    public ItemStack getRequestedItem() {
        return requestedItem;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItem(stack, requestedItem) && stack.getCount() >= count;
    }

    @Override
    public boolean matchesItem(ItemStack stack) {
        return ItemStack.isSameItem(stack, requestedItem);
    }

    @Override
    public String getDescription() {
        return "Deliver " + count + "x " + requestedItem.getHoverName().getString();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "delivery");
        Identifier itemId = BuiltInRegistries.ITEM.getKey(requestedItem.getItem());
        tag.putString("itemId", itemId.toString());
        tag.putInt("count", count);
        return tag;
    }

    public static DeliveryRequest fromNbt(CompoundTag tag) {
        String itemIdStr = tag.getStringOr("itemId", "minecraft:air");
        int count = tag.getIntOr("count", 1);
        ItemStack stack = ItemStack.EMPTY;
        try {
            Identifier itemId = Identifier.parse(itemIdStr);
            stack = new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), count);
        } catch (Exception ignored) {
        }
        return new DeliveryRequest(stack, count);
    }
}
