package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.Pane;
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
 * Town hall statistics page.
 * Shows citizen population breakdown by job, child/unemployed counts, and colony statistics.
 * Ported from MineColonies WindowStatsPage.
 *
 * @param <V> Town hall view type.
 */
public class WindowStatsPage<V> extends AbstractWindowTownHall<V>
{
    /**
     * Map of intervals.
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
    public String selectedInterval = "com.minecolonies.coremod.gui.interval.yesterday";

    /**
     * Constructor for the town hall window.
     *
     * @param townHall the town hall view.
     */
    public WindowStatsPage(final V townHall)
    {
        super(townHall, "layoutstats.xml");
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateStats();
        createAndSetStatistics();
    }

    /**
     * Creates several statistics and sets them in the building GUI.
     */
    private void createAndSetStatistics()
    {
        // TODO: Calculate citizen population stats from buildingView.getColony()
        // Total citizens, job breakdown, unemployed, children

        final Text totalCitizenLabel = findPaneOfTypeByID(TOTAL_CITIZENS_LABEL, Text.class);
        if (totalCitizenLabel != null)
        {
            totalCitizenLabel.setText(Component.translatable("com.minecolonies.coremod.gui.townhall.population.totalcitizens.count", 0, 0));
        }

        final ScrollingList list = findPaneOfTypeByID("citizen-stats", ScrollingList.class);
        if (list != null)
        {
            list.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return jobMaxCountMap.size() + 2
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate job worker counts, unemployed, children
                }
            });
        }
    }

    /**
     * Update the display for the stats.
     */
    private void updateStats()
    {
        // TODO: Get stat types from buildingView.getColony().getStatisticsManager()
        final ScrollingList statsList = findPaneOfTypeByID("stats", ScrollingList.class);
        if (statsList != null)
        {
            statsList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return stats.size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate stat values with interval filtering
                }
            });
        }

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
            updateStats();
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_STATS;
    }
}
