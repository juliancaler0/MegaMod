package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code jump} / {@code jumpStrength} / {@code jumpHeight} predicate for horses.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben). In 1.21.11 the
 * {@code playerJumpPendingScale} field is protected and {@code getJumpBoostPower()} is
 * no longer exposed, so we read the scale via the {@code PlayerRideableJumping} interface.
 */
public class JumpProperty extends FloatRangeFromStringArrayProperty {


    protected JumpProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "jump", "jumpStrength", "jumpHeight"));
    }

    public static JumpProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new JumpProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Nullable
    @Override
    protected Float getRangeValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof AbstractHorse horse) {
            return horse.getPlayerJumpPendingScale(0);
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"jump", "jumpStrength", "jumpHeight"};
    }
}
