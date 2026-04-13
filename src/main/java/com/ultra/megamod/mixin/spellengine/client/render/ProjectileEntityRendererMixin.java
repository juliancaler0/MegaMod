package com.ultra.megamod.mixin.spellengine.client.render;

// TODO: 1.21.11 - ProjectileEntityRenderer was restructured
// The render method signature changed. This mixin provided custom rendering for
// arrows carrying spells (using SpellProjectileRenderer).
// Needs rewrite to use the new render state pipeline.

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.renderer.entity.ArrowRenderer")
public abstract class ProjectileEntityRendererMixin {
    // Original mixin:
    // Overrode arrow rendering when the arrow carried spell data
    // to show custom spell projectile visuals instead of the default arrow
}
