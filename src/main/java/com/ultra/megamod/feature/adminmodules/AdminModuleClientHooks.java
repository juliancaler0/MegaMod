package com.ultra.megamod.feature.adminmodules;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.baritone.screen.BotPathRenderer;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * NeoForge client-side event handlers for admin modules that can be implemented
 * via events rather than mixins. These read from {@link AdminModuleState}.
 *
 * Handles: Zoom, CustomFOV, FreeLook (camera angles), HandView, Bot ESC-cancel.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class AdminModuleClientHooks {

    // ── FOV: Zoom + CustomFOV ──────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (AdminModuleState.zoomEnabled) {
            float baseFov = event.getFOV();
            event.setFOV(baseFov / AdminModuleState.zoomFactor);
        } else if (AdminModuleState.customFovEnabled) {
            event.setFOV((float) AdminModuleState.customFovValue);
        }
    }

    // ── FreeLook: override camera angles ───────────────────────────────────

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (AdminModuleState.freeLookEnabled && AdminModuleState.freeLookActive) {
            event.setYaw(AdminModuleState.freeLookYaw);
            event.setPitch(AdminModuleState.freeLookPitch);
        }
    }

    // ── FreeLook: track mouse input for free look on client tick ────────────

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!AdminModuleState.freeLookEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // FreeLook: while the pick block key (middle mouse by default) is held,
        // decouple camera from player head direction
        boolean middleDown = mc.options.keyPickItem.isDown();

        if (middleDown) {
            if (!AdminModuleState.freeLookActive) {
                // Just started free looking -- save current angles
                AdminModuleState.freeLookActive = true;
                AdminModuleState.savedPlayerYaw = mc.player.getYRot();
                AdminModuleState.savedPlayerPitch = mc.player.getXRot();
                AdminModuleState.freeLookYaw = mc.player.getYRot();
                AdminModuleState.freeLookPitch = mc.player.getXRot();
            }
            // Update freelook angles from current player rotation (mouse moves these)
            AdminModuleState.freeLookYaw = mc.player.getYRot();
            AdminModuleState.freeLookPitch = mc.player.getXRot();

            // Restore player head to saved position so movement stays consistent
            mc.player.setYRot(AdminModuleState.savedPlayerYaw);
            mc.player.setXRot(AdminModuleState.savedPlayerPitch);
            mc.player.yRotO = AdminModuleState.savedPlayerYaw;
            mc.player.xRotO = AdminModuleState.savedPlayerPitch;
            mc.player.setYHeadRot(AdminModuleState.savedPlayerYaw);
        } else {
            if (AdminModuleState.freeLookActive) {
                // Released free look -- snap camera back to player head
                AdminModuleState.freeLookActive = false;
            }
        }
    }

    // ── Bot ESC-cancel: pressing ESC while no screen is open stops the bot ──

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ESC key (keyCode 256) — cancel bot when no screen is open
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
            if (mc.screen == null && BotPathRenderer.isBotActive()) {
                // Send stop command to server
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("bot_group_command", "{\"cmd\":\"stop\"}"),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                BotPathRenderer.clear();
            }
        }

        // Grave accent / tilde key (`) — alternative cancel that always works
        if (event.getKey() == GLFW.GLFW_KEY_GRAVE_ACCENT) {
            if (mc.screen == null && BotPathRenderer.isBotActive()) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("bot_group_command", "{\"cmd\":\"stop\"}"),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                BotPathRenderer.clear();
            }
        }
    }

    // ── HandView: modify hand rendering transform ──────────────────────────

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (!AdminModuleState.handViewEnabled) return;
        // Apply scale and offset to the hand rendering pose stack
        var poseStack = event.getPoseStack();
        poseStack.translate(AdminModuleState.handViewX, AdminModuleState.handViewY, 0.0);
        float s = AdminModuleState.handViewScale;
        poseStack.scale(s, s, s);
    }
}
