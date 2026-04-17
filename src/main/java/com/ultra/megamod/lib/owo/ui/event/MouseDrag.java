package com.ultra.megamod.lib.owo.ui.event;

import com.ultra.megamod.lib.owo.util.EventStream;
import net.minecraft.client.input.MouseButtonEvent;

public interface MouseDrag {
    boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY);

    static EventStream<MouseDrag> newStream() {
        return new EventStream<>(subscribers -> (click, deltaX, deltaY) -> {
            var anyTriggered = false;
            for (var subscriber : subscribers) {
                anyTriggered |= subscriber.onMouseDrag(click, deltaX, deltaY);
            }
            return anyTriggered;
        });
    }
}
