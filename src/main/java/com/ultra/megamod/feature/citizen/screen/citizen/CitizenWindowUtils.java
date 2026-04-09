package com.ultra.megamod.feature.citizen.screen.citizen;

import com.ultra.megamod.feature.citizen.blockui.Alignment;
import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneBuilders;
import com.ultra.megamod.feature.citizen.blockui.controls.Image;
import com.ultra.megamod.feature.citizen.blockui.controls.Text;
import com.ultra.megamod.feature.citizen.blockui.views.View;
import com.ultra.megamod.feature.citizen.screen.AbstractWindowSkeleton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.ultra.megamod.feature.citizen.screen.WindowConstants.*;
// GUI_ICONS_LOCATION removed in 1.21.11 - use Identifier.withDefaultNamespace("textures/gui/icons.png")
// import static net.minecraft.client.gui.Gui.GUI_ICONS_LOCATION;

/**
 * Utility class for creating health bars, saturation bars, happiness bars, and skill content
 * in citizen GUI windows.
 * Ported from MineColonies CitizenWindowUtils.
 */
public class CitizenWindowUtils
{
    /** Replacement for the removed Gui.GUI_ICONS_LOCATION constant */
    public static final Identifier GUI_ICONS_LOCATION = Identifier.withDefaultNamespace("textures/gui/icons.png");
    public static final Identifier HAPPINESS_ICONS_LOCATION = Identifier.fromNamespaceAndPath("megamod", "textures/gui/citizen/icons.png");

    private CitizenWindowUtils()
    {
        // Intentionally left empty.
    }

    /**
     * Enum for the available smileys (happiness display).
     */
    private enum SmileyEnum
    {
        EMPTY(HAPPINESS_ICONS_LOCATION, EMPTY_HEART_ICON_X, HEART_ICON_MC_Y, EMPTY_HEART_VALUE, null, null),
        HALF_RED(HAPPINESS_ICONS_LOCATION, HALF_RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE - 1, null, EMPTY),
        RED(HAPPINESS_ICONS_LOCATION, RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE, HALF_RED, EMPTY);

        public final int X;
        public final int Y;
        public final int happinessValue;
        public final SmileyEnum prevSmiley;
        public final SmileyEnum halfSmiley;
        public boolean isHalfSmiley = false;
        public final Identifier Image;

        SmileyEnum(final Identifier heartImage, final int x, final int y, final int happinessValue,
                   final SmileyEnum halfSmiley, final SmileyEnum prevSmiley)
        {
            this.Image = heartImage;
            this.X = x;
            this.Y = y;
            this.happinessValue = happinessValue;
            this.halfSmiley = halfSmiley;
            if (halfSmiley == null)
            {
                isHalfSmiley = true;
            }
            this.prevSmiley = prevSmiley;
        }
    }

    /**
     * Enum for the available hearts (health display).
     */
    private enum HeartsEnum
    {
        EMPTY(GUI_ICONS_LOCATION, EMPTY_HEART_ICON_X, HEART_ICON_MC_Y, EMPTY_HEART_VALUE, null, null),
        HALF_RED(GUI_ICONS_LOCATION, HALF_RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE - 1, null, EMPTY),
        RED(GUI_ICONS_LOCATION, RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE, HALF_RED, EMPTY),
        HALF_GOLDEN(GUI_ICONS_LOCATION, HALF_GOLD_HEART_ICON_X, HEART_ICON_MC_Y, GOLDEN_HEART_VALUE - 1, null, RED),
        GOLDEN(GUI_ICONS_LOCATION, GOLD_HEART_ICON_X, HEART_ICON_MC_Y, GOLDEN_HEART_VALUE, HALF_GOLDEN, RED),
        HALF_GREEN(GREEN_BLUE_ICON, GREEN_HALF_HEART_ICON_X, GREEN_HEARTS_ICON_Y, GREEN_HEART_VALUE - 1, null, GOLDEN),
        GREEN(GREEN_BLUE_ICON, GREEN_HEART_ICON_X, GREEN_HEARTS_ICON_Y, GREEN_HEART_VALUE, HALF_GREEN, GOLDEN),
        HALF_BLUE(GREEN_BLUE_ICON, BLUE_HALF_HEART_ICON_X, BLUE_HEARTS_ICON_Y, BLUE_HEART_VALUE - 1, null, GREEN),
        BLUE(GREEN_BLUE_ICON, BLUE_HEART_ICON_X, BLUE_HEARTS_ICON_Y, BLUE_HEART_VALUE, HALF_BLUE, GREEN);

