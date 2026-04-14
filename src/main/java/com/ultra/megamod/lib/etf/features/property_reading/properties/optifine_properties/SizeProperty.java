package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code sizes} / {@code size} predicate for slimes / magma cubes / phantoms.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class SizeProperty extends SimpleIntegerArrayProperty {


    protected SizeProperty(Properties properties, int propertyNum) throws RandomProperty.RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "sizes", "size"));
    }


    public static SizeProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new SizeProperty(properties, propertyNum);
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"sizes", "size"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof Slime slime) {
            return slime.getSize() - 1;
        } else if (entity != null && entity.entity() instanceof Phantom phantom) {
            return phantom.getPhantomSize();
        }
        return 0;
    }
}
