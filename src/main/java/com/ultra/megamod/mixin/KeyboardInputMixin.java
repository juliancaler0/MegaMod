package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into KeyboardInput to implement:
 * - GUIMove: allow WASD movement input while a GUI screen is open.
 *
 * In 1.21.11, KeyboardInput extends ClientInput and overrides tick().
 * The tick() method reads keybind states and sets the keyPresses field.
 * When a screen is open, vanilla returns empty input. We override the
 * keyPresses field after tick() completes to re-read actual key states.
 */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    /**
     * GUIMove: After tick() completes, if a screen is open and GUIMove
     * is enabled, override keyPresses with actual key states.
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void megamod$allowGuiMovement(CallbackInfo ci) {
        if (!AdminModuleState.guiMoveEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) return; // No screen open, vanilla input is fine

        // Re-read keybind states and override the keyPresses field
        boolean forward = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();
        boolean jump = mc.options.keyJump.isDown();
        boolean shift = mc.options.keyShift.isDown();
        boolean sprint = mc.options.keySprint.isDown();

        // Only override if any key is actually pressed
        if (forward || backward || left || right || jump || shift || sprint) {
            ((ClientInput) (Object) this).keyPresses = new Input(
                    forward, backward, left, right, jump, shift, sprint
            );
        }
    }
}
