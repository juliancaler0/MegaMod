package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code minute} predicate (wall-clock minute 0-59). Ported 1:1 from upstream. */
public class MinuteProperty extends SimpleIntegerArrayProperty {


    protected MinuteProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "minute"));
    }


    public static MinuteProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new MinuteProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"minute"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }
}
