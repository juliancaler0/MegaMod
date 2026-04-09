package com.ultra.megamod.feature.citizen.screen;

import com.ultra.megamod.feature.citizen.blockui.views.BOWindow;
import com.ultra.megamod.feature.citizen.screen.modules.TabsWindowModule;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.Random;

/**
 * Manage windows associated with buildings.
 * Creates the tab sidebar navigation for module-based building GUIs.
 * Ported from MineColonies AbstractBuildingWindow.
 *
 * @param <B> Class extending IBuildingView (TODO: define colony building view interface).
 */
public abstract class AbstractBuildingWindow<B> extends AbstractWindowSkeleton
{
    /**
     * The building view instance.
     */
    protected final B buildingView;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param buildingView the building view.
     * @param resource     window resource location.
     */
    public AbstractBuildingWindow(final B buildingView, final Identifier resource)
    {
        this(null, buildingView, resource);
    }

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param parent       the parent window.
     * @param buildingView the building view.
     * @param resource     window resource location.
     */
    public AbstractBuildingWindow(final BOWindow parent, final B buildingView, final Identifier resource)
    {
        super(parent, resource);
        this.buildingView = buildingView;

        // TODO: When colony building view API is ported, register tab sidebar from module views
        // For now, register tabs module with a hash-based seed for consistent random tab icons
        final TabsWindowModule tabsWindowModule = registerModule(TabsWindowModule::new, new Random(buildingView.hashCode()));

        if (shouldRenderDefaultSidebar())
        {
            // TODO: Wire up tab buttons from buildingView.getAllModuleViews() when IBuildingModuleView is ported
            // For now, main tab is always first:
            tabsWindowModule.renderTabButton(0,
                TabsWindowModule.TabImageSide.LEFT,
                Identifier.fromNamespaceAndPath("megamod", "textures/gui/modules/main.png"),
                Component.translatable("com.minecolonies.coremod.gui.main"),
                button -> {
                    // TODO: buildingView.getWindow().open() once colony API is ported
                });
        }
    }

    /**
     * Whether this window should render the side tabs. Defaults to {@code true}.
     *
     * @return true if so.
     */
    protected boolean shouldRenderDefaultSidebar()
    {
        return true;
    }

    @Override
    public void setPage(final boolean relative, final int page)
    {
        super.setPage(relative, page);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
}
