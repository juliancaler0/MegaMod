package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.accessories.client.AccessoriesClient;
import com.ultra.megamod.lib.accessories.client.EntityTarget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Adds a small button on the vanilla inventory screen that opens the lib/accessories
 * menu. Serves as a reliable fallback when the H keybind is shadowed by another
 * mod, unbound in {@code options.txt}, or not yet rebound by the user.
 *
 * <p>The lib's OWO-based inventory-button injection is disabled in this port — this
 * is the NeoForge-native replacement.</p>
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public final class AccessoriesInventoryButton {

    private AccessoriesInventoryButton() {}

    @SubscribeEvent
    public static void onInventoryOpen(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen inv)) return;

        int x = inv.getGuiLeft() + 63;
        int y = inv.getGuiTop() + 8;

        Button button = Button.builder(Component.literal("\u2726"), btn -> openAccessoriesScreen())
                .bounds(x, y, 12, 12)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Accessories")))
                .build();
        event.addListener(button);
    }

    private static void openAccessoriesScreen() {
        AccessoriesClient.attemptToOpenScreen(EntityTarget.PLAYER);
    }
}
