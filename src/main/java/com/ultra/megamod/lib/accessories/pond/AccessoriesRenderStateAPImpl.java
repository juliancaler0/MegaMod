package com.ultra.megamod.lib.accessories.pond;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;

public interface AccessoriesRenderStateAPImpl extends AccessoriesRenderStateAPI {

    Reference2ObjectMap<ContextKey<?>, Object> contextKeyToContextData();

    @Override
    default <T> T getRenderData(ContextKey<T> key) {
        // Check NeoForge's BaseRenderState extensions first
        if (this instanceof BaseRenderState baseState) {
            try {
                var data = baseState.getRenderData(key);
                if (data != null) return data;
            } catch (Exception ignored) {}
        }
        return (T) this.contextKeyToContextData().get(key);
    }

    @Override
    default <T> void setRenderData(ContextKey<T> key, T data) {
        var map = this.contextKeyToContextData();

        if (data == null) {
            map.remove(key);
        } else {
            map.put(key, data);
        }
    }

    @Override
    default <T> boolean hasRenderData(ContextKey<T> key) {
        // Check NeoForge's BaseRenderState extensions first
        if (this instanceof BaseRenderState baseState) {
            try {
                if (baseState.getRenderData(key) != null) return true;
            } catch (Exception ignored) {}
        }
        return this.contextKeyToContextData().containsKey(key);
    }

    @Override
    default void clearExtraData() {
        // Also clear NeoForge's BaseRenderState data
        if (this instanceof BaseRenderState baseState) {
            try {
                baseState.resetRenderData();
            } catch (Exception ignored) {}
        }
        this.contextKeyToContextData().clear();
    }
}
