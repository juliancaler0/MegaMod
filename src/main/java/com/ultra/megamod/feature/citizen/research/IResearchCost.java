package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Represents a cost that must be paid to start a research.
 * Implementations include item costs and experience costs.
 */
public interface IResearchCost {

    /**
     * Returns the type key for this cost (e.g., "item", "experience").
     * Used for NBT serialization dispatch.
     */
    String getType();

    /**
     * Checks whether the given player can afford this cost.
     *
     * @param player the player attempting to start the research
     * @return true if the player has the required resources
     */
    boolean canAfford(Player player);

    /**
     * Deducts the cost from the given player. Should only be called
     * after {@link #canAfford(Player)} returns true.
     *
     * @param player the player to charge
     */
    void deduct(Player player);

    /**
     * Returns a display-friendly text representation of this cost.
     */
    Component getDisplayText();

    /**
     * Serializes this cost to NBT.
     */
    CompoundTag toNbt();

    /**
     * Deserializes a research cost from NBT. Dispatches to the correct
     * implementation based on the "CostType" tag.
     *
     * @param tag the saved NBT data
     * @return the deserialized cost, or null if the type is unknown
     */
    static IResearchCost fromNbt(CompoundTag tag) {
        String type = tag.getStringOr("CostType", "");
        return switch (type) {
            case "item" -> ItemResearchCost.fromNbt(tag);
            case "experience" -> ExperienceResearchCost.fromNbt(tag);
            default -> null;
        };
    }
}
