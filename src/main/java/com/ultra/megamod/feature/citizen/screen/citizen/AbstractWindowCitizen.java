package com.ultra.megamod.feature.citizen.screen.citizen;

import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.screen.AbstractWindowSkeleton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Abstract base for citizen detail windows.
 * Sets up navigation tabs for citizen pages (main, requests, inventory, happiness, family, job).
 * Ported from MineColonies AbstractWindowCitizen.
 *
 * The citizen data is passed as a generic Object for now since the colony citizen data view
 * interfaces are being ported by another agent. When ICitizenDataView is available,
 * this class should be updated to use it.
 */
public abstract class AbstractWindowCitizen extends AbstractWindowSkeleton
{
    /**
     * The citizen data view object.
     * TODO: Type this as ICitizenDataView when the colony API is ported.
     */
    protected final Object citizen;

    /**
     * The colony view.
     * TODO: Type this as IColonyView when the colony API is ported.
     */
    protected final Object colony;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     * @param ui      the xml resource location.
     */
    public AbstractWindowCitizen(final Object citizen, final Identifier ui)
    {
        super(ui);
        this.citizen = citizen;
        this.colony = null; // TODO: citizen.getColony() when colony API is ported

        // ── Navigation Tab Buttons ───────────────────────────────────

        registerButton("mainTab", () -> new MainWindowCitizen(citizen).open());
        registerButton("mainIcon", () -> new MainWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("mainIcon")).build()
            .setText(Component.translatable("com.minecolonies.coremod.gui.citizen.main"));

        registerButton("requestTab", () -> new RequestWindowCitizen(citizen).open());
        registerButton("requestIcon", () -> new RequestWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("requestIcon")).build()
            .setText(Component.translatable("com.minecolonies.coremod.gui.citizen.requests"));

        registerButton("inventoryTab", () -> {
            // TODO: Network.getNetwork().sendToServer(new OpenInventoryMessage(citizen.getColony(), citizen.getName(), citizen.getEntityId()));
        });
        registerButton("inventoryIcon", () -> {
            // TODO: Network.getNetwork().sendToServer(new OpenInventoryMessage(citizen.getColony(), citizen.getName(), citizen.getEntityId()));
        });
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("inventoryIcon")).build()
            .setText(Component.translatable("com.minecolonies.coremod.gui.citizen.inventory"));

        registerButton("happinessTab", () -> new HappinessWindowCitizen(citizen).open());
        registerButton("happinessIcon", () -> new HappinessWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("happinessIcon")).build()
            .setText(Component.translatable("com.minecolonies.coremod.gui.citizen.happiness"));

        registerButton("familyTab", () -> new FamilyWindowCitizen(citizen).open());
        registerButton("familyIcon", () -> new FamilyWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("familyIcon")).build()
            .setText(Component.translatable("com.minecolonies.coremod.gui.citizen.family"));

        // TODO: Wire job tab visibility from citizen.getWorkBuilding() when colony API is ported
        // For now, always show job tab
        registerButton("jobTab", () -> new JobWindowCitizen(citizen).open());
        registerButton("jobIcon", () -> new JobWindowCitizen(citizen).open());
        PaneBuilders.tooltipBuilder().hoverPane(findPaneByID("jobIcon")).build()
            .setText(Component.translatable("com.minecolonies.coremod.gui.citizen.job"));
    }
}
