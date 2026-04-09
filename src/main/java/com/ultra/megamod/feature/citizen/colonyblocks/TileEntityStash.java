package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.request.*;
import com.ultra.megamod.feature.citizen.request.types.PickupRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Block entity for the colony stash — 27-slot container.
 * When items are placed inside, automatically creates PickupRequests in the
 * RequestManager so that a Deliveryman will come pick them up and bring them
 * to the Warehouse.
 */
public class TileEntityStash extends BlockEntity implements Container, IRequester {
    public static final int SIZE = 27;
    private final ItemStack[] items = new ItemStack[SIZE];

    /** Unique ID for this stash as a requester. */
    private UUID stashId = UUID.randomUUID();

    /** Tick counter for periodic pickup request checks. */
    private int tickCounter = 0;

    /** Token for any active pickup request. */
    @Nullable
    private IToken activePickupToken = null;

    public TileEntityStash(BlockPos pos, BlockState state) {
        super(ColonyBlockRegistry.STASH_BE.get(), pos, state);
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? items[slot] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SIZE || items[slot].isEmpty()) return ItemStack.EMPTY;
        ItemStack result = items[slot].split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        ItemStack stack = items[slot];
        items[slot] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SIZE) {
            items[slot] = stack;
            setChanged();
            // Trigger pickup request creation when items are placed
            onInventoryChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < SIZE; i++) {
            if (!items[i].isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items[i]);
                items[i] = ItemStack.EMPTY;
            }
        }
    }

    // ==================== Pickup Request Integration ====================

    /**
     * Called when the inventory changes. If the stash has items and no active
     * pickup request, creates one so a Deliveryman will come collect.
     */
    private void onInventoryChanged() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) return;

        if (!isEmpty() && activePickupToken == null) {
            createPickupRequest((ServerLevel) level);
        } else if (isEmpty() && activePickupToken != null) {
            cancelPickupRequest((ServerLevel) level);
        }
    }

    private void createPickupRequest(ServerLevel level) {
        // Find the first non-empty stack to use as the request description
        ItemStack firstItem = ItemStack.EMPTY;
        int totalItems = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                if (firstItem.isEmpty()) firstItem = stack;
                totalItems += stack.getCount();
            }
        }

        if (firstItem.isEmpty()) return;

        RequestManager rm = RequestManager.get(level);
        PickupRequest pickup = new PickupRequest(firstItem, getBlockPos());
        activePickupToken = rm.createRequest(this, pickup);
        setChanged();
    }

    private void cancelPickupRequest(ServerLevel level) {
        if (activePickupToken != null) {
            RequestManager rm = RequestManager.get(level);
            rm.cancelRequest(activePickupToken);
            activePickupToken = null;
            setChanged();
        }
    }

    // ==================== IRequester Implementation ====================

    @Override
    public IToken getRequesterId() {
        return new StandardToken(stashId);
    }

    @Override
    public String getRequesterName() {
        return "Colony Stash";
    }

    @Override
    public BlockPos getRequesterPosition() {
        return getBlockPos();
    }

    // ==================== NBT Persistence ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("StashId", stashId.toString());
        if (activePickupToken != null) {
            output.putString("ActivePickup", activePickupToken.getId().toString());
        }
        ValueOutput.ValueOutputList list = output.childrenList("Items");
        for (int i = 0; i < SIZE; i++) {
            if (!items[i].isEmpty()) {
                ValueOutput slotOutput = list.addChild();
                slotOutput.putInt("Slot", i);
                slotOutput.store("Item", ItemStack.CODEC, items[i]);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String idStr = input.getStringOr("StashId", "");
        if (!idStr.isEmpty()) {
            try {
                stashId = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                stashId = UUID.randomUUID();
            }
        }
        String pickupStr = input.getStringOr("ActivePickup", "");
        if (!pickupStr.isEmpty()) {
            try {
                activePickupToken = new StandardToken(UUID.fromString(pickupStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
        clearContent();
        ValueInput.ValueInputList list = input.childrenListOrEmpty("Items");
        for (ValueInput slotInput : list) {
            int slot = slotInput.getIntOr("Slot", -1);
            if (slot >= 0 && slot < SIZE) {
                ItemStack stack = slotInput.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
                items[slot] = stack;
            }
        }
    }
}
