package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code difficulty} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class DifficultyProperty extends SimpleIntegerArrayProperty {


    protected DifficultyProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "difficulty"));
    }


    public static DifficultyProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new DifficultyProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"difficulty"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null) {
            return entity.world().getDifficulty().getId();
        }
        return 0;
    }
}
