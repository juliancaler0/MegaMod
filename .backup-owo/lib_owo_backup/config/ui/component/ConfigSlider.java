package com.ultra.megamod.lib.owo.config.ui.component;

import com.ultra.megamod.lib.owo.ui.component.DiscreteSliderComponent;
import com.ultra.megamod.lib.owo.ui.core.Sizing;
import com.ultra.megamod.lib.owo.util.NumberReflection;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConfigSlider extends DiscreteSliderComponent implements OptionValueProvider {

    protected Class<? extends Number> valueType;

    public ConfigSlider() {
        super(Sizing.content(), 0, 1);
    }

    public ConfigSlider valueType(Class<? extends Number> valueType) {
        this.valueType = valueType;
        return this;
    }

    public ConfigSlider min(double min) {
        this.min = min;
        return this;
    }

    public ConfigSlider max(double max) {
        this.max = max;
        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        double value = this.min + this.value * (this.max - this.min);
        if (!NumberReflection.isFloatingPointType(this.valueType)) {
            value = Math.round(value);
        }

        return NumberReflection.convert(value, this.valueType);
    }
}
