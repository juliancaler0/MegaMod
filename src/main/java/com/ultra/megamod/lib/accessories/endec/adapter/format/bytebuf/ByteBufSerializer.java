package com.ultra.megamod.lib.accessories.endec.adapter.format.bytebuf;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Adapter for io.wispforest.endec.format.bytebuf.ByteBufSerializer.
 * Stub - actual serialization uses StreamCodec.
 */
public final class ByteBufSerializer {
    private final FriendlyByteBuf buf;

    private ByteBufSerializer(FriendlyByteBuf buf) {
        this.buf = buf;
    }

    public static ByteBufSerializer of(FriendlyByteBuf buf) {
        return new ByteBufSerializer(buf);
    }

    public static ByteBufSerializer of(ByteBuf buf) {
        return new ByteBufSerializer(new FriendlyByteBuf(buf));
    }

    public FriendlyByteBuf buf() {
        return buf;
    }
}
