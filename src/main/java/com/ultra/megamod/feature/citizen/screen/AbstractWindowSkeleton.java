package com.ultra.megamod.feature.citizen.screen;

import com.ultra.megamod.feature.citizen.blockui.Loader;
import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.ButtonHandler;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.BOWindow;
import com.ultra.megamod.feature.citizen.blockui.views.SwitchView;
import com.ultra.megamod.feature.citizen.screen.modules.IWindowModule;
import com.ultra.megamod.feature.citizen.screen.modules.IWindowWithLayoutModule;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Manage windows and their events.
 * Root base class for all colony GUI windows.
 * Ported from MineColonies AbstractWindowSkeleton.
 */
public abstract class AbstractWindowSkeleton extends BOWindow implements ButtonHandler
{
    @NotNull
    private final HashMap<String, Consumer<Button>> buttons;

    private final List<IWindowModule> modules = new ArrayList<>();

    /**
     * Panes used by the generic page handler
     */
    protected final Text pageNum;
    protected final Button buttonPrevPage;
    protected final Button buttonNextPage;
    protected SwitchView switchView;

    /**
     * This window's parent
     */
    @Nullable
    private final BOWindow parent;

    /**
     * Constructor with no parent window
     *
     * @param resource window resource location.
     */
    public AbstractWindowSkeleton(final Identifier resource)
    {
        this(null, resource);
    }

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param parent   the parent window.
     * @param resource window resource location.
     */
    public AbstractWindowSkeleton(@Nullable final BOWindow parent, final Identifier resource)
    {
        super(resource);
        this.parent = parent;

        buttons = new HashMap<>();

        switchView = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class);
        if (switchView != null)
        {
            buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
            buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
            PaneBuilders.singleLineTooltip(Component.translatable("com.minecolonies.core.gui.nextpage"), buttonNextPage);
            PaneBuilders.singleLineTooltip(Component.translatable("com.minecolonies.core.gui.prevpage"), buttonPrevPage);

            pageNum = findPaneOfTypeByID(LABEL_PAGE_NUMBER, Text.class);
            registerButton(BUTTON_NEXTPAGE, () -> setPage(true, 1));
            registerButton(BUTTON_PREVPAGE, () -> setPage(true, -1));
        }
        else
        {
            buttonNextPage = null;
            buttonPrevPage = null;
            pageNum = null;
        }

        // TODO: Send OpenGuiWindowTriggerMessage to server when network system is ported
    }

    /**
     * Get the list of modules.
     */
    @NotNull
    public List<IWindowModule> getModules()
    {
        return modules;
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Runnable action)
    {
        registerButton(id, button -> action.run());
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Consumer<Button> action)
    {
        buttons.put(id, action);
    }

    /**
     * Add a module to this window. Extending the original logic of the window.
     *
     * @param moduleBuilder the new module.
     */
    public final <T extends IWindowModule, A> T registerModule(final BiFunction<AbstractWindowSkeleton, A, T> moduleBuilder, final A argument)
    {
        final T module = moduleBuilder.apply(this, argument);
        this.modules.add(module);
        return module;
    }

    /**
     * Add a module to this window. Extending the original logic of the window.
     * This variant additionally loads a layout XML and injects it into the window.
     *
     * @param moduleBuilder the new module.
     */
    public final <T extends IWindowWithLayoutModule, A> T registerLayoutModule(final BiFunction<AbstractWindowSkeleton, A, T> moduleBuilder, A argument, int xPos, int yPos)
    {
        final T module = moduleBuilder.apply(this, argument);
        final Pane rootPane = Loader.createFromXMLFile2(module.getLayout(), this);
        rootPane.setPosition(xPos, yPos);
        module.onLayoutMounted(rootPane);
        this.modules.add(module);
        return module;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        this.modules.forEach(IWindowModule::onOpened);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        this.modules.forEach(IWindowModule::onUpdate);
    }

    @Override
    public void onClosed()
    {
        super.onClosed();
        this.modules.forEach(IWindowModule::onClosed);
    }

    /**
     * Handle a button clicked event. Find the registered event and execute that.
     *
     * @param button the button that was clicked.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (buttons.containsKey(button.getID()))
        {
            buttons.get(button.getID()).accept(button);
            // TODO: Send ClickGuiButtonTriggerMessage to server when network system is ported
        }

        modules.forEach(module -> module.onButtonClicked(button));
    }

    /**
     * Button clicked without an action. Method does nothing.
     *
     * @param ignored Parameter is ignored. Since some actions require a button, we must accept a button parameter.
     */
    public final void doNothing(final Button ignored)
    {
        //do nothing with that event
    }

    /**
     * Generic page handler, uses common ids
     *
     * @param relative whether page param is relative or absolute
     * @param page     if relative turn x pages forward/backward, if absolute turn to x-th page
     */
    public void setPage(final boolean relative, final int page)
    {
        if (switchView == null)
        {
            return;
        }

        final int switchPagesSize = switchView.getChildrenSize();

        if (switchPagesSize <= 1)
        {
            buttonPrevPage.off();
            buttonNextPage.off();
            pageNum.off();
            return;
        }

        final int curPage = switchView.setView(relative, page) + 1;

        buttonNextPage.on();
        buttonPrevPage.on();
        if (curPage == 1 && !switchView.isEndlessScrollingEnabled())
        {
            buttonPrevPage.off();
        }
        if (curPage == switchPagesSize && !switchView.isEndlessScrollingEnabled())
        {
            buttonNextPage.off();
        }
        if (pageNum != null)
        {
            pageNum.setText(Component.literal(curPage + "/" + switchPagesSize));
        }
    }

    @Override
    public void close()
    {
        super.close();
        if (parent != null)
        {
            parent.open();
        }
    }
}
