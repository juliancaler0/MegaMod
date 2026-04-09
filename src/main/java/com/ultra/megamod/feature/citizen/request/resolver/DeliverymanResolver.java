package com.ultra.megamod.feature.citizen.request.resolver;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.request.IRequest;
import com.ultra.megamod.feature.citizen.request.IRequestResolver;
import com.ultra.megamod.feature.citizen.request.IToken;
import com.ultra.megamod.feature.citizen.request.RequestState;
import com.ultra.megamod.feature.citizen.request.StandardToken;

/**
 * Resolves delivery requests by assigning them to a Deliveryman citizen.
 * The deliveryman will physically carry items between buildings.
 * <p>
 * Priority: 100 (medium — used when warehouse has the item but needs physical transport).
 */
public class DeliverymanResolver implements IRequestResolver {

    private final StandardToken resolverId = new StandardToken();

    @Override
    public IToken getResolverId() {
        return resolverId;
    }

    @Override
    public boolean canResolve(IRequest request) {
        // Deliveryman can resolve any delivery or pickup request type.
        // The actual citizen availability is checked at resolve time.
        // This allows requests to be queued for deliverymen even if none are immediately free.
        String desc = request.getRequestable().getDescription().toLowerCase();
        return desc.contains("delivery") || desc.contains("pickup") || desc.contains("food") || desc.contains("tool");
    }

    @Override
    public void resolve(IRequest request) {
        // Stub: in a full implementation, this finds the nearest free deliveryman
        // and assigns the request to their task queue.
        request.setState(RequestState.ASSIGNED);
        request.setResolver(resolverId);
        MegaMod.LOGGER.debug("DeliverymanResolver assigned request {}", request.getToken().getId());
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
