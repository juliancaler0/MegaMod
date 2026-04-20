package com.ultra.megamod.lib.accessories.owo.ui.event;

import com.ultra.megamod.lib.accessories.owo.util.EventStream;

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
