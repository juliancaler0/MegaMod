package com.ultra.megamod.feature.combat.paladins.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.entity.BannerEntity;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public class BannerEntityRenderer extends EntityRenderer<BannerEntity, BannerEntityRenderer.BannerRenderState> {
    private final ItemRenderer itemRenderer;

    public BannerEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    public static final Identifier modelId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_effect/battle_banner");
    private static final RenderType layer =
            RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

    private static Color.IntFormat innerColor = Color.IntFormat.fromLongRGBA(0xFF0000FFL);
    private static Color.IntFormat outerColor = Color.IntFormat.fromLongRGBA(0xFFCC66FFL);

    @Override
    public BannerRenderState createRenderState() {
        return new BannerRenderState();
    }

    @Override
    public void extractRenderState(BannerEntity entity, BannerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getYRot();
        state.entityId = entity.getId();
    }

    public void render(BannerRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-1F * state.yRot + 180F));
        poseStack.translate(0, 0.5, 0);

        CustomModels.render(layer, itemRenderer, modelId, poseStack, bufferSource, light, state.entityId);

        poseStack.translate(0.5, 0, 0.5);

        poseStack.popPose();
    }

    public static class BannerRenderState extends EntityRenderState {
        public float yRot;
        public int entityId;
    }
}
