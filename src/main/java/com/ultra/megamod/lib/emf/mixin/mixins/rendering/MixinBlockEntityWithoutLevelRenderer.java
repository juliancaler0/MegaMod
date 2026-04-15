package com.ultra.megamod.lib.emf.mixin.mixins.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;


import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class MixinBlockEntityWithoutLevelRenderer {


    @Shadow
    @Nullable
    private SpecialModelRenderer<?> specialRenderer;

    private static final String RENDER = "submit";

    @Inject(method = RENDER,
            at = @At(value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/renderer/special/SpecialModelRenderer;submit(Ljava/lang/Object;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V"
                    , shift = At.Shift.BEFORE))
    private void emf$setRenderFactory(CallbackInfo ci) {
        if (specialRenderer instanceof SkullSpecialRenderer) {
            EMFAnimationEntityContext.setLayerFactory(
                    RenderTypes::entityCutoutNoCullZOffset
            );
        }

        EMFManager.getInstance().entityRenderCount++;
        setPlayerEntity();
    }


    @Inject(method = RENDER, at = @At(value = "RETURN"))
    private void emf$reset(final CallbackInfo ci) {
        EMFAnimationEntityContext.reset();
    }


    @Unique
    private void setPlayerEntity() {
        if (Minecraft.getInstance().player == null) return;
        ETFEntityRenderState state = ETFEntityRenderState.forEntity( (ETFEntity) Minecraft.getInstance().player);
        // ETFRenderContext.setCurrentEntity(state); // todo was this a mistake? Should this be EMF only? ignoring for now during rewrite
        EMFAnimationEntityContext.setCurrentEntityNoIteration((EMFEntityRenderState) state);
    }
}

