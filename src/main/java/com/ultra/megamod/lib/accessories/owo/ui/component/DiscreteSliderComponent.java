package com.ultra.megamod.lib.accessories.owo.ui.component;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import com.ultra.megamod.lib.accessories.owo.ui.core.Component;
import com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Stub for OWO's DiscreteSliderComponent.
 */
public class DiscreteSliderComponent implements Component {
    private String componentId;
    public boolean active = true;

    public DiscreteSliderComponent(Sizing horizontalSizing, int min, int max) {}

    private int min, max, value;

    public int discreteValue() { return value; }
    public DiscreteSliderComponent setFromDiscreteValue(int value) { this.value = value; return this; }
    public DiscreteSliderComponent snap(boolean snap) { return this; }
    public DiscreteSliderComponent scrollStep(float step) { return this; }

    public FlowLayout.EventSource<Consumer<Double>> onChanged() {
        return new FlowLayout.EventSource<>();
    }

    public double min() { return min; }
    public double max() { return max; }

    @Override public Component id(String id) { this.componentId = id; return this; }
    @Override @Nullable public String id() { return componentId; }
    @Override @Nullable public ParentComponent parent() { return null; }
    @Override public Component margins(Insets insets) { return this; }
    @Override public Component sizing(Sizing sizing) { return this; }
}
