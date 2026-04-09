/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.commands.CommandSource
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.permissions.PermissionSet
 */
package com.ultra.megamod.feature.computer.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;

public class AdminSystem {
    public static final Set<String> ADMIN_USERNAMES = Set.of("NeverNotch", "Dev");
    private static final Set<UUID> MUTED_PLAYERS = new HashSet<UUID>();

    public static boolean isMuted(UUID playerId) {
        return MUTED_PLAYERS.contains(playerId);
    }

    public static void mute(UUID playerId) {
        MUTED_PLAYERS.add(playerId);
    }

    public static void unmute(UUID playerId) {
        MUTED_PLAYERS.remove(playerId);
    }

    public static boolean isAdmin(ServerPlayer player) {
        return ADMIN_USERNAMES.contains(player.getGameProfile().name());
    }

    public static boolean isAdmin(String username) {
        return ADMIN_USERNAMES.contains(username);
    }

    public static String executeCommand(ServerPlayer player, String command) {
        if (!AdminSystem.isAdmin(player)) {
            return "Access denied.";
        }
        MinecraftServer server = player.level().getServer();
        final ArrayList<String> output = new ArrayList<String>();
        CommandSource capturingSource = new CommandSource(){

            public void sendSystemMessage(Component message) {
                output.add(message.getString());
            }

            public boolean acceptsSuccess() {
                return true;
            }

            public boolean acceptsFailure() {
                return true;
            }

            public boolean shouldInformAdmins() {
                return false;
            }
        };
        CommandSourceStack source = player.createCommandSourceStack().withSource(capturingSource).withPermission(PermissionSet.ALL_PERMISSIONS);
        try {
            server.getCommands().performPrefixedCommand(source, command);
        }
        catch (Exception e) {
            output.add("Error: " + e.getMessage());
        }
        if (output.isEmpty()) {
            return "Command executed.";
        }
        return String.join("\n", output);
    }
}

