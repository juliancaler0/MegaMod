package com.ultra.megamod.lib.accessories.owo.ui.core;

import com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.BiFunction;

/**
 * Adapter stub for io.wispforest.owo.ui.core.OwoUIAdapter.
 */
public class OwoUIAdapter<T extends ParentComponent> {
    public T rootComponent;

    @SuppressWarnings("unchecked")
    public static <T extends ParentComponent> OwoUIAdapter<T> create(Screen screen, BiFunction<Sizing, Sizing, T> rootComponentFactory) {
        var adapter = new OwoUIAdapter<T>();
        adapter.rootComponent = rootComponentFactory.apply(Sizing.fill(), Sizing.fill());
        return adapter;
    }
}
