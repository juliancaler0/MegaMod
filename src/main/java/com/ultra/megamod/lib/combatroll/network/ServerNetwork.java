package com.ultra.megamod.lib.combatroll.network;

import com.google.common.collect.Iterables;
import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.Platform;
import com.ultra.megamod.lib.combatroll.api.RollInvulnerable;
import com.ultra.megamod.lib.combatroll.api.event.Event;
import com.ultra.megamod.lib.combatroll.api.event.ServerSideRollEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetwork {
    public static String configSerialized = "";

    public static void initializeHandlers() {
        configSerialized = Packets.ConfigSync.serialize(CombatRollMod.config);
    }

    public static void handleRollPublish(Packets.RollPublish packet, MinecraftServer server, ServerPlayer player) {
        ServerLevel world = Iterables.tryFind(server.getAllLevels(), (element) -> element == player.level())
                .orNull();
        final var velocity = packet.velocity();
        final var forwardPacket = new Packets.RollAnimation(player.getId(), packet.visuals(), packet.velocity());
        Platform.tracking(player).forEach(serverPlayer -> {
            try {
                if (serverPlayer.getId() != player.getId() && Platform.networkS2C_CanSend(serverPlayer, Packets.RollAnimation.PACKET_ID)) {
                    Platform.networkS2C_Send(serverPlayer, forwardPacket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        world.getServer().executeIfPossible(() -> {
            ((RollInvulnerable)player).setRollInvulnerableTicks(CombatRollMod.config.invulnerable_ticks_upon_roll);
            player.causeFoodExhaustion(CombatRollMod.config.exhaust_on_roll);
            var proxy = (Event.Proxy<ServerSideRollEvents.PlayerStartRolling>)ServerSideRollEvents.PLAYER_START_ROLLING;
            proxy.handlers.forEach(handler -> { handler.onPlayerStartedRolling(player, velocity);});
        });
    }
}
