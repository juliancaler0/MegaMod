package com.ultra.megamod.feature.furniture;

import net.minecraft.client.Minecraft;

/**
 * Client-only helper to open the quest board screen.
 * Separated to avoid loading client classes on the server.
 */
public class QuestBoardClientHelper {
    public static void openScreen() {
        Minecraft.getInstance().setScreen(new QuestBoardScreen());
    }
}
