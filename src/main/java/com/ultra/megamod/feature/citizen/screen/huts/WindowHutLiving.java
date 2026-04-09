package com.ultra.megamod.feature.citizen.screen.huts;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import com.ultra.megamod.feature.citizen.screen.AbstractBuildingMainWindow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.BUTTON_RECALL;

/**
 * Living building (house/tavern) window.
 * Shows assigned citizens with assign/recall buttons.
 * Ported from MineColonies WindowHutLiving.
 *
 * @param <B> Living building view type (TODO: define LivingBuildingView when colony API is ported).
 */
public class WindowHutLiving<B> extends AbstractBuildingMainWindow<B>
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_ASSIGN = "assign";

    /**
     * Label showing the assigned count.
     */
    private static final String ASSIGNED_LABEL = "assignedlabel";

    /**
     * Id to identify the list of citizens in the view.
     */
    private static final String LIST_CITIZEN = "assignedCitizen";

    /**
     * The list of citizens assigned to this hut.
     */
    private ScrollingList citizenList;

    /**
     * Creates the living building window.
     *
     * @param building View of the home building.
     */
    public WindowHutLiving(final B building)
    {
        super(building, Identifier.fromNamespaceAndPath("megamod", "gui/windowhuthome.xml"));

        super.registerButton(BUTTON_ASSIGN, this::assignClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
    }

    /**
     * On recall clicked.
     */
    private void recallClicked()
    {
        // TODO: Network.getNetwork().sendToServer(new RecallCitizenHutMessage(buildingView));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        citizenList = findPaneOfTypeByID(LIST_CITIZEN, ScrollingList.class);
        if (citizenList != null)
        {
            citizenList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return home.getResidents().size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate citizen name and job from colony data
                }
            });
        }

        refreshView();
    }

    /**
     * Refresh the view.
     */
    private void refreshView()
    {
        final Text assignedLabel = findPaneOfTypeByID(ASSIGNED_LABEL, Text.class);
        if (assignedLabel != null)
        {
            assignedLabel.setText(Component.translatable("com.minecolonies.coremod.gui.home.assigned", 0, 0));
        }
        if (citizenList != null)
        {
            citizenList.refreshElementPanes();
        }
    }

    /**
     * Action when an assign button is clicked.
     */
    private void assignClicked()
    {
        // TODO: Check buildingView.getBuildingLevel() > 0
        // new WindowAssignCitizen(buildingView.getColony(), buildingView).open();
    }
}
