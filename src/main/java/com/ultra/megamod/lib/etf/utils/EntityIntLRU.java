package com.ultra.megamod.lib.etf.utils;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;

import java.util.UUID;

/**
 * UUID-keyed int LRU cache used by the ETF random-property / variant selector system.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben) for MegaMod's integrated ETF port.
 */
public class EntityIntLRU extends Object2IntLinkedOpenHashMap<UUID> {
    final int capacity;

    {
        defaultReturnValue(-1);
    }

    public EntityIntLRU(int capacity) {
        this.capacity = capacity;
    }

    public EntityIntLRU() {
        this.capacity = 2048;
    }


    @Override
    public int getInt(Object k) {
        return super.getAndMoveToFirst((UUID) k);
    }

    @Override
    public int put(UUID uuid, int v) {
        if (size() >= capacity) {
            UUID lastKey = lastKey();
            if (!lastKey.equals(uuid)) {
                removeInt(lastKey);
            }
        }
        return this.putAndMoveToFirst(uuid, v);
    }
}
