package com.ultra.megamod.feature.citizen.request.types;

import com.ultra.megamod.feature.citizen.request.IRequestable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Request for crafting a specific item.
 * A crafting building that knows the recipe will attempt to produce the output.
 */
public class CraftingRequest implements IRequestable {

    private final ItemStack desiredOutput;
    private final int count;

    public CraftingRequest(ItemStack desiredOutput, int count) {
        this.desiredOutput = desiredOutput.copy();
        this.count = count;
    }

    public ItemStack getDesiredOutput() {
        return desiredOutput;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItem(stack, desiredOutput) && stack.getCount() >= count;
    }

    @Override
    public String getDescription() {
        return "Craft " + count + "x " + desiredOutput.getHoverName().getString();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "crafting");
        Identifier itemId = BuiltInRegistries.ITEM.getKey(desiredOutput.getItem());
        tag.putString("outputId", itemId.toString());
        tag.putInt("count", count);
        return tag;
    }

    public static CraftingRequest fromNbt(CompoundTag tag) {
        String outputIdStr = tag.getStringOr("outputId", "minecraft:air");
        int count = tag.getIntOr("count", 1);
        ItemStack stack = ItemStack.EMPTY;
        try {
            Identifier itemId = Identifier.parse(outputIdStr);
            stack = new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), count);
        } catch (Exception ignored) {
        }
        return new CraftingRequest(stack, count);
    }
}
