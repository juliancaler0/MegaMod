package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import com.mojang.math.Axis;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import com.ultra.megamod.lib.spellengine.entity.SpellCloud;

public class SpellCloudRenderer extends EntityRenderer<SpellCloud, SpellCloudRenderer.SpellCloudRenderState> {

    public SpellCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SpellCloudRenderState createRenderState() {
        return new SpellCloudRenderState();
    }

    @Override
    public void extractRenderState(SpellCloud entity, SpellCloudRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.tickDelta = partialTick;
    }

    public void render(SpellCloudRenderState state, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
        // TODO: Cloud model rendering requires adaptation to 1.21.11 rendering pipeline
        // The SpellCloud visual effect is cosmetic; particle effects still work.
    }

    public static class SpellCloudRenderState extends EntityRenderState {
        public SpellCloud entity;
        public float tickDelta;
    }
}
