package com.ultra.megamod.lib.playeranim.core.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Stub for protocol serialization utilities.
 */
public final class ProtocolUtils {
    public static void writeString(ByteBuf buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        VarIntUtils.writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readString(ByteBuf buf) {
        int len = VarIntUtils.readVarInt(buf);
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static <T> void writeList(ByteBuf buf, List<T> list, BiConsumer<T, ByteBuf> writer) {
        VarIntUtils.writeVarInt(buf, list.size());
        for (T item : list) {
            writer.accept(item, buf);
        }
    }

    public static <T> List<T> readList(ByteBuf buf, Function<ByteBuf, T> reader) {
        int count = VarIntUtils.readVarInt(buf);
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }

    private ProtocolUtils() {}
}
