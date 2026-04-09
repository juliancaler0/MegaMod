package com.ultra.megamod.feature.citizen.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Interface defining a colony, implemented by both {@link Colony} (server-side)
 * and {@link ColonyView} (client-side read-only).
 * <p>
 * Modeled after MineColonies' IColony interface, but adapted for MegaMod's
 * simpler architecture where colonies use string faction IDs and integrate
 * with the existing citizen / claim / economy systems.
 */
public interface IColony {

    /**
     * Returns the unique integer ID of this colony.
     */
    int getId();

    /**
     * Returns the string faction ID (e.g., "drek", "julian_colony").
     * This is the key used throughout the codebase to identify the colony,
     * replacing the old FactionData.getFactionId().
     */
    @NotNull
    String getFactionId();

    /**
     * Returns the human-readable display name of the colony (e.g., "Drek's Colony").
     */
    @NotNull
    String getDisplayName();

    /**
     * Sets the display name.
     */
    void setDisplayName(@NotNull String name);

    /**
     * Returns the center position of the colony (Town Hall / Town Chest location).
     */
    @Nullable
    BlockPos getCenter();

    /**
     * Returns the Town Chest position, alias for getCenter().
     * Matches the old FactionData.getTownChestPos() API.
     */
    @Nullable
    default BlockPos getTownChestPos() {
        return getCenter();
    }

    /**
     * Sets the Town Chest / center position.
     */
    default void setTownChestPos(@NotNull BlockPos pos) {
        // Default no-op; overridden in Colony
    }

    /**
     * Whether the colony has a Town Chest placed.
     */
    default boolean hasTownChest() {
        BlockPos center = getCenter();
        return center != null && !center.equals(BlockPos.ZERO);
    }

    /**
     * Returns the UUID of the colony owner/leader.
     */
    @Nullable
    UUID getLeaderUuid();

    /**
     * Returns the total number of player members (including leader + officers).
     */
    int getMemberCount();

    /**
     * Returns the dimension this colony exists in.
     */
    @Nullable
    ResourceKey<Level> getDimension();

    /**
     * Returns the permissions system for this colony.
     */
    @NotNull
    ColonyPermissions getPermissions();

    /**
     * Returns the building manager for this colony.
     */
    @NotNull
    ColonyBuildingManager getBuildingManager();

    /**
     * Returns the citizen manager for this colony.
     */
    @NotNull
    ColonyCitizenManager getCitizenManager();

    /**
     * Whether this colony is on the client side (a view).
     */
    boolean isRemote();

    /**
     * Checks if the given player UUID is a member of this colony.
     */
    boolean isMember(@NotNull UUID playerUuid);

    /**
     * Returns the overall happiness of the colony (0.0 - 10.0).
     */
    double getOverallHappiness();

    /**
     * Returns the day count of the colony.
     */
    int getDay();

    /**
     * Whether raids are enabled for this colony.
     */
    boolean isRaidersEnabled();

    /**
     * Whether this colony is marked as abandoned.
     */
    boolean isAbandoned();

    /**
     * Maximum number of NPCs this colony supports.
     */
    int getMaxNPCs();

    /**
     * Mark the colony as needing save.
     */
    void markDirty();
}
