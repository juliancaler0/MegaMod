package com.ultra.megamod.lib.accessories.client.gui;

/**
 * Interface for accessing slot positions. This was originally an OWO mixin accessor.
 * Slot x/y fields are public in NeoForge, so we can use direct access.
 */
public interface SlotAccessor {
    void owo$setX(int x);
    void owo$setY(int y);
}
