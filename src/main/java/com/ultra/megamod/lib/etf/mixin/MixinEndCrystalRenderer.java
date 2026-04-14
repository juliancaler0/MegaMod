package com.ultra.megamod.lib.etf.mixin;

import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * {@link EndCrystalRenderer} caches its {@code RenderType} as a static field. The
 * generic {@link MixinRenderTypes} swap fires only when the {@code RenderType} is
 * being built — by the time the end crystal is rendered, the texture is already baked
 * into the static. Re-create the {@code RenderType} each frame so the swap hook fires.
 * <p>
 * Ported 1:1 from upstream ETF (1.21.11 branch).
 */
@Mixin(EndCrystalRenderer.class)
public abstract class MixinEndCrystalRenderer {

    @Shadow @Final private static Identifier END_CRYSTAL_LOCATION;

    @ModifyArg(
            method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 0),
            index = 3,
            require = 0)
    private RenderType etf$modifyTexture(RenderType cached) {
        // recreate each frame so the ETF texture swap sees this call
        return RenderTypes.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    }
}
