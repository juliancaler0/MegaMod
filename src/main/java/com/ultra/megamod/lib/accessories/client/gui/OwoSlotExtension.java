package com.ultra.megamod.lib.accessories.client.gui;

/**
 * Extension interface for slot disable/enable overrides.
 * This was originally part of OWO's mixin system.
 */
public interface OwoSlotExtension {
    default boolean owo$getDisabledOverride() { return false; }
    default void owo$setDisabledOverride(boolean disabled) {}
}
