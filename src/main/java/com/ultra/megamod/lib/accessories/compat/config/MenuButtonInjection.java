package com.ultra.megamod.lib.accessories.compat.config;

import net.minecraft.resources.Identifier;

/**
 * Config entry for menu button positioning.
 */
public record MenuButtonInjection(Identifier targetScreen, int xOffset, int yOffset) {
    public Identifier menuType() { return targetScreen; }
}
