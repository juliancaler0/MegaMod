package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.BooleanProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code isCreative} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class CreativeProperty extends BooleanProperty {


    protected CreativeProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericBooleanThatCanNull(properties, propertyNum, "isCreative", "creative"));
    }

    public static CreativeProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new CreativeProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    @Nullable
    protected Boolean getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity != null && etfEntity.entity() instanceof Player player) return player.isCreative();
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"isCreative", "creative"};
    }
}
