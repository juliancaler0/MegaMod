package com.ultra.megamod.feature.backpacks.network;

import com.ultra.megamod.feature.backpacks.BackpackItem;
import com.ultra.megamod.feature.backpacks.BackpackTier;
import com.ultra.megamod.feature.backpacks.BackpackWearableManager;
import com.ultra.megamod.feature.backpacks.SleepingBagItem;
import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager;
import com.ultra.megamod.feature.backpacks.upgrade.voiding.VoidUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server payload for backpack GUI button actions.
 * Actions: sort, quick_stack, transfer_to_backpack, transfer_to_player, equip, unequip, deploy_sleeping_bag
 */
public record BackpackActionPayload(String action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BackpackActionPayload> TYPE =
        new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "backpack_action"));

    public static final StreamCodec<FriendlyByteBuf, BackpackActionPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, BackpackActionPayload>() {
            public BackpackActionPayload decode(FriendlyByteBuf buf) {
                return new BackpackActionPayload(buf.readUtf());
            }
            public void encode(FriendlyByteBuf buf, BackpackActionPayload payload) {
                buf.writeUtf(payload.action());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnServer(BackpackActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            String action = payload.action();

            // Handle void filter actions (these don't require an open BackpackMenu)
            if (action.equals("void_add_filter")) {
                handleVoidAddFilter(player);
                return;
            }
            if (action.startsWith("void_remove_filter:")) {
                String itemId = action.substring("void_remove_filter:".length());
                handleVoidRemoveFilter(player, itemId);
                return;
            }

            AbstractContainerMenu openMenu = player.containerMenu;
            if (!(openMenu instanceof BackpackMenu bpMenu)) return;

            switch (action) {
                case "sort" -> handleSort(player, bpMenu);
                case "quick_stack" -> handleQuickStack(player, bpMenu);
                case "transfer_to_backpack" -> handleTransferToBackpack(player, bpMenu);
                case "transfer_to_player" -> handleTransferToPlayer(player, bpMenu);
                case "equip" -> handleEquip(player, bpMenu);
                case "unequip" -> handleUnequip(player, bpMenu);
                case "deploy_sleeping_bag" -> handleDeploySleepingBag(player, bpMenu);
                case "toggle_tools" -> handleToggleTools(player, bpMenu);
                case "toggle_magnet" -> handleToggleUpgrade(player, bpMenu, "magnet", "Magnet");
                case "toggle_pickup" -> handleToggleUpgrade(player, bpMenu, "auto_pickup", "Auto-Pickup");
                case "toggle_feeding" -> handleToggleUpgrade(player, bpMenu, "feeding", "Feeding");
                case "sleeping_bag" -> handleSleepingBagAction(player, bpMenu);
            }
        });
    }

    private static void handleSort(ServerPlayer player, BackpackMenu menu) {
        com.ultra.megamod.feature.sorting.SortingManager.sortContainer(
            player, com.ultra.megamod.feature.sorting.SortAlgorithm.CATEGORY);
    }

    private static void handleQuickStack(ServerPlayer player, BackpackMenu menu) {
        // Merge partial stacks between backpack and player inventory
        int bpSlots = menu.getTier().getStorageSlots();
        for (int i = 0; i < bpSlots; i++) {
            ItemStack bpStack = menu.getSlot(i).getItem();
            if (bpStack.isEmpty()) continue;
            for (int j = bpSlots; j < menu.slots.size(); j++) {
                ItemStack playerStack = menu.getSlot(j).getItem();
                if (ItemStack.isSameItemSameComponents(bpStack, playerStack)
                    && playerStack.getCount() < playerStack.getMaxStackSize()) {
                    int space = playerStack.getMaxStackSize() - playerStack.getCount();
                    int toMove = Math.min(space, bpStack.getCount());
                    playerStack.grow(toMove);
                    bpStack.shrink(toMove);
                    if (bpStack.isEmpty()) break;
                }
            }
        }
        menu.broadcastChanges();
    }

    private static void handleTransferToBackpack(ServerPlayer player, BackpackMenu menu) {
        int bpSlots = menu.getTier().getStorageSlots();
        // Move items from player inventory (slots 9-35, not hotbar) into backpack
        for (int j = bpSlots; j < bpSlots + 27; j++) { // player main inv (not hotbar)
            ItemStack playerStack = menu.getSlot(j).getItem();
            if (playerStack.isEmpty() || playerStack.getItem() instanceof BackpackItem) continue;
            // Try to move into backpack
            for (int i = 0; i < bpSlots; i++) {
                ItemStack bpStack = menu.getSlot(i).getItem();
                if (bpStack.isEmpty()) {
                    menu.getSlot(i).set(playerStack.copy());
                    menu.getSlot(j).set(ItemStack.EMPTY);
                    break;
                } else if (ItemStack.isSameItemSameComponents(bpStack, playerStack)
                    && bpStack.getCount() < bpStack.getMaxStackSize()) {
                    int space = bpStack.getMaxStackSize() - bpStack.getCount();
                    int toMove = Math.min(space, playerStack.getCount());
                    bpStack.grow(toMove);
                    playerStack.shrink(toMove);
                    if (playerStack.isEmpty()) break;
                }
            }
        }
        menu.broadcastChanges();
    }

    private static void handleTransferToPlayer(ServerPlayer player, BackpackMenu menu) {
        int bpSlots = menu.getTier().getStorageSlots();
        // Move items from backpack into player inventory
        for (int i = 0; i < bpSlots; i++) {
            ItemStack bpStack = menu.getSlot(i).getItem();
            if (bpStack.isEmpty()) continue;
            // Try to move into player inventory
            for (int j = bpSlots; j < menu.slots.size(); j++) {
                ItemStack playerStack = menu.getSlot(j).getItem();
                if (playerStack.isEmpty()) {
                    menu.getSlot(j).set(bpStack.copy());
                    menu.getSlot(i).set(ItemStack.EMPTY);
                    break;
                } else if (ItemStack.isSameItemSameComponents(bpStack, playerStack)
                    && playerStack.getCount() < playerStack.getMaxStackSize()) {
                    int space = playerStack.getMaxStackSize() - playerStack.getCount();
                    int toMove = Math.min(space, bpStack.getCount());
                    playerStack.grow(toMove);
                    bpStack.shrink(toMove);
                    if (bpStack.isEmpty()) break;
                }
            }
        }
        menu.broadcastChanges();
    }

    private static void handleEquip(ServerPlayer player, BackpackMenu menu) {
        // Save the backpack contents before closing the menu
        // Find the backpack in inventory and equip it
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                // Save container data to the item before equipping
                BackpackMenu.saveToItemStack(stack, menu.getBackpackContainer());
                if (BackpackWearableManager.equip(player, stack, i)) {
                    // Close the menu since the backpack is now worn
                    player.closeContainer();
                    player.displayClientMessage(
                        Component.literal("\u00A7aBackpack equipped! It's now on your back."), true);
                } else {
                    player.displayClientMessage(
                        Component.literal("\u00A7cYou already have a backpack equipped!"), true);
                }
                return;
            }
        }
        player.displayClientMessage(
            Component.literal("\u00A7cNo backpack found in inventory."), true);
    }

    private static void handleUnequip(ServerPlayer player, BackpackMenu menu) {
        // Save container contents to the equipped backpack before unequipping
        ItemStack equippedStack = BackpackWearableManager.getEquipped(player.getUUID());
        if (!equippedStack.isEmpty()) {
            BackpackMenu.saveToItemStack(equippedStack, menu.getBackpackContainer());
            BackpackMenu.saveToolsToItemStack(equippedStack, menu.getToolContainer());
            menu.getUpgradeManager().saveToStack(equippedStack);
        }

        if (BackpackWearableManager.unequip(player)) {
            // Close the container so client state resets cleanly
            player.closeContainer();
            player.displayClientMessage(
                Component.literal("\u00A7eBackpack unequipped and returned to inventory."), true);
        } else {
            player.displayClientMessage(
                Component.literal("\u00A7cNo backpack is currently equipped."), true);
        }
    }

    /**
     * Handle the "deploy_sleeping_bag" action from the backpack GUI.
     * Checks if the player has a sleeping bag in the backpack inventory,
     * then performs the sleep effect (skip to dawn + brief blindness).
     */
    private static void handleDeploySleepingBag(ServerPlayer player, BackpackMenu menu) {
        // Check if the player is wearing a backpack (this action comes from the backpack GUI)
        if (!BackpackWearableManager.isWearing(player.getUUID())) {
            // They might have the backpack open from inventory — check if a backpack is in the menu
            boolean hasBackpack = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).getItem() instanceof BackpackItem) {
                    hasBackpack = true;
                    break;
                }
            }
            if (!hasBackpack) {
                player.displayClientMessage(
                    Component.literal("\u00A7cYou need a backpack to use this feature."), true);
                return;
            }
        }

        // Search for a sleeping bag in the backpack's storage slots
        int bpSlots = menu.getTier().getStorageSlots();
        boolean foundSleepingBag = false;
        for (int i = 0; i < bpSlots; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof SleepingBagItem) {
                foundSleepingBag = true;
                break;
            }
        }

        // Also check player inventory for a sleeping bag
        if (!foundSleepingBag) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).getItem() instanceof SleepingBagItem) {
                    foundSleepingBag = true;
                    break;
                }
            }
        }

        if (!foundSleepingBag) {
            player.displayClientMessage(
                Component.literal("\u00A7cNo sleeping bag found in your backpack or inventory."), true);
            return;
        }

        ServerLevel level = (ServerLevel) player.level();

        // Delegate to SleepingBagItem's static sleep logic
        // Check time/dimension/monsters first
        long timeOfDay = level.getDayTime() % 24000L;
        boolean isNight = timeOfDay >= 12541L && timeOfDay <= 23459L;
        boolean isThundering = level.isThundering();

        if (!isNight && !isThundering) {
            player.displayClientMessage(
                Component.literal("\u00A7cYou can only sleep at night or during a thunderstorm."), true);
            return;
        }

        // Check for nearby monsters
        boolean monstersNearby = !level.getEntitiesOfClass(
            net.minecraft.world.entity.monster.Monster.class,
            player.getBoundingBox().inflate(8.0, 5.0, 8.0),
            monster -> monster.isAlive() && !monster.isNoAi()
        ).isEmpty();

        if (monstersNearby) {
            player.displayClientMessage(
                Component.literal("\u00A7cYou may not rest now, there are monsters nearby."), true);
            return;
        }

        // Close the backpack GUI first, then perform sleep
        player.closeContainer();

        // Perform sleep
        SleepingBagItem.performSleep(player, level);
    }

    /**
     * Toggle tool slot visibility on the backpack menu.
     */
    private static void handleToggleTools(ServerPlayer player, BackpackMenu menu) {
        boolean newState = !menu.isToolSlotsVisible();
        menu.setToolSlotsVisible(newState);
        player.displayClientMessage(
            Component.literal("\u00A7eTool slots: " + (newState ? "\u00A7aVisible" : "\u00A7cHidden")), true);
    }

    /**
     * Generic toggle handler for upgrades (magnet, auto_pickup, feeding).
     * Loads the UpgradeManager from the worn backpack, toggles the upgrade's active state, and saves.
     */
    private static void handleToggleUpgrade(ServerPlayer player, BackpackMenu menu,
                                             String upgradeId, String displayName) {
        // Get the backpack stack — either worn or in inventory
        ItemStack backpackStack = BackpackWearableManager.getEquipped(player.getUUID());
        if (backpackStack.isEmpty()) {
            // Try from inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof BackpackItem) {
                    backpackStack = stack;
                    break;
                }
            }
        }
        if (backpackStack.isEmpty()) {
            player.displayClientMessage(
                Component.literal("\u00A7cNo backpack found!"), true);
            return;
        }

        BackpackTier tier = ((BackpackItem) backpackStack.getItem()).getTier(backpackStack);
        UpgradeManager mgr = new UpgradeManager(tier);
        mgr.initializeFromStack(backpackStack);

        BackpackUpgrade upgrade = null;
        for (BackpackUpgrade u : mgr.getActiveUpgrades()) {
            if (u.getId().equals(upgradeId)) {
                upgrade = u;
                break;
            }
        }

        if (upgrade == null) {
            player.displayClientMessage(
                Component.literal("\u00A7c" + displayName + " upgrade is not installed!"), true);
            return;
        }

        boolean newState = !upgrade.isActive();
        upgrade.setActive(newState);
        mgr.saveToStack(backpackStack);

        player.displayClientMessage(
            Component.literal("\u00A7e" + displayName + ": " + (newState ? "\u00A7aActive" : "\u00A7cInactive")), true);
    }

    /**
     * Handle "sleeping_bag" action — skip to day if night/thunderstorm.
     */
    private static void handleSleepingBagAction(ServerPlayer player, BackpackMenu menu) {
        ServerLevel level = (ServerLevel) player.level();

        long timeOfDay = level.getDayTime() % 24000L;
        boolean isNight = timeOfDay >= 12541L && timeOfDay <= 23459L;
        boolean isThundering = level.isThundering();

        if (!isNight && !isThundering) {
            player.displayClientMessage(
                Component.literal("\u00A7cYou can only use this at night or during a thunderstorm."), true);
            return;
        }

        // Set time to day
        long currentDay = level.getDayTime() / 24000L;
        level.setDayTime((currentDay + 1) * 24000L);

        if (isThundering) {
            level.setWeatherParameters(6000, 0, false, false);
        }

        player.displayClientMessage(
            Component.literal("\u00A7aSlept through the night!"), true);
    }

    // ========================
    // Void Filter handlers
    // ========================

    /**
     * Find the backpack ItemStack the player has (worn or in inventory).
     */
    private static ItemStack findBackpackStack(ServerPlayer player) {
        ItemStack backpackStack = BackpackWearableManager.getEquipped(player.getUUID());
        if (!backpackStack.isEmpty()) return backpackStack;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Handle "void_add_filter" — adds the item in the player's main hand to the void upgrade's filter.
     */
    private static void handleVoidAddFilter(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.displayClientMessage(
                Component.literal("\u00A7cYou must hold an item to add it to the void filter."), true);
            return;
        }

        ItemStack backpackStack = findBackpackStack(player);
        if (backpackStack.isEmpty()) {
            player.displayClientMessage(
                Component.literal("\u00A7cNo backpack found!"), true);
            return;
        }

        BackpackTier tier = ((BackpackItem) backpackStack.getItem()).getTier(backpackStack);
        UpgradeManager mgr = new UpgradeManager(tier);
        mgr.initializeFromStack(backpackStack);

        VoidUpgrade voidUpgrade = mgr.getUpgrade(VoidUpgrade.class);
        if (voidUpgrade == null) {
            player.displayClientMessage(
                Component.literal("\u00A7cVoid upgrade is not installed!"), true);
            return;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();
        if (voidUpgrade.getFilterList().contains(itemId)) {
            player.displayClientMessage(
                Component.literal("\u00A7e" + itemId + " is already in the void filter."), true);
            return;
        }

        voidUpgrade.addToFilter(itemId);
        mgr.saveToStack(backpackStack);

        player.displayClientMessage(
            Component.literal("\u00A7aAdded " + itemId + " to void filter."), true);
    }

    /**
     * Handle "void_remove_filter:ITEM_ID" — removes the given item ID from the void filter.
     */
    private static void handleVoidRemoveFilter(ServerPlayer player, String itemId) {
        if (itemId.isEmpty()) return;

        ItemStack backpackStack = findBackpackStack(player);
        if (backpackStack.isEmpty()) {
            player.displayClientMessage(
                Component.literal("\u00A7cNo backpack found!"), true);
            return;
        }

        BackpackTier tier = ((BackpackItem) backpackStack.getItem()).getTier(backpackStack);
        UpgradeManager mgr = new UpgradeManager(tier);
        mgr.initializeFromStack(backpackStack);

        VoidUpgrade voidUpgrade = mgr.getUpgrade(VoidUpgrade.class);
        if (voidUpgrade == null) {
            player.displayClientMessage(
                Component.literal("\u00A7cVoid upgrade is not installed!"), true);
            return;
        }

        voidUpgrade.removeFromFilter(itemId);
        mgr.saveToStack(backpackStack);

        player.displayClientMessage(
            Component.literal("\u00A7eRemoved " + itemId + " from void filter."), true);
    }
}
