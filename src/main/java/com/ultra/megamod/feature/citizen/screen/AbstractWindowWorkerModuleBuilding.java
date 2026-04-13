package com.ultra.megamod.feature.citizen.screen;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Abstract class for window for worker building.
 * Adds hire/recall buttons, delivery priority controls, and worker list display.
 * Ported from MineColonies AbstractWindowWorkerModuleBuilding.
 *
 * @param <B> Class extending IBuildingView (TODO: define colony building view interface).
 */
public abstract class AbstractWindowWorkerModuleBuilding<B> extends AbstractBuildingMainWindow<B>
{
    /**
     * Id of the hire/fire button in the GUI.
     */
    private static final String BUTTON_HIRE = "hire";

    /**
     * Id of the scroll view
     */
    private static final String LIST_WORKERS = "workers";

    /**
     * Id of the recall button in the GUI.
     */
    private static final String BUTTON_RECALL = "recall";

    /**
     * Id of the priority value label in the GUI.
     */
    private static final String LABEL_PRIO_VALUE = "prioValue";

    /**
     * Id of the name label in the GUI.
     */
    private static final String LABEL_WORKERNAME = "workerName";

    /**
     * Button to increase delivery prio.
     */
    private static final String BUTTON_DP_UP = "deliveryPrioUp";

    /**
     * Button to decrease delivery prio.
     */
    private static final String BUTTON_DP_DOWN = "deliveryPrioDown";

    /**
     * Button to force a pickup
     */
    private static final String BUTTON_FORCE_PICKUP = "forcePickup";

    /**
     * Translation keys for priority text.
     */
    private static final String TEXT_PICKUP_PRIORITY = "com.minecolonies.coremod.gui.workerhuts.pickup_priority";
    private static final String TEXT_PICKUP_PRIORITY_NEVER = "com.minecolonies.coremod.gui.workerhuts.pickup_priority.never";

    /**
     * Current pickup priority of the building.
     */
    private int prio = 5; // TODO: Read from buildingView.getBuildingDmPrio() when colony API is ported

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending IBuildingView.
     * @param resource window resource location.
     */
    protected AbstractWindowWorkerModuleBuilding(final B building, final Identifier resource)
    {
        super(building, resource);

        super.registerButton(BUTTON_HIRE, this::hireClicked);
        super.registerButton(BUTTON_RECALL, this::recallClicked);
        super.registerButton(BUTTON_DP_UP, this::deliveryPriorityUp);
        super.registerButton(BUTTON_DP_DOWN, this::deliveryPriorityDown);
        super.registerButton(BUTTON_FORCE_PICKUP, this::forcePickup);
    }

    private void updatePriorityLabel()
    {
        final Text prioLabel = findPaneOfTypeByID(LABEL_PRIO_VALUE, Text.class);
        if (prioLabel == null)
        {
            return;
        }

        Component component;
        if (prio == 0)
        {
            component = Component.translatable(TEXT_PICKUP_PRIORITY)
                .append(Component.translatable(TEXT_PICKUP_PRIORITY_NEVER));
        }
        else
        {
            component = Component.translatable(TEXT_PICKUP_PRIORITY)
                .append(Component.literal(prio + "/10"));
        }
        prioLabel.setText(component);
    }

    private void deliveryPriorityUp()
    {
        if (prio != 10)
        {
            prio++;
        }
        // TODO: Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(buildingView, true));
        updatePriorityLabel();
    }

    private void deliveryPriorityDown()
    {
        if (prio != 0)
        {
            prio--;
        }
        // TODO: Network.getNetwork().sendToServer(new ChangeDeliveryPriorityMessage(buildingView, false));
        updatePriorityLabel();
    }

    private void forcePickup()
    {
        // TODO: Network.getNetwork().sendToServer(new ForcePickupMessage(buildingView));
    }

    /**
     * Action when a hire button is clicked.
     *
     * @param button the clicked button.
     */
    protected void hireClicked(@NotNull final Button button)
    {
        // TODO: Check buildingView.allowsAssignment() and open WindowHireWorker
        // new WindowHireWorker(buildingView.getColony(), buildingView.position()).open();
    }

    /**
     * Action when a recall button is clicked.
     */
    private void recallClicked()
    {
        // TODO: Network.getNetwork().sendToServer(new RecallCitizenMessage(buildingView));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        // TODO: Populate workers list from buildingView.getModuleViews(WorkerBuildingModuleView.class)
        // For now, set up the worker list data provider with empty data
        if (findPaneByID(LIST_WORKERS) != null)
        {
            ScrollingList workerList = findPaneOfTypeByID(LIST_WORKERS, ScrollingList.class);
            workerList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return workers.size();
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: Populate worker name from colony citizen data
                }
            });
        }

        updatePriorityLabel();
    }
}
