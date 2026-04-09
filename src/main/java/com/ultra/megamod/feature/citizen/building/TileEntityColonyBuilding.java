package com.ultra.megamod.feature.citizen.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Shared block entity for all colony hut blocks.
 * Stores the building type ID, colony (faction) UUID, level, and style.
 * One block entity type is registered for all hut blocks via
 * {@link ColonyBuildingRegistry#COLONY_BUILDING_BE}.
 * <p>
 * Follows the same persistence pattern as TownChestBlockEntity
 * using {@link ValueOutput}/{@link ValueInput}.
 */
public class TileEntityColonyBuilding extends BlockEntity {

    private String buildingId = "";
    private UUID colonyId = null;
    private int level = 0;
    private String style = "";
    private String customName = "";

    public TileEntityColonyBuilding(BlockPos pos, BlockState state) {
        super(ColonyBuildingRegistry.COLONY_BUILDING_BE.get(), pos, state);
    }

    // ==================== Getters / Setters ====================

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
        setChanged();
    }

    public UUID getColonyId() {
        return colonyId;
    }

    public void setColonyId(UUID colonyId) {
        this.colonyId = colonyId;
        setChanged();
    }

    public int getBuildingLevel() {
        return level;
    }

    public void setBuildingLevel(int level) {
        int oldLevel = this.level;
        this.level = level;
        setChanged();
        // Notify quest system of building upgrade
        if (level > oldLevel && this.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && colonyId != null) {
            try {
                // colonyId is the owner player UUID -- notify quest system
                com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(serverLevel)
                        .onBuildingUpgrade(serverLevel, colonyId, buildingId, level);
            } catch (Exception ignored) {
                // Quest system may not be initialized yet
            }
        }
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
        setChanged();
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName != null ? customName : "";
        setChanged();
    }

    // ==================== NBT Persistence ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("BuildingId", buildingId);
        if (colonyId != null) {
            output.putString("ColonyId", colonyId.toString());
        }
        output.putInt("Level", level);
        output.putString("Style", style);
        output.putString("CustomName", customName);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        buildingId = input.getStringOr("BuildingId", "");
        String colonyIdStr = input.getStringOr("ColonyId", "");
        if (!colonyIdStr.isEmpty()) {
            try {
                colonyId = UUID.fromString(colonyIdStr);
            } catch (IllegalArgumentException ignored) {
                colonyId = null;
            }
        }
        level = input.getIntOr("Level", 0);
        style = input.getStringOr("Style", "");
        customName = input.getStringOr("CustomName", "");
    }
}
