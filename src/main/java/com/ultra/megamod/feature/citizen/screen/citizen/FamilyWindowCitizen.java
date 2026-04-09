package com.ultra.megamod.feature.citizen.screen.citizen;

import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.ScrollingList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen family detail window.
 * Shows parents, partner, siblings, and children.
 * Ported from MineColonies FamilyWindowCitizen.
 */
public class FamilyWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Holder of a list element.
     */
    protected final ScrollingList siblingList;
    protected final ScrollingList childrenList;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     */
    public FamilyWindowCitizen(final Object citizen)
    {
        super(citizen, Identifier.fromNamespaceAndPath("megamod", "gui/citizen/family.xml"));
        siblingList = findPaneOfTypeByID("siblings", ScrollingList.class);
        childrenList = findPaneOfTypeByID("children", ScrollingList.class);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        // TODO: Populate parent names, partner name from citizen data
        final Text parentA = findPaneOfTypeByID("parentA", Text.class);
        if (parentA != null)
        {
            parentA.setText(Component.translatable("com.minecolonies.coremod.gui.citizen.family.unknown"));
        }
        final Text parentB = findPaneOfTypeByID("parentB", Text.class);
        if (parentB != null)
        {
            parentB.setText(Component.translatable("com.minecolonies.coremod.gui.citizen.family.unknown"));
        }
        final Text partner = findPaneOfTypeByID("partner", Text.class);
        if (partner != null)
        {
            partner.setText(Component.literal("-"));
        }

        // TODO: Populate children and siblings lists from citizen data
        if (childrenList != null)
        {
            childrenList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return citizen.getChildren().size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: rowPane.findPaneOfTypeByID("name", Text.class).setText(...)
                }
            });
        }

        if (siblingList != null)
        {
            siblingList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return 0; // TODO: return citizen.getSiblings().size()
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    // TODO: rowPane.findPaneOfTypeByID("name", Text.class).setText(...)
                }
            });
        }
    }
}
