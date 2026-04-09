package com.ultra.megamod.feature.citizen.screen;

import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.BOWindow;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.DESC_LABEL;

/**
 * Generic module window class. This creates the navigational menu.
 * Base for building module sub-pages (crafting, settings, etc.).
 * Ported from MineColonies AbstractModuleWindow.
 *
 * @param <T> Module view type (TODO: define IBuildingModuleView interface).
 */
public abstract class AbstractModuleWindow<T> extends AbstractBuildingWindow<Object>
{
    /**
     * Module view.
     */
    protected final T moduleView;

    /**
     * Constructor for the window.
     *
     * @param moduleView the module view.
     * @param buildingView the building view this module belongs to.
     * @param resource   window resource location.
     */
    public AbstractModuleWindow(final T moduleView, final Object buildingView, final Identifier resource)
    {
        this(null, moduleView, buildingView, resource);
    }

    /**
     * Constructor for the window.
     *
     * @param parent     the parent window.
     * @param moduleView the module view.
     * @param buildingView the building view this module belongs to.
     * @param resource   window resource location.
     */
    public AbstractModuleWindow(final BOWindow parent, final T moduleView, final Object buildingView, final Identifier resource)
    {
        super(parent, buildingView, resource);
        this.moduleView = moduleView;

        // TODO: Set header from moduleView.getDesc() when colony API is ported
    }

    /**
     * Update the header
     *
     * @param header the header text.
     */
    protected void setHeader(@Nullable final MutableComponent header)
    {
        final Text labelPane = window.findPaneOfTypeByID(DESC_LABEL, Text.class);
        if (labelPane != null && header != null)
        {
            labelPane.setText(header);
        }
    }
}
