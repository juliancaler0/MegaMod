package com.ultra.megamod.mixin.spellengine.client;

// TODO: 1.21.11 - EntityRenderer API changed significantly (render states, shouldRender signature)
// This mixin forces beam-casting entities to always render even if outside frustum.
// Commenting out body until the correct method signature is identified.

import net.minecraft.world.entity.Entity;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.entity.EntityRenderer;

@Mixin(EntityRenderer.class)
public class EntityRenderMixin {
    // Original mixin injected into shouldRender to force rendering of beam-casting entities
    // Target method signature changed in 1.21.11
}
