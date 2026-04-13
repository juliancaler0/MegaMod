package com.ultra.megamod.lib.accessories.endec.adapter.format.bytebuf;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Adapter for io.wispforest.endec.format.bytebuf.ByteBufDeserializer.
 * Stub - actual deserialization uses StreamCodec.
 */
public final class ByteBufDeserializer {
    private final FriendlyByteBuf buf;

    private ByteBufDeserializer(FriendlyByteBuf buf) {
        this.buf = buf;
    }

    public static ByteBufDeserializer of(FriendlyByteBuf buf) {
        return new ByteBufDeserializer(buf);
    }

    public static ByteBufDeserializer of(ByteBuf buf) {
        return new ByteBufDeserializer(new FriendlyByteBuf(buf));
    }

    public FriendlyByteBuf buf() {
        return buf;
    }
}
