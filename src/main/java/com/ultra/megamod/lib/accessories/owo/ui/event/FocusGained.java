package com.ultra.megamod.lib.accessories.owo.ui.event;

import com.ultra.megamod.lib.accessories.owo.ui.core.UIComponent;
import com.ultra.megamod.lib.accessories.owo.util.EventStream;

public interface FocusGained {
    void onFocusGained(UIComponent.FocusSource source);

    static EventStream<FocusGained> newStream() {
        return new EventStream<>(subscribers -> source -> {
            for (var subscriber : subscribers) {
                subscriber.onFocusGained(source);
            }
        });
    }
}
