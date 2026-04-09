package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Standard implementation of {@link IAssignsCitizen} for residential buildings.
 * Manages a list of assigned citizen residents with capacity based on building level.
 * Provides full NBT persistence.
 */
public class ResidenceBuildingModule implements IAssignsCitizen, IPersistentModule {

    private static final String NBT_MAX_RESIDENTS = "MaxResidents";
    private static final String NBT_RESIDENTS = "Residents";

    /**
     * Base number of residents at building level 1.
     */
    private static final int BASE_RESIDENTS = 2;

    /**
     * Additional residents gained per building level above 1.
     */
    private static final int RESIDENTS_PER_LEVEL = 1;

    private int maxResidents;
    private final List<UUID> assignedResidentIds;

    /**
     * Creates a new residence module with capacity derived from building level.
     *
     * @param buildingLevel the current building level (1-5)
     */
    public ResidenceBuildingModule(int buildingLevel) {
        this.maxResidents = calculateMaxResidents(buildingLevel);
        this.assignedResidentIds = new ArrayList<>();
    }

    @Override
    public String getModuleId() {
        return "residence";
    }

    @Override
    public int getMaxResidents() {
        return maxResidents;
    }

    @Override
    public List<UUID> getResidents() {
        return Collections.unmodifiableList(assignedResidentIds);
    }

    @Override
    public boolean assignResident(UUID citizenId) {
        if (citizenId == null) {
            return false;
        }
        if (assignedResidentIds.size() >= maxResidents) {
            return false;
        }
        if (assignedResidentIds.contains(citizenId)) {
            return false;
        }
        assignedResidentIds.add(citizenId);
        return true;
    }

    @Override
    public void removeResident(UUID citizenId) {
        assignedResidentIds.remove(citizenId);
    }

    /**
     * Updates the residence capacity based on a new building level.
     *
     * @param buildingLevel the new building level
     */
    public void onBuildingLevelChanged(int buildingLevel) {
        this.maxResidents = calculateMaxResidents(buildingLevel);
    }

    @Override
    public void onBuildingLoad(CompoundTag tag) {
        CompoundTag moduleTag = tag.getCompoundOrEmpty(getModuleId());
        maxResidents = moduleTag.getIntOr(NBT_MAX_RESIDENTS, maxResidents);

        assignedResidentIds.clear();
        if (moduleTag.contains(NBT_RESIDENTS)) {
            ListTag residentList = moduleTag.getListOrEmpty(NBT_RESIDENTS);
            for (int i = 0; i < residentList.size(); i++) {
                Tag entry = residentList.get(i);
                if (entry instanceof StringTag stringTag) {
                    try {
                        assignedResidentIds.add(UUID.fromString(stringTag.value()));
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid UUIDs
                    }
                }
            }
        }
    }

    @Override
    public void onBuildingSave(CompoundTag tag) {
        CompoundTag moduleTag = new CompoundTag();
        moduleTag.putInt(NBT_MAX_RESIDENTS, maxResidents);

        ListTag residentList = new ListTag();
        for (UUID id : assignedResidentIds) {
            residentList.add(StringTag.valueOf(id.toString()));
        }
        moduleTag.put(NBT_RESIDENTS, residentList);

        tag.put(getModuleId(), moduleTag);
    }

    @Override
    public void onBuildingTick(Level level) {
        // Residences don't need per-tick logic by default.
    }

    /**
     * Returns true if this residence has open slots for new residents.
     *
     * @return true if residents can still be assigned
     */
    public boolean hasOpenSlots() {
        return assignedResidentIds.size() < maxResidents;
    }

    /**
     * Returns the number of currently assigned residents.
     *
     * @return the resident count
     */
    public int getResidentCount() {
        return assignedResidentIds.size();
    }

    /**
     * Calculates the maximum number of residents for a given building level.
     * Level 1 = 2 residents, each additional level adds 1 more.
     *
     * @param buildingLevel the building level (1-5)
     * @return the maximum resident count
     */
    private static int calculateMaxResidents(int buildingLevel) {
        return BASE_RESIDENTS + Math.max(0, buildingLevel - 1) * RESIDENTS_PER_LEVEL;
    }
}
