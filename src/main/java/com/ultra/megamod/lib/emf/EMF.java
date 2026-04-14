package com.ultra.megamod.lib.emf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the EMF (Entity Model Features) port.
 * <p>
 * Phase D: parser + evaluator only. No render integration.
 * <p>
 * Port of Entity_Model_Features by traben. License: LGPL v3 (see LICENSE.md).
 */
public final class EMF {
    public static final String MOD_ID = "entity_model_features";
    public static final Logger LOGGER = LoggerFactory.getLogger("Entity Model Features");

    private EMF() {
    }

    /**
     * Toggle to enable verbose parsing / model creation logs. Upstream exposes this via
     * its config object; we keep it as a plain static field until Phase E wires config.
     */
    public static boolean logModelCreationData = false;

    /**
     * Upstream honours a flag that rejects bespoke EMF syntax so packs work 1:1 under
     * OptiFine. Default: tolerant (false). Phase E may expose it through config.
     */
    public static boolean enforceOptiFineAnimSyntaxLimits = false;
}
