package com.ultra.megamod.feature.citizen.screen;

import com.ultra.megamod.feature.citizen.blockui.Alignment;
import com.ultra.megamod.feature.citizen.blockui.controls.Button;
import com.ultra.megamod.feature.citizen.blockui.controls.ButtonImage;
import com.ultra.megamod.feature.citizen.blockui.controls.ItemIcon;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.Box;
import com.ultra.megamod.feature.citizen.screen.citizen.MainWindowCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * Citizen interaction window.
 * Shows the citizen's dialogue with response buttons for quest/interaction handling.
 * Ported from MineColonies WindowInteraction.
 */
public class WindowInteraction extends AbstractWindowSkeleton
{
    /**
     * Response buttons default id, gets the response index added to the end 1 to x.
     */
    public static final String BUTTON_RESPONSE_ID = "response_";

    /**
     * The citizen data view object.
     * TODO: Type as ICitizenDataView when colony API is ported.
     */
    private final Object citizen;

    /**
     * The current interaction index in the list.
     */
    private int currentInteraction = 0;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen data view to bind the window to.
     */
    public WindowInteraction(final Object citizen)
    {
        super(new MainWindowCitizen(citizen), Identifier.fromNamespaceAndPath("megamod", "gui/citizen/windowinteraction.xml"));
        this.citizen = citizen;
        registerButton(BUTTON_CANCEL, this::cancelClicked);
    }

    private void cancelClicked()
    {
        close();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        setupInteraction();
    }

    /**
     * Setup the current interaction.
     * TODO: Wire to IInteractionResponseHandler when colony interaction API is ported.
     */
    private void setupInteraction()
    {
        // Make sure requestItem icon is hidden by default
        final ItemIcon requestItem = window.findPaneOfTypeByID("requestItem", ItemIcon.class);
        if (requestItem != null)
        {
            requestItem.hide();
        }

        // TODO: Get interaction handler from citizen.getOrderedInteractions()
        // TODO: Set chat text from handler.getInquiry()
        // TODO: Create response buttons from handler.getPossibleResponses()

        final Box group = findPaneOfTypeByID(RESPONSE_BOX_ID, Box.class);
        if (group != null)
        {
            group.getChildren().clear();
        }

        final Text chatText = findPaneOfTypeByID(CHAT_LABEL_ID, Text.class);
        if (chatText != null)
        {
            chatText.setTextAlignment(Alignment.TOP_LEFT);
            chatText.setAlignment(Alignment.TOP_LEFT);
            // TODO: chatText.setText(Component.literal(citizen.getName() + ": " + handler.getInquiry(...).getString()));
            chatText.setText(Component.literal("Citizen: Hello!"));
        }
    }

    @Override
    public void onClosed()
    {
        super.onClosed();
        // TODO: Send InteractionClose message to server
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_CANCEL))
        {
            super.onButtonClicked(button);
        }
        else
        {
            // TODO: Handle response button clicks via IInteractionResponseHandler
            // Parse response index from button ID: Integer.parseInt(button.getID().replace("response_", "")) - 1
            // Call handler.onClientResponseTriggered(index, player, citizen, this)
            currentInteraction++;
            setupInteraction();
        }
    }
}
