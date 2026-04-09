package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.DropDownList;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Town hall info page.
 * Shows work orders list with priority controls and colony event log with interval filter.
 * Ported from MineColonies WindowInfoPage.
 *
 * @param <V> Town hall view type.
 */
public class WindowInfoPage<V> extends AbstractWindowTownHall<V>
{
    /**
     * Map of intervals for event log filtering.
     */
    public static final LinkedHashMap<String, Integer> INTERVAL = new LinkedHashMap<>();

    static
    {
        INTERVAL.put("com.minecolonies.coremod.gui.interval.yesterday", 1);
        INTERVAL.put("com.minecolonies.coremod.gui.interval.lastweek", 7);
        INTERVAL.put("com.minecolonies.coremod.gui.interval.100days", 100);
        INTERVAL.put("com.minecolonies.coremod.gui.interval.alltime", -1);
    }

    /**
     * Drop down list for interval.
     */
    private DropDownList intervalDropdown;

    /**
     * Current selected interval.
     */
    public String selectedInterval = "com.minecolonies.coremod.gui.interval.alltime";

    /**
     * Constructor for the town hall window.
     *
     * @param building the town hall view.
     */
    public WindowInfoPage(final V building)
    {
        super(building, "layoutinfo.xml");

        registerButton(BUTTON_UP, this::updatePriority);
        registerButton(BUTTON_DOWN, this::updatePriority);
        registerButton(BUTTON_DELETE, this::deleteWorkOrder);

        intervalDropdown = findPaneOfTypeByID(DROPDOWN_INTERVAL_ID, DropDownList.class);
        if (intervalDropdown != null)
        {
            intervalDropdown.setHandler(this::onDropDownListChanged);
            intervalDropdown.setDataProvider(new DropDownList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return INTERVAL.size();
                }

                @Override
                public MutableComponent getLabel(final int index)
                {
                    return Component.translatable((String) INTERVAL.keySet().toArray()[index]);
                }
            });
            intervalDropdown.setSelectedIndex(new ArrayList<>(INTERVAL.keySet()).indexOf(selectedInterval));
        }
    }

    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        final String temp = (String) INTERVAL.keySet().toArray()[dropDownList.getSelectedIndex()];
        if (!temp.equals(selectedInterval))
        {
            selectedInterval = temp;
            fillEventsList();
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        fillWorkOrderList();
        fillEventsList();
    }

    private void fillEventsList()
    {
        // TODO: Populate events list from buildingView.getColonyEvents() when colony API is ported
        final ScrollingList eventsList = findPaneOfTypeByID(EVENTS_LIST, ScrollingList.class);
        if (eventsList != null)
        {
            eventsList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return filtered events.size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate event data
                }
            });
        }
    }

    /**
     * On Button click update the priority.
     *
     * @param button the clicked button.
     */
    private void updatePriority(@NotNull final Button button)
    {
        // TODO: Read work order ID from hidden pane and send WorkOrderChangeMessage
    }

    /**
     * On Button click remove the workOrder.
     *
     * @param button the clicked button.
     */
    private void deleteWorkOrder(@NotNull final Button button)
    {
        // TODO: Send WorkOrderChangeMessage with delete flag
    }

    /**
     * Fills the workOrder list inside the townhall GUI.
     */
    private void fillWorkOrderList()
    {
        final ScrollingList workOrderList = findPaneOfTypeByID(LIST_WORKORDER, ScrollingList.class);
        if (workOrderList == null)
        {
            return;
        }

        workOrderList.enable();
        workOrderList.show();

        workOrderList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 0; // TODO: return workOrders.size() from colony data
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                // TODO: Populate work order data
            }
        });
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_INFOPAGE;
    }
}
