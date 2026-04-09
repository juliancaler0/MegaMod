package com.ultra.megamod.feature.ambientsounds.util;

import java.util.HashSet;
import java.util.Set;

public class SimpleQuadBitSet {

    private final Set<Long> bits = new HashSet<>();

    public void set(int x, int z) {
        bits.add(encode(x, z));
    }

    public boolean get(int x, int z) {
        return bits.contains(encode(x, z));
    }

    public void clear() {
        bits.clear();
    }

    private long encode(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
