package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code monthDay} / {@code dayMonth} predicate (1-31). Ported 1:1 from upstream. */
public class MonthDayProperty extends SimpleIntegerArrayProperty {


    protected MonthDayProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "monthDay", "dayMonth"));
    }


    public static MonthDayProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new MonthDayProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"monthDay", "dayMonth"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
}
