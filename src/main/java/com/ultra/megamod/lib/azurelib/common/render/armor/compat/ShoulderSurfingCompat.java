package com.ultra.megamod.lib.azurelib.common.render.armor.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import com.ultra.megamod.lib.azurelib.common.platform.Services;

/**
 * A utility class designed to handle interactions with the "Shoulder Surfing" mod. This class provides methods for:
 * <ul>
 * <li>Detecting whether the "Shoulder Surfing" mod is loaded,</li>
 * <li>Initializing a compatibility layer that integrates with the mod's features and</li>
 * <li>Retrieving alpha transparency values for rendering purposes.</li>
 * </ul>
 * <p>
 * The Shoulder Surfing API is not available in this version, so this compat layer
 * is stubbed out. It always returns default values (fully opaque).
 * </p>
 */
public class ShoulderSurfingCompat {

    private static boolean isLoaded = false;

    /**
     * Initializes the compatibility layer for the "Shoulder Surfing" mod. This method checks whether the "Shoulder
     * Surfing" mod is loaded using the platform-specific implementation of the {@code isModLoaded} method. If the mod
     * is detected, it sets the internal state to indicate that the compatibility layer is successfully loaded.
     */
    public static void init() {
        if (Services.PLATFORM.isModLoaded("shouldersurfing")) {
            isLoaded = true;
        }
    }

    /**
     * Determines if the compatibility layer for the "Shoulder Surfing" mod is initialized and the mod is loaded.
     *
     * @return {@code true} if the "Shoulder Surfing" mod is detected as loaded, and the compatibility layer is
     *         initialized; {@code false} otherwise.
     */
    public static boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Retrieves the alpha transparency value for the provided entity. Since the Shoulder Surfing API is not
     * available in this version, this always returns 1.0 (fully opaque).
     *
     * @param currentEntity the entity for which the alpha transparency value is being determined.
     * @return always returns 1.0 (fully opaque).
     */
    public static float getAlpha(Entity currentEntity) {
        // Shoulder Surfing mod API not available in 1.21.11 — return fully opaque
        return 1.0F;
    }

    private ShoulderSurfingCompat() { /* NO-OP */}
}
