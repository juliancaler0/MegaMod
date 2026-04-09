package com.ultra.megamod.feature.citizen.request;

import java.util.UUID;

/**
 * Unique identifier for requests and resolvers in the colony request system.
 */
public interface IToken {

    /**
     * Returns the unique ID for this token.
     *
     * @return the UUID
     */
    UUID getId();
}
