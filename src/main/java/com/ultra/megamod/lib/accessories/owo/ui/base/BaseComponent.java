package com.ultra.megamod.lib.accessories.owo.ui.base;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter stub for io.wispforest.owo.ui.base.BaseComponent.
 */
public abstract class BaseComponent implements Component {
    private String id;
    private ParentComponent parent;

    // Public fields that subclasses access
    public int x, y, width, height;
    public AnimatableProperty<Sizing> horizontalSizing = AnimatableProperty.of(Sizing.content());
    public AnimatableProperty<Sizing> verticalSizing = AnimatableProperty.of(Sizing.content());

    @Override
    public Component id(String id) {
        this.id = id;
        return this;
    }

    @Override
    @Nullable
    public String id() {
        return id;
    }

    @Override
    @Nullable
    public ParentComponent parent() {
        return parent;
    }

    @Override
    public int x() { return x; }

    @Override
    public int y() { return y; }

    @Override
    public int width() { return width; }

    @Override
    public int height() { return height; }

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean isActive() { return true; }

    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}

    public void update(float delta, int mouseX, int mouseY) {}
}
