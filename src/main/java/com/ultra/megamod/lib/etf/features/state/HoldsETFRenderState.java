package com.ultra.megamod.lib.etf.features.state;

import com.ultra.megamod.lib.etf.utils.ETFEntity;

public interface HoldsETFRenderState {
    ETFEntityRenderState etf$getState();
    void etf$initState(ETFEntity entity);
}