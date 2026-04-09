package com.ultra.megamod.feature.citizen.screen.citizen;

import net.minecraft.resources.Identifier;

/**
 * Citizen job detail window.
 * Shows primary/secondary skills and their dependencies (complimentary/adverse).
 * Ported from MineColonies JobWindowCitizen.
 */
public class JobWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     */
    public JobWindowCitizen(final Object citizen)
    {
        super(citizen, Identifier.fromNamespaceAndPath("megamod", "gui/citizen/job.xml"));
        // TODO: CitizenWindowUtils.updateJobPage(citizen, this, colony) when colony API is ported
    }
}
