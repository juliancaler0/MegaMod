package com.ultra.megamod.feature.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gravestone block entity that stores a dead player's items.
 * Items persist via GravestoneDataManager (NbtIo singleton pattern).
 * Auto-drops after 60 minutes (72000 ticks).
 */
public class GravestoneBlockEntity extends BlockEntity {
    private UUID ownerUuid;
    private String ownerName = "";
    private long spawnGameTime;
    private final List<ItemStack> storedItems = new ArrayList<>();
    private static final long DESPAWN_TICKS = 72000L; // 60 minutes
    private boolean savedToManager = false;

    public GravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestoneRegistry.GRAVESTONE_BE.get(), pos, state);
    }

    public UUID getOwnerUuid() { return ownerUuid; }
    public String getOwnerName() { return ownerName; }

    public void setOwner(UUID uuid, String name) {
        this.ownerUuid = uuid;
        this.ownerName = name;
        this.spawnGameTime = 0;
        setChanged();
    }

    public void addItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            storedItems.add(stack.copy());
            setChanged();
        }
    }

    public void dropAllItems(Level level, BlockPos pos) {
        for (ItemStack stack : storedItems) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            }
        }
        storedItems.clear();
        // Remove from persistence manager
        if (ownerUuid != null) {
            GravestoneDataManager.removeGravestone(worldPosition);
        }
        setChanged();
    }

    public void tick(Level level) {
        if (level.isClientSide()) return;
        if (spawnGameTime == 0) {
            spawnGameTime = level.getGameTime();
            setChanged();
        }

        // Save to persistence manager on first tick (so data survives restart)
        if (!savedToManager && ownerUuid != null && !storedItems.isEmpty()) {
            GravestoneDataManager.saveGravestone(
                (net.minecraft.server.level.ServerLevel) level,
                worldPosition, ownerUuid, ownerName, spawnGameTime, storedItems);
            savedToManager = true;
        }

        long elapsed = level.getGameTime() - spawnGameTime;
        if (elapsed >= DESPAWN_TICKS) {
            dropAllItems(level, worldPosition);
            level.removeBlock(worldPosition, false);
        }
    }

    // ==================== NBT Persistence ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (ownerUuid != null) {
            output.putString("OwnerUUID", ownerUuid.toString());
        }
        output.putString("OwnerName", ownerName);
        output.putLong("SpawnTime", spawnGameTime);
        output.putBoolean("SavedToManager", savedToManager);
        output.putInt("ItemCount", storedItems.size());
        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = storedItems.get(i);
            if (!stack.isEmpty()) {
                output.putString("Item" + i + "Id",
                    BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                output.putInt("Item" + i + "Count", stack.getCount());
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String uuidStr = input.getStringOr("OwnerUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                ownerUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
        ownerName = input.getStringOr("OwnerName", "");
        spawnGameTime = input.getLongOr("SpawnTime", 0L);
        savedToManager = input.getBooleanOr("SavedToManager", false);
        int count = input.getIntOr("ItemCount", 0);
        storedItems.clear();
        for (int i = 0; i < count; i++) {
            String itemId = input.getStringOr("Item" + i + "Id", "");
            int itemCount = input.getIntOr("Item" + i + "Count", 1);
            if (!itemId.isEmpty()) {
                Identifier id = Identifier.parse(itemId);
                Item item = BuiltInRegistries.ITEM.getValue(id);
                if (item != null && item != Items.AIR) {
                    storedItems.add(new ItemStack(item, itemCount));
                }
            }
        }
    }
}
