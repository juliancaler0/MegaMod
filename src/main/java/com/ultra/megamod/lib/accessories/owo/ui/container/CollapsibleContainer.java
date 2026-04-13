package com.ultra.megamod.lib.accessories.owo.ui.container;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;

import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.ui.container.CollapsibleContainer.
 */
public class CollapsibleContainer extends FlowLayout {

    protected boolean expanded;
    protected final FlowLayout titleLayoutInstance;

    protected CollapsibleContainer(Sizing horizontalSizing, Sizing verticalSizing, net.minecraft.network.chat.Component title, boolean expanded) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.expanded = expanded;
        this.titleLayoutInstance = new FlowLayout(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
    }

    public FlowLayout titleLayout() { return titleLayoutInstance; }

    public boolean expanded() { return expanded; }

    public void toggleExpansion() { this.expanded = !this.expanded; }

    public EventStream<Consumer<Boolean>> onToggled() { return new EventStream<>(); }

    @Override
    public CollapsibleContainer surface(SurfaceRenderer<Component> surfaceFunc) { return this; }

    @Override
    public CollapsibleContainer surface(Surface surface) { return this; }

    /**
     * Simple event stream stub that allows subscribe calls.
     */
    public static class EventStream<T> {
        public void subscribe(T listener) {
            // No-op in stub
        }
    }
}
