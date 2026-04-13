package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code clientGameMode} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ClientGameModeProperty extends SimpleIntegerArrayProperty {


    protected ClientGameModeProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "clientGameMode"));
    }


    public static ClientGameModeProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ClientGameModeProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"clientGameMode"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        if (Minecraft.getInstance().player != null) {
            var mode = Minecraft.getInstance().player.gameMode();
            if (mode != null) return mode.getId();
        }
        return -1;
    }
}
