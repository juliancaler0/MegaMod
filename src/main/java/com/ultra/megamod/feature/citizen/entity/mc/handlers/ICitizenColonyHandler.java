package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.colony.IColony;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Handler interface for all colony-related citizen entity methods.
 * Ported from MineColonies' ICitizenColonyHandler.
 */
public interface ICitizenColonyHandler {

    /**
     * Server-specific update for the EntityCitizen.
     *
     * @param colonyID  the colony id.
     * @param citizenID the citizen id.
     */
    void registerWithColony(int colonyID, int citizenID);

    /**
     * Update the client side of the citizen entity.
     */
    void updateColonyClient();

    /**
     * Getter for the colony.
     *
     * @return the colony of the citizen or null.
     */
    @Nullable
    IColony getColonyOrRegister();

    /**
     * Getter for the colony id.
     *
     * @return the colony id.
     */
    int getColonyId();

    /**
     * Setter for the colony id.
     *
     * @param colonyId the new colonyId.
     */
    void setColonyId(int colonyId);

    /**
     * Actions when the entity is removed.
     */
    void onCitizenRemoved();

    /**
     * Entity data update callback.
     */
    void onSyncDataUpdate(EntityDataAccessor<?> dataAccessor);

    /**
     * Whether registered to a colony.
     */
    boolean registered();

    /**
     * Unsafe colony getter, doesn't run registration.
     */
    @Nullable
    IColony getColony();
}
