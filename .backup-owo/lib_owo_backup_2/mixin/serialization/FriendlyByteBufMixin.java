package com.ultra.megamod.lib.owo.mixin.serialization;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.format.bytebuf.ByteBufDeserializer;
import com.ultra.megamod.lib.accessories.endec.adapter.format.bytebuf.ByteBufSerializer;
import com.ultra.megamod.lib.accessories.endec.adapter.util.EndecBuffer;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"DataFlowIssue"})
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin implements EndecBuffer {
    @Override
    public <T> void write(SerializationContext ctx, Endec<T> endec, T value) {
        endec.encodeFully(ctx, () -> ByteBufSerializer.of((FriendlyByteBuf) (Object) this), value);
    }

    @Override
    public <T> T read(SerializationContext ctx, Endec<T> endec) {
        return endec.decodeFully(ctx, ByteBufDeserializer::of, (FriendlyByteBuf) (Object) this);
    }
}
