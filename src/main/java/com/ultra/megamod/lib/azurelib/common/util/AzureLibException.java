package com.ultra.megamod.lib.azurelib.common.util;

import net.minecraft.resources.Identifier;

/**
 * Generic {@link Exception} wrapper for AzureLib.<br>
 * Mostly just serves as a marker for internal error handling.
 */
public class AzureLibException extends RuntimeException {

    public AzureLibException(Identifier fileLocation, String message) {
        super(fileLocation + ": " + message);
    }

    public AzureLibException(String message, Throwable cause) {
        super(message, cause);
    }

    public AzureLibException(String message) {
        super(message);
    }

    public AzureLibException(Throwable cause) {
        super(cause);
    }
}
