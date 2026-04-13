package com.ultra.megamod.feature.worldedit;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Admin-gating used by every WorldEdit command and tool. A command
 * invocation from a non-admin returns false and sends a terse message.
 */
public final class WorldEditPermissions {

    private WorldEditPermissions() {}

    public static boolean isAdmin(ServerPlayer player) {
        return player != null && AdminSystem.isAdmin(player);
    }

    public static boolean requireAdmin(CommandSourceStack src) {
        if (!(src.getEntity() instanceof ServerPlayer sp)) return false;
        if (!isAdmin(sp)) {
            src.sendFailure(Component.literal("WorldEdit is admin-only."));
            return false;
        }
        return true;
    }

    public static boolean requireAdmin(ServerPlayer sp) {
        if (!isAdmin(sp)) {
            sp.sendSystemMessage(Component.literal("WorldEdit is admin-only."));
            return false;
        }
        return true;
    }
}
