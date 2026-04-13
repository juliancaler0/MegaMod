package com.ultra.megamod.mixin.spellengine.client;

// TODO: 1.21.11 - ItemRenderer and ItemModels were restructured
// ItemModels class was removed. Item model resolution works differently now.
// This mixin provided custom model overrides for spell items.
// Commenting out until the new item model system is understood.

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.renderer.entity.ItemRenderer;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    // Original mixin injected into getModel to provide custom models for spell items
    // ItemModels class no longer exists in 1.21.11
}
