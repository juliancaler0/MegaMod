package com.ultra.megamod.feature.baritone.process;

import com.ultra.megamod.feature.baritone.goals.Goal;
import com.ultra.megamod.feature.baritone.goals.GoalGetToBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Process: interact with chests — deposit items, withdraw specific items, or sort.
 * Navigates to the target chest and manipulates container inventory server-side.
 */
public class ChestProcess implements BotProcess {
    private boolean active = false;
    private String status = "Idle";
    private ServerPlayer player;
    private ServerLevel level;

    // Mode
    private Mode mode = Mode.DEPOSIT;
    private Item withdrawTarget; // Item to withdraw (for WITHDRAW mode)
    private int withdrawCount; // How many to withdraw

    // Navigation state
    private BlockPos targetChest;
    private boolean navigating = true;
    private int scanRadius = 32;
    private int itemsTransferred = 0;
    private int chestsProcessed = 0;

    // Sort mode state
    private List<BlockPos> sortChests;
    private int sortIndex = 0;

    public enum Mode {
        DEPOSIT, WITHDRAW, SORT
    }

    public void startDeposit(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
        this.mode = Mode.DEPOSIT;
        this.withdrawTarget = null;
        this.targetChest = null;
        this.navigating = true;
        this.itemsTransferred = 0;
        this.chestsProcessed = 0;
        this.active = true;
        this.status = "Searching for chest to deposit...";
    }

    public void startWithdraw(ServerPlayer player, ServerLevel level, String itemName, int count) {
        this.player = player;
        this.level = level;
        this.mode = Mode.WITHDRAW;
        this.withdrawCount = count;
        this.targetChest = null;
        this.navigating = true;
        this.itemsTransferred = 0;
        this.chestsProcessed = 0;
        this.active = true;

        // Parse item name
        Identifier loc = Identifier.parse(itemName.contains(":") ? itemName : "minecraft:" + itemName);
        this.withdrawTarget = BuiltInRegistries.ITEM.getValue(loc);
        if (this.withdrawTarget == null || this.withdrawTarget == Items.AIR) {
            this.status = "Unknown item: " + itemName;
            this.active = false;
            return;
        }
        this.status = "Searching for chest containing " + itemName + "...";
    }

    public void startSort(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
        this.mode = Mode.SORT;
        this.targetChest = null;
        this.navigating = true;
        this.itemsTransferred = 0;
        this.chestsProcessed = 0;
        this.sortChests = null;
        this.sortIndex = 0;
        this.active = true;
        this.status = "Sorting nearby chests...";
    }

    @Override
    public String name() { return "Chest"; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public double priority() { return 55; }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean safeToCancel) {
        if (!active || player == null || level == null) return null;

        switch (mode) {
            case DEPOSIT -> { return tickDeposit(calcFailed); }
            case WITHDRAW -> { return tickWithdraw(calcFailed); }
            case SORT -> { return tickSort(calcFailed); }
        }
        return null;
    }

