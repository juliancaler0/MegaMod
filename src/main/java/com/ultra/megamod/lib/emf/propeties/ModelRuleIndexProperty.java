package com.ultra.megamod.lib.emf.propeties;

import org.jetbrains.annotations.NotNull;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

import java.util.Properties;

public class ModelRuleIndexProperty extends SimpleIntegerArrayProperty {
    protected ModelRuleIndexProperty(Properties properties, int propertyNum) throws RandomProperty.RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "modelRule", "model_rule"));
    }

    public static ModelRuleIndexProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ModelRuleIndexProperty(properties, propertyNum);
        } catch (RandomProperty.RandomPropertyException var3) {
            return null;
        }
    }

    public @NotNull String[] getPropertyIds() {
        return new String[]{"modelRule", "model_rule"};
    }

    protected int getValueFromEntity(ETFEntityRenderState entity) {
        return EMFManager.getInstance().lastModelRuleOfEntity.getInt(entity.uuid());
    }
}
