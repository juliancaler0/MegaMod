package com.ultra.megamod.mixin.spellengine.client.render;

// TODO: 1.21.11 - InGameHud is now net.minecraft.client.gui.Gui
// Method signatures changed. This mixin hides offhand when spell hotbar is active
// and renders the spell HUD. NeoForge-specific invocation handles HUD rendering separately.

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class InGameHudMixin {
    // Original mixin:
    // 1. Hid offhand item when spell hotbar is showing
    // 2. Rendered spell HUD after mount health
    // Both features are handled by NeoForge GUI layer events instead
}
