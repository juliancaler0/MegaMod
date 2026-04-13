package com.ultra.megamod.lib.accessories.owo.ui.core;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Component.
 * Minimal interface to satisfy compile-time dependencies.
 */
public interface Component extends PositionedRectangle {
    Component id(String id);
    @Nullable String id();
    @Nullable ParentComponent parent();

    default int x() { return 0; }
    default int y() { return 0; }
    default int width() { return 0; }
    default int height() { return 0; }
    default boolean isInBoundingBox(double x, double y) { return false; }

    default Component margins(Insets insets) { return this; }
    default Component sizing(Sizing sizing) { return this; }
    default Component positioning(Positioning positioning) { return this; }
    default Component tooltip(net.minecraft.network.chat.Component tooltip) { return this; }
    default <C extends Component> C configure(Consumer<C> consumer) {
        consumer.accept((C) this);
        return (C) this;
    }
    default Sizing horizontalSizing() { return Sizing.content(); }
    default Sizing verticalSizing() { return Sizing.content(); }
    default void inflate(Size space) { /* no-op in stub */ }
    default Component horizontalSizing(Sizing sizing) { return this; }
    default Component verticalSizing(Sizing sizing) { return this; }
}
