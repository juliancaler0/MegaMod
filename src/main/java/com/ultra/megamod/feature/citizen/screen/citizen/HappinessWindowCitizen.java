package com.ultra.megamod.feature.citizen.screen.citizen;

import net.minecraft.resources.Identifier;

/**
 * Citizen happiness detail window.
 * Shows happiness modifiers and their contribution to overall happiness.
 * Ported from MineColonies HappinessWindowCitizen.
 */
public class HappinessWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     */
    public HappinessWindowCitizen(final Object citizen)
    {
        super(citizen, Identifier.fromNamespaceAndPath("megamod", "gui/citizen/happiness.xml"));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        // TODO: CitizenWindowUtils.updateHappiness(citizen, this) when colony API is ported
    }
}
