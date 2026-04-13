package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.BooleanProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code isClientPlayer} predicate. True for the local player only.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ClientPlayerProperty extends BooleanProperty {


    protected ClientPlayerProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericBooleanThatCanNull(properties, propertyNum, "isClientPlayer", "clientPlayer"));
    }

    public static ClientPlayerProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ClientPlayerProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    @Nullable
    protected Boolean getValueFromEntity(ETFEntityRenderState etfEntity) {
        return etfEntity != null && etfEntity.entity() instanceof Player entity
                && Minecraft.getInstance().player != null
                && entity.getUUID().equals(Minecraft.getInstance().player.getUUID());
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"isClientPlayer", "clientPlayer"};
    }
}
