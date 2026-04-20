package com.ultra.megamod.lib.accessories.neoforge.client;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.client.AccessoriesClient;
import com.ultra.megamod.lib.accessories.client.DrawUtils;
import com.ultra.megamod.mixin.accessories.client.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Injects the "open accessories screen" button into the player inventory and creative
 * inventory screens, replacing source's owo-lib Layers.add(...) machinery which isn't
 * ported. Offsets and textures match source (see AccessoriesConfigModel / AccessoriesClient).
 */
public final class AccessoriesInventoryButton {

    private AccessoriesInventoryButton() {}

    public static void onInitPost(ScreenEvent.Init.Post event) {
        var screen = event.getScreen();
        if (!(screen instanceof AbstractContainerScreen<?> container)) return;

        var accessor = (AbstractContainerScreenAccessor) container;
        int left = accessor.accessories$leftPos();
        int top = accessor.accessories$topPos();

        int x, y;
        if (screen instanceof InventoryScreen) {
            x = left + 66;
            y = top + 8;
        } else if (screen instanceof CreativeModeInventoryScreen) {
            x = left + 96;
            y = top + 6;
        } else {
            return;
        }

        event.addListener(new OpenAccessoriesButton(x, y));
    }

    private static final class OpenAccessoriesButton extends AbstractButton {
        private static final Identifier ICON = Identifier.fromNamespaceAndPath(
                Accessories.MODID, "textures/gui/accessories_open_icon.png");
        private static final Identifier ICON_HOVERED = Identifier.fromNamespaceAndPath(
                Accessories.MODID, "textures/gui/accessories_open_icon_hovered.png");

        OpenAccessoriesButton(int x, int y) {
            super(x, y, 9, 9, Component.translatable(Accessories.MODID + ".open.screen"));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            AccessoriesClient.attemptToOpenScreenFromEntity(player);
        }

        @Override
        protected void renderContents(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            DrawUtils.blit(graphics, isHovered() ? ICON_HOVERED : ICON,
                    this.getX(), this.getY(), 0, 0, 8, 8, 8, 8);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
