package com.ultra.megamod.feature.citizen.screen.huts;

import com.ultra.megamod.feature.citizen.screen.AbstractWindowWorkerModuleBuilding;
import net.minecraft.resources.Identifier;

/**
 * Generic worker building placeholder window.
 * Used by all worker buildings that don't have a specialized GUI.
 * Ported from MineColonies WindowHutWorkerModulePlaceholder.
 *
 * @param <B> Object extending IBuildingView (TODO: define colony building view interface).
 */
public class WindowHutWorkerModulePlaceholder<B> extends AbstractWindowWorkerModuleBuilding<B>
{
    /**
     * Constructor for generic worker building placeholder.
     *
     * @param building the building view.
     */
    public WindowHutWorkerModulePlaceholder(final B building)
    {
        super(building, Identifier.fromNamespaceAndPath("megamod", "gui/windowhutworkerplaceholder.xml"));
    }
}
