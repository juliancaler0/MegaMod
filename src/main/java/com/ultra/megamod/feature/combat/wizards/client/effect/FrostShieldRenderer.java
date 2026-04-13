package com.ultra.megamod.feature.combat.wizards.client.effect;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.spell.client.render.CustomLayers;
import com.ultra.megamod.feature.combat.spell.client.render.LightEmission;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

public class FrostShieldRenderer implements CustomModelStatusEffect.Renderer {
    public static final Identifier modelId_base = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/frost_shield_base");
    public static final Identifier modelId_overlay = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/frost_shield_overlay");

    private static final RenderType BASE_RENDER_LAYER = RenderTypes.entityTranslucent(
            Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"));
    private static final RenderType OVERLAY_RENDER_LAYER = CustomLayers.spellEffect(LightEmission.RADIATE, false);

    @Override
    public void renderEffect(int amplifier, LivingEntity livingEntity, float delta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        float yOffset = 0.51F; // y + 0.01 to avoid Y fighting
        matrixStack.pushPose();
        matrixStack.translate(0, yOffset, 0);
        CustomModels.render(BASE_RENDER_LAYER, Minecraft.getInstance().getItemRenderer(), modelId_base,
                matrixStack, vertexConsumers, light, livingEntity.getId());
        matrixStack.popPose();

        float overlayScale = 1.05F;
        matrixStack.pushPose();
        matrixStack.translate(0, yOffset, 0);
        matrixStack.scale(overlayScale, overlayScale, overlayScale);
        CustomModels.render(OVERLAY_RENDER_LAYER, Minecraft.getInstance().getItemRenderer(), modelId_overlay,
                matrixStack, vertexConsumers, light, livingEntity.getId());
        matrixStack.popPose();
    }
}
