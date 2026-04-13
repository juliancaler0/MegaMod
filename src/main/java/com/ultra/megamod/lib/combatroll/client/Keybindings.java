package com.ultra.megamod.lib.combatroll.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import net.minecraft.client.KeyMapping;

import java.util.List;

public class Keybindings {
    public static KeyMapping roll;
    public static List<KeyMapping> all;

    static {
        roll = new KeyMapping(
                "keybinds.megamod.combatroll.roll",
                InputConstants.KEY_LALT,
                AccessoryKeybind.MEGAMOD_CATEGORY);

        all = List.of(roll);
    }
}
