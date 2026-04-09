package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.Color;
import com.ultra.megamod.feature.citizen.blockui.controls.ButtonImage;
import com.ultra.megamod.feature.citizen.blockui.controls.Image;
import com.ultra.megamod.feature.citizen.screen.AbstractBuildingMainWindow;
import net.minecraft.resources.Identifier;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Abstract base for all town hall pages.
 * Handles tab button highlighting (hiding active tab, showing inactive ones).
 * Ported from MineColonies AbstractWindowTownHall.
 *
 * @param <V> Town hall view type (TODO: define ITownHallView interface).
 */
public abstract class AbstractWindowTownHall<V> extends AbstractBuildingMainWindow<V>
{
    /**
     * Color constants for builder list.
     */
    public static final int RED = Color.getByName("red", 0);
    public static final int DARKGREEN = Color.getByName("darkgreen", 0);
    public static final int ORANGE = Color.getByName("orange", 0);
    public static final int YELLOW = Color.getByName("yellow", 0);

    /**
     * Constructor for the town hall window.
     *
     * @param townHall the town hall view.
     * @param page     the xml layout page name (e.g. "layoutactions.xml").
     */
    public AbstractWindowTownHall(final V townHall, final String page)
    {
        super(townHall, Identifier.fromNamespaceAndPath("megamod", "gui/townhall/" + page));

        registerButton(BUTTON_ACTIONS, () -> new WindowMainPage<>(townHall).open());
        registerButton(BUTTON_INFOPAGE, () -> new WindowInfoPage<>(townHall).open());
        registerButton(BUTTON_PERMISSIONS, () -> new WindowPermissionsPage<>(townHall).open());
        registerButton(BUTTON_CITIZENS, () -> new WindowCitizenPage<>(townHall).open());
        registerButton(BUTTON_STATS, () -> new WindowStatsPage<>(townHall).open());
        registerButton(BUTTON_SETTINGS, () -> new WindowSettings<>(townHall).open());
        registerButton(BUTTON_ALLIANCE, () -> new WindowAlliancePage<>(townHall).open());

        // Hide the active tab's image and button, show the pressed state
        final Image activeImage = findPaneOfTypeByID(getWindowId() + "0", Image.class);
        if (activeImage != null) activeImage.hide();
        final ButtonImage activeButton = findPaneOfTypeByID(getWindowId(), ButtonImage.class);
        if (activeButton != null) activeButton.hide();

        final ButtonImage pressedButton = findPaneOfTypeByID(getWindowId() + "1", ButtonImage.class);
        if (pressedButton != null) pressedButton.show();
    }

    /**
     * Get the id that identifies the window.
     *
     * @return the string id.
     */
    protected abstract String getWindowId();

    @Override
    protected boolean shouldRenderDefaultSidebar()
    {
        return false;
    }
}
