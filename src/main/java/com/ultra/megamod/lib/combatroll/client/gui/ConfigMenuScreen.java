package com.ultra.megamod.lib.combatroll.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigMenuScreen extends Screen {
    private Screen previous;

    public ConfigMenuScreen(Screen parent) {
        super(Component.translatable("gui.megamod.combatroll.config_menu"));
        this.previous = parent;
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
                Button.builder(Component.translatable("gui.megamod.combatroll.settings"), button -> {
                            // No AutoConfig in NeoForge port - settings are static defaults
                        })
                        .pos(buttonCenterX, buttonCenterY)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
        addRenderableWidget(
                Button.builder(Component.translatable("gui.megamod.combatroll.hud"), button -> {
                            minecraft.setScreen(new HudConfigScreen(this));
                        })
                        .pos(buttonCenterX, buttonCenterY + 30)
                        .size(buttonWidth, buttonHeight)
                        .build()
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(previous);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }
}
