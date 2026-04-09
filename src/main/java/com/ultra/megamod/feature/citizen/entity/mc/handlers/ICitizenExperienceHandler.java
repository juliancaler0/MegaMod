package com.ultra.megamod.feature.citizen.entity.mc.handlers;

/**
 * Handler interface for all experience-related citizen entity methods.
 * Ported from MineColonies' ICitizenExperienceHandler.
 */
public interface ICitizenExperienceHandler {

    /**
     * Updates the level of the citizen.
     */
    void updateLevel();

    /**
     * Add experience points to citizen.
     *
     * @param xp the amount of points added.
     */
    void addExperience(double xp);

    /**
     * Drop some experience share depending on the experience and experienceLevel.
     */
    void dropExperience();

    /**
     * Collect exp orbs around the entity.
     */
    void gatherXp();
}
