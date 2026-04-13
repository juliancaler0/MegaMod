package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code yearDay} / {@code dayYear} predicate (1-366). Ported 1:1 from upstream. */
public class YearDayProperty extends SimpleIntegerArrayProperty {


    protected YearDayProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "yearDay", "dayYear"));
    }


    public static YearDayProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new YearDayProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"yearDay", "dayYear"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    }
}
