package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code maxHealth} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class MaxHealthProperty extends FloatRangeFromStringArrayProperty {


    protected MaxHealthProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "maxHealth", "max_health"));
    }

    public static MaxHealthProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new MaxHealthProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Nullable
    @Override
    protected Float getRangeValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof LivingEntity alive)
            return alive.getMaxHealth();
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"maxHealth", "max_health"};
    }
}
