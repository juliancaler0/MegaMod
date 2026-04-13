package com.ultra.megamod.lib.accessories.owo.ui.container;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.ui.container.FlowLayout.
 */
public class FlowLayout implements ParentComponent {

    public enum Algorithm {
        HORIZONTAL, VERTICAL
    }

    private String componentId;
    private final List<Component> childList = new ArrayList<>();

    public FlowLayout() {}

    public FlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {}

    @Override
    public FlowLayout id(String id) { this.componentId = id; return this; }

    @Override
    @Nullable
    public String id() { return componentId; }

    @Override
    @Nullable
    public ParentComponent parent() { return null; }

    @Override
    @Nullable
    public <C extends Component> C childById(Class<C> clazz, String id) { return null; }

    @Override
    public void removeChild(Component child) { childList.remove(child); }

    @Override
    public List<Component> children() { return childList; }

    @Override
    public FlowLayout child(Component child) { if (child != null) childList.add(child); return this; }

    @Override
    public FlowLayout child(int index, Component child) { if (child != null) childList.add(Math.min(index, childList.size()), child); return this; }

    public FlowLayout children(Collection<? extends Component> children) { childList.addAll(children); return this; }

    public FlowLayout gap(int gap) { return this; }

    public FlowLayout padding(Insets padding) { return this; }

    @FunctionalInterface
    public interface SurfaceRenderer<T extends Component> {
        void draw(OwoUIDrawContext ctx, T component);
    }

    public FlowLayout surface(Surface surface) { return this; }

    public FlowLayout surface(SurfaceRenderer<Component> surfaceFunc) { return this; }

    public FlowLayout allowOverflow(boolean allow) { return this; }

    public FlowLayout horizontalAlignment(HorizontalAlignment alignment) { return this; }

    public FlowLayout verticalAlignment(VerticalAlignment alignment) { return this; }

    @Override
    public FlowLayout positioning(Positioning positioning) { return this; }

    @Override
    public FlowLayout margins(Insets insets) { return this; }

    @Override
    public FlowLayout sizing(Sizing sizing) { return this; }

    public FlowLayout sizing(Sizing horizontal, Sizing vertical) { return this; }

    public FlowLayout horizontalSizing(Sizing sizing) { return this; }

    public FlowLayout verticalSizing(Sizing sizing) { return this; }

    @Override
    public FlowLayout tooltip(net.minecraft.network.chat.Component tooltip) { return this; }

    @Override
    public boolean isInBoundingBox(double x, double y) { return false; }

    public void clearChildren() { childList.clear(); }

    public EventSource<MouseScrollHandler> mouseScroll() { return new EventSource<>(); }

    protected void parentUpdate(float delta, int mouseX, int mouseY) {}

    @FunctionalInterface
    public interface MouseScrollHandler {
        boolean handle(double mouseX, double mouseY, double amount);
    }

    public static class EventSource<T> {
        public EventSource<T> subscribe(T handler) { return this; }
        public void register(T handler) {}
    }
}
