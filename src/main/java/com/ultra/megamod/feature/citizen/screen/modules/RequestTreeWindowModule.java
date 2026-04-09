package com.ultra.megamod.feature.citizen.screen.modules;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.*;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import com.ultra.megamod.feature.citizen.screen.AbstractWindowSkeleton;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Window module for displaying request trees with depth-indented items.
 * Supports cancel, fulfill, and detail-view actions on requests.
 * Ported from MineColonies RequestTreeWindowModule.
 */
public abstract class RequestTreeWindowModule implements IWindowWithLayoutModule
{
    private static final int AUTO_REFRESH_TICKS = 100;

    /**
     * The parenting window.
     */
    protected final AbstractWindowSkeleton parent;

    /**
     * The colony view.
     * TODO: Type as IColonyView when colony API is ported.
     */
    protected final Object colony;

    /**
     * Scrolling list of the resources.
     */
    protected ScrollingList resourceList;

    /**
     * Life count for rotating item displays.
     */
    private int lifeCount = 0;

    /**
     * Current ticks until next refresh.
     */
    private int ticks = 0;

    /**
     * Constructor to initiate the window request tree windows.
     *
     * @param parent the parenting window.
     * @param colony the colony we're located in.
     */
    public RequestTreeWindowModule(final AbstractWindowSkeleton parent, final Object colony)
    {
        this.parent = parent;
        this.colony = colony;
    }

    @Override
    public void onLayoutMounted(final Pane rootPane)
    {
        parent.registerButton(REQUEST_DETAIL, this::detailedClicked);
        parent.registerButton(REQUEST_CANCEL, this::cancel);
        parent.registerButton(REQUEST_FULFILL, this::onFulfill);

        resourceList = rootPane.findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);
        if (resourceList != null)
        {
            resourceList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return getCachedOpenRequests().size()
                }

                @Override
                public void updateElement(final int index, final Pane rowPane)
                {
                    // TODO: Populate request display (icon, text, depth indentation, cancel/fulfill buttons)
                }
            });
        }
    }

    @Override
    @NotNull
    public final Identifier getLayout()
    {
        return Identifier.fromNamespaceAndPath("megamod", "gui/layouthuts/layoutrequeststree.xml");
    }

    @Override
    public void onUpdate()
    {
        final long window = GLFW.glfwGetCurrentContext();
        final boolean shiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!shiftDown)
        {
            lifeCount++;
        }
        ticks++;

        if (ticks >= AUTO_REFRESH_TICKS)
        {
            refreshOpenRequests();
            ticks = 0;
        }
    }

    /**
     * Refresh the map of open requests.
     */
    public final void refreshOpenRequests()
    {
        // TODO: Invalidate cached request list
    }

    /**
     * Get the open requests for this module to handle.
     *
     * @return an immutable collection.
     */
    protected abstract Collection<?> getOpenRequests();

    /**
     * If the request can show child requests as well.
     */
    protected boolean canDisplayChildRequests()
    {
        return true;
    }

    /**
     * After request cancel has been clicked.
     */
    private void cancel(@NotNull final Button button)
    {
        // TODO: Get request from list index, send UpdateRequestStateMessage with CANCELLED
    }

    /**
     * On fulfill button click.
     */
    private void onFulfill(@NotNull final Button button)
    {
        // TODO: Transfer items from player inventory to citizen and overrule request
    }

    /**
     * After request detail has been clicked open the detail window.
     */
    private void detailedClicked(@NotNull final Button button)
    {
        // TODO: new WindowRequestDetail(parent, request, colonyId, this).open();
    }

    /**
     * Open the detail page for a given request.
     *
     * @param request the request instance.
     */
    public final void openDetails(final Object request)
    {
        // TODO: new WindowRequestDetail(parent, request, colony.getID(), this).open();
    }

    /**
     * Interface for setting that the request tree window supports fulfilling.
     */
    public interface IRequestTreeSupportsFulfill
    {
        void onFulfill(@NotNull final Object request);
    }
}
