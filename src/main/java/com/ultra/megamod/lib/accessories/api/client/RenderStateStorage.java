package com.ultra.megamod.lib.accessories.api.client;

import net.minecraft.util.context.ContextKey;

public interface RenderStateStorage {

    default <T> T getRenderData(ContextKey<T> key) {
        throw new IllegalStateException("Interface Stub method not implemented!");
    }

    default <T> boolean hasRenderData(ContextKey<T> key)  {
        throw new IllegalStateException("Interface Stub method not implemented!");
    }

    default <T> void setRenderData(ContextKey<T> key, T data)  {
        throw new IllegalStateException("Interface Stub method not implemented!");
    }

    default void clearExtraData() {
        throw new IllegalStateException("Interface Stub method not implemented!");
    }
}
