package com.ultra.megamod.feature.citizen.request;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Represents a single request instance in the colony request system.
 * Tracks the full lifecycle from creation through resolution or cancellation.
 */
public interface IRequest {

    /**
     * Returns the unique token for this request.
     *
     * @return the request token
     */
    IToken getToken();

    /**
     * Returns the requester that created this request.
     *
     * @return the requester
     */
    IRequester getRequester();

    /**
     * Returns what is being requested.
     *
     * @return the requestable
     */
    IRequestable getRequestable();

    /**
     * Returns the current state of this request.
     *
     * @return the request state
     */
    RequestState getState();

    /**
     * Sets the state of this request.
     *
     * @param state the new state
     */
    void setState(RequestState state);

    /**
     * Returns the token of the resolver assigned to this request, or null if unassigned.
     *
     * @return the resolver token, or null
     */
    @Nullable
    IToken getResolverId();

    /**
     * Sets the resolver assigned to this request.
     *
     * @param resolverId the resolver token
     */
    void setResolver(IToken resolverId);

    /**
     * Returns the delivered item stack, or null if not yet delivered.
     *
     * @return the delivery item, or null
     */
    @Nullable
    ItemStack getDelivery();

    /**
     * Sets the delivered item stack.
     *
     * @param delivery the delivered item
     */
    void setDelivery(ItemStack delivery);
}
