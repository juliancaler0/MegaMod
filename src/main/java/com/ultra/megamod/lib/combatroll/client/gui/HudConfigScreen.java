package com.ultra.megamod.lib.combatroll.client.gui;

import com.ultra.megamod.lib.combatroll.client.CombatRollClient;
import com.ultra.megamod.lib.combatroll.config.HudConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

public class HudConfigScreen extends Screen {
    private Screen previous;

    public HudConfigScreen(Screen previous) {
        super(Component.translatable("gui.megamod.combatroll.hud"));
        this.previous = previous;
    }

    @Override
    protected void init() {
        var buttonWidth = 120;
        var buttonHeight = 20;
        var buttonCenterX = (width / 2) - (buttonWidth / 2);
        var buttonCenterY = (height / 2) - (buttonHeight / 2);

        addRenderableWidget(
            Button.builder(Component.translatable("gui.megamod.combatroll.close"), button -> { onClose(); })
                .pos(buttonCenterX, buttonCenterY - 30)
                .size(buttonWidth, buttonHeight)
                .build()
        );
        addRenderableWidget(
            Button.builder(Component.translatable("gui.megamod.combatroll.corner"), button -> { nextOrigin(); })
                .pos(buttonCenterX, buttonCenterY)
                .size(buttonWidth, buttonHeight)
                .build()
        );
        addRenderableWidget(
            Button.builder(Component.translatable("gui.megamod.combatroll.reset"), button -> { reset(); })
                .pos(buttonCenterX, buttonCenterY + 30)
                .size(buttonWidth, buttonHeight)
                .build()
        );
    }

    @Override
    public void onClose() {
        this.save();
        this.minecraft.setScreen(previous);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        HudRenderHelper.render(context, delta);
    }

    public boolean mouseDragged(MouseButtonEvent e, double deltaX, double deltaY) {
        if (e.button() == 0) {
            var config = CombatRollClient.hudConfig;
            config.rollWidget.offset = new Vector2f(
                    (float) (config.rollWidget.offset.x + deltaX),
                    (float) (config.rollWidget.offset.y + deltaY));
        }
        return super.mouseDragged(e, deltaX, deltaY);
    }

    public static void nextOrigin() {
        var config = CombatRollClient.hudConfig;
        HudElement.Origin origin;
        try {
            origin = HudElement.Origin.values()[(config.rollWidget.origin.ordinal() + 1)];
            config.rollWidget = new HudElement(origin, origin.initialOffset());
        } catch (Exception e) {
            origin = HudElement.Origin.values()[0];
            config.rollWidget = new HudElement(origin, origin.initialOffset());
        }
    }

    public void save() {
        // Config is in-memory only for this port
    }

    public void reset() {
        var config = CombatRollClient.hudConfig;
        config.rollWidget = HudConfig.createDefaultRollWidget();
    }
}
