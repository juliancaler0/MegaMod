package com.ultra.megamod.feature.citizen.request;

/**
 * Resolver that can fulfill requests in the colony request system.
 * Multiple resolvers are registered and sorted by priority when assigning requests.
 */
public interface IRequestResolver {

    /**
     * Returns the unique token identifying this resolver.
     *
     * @return the resolver token
     */
    IToken getResolverId();

    /**
     * Checks whether this resolver can fulfill the given request.
     *
     * @param request the request to check
     * @return true if this resolver can handle the request
     */
    boolean canResolve(IRequest request);

    /**
     * Attempts to resolve the given request.
     * Implementations may modify the request state (e.g., set to ASSIGNED or IN_PROGRESS).
     *
     * @param request the request to resolve
     */
    void resolve(IRequest request);

    /**
     * Returns the priority of this resolver. Lower values indicate higher priority.
     * When multiple resolvers can handle a request, the one with the lowest priority is chosen.
     *
     * @return the priority value
     */
    int getPriority();
}
