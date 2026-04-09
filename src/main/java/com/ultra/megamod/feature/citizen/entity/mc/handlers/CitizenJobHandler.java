package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.IJob;
import com.ultra.megamod.feature.citizen.job.JobRegistry;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import static com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen.DATA_JOB;

/**
 * Handles the citizen job methods for the MC citizen entity.
 * Ported from MineColonies' CitizenJobHandler, adapted for MegaMod's CitizenJob enum.
 * <p>
 * Now integrates with the job AI system: when a job is assigned, a corresponding
 * {@link IJob} instance is created from the {@link JobRegistry}, which contains
 * the AI state machine that drives the citizen's work behavior.
 */
public class CitizenJobHandler implements ICitizenJobHandler {

    private final MCEntityCitizen citizen;

    /** The CitizenJob enum value (data-only, used for display/persistence). */
    @Nullable
    private CitizenJob currentJob;

    /** The actual IJob instance with AI (created from JobRegistry). */
    @Nullable
    private IJob jobInstance;

    public CitizenJobHandler(MCEntityCitizen citizen) {
        this.citizen = citizen;
    }

    @Override
    public void setModelDependingOnJob(@Nullable CitizenJob job) {
        // Update the synched data so the client knows what job is assigned
        if (job != null) {
            citizen.getEntityData().set(DATA_JOB, job.name());
        } else {
            citizen.getEntityData().set(DATA_JOB, "");
        }
    }

    @Override
    public void onJobChanged(@Nullable CitizenJob job) {
        // Stop the old job AI if one was running
        if (jobInstance != null) {
            jobInstance.onStop();
            jobInstance = null;
        }

        this.currentJob = job;
        setModelDependingOnJob(job);

        // Create and start the new job AI if applicable
        if (job != null && JobRegistry.hasAI(job)) {
            jobInstance = JobRegistry.createJob(job, citizen);
            if (jobInstance != null) {
                jobInstance.onStart();
            }
        }
    }

    @Override
    @Nullable
    public CitizenJob getColonyJob() {
        return currentJob;
    }

    @Override
    public boolean shouldRunAvoidance() {
        // Non-guard/recruit citizens should flee from danger
        if (jobInstance != null) {
            return jobInstance.allowsAvoidance();
        }
        return currentJob == null || currentJob.isWorker();
    }

    /**
     * Set the job directly (used during deserialization).
     * Does NOT create the AI - call {@link #initializeJobAI()} separately after loading.
     */
    public void setJob(@Nullable CitizenJob job) {
        this.currentJob = job;
    }

    /**
     * Initialize the job AI after deserialization. Should be called after
     * both the job type and building position have been loaded.
     */
    public void initializeJobAI() {
        if (currentJob != null && jobInstance == null && JobRegistry.hasAI(currentJob)) {
            jobInstance = JobRegistry.createJob(currentJob, citizen);
            if (jobInstance != null) {
                jobInstance.onStart();
            }
        }
    }

    /**
     * Get the current IJob instance (with AI).
     *
     * @return the job instance, or null if no AI is assigned
     */
    @Nullable
    public IJob getJobInstance() {
        return jobInstance;
    }

    /**
     * Set the building position for the current job.
     *
     * @param pos the building position
     */
    public void setJobBuildingPos(@Nullable BlockPos pos) {
        if (jobInstance != null) {
            jobInstance.setBuildingPos(pos);
        }
    }

    /**
     * Tick the job AI. Called from MCEntityCitizen.aiStep() on the server side.
     */
    public void tickJobAI() {
        if (jobInstance != null && !citizen.level().isClientSide()) {
            jobInstance.onTick();
        }
    }

    /**
     * Called when the citizen wakes up (new day).
     */
    public void onWakeUp() {
        if (jobInstance != null) {
            jobInstance.onWakeUp();
        }
    }
}
