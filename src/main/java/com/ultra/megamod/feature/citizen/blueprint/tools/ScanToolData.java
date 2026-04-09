package com.ultra.megamod.feature.citizen.blueprint.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Data holder for scan tool state, stored in item NBT via DataComponents.
 * Manages 10 scan slots, each with two corner positions, an optional anchor, and a name.
 * Designed for lazy access -- only reads/writes the parts of the tag actually needed.
 */
public class ScanToolData {

    /** Number of scan slots (mapped to keys 0-9). */
    public static final int NUM_SLOTS = 10;

    private static final String NBT_SLOTS = "megamod:scan_slots";
    private static final String NBT_CURRENT = "megamod:scan_current";

    private final CompoundTag tag;

    /**
     * Wraps an existing CompoundTag for read/write access.
     * The tag is mutated in place -- caller is responsible for writing it back to the item stack.
     */
    public ScanToolData(@NotNull CompoundTag tag) {
        this.tag = tag;
    }

    /**
     * Returns the backing tag. Do not modify directly unless you know what you are doing.
     */
    @NotNull
    public CompoundTag getInternalTag() {
        return tag;
    }

    // ---- Slot navigation ----

    /**
     * Returns the currently selected slot index (0-9). Defaults to 1 if not set.
     */
    public int getCurrentSlotId() {
        int id = tag.getIntOr(NBT_CURRENT, 1);
        return Math.max(0, Math.min(NUM_SLOTS - 1, id));
    }

    /**
     * Returns the data for the currently selected slot.
     */
    @NotNull
    public Slot getCurrentSlotData() {
        int current = getCurrentSlotId();
        ListTag slots = tag.getListOrEmpty(NBT_SLOTS);
        if (current < slots.size()) {
            Tag entry = slots.get(current);
            if (entry instanceof CompoundTag compound) {
                return new Slot(compound);
            }
        }
        return new Slot(new CompoundTag());
    }

    /**
     * Saves data into the currently selected slot.
     */
    public void setCurrentSlotData(@Nullable Slot data) {
        int current = getCurrentSlotId();
        ListTag slots = tag.getListOrEmpty(NBT_SLOTS);
        // Ensure the list is large enough
        while (current >= slots.size()) {
            slots.add(new CompoundTag());
        }
        slots.set(current, data == null ? new CompoundTag() : data.write(new CompoundTag()));
        tag.put(NBT_SLOTS, slots);
    }

    /**
     * Advances to the next slot, wrapping from 9 back to 0.
     */
    public void nextSlot() {
        moveTo((getCurrentSlotId() + 1) % NUM_SLOTS);
    }

    /**
     * Goes back to the previous slot, wrapping from 0 to 9.
     */
    public void prevSlot() {
        moveTo((getCurrentSlotId() + NUM_SLOTS - 1) % NUM_SLOTS);
    }

    /**
     * Moves directly to the specified slot.
     */
    public void moveTo(int slot) {
        tag.putInt(NBT_CURRENT, Math.max(0, Math.min(NUM_SLOTS - 1, slot)));
    }

    // ---- Convenience setters for the current slot ----

    /**
     * Sets pos1 of the current slot.
     */
    public void setPos1(@NotNull BlockPos pos) {
        Slot slot = getCurrentSlotData();
        Slot updated = new Slot(slot.getName(), pos, slot.getPos2(), slot.getAnchor().orElse(null));
        setCurrentSlotData(updated);
    }

    /**
     * Sets pos2 of the current slot.
     */
    public void setPos2(@NotNull BlockPos pos) {
        Slot slot = getCurrentSlotData();
        Slot updated = new Slot(slot.getName(), slot.getPos1(), pos, slot.getAnchor().orElse(null));
        setCurrentSlotData(updated);
    }

    /**
     * Sets the anchor position of the current slot.
     */
    public void setAnchor(@Nullable BlockPos anchor) {
        Slot slot = getCurrentSlotData();
        Slot updated = new Slot(slot.getName(), slot.getPos1(), slot.getPos2(), anchor);
        setCurrentSlotData(updated);
    }

    /**
     * Sets the name of the current slot.
     */
    public void setName(@NotNull String name) {
        Slot slot = getCurrentSlotData();
        Slot updated = new Slot(name, slot.getPos1(), slot.getPos2(), slot.getAnchor().orElse(null));
        setCurrentSlotData(updated);
    }

    // ========================================================================
    // Slot inner class
    // ========================================================================

    /**
     * Data for a single scan slot: two corner positions, an optional anchor, and a name.
     */
    public static class Slot {
        private final String name;
        private final BlockPos pos1;
        private final BlockPos pos2;
        @Nullable
        private final BlockPos anchor;

        /**
         * Constructs a slot from explicit values.
         */
        public Slot(@NotNull String name, @NotNull BlockPos pos1, @NotNull BlockPos pos2, @Nullable BlockPos anchor) {
            this.name = name;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.anchor = anchor;
        }

        /**
         * Deserializes a slot from a CompoundTag.
         */
        public Slot(@NotNull CompoundTag tag) {
            this.pos1 = readBlockPos(tag, "p1");
            this.pos2 = readBlockPos(tag, "p2");
            this.anchor = tag.contains("ax") ? new BlockPos(
                    tag.getIntOr("ax", 0),
                    tag.getIntOr("ay", 0),
                    tag.getIntOr("az", 0)) : null;
            this.name = tag.getStringOr("name", "");
        }

        /**
         * Serializes this slot into the given CompoundTag.
         * @return the same tag for chaining
         */
        @NotNull
        public CompoundTag write(@NotNull CompoundTag tag) {
            writeBlockPos(tag, "p1", pos1);
            writeBlockPos(tag, "p2", pos2);
            if (anchor != null) {
                tag.putInt("ax", anchor.getX());
                tag.putInt("ay", anchor.getY());
                tag.putInt("az", anchor.getZ());
            } else {
                tag.remove("ax");
                tag.remove("ay");
                tag.remove("az");
            }
            tag.putString("name", name);
            return tag;
        }

        public boolean isEmpty() {
            return name.isEmpty() && pos1.equals(BlockPos.ZERO) && pos2.equals(BlockPos.ZERO);
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public BlockPos getPos1() {
            return pos1;
        }

        @NotNull
        public BlockPos getPos2() {
            return pos2;
        }

        @NotNull
        public Optional<BlockPos> getAnchor() {
            return Optional.ofNullable(anchor);
        }

        // ---- Internal helpers ----

        private static BlockPos readBlockPos(CompoundTag tag, String prefix) {
            return new BlockPos(
                    tag.getIntOr(prefix + "_x", 0),
                    tag.getIntOr(prefix + "_y", 0),
                    tag.getIntOr(prefix + "_z", 0)
            );
        }

        private static void writeBlockPos(CompoundTag tag, String prefix, BlockPos pos) {
            tag.putInt(prefix + "_x", pos.getX());
            tag.putInt(prefix + "_y", pos.getY());
            tag.putInt(prefix + "_z", pos.getZ());
        }
    }
}
