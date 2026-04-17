package com.ultra.megamod.lib.owo.ui.event;

import com.ultra.megamod.lib.owo.util.EventStream;

public interface MouseLeave {
    void onMouseLeave();

    static EventStream<MouseLeave> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onMouseLeave();
            }
        });
    }
}
