package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code month} predicate (January = 0). Ported 1:1 from upstream. */
public class MonthProperty extends SimpleIntegerArrayProperty {


    protected MonthProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "month"));
    }


    public static MonthProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new MonthProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"month"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.MONTH);
    }
}
