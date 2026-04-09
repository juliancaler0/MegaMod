package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.DropDownList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Town hall main actions page.
 * Shows colony name, color picker, texture style, name style, mercenary button, map button.
 * Ported from MineColonies WindowMainPage.
 *
 * @param <V> Town hall view type.
 */
public class WindowMainPage<V> extends AbstractWindowTownHall<V>
{
    /**
     * Drop down list for color style.
     */
    private DropDownList colorDropDownList;

    /**
     * Label for the colony name.
     */
    private final Text title;

    /**
     * Constructor for the town hall window.
     *
     * @param building the town hall view.
     */
    public WindowMainPage(final V building)
    {
        super(building, "layoutactions.xml");
        initDropDowns();

        title = findPaneOfTypeByID(LABEL_BUILDING_NAME, Text.class);

        registerButton(BUTTON_CHANGE_SPEC, this::doNothing);
        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_MERCENARY, this::mercenaryClicked);
        registerButton(BUTTON_TOWNHALLMAP, this::mapButtonClicked);
        registerButton(BUTTON_BANNER_PICKER, this::openBannerPicker);
        registerButton(BUTTON_RESET_TEXTURE, this::resetTextureStyle);
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initDropDowns()
    {
        colorDropDownList = findPaneOfTypeByID(DROPDOWN_COLOR_ID, DropDownList.class);
        if (colorDropDownList != null)
        {
            colorDropDownList.setHandler(this::onDropDownListChanged);

            final List<ChatFormatting> textColors = Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).toList();

            colorDropDownList.setDataProvider(new DropDownList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return textColors.size();
                }

                @Override
                public MutableComponent getLabel(final int index)
                {
                    if (index >= 0 && index < textColors.size())
                    {
                        final String colorName = textColors.get(index).getName().replace("_", " ");
                        return Component.literal(colorName.substring(0, 1).toUpperCase(Locale.US) + colorName.substring(1));
                    }
                    return Component.literal("");
                }
            });
        }

        // TODO: Initialize texture style and name style dropdowns when colony API is ported
    }

    /**
     * Called when the dropdownList changed.
     */
    private void onDropDownListChanged(final DropDownList dropDownList)
    {
        // TODO: Network.getNetwork().sendToServer(new TeamColonyColorChangeMessage(dropDownList.getSelectedIndex(), buildingView));
    }

    private void openBannerPicker(final Button button)
    {
        // TODO: Open WindowBannerPicker when ported
    }

    private void resetTextureStyle()
    {
        // TODO: Network.getNetwork().sendToServer(new ColonyTextureStyleMessage(buildingView.getColony(), "default"));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (title != null)
        {
            // TODO: title.setText(Component.literal(buildingView.getColony().getName()));
            title.setText(Component.literal("Colony"));
        }
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        // TODO: new WindowTownHallNameEntry(buildingView.getColony()).open();
    }

    /**
     * Action performed when mercenary button is clicked.
     */
    private void mercenaryClicked()
    {
        // TODO: new WindowTownHallMercenary(buildingView.getColony()).open();
    }

    /**
     * Opens the map on button clicked.
     */
    private void mapButtonClicked()
    {
        // TODO: new WindowColonyMap(true, buildingView).open();
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_ACTIONS;
    }
}
