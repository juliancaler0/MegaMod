package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * The central texture-swap hook.
 * <p>
 * Every entity-related {@code RenderType} factory in {@link RenderTypes} takes an
 * {@link Identifier} as its first argument. We intercept that argument at HEAD and
 * substitute it with the ETF variant identifier (if any) for the currently-rendering
 * entity. Downstream code composes the {@code RenderType} with the swapped texture, and
 * the swap flows through the rest of the rendering pipeline naturally.
 * <p>
 * In 1.21.11 MC moved these factory methods from {@code RenderType} to
 * {@code RenderTypes}. In 1.21.10- they lived on {@code RenderType}. Ported from upstream
 * ETF, keeping only the 1.21.11 form.
 */
@Mixin(RenderTypes.class)
public abstract class MixinRenderTypes {

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyVariable(
            method = {
                    "entitySolid",
                    "entitySolidZOffsetForward",
                    "eyes",
                    "energySwirl",
                    "entitySmoothCutout",
                    "itemEntityTranslucentCull",
                    "entityCutout",
                    "entityCutoutNoCull(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;",
                    "entityCutoutNoCullZOffset(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;",
                    "entityDecal",
                    "entityNoOutline",
                    "entityTranslucent(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;",
                    "entityTranslucentEmissive(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;",
                    "armorCutoutNoCull",
                    "armorTranslucent",
                    "entityShadow",
                    "entityTranslucentCullItemTarget",
                    "entityCutoutZOffset(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;",
                    "entityCutoutDissolve"
            },
            at = @At(value = "HEAD"),
            index = 0,
            argsOnly = true,
            require = 0)
    private static Identifier etf$mixinAllEntityLayers(Identifier value) {
        if (value == null) return null;
        return ETFUtils2.getETFVariantNotNullForInjector(value);
    }
}
