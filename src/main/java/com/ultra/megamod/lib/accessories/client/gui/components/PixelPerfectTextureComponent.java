package com.ultra.megamod.lib.accessories.client.gui.components;
import com.ultra.megamod.lib.accessories.owo.ui.core.*;

import com.ultra.megamod.lib.accessories.owo.ui.base.BaseUIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class PixelPerfectTextureComponent extends BaseUIComponent {

    private final Identifier texture;

    public PixelPerfectTextureComponent(Identifier texture, int textureWidth, int textureHeight, int scale) {
        this(texture, Sizing.fixed(textureWidth * scale), Sizing.fixed(textureHeight * scale));
    }

    public PixelPerfectTextureComponent(Identifier texture, Sizing horizontalSizing, Sizing verticalSizing) {
        super();

        this.texture = texture;

        if(horizontalSizing.isContent()) throw new IllegalStateException("HorizontalSizing of PixelPerfectTextureComponent was found to be Content Sizing, which is not allowed!");
        if(verticalSizing.isContent()) throw new IllegalStateException("VerticalSizing of PixelPerfectTextureComponent was found to be Content Sizing, which is not allowed!");

        this.horizontalSizing(horizontalSizing);
        this.verticalSizing(verticalSizing);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        drawPixelPerfectTextureQuad(context, texture, this.x(), this.y(), this.width(), this.height());
    }

    public static void drawPixelPerfectTextureQuad(OwoUIGraphics context, Identifier texture, int x1, int y1, int width, int height) {
        // Simplified rendering - delegate to DrawUtils
        com.ultra.megamod.lib.accessories.client.DrawUtils.blit(context, texture, x1, y1, width, height);
    }
}
