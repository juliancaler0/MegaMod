package com.ultra.megamod.lib.spellengine;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

/**
 * Platform abstraction for SpellEngine, simplified for NeoForge-only.
 */
public class Platform {
    public static final boolean Fabric = false;
    public static final boolean Forge = false;
    public static final boolean NeoForge = true;

    public enum Type { FABRIC, FORGE, NEOFORGE }

    protected static Type getPlatformType() {
        return Type.NEOFORGE;
    }

    public interface Util {
        boolean isModLoaded(String modid);
        void awakeSlotModCompat();
        void sendVanillaPacket_S2C(ServerPlayer player, Packet<?> packet);
    }

    private static final Util UTIL = new Util() {
        @Override
        public boolean isModLoaded(String modid) {
            return ModList.get().isLoaded(modid);
        }

        @Override
        public void awakeSlotModCompat() {
            // No-op for NeoForge
        }

        @Override
        public void sendVanillaPacket_S2C(ServerPlayer player, Packet<?> packet) {
            player.connection.send(packet);
        }
    };

    public static Util util() {
        return UTIL;
    }
}
