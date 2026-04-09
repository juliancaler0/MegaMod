package com.ultra.megamod.feature.computer.admin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Admin vanish mode — invisible to all non-admin players, no join/leave messages.
 */
public class VanishManager {

    private static final Set<UUID> vanished = new HashSet<>();

    public static boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public static void toggle(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (vanished.contains(uuid)) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public static void vanish(ServerPlayer player) {
        UUID uuid = player.getUUID();
        vanished.add(uuid);

        // Hide from all non-admin players
        PlayerList playerList = player.level().getServer().getPlayerList();
        for (ServerPlayer other : playerList.getPlayers()) {
            if (other == player) continue;
            if (AdminSystem.isAdmin(other)) continue;
            other.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(uuid)));
        }

        // Make invisible
        player.setInvisible(true);

        player.sendSystemMessage(Component.literal("[Vanish] You are now invisible.").withStyle(ChatFormatting.GREEN));
    }

    public static void unvanish(ServerPlayer player) {
        UUID uuid = player.getUUID();
        vanished.remove(uuid);

        // Show to all players again
        PlayerList playerList = player.level().getServer().getPlayerList();
        for (ServerPlayer other : playerList.getPlayers()) {
            if (other == player) continue;
            other.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player)));
        }

        // Remove invisibility
        player.setInvisible(false);

        player.sendSystemMessage(Component.literal("[Vanish] You are now visible.").withStyle(ChatFormatting.YELLOW));
    }

    /**
     * Called when a new player joins — hide all vanished admins from them.
     */
    public static void onPlayerJoin(ServerPlayer joiner) {
        if (AdminSystem.isAdmin(joiner)) return; // Admins see everyone
        for (UUID vanishedUuid : vanished) {
            joiner.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(vanishedUuid)));
        }
    }

    /**
     * Called when a vanished player disconnects.
     */
    public static void onPlayerDisconnect(UUID uuid) {
        vanished.remove(uuid);
    }

    public static Set<UUID> getVanishedPlayers() {
        return Set.copyOf(vanished);
    }

    public static void reset() {
        vanished.clear();
    }
}
