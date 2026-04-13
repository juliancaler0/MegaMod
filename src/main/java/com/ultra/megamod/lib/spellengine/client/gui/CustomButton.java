package com.ultra.megamod.lib.spellengine.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CustomButton extends AbstractButton {

    private static final Identifier BUTTONS_TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/gui/buttons.png");
    private int u;
    private int v;
    private int stateOffsetY;
    private final OnPress onPress;

    @FunctionalInterface
    public interface OnPress {
        void onPress(CustomButton button);
    }

    public CustomButton(int x, int y, Type type, OnPress onPress) {
        super(x, y, type.width(), type.height(), Component.empty());
        this.u = type.u();
        this.v = type.v();
        this.stateOffsetY = type.stateOffsetY();
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.onPress.onPress(this);
    }

    public enum Type {
        SMALL_UP,
        SMALL_DOWN
        ;

        private Drawable.DrawRect rect() {
            return switch (this) {
                case SMALL_UP -> new Drawable.DrawRect(0, 0, 11, 7);
                case SMALL_DOWN -> new Drawable.DrawRect(16, 0, 11, 7);
            };
        }

        public int stateOffsetY() {
            return switch (this) {
                case SMALL_UP, SMALL_DOWN -> 16;
            };
        }

        public int width() {
            return rect().width();
        }

        public int height() {
            return rect().height();
        }

        public int u() {
            return rect().u();
        }

        public int v() {
            return rect().v();
        }
    }

    private int getTextureY() {
        int i = 0;
        if (!this.active) {
            i = 2;
        } else if (this.isHovered()) {
            i = 1;
        }
        return v + i * stateOffsetY;
    }

    @Override
    protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, this.getX(), this.getY(),
                (float) this.u, (float) this.getTextureY(), this.getWidth(), this.getHeight(), 256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
