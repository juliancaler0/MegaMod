package com.ultra.megamod.feature.citizen.screen.townhall;

import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.controls.TextField;
import com.ultra.megamod.feature.citizen.screen.AbstractWindowSkeleton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;

/**
 * UI to found a new colony.
 * Shows settlement covenant text, closest colony info, and colony name input.
 * Ported from MineColonies WindowTownHallColonyManage.
 */
public class WindowTownHallColonyManage extends AbstractWindowSkeleton
{
    private static final String BUTTON_CREATE = "create";
    private static final long TICKS_SECOND = 20;

    /**
     * Townhall position.
     */
    private final BlockPos pos;

    /**
     * If it is a reactivated colony.
     */
    private final boolean reactivate;

    public WindowTownHallColonyManage(final BlockPos pos, final String closestName, final int closestDistance, final String preName, final boolean reactivate)
    {
        super(Identifier.fromNamespaceAndPath("megamod", "gui/townhall/windowcolonymanagement.xml"));
        this.pos = pos;
        this.reactivate = reactivate;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));

        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_CREATE, this::onCreate);

        final TextField colonyNameField = this.findPaneOfTypeByID("colonyname", TextField.class);
        if (colonyNameField != null)
        {
            colonyNameField.setText(preName.isEmpty()
                ? Component.translatable("com.minecolonies.coremod.gui.colony.defaultname", mc.player.getName()).getString()
                : preName);
        }

        final Text text1 = this.findPaneOfTypeByID("text1", Text.class);
        if (text1 != null)
        {
            text1.setText(Component.translatable("com.minecolonies.core.settlementcovenant1",
                Math.max(13, Minecraft.getInstance().level.getGameTime() / TICKS_SECOND / 60 / 100)));
        }

        final Text text3 = this.findPaneOfTypeByID("text3", Text.class);
        if (text3 != null)
        {
            if (closestDistance < 1000)
            {
                text3.setText(Component.translatable("com.minecolonies.core.settlementcovenant3.hasclose",
                    Component.literal(closestName).withStyle(ChatFormatting.RED),
                    Component.literal(String.valueOf(closestDistance)).withStyle(ChatFormatting.RED)));
            }
            else
            {
                text3.setText(Component.translatable("com.minecolonies.core.settlementcovenant3.noclose"));
            }
        }
    }

    /**
     * On create button.
     */
    public void onCreate()
    {
        final TextField colonyNameField = this.findPaneOfTypeByID("colonyname", TextField.class);
        final String colonyName = colonyNameField != null ? colonyNameField.getText() : "Colony";

        // TODO: Send CreateColonyMessage to server when colony network is ported
        // TODO: Spawn dragon breath particles and play campfire sound

        close();
    }
}
