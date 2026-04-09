package com.ultra.megamod.feature.backpacks.client;

import com.ultra.megamod.feature.backpacks.BackpackItem;
import com.ultra.megamod.feature.backpacks.network.OpenBackpackPayload;
import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "megamod", value = {Dist.CLIENT})
public class BackpackKeybind {

    public static KeyMapping OPEN_BACKPACK;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_BACKPACK = new KeyMapping("key.megamod.open_backpack", GLFW.GLFW_KEY_B, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(OPEN_BACKPACK);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (OPEN_BACKPACK == null || !OPEN_BACKPACK.consumeClick()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        Player player = mc.player;

        // First check if player has an equipped backpack (worn on back)
        if (com.ultra.megamod.feature.backpacks.BackpackWearableManager.isClientWearing(player.getId())) {
            // Send -1 to signal "open equipped backpack"
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new OpenBackpackPayload(-1),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return;
        }

        // Otherwise scan inventory for a backpack
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new OpenBackpackPayload(i),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                return;
            }
        }
    }
}
