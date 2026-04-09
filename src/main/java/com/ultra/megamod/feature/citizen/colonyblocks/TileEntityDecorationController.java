package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the decoration controller.
 * Stores schematic name and corner positions for blueprint-based decoration placement.
 */
public class TileEntityDecorationController extends BlockEntity {

    private String schematicName = "";
    private BlockPos corner1 = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    public TileEntityDecorationController(BlockPos pos, BlockState state) {
        super(ColonyBlockRegistry.DECORATION_CONTROLLER_BE.get(), pos, state);
    }

    public String getSchematicName() {
        return schematicName;
    }

    public void setSchematicName(String name) {
        this.schematicName = name;
        setChanged();
    }

    public BlockPos getCorner1() {
        return corner1;
    }

    public void setCorner1(BlockPos pos) {
        this.corner1 = pos;
        setChanged();
    }

    public BlockPos getCorner2() {
        return corner2;
    }

    public void setCorner2(BlockPos pos) {
        this.corner2 = pos;
        setChanged();
    }

    /**
     * Sets both corner positions at once.
     */
    public void setCorners(BlockPos c1, BlockPos c2) {
        this.corner1 = c1;
        this.corner2 = c2;
        setChanged();
    }

    /**
     * Returns the decoration level from the block state's LEVEL property.
     */
    public int getDecorationLevel() {
        if (getBlockState() != null && getBlockState().hasProperty(BlockDecorationController.LEVEL)) {
            return getBlockState().getValue(BlockDecorationController.LEVEL);
        }
        return 0;
    }

    /**
     * Returns the schematic bounds as an AABB defined by corner1 and corner2.
     * Returns null if corners are not set (both ZERO).
     */
    public AABB getSchematicBounds() {
        if (corner1.equals(BlockPos.ZERO) && corner2.equals(BlockPos.ZERO)) {
            return null;
        }
        return new AABB(
            Math.min(corner1.getX(), corner2.getX()),
            Math.min(corner1.getY(), corner2.getY()),
            Math.min(corner1.getZ(), corner2.getZ()),
            Math.max(corner1.getX(), corner2.getX()) + 1,
            Math.max(corner1.getY(), corner2.getY()) + 1,
            Math.max(corner1.getZ(), corner2.getZ()) + 1
        );
    }

    /**
     * Returns true if a schematic name has been assigned.
     */
    public boolean hasSchematic() {
        return schematicName != null && !schematicName.isEmpty();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("SchematicName", schematicName);
        output.putInt("Corner1X", corner1.getX());
        output.putInt("Corner1Y", corner1.getY());
        output.putInt("Corner1Z", corner1.getZ());
        output.putInt("Corner2X", corner2.getX());
        output.putInt("Corner2Y", corner2.getY());
        output.putInt("Corner2Z", corner2.getZ());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        schematicName = input.getStringOr("SchematicName", "");
        int c1x = input.getIntOr("Corner1X", 0);
        int c1y = input.getIntOr("Corner1Y", 0);
        int c1z = input.getIntOr("Corner1Z", 0);
        corner1 = new BlockPos(c1x, c1y, c1z);
        int c2x = input.getIntOr("Corner2X", 0);
        int c2y = input.getIntOr("Corner2Y", 0);
        int c2z = input.getIntOr("Corner2Z", 0);
        corner2 = new BlockPos(c2x, c2y, c2z);
    }
}