    private PathingCommand tickDeposit(boolean calcFailed) {
        // Find nearest chest if we don't have a target
        if (targetChest == null) {
            targetChest = findNearestChest();
            if (targetChest == null) {
                status = "No chest found nearby";
                active = false;
                return null;
            }
            navigating = true;
        }

        // Check if we're adjacent to the chest
        if (isAdjacentTo(targetChest)) {
            navigating = false;
            // Perform deposit
            Container container = getContainer(targetChest);
            if (container == null) {
                status = "Chest inaccessible";
                active = false;
                return null;
            }

            int deposited = depositItems(container);
            itemsTransferred += deposited;
            chestsProcessed++;
            status = "Deposited " + itemsTransferred + " items into " + chestsProcessed + " chest(s)";
            active = false;
            return null;
        }

        // Navigate to chest
        if (calcFailed) {
            status = "Can't reach chest, searching for another...";
            targetChest = null;
            return null;
        }

        status = "Going to chest at " + targetChest.getX() + "," + targetChest.getY() + "," + targetChest.getZ();
        Goal goal = new GoalGetToBlock(targetChest.getX(), targetChest.getY(), targetChest.getZ());
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    private PathingCommand tickWithdraw(boolean calcFailed) {
        if (withdrawTarget == null) {
            status = "No withdraw target set";
            active = false;
            return null;
        }

        // Find chest containing the target item
        if (targetChest == null) {
            targetChest = findChestContaining(withdrawTarget);
            if (targetChest == null) {
                status = "No chest with " + BuiltInRegistries.ITEM.getKey(withdrawTarget) + " found";
                active = false;
                return null;
            }
            navigating = true;
        }

        // Check if we're adjacent
        if (isAdjacentTo(targetChest)) {
            navigating = false;
            Container container = getContainer(targetChest);
            if (container == null) {
                status = "Chest inaccessible";
                active = false;
                return null;
            }

            int withdrawn = withdrawItems(container, withdrawTarget, withdrawCount);
            itemsTransferred += withdrawn;
            chestsProcessed++;
            status = "Withdrew " + itemsTransferred + " " + BuiltInRegistries.ITEM.getKey(withdrawTarget);
            active = false;
            return null;
        }

        // Navigate to chest
        if (calcFailed) {
            status = "Can't reach chest, searching for another...";
            targetChest = null;
            return null;
        }

        status = "Going to chest with " + BuiltInRegistries.ITEM.getKey(withdrawTarget);
        Goal goal = new GoalGetToBlock(targetChest.getX(), targetChest.getY(), targetChest.getZ());
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    private PathingCommand tickSort(boolean calcFailed) {
        // First scan: find all nearby chests
        if (sortChests == null) {
            sortChests = findAllChests();
            if (sortChests.isEmpty()) {
                status = "No chests found nearby";
                active = false;
                return null;
            }
            sortIndex = 0;
        }

        if (sortIndex >= sortChests.size()) {
            status = "Sort complete! Organized " + chestsProcessed + " chests (" + itemsTransferred + " items moved)";
            active = false;
            return null;
        }

        targetChest = sortChests.get(sortIndex);

        // Check if we're adjacent
        if (isAdjacentTo(targetChest)) {
            Container container = getContainer(targetChest);
            if (container != null) {
                int moved = sortContainer(container);
                itemsTransferred += moved;
                chestsProcessed++;
            }
            sortIndex++;
            targetChest = null;
            return new PathingCommand(null, PathingCommand.CommandType.REQUEST_PAUSE);
        }

        // Navigate to next chest
        if (calcFailed) {
            sortIndex++;
            targetChest = null;
            return null;
        }

        status = "Sorting chest " + (sortIndex + 1) + "/" + sortChests.size();
        Goal goal = new GoalGetToBlock(targetChest.getX(), targetChest.getY(), targetChest.getZ());
        return new PathingCommand(goal, PathingCommand.CommandType.SET_GOAL_AND_PATH);
    }

    // === Chest finding ===

    private BlockPos findNearestChest() {
        BlockPos playerPos = player.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dy = -scanRadius / 2; dy <= scanRadius / 2; dy++) {
                for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (isChest(pos)) {
                        double dist = pos.distSqr(playerPos);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = pos;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private BlockPos findChestContaining(Item item) {
        BlockPos playerPos = player.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dy = -scanRadius / 2; dy <= scanRadius / 2; dy++) {
                for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (isChest(pos)) {
                        Container container = getContainer(pos);
                        if (container != null && containerContains(container, item)) {
                            double dist = pos.distSqr(playerPos);
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                nearest = pos;
                            }
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private List<BlockPos> findAllChests() {
        BlockPos playerPos = player.blockPosition();
        List<BlockPos> chests = new ArrayList<>();

        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dy = -scanRadius / 2; dy <= scanRadius / 2; dy++) {
                for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (isChest(pos)) {
                        chests.add(pos);
                    }
                }
            }
        }

        // Sort by distance
        chests.sort(Comparator.comparingDouble(p -> p.distSqr(playerPos)));
        return chests;
    }

    private boolean isChest(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof BarrelBlock;
    }

    private Container getContainer(BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            return container;
        }
        return null;
    }

    private boolean containerContains(Container container, Item item) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).is(item)) return true;
        }
        return false;
    }

    // === Item transfer ===

    /**
     * Deposit all non-tool, non-weapon, non-armor items from player inventory into container.
     */
    private int depositItems(Container container) {
        int deposited = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            // Skip armor slots (36-39) and offhand (40)
            if (i >= 36) continue;

            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Skip tools, weapons, and armor
            if (isToolOrWeaponOrArmor(stack)) continue;

            // Try to insert into container
            ItemStack remaining = insertIntoContainer(container, stack.copy());
            int transferred = stack.getCount() - remaining.getCount();
            if (transferred > 0) {
                deposited += transferred;
                if (remaining.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                } else {
                    player.getInventory().setItem(i, remaining);
                }
            }
        }
        player.getInventory().setChanged();
        return deposited;
    }

    /**
     * Withdraw a specific item from a container into the player's inventory.
     */
    private int withdrawItems(Container container, Item target, int maxCount) {
        int withdrawn = 0;
        for (int i = 0; i < container.getContainerSize() && withdrawn < maxCount; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !stack.is(target)) continue;

            int toTake = Math.min(stack.getCount(), maxCount - withdrawn);
            ItemStack taken = stack.split(toTake);

            // Try to add to player inventory
            if (player.getInventory().add(taken)) {
                withdrawn += toTake;
            } else {
                // Inventory full, put back what we couldn't take
                stack.grow(taken.getCount());
                break;
            }
        }
        container.setChanged();
        player.getInventory().setChanged();
        return withdrawn;
    }

    /**
     * Sort a container's items (group same items together, move to front).
     */
    private int sortContainer(Container container) {
        int size = container.getContainerSize();
        // Collect all items
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
                container.setItem(i, ItemStack.EMPTY);
            }
        }

        // Merge stacks of same item
        int moved = 0;
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack item : items) {
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, item) && existing.getCount() < existing.getMaxStackSize()) {
                    int canAdd = Math.min(item.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(canAdd);
                    item.shrink(canAdd);
                    if (canAdd > 0) moved++;
                    if (item.isEmpty()) { found = true; break; }
                }
            }
            if (!found && !item.isEmpty()) {
                merged.add(item);
            }
        }

        // Sort by item name for consistency
        merged.sort(Comparator.comparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString()));

        // Place back into container
        for (int i = 0; i < merged.size() && i < size; i++) {
            container.setItem(i, merged.get(i));
        }
        container.setChanged();
        return moved;
    }

    private ItemStack insertIntoContainer(Container container, ItemStack stack) {
        // First pass: merge into existing stacks
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int canAdd = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(canAdd);
                stack.shrink(canAdd);
            }
        }
        // Second pass: place in empty slots
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, stack.copy());
                stack = ItemStack.EMPTY;
            }
        }
        container.setChanged();
        return stack;
    }

    private boolean isToolOrWeaponOrArmor(ItemStack stack) {
        Item item = stack.getItem();
        // Check via registry key path since many item classes were removed in 1.21.11
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        if (path.contains("helmet") || path.contains("chestplate") || path.contains("leggings")
            || path.contains("boots") || path.contains("shield") || path.contains("bow")
            || path.contains("crossbow") || path.contains("trident") || path.contains("fishing_rod")
            || path.contains("elytra")) return true;

        // Also check tool types via registry key
        return path.contains("sword")
            || (path.contains("axe") && !path.contains("pickaxe"))
            || path.contains("pickaxe")
            || path.contains("shovel")
            || path.contains("hoe");
    }

    private boolean isAdjacentTo(BlockPos pos) {
        BlockPos bp = player.blockPosition();
        int dx = Math.abs(bp.getX() - pos.getX());
        int dy = Math.abs(bp.getY() - pos.getY());
        int dz = Math.abs(bp.getZ() - pos.getZ());
        return dx + dz <= 2 && dy <= 1;
    }

    public int getItemsTransferred() { return itemsTransferred; }
    public int getChestsProcessed() { return chestsProcessed; }
    public Mode getMode() { return mode; }

    public void updateState(ServerPlayer player, ServerLevel level) {
        this.player = player;
        this.level = level;
    }

    @Override
    public void onLostControl() {}

    @Override
    public void cancel() {
        active = false;
        targetChest = null;
        sortChests = null;
        status = mode.name().toLowerCase() + " stopped (" + itemsTransferred + " items transferred)";
    }

    @Override
    public String getStatus() { return status; }
}
