package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code language} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class LanguageProperty extends StringArrayOrRegexProperty {


    protected LanguageProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(RandomProperty.readPropertiesOrThrow(properties, propertyNum, "language"));
    }

    public static LanguageProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new LanguageProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @Nullable String getValueFromEntity(ETFEntityRenderState etfEntity) {
        return Minecraft.getInstance().options.languageCode;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"language"};
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return false;
    }
}
