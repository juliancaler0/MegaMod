package com.ultra.megamod.feature.combat.archers.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.render.CustomLayers;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import com.ultra.megamod.lib.spellengine.api.render.LightEmission;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.math.Axis;

public class HuntersMarkRenderer implements CustomModelStatusEffect.Renderer {
    public static final Identifier modelId = Identifier.fromNamespaceAndPath(ArchersMod.ID, "spell_effect/hunters_mark");
    private static final RenderType GLOWING_RENDER_LAYER = CustomLayers.spellEffect(LightEmission.GLOW, false);

    @Override
    public void renderEffect(int amplifier, LivingEntity livingEntity, float delta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        if (livingEntity.getHealth() <= 0 || !livingEntity.isAlive()) { return; }
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var direction = camera.position().subtract(livingEntity.position()).normalize().scale(livingEntity.getBbWidth() * 0.5F);

        matrixStack.pushPose();
        var verticalOffset = (livingEntity.getBbHeight() / livingEntity.getScale()) * 0.75F;
        matrixStack.translate(direction.x, verticalOffset, direction.z);

        matrixStack.mulPose(Axis.YP.rotationDegrees(180F + (float) Math.toDegrees(Math.atan2(direction.x, direction.z))));
        // Use player entity's pitch since Camera no longer exposes getXRot directly
        var player = Minecraft.getInstance().player;
        float xRot = player != null ? player.getXRot() : 0F;
        matrixStack.mulPose(Axis.XP.rotationDegrees(xRot));

        CustomModels.render(GLOWING_RENDER_LAYER, itemRenderer, modelId,
                matrixStack, vertexConsumers, light, livingEntity.getId());

        matrixStack.popPose();
    }
}
