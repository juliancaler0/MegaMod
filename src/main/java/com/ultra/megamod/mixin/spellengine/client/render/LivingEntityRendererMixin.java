package com.ultra.megamod.mixin.spellengine.client.render;

// TODO: 1.21.11 - LivingEntityRenderer.render() signature changed completely
// Now uses render states instead of entity params directly.
// This mixin added spin animation during channeling and rendered custom status effect models.
// Needs rewrite to use the new render state system.

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    // Original mixin:
    // 1. Added rotation during channeled spell casting (render HEAD)
    // 2. Rendered custom model status effects (render TAIL)
    // Both need adaptation to the new render state pipeline
}
