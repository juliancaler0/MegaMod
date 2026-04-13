package com.ultra.megamod.lib.etf.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import com.ultra.megamod.lib.etf.ETF;

/**
 * Generic LRU cache used for ETF's variant texture caches.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ETFLruCache<X, Y> extends Object2ObjectLinkedOpenHashMap<X, Y> {

    final int capacity;

    {
        defRetValue = null;
    }

    public ETFLruCache() {
        this.capacity = 2048;
    }

    @Nullable
    public Y get(Object key) {
        //noinspection unchecked
        return getAndMoveToFirst((X) key);
    }


    public Y put(X key, Y value) {
        double sizeModifier = Math.max(1, ETF.config().getConfig().advanced_IncreaseCacheSizeModifier);
        if (size() >= capacity * sizeModifier) {
            X lastKey = lastKey();
            if (!lastKey.equals(key)) {
                remove(lastKey);
            }
        }
        return putAndMoveToFirst(key, value);
    }

    public void removeEntryOnly(X key) {
        remove(key);
    }
}
