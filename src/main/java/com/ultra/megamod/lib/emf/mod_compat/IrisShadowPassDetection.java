package com.ultra.megamod.lib.emf.mod_compat;

import com.ultra.megamod.lib.emf.utils.EMFUtils;
import com.ultra.megamod.lib.etf.ETF;

import java.util.Objects;

public abstract class IrisShadowPassDetection {

    public abstract boolean inShadowPass();

    private static IrisShadowPassDetection instance;
    public static IrisShadowPassDetection getInstance() {
                instance = new IrisShadowPassDetection() {
                    @Override
                    public boolean inShadowPass() {
                        return false;
                    }
                };
        return instance;
    }
}
