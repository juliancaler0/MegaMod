package com.ultra.megamod.feature.backpacks.upgrade;

import com.ultra.megamod.feature.backpacks.BackpackTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the upgrade lifecycle for a backpack.
 * Scans upgrade slot contents, instantiates BackpackUpgrade objects,
 * and handles their persistence via CustomData.
 */
public class UpgradeManager {

    private final List<BackpackUpgrade> activeUpgrades = new ArrayList<>();
    private final SimpleContainer upgradeSlotContainer;
    private final BackpackTier tier;

    public UpgradeManager(BackpackTier tier) {
        this.tier = tier;
        this.upgradeSlotContainer = new SimpleContainer(tier.getUpgradeSlots());
    }

    /**
     * Initialize from a backpack ItemStack.
     * Loads upgrade slot items and instantiates BackpackUpgrade objects.
     */
    public void initializeFromStack(ItemStack backpackStack) {
        activeUpgrades.clear();

        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        CompoundTag tag = customData.copyTag();

        // Load upgrade slot items
        if (tag.contains("UpgradeSlots")) {
            ListTag upgradeList = tag.getListOrEmpty("UpgradeSlots");
            for (int i = 0; i < upgradeList.size(); i++) {
                net.minecraft.nbt.Tag entry = upgradeList.get(i);
                if (entry instanceof CompoundTag itemTag) {
                    int slot = itemTag.getIntOr("Slot", -1);
                    if (slot >= 0 && slot < upgradeSlotContainer.getContainerSize()) {
                        String itemId = itemTag.getStringOr("id", "");
                        if (!itemId.isEmpty()) {
                            var id = net.minecraft.resources.Identifier.parse(itemId);
                            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                            if (item != null) {
                                upgradeSlotContainer.setItem(slot, new ItemStack(item, 1));
                            }
                        }
                    }
                }
            }
        }

        // Instantiate upgrade objects from items
        for (int i = 0; i < upgradeSlotContainer.getContainerSize(); i++) {
            ItemStack slotItem = upgradeSlotContainer.getItem(i);
            if (slotItem.getItem() instanceof UpgradeItem upgradeItem) {
                BackpackUpgrade upgrade = upgradeItem.createUpgrade();
                // Load saved data for this upgrade
                if (tag.contains("UpgradeData")) {
                    CompoundTag upgradeData = tag.getCompoundOrEmpty("UpgradeData");
                    if (upgradeData.contains(upgrade.getId())) {
                        upgrade.loadFromTag(upgradeData.getCompoundOrEmpty(upgrade.getId()));
                    }
                }
                activeUpgrades.add(upgrade);
            }
        }
    }

    /**
     * Save all upgrade data back to the backpack ItemStack.
     */
    public void saveToStack(ItemStack backpackStack) {
        CustomData customData = backpackStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        // Save upgrade slot items
        ListTag upgradeList = new ListTag();
        for (int i = 0; i < upgradeSlotContainer.getContainerSize(); i++) {
            ItemStack slotItem = upgradeSlotContainer.getItem(i);
            if (!slotItem.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(slotItem.getItem()).toString());
                upgradeList.add(itemTag);
            }
        }
        tag.put("UpgradeSlots", upgradeList);

        // Save per-upgrade data
        CompoundTag upgradeData = new CompoundTag();
        for (BackpackUpgrade upgrade : activeUpgrades) {
            CompoundTag data = new CompoundTag();
            upgrade.saveToTag(data);
            upgradeData.put(upgrade.getId(), data);
        }
        tag.put("UpgradeData", upgradeData);

        backpackStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Tick all tickable upgrades. */
    public void tickAll(ServerPlayer player, ServerLevel level) {
        for (BackpackUpgrade upgrade : activeUpgrades) {
            if (upgrade.isActive() && upgrade instanceof ITickableUpgrade tickable) {
                if (player.tickCount % tickable.getTickRate() == 0) {
                    tickable.tick(player, level);
                }
            }
        }
    }

    /** Check if any active upgrade needs ticking. */
    public boolean hasTickingUpgrade() {
        for (BackpackUpgrade upgrade : activeUpgrades) {
            if (upgrade.isActive() && upgrade instanceof ITickableUpgrade) return true;
        }
        return false;
    }

    /** Get a specific upgrade by type. */
    @SuppressWarnings("unchecked")
    public <T extends BackpackUpgrade> T getUpgrade(Class<T> type) {
        for (BackpackUpgrade upgrade : activeUpgrades) {
            if (type.isInstance(upgrade)) return (T) upgrade;
        }
        return null;
    }

    /** Check if an upgrade with the given ID is installed. */
    public boolean hasUpgrade(String upgradeId) {
        for (BackpackUpgrade upgrade : activeUpgrades) {
            if (upgrade.getId().equals(upgradeId)) return true;
        }
        return false;
    }

    /** Get all installed upgrade IDs. */
    public List<String> getInstalledUpgradeIds() {
        List<String> ids = new ArrayList<>();
        for (BackpackUpgrade upgrade : activeUpgrades) ids.add(upgrade.getId());
        return ids;
    }

    public List<BackpackUpgrade> getActiveUpgrades() {
        return Collections.unmodifiableList(activeUpgrades);
    }

    public SimpleContainer getUpgradeSlotContainer() {
        return upgradeSlotContainer;
    }

    public BackpackTier getTier() { return tier; }

    // --- Static convenience methods for external code ---

    /** Check if a backpack stack has a specific upgrade installed (static lookup). */
    public static boolean stackHasUpgrade(ItemStack backpackStack, String upgradeId) {
        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("UpgradeSlots")) return false;
        ListTag list = tag.getListOrEmpty("UpgradeSlots");
        for (int i = 0; i < list.size(); i++) {
            net.minecraft.nbt.Tag entry = list.get(i);
            if (entry instanceof CompoundTag itemTag) {
                String itemId = itemTag.getStringOr("id", "");
                if (itemId.contains(upgradeId)) return true;
            }
        }
        return false;
    }
}
