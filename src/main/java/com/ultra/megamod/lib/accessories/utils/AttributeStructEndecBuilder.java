package com.ultra.megamod.lib.accessories.utils;


import com.ultra.megamod.lib.accessories.endec.adapter.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeStructEndecBuilder<T> {

    private final Map<SerializationAttribute, StructEndec<T>> branches = new LinkedHashMap<>();

    public AttributeStructEndecBuilder(StructEndec<T> endec, SerializationAttribute attribute) {
        this.branches.put(attribute, endec);
    }

    public AttributeStructEndecBuilder<T> orElseIf(StructEndec<T> endec, SerializationAttribute attribute) {
        return orElseIf(attribute, endec);
    }

    public AttributeStructEndecBuilder<T> orElseIf(SerializationAttribute attribute, StructEndec<T> endec) {
        if (this.branches.containsKey(attribute)) {
            throw new IllegalStateException("Cannot have more than one branch for attribute " + attribute.name);
        }

        this.branches.put(attribute, endec);
        return this;
    }

    public StructEndec<T> orElse(StructEndec<T> endec) {
        // Resolve the primary branch codec (first branch = human-readable) for Codec-based usage
        var primaryBranch = AttributeStructEndecBuilder.this.branches.values().iterator().next();

        return new StructEndec<T>() {
            @Override
            public com.mojang.serialization.Codec<T> codec() {
                // Delegate to the primary (human-readable) branch's codec for
                // persistence/fieldCodec usage. Falls back to the fallback endec's codec
                // if the primary branch doesn't support codec().
                try {
                    return primaryBranch.codec();
                } catch (UnsupportedOperationException e) {
                    return endec.codec();
                }
            }

            @Override
            public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, T value) {
                var branchEndec = endec;

                for (var branch : AttributeStructEndecBuilder.this.branches.entrySet()) {
                    if (ctx.hasAttribute(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                branchEndec.encodeStruct(ctx, serializer, struct, value);
            }

            @Override
            public T decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
                var branchEndec = endec;

                for (var branch : AttributeStructEndecBuilder.this.branches.entrySet()) {
                    if (ctx.hasAttribute(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                return branchEndec.decodeStruct(ctx, deserializer, struct);
            }
        };
    }
}
