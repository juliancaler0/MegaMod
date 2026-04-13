package com.ultra.megamod.lib.azurelib.common.util;

/**
 * Helper class for various AzureLib-specific functions.
 */
public record AzureLibUtil() {

    public static <T> T self(Object object) {
        return (T) object;
    }
}