        public final int X;
        public final int Y;
        public final int hpValue;
        public final HeartsEnum prevHeart;
        public final HeartsEnum halfHeart;
        public boolean isHalfHeart = false;
        public final Identifier Image;

        HeartsEnum(final Identifier heartImage, final int x, final int y, final int hpValue,
                   final HeartsEnum halfHeart, final HeartsEnum prevHeart)
        {
            this.Image = heartImage;
            this.X = x;
            this.Y = y;
            this.hpValue = hpValue;
            this.halfHeart = halfHeart;
            if (halfHeart == null)
            {
                isHalfHeart = true;
            }
            this.prevHeart = prevHeart;
        }
    }

    /**
     * Creates a health bar according to the given health value.
     *
     * @param health        the health amount.
     * @param healthBarView the health bar view.
     */
    public static void createHealthBar(int health, final View healthBarView)
    {
        healthBarView.setAlignment(Alignment.MIDDLE_RIGHT);
        final Text healthLabel = healthBarView.findPaneOfTypeByID(WINDOW_ID_HEALTHLABEL, Text.class);
        if (healthLabel != null)
        {
            healthLabel.setText(Component.literal(Integer.toString(health / 2)));
        }

        // Add empty heart background
        for (int i = 0; i < MAX_HEART_ICONS; i++)
        {
            addHeart(healthBarView, i, HeartsEnum.EMPTY);
        }

        // Current heart we're filling
        int heartPos = 0;

        // Order we're filling the hearts with from high to low
        final List<HeartsEnum> heartList = new ArrayList<>();
        heartList.add(HeartsEnum.BLUE);
        heartList.add(HeartsEnum.GREEN);
        heartList.add(HeartsEnum.GOLDEN);
        heartList.add(HeartsEnum.RED);

        for (final HeartsEnum heart : heartList)
        {
            if (heart.isHalfHeart || heart.prevHeart == null)
            {
                continue;
            }

            // Add full hearts
            for (int i = heartPos; i < MAX_HEART_ICONS && health > (heart.prevHeart.hpValue * MAX_HEART_ICONS + 1); i++)
            {
                addHeart(healthBarView, heartPos, heart);
                health -= (heart.hpValue - heart.prevHeart.hpValue);
                heartPos++;
            }

            // Add half heart
            if (health % 2 == 1 && heartPos < MAX_HEART_ICONS && heart.halfHeart != null && health > heart.prevHeart.hpValue * MAX_HEART_ICONS)
            {
                addHeart(healthBarView, heartPos, heart.prevHeart);
                addHeart(healthBarView, heartPos, heart.halfHeart);
                health -= (heart.halfHeart.hpValue - heart.prevHeart.hpValue);
                heartPos++;
            }

            if (heartPos >= MAX_HEART_ICONS)
            {
                return;
            }
        }
    }

