package com.ultra.megamod.feature.combat.archers.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public class RootsRenderer implements CustomModelStatusEffect.Renderer {

    public static final Identifier modelId = Identifier.fromNamespaceAndPath(ArchersMod.ID, "spell_effect/entangling_roots");

    @Override
    public void renderEffect(int amplifier, LivingEntity livingEntity, float delta, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0.5, 0);
        CustomModels.render(Sheets.solidBlockSheet(), Minecraft.getInstance().getItemRenderer(), modelId,
                matrixStack, vertexConsumers, light, livingEntity.getId());
        matrixStack.popPose();
    }
}
