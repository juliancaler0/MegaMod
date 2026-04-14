package com.ultra.megamod.lib.emf.compat;

import com.ultra.megamod.lib.etf.ETF;

/**
 * Shim around Iris shadow-pass detection.
 * <p>
 * Upstream EMF uses this to short-circuit heavy animation writes during the
 * Iris shadow-mapping pass. We keep the same API so the {@code
 * animationFrameSkipDuringIrisShadowPass} config option can be honoured
 * without a hard Iris dependency — if Iris isn't installed, the detector
 * always returns {@code false} and the skip is a no-op.
 */
public abstract class IrisShadowPassDetection {

    public abstract boolean inShadowPass();

    private static IrisShadowPassDetection instance;

    public static IrisShadowPassDetection getInstance() {
        if (instance == null) {
            if (ETF.isThisModLoaded("iris") || ETF.isThisModLoaded("oculus")) {
                try {
                    instance = new IrisAwareImpl();
                } catch (Throwable t) {
                    instance = new NoOp();
                }
            } else {
                instance = new NoOp();
            }
        }
        return instance;
    }

    /** Static default for clients that never loaded Iris/Oculus. */
    private static final class NoOp extends IrisShadowPassDetection {
        @Override
        public boolean inShadowPass() {
            return false;
        }
    }

    /**
     * Reflective Iris API lookup. Keeps EMF independent of the Iris build deps.
     * If reflection fails at any point we degrade to {@code false}.
     */
    private static final class IrisAwareImpl extends IrisShadowPassDetection {
        private final java.lang.reflect.Method isRenderingShadowPass;
        private final Object irisApi;

        IrisAwareImpl() throws Throwable {
            Class<?> apiClazz = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            java.lang.reflect.Method getInstance = apiClazz.getMethod("getInstance");
            this.irisApi = getInstance.invoke(null);
            this.isRenderingShadowPass = apiClazz.getMethod("isRenderingShadowPass");
        }

        @Override
        public boolean inShadowPass() {
            try {
                Object r = isRenderingShadowPass.invoke(irisApi);
                return r instanceof Boolean b && b;
            } catch (Throwable t) {
                return false;
            }
        }
    }
}
