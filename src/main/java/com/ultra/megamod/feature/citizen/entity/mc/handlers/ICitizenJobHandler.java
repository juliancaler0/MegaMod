package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.job.IJob;
import org.jetbrains.annotations.Nullable;

/**
 * Handler interface for all job-related citizen entity methods.
 * Ported from MineColonies' ICitizenJobHandler, adapted for MegaMod's CitizenJob enum.
 */
public interface ICitizenJobHandler {

    /**
     * Set Model depending on job.
     *
     * @param job the new job.
     */
    void setModelDependingOnJob(@Nullable CitizenJob job);

    /**
     * Defines job changes and state changes of the citizen.
     *
     * @param job the set job.
     */
    void onJobChanged(@Nullable CitizenJob job);

    /**
     * Gets the current job of the entity.
     *
     * @return the job or null.
     */
    @Nullable
    CitizenJob getColonyJob();

    /**
     * Method to check if the citizen job allows to run the avoidance task.
     *
     * @return true if so.
     */
    boolean shouldRunAvoidance();
}
