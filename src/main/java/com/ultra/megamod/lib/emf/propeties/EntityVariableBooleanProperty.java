package com.ultra.megamod.lib.emf.propeties;

import org.jetbrains.annotations.NotNull;
import com.ultra.megamod.lib.emf.models.animation.math.MathValue;
import com.ultra.megamod.lib.emf.utils.EMFEntity;
import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EntityVariableBooleanProperty extends RandomProperty {
    private final Map<String, Boolean> VARIABLE_MAP;

    protected EntityVariableBooleanProperty(Properties properties, int propertyNum) throws RandomProperty.RandomPropertyException {
        String keyPrefix = "varb." + propertyNum + ".";
        this.VARIABLE_MAP = new HashMap<>();
        properties.forEach((key, value) -> {
            if (key != null && ((String) key).startsWith(keyPrefix)) {
                String instruction = ((String) value).trim();
                String variableKey = "varb." + ((String) key).replaceAll(keyPrefix, "");
                if (!variableKey.isBlank() && !instruction.isBlank()) {
                    boolean matchTrue = instruction.contains("true");
                    this.VARIABLE_MAP.put(variableKey, matchTrue);
                }
            }

        });
        if (this.VARIABLE_MAP.isEmpty()) {
            throw new RandomProperty.RandomPropertyException("Variable booleans failed");
        }
    }

    public static EntityVariableBooleanProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new EntityVariableBooleanProperty(properties, propertyNum);
        } catch (RandomProperty.RandomPropertyException var3) {
            return null;
        }
    }

    @Override
    protected boolean testEntityInternal(final ETFEntityRenderState etfEntity) {
        if (etfEntity != null && etfEntity.entity() instanceof EMFEntity IEMFEntity) {
            for (Map.Entry<String, Boolean> stringFunctionEntry : VARIABLE_MAP.entrySet()) {
                boolean value = MathValue.toBoolean(
                        IEMFEntity.emf$getVariableMap().getFloat(stringFunctionEntry.getKey())
                );
                if (stringFunctionEntry.getValue() != value) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public @NotNull String[] getPropertyIds() {
        return new String[]{"varb"};
    }

    @Override
    protected String getPrintableRuleInfo() {
        return null;
    }
}
