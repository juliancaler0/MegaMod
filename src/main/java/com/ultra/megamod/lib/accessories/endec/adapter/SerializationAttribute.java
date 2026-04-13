package com.ultra.megamod.lib.accessories.endec.adapter;

/**
 * Adapter for io.wispforest.endec.SerializationAttribute.
 * Represents a marker for serialization context (e.g., HUMAN_READABLE).
 */
public class SerializationAttribute {
    public final String name;

    public SerializationAttribute(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "SerializationAttribute[" + name + "]";
    }

    public static <T extends Instance> WithValue<T> withValue(String name) {
        return new WithValue<>(name);
    }

    public Instance instance(Object value) {
        var attr = this;
        return new Instance() {
            @Override
            public SerializationAttribute attribute() {
                return attr;
            }

            @Override
            public Object value() {
                return value;
            }
        };
    }

    public interface Instance {
        SerializationAttribute attribute();
        Object value();
    }

    public static class WithValue<T> extends SerializationAttribute {
        public WithValue(String name) {
            super(name);
        }

        @SuppressWarnings("unchecked")
        public T withValue(Object value) {
            return (T) value;
        }
    }
}
