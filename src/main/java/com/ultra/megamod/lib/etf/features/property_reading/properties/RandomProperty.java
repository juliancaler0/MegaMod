package com.ultra.megamod.lib.etf.features.property_reading.properties;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.EntityBooleanLRU;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * Abstract base class for a single random-property predicate.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public abstract class RandomProperty {

    protected final EntityBooleanLRU entityCachedInitialResult = new EntityBooleanLRU();
    private boolean canUpdate = true;

    /**
     * Reads the given property data from the properties file, allowing for multiple property names and throws an
     * exception to ensure that any actual returned String is always non-blank.
     */
    @NotNull
    public static String readPropertiesOrThrow(Properties properties, int propertyNum, String... propertyId) throws RandomPropertyException {
        if (propertyId.length == 0)
            throw new IllegalArgumentException("[ETF] readPropertiesOrThrow() was given empty property id's");
        for (String id : propertyId) {
            if (properties.containsKey(id + "." + propertyNum)) {
                String dataFromProps = properties.getProperty(id + "." + propertyNum).strip();
                if (!dataFromProps.isBlank()) return dataFromProps;
            }
        }
        throw new RandomPropertyException("failed to read property [" + propertyId[0] + "]");
    }

    /**
     * Test the entity against this property.
     */
    public boolean testEntity(ETFEntityRenderState entity, boolean isUpdate) {
        var key = entity.uuid();
        if (isUpdate && !canPropertyUpdate() && entityCachedInitialResult.containsKey(key)) {
            return entityCachedInitialResult.getBoolean(key);
        }
        try {
            return testEntityInternal(entity);
        } catch (Exception ignored) {
            return false;
        }
    }

    protected abstract boolean testEntityInternal(ETFEntityRenderState entity);

    public final boolean canPropertyUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(final boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    @NotNull
    public abstract String[] getPropertyIds();

    @NotNull
    public String getPropertyId() {
        return getPropertyIds()[0];
    }

    protected abstract String getPrintableRuleInfo();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Property: " + getPropertyId() + ", Rule: " + getPrintableRuleInfo() + "]";
    }

    public void cacheEntityInitialResult(ETFEntityRenderState entity) {
        try {
            entityCachedInitialResult.put(entity.uuid(), testEntityInternal(entity));
        } catch (Exception ignored) {
            entityCachedInitialResult.put(entity.uuid(), false);
        }
    }

    /**
     * Exception thrown when a property file entry fails to construct — e.g. missing key,
     * malformed value, blank. These are expected and should be caught + ignored by the
     * factory that constructed this property.
     */
    public static class RandomPropertyException extends Exception {
        public RandomPropertyException(String reason) {
            super("[ETF] " + reason);
        }
    }
}
