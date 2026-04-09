package com.ultra.megamod.feature.citizen.screen.townhall;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.BUTTON_SETTINGS;

/**
 * Town hall settings page.
 * Shows colony-wide settings (auto-hiring, housing, move-in, enter/leave messages, construction tape).
 * Ported from MineColonies WindowSettings.
 *
 * @param <V> Town hall view type.
 */
public class WindowSettings<V> extends AbstractWindowTownHall<V>
{
    /**
     * Constructor for the town hall window.
     *
     * @param townHall the town hall view.
     */
    public WindowSettings(final V townHall)
    {
        super(townHall, "layoutsettings.xml");

        // TODO: Wire up settings handlers when colony settings module API is ported
        // moduleView.getSetting(AUTO_HIRING_MODE).setupHandler(...)
        // moduleView.getSetting(MOVE_IN).setupHandler(...)
        // moduleView.getSetting(AUTO_HOUSING_MODE).setupHandler(...)
        // moduleView.getSetting(ENTER_LEAVE_MESSAGES).setupHandler(...)
        // moduleView.getSetting(CONSTRUCTION_TAPE).setupHandler(...)
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        // TODO: Render settings when colony settings module API is ported
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_SETTINGS;
    }
}
