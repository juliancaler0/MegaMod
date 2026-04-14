package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.BooleanProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code isSpawner} / {@code spawner} predicate — matches the mob-spawner preview
 * mini-entity using the {@link ETFApi#ETF_SPAWNER_MARKER} UUID tag.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class SpawnerProperty extends BooleanProperty {


    protected SpawnerProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericBooleanThatCanNull(properties, propertyNum, "isSpawner", "spawner"));
    }

    public static SpawnerProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new SpawnerProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    @Nullable
    protected Boolean getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity != null) {
            return etfEntity.uuid().getLeastSignificantBits() == ETFApi.ETF_SPAWNER_MARKER;
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"isSpawner", "spawner"};
    }
}
