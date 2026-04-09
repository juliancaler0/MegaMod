package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

/**
 * Block entity for the scarecrow — stores the assigned farmer and field bounds.
 */
public class TileEntityScarecrow extends BlockEntity {

    private UUID assignedFarmer = null;
    private String assignedFarmerName = "";
    private BlockPos fieldMin = BlockPos.ZERO;
    private BlockPos fieldMax = BlockPos.ZERO;
    private int fieldRadius = 8; // default radius around the scarecrow

    public TileEntityScarecrow(BlockPos pos, BlockState state) {
        super(ColonyBlockRegistry.SCARECROW_BE.get(), pos, state);
    }

    // === Getters / Setters ===

    public UUID getAssignedFarmer() {
        return assignedFarmer;
    }

    public void setAssignedFarmer(UUID uuid, String name) {
        this.assignedFarmer = uuid;
        this.assignedFarmerName = name != null ? name : "";
        recalculateField();
        setChanged();
    }

    public String getAssignedFarmerName() {
        return assignedFarmerName;
    }

    public BlockPos getFieldMin() {
        return fieldMin;
    }

    public BlockPos getFieldMax() {
        return fieldMax;
    }

    public int getFieldRadius() {
        return fieldRadius;
    }

    public void setFieldRadius(int radius) {
        this.fieldRadius = Math.max(1, Math.min(16, radius));
        recalculateField();
        setChanged();
    }

    private void recalculateField() {
        if (worldPosition != null) {
            fieldMin = worldPosition.offset(-fieldRadius, 0, -fieldRadius);
            fieldMax = worldPosition.offset(fieldRadius, 0, fieldRadius);
        }
    }

    public boolean isInField(BlockPos pos) {
        return pos.getX() >= fieldMin.getX() && pos.getX() <= fieldMax.getX()
            && pos.getZ() >= fieldMin.getZ() && pos.getZ() <= fieldMax.getZ();
    }

    /**
     * Returns the field area as an AABB for spatial queries.
     * The vertical range spans from 4 blocks below to 4 blocks above the scarecrow.
     */
    public AABB getFieldBounds() {
        return new AABB(
            fieldMin.getX(), worldPosition.getY() - 4, fieldMin.getZ(),
            fieldMax.getX() + 1, worldPosition.getY() + 4, fieldMax.getZ() + 1
        );
    }

    // === NBT ===

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (assignedFarmer != null) {
            output.putString("FarmerUUID", assignedFarmer.toString());
        }
        output.putString("FarmerName", assignedFarmerName);
        output.putInt("FieldRadius", fieldRadius);
        output.putInt("FieldMinX", fieldMin.getX());
        output.putInt("FieldMinY", fieldMin.getY());
        output.putInt("FieldMinZ", fieldMin.getZ());
        output.putInt("FieldMaxX", fieldMax.getX());
        output.putInt("FieldMaxY", fieldMax.getY());
        output.putInt("FieldMaxZ", fieldMax.getZ());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String uuidStr = input.getStringOr("FarmerUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                assignedFarmer = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                assignedFarmer = null;
            }
        }
        assignedFarmerName = input.getStringOr("FarmerName", "");
        fieldRadius = input.getIntOr("FieldRadius", 8);
        int minX = input.getIntOr("FieldMinX", 0);
        int minY = input.getIntOr("FieldMinY", 0);
        int minZ = input.getIntOr("FieldMinZ", 0);
        int maxX = input.getIntOr("FieldMaxX", 0);
        int maxY = input.getIntOr("FieldMaxY", 0);
        int maxZ = input.getIntOr("FieldMaxZ", 0);
        fieldMin = new BlockPos(minX, minY, minZ);
        fieldMax = new BlockPos(maxX, maxY, maxZ);
    }
}
