package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties.external;

import com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties.NBTProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.Properties;

/**
 * {@code nbtVehicle} predicate. Same as {@link NBTProperty} but reads the NBT of
 * the entity's current vehicle.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class NBTVehicleProperty extends NBTProperty {
    protected NBTVehicleProperty(final Properties properties, final int propertyNum, final String nbtPrefix) throws RandomPropertyException {
        super(properties, propertyNum, nbtPrefix);
    }

    public static NBTProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new NBTVehicleProperty(properties, propertyNum, "nbtVehicle");
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected CompoundTag getEntityNBT(final ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof Entity e) {
            ETFEntity vehicle = (ETFEntity) e.getVehicle();
            return vehicle != null ? vehicle.etf$getNbt() : INTENTIONAL_FAILURE;
        }
        return INTENTIONAL_FAILURE;
    }
}
