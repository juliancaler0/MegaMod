package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.LongRangeFromStringArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

import java.util.Properties;


public class TimeOfDayProperty extends LongRangeFromStringArrayProperty {


    protected TimeOfDayProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "dayTime"));
    }

    public static TimeOfDayProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new TimeOfDayProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Nullable
    @Override
    protected Long getRangeValueFromEntity(ETFEntityRenderState entity) {
        if (entity.world() != null)
            return entity.world().getDayTime() % 24000;
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"dayTime"};
    }

}