    /**
     * Adds a heart to the healthbarView at the given position.
     */
    private static void addHeart(final View healthBarView, final int heartPos, final HeartsEnum heart)
    {
        @NotNull final Image heartImage = new Image();
        heartImage.setImage(heart.Image, heart.X, heart.Y, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);

        heartImage.setSize(HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
        heartImage.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
        healthBarView.addChild(heartImage);
    }

    private static int getYOffset(final int i)
    {
        return (i >= 10 ? SATURATION_ICON_POS_Y : 0);
    }

    private static int getXOffsetModifier(final int i)
    {
        return (i >= 10 ? i - 10 : i);
    }

    /**
     * Creates a saturation bar.
     *
     * @param curSaturation the current saturation level.
     * @param view          the view to add these to.
     */
    public static void createSaturationBar(final double curSaturation, final View view)
    {
        final View satBar = view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class);
        if (satBar == null)
        {
            return;
        }

        satBar.setAlignment(Alignment.MIDDLE_RIGHT);

        final int maxSaturation = 20; // ICitizenData.MAX_SATURATION

        // Max saturation (black food items)
        for (int i = 0; i < maxSaturation; i++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(GUI_ICONS_LOCATION, EMPTY_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);

            saturation.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(getXOffsetModifier(i) * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y + getYOffset(i));
            satBar.addChild(saturation);
        }

        // Current saturation (full food hearts)
        int saturationPos;
        for (saturationPos = 0; saturationPos < ((int) curSaturation); saturationPos++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(GUI_ICONS_LOCATION, FULL_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);

            saturation.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(getXOffsetModifier(saturationPos) * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y + getYOffset(saturationPos));
            satBar.addChild(saturation);
        }

        // Half food items
        if (curSaturation / 2 % 1 > 0)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(GUI_ICONS_LOCATION, HALF_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);

            saturation.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
            saturation.setPosition(getXOffsetModifier(saturationPos) * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y + getYOffset(saturationPos));
            satBar.addChild(saturation);
        }
    }

    /**
     * Creates a happiness bar according to the given happiness value.
     *
     * @param happiness        the happiness value (0-10).
     * @param happinessBarView the happiness bar view.
     */
    public static void createHappinessBar(int happiness, final View happinessBarView)
    {
        happiness = happiness * 2;

        // Add empty smiley background
        for (int i = 0; i < MAX_HEART_ICONS; i++)
        {
            addSmiley(happinessBarView, i, SmileyEnum.EMPTY);
        }

        int smileyPos = 0;
        final List<SmileyEnum> heartList = new ArrayList<>();
        heartList.add(SmileyEnum.RED);

        for (final SmileyEnum smiley : heartList)
        {
            if (smiley.isHalfSmiley || smiley.prevSmiley == null)
            {
                continue;
            }

            for (int i = smileyPos; i < MAX_HEART_ICONS && happiness > (smiley.prevSmiley.happinessValue * MAX_HEART_ICONS + 1); i++)
            {
                addSmiley(happinessBarView, smileyPos, smiley);
                happiness -= (smiley.happinessValue - smiley.prevSmiley.happinessValue);
                smileyPos++;
            }

            if (happiness % 2 == 1 && smileyPos < MAX_HEART_ICONS && smiley.halfSmiley != null && happiness > smiley.prevSmiley.happinessValue * MAX_HEART_ICONS)
            {
                addSmiley(happinessBarView, smileyPos, smiley.prevSmiley);
                addSmiley(happinessBarView, smileyPos, smiley.halfSmiley);
                happiness -= (smiley.halfSmiley.happinessValue - smiley.prevSmiley.happinessValue);
                smileyPos++;
            }

            if (smileyPos >= MAX_HEART_ICONS)
            {
                return;
            }
        }
    }

    /**
     * Adds a smiley to the happiness view at the given position.
     */
    private static void addSmiley(final View happinessBarView, final int happinessPos, final SmileyEnum smiley)
    {
        @NotNull final Image smileyImage = new Image();
        smileyImage.setImage(smiley.Image, smiley.X, smiley.Y, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);

        smileyImage.setSize(HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
        smileyImage.setPosition(happinessPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
        happinessBarView.addChild(smileyImage);
    }

    /**
     * General happiness bar setup on a window.
     *
     * @param happiness the happiness value (0-10).
     * @param window    the window to set up on.
     */
    public static void createHappinessBar(final int happiness, final AbstractWindowSkeleton window)
    {
        final View happinessBar = window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class);
        if (happinessBar != null)
        {
            happinessBar.setAlignment(Alignment.MIDDLE_RIGHT);
        }
        final Text happinessLabel = window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS, Text.class);
        if (happinessLabel != null)
        {
            happinessLabel.setText(Component.literal(Integer.toString(happiness)));
        }
        if (happinessBar != null)
        {
            createHappinessBar(happiness, happinessBar);
        }
    }
}
