package com.ultra.megamod.lib.owo.ui.event;

import com.ultra.megamod.lib.owo.util.EventStream;

public interface FocusLost {
    void onFocusLost();

    static EventStream<FocusLost> newStream() {
        return new EventStream<>(subscribers -> () -> {
            for (var subscriber : subscribers) {
                subscriber.onFocusLost();
            }
        });
    }
}
