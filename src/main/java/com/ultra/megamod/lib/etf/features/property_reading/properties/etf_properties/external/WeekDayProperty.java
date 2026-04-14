package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code weekDay} / {@code dayWeek} predicate (Sunday = 1). Ported 1:1 from upstream. */
public class WeekDayProperty extends SimpleIntegerArrayProperty {


    protected WeekDayProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "weekDay", "dayWeek"));
    }


    public static WeekDayProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new WeekDayProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"weekDay", "dayWeek"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }
}
