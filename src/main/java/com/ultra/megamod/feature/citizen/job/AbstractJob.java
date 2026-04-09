package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation of {@link IJob}. Holds reference to the citizen entity,
 * manages the AI lifecycle, and handles action counting + NBT serialization.
 * <p>
 * Ported from MineColonies' AbstractJob, adapted for MegaMod's architecture.
 * Subclasses only need to implement {@link #getJobType()} and {@link #createAI()}.
 */
public abstract class AbstractJob implements IJob {

    private static final String TAG_BUILDING_POS = "BuildingPos";
    private static final String TAG_ACTIONS_DONE = "ActionsDone";
    private static final String TAG_JOB_DATA = "JobData";

    /** The citizen entity this job is assigned to. */
    @NotNull
    protected final MCEntityCitizen citizen;

    /** Position of the workplace building. */
    @Nullable
    protected BlockPos buildingPos;

    /** The AI instance for this job. */
    @Nullable
    protected AbstractEntityAIBasic ai;

    /** Counter for work actions done since last dump/reset. */
    private int actionsDone = 0;

    /** Whether the AI has been initialized. */
    private boolean aiInitialized = false;

    /**
     * Create a new job for the given citizen.
     *
     * @param citizen the citizen entity
     */
    public AbstractJob(@NotNull MCEntityCitizen citizen) {
        this.citizen = citizen;
    }

    // ==================== IJob Implementation ====================

    @Override
    @NotNull
    public MCEntityCitizen getCitizen() {
        return citizen;
    }

    @Override
    @Nullable
    public BlockPos getBuildingPos() {
        return buildingPos;
    }

    @Override
    public void setBuildingPos(@Nullable BlockPos pos) {
        this.buildingPos = pos;
    }

    @Override
    @Nullable
    public AbstractEntityAIBasic getAI() {
        return ai;
    }

    @Override
    public void onStart() {
        // Initialize the AI when the job starts
        if (!aiInitialized) {
            this.ai = createAI();
            aiInitialized = true;
        }
    }

    @Override
    public void onStop() {
        if (ai != null) {
            ai.onRemoval();
            ai = null;
        }
        aiInitialized = false;
    }

    @Override
    public void onTick() {
        if (citizen.level().isClientSide()) {
            return;
        }

        // Lazy-init the AI if it hasn't been created yet
        if (!aiInitialized) {
            onStart();
        }

        // Tick the AI state machine
        if (ai != null) {
            ai.tick();
        }
    }

    @Override
    public void onWakeUp() {
        // Reset daily state - subclasses can override
    }

    @Override
    public int getActionsDone() {
        return actionsDone;
    }

    @Override
    public void incrementActionsDone() {
        actionsDone++;
    }

    @Override
    public void incrementActionsDone(int amount) {
        actionsDone += amount;
    }

    @Override
    public void clearActionsDone() {
        actionsDone = 0;
    }

    @Override
    public boolean canAIBeInterrupted() {
        if (ai != null) {
            return ai.getState().isOkayToEat();
        }
        return true;
    }

    @Override
    public boolean allowsAvoidance() {
        return true;
    }

    // ==================== NBT ====================

    @Override
    public void saveToNBT(@NotNull CompoundTag tag) {
        tag.putString("JobType", getJobType().name());
        tag.putInt(TAG_ACTIONS_DONE, actionsDone);

        if (buildingPos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", buildingPos.getX());
            posTag.putInt("Y", buildingPos.getY());
            posTag.putInt("Z", buildingPos.getZ());
            tag.put(TAG_BUILDING_POS, posTag);
        }

        // Allow subclasses to save extra data
        CompoundTag jobData = new CompoundTag();
        saveJobData(jobData);
        if (!jobData.isEmpty()) {
            tag.put(TAG_JOB_DATA, jobData);
        }
    }

    @Override
    public void loadFromNBT(@NotNull CompoundTag tag) {
        actionsDone = tag.getIntOr(TAG_ACTIONS_DONE, 0);

        if (tag.contains(TAG_BUILDING_POS)) {
            CompoundTag posTag = tag.getCompoundOrEmpty(TAG_BUILDING_POS);
            buildingPos = new BlockPos(
                    posTag.getIntOr("X", 0),
                    posTag.getIntOr("Y", 0),
                    posTag.getIntOr("Z", 0)
            );
        }

        // Allow subclasses to load extra data
        if (tag.contains(TAG_JOB_DATA)) {
            loadJobData(tag.getCompoundOrEmpty(TAG_JOB_DATA));
        }
    }

    /**
     * Override in subclasses to save job-specific data.
     *
     * @param tag the compound tag to write to
     */
    protected void saveJobData(@NotNull CompoundTag tag) {
        // Default: nothing extra
    }

    /**
     * Override in subclasses to load job-specific data.
     *
     * @param tag the compound tag to read from
     */
    protected void loadJobData(@NotNull CompoundTag tag) {
        // Default: nothing extra
    }
}
