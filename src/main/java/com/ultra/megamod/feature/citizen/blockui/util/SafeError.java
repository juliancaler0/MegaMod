package com.ultra.megamod.feature.citizen.blockui.util;

import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for throwing errors which is safe during production.
 */
public class SafeError
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeError.class);

    /**
     * Safe error throw call that only throws an exception during development, but logs an error in production instead so no crashes to desktop may occur.
     *
     * @param exception the exception instance.
     */
    public static void throwInDev(final RuntimeException exception)
    {
        // In development, throw; in production, log only.
        // FMLEnvironment.production was removed in 1.21.11; always throw in dev.
        // Use a runtime check via class loading to detect production.
        try {
            // If assertions are enabled (dev), throw the exception
            assert false : "dev-check";
            // Assertions disabled (production) - just log
            LOGGER.error(exception.getMessage(), exception);
        } catch (AssertionError ae) {
            throw exception;
        }
    }
}
