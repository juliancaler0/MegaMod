package com.ultra.megamod.lib.spellengine.api.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;

import java.util.List;

public class OrbitingEffectRenderer implements CustomModelStatusEffect.Renderer {
    public record Model(RenderType layer, Identifier modelId) { }
    private List<Model> models;
    private float scale;
    private float horizontalOffset;
    protected float orbitingSpeed = 2.25F; // Speed of orbiting effect

    public OrbitingEffectRenderer(List<Model> models, float scale, float horizontalOffset) {
        this.models = models;
        this.scale = scale;
        this.horizontalOffset = horizontalOffset;
    }

    @Override
    public void renderEffect(int amplifier, LivingEntity livingEntity, float delta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        matrixStack.pushPose();
        var time = livingEntity.tickCount + delta;

        var initialAngle = time * orbitingSpeed - 45.0F;
        var entityScale = livingEntity.getScale();
        var horizontalOffset = this.horizontalOffset * livingEntity.getScale();
        var verticalOffset = livingEntity.getBbHeight() / (2F * entityScale);
        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        var stacks = amplifier + 1;
        var turnAngle = 360F / stacks;
        for (int i = 0; i < stacks; i++) {
            var angle = initialAngle + turnAngle * i;
            renderModel(matrixStack, scale, verticalOffset, horizontalOffset, angle, itemRenderer, vertexConsumers, light, livingEntity);
        }

        matrixStack.popPose();
    }

    private void renderModel(PoseStack matrixStack, float scale, float verticalOffset, float horizontalOffset, float rotation,
                               ItemRenderer itemRenderer, MultiBufferSource vertexConsumers, int light, LivingEntity livingEntity) {
        matrixStack.pushPose();

        matrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        matrixStack.translate(0, verticalOffset, -horizontalOffset);
        matrixStack.scale(scale, scale, scale);

        for(var model: models) {
            matrixStack.pushPose();
            CustomModels.render(model.layer, itemRenderer, model.modelId,
                    matrixStack, vertexConsumers, light, livingEntity.getId());
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }
}
