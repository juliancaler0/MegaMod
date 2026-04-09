package com.ultra.megamod.feature.citizen.request;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * UUID-based token implementation for uniquely identifying requests and resolvers.
 */
public record StandardToken(UUID id) implements IToken {

    /**
     * Creates a new token with a random UUID.
     */
    public StandardToken() {
        this(UUID.randomUUID());
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Serializes this token to NBT.
     *
     * @return the compound tag
     */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("uuid", id.toString());
        return tag;
    }

    /**
     * Deserializes a token from NBT.
     *
     * @param tag the compound tag
     * @return the loaded token
     */
    public static StandardToken fromNbt(CompoundTag tag) {
        String uuidStr = tag.getStringOr("uuid", "");
        if (uuidStr.isEmpty()) {
            return new StandardToken();
        }
        try {
            return new StandardToken(UUID.fromString(uuidStr));
        } catch (IllegalArgumentException e) {
            return new StandardToken();
        }
    }
}
