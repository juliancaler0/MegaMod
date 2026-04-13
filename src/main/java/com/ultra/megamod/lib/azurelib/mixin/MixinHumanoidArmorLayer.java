package com.ultra.megamod.lib.azurelib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererRegistry;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, A extends HumanoidModel<S>> {

    private static boolean azurelib$loggedOnce = false;
    private static boolean azurelib$lambdaLogged = false;

    @Invoker("getArmorModel")
    abstract A azurelibArmor$getArmorModel(S renderState, EquipmentSlot slot);

    @Inject(
        method = "renderArmorPiece",
        at = @At("HEAD"),
        cancellable = true
    )
    private void azurelibArmor$renderAzurelibModel(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        ItemStack itemStack,
        EquipmentSlot equipmentSlot,
        int packedLight,
        S renderState,
        CallbackInfo ci
    ) {
        var renderer = AzArmorRendererRegistry.getOrNull(itemStack);
        if (renderer == null) return;

        if (!azurelib$loggedOnce) {
            azurelib$loggedOnce = true;
            com.ultra.megamod.MegaMod.LOGGER.info("[AzArmor] Intercepting render for {} slot={}", itemStack.getItem(), equipmentSlot);
        }

        final A baseModel = azurelibArmor$getArmorModel(renderState, equipmentSlot);
        baseModel.setupAnim(renderState);

        final int color = itemStack.is(ItemTags.DYEABLE)
                ? ARGB.opaque(DyedItemColor.getOrDefault(itemStack, -6265536))
                : -1;

        var config = renderer.rendererPipeline().config();
        var textureLoc = config.textureLocation(Minecraft.getInstance().player, itemStack);
        RenderType renderType = RenderTypes.entityCutoutNoCull(textureLoc);

        // Capture state for deferred render
        final ItemStack stack = itemStack;
        final EquipmentSlot slot = equipmentSlot;
        final S state = renderState;
        final int light = packedLight;

        // Pass the actual poseStack — submitCustomGeometry captures it at submit time
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            try {
                if (!azurelib$lambdaLogged) {
                    azurelib$lambdaLogged = true;
                    com.ultra.megamod.MegaMod.LOGGER.info("[AzArmor] Lambda fired for {} slot={} buffer={} textureLoc={}",
                            stack.getItem(), slot, buffer != null, textureLoc);
                }

                renderer.prepForRender(Minecraft.getInstance().player, stack, slot, baseModel);
                var armorModel = renderer.rendererPipeline().armorModel();
                armorModel.setupAnim(state);

                PoseStack renderPose = new PoseStack();
                renderPose.last().pose().set(pose.pose());
                renderPose.last().normal().set(pose.normal());

                armorModel.renderAzureBuffer(renderPose, buffer, light, OverlayTexture.NO_OVERLAY, color);
            } catch (Throwable t) {
                com.ultra.megamod.MegaMod.LOGGER.error("[AzArmor] Render failed for {}: {}", stack.getItem(), t.getMessage(), t);
            }
        });

        ci.cancel();
    }
}
