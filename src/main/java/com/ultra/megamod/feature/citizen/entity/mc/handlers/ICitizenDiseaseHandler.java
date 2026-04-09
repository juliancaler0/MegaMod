package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Handler interface for citizen disease tracking.
 * Ported from MineColonies' ICitizenDiseaseHandler.
 */
public interface ICitizenDiseaseHandler {

    /**
     * To tick the handler.
     */
    void update(int tickRate);

    /**
     * Check if the citizen is sick and must be healed.
     */
    boolean isSick();

    /**
     * Write the handler to NBT.
     */
    void write(CompoundTag compound);

    /**
     * Read the handler from NBT.
     */
    void read(CompoundTag compound);

    /**
     * Get the current disease name, if any.
     */
    @Nullable
    String getDiseaseName();

    /**
     * Cure the citizen.
     */
    void cure();

    /**
     * Set a disease on the citizen.
     *
     * @param diseaseName disease name to set.
     * @return true if they actually became sick.
     */
    boolean setDisease(String diseaseName);
}
