package com.ultra.megamod.feature.citizen.research;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Research cost requiring a specific item and count from the player's inventory.
 */
public class ItemResearchCost implements IResearchCost {

    private final Item item;
    private final int count;

    public ItemResearchCost(Item item, int count) {
        this.item = item;
        this.count = Math.max(1, count);
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String getType() {
        return "item";
    }

    @Override
    public boolean canAfford(Player player) {
        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                found += stack.getCount();
                if (found >= count) return true;
            }
        }
        return false;
    }

    @Override
    public void deduct(Player player) {
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public Component getDisplayText() {
        return Component.literal(count + "x ").append(new ItemStack(item).getHoverName());
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("CostType", "item");
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        tag.putString("ItemId", itemId.toString());
        tag.putInt("Count", count);
        return tag;
    }

    public static ItemResearchCost fromNbt(CompoundTag tag) {
        Identifier itemId = Identifier.tryParse(tag.getStringOr("ItemId", "minecraft:air"));
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null) item = Items.AIR;
        int count = tag.getIntOr("Count", 1);
        return new ItemResearchCost(item, count);
    }
}
