package com.ultra.megamod.feature.citizen.job;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.job.ai.AbstractEntityAIBasic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for all citizen jobs. A job is assigned to a single MCEntityCitizen
 * and manages the AI that drives the citizen's work behavior.
 * <p>
 * Ported from MineColonies' IJob interface, simplified for MegaMod.
 * MineColonies uses a complex registry/factory system; we use CitizenJob enum
 * directly as the job type identifier.
 */
public interface IJob {

    /**
     * Get the job type enum value.
     *
     * @return the CitizenJob type
     */
    @NotNull
    CitizenJob getJobType();

    /**
     * Get the citizen entity this job is assigned to.
     *
     * @return the citizen entity
     */
    @NotNull
    MCEntityCitizen getCitizen();

    /**
     * Get the position of the workplace building.
     *
     * @return the building position, or null if unassigned
     */
    @Nullable
    BlockPos getBuildingPos();

    /**
     * Set the position of the workplace building.
     *
     * @param pos the building position
     */
    void setBuildingPos(@Nullable BlockPos pos);

    /**
     * Create and return the AI instance for this job.
     * Called once when the job is first assigned or loaded.
     *
     * @return the AI instance
     */
    @NotNull
    AbstractEntityAIBasic createAI();

    /**
     * Get the currently active AI.
     *
     * @return the AI instance, or null if not yet created
     */
    @Nullable
    AbstractEntityAIBasic getAI();

    /**
     * Called when the job is first assigned to a citizen.
     */
    void onStart();

    /**
     * Called when the job is removed from a citizen (job change, death, etc.).
     */
    void onStop();

    /**
     * Called every server tick on the citizen entity.
     * Delegates to the AI's tick method.
     */
    void onTick();

    /**
     * Called when the citizen wakes up (new day).
     */
    void onWakeUp();

    /**
     * Get the number of work actions completed since last reset.
     *
     * @return action count
     */
    int getActionsDone();

    /**
     * Increment the actions-done counter by 1.
     */
    void incrementActionsDone();

    /**
     * Increment the actions-done counter by a specific amount.
     *
     * @param amount the amount to add
     */
    void incrementActionsDone(int amount);

    /**
     * Reset the actions-done counter to 0.
     */
    void clearActionsDone();

    /**
     * Whether the AI can be interrupted (for eating, etc.).
     *
     * @return true if interruptible
     */
    boolean canAIBeInterrupted();

    /**
     * Whether the citizen should run avoidance AI (flee from threats).
     *
     * @return true if avoidance is allowed
     */
    boolean allowsAvoidance();

    /**
     * Save job-specific data to NBT.
     *
     * @param tag the compound tag to save to
     */
    void saveToNBT(@NotNull CompoundTag tag);

    /**
     * Load job-specific data from NBT.
     *
     * @param tag the compound tag to load from
     */
    void loadFromNBT(@NotNull CompoundTag tag);
}
