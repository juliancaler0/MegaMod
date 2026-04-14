package com.ultra.megamod.lib.etf.features.state;

import com.ultra.megamod.lib.etf.utils.ETFEntity;

/**
 * Interface mixed into vanilla render states so ETF (and EMF) can pull a cached
 * {@link ETFEntityRenderState} off them. Mixins land in Phase B; interface is here
 * so Phase A code compiles against the same contract as upstream.
 */
public interface HoldsETFRenderState {
    ETFEntityRenderState etf$getState();
    void etf$initState(ETFEntity entity);
}
