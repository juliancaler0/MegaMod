package com.ultra.megamod.lib.owo.ui.event;

import com.ultra.megamod.lib.owo.util.EventStream;

public interface MouseEnter {
    void onMouseEnter();

    static EventStream<MouseEnter> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseEnter();
            }
        });
    }
}
