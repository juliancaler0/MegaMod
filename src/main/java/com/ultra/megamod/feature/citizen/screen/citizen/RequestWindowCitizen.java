package com.ultra.megamod.feature.citizen.screen.citizen;

import net.minecraft.resources.Identifier;

/**
 * Citizen request window.
 * Shows citizen's open requests with tree display and fulfill/cancel buttons.
 * Ported from MineColonies RequestWindowCitizen.
 */
public class RequestWindowCitizen extends AbstractWindowCitizen
{
    public static final Identifier WINDOW_ID = Identifier.fromNamespaceAndPath("megamod", "gui/citizen/requests.xml");

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     */
    public RequestWindowCitizen(final Object citizen)
    {
        super(citizen, WINDOW_ID);

        // TODO: Register RequestTreeWindowModule layout module when request system is ported
        // this.requestTreeModule = registerLayoutModule(CitizenRequestTreeWindowModule::new, citizen, 33, 29);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        // TODO: Auto-open request detail if autoOpenRequest is set
    }

    // TODO: Port CitizenRequestTreeWindowModule inner class when request system APIs are available
    // It extends RequestTreeWindowModule and implements IRequestTreeSupportsFulfill
}
