package com.ultra.megamod.lib.etf.config;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperties;

/**
 * Minimal Phase A stub of the ETF config object.
 * <p>
 * Upstream ETF ships a large {@code ETFConfig} with dozens of toggles persisted via
 * {@code TConfigHandler}. The full config drives render behaviour (emissive, enchant,
 * skin features, debug overlay) which is not landing until later phases. This stub
 * exposes only the knobs that the parser / predicate / selector layer actually reads:
 * <ul>
 *   <li>{@link #isPropertyDisabled(RandomProperties.RandomPropertyFactory)} — defaults false</li>
 *   <li>{@link #canPropertyUpdate(RandomProperties.RandomPropertyFactory)} — defaults true</li>
 *   <li>{@link #advanced_IncreaseCacheSizeModifier} — defaults 1 (no scaling)</li>
 *   <li>{@link #optifine_allowWeirdSkipsInTrueRandom} — defaults false</li>
 * </ul>
 * Phase C will replace this with a real config backed by a JSON file.
 */
public class ETFConfig {

    public double advanced_IncreaseCacheSizeModifier = 1.0;

    public boolean optifine_allowWeirdSkipsInTrueRandom = false;

    /**
     * Whether the given registered property should be skipped by the parser. Always false in the
     * Phase A stub — all upstream predicates are enabled.
     */
    public boolean isPropertyDisabled(RandomProperties.RandomPropertyFactory factory) {
        return false;
    }

    /**
     * Whether the given registered property can re-evaluate over time. In upstream ETF this is a
     * user toggle; the stub mirrors the upstream default (derived from the property's
     * spawn-lock flag).
     */
    public boolean canPropertyUpdate(RandomProperties.RandomPropertyFactory factory) {
        return factory.updatesOverTime();
    }
}
