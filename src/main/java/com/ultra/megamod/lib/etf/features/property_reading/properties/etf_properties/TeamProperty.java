package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code teams} / {@code team} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class TeamProperty extends StringArrayOrRegexProperty {


    protected TeamProperty(Properties properties, int propertyNum) throws RandomProperty.RandomPropertyException {
        super(RandomProperty.readPropertiesOrThrow(properties, propertyNum, "teams", "team"));
    }

    public static TeamProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new TeamProperty(properties, propertyNum);
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @Nullable String getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity.scoreboardTeam() != null) {
            return etfEntity.scoreboardTeam().getName();
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"teams", "team"};
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return false;
    }
}
