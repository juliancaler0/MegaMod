package com.ultra.megamod.lib.accessories.owo.ui.event;

import com.ultra.megamod.lib.accessories.owo.util.EventStream;
import net.minecraft.client.input.MouseButtonEvent;

public interface MouseUp {
    boolean onMouseUp(MouseButtonEvent click);

    static EventStream<MouseUp> newStream() {
        return new EventStream<>(subscribers -> (click) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseUp(click);
            }
            return anyTriggered;
        });
    }
}
