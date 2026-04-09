package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * Represents a prerequisite condition that must be met before a research
 * can be started. Implementations check colony state such as building levels
 * or completion of prior research.
 */
public interface IResearchRequirement {

    /**
     * Checks whether this requirement is satisfied given the colony's
     * current research manager state.
     *
     * @param manager the colony's research manager (provides access to
     *                buildings, completed research, etc.)
     * @return true if the requirement is met
     */
    boolean isFulfilled(ResearchManager manager);

    /**
     * Returns a display-friendly description of what this requirement needs.
     */
    Component getDisplayText();

    /**
     * Serializes this requirement to NBT.
     */
    CompoundTag toNbt();

    /**
     * Deserializes a research requirement from NBT. Dispatches to the
     * correct implementation based on the "ReqType" tag.
     *
     * @param tag the saved NBT data
     * @return the deserialized requirement, or null if the type is unknown
     */
    static IResearchRequirement fromNbt(CompoundTag tag) {
        String type = tag.getStringOr("ReqType", "");
        return switch (type) {
            case "building" -> BuildingResearchRequirement.fromNbt(tag);
            case "research" -> ResearchResearchRequirement.fromNbt(tag);
            default -> null;
        };
    }
}
