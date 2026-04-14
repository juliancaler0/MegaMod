package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Forces the {@link EnderDragonRenderer} to rebuild its cached {@code RenderType}s per
 * frame so the ETF texture-swap hook in {@link MixinRenderTypes} can substitute the
 * dragon texture identifier. Upstream does the same for {@code RENDER_TYPE},
 * {@code DECAL}, and {@code EYES}.
 * <p>
 * Ported 1:1 from upstream ETF (1.21.11 branch).
 */
@Mixin(EnderDragonRenderer.class)
public abstract class MixinEnderDragonRenderer extends EntityRenderer<EnderDragon, EnderDragonRenderState> {

    @Shadow @Final private static Identifier DRAGON_LOCATION;
    @Shadow @Final private static Identifier DRAGON_EYES_LOCATION;
    @Shadow @Final private static RenderType RENDER_TYPE;
    @Shadow @Final private static RenderType DECAL;
    @Shadow @Final private static RenderType EYES;

    @SuppressWarnings("unused")
    protected MixinEnderDragonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @ModifyArg(
            method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"),
            require = 0)
    private RenderType etf$returnAlteredTexture(RenderType texturedRenderLayer) {
        return remapRenderType(texturedRenderLayer);
    }

    @Unique
    private static @Nullable RenderType remapRenderType(RenderType texturedRenderLayer) {
        try {
            if (DECAL.equals(texturedRenderLayer)) {
                return RenderTypes.entityDecal(DRAGON_LOCATION);
            } else if (RENDER_TYPE.equals(texturedRenderLayer)) {
                return RenderTypes.entityCutoutNoCull(DRAGON_LOCATION);
            } else if (EYES.equals(texturedRenderLayer)) {
                return RenderTypes.eyes(DRAGON_EYES_LOCATION);
            }
        } catch (Exception e) {
            ETFUtils2.logError(e.toString());
        }
        return texturedRenderLayer;
    }
}
