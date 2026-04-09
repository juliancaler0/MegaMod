package com.ultra.megamod.feature.citizen.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client-side state for "click a block to assign" mode.
 * When a player clicks Set Bed / Set Chest / Set Work Pos / Set Upkeep Chest,
 * the interaction screen closes and this mode activates. The next right-click
 * on a valid block assigns the position.
 */
public class CitizenAssignmentMode {

    public enum AssignType {
        BED("bed", "Right-click a bed to assign it."),
        CHEST("chest", "Right-click a chest to assign it."),
        WORK("work", "Right-click a block to set the work area."),
        UPKEEP_CHEST("upkeep_chest", "Right-click a chest to assign as upkeep chest."),
        TOWN_CHEST("town_chest", "Right-click a Town Chest to assign it.");

        public final String actionId;
        public final String prompt;

        AssignType(String actionId, String prompt) {
            this.actionId = actionId;
            this.prompt = prompt;
        }
    }

    private static AssignType pendingType = null;
    private static int pendingEntityId = -1;
    private static long startTick = 0;
    private static final long TIMEOUT_TICKS = 200; // 10 seconds

    public static void enter(AssignType type, int entityId) {
        pendingType = type;
        pendingEntityId = entityId;
        var mc = Minecraft.getInstance();
        startTick = mc.level != null ? mc.level.getGameTime() : 0;

        // Show action bar prompt
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("\u00A7e\u00A7l" + type.prompt + " \u00A77(Right-click to cancel)"), true);
        }
    }

    public static boolean isActive() {
        if (pendingType == null) return false;

        // Check timeout
        var mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getGameTime() - startTick > TIMEOUT_TICKS) {
            cancel();
            return false;
        }
        return true;
    }

    public static AssignType getType() {
        return pendingType;
    }

    public static int getEntityId() {
        return pendingEntityId;
    }

    public static void clear() {
        pendingType = null;
        pendingEntityId = -1;
    }

    public static void cancel() {
        if (pendingType != null) {
            var mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("\u00A77Assignment cancelled."), true);
            }
        }
        clear();
    }
}
