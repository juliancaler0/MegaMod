package com.ultra.megamod.lib.emf.runtime;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Monotonically-incrementing frame counter used by the {@code frame_counter}
 * animation variable. Matches upstream EMF's counter semantics — bumped
 * once per client tick, never reset.
 */
public final class EmfFrameCounter {

    private static final AtomicLong COUNTER = new AtomicLong(0L);

    private EmfFrameCounter() {
    }

    public static void tick() {
        COUNTER.incrementAndGet();
    }

    public static long current() {
        return COUNTER.get();
    }
}
