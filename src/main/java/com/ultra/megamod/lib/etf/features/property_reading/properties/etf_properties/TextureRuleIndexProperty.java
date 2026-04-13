package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code textureRule} / {@code texture_rule} predicate. Reads the last matched rule
 * index recorded for the entity in {@link ETFManager#LAST_RULE_INDEX_OF_ENTITY}.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class TextureRuleIndexProperty extends SimpleIntegerArrayProperty {


    protected TextureRuleIndexProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "textureRule", "texture_rule"));
    }


    public static TextureRuleIndexProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new TextureRuleIndexProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"textureRule", "texture_rule"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        int val = ETFManager.getInstance().LAST_RULE_INDEX_OF_ENTITY.getInt(entity.uuid());
        return Math.max(val, 0);
    }
}
