package com.ultra.megamod.lib.accessories.owo.ui.core;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Adapter stub for io.wispforest.owo.ui.core.ParentComponent.
 */
public interface ParentComponent extends Component {
    @Nullable <C extends Component> C childById(Class<C> clazz, String id);
    void removeChild(Component child);

    default List<Component> children() { return List.of(); }
    default ParentComponent child(Component child) { return this; }
    default ParentComponent child(int index, Component child) { return this; }

    default ParentComponent padding(Insets padding) { return this; }
    default ParentComponent gap(int gap) { return this; }
    default ParentComponent allowOverflow(boolean allow) { return this; }
    default ParentComponent horizontalAlignment(HorizontalAlignment alignment) { return this; }
    default ParentComponent verticalAlignment(VerticalAlignment alignment) { return this; }
    default ParentComponent surface(Object surface) { return this; }
}
