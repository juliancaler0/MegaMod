package com.ultra.megamod.lib.accessories.owo.serialization;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationAttribute;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;

/**
 * Adapter for io.wispforest.owo.serialization.RegistriesAttribute.
 * Provides registry access as a serialization attribute.
 */
public final class RegistriesAttribute implements SerializationAttribute.Instance {

    public static final SerializationAttribute.WithValue<RegistriesAttribute> REGISTRIES = SerializationAttribute.withValue("registries");

    private final HolderLookup.Provider registries;

    private RegistriesAttribute(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    public HolderLookup.Provider registries() {
        return registries;
    }

    public HolderLookup.Provider infoGetter() {
        return registries;
    }

    public RegistryAccess registryManager() {
        if (registries instanceof RegistryAccess ra) {
            return ra;
        }
        throw new IllegalStateException("RegistriesAttribute does not hold a RegistryAccess");
    }

    @Override
    public SerializationAttribute attribute() { return REGISTRIES; }

    @Override
    public Object value() { return this; }

    public static RegistriesAttribute of(HolderLookup.Provider registries) {
        return new RegistriesAttribute(registries);
    }

    public static RegistriesAttribute fromInfoGetter(Object infoGetter) {
        return null;
    }

    public static RegistriesAttribute tryFromCachedInfoGetter(Object infoGetter) {
        return null;
    }
}
