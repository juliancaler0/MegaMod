package com.ultra.megamod.feature.schematic.data;

import com.ultra.megamod.feature.citizen.blueprint.Blueprint;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import com.ultra.megamod.feature.schematic.placement.PlacementTransform;
import com.ultra.megamod.feature.schematic.placement.SchematicPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Represents a server-side build order: a schematic placement that a Builder citizen will construct.
 * Contains the pre-computed build queue (blocks sorted in placement order).
 */
public class BuildOrder {

    /**
     * Indicates whether a build order was created from a schematic file or a colony blueprint.
     */
    public enum SourceType {
        SCHEMATIC,
        BLUEPRINT
    }

    private final UUID orderId;
    private final UUID ownerUUID;
    private final String schematicName;
    private final BlockPos origin;
    private final int rotationIndex;
    private final int mirrorIndex;
    private final List<BuildEntry> buildQueue;
    private int progressIndex;
    private int assignedBuilderEntityId;
    private final long createdTime;
    private boolean adminBypass;
    private int speedMultiplier = 1;
    private final SourceType sourceType;

    public record BuildEntry(BlockPos worldPos, BlockState state) {}

    public BuildOrder(UUID orderId, UUID ownerUUID, String schematicName,
                      BlockPos origin, int rotationIndex, int mirrorIndex,
                      List<BuildEntry> buildQueue, long createdTime) {
        this(orderId, ownerUUID, schematicName, origin, rotationIndex, mirrorIndex,
                buildQueue, createdTime, SourceType.SCHEMATIC);
    }

    public BuildOrder(UUID orderId, UUID ownerUUID, String schematicName,
                      BlockPos origin, int rotationIndex, int mirrorIndex,
                      List<BuildEntry> buildQueue, long createdTime, SourceType sourceType) {
        this.orderId = orderId;
        this.ownerUUID = ownerUUID;
        this.schematicName = schematicName;
        this.origin = origin;
        this.rotationIndex = rotationIndex;
        this.mirrorIndex = mirrorIndex;
        this.buildQueue = buildQueue;
        this.progressIndex = 0;
        this.assignedBuilderEntityId = -1;
        this.createdTime = createdTime;
        this.adminBypass = false;
        this.sourceType = sourceType;
    }

    public UUID getOrderId() { return orderId; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getSchematicName() { return schematicName; }
    public BlockPos getOrigin() { return origin; }
    public int getRotationIndex() { return rotationIndex; }
    public int getMirrorIndex() { return mirrorIndex; }
    public List<BuildEntry> getBuildQueue() { return buildQueue; }
    public int getProgressIndex() { return progressIndex; }
    public void setProgressIndex(int index) { this.progressIndex = index; }
    public int getAssignedBuilderEntityId() { return assignedBuilderEntityId; }
    public void setAssignedBuilderEntityId(int id) { this.assignedBuilderEntityId = id; }
    public long getCreatedTime() { return createdTime; }
    public boolean isAdminBypass() { return adminBypass; }
    public void setAdminBypass(boolean bypass) { this.adminBypass = bypass; }
    public int getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(int multiplier) { this.speedMultiplier = Math.max(1, multiplier); }
    public SourceType getSourceType() { return sourceType; }

    public int getTotalBlocks() { return buildQueue.size(); }
    public boolean isComplete() { return progressIndex >= buildQueue.size(); }
    public boolean hasBuilder() { return assignedBuilderEntityId >= 0; }

    public float getProgressPercent() {
        return buildQueue.isEmpty() ? 0 : (float) progressIndex / buildQueue.size();
    }

    /**
     * Gets the next block to be placed, or null if complete.
     */
    public BuildEntry getNextBlock() {
        if (progressIndex >= buildQueue.size()) return null;
        return buildQueue.get(progressIndex);
    }

    /**
     * Returns the remaining blocks to be placed (from current progress onward).
     */
    public List<BuildEntry> getRemainingBlocks() {
        if (progressIndex >= buildQueue.size()) return List.of();
        return buildQueue.subList(progressIndex, buildQueue.size());
    }

    /**
     * Creates a build order from a schematic placement.
     * Build queue is sorted: Y ascending (bottom to top), then X, then Z.
     * Support-dependent blocks (torches, signs, etc.) are deferred after support blocks.
     */
    public static BuildOrder create(UUID ownerUUID, SchematicData schematic,
                                    SchematicPlacement placement, long gameTime) {
        Map<BlockPos, BlockState> worldBlocks = placement.getWorldBlocks();

        // Sort blocks: Y ascending, then X, then Z
        List<BuildEntry> queue = new ArrayList<>(worldBlocks.size());
        for (Map.Entry<BlockPos, BlockState> entry : worldBlocks.entrySet()) {
            queue.add(new BuildEntry(entry.getKey(), entry.getValue()));
        }

        // Primary sort: Y ascending (build bottom-to-top)
        // Secondary: X ascending, Z ascending (consistent order within layers)
        queue.sort((a, b) -> {
            int cmp = Integer.compare(a.worldPos.getY(), b.worldPos.getY());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.worldPos.getX(), b.worldPos.getX());
            if (cmp != 0) return cmp;
            return Integer.compare(a.worldPos.getZ(), b.worldPos.getZ());
        });

        // Defer support-dependent blocks (blocks that need an adjacent solid block to stay)
        List<BuildEntry> deferred = new ArrayList<>();
        List<BuildEntry> sorted = new ArrayList<>(queue.size());
        Set<BlockPos> placed = new HashSet<>();

        for (BuildEntry entry : queue) {
            if (needsSupport(entry.state)) {
                deferred.add(entry);
            } else {
                sorted.add(entry);
                placed.add(entry.worldPos);
            }
        }

        // Add deferred blocks at the end (their support blocks should already be in the queue)
        sorted.addAll(deferred);

        return new BuildOrder(
                UUID.randomUUID(), ownerUUID, schematic.getName(),
                placement.getOrigin(), placement.getRotationIndex(), placement.getMirrorIndex(),
                sorted, gameTime
        );
    }

