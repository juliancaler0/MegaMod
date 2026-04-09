package com.ultra.megamod.feature.citizen.request;

import net.minecraft.core.BlockPos;

/**
 * Represents an entity that can create requests in the colony request system.
 * Typically a building or a citizen.
 */
public interface IRequester {

    /**
     * Returns the unique token identifying this requester.
     *
     * @return the requester token
     */
    IToken getRequesterId();

    /**
     * Returns the display name of this requester.
     *
     * @return the requester name
     */
    String getRequesterName();

    /**
     * Returns the block position of this requester in the world.
     *
     * @return the requester position
     */
    BlockPos getRequesterPosition();
}
