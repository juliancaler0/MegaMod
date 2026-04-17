package com.ultra.megamod.lib.owo.serialization;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import net.minecraft.core.component.DataComponentType;

public interface OwoDataComponentTypeBuilder<T> {
    default DataComponentType.Builder<T> endec(Endec<T> endec) {
        return this.endec(endec, SerializationContext.empty());
    }

    default DataComponentType.Builder<T> endec(Endec<T> endec, SerializationContext assumedContext) {
        return ((DataComponentType.Builder<T>) this)
            .persistent(CodecUtils.toCodec(endec, assumedContext))
            .networkSynchronized(CodecUtils.toPacketCodec(endec));
    }
}
