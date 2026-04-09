package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Town hall alliance page.
 * Shows direct connections, indirect connections, and connection events (ally requests/feuds).
 * Ported from MineColonies WindowAlliancePage.
 *
 * @param <V> Town hall view type.
 */
public class WindowAlliancePage<V> extends AbstractWindowTownHall<V>
{
    /**
     * Buttons to alter ally status on colony list.
     */
    private static final String REQUEST_ALLY = "requestally";
    private static final String START_FEUD = "startfeud";
    private static final String SET_NEUTRAL = "setneutral";
    private static final String ACCEPT_ALLY = "acceptally";

    /**
     * Special list IDs.
     */
    private static final String LIST_DIRECT = "directcolonylist";
    private static final String LIST_INDIRECT = "indirectcolonylist";
    private static final String LIST_EVENTS = "connectioneventlist";

    /**
     * Scrolling lists of connections.
     */
    private final ScrollingList directConnections;
    private final ScrollingList indirectConnections;
    private final ScrollingList connectionEvents;

    /**
     * Constructor for the town hall window.
     *
     * @param building the town hall view.
     */
    public WindowAlliancePage(final V building)
    {
        super(building, "layoutalliance.xml");

        directConnections = findPaneOfTypeByID(LIST_DIRECT, ScrollingList.class);
        indirectConnections = findPaneOfTypeByID(LIST_INDIRECT, ScrollingList.class);
        connectionEvents = findPaneOfTypeByID(LIST_EVENTS, ScrollingList.class);

        registerButton(REQUEST_ALLY, this::requestAlly);
        registerButton(START_FEUD, this::startFeud);
        registerButton(SET_NEUTRAL, this::setNeutral);
        registerButton(ACCEPT_ALLY, this::acceptAlly);

        // TODO: Check if connection data is empty and toggle missingconnections/activeconnections panes

        updateConnections();
        updateEvents();
    }

    private void setNeutral(@NotNull final Button button)
    {
        // TODO: Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(..., ConnectionEventType.NEUTRAL_SET, ...));
    }

    private void startFeud(@NotNull final Button button)
    {
        // TODO: Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(..., ConnectionEventType.FEUD_STARTED, ...));
    }

    private void requestAlly(@NotNull final Button button)
    {
        // TODO: Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(..., ConnectionEventType.ALLY_REQUEST, ...));
    }

    private void acceptAlly(@NotNull final Button button)
    {
        // TODO: Network.getNetwork().sendToServer(new TriggerConnectionEventMessage(..., ConnectionEventType.ALLY_CONFIRMED, ...));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateConnections();
        updateEvents();
    }

    /**
     * Updates the colony connection lists.
     */
    private void updateConnections()
    {
        if (directConnections != null)
        {
            directConnections.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return directConnectionData.size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate connection name, distance, status, action buttons
                }
            });
        }

        if (indirectConnections != null)
        {
            indirectConnections.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return indirectConnectionData.size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate indirect connection data
                }
            });
        }
    }

    /**
     * Updates the connection events list.
     */
    private void updateEvents()
    {
        if (connectionEvents != null)
        {
            connectionEvents.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return connectionEvents.size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate event name, description, accept button
                }
            });
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ALLIANCE;
    }
}
