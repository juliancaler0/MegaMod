package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code textureSuffix} / {@code texture_suffix} predicate. Reads the last picked
 * variant suffix for the entity in {@link ETFManager#LAST_SUFFIX_OF_ENTITY}.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class TextureSuffixProperty extends SimpleIntegerArrayProperty {


    protected TextureSuffixProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "textureSuffix", "texture_suffix"));
    }


    public static TextureSuffixProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new TextureSuffixProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"textureSuffix", "texture_suffix"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        int val = ETFManager.getInstance().LAST_SUFFIX_OF_ENTITY.getInt(entity.uuid());
        return Math.max(val, 0);
    }
}
