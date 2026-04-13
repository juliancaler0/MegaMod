package com.ultra.megamod.lib.spellengine;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;

public class PlatformClient {
    public interface Util {
        // These are likely necessary due to some mapping differences
        void sendVanillaPacket_C2S(LocalPlayer player, Packet<?> packet);
    }

    
    public static Util util() {
        throw new AssertionError();
    }
}
