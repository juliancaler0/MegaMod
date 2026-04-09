package com.ultra.megamod.feature.backpacks.upgrade.crafting;

import com.ultra.megamod.feature.backpacks.BackpackTier;
import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Crafting table upgrade for backpacks.
 * Provides a 3x3 crafting grid (9 slots) and 1 result slot.
 * Grid items persist when the backpack is closed.
 */
public class CraftingUpgrade extends BackpackUpgrade {

    private final SimpleContainer craftingGrid = new SimpleContainer(9);

    @Override
    public String getId() {
        return "crafting";
    }

    @Override
    public String getDisplayName() {
        return "Crafting";
    }

    @Override
    public int getSlotCount() {
        return 10; // 9 grid + 1 result
    }

    @Override
    public int getTabWidth() {
        return 83;
    }

    @Override
    public int getTabHeight() {
        return 112;
    }

    @Override
    public SimpleContainer createContainer() {
        return new SimpleContainer(getSlotCount());
    }

    @Override
    public List<Slot> createSlots(SimpleContainer container, int baseX, int baseY) {
        List<Slot> slots = new ArrayList<>();
        // 3x3 crafting grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                slots.add(new Slot(container, index, baseX + col * 18, baseY + row * 18));
            }
        }
        // Result slot (index 9)
        slots.add(new Slot(container, 9, baseX + 76, baseY + 18));
        return slots;
    }

    @Override
    public void onInstalled(ItemStack backpackStack, BackpackTier tier) {
        // Nothing special on install
    }

    @Override
    public List<ItemStack> onRemoved() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < craftingGrid.getContainerSize(); i++) {
            ItemStack stack = craftingGrid.getItem(i);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        craftingGrid.clearContent();
        return drops;
    }

    @Override
    public void saveToTag(CompoundTag tag) {
        super.saveToTag(tag);

        ListTag gridItems = new ListTag();
        for (int i = 0; i < craftingGrid.getContainerSize(); i++) {
            ItemStack stack = craftingGrid.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                itemTag.putInt("count", stack.getCount());
                gridItems.add(itemTag);
            }
        }
        tag.put("CraftingGrid", gridItems);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        super.loadFromTag(tag);

        craftingGrid.clearContent();
        if (tag.contains("CraftingGrid")) {
            ListTag gridItems = tag.getListOrEmpty("CraftingGrid");
            for (int i = 0; i < gridItems.size(); i++) {
                net.minecraft.nbt.Tag entry = gridItems.get(i);
                if (entry instanceof CompoundTag itemTag) {
                    int slot = itemTag.getIntOr("Slot", -1);
                    if (slot >= 0 && slot < 9) {
                        String itemId = itemTag.getStringOr("id", "");
                        int count = itemTag.getIntOr("count", 1);
                        if (!itemId.isEmpty()) {
                            Identifier id = Identifier.parse(itemId);
                            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(id);
                            if (item != null && item != Items.AIR) {
                                craftingGrid.setItem(slot, new ItemStack(item, count));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the internal crafting grid container for direct access.
     */
    public SimpleContainer getCraftingGrid() {
        return craftingGrid;
    }
}
