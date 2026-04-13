package com.ultra.megamod.lib.accessories.owo.ui.component;

import com.ultra.megamod.lib.accessories.owo.ui.core.Sizing;
import com.ultra.megamod.lib.accessories.owo.ui.container.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Adapter stub for io.wispforest.owo.ui.component.Components.
 */
public class Components {

    public static ButtonComponent button(Component label, Consumer<ButtonComponent> onPress) {
        return new ButtonComponent(label, onPress);
    }

    public static LabelComponent label(Component text) {
        return new LabelComponent(text);
    }

    public static DiscreteSliderComponent discreteSlider(Sizing horizontalSizing, int min, int max) {
        return new DiscreteSliderComponent(horizontalSizing, min, max);
    }

    public static GridLayout grid(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
        return new GridLayout(horizontalSizing, verticalSizing, rows, columns);
    }

    public static com.ultra.megamod.lib.accessories.owo.ui.core.Component box(Sizing sizing) {
        return new LabelComponent(Component.empty());
    }

    public static com.ultra.megamod.lib.accessories.owo.ui.core.Component spacer(int size) {
        return new LabelComponent(Component.empty());
    }

    public static class LabelComponent implements com.ultra.megamod.lib.accessories.owo.ui.core.Component {
        private Component text;
        private String componentId;

        public LabelComponent(Component text) {
            this.text = text;
        }

        public Component text() { return text; }
        public LabelComponent text(Component text) { this.text = text; return this; }
        public LabelComponent color(com.ultra.megamod.lib.accessories.owo.ui.core.Color color) { return this; }
        public LabelComponent maxWidth(int width) { return this; }
        public LabelComponent lineHeight(int height) { return this; }
        public LabelComponent shadow(boolean shadow) { return this; }

        @Override public com.ultra.megamod.lib.accessories.owo.ui.core.Component id(String id) { componentId = id; return this; }
        @Override public String id() { return componentId; }
        @Override public com.ultra.megamod.lib.accessories.owo.ui.core.ParentComponent parent() { return null; }
    }
}
