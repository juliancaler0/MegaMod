package com.ultra.megamod.lib.accessories.owo.ui.component;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.ui.component.ButtonComponent.
 */
public class ButtonComponent implements Component {

    public boolean active = true;
    public boolean visible = true;
    private String componentId;
    private final net.minecraft.network.chat.Component label;
    private Consumer<ButtonComponent> onPress;

    public ButtonComponent(net.minecraft.network.chat.Component label, Consumer<ButtonComponent> onPress) {
        this.label = label;
        this.onPress = onPress;
    }

    public boolean active() { return active; }
    public boolean isActive() { return active; }
    public boolean isHovered() { return false; }
    public int x() { return 0; }
    public int y() { return 0; }
    public int getX() { return 0; }
    public int getY() { return 0; }
    public int width() { return 0; }
    public int height() { return 0; }

    public net.minecraft.network.chat.Component getMessage() { return label; }
    public void setMessage(net.minecraft.network.chat.Component message) { /* stub */ }

    public ButtonComponent onPress(Consumer<ButtonComponent> press) { this.onPress = press; return this; }
    public com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout.EventSource<com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout.MouseScrollHandler> mouseScroll() {
        return new com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout.EventSource<>();
    }
    public ButtonComponent renderer(Renderer renderer) { return this; }
    @Override public ButtonComponent tooltip(net.minecraft.network.chat.Component tooltip) { return this; }
    @Override public ButtonComponent margins(Insets insets) { return this; }
    @Override public ButtonComponent sizing(Sizing sizing) { return this; }
    public ButtonComponent horizontalSizing(Sizing sizing) { return this; }
    public ButtonComponent verticalSizing(Sizing sizing) { return this; }
    @Override public ButtonComponent positioning(Positioning positioning) { return this; }
    @Override public ButtonComponent id(String id) { this.componentId = id; return this; }
    @Override @Nullable public String id() { return componentId; }
    @Override @Nullable public ParentComponent parent() { return null; }

    @FunctionalInterface
    public interface Renderer {
        void draw(OwoUIDrawContext context, ButtonComponent button, float delta);
    }
}
