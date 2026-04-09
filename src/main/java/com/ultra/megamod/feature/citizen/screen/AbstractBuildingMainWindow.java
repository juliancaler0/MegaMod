package com.ultra.megamod.feature.citizen.screen;

import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Base window class for the main window behind buildings.
 * Provides build/repair button management, inventory access, name editing.
 * Ported from MineColonies AbstractBuildingMainWindow.
 *
 * @param <B> Class extending IBuildingView (TODO: define colony building view interface).
 */
public abstract class AbstractBuildingMainWindow<B> extends AbstractBuildingWindow<B>
{
    // ── Translation keys for build button states ─────────────────────────
    private static final String ACTION_BUILD_REPAIR = "com.minecolonies.coremod.gui.workerhuts.buildrepair";
    private static final String ACTION_CANCEL_BUILD = "com.minecolonies.coremod.gui.workerhuts.cancelbuild";
    private static final String ACTION_CANCEL_UPGRADE = "com.minecolonies.coremod.gui.workerhuts.cancelupgrade";
    private static final String ACTION_CANCEL_REPAIR = "com.minecolonies.coremod.gui.workerhuts.cancelrepair";
    private static final String ACTION_CANCEL_DECONSTRUCTION = "com.minecolonies.coremod.gui.workerhuts.canceldeconstruction";
    private static final String PARTIAL_INFO_TEXT = "com.minecolonies.coremod.gui.workerhuts.info.";

    /**
     * The title displayed at the top of the window showing the building name.
     */
    private final Text title;

    /**
     * The build button.
     */
    private final Button buttonBuild;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param buildingView the building view.
     * @param resource     window resource location.
     */
    public AbstractBuildingMainWindow(final B buildingView, final Identifier resource)
    {
        super(buildingView, resource);

        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_INFO, this::infoClicked);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        registerButton(BUTTON_EDIT_NAME, this::editName);
        registerButton(BUTTON_ALLINVENTORY, this::allInventoryClicked);

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);
        buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        // TODO: Wire info button visibility from building type translation key when colony API is ported
    }

    /**
     * Action when build button is clicked.
     * TODO: Wire to BuildRequestMessage and WindowBuildBuilding when colony network is ported.
     */
    private void buildClicked()
    {
        if (buttonBuild == null)
        {
            return;
        }

        final String buttonLabel = buttonBuild.getText().getContents() instanceof TranslatableContents
            ? ((TranslatableContents) buttonBuild.getText().getContents()).getKey()
            : buttonBuild.getTextAsString();

        if (buttonLabel.equalsIgnoreCase(ACTION_CANCEL_BUILD) || buttonLabel.equalsIgnoreCase(ACTION_CANCEL_UPGRADE))
        {
            // TODO: Network.getNetwork().sendToServer(new BuildRequestMessage(buildingView, BuildRequestMessage.Mode.BUILD, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(ACTION_CANCEL_REPAIR))
        {
            // TODO: Network.getNetwork().sendToServer(new BuildRequestMessage(buildingView, BuildRequestMessage.Mode.REPAIR, BlockPos.ZERO));
        }
        else if (buttonLabel.equalsIgnoreCase(ACTION_CANCEL_DECONSTRUCTION))
        {
            // TODO: Network.getNetwork().sendToServer(new BuildRequestMessage(buildingView, BuildRequestMessage.Mode.REMOVE, BlockPos.ZERO));
        }
        else
        {
            // TODO: new WindowBuildBuilding(buildingView.getColony(), buildingView).open();
        }
    }

    /**
     * Action when info button is clicked.
     */
    private void infoClicked()
    {
        // TODO: new WindowInfo(buildingView).open();
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        // TODO: Network.getNetwork().sendToServer(new OpenInventoryMessage(buildingView));
    }

    /**
     * Edit custom name action.
     */
    private void editName()
    {
        // TODO: new WindowHutNameEntry(buildingView).open();
    }

    /**
     * Action when allInventory button is clicked.
     */
    private void allInventoryClicked()
    {
        // TODO: new WindowHutAllInventory(buildingView, this).open();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateButtonBuild();
    }

    /**
     * Update the state and label for the Build button.
     * TODO: Read building state from buildingView when colony API is ported.
     */
    private void updateButtonBuild()
    {
        if (buttonBuild == null)
        {
            return;
        }

        // TODO: Check buildingView.isBuilding(), isRepairing(), isDeconstructing()
        // and set appropriate cancel text. Default to build/repair.
        // For now, keep whatever text was set in the XML.
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setPage(false, 0);

        if (title != null)
        {
            // TODO: Use buildingView.getBuildingDisplayName() and getBuildingLevel()
            // For now just set a placeholder
            title.setText(Component.literal("Building"));
        }

        updateButtonBuild();
    }
}
