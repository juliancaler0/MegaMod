package com.ultra.megamod.lib.spellengine;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;

public class PlatformClient {
    public interface Util {
        // These are likely necessary due to some mapping differences
        void sendVanillaPacket_C2S(LocalPlayer player, Packet<?> packet);
    }

    /**
     * NeoForge 1.21.11 implementation. Source SpellEngine sends raw vanilla packets
     * (e.g. {@code ServerboundMovePlayerPacket}) every tick during a beam cast so the
     * server's view of the caster's position stays accurate for beam targeting. NeoForge
     * exposes this directly via {@code player.connection.send(packet)}.
     */
    private static final Util IMPL = (player, packet) -> {
        if (player != null && player.connection != null) {
            player.connection.send(packet);
        }
    };

    public static Util util() {
        return IMPL;
    }
}
