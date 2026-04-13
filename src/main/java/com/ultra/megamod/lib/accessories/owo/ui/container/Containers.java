package com.ultra.megamod.lib.accessories.owo.ui.container;

import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import com.ultra.megamod.lib.accessories.owo.ui.core.Sizing;

/**
 * Adapter stub for io.wispforest.owo.ui.container.Containers.
 */
public class Containers {
    public static FlowLayout horizontalFlow(Sizing horizontal, Sizing vertical) {
        return new FlowLayout(horizontal, vertical, FlowLayout.Algorithm.HORIZONTAL);
    }

    public static FlowLayout verticalFlow(Sizing horizontal, Sizing vertical) {
        return new FlowLayout(horizontal, vertical, FlowLayout.Algorithm.VERTICAL);
    }

    public static StackLayout stack(Sizing horizontal, Sizing vertical) {
        return new StackLayout();
    }

    public static GridLayout grid(Sizing horizontal, Sizing vertical, int rows, int cols) {
        return new GridLayout(horizontal, vertical, rows, cols);
    }

    public static <C extends Component> ScrollContainer<C> verticalScroll(Sizing horizontal, Sizing vertical, C child) {
        return new ScrollContainer<>(ScrollContainer.ScrollDirection.VERTICAL, horizontal, vertical, child);
    }

    public static <C extends Component> ScrollContainer<C> horizontalScroll(Sizing horizontal, Sizing vertical, C child) {
        return new ScrollContainer<>(ScrollContainer.ScrollDirection.HORIZONTAL, horizontal, vertical, child);
    }
}
