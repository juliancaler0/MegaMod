package com.ultra.megamod.feature.adminmodules.client;

import com.ultra.megamod.feature.adminmodules.network.PortableCraftPayload;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Admin-only portable crafting. Uses direct key detection (C key) instead of
 * a registered KeyMapping so it doesn't appear in the keybinds menu for normal players.
 */
@EventBusSubscriber(modid = "megamod", value = {Dist.CLIENT})
public class PortableCraftKeybind {
    private static int selectionState = 0;
    private static final String[] WORKSTATIONS = {"crafting_table", "smithing_table", "anvil"};
    private static boolean wasPressed = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!AdminSystem.isAdmin(mc.player.getGameProfile().name())) return;

        boolean pressed = GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;

        // Only trigger on key-down edge (not while held)
        if (pressed && !wasPressed) {
            String workstation = WORKSTATIONS[selectionState];
            selectionState = (selectionState + 1) % WORKSTATIONS.length;

            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new PortableCraftPayload(workstation),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
        }
        wasPressed = pressed;
    }
}
