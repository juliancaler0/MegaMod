package com.ultra.megamod.feature.backpacks.upgrade.voiding;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Void upgrade — automatically destroys items that match a configurable filter list.
 * Event-driven: BackpackEvents checks this upgrade when items enter the backpack.
 */
public class VoidUpgrade extends BackpackUpgrade {

    private final List<String> voidedItemIds = new ArrayList<>();

    @Override
    public String getId() {
        return "void";
    }

    @Override
    public String getDisplayName() {
        return "Void";
    }

    /**
     * Check whether the given item stack matches the void filter.
     */
    public boolean isItemVoided(ItemStack stack) {
        if (!isActive() || stack.isEmpty()) return false;
        String registryName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return voidedItemIds.contains(registryName);
    }

    public void addToFilter(String itemId) {
        if (!voidedItemIds.contains(itemId)) {
            voidedItemIds.add(itemId);
        }
    }

    public void removeFromFilter(String itemId) {
        voidedItemIds.remove(itemId);
    }

    public List<String> getFilterList() {
        return Collections.unmodifiableList(voidedItemIds);
    }

    @Override
    public void saveToTag(CompoundTag tag) {
        super.saveToTag(tag);
        ListTag filterTag = new ListTag();
        for (String id : voidedItemIds) {
            filterTag.add(StringTag.valueOf(id));
        }
        tag.put("VoidFilter", filterTag);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        super.loadFromTag(tag);
        voidedItemIds.clear();
        if (tag.contains("VoidFilter")) {
            ListTag filterTag = tag.getListOrEmpty("VoidFilter");
            for (int i = 0; i < filterTag.size(); i++) {
                String id = filterTag.getString(i).orElse("");
                if (!id.isEmpty()) {
                    voidedItemIds.add(id);
                }
            }
        }
    }
}
