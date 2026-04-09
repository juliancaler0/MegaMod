package com.ultra.megamod.feature.citizen.blueprint.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores block changes made during structure placement for undo/redo support.
 * Each change records the position, the previous block state, and any previous
 * tile entity data so the world can be restored to its original state.
 *
 * <p>Supports NBT serialization for persistence across server restarts.</p>
 */
public class ChangeStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStorage.class);

    private static final String TAG_CHANGES = "Changes";
    private static final String TAG_POS_X = "X";
    private static final String TAG_POS_Y = "Y";
    private static final String TAG_POS_Z = "Z";
    private static final String TAG_OLD_STATE = "OldState";
    private static final String TAG_NEW_STATE = "NewState";
    private static final String TAG_OLD_TE = "OldTE";

    /** Block update flag: notify neighbors (1) + send to clients (2). */
    private static final int UPDATE_FLAG = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;

    /**
     * Record of a single block change.
     *
     * @param pos            the world position that was modified.
     * @param oldState       the block state before the change.
     * @param oldTileEntity  the tile entity NBT data before the change (null if none).
     */
    public record BlockChange(
        BlockPos pos,
        BlockState oldState,
        @Nullable CompoundTag oldTileEntity
    ) {}

    private final List<BlockChange> changes = new ArrayList<>();
    private boolean dirty = false;

    /**
     * Record a block change. Call this BEFORE modifying the world at the given position.
     *
     * @param pos            the world position being modified.
     * @param oldState       the current block state at the position.
     * @param oldTileEntity  the current tile entity data (null if none).
     */
    public void addChange(BlockPos pos, BlockState oldState, @Nullable CompoundTag oldTileEntity) {
        changes.add(new BlockChange(pos.immutable(), oldState, oldTileEntity != null ? oldTileEntity.copy() : null));
        dirty = true;
    }

    /**
     * Undo all changes in reverse order, restoring the world to its previous state.
     *
     * @param world the world to restore.
     */
    public void undo(Level world) {
        if (world.isClientSide()) {
            LOGGER.warn("ChangeStorage.undo called on client side -- aborting");
            return;
        }

        List<BlockChange> reversed = new ArrayList<>(changes);
        Collections.reverse(reversed);

        for (BlockChange change : reversed) {
            BlockState currentState = world.getBlockState(change.pos());

            // Set to cobblestone first to force a full block update (avoids stale state issues)
            world.setBlock(change.pos(), Blocks.COBBLESTONE.defaultBlockState(), UPDATE_FLAG);
            world.setBlock(change.pos(), change.oldState(), UPDATE_FLAG);

            // Restore tile entity data if present
            if (change.oldTileEntity() != null) {
                CompoundTag adjustedTag = change.oldTileEntity().copy();
                adjustedTag.putInt("x", change.pos().getX());
                adjustedTag.putInt("y", change.pos().getY());
                adjustedTag.putInt("z", change.pos().getZ());

                // Recreate the block entity from scratch using loadStatic
                BlockState restoredState = world.getBlockState(change.pos());
                BlockEntity loaded = BlockEntity.loadStatic(change.pos(), restoredState, adjustedTag, world.registryAccess());
                if (loaded != null) {
                    world.getChunkAt(change.pos()).setBlockEntity(loaded);
                    loaded.setChanged();
                }
            }
        }

        LOGGER.debug("Undid {} block changes", changes.size());
    }

    /**
     * Re-apply all changes in forward order. This is used to redo after an undo.
     * Note: This only re-triggers the block removal of old states -- the actual
     * new states must be re-placed by running the placement step again.
     * For a complete redo, use in conjunction with a StructurePlacer.
     *
     * @param world the world to modify.
     */
    public void redo(Level world) {
        if (world.isClientSide()) {
            LOGGER.warn("ChangeStorage.redo called on client side -- aborting");
            return;
        }

        for (BlockChange change : changes) {
            // Remove the restored old state so the placer can re-place
            BlockState currentState = world.getBlockState(change.pos());
            if (currentState.equals(change.oldState())) {
                world.removeBlock(change.pos(), false);
            }
        }

        LOGGER.debug("Redo preparation cleared {} positions", changes.size());
    }

    /**
     * Get the number of recorded changes.
     *
     * @return the change count.
     */
    public int size() {
        return changes.size();
    }

    /**
     * Whether any changes have been recorded.
     *
     * @return true if there are recorded changes.
     */
    public boolean isEmpty() {
        return changes.isEmpty();
    }

    /**
     * Get the list of all recorded changes (read-only view).
     *
     * @return unmodifiable list of block changes.
     */
    public List<BlockChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    /**
     * Clear all recorded changes.
     */
    public void clear() {
        changes.clear();
        dirty = true;
    }

    /**
     * Whether the storage has been modified since the last save.
     *
     * @return true if dirty.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Mark the storage as clean (typically after saving).
     */
    public void markClean() {
        dirty = false;
    }

    // ==================== NBT Serialization ====================

    /**
     * Serialize this change storage to an NBT compound tag.
     *
     * @param registryAccess the registry access for block state serialization.
     * @return the serialized NBT.
     */
    public CompoundTag save(net.minecraft.core.HolderLookup.Provider registryAccess) {
        CompoundTag root = new CompoundTag();
        ListTag changeList = new ListTag();

        for (BlockChange change : changes) {
            CompoundTag entry = new CompoundTag();
            entry.putInt(TAG_POS_X, change.pos().getX());
            entry.putInt(TAG_POS_Y, change.pos().getY());
            entry.putInt(TAG_POS_Z, change.pos().getZ());

            // Serialize block state using NbtOps
            BlockState.CODEC.encodeStart(NbtOps.INSTANCE, change.oldState())
                .resultOrPartial(err -> LOGGER.warn("Failed to serialize block state: {}", err))
                .ifPresent(tag -> entry.put(TAG_OLD_STATE, tag));

            if (change.oldTileEntity() != null) {
                entry.put(TAG_OLD_TE, change.oldTileEntity().copy());
            }

            changeList.add(entry);
        }

        root.put(TAG_CHANGES, changeList);
        return root;
    }

    /**
     * Deserialize a change storage from an NBT compound tag.
     *
     * @param root           the NBT data to load from.
     * @param registryAccess the registry access for block state deserialization.
     * @return a new ChangeStorage populated with the loaded changes.
     */
    public static ChangeStorage load(CompoundTag root, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ChangeStorage storage = new ChangeStorage();

        if (!root.contains(TAG_CHANGES)) {
            return storage;
        }

        ListTag changeList = root.getListOrEmpty(TAG_CHANGES);

        for (int i = 0; i < changeList.size(); i++) {
            CompoundTag entry = changeList.getCompound(i).orElse(new CompoundTag());

            int x = entry.getIntOr(TAG_POS_X, 0);
            int y = entry.getIntOr(TAG_POS_Y, 0);
            int z = entry.getIntOr(TAG_POS_Z, 0);
            BlockPos pos = new BlockPos(x, y, z);

            // Deserialize block state
            BlockState oldState = Blocks.AIR.defaultBlockState();
            if (entry.contains(TAG_OLD_STATE)) {
                Tag stateTag = entry.get(TAG_OLD_STATE);
                if (stateTag != null) {
                    oldState = BlockState.CODEC.parse(NbtOps.INSTANCE, stateTag)
                        .resultOrPartial(err -> LOGGER.warn("Failed to deserialize block state: {}", err))
                        .orElse(Blocks.AIR.defaultBlockState());
                }
            }

            CompoundTag oldTE = entry.contains(TAG_OLD_TE) ? entry.getCompoundOrEmpty(TAG_OLD_TE) : null;

            storage.changes.add(new BlockChange(pos, oldState, oldTE));
        }

        LOGGER.debug("Loaded {} block changes from NBT", storage.changes.size());
        return storage;
    }
}
