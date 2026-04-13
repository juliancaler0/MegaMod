package com.ultra.megamod.lib.accessories.owo.ui.container;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Adapter stub for io.wispforest.owo.ui.container.StackLayout.
 */
public class StackLayout implements ParentComponent {

    private String componentId;
    private final List<Component> childList = new ArrayList<>();

    @Override
    public StackLayout id(String id) { this.componentId = id; return this; }
    @Override @Nullable public String id() { return componentId; }
    @Override @Nullable public ParentComponent parent() { return null; }
    @Override @Nullable public <C extends Component> C childById(Class<C> clazz, String id) { return null; }
    @Override public void removeChild(Component child) { childList.remove(child); }
    @Override public List<Component> children() { return childList; }
    @Override public StackLayout child(Component child) { if (child != null) childList.add(child); return this; }
    @Override public StackLayout child(int index, Component child) { if (child != null) childList.add(Math.min(index, childList.size()), child); return this; }

    public StackLayout padding(Insets padding) { return this; }
    public StackLayout surface(Object surface) { return this; }
    public StackLayout surface(BiConsumer<Object, StackLayout> surfaceFunc) { return this; }
    public StackLayout allowOverflow(boolean allow) { return this; }
    @Override public StackLayout positioning(Positioning positioning) { return this; }
    @Override public StackLayout margins(Insets insets) { return this; }
    @Override public StackLayout sizing(Sizing sizing) { return this; }
    @Override public boolean isInBoundingBox(double x, double y) { return false; }
}
