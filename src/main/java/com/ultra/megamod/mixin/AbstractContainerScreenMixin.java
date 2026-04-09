package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into AbstractContainerScreen to implement:
 * - ContainerButtons: add steal (take all) and dump (give all) functionality
 *   when a container screen is open and the module is enabled.
 *
 * When the flag is active, pressing 'T' steals all items from the container
 * into the player inventory, and pressing 'G' dumps all player inventory
 * items into the container. Uses quick-move (shift-click) operations.
 *
 * In 1.21.11, keyPressed takes a KeyEvent record (key, scancode, modifiers).
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Shadow protected AbstractContainerMenu menu;

    /**
     * Intercept render to display hint text when container buttons are enabled.
     * Shows "[T] Steal | [G] Dump" at the top of the container screen.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void megamod$renderContainerHints(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (AdminModuleState.containerButtonsEnabled) {
            AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
            graphics.drawCenteredString(
                net.minecraft.client.Minecraft.getInstance().font,
                "\u00a7e[T] \u00a7fSteal All  \u00a7e[G] \u00a7fDump All",
                self.width / 2, 4, 0xFFFFFF
            );
        }
    }

    /**
     * Intercept key presses to handle T (steal) and G (dump) keys.
     * T = quick-move all container slots to player inventory.
     * G = quick-move all player inventory slots to container.
     *
     * In 1.21.11, keyPressed takes a KeyEvent record with key(), scancode(), modifiers().
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void megamod$onKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (!AdminModuleState.containerButtonsEnabled) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        int keyCode = keyEvent.key();

        // T key = 84, steal all from container
        if (keyCode == 84) {
            for (Slot slot : menu.slots) {
                // Container slots are typically the ones NOT in player inventory
                if (slot.hasItem() && !(slot.container instanceof net.minecraft.world.entity.player.Inventory)) {
                    mc.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
                }
            }
            cir.setReturnValue(true);
        }
        // G key = 71, dump all from player inventory to container
        else if (keyCode == 71) {
            for (Slot slot : menu.slots) {
                // Player inventory slots
                if (slot.hasItem() && slot.container instanceof net.minecraft.world.entity.player.Inventory) {
                    mc.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
                }
            }
            cir.setReturnValue(true);
        }
    }
}
