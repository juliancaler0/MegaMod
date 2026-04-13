package com.ultra.megamod.feature.combat.wizards.client.effect;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.spell.client.render.CustomLayers;
import com.ultra.megamod.feature.combat.spell.client.render.LightEmission;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;

public class FrozenRenderer implements CustomModelStatusEffect.Renderer {

    private static final RenderType RENDER_LAYER = CustomLayers.spellEffect(LightEmission.RADIATE, false);

    public static final Identifier modelId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/frost_trap");

    @Override
    public void renderEffect(int amplifier, LivingEntity livingEntity, float delta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0.5, 0);
        CustomModels.render(RENDER_LAYER, Minecraft.getInstance().getItemRenderer(), modelId,
                matrixStack, vertexConsumers, light, livingEntity.getId());
        matrixStack.popPose();
    }
}
