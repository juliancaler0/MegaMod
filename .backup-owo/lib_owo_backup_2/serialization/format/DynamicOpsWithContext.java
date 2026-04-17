package com.ultra.megamod.lib.owo.serialization.format;

import com.mojang.serialization.DynamicOps;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import net.minecraft.resources.DelegatingOps;

public class DynamicOpsWithContext<T> extends DelegatingOps<T> implements ContextHolder {

    private final SerializationContext capturedContext;

    protected DynamicOpsWithContext(SerializationContext capturedContext, DynamicOps<T> delegate) {
        super(delegate);

        this.capturedContext = capturedContext;
    }

    public static <T> DynamicOpsWithContext<T> of(SerializationContext context, DynamicOps<T> delegate) {
        return new DynamicOpsWithContext<>(context, delegate);
    }

    public static <T> DynamicOpsWithContext<T> ofEmptyContext(DynamicOps<T> delegate) {
        return new DynamicOpsWithContext<>(SerializationContext.empty(), delegate);
    }

    @Override
    public SerializationContext capturedContext() {
        return this.capturedContext;
    }
}
