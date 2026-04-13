package com.ultra.megamod.lib.etf.config;

/**
 * Phase A stand-in for upstream ETF's {@code TConfigHandler<ETFConfig>} so that the
 * parser / selector code can use the same {@code ETF.config().getConfig()} access
 * pattern that the upstream code uses.
 * <p>
 * This shim holds a single live {@link ETFConfig} instance. Phase C will replace it
 * with a real on-disk-backed handler.
 */
public class ETFConfigHandler {

    private ETFConfig config;

    public ETFConfigHandler() {
        this.config = new ETFConfig();
    }

    public ETFConfig getConfig() {
        return config;
    }

    public void setConfig(ETFConfig newConfig) {
        if (newConfig != null) this.config = newConfig;
    }

    public ETFConfig copyOfConfig() {
        ETFConfig copy = new ETFConfig();
        copy.advanced_IncreaseCacheSizeModifier = config.advanced_IncreaseCacheSizeModifier;
        copy.optifine_allowWeirdSkipsInTrueRandom = config.optifine_allowWeirdSkipsInTrueRandom;
        return copy;
    }

    /**
     * Stub — in Phase A there is no file persistence.
     */
    public void saveToFile() {
        // no-op in Phase A
    }
}
