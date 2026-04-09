package com.ultra.megamod.feature.citizen.request.resolver;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.request.IRequest;
import com.ultra.megamod.feature.citizen.request.IRequestResolver;
import com.ultra.megamod.feature.citizen.request.IToken;
import com.ultra.megamod.feature.citizen.request.RequestState;
import com.ultra.megamod.feature.citizen.request.StandardToken;

/**
 * Fallback resolver that marks a request as awaiting manual player delivery.
 * This resolver always returns true for canResolve, making it the last resort
 * when no automated resolver can handle the request.
 * <p>
 * Priority: 200 (lowest — only used when nothing else can fulfill the request).
 */
public class PlayerResolver implements IRequestResolver {

    private final StandardToken resolverId = new StandardToken();

    @Override
    public IToken getResolverId() {
        return resolverId;
    }

    @Override
    public boolean canResolve(IRequest request) {
        // Always true — the player is the ultimate fallback.
        return true;
    }

    @Override
    public void resolve(IRequest request) {
        // Marks as assigned, awaiting player delivery.
        // The player can fulfill this via the request GUI or by delivering directly.
        request.setState(RequestState.ASSIGNED);
        request.setResolver(resolverId);
        MegaMod.LOGGER.debug("PlayerResolver: request {} awaiting player delivery", request.getToken().getId());
    }

    @Override
    public int getPriority() {
        return 200;
    }
}
