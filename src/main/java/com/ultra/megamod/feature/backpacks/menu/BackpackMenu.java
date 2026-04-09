package com.ultra.megamod.feature.backpacks.menu;

import com.ultra.megamod.feature.backpacks.BackpackBlockEntity;
import com.ultra.megamod.feature.backpacks.BackpackItem;
import com.ultra.megamod.feature.backpacks.BackpackRegistry;
import com.ultra.megamod.feature.backpacks.BackpackTier;
import com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager;
import com.ultra.megamod.feature.backpacks.upgrade.UpgradeSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class BackpackMenu extends AbstractContainerMenu {

    private final Container backpackContainer;
    private final Container toolContainer;
    private final UpgradeManager upgradeManager;
    private final BackpackTier tier;
    private final int backpackSlotIndex;
    private final int backpackSlotCount;
    private final int toolSlotCount;
    private final int upgradeSlotCount;
    private boolean toolSlotsVisible = true;

    // When opened from a block entity, store the block pos for saving back on close
    private BlockPos blockEntityPos = null;

    /**
     * Server-side constructor.
     */
    public BackpackMenu(int containerId, Inventory playerInv, SimpleContainer backpackContainer,
                        SimpleContainer toolContainer, UpgradeManager upgradeManager,
                        int tierOrdinal, int backpackSlotIndex) {
        super(BackpackRegistry.BACKPACK_MENU.get(), containerId);
        this.backpackContainer = backpackContainer;
        this.toolContainer = toolContainer;
        this.upgradeManager = upgradeManager;
        this.tier = BackpackTier.values()[tierOrdinal];
        this.backpackSlotIndex = backpackSlotIndex;
        this.backpackSlotCount = tier.getStorageSlots();
        this.toolSlotCount = tier.getToolSlots();
        this.upgradeSlotCount = tier.getUpgradeSlots();

        int rows = tier.getStorageRows();

        // Backpack storage slots
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                if (index < backpackSlotCount) {
                    this.addSlot(new BackpackSlot(backpackContainer, index, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        // Tool slots (left side of the GUI)
        for (int i = 0; i < toolSlotCount; i++) {
            this.addSlot(new ToolSlot(toolContainer, i, -14, 18 + i * 18));
        }

        // Upgrade slots (right side of the GUI)
        SimpleContainer upgradeContainer = upgradeManager.getUpgradeSlotContainer();
        for (int i = 0; i < upgradeSlotCount; i++) {
            this.addSlot(new UpgradeSlot(upgradeContainer, i, 176 + 4, 18 + i * 22));
        }

        int playerInvY = 18 + rows * 18 + 14;

        // Player main inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        // Player hotbar
        int hotbarY = playerInvY + 3 * 18 + 4;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    /**
     * Client-side constructor (from network buffer).
     */
    public BackpackMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readInt());
    }

    private BackpackMenu(int containerId, Inventory playerInv, int tierOrdinal) {
        this(containerId, playerInv,
                new SimpleContainer(BackpackTier.values()[tierOrdinal].getStorageSlots()),
                new SimpleContainer(BackpackTier.values()[tierOrdinal].getToolSlots()),
                new UpgradeManager(BackpackTier.values()[tierOrdinal]),
                tierOrdinal, -1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            int toolStart = backpackSlotCount;
            int toolEnd = toolStart + toolSlotCount;
            int upgradeStart = toolEnd;
            int upgradeEnd = upgradeStart + upgradeSlotCount;
            int playerStart = upgradeEnd;
            int playerEnd = playerStart + 36;

            if (index < backpackSlotCount) {
                // From backpack storage → player inventory
                if (!this.moveItemStackTo(slotStack, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < toolEnd) {
                // From tool slot → player inventory
                if (!this.moveItemStackTo(slotStack, playerStart, playerEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player → backpack (or tool slot for tools)
                if (slotStack.getItem() instanceof BackpackItem) {
                    return ItemStack.EMPTY;
                }
                // Try tool slots first for eligible items
                if (ToolSlot.isToolItem(slotStack) && !this.moveItemStackTo(slotStack, toolStart, toolEnd, false)) {
                    // Tool slots full, try backpack storage
                }
                if (!slotStack.isEmpty() && !this.moveItemStackTo(slotStack, 0, backpackSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * Set the block entity position for saving back when menu is closed.
     * Called by BackpackBlock when opening from a placed backpack.
     */
    public void setBlockEntityPos(BlockPos pos) {
        this.blockEntityPos = pos;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) return;

        // If opened from a block entity (slotIndex == -2), save back to block entity
        if (backpackSlotIndex == -2 && blockEntityPos != null) {
            net.minecraft.world.level.block.entity.BlockEntity be = player.level().getBlockEntity(blockEntityPos);
            if (be instanceof BackpackBlockEntity backpackBE) {
                backpackBE.fromContainer(backpackContainer);
                backpackBE.fromToolContainer(toolContainer);
            }
            return;
        }

        // Find the backpack ItemStack and save inventory back to it
        ItemStack backpackStack = ItemStack.EMPTY;

        if (backpackSlotIndex == -1) {
            // Equipped backpack (worn on back) — save back to wearable manager
            backpackStack = com.ultra.megamod.feature.backpacks.BackpackWearableManager.getEquipped(player.getUUID());
        } else if (backpackSlotIndex >= 0 && backpackSlotIndex < player.getInventory().getContainerSize()) {
            backpackStack = player.getInventory().getItem(backpackSlotIndex);
        }

        if (backpackStack.isEmpty() || !(backpackStack.getItem() instanceof BackpackItem)) {
            // Try to find backpack in inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof BackpackItem) {
                    backpackStack = stack;
                    break;
                }
            }
        }

        if (!backpackStack.isEmpty() && backpackStack.getItem() instanceof BackpackItem) {
            saveToItemStack(backpackStack, backpackContainer);
            saveToolsToItemStack(backpackStack, toolContainer);
            upgradeManager.saveToStack(backpackStack);
        }
    }

    /**
     * Save the container contents into the backpack ItemStack's CustomData.
     * Stores item ID, count, and any custom data components the item carries.
     */
    public static void saveToItemStack(ItemStack backpackStack, Container container) {
        CustomData customData = backpackStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        ListTag itemList = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slotItem = container.getItem(i);
            if (!slotItem.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(slotItem.getItem()).toString());
                itemTag.putInt("count", slotItem.getCount());
                // Preserve custom data (enchantments, damage, names, etc.)
                CustomData slotData = slotItem.get(DataComponents.CUSTOM_DATA);
                if (slotData != null) {
                    itemTag.put("tag", slotData.copyTag());
                }
                // Preserve damage
                if (slotItem.isDamaged()) {
                    itemTag.putInt("damage", slotItem.getDamageValue());
                }
                itemList.add(itemTag);
            }
        }
        tag.put("BackpackItems", itemList);
        backpackStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Load container contents from the backpack ItemStack's CustomData.
     */
    public static SimpleContainer loadFromItemStack(ItemStack backpackStack, BackpackTier tier) {
        SimpleContainer container = new SimpleContainer(tier.getStorageSlots());
        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return container;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("BackpackItems")) return container;

        ListTag itemList = tag.getListOrEmpty("BackpackItems");
        for (int i = 0; i < itemList.size(); i++) {
            net.minecraft.nbt.Tag entry = itemList.get(i);
            if (entry instanceof CompoundTag itemTag) {
                int slot = itemTag.getIntOr("Slot", -1);
                if (slot >= 0 && slot < container.getContainerSize()) {
                    String itemId = itemTag.getStringOr("id", "");
                    int count = itemTag.getIntOr("count", 1);
                    if (!itemId.isEmpty()) {
                        net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.parse(itemId);
                        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                        if (item != null && item != net.minecraft.world.item.Items.AIR) {
                            ItemStack stack = new ItemStack(item, count);
                            // Restore custom data
                            CompoundTag slotData = itemTag.getCompoundOrEmpty("tag");
                            if (!slotData.isEmpty()) {
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(slotData));
                            }
                            // Restore damage
                            int damage = itemTag.getIntOr("damage", 0);
                            if (damage > 0) {
                                stack.setDamageValue(damage);
                            }
                            container.setItem(slot, stack);
                        }
                    }
                }
            }
        }
        return container;
    }

    public BackpackTier getTier() {
        return tier;
    }

    public Container getBackpackContainer() {
        return backpackContainer;
    }

    public Container getToolContainer() {
        return toolContainer;
    }

    public int getToolSlotCount() {
        return toolSlotCount;
    }

    public int getUpgradeSlotCount() {
        return upgradeSlotCount;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public boolean isToolSlotsVisible() {
        return toolSlotsVisible;
    }

    public void setToolSlotsVisible(boolean visible) {
        this.toolSlotsVisible = visible;
    }

    /**
     * Save tool slot contents to the backpack ItemStack's CustomData.
     */
    public static void saveToolsToItemStack(ItemStack backpackStack, Container toolContainer) {
        CustomData customData = backpackStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        ListTag toolList = new ListTag();
        for (int i = 0; i < toolContainer.getContainerSize(); i++) {
            ItemStack slotItem = toolContainer.getItem(i);
            if (!slotItem.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(slotItem.getItem()).toString());
                itemTag.putInt("count", slotItem.getCount());
                CustomData slotData = slotItem.get(DataComponents.CUSTOM_DATA);
                if (slotData != null) {
                    itemTag.put("tag", slotData.copyTag());
                }
                if (slotItem.isDamaged()) {
                    itemTag.putInt("damage", slotItem.getDamageValue());
                }
                toolList.add(itemTag);
            }
        }
        tag.put("BackpackTools", toolList);
        backpackStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Load tool slot contents from the backpack ItemStack's CustomData.
     */
    public static SimpleContainer loadToolsFromItemStack(ItemStack backpackStack, BackpackTier tier) {
        SimpleContainer container = new SimpleContainer(tier.getToolSlots());
        CustomData customData = backpackStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return container;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("BackpackTools")) return container;

        ListTag toolList = tag.getListOrEmpty("BackpackTools");
        for (int i = 0; i < toolList.size(); i++) {
            net.minecraft.nbt.Tag entry = toolList.get(i);
            if (entry instanceof CompoundTag itemTag) {
                int slot = itemTag.getIntOr("Slot", -1);
                if (slot >= 0 && slot < container.getContainerSize()) {
                    String itemId = itemTag.getStringOr("id", "");
                    int count = itemTag.getIntOr("count", 1);
                    if (!itemId.isEmpty()) {
                        net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.parse(itemId);
                        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                        if (item != null && item != net.minecraft.world.item.Items.AIR) {
                            ItemStack stack = new ItemStack(item, count);
                            CompoundTag slotData = itemTag.getCompoundOrEmpty("tag");
                            if (!slotData.isEmpty()) {
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(slotData));
                            }
                            int damage = itemTag.getIntOr("damage", 0);
                            if (damage > 0) {
                                stack.setDamageValue(damage);
                            }
                            container.setItem(slot, stack);
                        }
                    }
                }
            }
        }
        return container;
    }
}
