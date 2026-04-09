package com.ultra.megamod.feature.dungeons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.dungeons.client.model.KunaiModel;
import com.ultra.megamod.feature.dungeons.entity.KunaiEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class KunaiRenderer extends EntityRenderer<KunaiEntity, KunaiRenderer.KunaiRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("megamod", "textures/entity/kunai.png");
    private final KunaiModel model;

    public KunaiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new KunaiModel(context.bakeLayer(KunaiModel.LAYER_LOCATION));
    }

    public void render(KunaiRenderState state, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(state.partialTick, state.yRotO, state.yRot) - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(state.partialTick, state.xRotO, state.xRot)));
        VertexConsumer vb = bufferIn.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vb, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @Override
    public KunaiRenderState createRenderState() {
        return new KunaiRenderState();
    }

    @Override
    public void extractRenderState(KunaiEntity entity, KunaiRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.partialTick = partialTick;
        state.yRot = entity.getYRot();
        state.yRotO = entity.yRotO;
        state.xRot = entity.getXRot();
        state.xRotO = entity.xRotO;
    }

    public static class KunaiRenderState extends EntityRenderState {
        public float partialTick;
        public float yRot;
        public float yRotO;
        public float xRot;
        public float xRotO;
    }
}
