package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;

/**
 * Represents a single effect granted by completing a research.
 * Effects can be multipliers on colony stats, feature unlocks, or flat modifiers.
 */
public interface IResearchEffect {

    /**
     * Returns the unique identifier for this effect (e.g., "worker_speed_1").
     */
    String getId();

    /**
     * Returns a human-readable description of what this effect does.
     */
    String getDescription();

    /**
     * Applies this effect to the given effect manager.
     *
     * @param manager the colony's active effect manager
     */
    void apply(ResearchEffectManager manager);

    /**
     * Removes this effect from the given effect manager.
     *
     * @param manager the colony's active effect manager
     */
    void remove(ResearchEffectManager manager);

    /**
     * Serializes this effect to NBT.
     */
    CompoundTag toNbt();

    /**
     * Deserializes a research effect from NBT. Dispatches to the correct
     * implementation based on the "Type" tag.
     *
     * @param tag the saved NBT data
     * @return the deserialized effect, or null if the type is unknown
     */
    static IResearchEffect fromNbt(CompoundTag tag) {
        String type = tag.getStringOr("Type", "");
        return switch (type) {
            case "MULTIPLIER", "UNLOCK", "MODIFIER" -> ResearchEffect.fromNbt(tag);
            default -> null;
        };
    }
}
