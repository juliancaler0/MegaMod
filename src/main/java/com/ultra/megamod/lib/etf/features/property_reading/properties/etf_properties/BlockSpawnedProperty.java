package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties.BlocksProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code blockSpawned} — block the entity initially spawned on.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class BlockSpawnedProperty extends BlocksProperty {


    protected BlockSpawnedProperty(final Properties properties, final int propertyNum, final String[] ids) throws RandomPropertyException {
        super(properties, propertyNum, ids);
    }

    public static BlocksProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new BlockSpawnedProperty(properties, propertyNum, new String[]{"blockSpawned"});
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"blockSpawned"};
    }
}
