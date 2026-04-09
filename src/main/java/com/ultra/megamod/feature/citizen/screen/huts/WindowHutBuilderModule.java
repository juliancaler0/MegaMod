package com.ultra.megamod.feature.citizen.screen.huts;

import com.ultra.megamod.feature.citizen.screen.AbstractWindowWorkerModuleBuilding;
import net.minecraft.resources.Identifier;

/**
 * Builder's hut window.
 * Uses the generic worker placeholder layout but can optionally show a guide on first open.
 * Ported from MineColonies WindowHutBuilderModule.
 *
 * @param <B> Builder building view type (TODO: define BuildingBuilder.View when colony API is ported).
 */
public class WindowHutBuilderModule<B> extends AbstractWindowWorkerModuleBuilding<B>
{
    /**
     * If the guide should be attempted to be opened.
     */
    private final boolean needGuide;

    /**
     * Constructor for window builder hut.
     *
     * @param building the builder building view.
     */
    public WindowHutBuilderModule(final B building)
    {
        this(building, true);
    }

    /**
     * Constructor for window builder hut.
     *
     * @param building  the builder building view.
     * @param needGuide if the guide should be opened on first open.
     */
    public WindowHutBuilderModule(final B building, final boolean needGuide)
    {
        super(building, Identifier.fromNamespaceAndPath("megamod", "gui/windowhutworkerplaceholder.xml"));
        this.needGuide = needGuide;
    }

    @Override
    public void onOpened()
    {
        if (needGuide)
        {
            // TODO: Check if player has the guide advancement
            // If not, close this window and open WindowHutGuide(buildingView) instead
            // final Advancement ad = ...
            // if (ad == null || !done) { close(); new WindowHutGuide(buildingView).open(); return; }
        }
        super.onOpened();
    }
}
