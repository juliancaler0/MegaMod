package com.ultra.megamod.lib.accessories.misc;

import org.jetbrains.annotations.ApiStatus;

/**
 * In 1.21.11, game rules can't be registered after the built-in ones (registry freezes early).
 * Using a simple config flag instead. The keepInventory game rule already covers most cases.
 */
@ApiStatus.Internal
public class AccessoriesGameRules {

    // Simple config flag replacing the game rule
    public static boolean keepAccessoryInventory = false;
}
