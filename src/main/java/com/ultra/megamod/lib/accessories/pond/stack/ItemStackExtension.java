package com.ultra.megamod.lib.accessories.pond.stack;

import com.ultra.megamod.lib.accessories.utils.ItemStackResize;
import com.ultra.megamod.lib.accessories.owo.util.EventStream;

public interface ItemStackExtension {
    EventStream<ItemStackResize> accessories$getResizeEvent();
}
