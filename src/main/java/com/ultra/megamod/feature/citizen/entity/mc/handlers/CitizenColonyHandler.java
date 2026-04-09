package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.colony.ColonyManager;
import com.ultra.megamod.feature.citizen.colony.IColony;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen.*;

/**
 * Handles all colony related methods for the MC citizen entity.
 * Ported from MineColonies' CitizenColonyHandler, adapted for MegaMod colony system.
 */
public class CitizenColonyHandler implements ICitizenColonyHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CitizenColonyHandler.class);

    private final MCEntityCitizen citizen;
    private int colonyId = 0;
    @Nullable
    private IColony colony;
    private boolean registered = false;
    private boolean needsClientUpdate = false;

    public CitizenColonyHandler(MCEntityCitizen citizen) {
        this.citizen = citizen;
    }

    @Override
    public void registerWithColony(int colonyID, int citizenID) {
        if (registered) {
            return;
        }

        this.colonyId = colonyID;
        citizen.setCitizenId(citizenID);

        if (colonyId == 0 || citizen.getCitizenId() == 0) {
            return;
        }

        if (citizen.level().isClientSide()) {
            return;
        }

        // Look up colony from MegaMod's ColonyManager
        final IColony foundColony = ColonyManager.get(
                (ServerLevel) citizen.level()).getColonyById(colonyId);

        if (foundColony == null) {
            LOG.warn("MCEntityCitizen '{}' unable to find Colony #{}", citizen.getUUID(), colonyId);
            return;
        }

        this.colony = foundColony;
        registered = true;
    }

    @Override
    public void updateColonyClient() {
        if (needsClientUpdate) {
            if (colonyId == 0) {
                colonyId = citizen.getEntityData().get(DATA_COLONY_ID);
            }

            if (citizen.getCitizenId() == 0) {
                citizen.setCitizenId(citizen.getEntityData().get(DATA_CITIZEN_ID));
            }

            citizen.setFemale(citizen.getEntityData().get(DATA_IS_FEMALE) != 0);
            citizen.setTextureId(citizen.getEntityData().get(DATA_TEXTURE));
            citizen.setRenderMetadata(citizen.getEntityData().get(DATA_RENDER_METADATA));
            citizen.setTextureDirty();

            needsClientUpdate = false;
        }
    }

    @Override
    public void onSyncDataUpdate(EntityDataAccessor<?> data) {
        if (data.equals(DATA_COLONY_ID) || data.equals(DATA_CITIZEN_ID) ||
                data.equals(DATA_IS_FEMALE) || data.equals(DATA_IS_CHILD) ||
                data.equals(DATA_TEXTURE) || data.equals(DATA_TEXTURE_SUFFIX) ||
                data.equals(DATA_STYLE) || data.equals(DATA_RENDER_METADATA) ||
                data.equals(DATA_JOB)) {
            needsClientUpdate = true;
        }
    }

    @Override
    public boolean registered() {
        return registered;
    }

    @Override
    @Nullable
    public IColony getColonyOrRegister() {
        if (colony == null && !citizen.level().isClientSide()) {
            registerWithColony(getColonyId(), citizen.getCitizenId());
        }
        return colony;
    }

    @Override
    @Nullable
    public IColony getColony() {
        return colony;
    }

    @Override
    public int getColonyId() {
        return colonyId;
    }

    @Override
    public void setColonyId(int colonyId) {
        this.colonyId = colonyId;
    }

    @Override
    public void onCitizenRemoved() {
        // Unregister from colony if needed
        registered = false;
    }
}
