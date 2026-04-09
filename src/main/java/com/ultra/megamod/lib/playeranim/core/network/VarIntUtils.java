package com.ultra.megamod.lib.playeranim.core.network;

import io.netty.buffer.ByteBuf;

/**
 * Stub for VarInt encoding/decoding utilities.
 */
public final class VarIntUtils {
    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public static int readVarInt(ByteBuf buf) {
        int result = 0;
        int shift = 0;
        int b;
        do {
            b = buf.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    private VarIntUtils() {}
}
