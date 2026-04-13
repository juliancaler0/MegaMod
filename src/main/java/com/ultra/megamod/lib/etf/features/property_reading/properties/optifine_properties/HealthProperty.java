package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code health} predicate. Supports plain ({@code health.1=20}) and percentage
 * ({@code health.1=50%}) forms.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class HealthProperty extends FloatRangeFromStringArrayProperty {

    private final boolean isPercentage;

    protected HealthProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "health"));
        isPercentage = originalInput.contains("%");
    }

    public static HealthProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new HealthProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Nullable
    @Override
    protected Float getRangeValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof LivingEntity alive) {
            float health = alive.getHealth();
            return isPercentage ? (float) Mth.ceil(health / alive.getMaxHealth() * 100) : health;
        }
        return null;
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"health"};
    }
}
