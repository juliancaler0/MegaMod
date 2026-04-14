package com.ultra.megamod.lib.emf;

/**
 * Base exception type for EMF parsing / evaluation errors.
 * <p>
 * Upstream records exceptions against the active model for the debug HUD to surface;
 * our Phase D port just carries the message, because the HUD integration is deferred
 * to Phase E. The {@link #record()} method is a no-op stub that matches the upstream
 * surface area so callers don't need rewrites later.
 */
public class EMFException extends RuntimeException {

    public EMFException(String message) {
        super(message);
    }

    public EMFException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Matches {@code EMFException#record()} upstream. Intended for future HUD wiring.
     */
    public void record() {
        EMF.LOGGER.warn(getMessage());
    }

    public static EMFException recordException(RuntimeException e) {
        EMF.LOGGER.warn(e.getMessage());
        return new EMFException(e.getMessage(), e);
    }
}
