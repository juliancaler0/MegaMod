package com.ultra.megamod.lib.accessories.endec.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for io.wispforest.endec.SerializationContext.
 * Carries context information during serialization/deserialization.
 */
public final class SerializationContext {

    private static final SerializationContext EMPTY = new SerializationContext(Map.of());

    private final Map<SerializationAttribute, Object> attributes;

    private SerializationContext(Map<SerializationAttribute, Object> attributes) {
        this.attributes = attributes;
    }

    public static SerializationContext empty() {
        return EMPTY;
    }

    public static SerializationContext attributes(SerializationAttribute attr) {
        var map = new HashMap<SerializationAttribute, Object>();
        map.put(attr, Boolean.TRUE);
        return new SerializationContext(map);
    }

    public static SerializationContext attributes(SerializationAttribute.Instance instance) {
        var map = new HashMap<SerializationAttribute, Object>();
        map.put(instance.attribute(), instance.value());
        return new SerializationContext(map);
    }

    public static SerializationContext attributes(SerializationAttribute.Instance... instances) {
        var map = new HashMap<SerializationAttribute, Object>();
        for (var inst : instances) {
            map.put(inst.attribute(), inst.value());
        }
        return new SerializationContext(map);
    }

    public SerializationContext withAttributes(SerializationAttribute attr) {
        var map = new HashMap<>(this.attributes);
        map.put(attr, Boolean.TRUE);
        return new SerializationContext(map);
    }

    public SerializationContext withAttributes(SerializationAttribute.Instance instance) {
        var map = new HashMap<>(this.attributes);
        map.put(instance.attribute(), instance.value());
        return new SerializationContext(map);
    }

    public SerializationContext withAttributes(SerializationAttribute.Instance... instances) {
        var map = new HashMap<>(this.attributes);
        for (var inst : instances) {
            map.put(inst.attribute(), inst.value());
        }
        return new SerializationContext(map);
    }

    public SerializationContext and(SerializationContext other) {
        if (other == null || other.attributes.isEmpty()) return this;
        var map = new HashMap<>(this.attributes);
        map.putAll(other.attributes);
        return new SerializationContext(map);
    }

    public boolean hasAttribute(SerializationAttribute attr) {
        return attributes.containsKey(attr);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttributeValue(SerializationAttribute attr) {
        return (T) attributes.get(attr);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttributeValue(SerializationAttribute.WithValue<T> attr) {
        return (T) attributes.get(attr);
    }

    @SuppressWarnings("unchecked")
    public <T> T requireAttributeValue(SerializationAttribute.WithValue<T> attr) {
        T value = (T) attributes.get(attr);
        if (value == null) {
            throw new IllegalStateException("Required serialization attribute not found: " + attr.name());
        }
        return value;
    }
}
