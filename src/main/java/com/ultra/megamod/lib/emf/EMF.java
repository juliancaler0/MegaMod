package com.ultra.megamod.lib.emf;

import com.ultra.megamod.lib.emf.config.EMFConfigHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the EMF (Entity Model Features) port.
 * <p>
 * Phase D: parser + evaluator only. Phase E: render mixins wired up. Phase F:
 * config + debug + API + UUID variant cache + texture redirect + mod compat.
 * <p>
 * Port of Entity_Model_Features by traben. License: LGPL v3 (see LICENSE.md).
 */
public final class EMF {
    public static final String MOD_ID = "entity_model_features";
    public static final Logger LOGGER = LoggerFactory.getLogger("Entity Model Features");

    private EMF() {
    }

    /**
     * Toggle to enable verbose parsing / model creation logs. Mirrored from
     * {@link com.ultra.megamod.lib.emf.config.EMFConfig#logModelCreationData} by
     * {@link EMFConfigHandler} so hot code paths avoid a config lookup.
     */
    public static boolean logModelCreationData = false;

    /**
     * Upstream honours a flag that rejects bespoke EMF syntax so packs work 1:1 under
     * OptiFine. Default: tolerant (false). Mirrored from config.
     */
    public static boolean enforceOptiFineAnimSyntaxLimits = false;

    /**
     * Upstream sets this during the first resource-reload so mixin hot-paths know
     * to cheaply no-op. Kept as a public flag so our mixins can read it.
     */
    public static volatile boolean isLoadingPhase = false;

    private static EMFConfigHandler CONFIG = null;

    public static EMFConfigHandler config() {
        if (CONFIG == null) {
            CONFIG = new EMFConfigHandler();
        }
        return CONFIG;
    }
}
