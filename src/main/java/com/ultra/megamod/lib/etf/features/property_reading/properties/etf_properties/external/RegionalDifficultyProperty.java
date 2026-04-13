package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code regionalDifficulty} / {@code regional_difficulty} predicate.
 * <p>
 * In 1.21.11 upstream drops {@code Level.getCurrentDifficultyAt(BlockPos)} and
 * falls back to the plain difficulty id. Phase A mirrors that behaviour.
 */
public class RegionalDifficultyProperty extends FloatRangeFromStringArrayProperty {


    protected RegionalDifficultyProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "regionalDifficulty", "regional_difficulty"));
    }


    public static RegionalDifficultyProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new RegionalDifficultyProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"regionalDifficulty", "regional_difficulty"};
    }

    @Override
    protected Float getRangeValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.world() != null) {
            return (float) entity.world().getDifficulty().getId();
        }
        return 0f;
    }
}
