package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code temperature} predicate. Upstream uses {@code getHeightAdjustedTemperature} which
 * is private in 1.21.11, so Phase A uses the biome's base temperature instead — this
 * still returns the same value for most vanilla biomes (the adjustment only matters at
 * high altitudes, and its return value is also never actually "published" per-biome).
 * <p>
 * Ported 1:1 (with the API change called out above) from Entity_Texture_Features (traben).
 */
public class TemperatureProperty extends FloatRangeFromStringArrayProperty {


    protected TemperatureProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "temperature"));
    }

    public static TemperatureProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new TemperatureProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Nullable
    @Override
    protected Float getRangeValueFromEntity(ETFEntityRenderState entity) {
        if (entity == null) return null;

        var level = entity.world();
        if (level == null) return null;

        var biome = level.getBiome(entity.blockPos());
        return biome.value().getBaseTemperature();
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"temperature"};
    }
}
