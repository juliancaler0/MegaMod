package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.BooleanProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.monster.Creeper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code creeperCharged} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ChargedCreeperProperty extends BooleanProperty {


    protected ChargedCreeperProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericBooleanThatCanNull(properties, propertyNum, "creeperCharged", "creeper_charged"));
    }

    public static ChargedCreeperProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ChargedCreeperProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    @Nullable
    protected Boolean getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity != null && etfEntity.entity() instanceof Creeper creeper)
            return creeper.isPowered();
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"creeperCharged", "creeper_charged"};
    }
}
