package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Properties;

/** {@code year} predicate. Ported 1:1 from upstream. */
public class YearProperty extends SimpleIntegerArrayProperty {


    protected YearProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "year"));
    }


    public static YearProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new YearProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"year"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return Calendar.getInstance().get(Calendar.YEAR);
    }
}
