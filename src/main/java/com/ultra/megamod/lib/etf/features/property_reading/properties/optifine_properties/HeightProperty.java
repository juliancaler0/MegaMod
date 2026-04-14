package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code heights} / {@code height} predicate. Also understands legacy {@code minHeight} /
 * {@code maxHeight} pairs.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class HeightProperty extends SimpleIntegerArrayProperty {


    protected HeightProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "heights", "height"));
    }


    public static HeightProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            if (!(properties.containsKey("heights." + propertyNum) || properties.containsKey("height." + propertyNum))
                    && (properties.containsKey("minHeight." + propertyNum) || properties.containsKey("maxHeight." + propertyNum))) {
                String min = "-64";
                String max = "319";
                if (properties.containsKey("minHeight." + propertyNum)) {
                    min = properties.getProperty("minHeight." + propertyNum).strip();
                }
                if (properties.containsKey("maxHeight." + propertyNum)) {
                    max = properties.getProperty("maxHeight." + propertyNum).strip();
                }
                properties.put("heights." + propertyNum, min + "-" + max);
            }
            return new HeightProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"heights", "height"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return entity.blockY();
    }
}
