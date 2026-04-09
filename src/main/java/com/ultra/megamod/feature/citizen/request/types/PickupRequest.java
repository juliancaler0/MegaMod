package com.ultra.megamod.feature.citizen.request.types;

import com.ultra.megamod.feature.citizen.request.IRequestable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Request for picking up a specific item from a location.
 */
public class PickupRequest implements IRequestable {

    private final ItemStack itemToPickup;
    private final BlockPos from;

    public PickupRequest(ItemStack itemToPickup, BlockPos from) {
        this.itemToPickup = itemToPickup.copy();
        this.from = from;
    }

    public ItemStack getItemToPickup() {
        return itemToPickup;
    }

    public BlockPos getFrom() {
        return from;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItem(stack, itemToPickup);
    }

    @Override
    public String getDescription() {
        return "Pick up " + itemToPickup.getCount() + "x " + itemToPickup.getHoverName().getString()
            + " from [" + from.getX() + ", " + from.getY() + ", " + from.getZ() + "]";
    }

    @Override
    public int getCount() {
        return itemToPickup.getCount();
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "pickup");
        Identifier itemId = BuiltInRegistries.ITEM.getKey(itemToPickup.getItem());
        tag.putString("itemId", itemId.toString());
        tag.putInt("itemCount", itemToPickup.getCount());
        tag.putInt("fromX", from.getX());
        tag.putInt("fromY", from.getY());
        tag.putInt("fromZ", from.getZ());
        return tag;
    }

    public static PickupRequest fromNbt(CompoundTag tag) {
        String itemIdStr = tag.getStringOr("itemId", "minecraft:air");
        int itemCount = tag.getIntOr("itemCount", 1);
        int fromX = tag.getIntOr("fromX", 0);
        int fromY = tag.getIntOr("fromY", 0);
        int fromZ = tag.getIntOr("fromZ", 0);
        ItemStack stack = ItemStack.EMPTY;
        try {
            Identifier itemId = Identifier.parse(itemIdStr);
            stack = new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), itemCount);
        } catch (Exception ignored) {
        }
        return new PickupRequest(stack, new BlockPos(fromX, fromY, fromZ));
    }
}
