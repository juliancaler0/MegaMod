package com.ultra.megamod.lib.combatroll.client.gui;

import com.ultra.megamod.lib.combatroll.client.CombatRollClient;
import com.ultra.megamod.lib.combatroll.client.Keybindings;
import com.ultra.megamod.lib.combatroll.internals.RollManager;
import com.ultra.megamod.lib.combatroll.internals.RollingEntity;
import com.ultra.megamod.mixin.combatroll.CombatRollKeybindingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HudRenderHelper {
    private static final Identifier ARROW = Identifier.fromNamespaceAndPath("megamod", "textures/hud/combatroll_arrow.png");
    private static final Identifier ARROW_BACKGROUND = Identifier.fromNamespaceAndPath("megamod", "textures/hud/combatroll_arrow_background.png");

    public static void render(GuiGraphics context, float tickDelta) {
        var config = CombatRollClient.config;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ViewModel viewModel;
        if (player == null) {
            viewModel = ViewModel.mock();
        } else {
            if (player.isCreative() && !config.showHUDInCreative) {
                return;
            }
            if (player.isSpectator()) {
                return;
            }
            var rollingPlayer = ((RollingEntity)player);
            var cooldownInfo = rollingPlayer.getRollManager().getCooldown();
            if (!config.showWhenFull && cooldownInfo.availableRolls() == cooldownInfo.maxRolls()) {
                return;
            }
            viewModel = ViewModel.create(cooldownInfo, tickDelta);
        }

        var screenWidth = client.getWindow().getGuiScaledWidth();
        var screenHeight = client.getWindow().getGuiScaledHeight();
        var rollWidget = CombatRollClient.hudConfig.rollWidget;
        var originPoint = rollWidget.origin.getPoint(screenWidth, screenHeight);
        var drawOffset = rollWidget.offset;

        int horizontalSpacing = 8;
        int biggestTextureSize = 15;
        int widgetWidth = biggestTextureSize + (horizontalSpacing * viewModel.elements.size());
        int widgetHeight = biggestTextureSize;
        int drawX = (int) (originPoint.x + drawOffset.x);
        int drawY = (int) (originPoint.y + drawOffset.y - (widgetHeight) / 2);
        int drawnWith = 0;
        for(var element: viewModel.elements()) {
            int x = 0;
            int y = 0;
            int u = 0;
            int v = 0;
            int width = 0;
            int height = 0;
            int textureSize = 0;

            x = drawX + drawnWith;
            y = drawY;
            u = 0;
            v = 0;
            width = height = textureSize = 15;
            var backgroundColorARGB = ARGB.colorFromFloat(((float)config.hudBackgroundOpacity) / 100F, 1, 1, 1);
            context.blit(RenderPipelines.GUI_TEXTURED, ARROW_BACKGROUND, x, y, (float)u, (float)v, width, height, textureSize, textureSize, backgroundColorARGB);

            var color = element.color;
            float red = ((float) ((color >> 16) & 0xFF)) / 255F;
            float green = ((float) ((color >> 8) & 0xFF)) / 255F;
            float blue = ((float) (color & 0xFF)) / 255F;

            var arrowColorARGB = ARGB.colorFromFloat(1F, red, green, blue);

            var prevTextureSize = textureSize;
            textureSize = 13;
            var shift = (prevTextureSize - textureSize) / 2;
            width = textureSize;
            height = Math.round((element.full) * textureSize);
            x = drawX + drawnWith + shift;
            y = drawY + textureSize - height + shift;
            u = 0;
            v = textureSize - height;
            context.blit(RenderPipelines.GUI_TEXTURED, ARROW, x, y, (float)u, (float)v, width, height, textureSize, textureSize, arrowColorARGB);

            drawnWith += horizontalSpacing;
        }

        if (config.showKeybinding) {
            var font = client.gui.getFont();

            int keybindingX = drawX + drawnWith / 2;
            int keybindingY = drawY + 1;

            var iconHAnchor = Drawable.Anchor.CENTER;
            var iconVAnchor = Drawable.Anchor.TRAILING;

            switch (config.keybindingLabelPosition) {
                case TOP -> {
                    // keybindingY -= 1;
                }
                case LEFT -> {
                    keybindingX = drawX;
                    keybindingY = drawY + widgetHeight / 2;
                    iconHAnchor = Drawable.Anchor.TRAILING;
                    iconVAnchor = Drawable.Anchor.CENTER;
                }
            }

            if (viewModel.drawable != null) {
                viewModel.drawable.draw(context, keybindingX, keybindingY, iconHAnchor, iconVAnchor);
            } else if (viewModel.label != null) {
                var label = viewModel.label;
                var textLength = font.width(label);
                var buttonLength = textLength + HudKeyVisuals.buttonLeading.draw().width() + HudKeyVisuals.buttonTrailing.draw().width();
                if (iconHAnchor == Drawable.Anchor.TRAILING) {
                    keybindingX -= buttonLength / 2;
                }

                HudKeyVisuals.buttonLeading.draw(context, keybindingX - (textLength / 2), keybindingY, Drawable.Anchor.TRAILING, iconVAnchor);
                HudKeyVisuals.buttonCenter.drawFlexibleWidth(context, keybindingX - (textLength / 2), keybindingY, textLength, iconVAnchor);
                HudKeyVisuals.buttonTrailing.draw(context, keybindingX + (textLength / 2), keybindingY, Drawable.Anchor.LEADING, iconVAnchor);

                var textHeight = font.lineHeight + 1; // +1 for shadow
                var textY = keybindingY;
                switch (iconVAnchor) {
                    case LEADING -> textY = textY;
                    case TRAILING -> textY -= textHeight;
                    case CENTER -> textY -= (textHeight / 2 - 1);
                }
                context.pose().pushMatrix();
                context.drawCenteredString(font, label, keybindingX, textY, 0xFFFFFFFF);
                context.pose().popMatrix();
            }
        }
    }

    private record ViewModel(List<Element> elements, String label, @Nullable Drawable.Component drawable) {
        record Element(int color, float full) { }

        static ViewModel create(RollManager.CooldownInfo info, float tickDelta) {
            var config = CombatRollClient.config;
            var elements = new ArrayList<ViewModel.Element>();
            for (int i = 0; i < info.maxRolls(); ++i) {
                var color = config.hudArrowColor;
                float full = 0;
                if ((i == info.availableRolls())) {
                    full = ((float) info.elapsed()) / ((float) info.total());
                    full = Math.min(full, 1F);

                    if (config.playCooldownFlash) {
                        var missingTicks = info.total() - info.elapsed();
                        var sparkleTicks = 2;
                        if (missingTicks <= sparkleTicks) {
                            float sparkle = ((sparkleTicks / 2) - ((missingTicks - 1 + (1F - tickDelta)) / (sparkleTicks)));
                            float red = ((float) ((color >> 16) & 0xFF)) / 255F;
                            float green = ((float) ((color >> 8) & 0xFF)) / 255F;
                            float blue = ((float) (color & 0xFF)) / 255F;
                            int redBits = (int) (mixNumberFloat(red, 1, sparkle) * 255F);
                            int greenBits = (int) (mixNumberFloat(green, 1, sparkle) * 255F);
                            int blueBits = (int) (mixNumberFloat(blue, 1, sparkle) * 255F);
                            color = redBits;
                            color = (color << 8) + greenBits;
                            color = (color << 8) + blueBits;
                        }
                    }
                }
                if (i < (info.availableRolls())) {
                    full = 1;
                }
                elements.add(new ViewModel.Element(color, full));
            }


            var keybinding = Keybindings.roll;
            var key = ((CombatRollKeybindingAccessor) keybinding).combatroll$getKey().getName();
            var drawable = HudKeyVisuals.custom.get(key);
            var label = keybinding.getTranslatedKeyMessage()
                    .getString()
                    .toUpperCase(Locale.US);
            label = acronym(label, 3);

            return new ViewModel(elements, label, drawable);
        }

        static ViewModel mock() {
            var config = CombatRollClient.config;
            var color = config.hudArrowColor;
            return new ViewModel(
                    List.of(
                            new ViewModel.Element(color, 1),
                            new ViewModel.Element(color, 0.5F),
                            new ViewModel.Element(color, 0)
                    ),
                    "R",
                    null
            );
        }

        private static float mixNumberFloat(float a, float b, float bias) {
            return a + (b - a) * bias;
        }

        private static String acronym(String phrase, int maxLength) {
            StringBuilder result = new StringBuilder();
            for (String token : phrase.split("\\s+")) {
                result.append(token.toUpperCase().charAt(0));
            }
            var resultString = result.toString();
            // Make the result at most 3 characters long
            if (resultString.length() > maxLength) {
                resultString = resultString.substring(0, maxLength);
            }
            return result.toString();
        }
    }
}