    /**
     * Creates a build order from a colony Blueprint with a given RotationMirror transform.
     * Iterates over the blueprint's block data, applies the rotation/mirror transform to each
     * local position, offsets to the world origin, and sorts the queue in placement order
     * (Y ascending, then X, then Z, with support-dependent blocks deferred).
     *
     * @param ownerUUID the UUID of the player who created the order
     * @param blueprint the colony Blueprint containing block data
     * @param origin    the world position where the blueprint's anchor is placed
     * @param rm        the rotation/mirror to apply to the blueprint
     * @param gameTime  the server game time when the order was created
     * @return a new BuildOrder with SourceType.BLUEPRINT
     */
    public static BuildOrder createFromBlueprint(UUID ownerUUID, Blueprint blueprint,
                                                  BlockPos origin, RotationMirror rm,
                                                  long gameTime) {
        BlockPos primaryOffset = blueprint.getPrimaryBlockOffset();
        short sizeX = blueprint.getSizeX();
        short sizeY = blueprint.getSizeY();
        short sizeZ = blueprint.getSizeZ();

        List<BuildEntry> queue = new ArrayList<>();

        for (short y = 0; y < sizeY; y++) {
            for (short z = 0; z < sizeZ; z++) {
                for (short x = 0; x < sizeX; x++) {
                    BlockState state = blueprint.getBlockState(x, y, z);
                    if (state.isAir() || state.getBlock() == Blocks.STRUCTURE_VOID) {
                        continue;
                    }

                    // Apply rotation/mirror to the local position relative to the primary offset
                    BlockPos localPos = new BlockPos(x, y, z);
                    BlockPos transformedLocal = rm.applyToPos(
                            localPos.subtract(primaryOffset)
                    );
                    BlockPos worldPos = origin.offset(transformedLocal);

                    // Apply rotation/mirror to the block state's directional properties
                    BlockState transformedState = state;
                    if (rm.isMirrored()) {
                        transformedState = transformedState.mirror(rm.getMirror());
                    }
                    transformedState = transformedState.rotate(rm.getRotation());

                    queue.add(new BuildEntry(worldPos, transformedState));
                }
            }
        }

        // Sort: Y ascending (bottom to top), then X, then Z
        queue.sort((a, b) -> {
            int cmp = Integer.compare(a.worldPos.getY(), b.worldPos.getY());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.worldPos.getX(), b.worldPos.getX());
            if (cmp != 0) return cmp;
            return Integer.compare(a.worldPos.getZ(), b.worldPos.getZ());
        });

        // Defer support-dependent blocks
        List<BuildEntry> deferred = new ArrayList<>();
        List<BuildEntry> sorted = new ArrayList<>(queue.size());
        for (BuildEntry entry : queue) {
            if (needsSupport(entry.state)) {
                deferred.add(entry);
            } else {
                sorted.add(entry);
            }
        }
        sorted.addAll(deferred);

        // Determine rotation/mirror indices for serialization
        int rotIdx = switch (rm.getRotation()) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
        };
        int mirIdx = rm.isMirrored() ? 1 : 0;

        String name = blueprint.getFileName() != null ? blueprint.getFileName()
                : (blueprint.getName() != null ? blueprint.getName() : "blueprint");

        return new BuildOrder(
                UUID.randomUUID(), ownerUUID, name,
                origin, rotIdx, mirIdx,
                sorted, gameTime, SourceType.BLUEPRINT
        );
    }

    /**
     * Returns true if this block needs an adjacent support block to stay placed
     * (e.g., torches, buttons, signs, ladders, vines, etc.)
     */
    private static boolean needsSupport(BlockState state) {
        var block = state.getBlock();
        // Check common support-needing block types
        return block instanceof net.minecraft.world.level.block.TorchBlock
                || block instanceof net.minecraft.world.level.block.ButtonBlock
                || block instanceof net.minecraft.world.level.block.LeverBlock
                || block instanceof net.minecraft.world.level.block.SignBlock
                || block instanceof net.minecraft.world.level.block.LadderBlock
                || block instanceof net.minecraft.world.level.block.VineBlock
                || block instanceof net.minecraft.world.level.block.BannerBlock
                || block instanceof net.minecraft.world.level.block.LanternBlock
                || block instanceof net.minecraft.world.level.block.CarpetBlock
                || block instanceof net.minecraft.world.level.block.SnowLayerBlock
                || block instanceof net.minecraft.world.level.block.FlowerBlock
                || block instanceof net.minecraft.world.level.block.SaplingBlock
                || block instanceof net.minecraft.world.level.block.PressurePlateBlock
                || block instanceof net.minecraft.world.level.block.TripWireBlock;
    }
}
