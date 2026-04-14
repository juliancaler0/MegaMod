package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.BooleanProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code baby} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class BabyProperty extends BooleanProperty {


    protected BabyProperty(Properties properties, int propertyNum) throws RandomProperty.RandomPropertyException {
        super(getGenericBooleanThatCanNull(properties, propertyNum, "baby"));
    }

    public static BabyProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new BabyProperty(properties, propertyNum);
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }

    @Override
    @Nullable
    protected Boolean getValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof LivingEntity alive) {
            return alive.isBaby();
        }
        return null;
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"baby"};
    }
}
