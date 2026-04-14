package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code hour} predicate (wall-clock hour 0-23). Ported 1:1 from upstream. */
public class HourProperty extends SimpleIntegerArrayProperty {


    protected HourProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "hour"));
    }


    public static HourProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new HourProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"hour"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}
