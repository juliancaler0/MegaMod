package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.core.BlockPos;

/**
 * Handler interface for all sleep-related citizen entity methods.
 * Ported from MineColonies' ICitizenSleepHandler.
 */
public interface ICitizenSleepHandler {

    /**
     * Is the citizen asleep?
     *
     * @return true when asleep.
     */
    boolean isAsleep();

    /**
     * Attempts a sleep interaction with the citizen and the given bed.
     *
     * @param bedLocation The possible location to sleep.
     * @return if successful.
     */
    boolean trySleep(BlockPos bedLocation);

    /**
     * Called when the citizen wakes up.
     */
    void onWakeUp();

    /**
     * Get the bed location of the citizen.
     *
     * @return the bed location.
     */
    BlockPos getBedLocation();

    /**
     * Whether we should start to go sleeping.
     *
     * @return true if should sleep.
     */
    boolean shouldGoSleep();
}
