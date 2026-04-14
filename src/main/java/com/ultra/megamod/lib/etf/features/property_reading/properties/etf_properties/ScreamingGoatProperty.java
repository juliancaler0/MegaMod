package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.BooleanProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.animal.goat.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code screamingGoat} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ScreamingGoatProperty extends BooleanProperty {


    protected ScreamingGoatProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericBooleanThatCanNull(properties, propertyNum, "screamingGoat", "screaming_goat"));
    }

    public static ScreamingGoatProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ScreamingGoatProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    @Nullable
    protected Boolean getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity != null && etfEntity.entity() instanceof Goat goat) return goat.isScreamingGoat();
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"screamingGoat", "screaming_goat"};
    }
}
