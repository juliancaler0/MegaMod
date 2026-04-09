package com.ultra.megamod.feature.citizen.screen.citizen;

import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.*;
import com.ultra.megamod.feature.citizen.blockui.views.View;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Main citizen detail window.
 * Shows citizen name, gender, health bar, saturation bar, happiness bar, and skill levels.
 * Ported from MineColonies MainWindowCitizen.
 */
public class MainWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Tick function for updating every second.
     */
    private int tick = 0;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     */
    public MainWindowCitizen(final Object citizen)
    {
        super(citizen, Identifier.fromNamespaceAndPath("megamod", "gui/citizen/main.xml"));

        // TODO: Set status icon from citizen.getVisibleStatus() when colony API is ported
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (tick++ == 20)
        {
            tick = 0;
            // TODO: CitizenWindowUtils.createSkillContent(citizen, this) when colony API is ported
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        // TODO: Set citizen name, health bar, saturation bar, happiness bar, skill content, gender icon
        // when colony citizen data API is ported:
        // findPaneOfTypeByID(WINDOW_ID_NAME, Text.class).setText(Component.literal(citizen.getName()));
        // CitizenWindowUtils.createHealthBar(citizen, findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
        // CitizenWindowUtils.createSaturationBar(citizen, this);
        // CitizenWindowUtils.createHappinessBar(citizen, this);
        // CitizenWindowUtils.createSkillContent(citizen, this);
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        // Handle skill adjustment buttons (creative-only)
        if (button.getID().contains(PLUS_PREFIX))
        {
            final String label = button.getID().replace(PLUS_PREFIX, "");
            // TODO: Network.getNetwork().sendToServer(new AdjustSkillCitizenMessage(colony, citizen, 1, Skill.valueOf(label)));
        }
        else if (button.getID().contains(MINUS_PREFIX))
        {
            final String label = button.getID().replace(MINUS_PREFIX, "");
            // TODO: Network.getNetwork().sendToServer(new AdjustSkillCitizenMessage(colony, citizen, -1, Skill.valueOf(label)));
        }
    }
}
