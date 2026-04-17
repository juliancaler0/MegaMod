package com.ultra.megamod.lib.owo.serialization.format;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;

/**
 * A common interface for parts of a serialization infrastructure
 * which provide an instance of {@link SerializationContext}. Primarily
 * used for attaching context to {@link com.mojang.serialization.DynamicOps}
 */
public interface ContextHolder {
    SerializationContext capturedContext();
}