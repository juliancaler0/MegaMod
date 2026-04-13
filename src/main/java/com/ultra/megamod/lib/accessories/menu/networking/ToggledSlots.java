package com.ultra.megamod.lib.accessories.menu.networking;

import com.ultra.megamod.lib.accessories.client.gui.OwoSlotExtension;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;

public record ToggledSlots(Map<Integer, Boolean> changedSlotStates) {

    public static void initMenu(AbstractContainerMenu menu) {
        // OWO menu messaging not ported - slot toggling is handled elsewhere
    }
}
