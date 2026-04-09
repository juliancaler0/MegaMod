package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;

/**
 * Handler interface for citizen mourning tracking.
 * Ported from MineColonies' ICitizenMournHandler.
 */
public interface ICitizenMournHandler {

    void read(CompoundTag compound);

    void write(CompoundTag compound);

    void addDeceasedCitizen(String name);

    Set<String> getDeceasedCitizens();

    void removeDeceasedCitizen(String name);

    void clearDeceasedCitizen();

    boolean shouldMourn();

    boolean isMourning();

    void setMourning(boolean mourn);
}
