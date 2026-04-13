package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code moonPhase} predicate. In 1.21.11 the moon phase lives on the client-side
 * {@code LevelRenderState}, so we pull it from {@code Minecraft.gameRenderer}.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class MoonPhaseProperty extends SimpleIntegerArrayProperty {


    protected MoonPhaseProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "moonPhase"));
    }


    public static MoonPhaseProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new MoonPhaseProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"moonPhase"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        try {
            return Minecraft.getInstance().gameRenderer.getLevelRenderState().skyRenderState.moonPhase.index();
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }
}
