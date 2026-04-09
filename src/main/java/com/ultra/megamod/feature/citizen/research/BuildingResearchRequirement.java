package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * Requires the colony's buildings of a given type to have a combined level
 * total at or above a minimum threshold before the research can be started.
 * <p>
 * For example, "residence level 3" means the colony needs residences whose
 * levels sum to at least 3 (e.g., one level 3, or three level 1s).
 * <p>
 * The building is identified by its string ID (e.g., "university", "library",
 * "barracks") matching {@code AbstractBuilding.getBuildingId()}.
 */
public class BuildingResearchRequirement implements IResearchRequirement {

    private final String buildingId;
    private final int minLevel;

    public BuildingResearchRequirement(String buildingId, int minLevel) {
        this.buildingId = buildingId;
        this.minLevel = minLevel;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public boolean isFulfilled(ResearchManager manager) {
        // Delegate to the manager which queries the sum of all building
        // levels of this type across the colony
        return manager.getBuildingLevel(buildingId) >= minLevel;
    }

    @Override
    public Component getDisplayText() {
        String name = capitalize(buildingId);
        return Component.literal(name + " totaling Level " + minLevel);
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ReqType", "building");
        tag.putString("BuildingId", buildingId);
        tag.putInt("MinLevel", minLevel);
        return tag;
    }

    public static BuildingResearchRequirement fromNbt(CompoundTag tag) {
        return new BuildingResearchRequirement(
                tag.getStringOr("BuildingId", ""),
                tag.getIntOr("MinLevel", 1)
        );
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace('_', ' ');
    }
}
