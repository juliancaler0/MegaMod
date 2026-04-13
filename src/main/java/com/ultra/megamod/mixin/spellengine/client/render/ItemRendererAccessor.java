package com.ultra.megamod.mixin.spellengine.client.render;

import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * ItemRendererAccessor is kept for API compatibility.
 * In MC 1.21.11, ItemRenderer no longer has renderBakedItemModel.
 * Item rendering now uses ItemStackRenderState.submit() instead.
 */
@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
    // renderBakedItemModel was removed in MC 1.21.4+
    // Item rendering now uses ItemStackRenderState
}
