package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFSprite;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Painting texture swap.
 * <p>
 * Paintings are drawn from a {@link TextureAtlasSprite} rather than a {@code RenderType},
 * so our {@link MixinRenderTypes} texture-swap hook doesn't fire. Instead we ModifyVariable
 * on the {@code TextureAtlasSprite} locals inside the painting renderer's submit method,
 * swapping them for the {@link ETFSprite#getSpriteVariant() ETF variant} sprite.
 */
@Mixin(PaintingRenderer.class)
public abstract class MixinPaintingRenderer {

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyVariable(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/PaintingRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0)
    private TextureAtlasSprite etf$swapPaintingSprite(TextureAtlasSprite original,
                                                      PaintingRenderState state) {
        if (original == null) return null;
        ETFEntityRenderState etfState = state instanceof HoldsETFRenderState h ? h.etf$getState() : null;
        if (etfState == null) return original;
        try {
            // Atlas sprites reference the source png via their contents; we don't have a
            // direct handle, but we can derive a plausible identifier from the sprite name.
            Identifier spriteId = original.contents().name();
            // Vanilla atlas-prefixes are stripped here because the actual resource id is:
            //   textures/painting/<sprite>.png
            Identifier texId = ETFUtils2.res(spriteId.getNamespace(),
                    "textures/painting/" + spriteId.getPath() + ".png");
            ETFTexture etfTexture = ETFManager.getInstance().getETFTextureVariant(texId, etfState);
            if (etfTexture == null || etfTexture.getTextureIdentifier(etfState).equals(texId)) {
                return original;
            }
            return new ETFSprite(original, etfTexture, false).getSpriteVariant();
        } catch (Exception e) {
            return original;
        }
    }
}
