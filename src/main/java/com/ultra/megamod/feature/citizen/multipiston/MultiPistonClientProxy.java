package com.ultra.megamod.feature.citizen.multipiston;

import net.minecraft.client.Minecraft;

/**
 * Client-only helper that wires the real screen-opening implementation onto
 * {@link MultiPistonBlock#OPEN_CONFIG_SCREEN}. Loaded from
 * {@code MegaModClient} so this class's references to
 * {@code net.minecraft.client.Minecraft} and {@code MultiPistonScreen} are
 * never reached on the dedicated server.
 */
public final class MultiPistonClientProxy {
    private MultiPistonClientProxy() {}

    public static void init() {
        MultiPistonBlock.OPEN_CONFIG_SCREEN = pos ->
                Minecraft.getInstance().setScreen(new MultiPistonScreen(pos));
    }
}
