package com.ultra.megamod.feature.sorting.client;

import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import com.ultra.megamod.feature.sorting.SortAlgorithm;
import com.ultra.megamod.feature.sorting.network.SortActionPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "megamod", value = {Dist.CLIENT})
public class SortKeybind {
    public static KeyMapping SORT_KEY;
    private static int algorithmIndex = 0;
    private static final SortAlgorithm[] ALGORITHMS = SortAlgorithm.values();

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        SORT_KEY = new KeyMapping("key.megamod.sort_inventory", GLFW.GLFW_KEY_O, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(SORT_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (SORT_KEY == null || !SORT_KEY.consumeClick()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // Only sort when a container screen is open, or player inventory
        if (mc.screen == null) return;

        String sortType = ALGORITHMS[algorithmIndex].name();
        // Cycle algorithm for next press
        algorithmIndex = (algorithmIndex + 1) % ALGORITHMS.length;

        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new SortActionPayload(sortType),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }
}
