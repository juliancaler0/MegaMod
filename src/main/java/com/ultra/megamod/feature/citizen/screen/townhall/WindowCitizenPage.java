package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.*;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Town hall citizens page.
 * Shows searchable citizen list with details (job, health, happiness, saturation, skills).
 * Ported from MineColonies WindowCitizenPage.
 *
 * @param <V> Town hall view type.
 */
public class WindowCitizenPage<V> extends AbstractWindowTownHall<V>
{
    /**
     * The filter for the citizen list.
     */
    private String filter = "";

    /**
     * Constructor for the town hall window.
     *
     * @param townHall the town hall view.
     */
    public WindowCitizenPage(final V townHall)
    {
        super(townHall, "layoutcitizens.xml");

        registerButton(NAME_LABEL, this::citizenSelected);
        registerButton(RECALL_ONE, this::recallOneClicked);

        final TextField searchField = window.findPaneOfTypeByID(SEARCH_INPUT, TextField.class);
        if (searchField != null)
        {
            searchField.setHandler(input -> {
                final String newFilter = input.getText();
                if (!newFilter.equals(filter))
                {
                    filter = newFilter;
                    updateCitizens();
                }
            });
        }
    }

    /**
     * Clears and resets all citizens.
     */
    private void updateCitizens()
    {
        // TODO: Populate citizens from buildingView.getColony().getCitizens() when colony API is ported
    }

    /**
     * On clicking a citizen name in the list.
     */
    private void citizenSelected(final Button button)
    {
        // TODO: Select citizen from list and fill citizen info
    }

    /**
     * Executed when the recall one button has been clicked.
     */
    private void recallOneClicked(final Button button)
    {
        // TODO: Network.getNetwork().sendToServer(new RecallSingleCitizenMessage(buildingView, selectedCitizen.getId()));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        fillCitizensList();
    }

    /**
     * Fills the citizens list in the GUI.
     */
    private void fillCitizensList()
    {
        final ScrollingList citizenList = findPaneOfTypeByID(LIST_CITIZENS, ScrollingList.class);
        if (citizenList == null)
        {
            return;
        }

        citizenList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return 0; // TODO: return citizens.size() from colony data
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                // TODO: Populate citizen name and skill tooltip
            }
        });
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_CITIZENS;
    }
}
