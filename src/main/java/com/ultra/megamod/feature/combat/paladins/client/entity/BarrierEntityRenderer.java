package com.ultra.megamod.feature.combat.paladins.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.entity.BarrierEntity;
import com.ultra.megamod.lib.spellengine.client.compatibility.ShaderCompatibility;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class BarrierEntityRenderer extends EntityRenderer<BarrierEntity, BarrierEntityRenderer.BarrierRenderState> {
    public static final Identifier blankTextureId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "item/barrier");
    public static final List<BarrierEntity> activeBarriers = new ArrayList<>();

    private static final int[] LIGHT_UP_ORDER = {0, 2, 8, 6, 4, 3, 9, 1, 5, 10, 7, 11};

    public static void setup() {
        // Event subscription is automatic via @EventBusSubscriber
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        renderAllInWorld(event.getPoseStack(), bufferSource, Minecraft.getInstance().gameRenderer.getMainCamera(),
                LightTexture.FULL_BRIGHT, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    public BarrierEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BarrierRenderState createRenderState() {
        return new BarrierRenderState();
    }

    @Override
    public void extractRenderState(BarrierEntity entity, BarrierRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isAlive = entity.isAlive();
        state.barrierEntity = entity;
    }

    public void render(BarrierRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (state.isAlive && state.barrierEntity != null) {
            activeBarriers.add(state.barrierEntity);
        }
    }

    public static void renderAllInWorld(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, int light, float tickDelta) {
        poseStack.pushPose();
        Vec3 camPos = camera.position();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        var config = ShaderCompatibility.isShaderPackInUse() ? Config.IRIS : Config.VANILLA;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(config.layer());
        for (BarrierEntity entity : activeBarriers) {
            poseStack.pushPose();
            poseStack.translate(entity.getX(), entity.getY() + 1, entity.getZ());
            renderShield(entity, poseStack, vertexConsumer, light, tickDelta, config);
            poseStack.popPose();
        }
        bufferSource.endBatch();
        poseStack.popPose();
        activeBarriers.clear();
    }

    private record Config(
            RenderType layer,
            float red,
            float green,
            float blue,
            float alpha,
            float panelFlashAlpha,
            float expirationPulseAlpha) {

        private static final Color shield = Color.from(0xffcc66);

        public static final Config VANILLA = new Config(
                RenderTypes.beaconBeam(TextureAtlas.LOCATION_BLOCKS, true),
                shield.red(), shield.green(), shield.blue(), 0.8f, 0.9f, 1f);

        public static final Config IRIS = new Config(
                RenderTypes.beaconBeam(TextureAtlas.LOCATION_BLOCKS, false),
                shield.red(), shield.green(), shield.blue(), 0.5f, 1f, 0.8f);
    }

    public static void renderShield(BarrierEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int light, float tickDelta, Config config) {
        var entry = entity.getSpellEntry();
        if (entry == null) {
            return;
        }
        var spell = entry.value();

        float radius = spell.range * 0.8f;
        float zSlant = (float) (Math.PI / 8f);
        float size = (radius * Mth.sqrt(3f)) / 3f;
        float offset = radius * (Mth.sin(zSlant) + 1);

        int overlayUV = OverlayTexture.NO_OVERLAY;

        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(blankTextureId);
        float u1 = sprite.getU0();
        float u2 = sprite.getU1();
        float v1 = sprite.getV0();
        float v2 = sprite.getV1();

        double fullTime = entity.level().getGameTime() / 20d;
        long time = entity.level().getGameTime() / 20;
        double delta = (fullTime - time) * 2;
        if (delta > 1) delta = 2 - delta;
        delta = 1 - Math.pow(1 - delta, 4);

        for (int m = 0; m < 2; m++) {
            for (int i = 0; i < 6; i++) {
                poseStack.pushPose();
                if (m == 0) poseStack.mulPose(Axis.XP.rotation((float) Math.PI));
                poseStack.translate(offset, 0, 0);
                // Rotate around pivot point (-offset, 0, 0)
                poseStack.translate(-offset, 0, 0);
                poseStack.mulPose(Axis.YP.rotation((float) (i / 3f * Math.PI)));
                poseStack.translate(offset, 0, 0);
                poseStack.mulPose(Axis.ZP.rotation(zSlant));

                float r = config.red();
                float g = config.green();
                float b = config.blue();
                float alpha = config.alpha();

                if (entity.tickCount >= entity.getTimeToLive() - entity.expirationDuration()) {
                    int relAge = entity.getTimeToLive() - entity.expirationDuration() - entity.tickCount;
                    alpha = config.expirationPulseAlpha * Math.abs(Mth.cos((float) ((relAge * 1.25f) / 10f * Math.PI)));
                } else if (time % 12 == LIGHT_UP_ORDER[i + (m * 6)]) {
                    var glow = (float) (0.5f * delta);
                    r = blend(r, 1f, glow);
                    g = blend(g, 1f, glow);
                    b = blend(b, 1f, glow);
                    alpha = blend(alpha, config.panelFlashAlpha(), glow);
                }

                Matrix4f matrix = new Matrix4f(poseStack.last().pose());
                var matrixEntry = poseStack.last();
                vertexConsumer.addVertex(matrix, 0, radius, -size).setColor(r, g, b, 0f).setUv(u1, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, 0, -size).setColor(r, g, b, alpha).setUv(u1, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, 0, size).setColor(r, g, b, alpha).setUv(u2, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, radius, size).setColor(r, g, b, 0f).setUv(u2, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);

                vertexConsumer.addVertex(matrix, 0, radius, size).setColor(r, g, b, 0f).setUv(u1, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, 0, size).setColor(r, g, b, alpha).setUv(u1, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, 0, -size).setColor(r, g, b, alpha).setUv(u2, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, radius, -size).setColor(r, g, b, 0f).setUv(u2, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);

                poseStack.popPose();
                poseStack.pushPose();
                Matrix4f newMatrix = poseStack.last().pose();
                if (m == 0) poseStack.mulPose(Axis.XP.rotation((float) Math.PI));
                poseStack.translate(offset, 0, 0);
                // Rotate around pivot point (-offset, 0, 0)
                poseStack.translate(-offset, 0, 0);
                poseStack.mulPose(Axis.YP.rotation((float) ((i - 1) / 3f * Math.PI)));
                poseStack.translate(offset, 0, 0);
                poseStack.mulPose(Axis.ZP.rotation(zSlant));

                vertexConsumer.addVertex(matrix, 0, radius, size).setColor(r, g, b, 0f).setUv(u2, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, 0, size).setColor(r, g, b, alpha).setUv(u2, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(newMatrix, 0, 0, -size).setColor(r, g, b, alpha).setUv(u1, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, radius, size).setColor(r, g, b, 0f).setUv(u1, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);

                vertexConsumer.addVertex(matrix, 0, radius, size).setColor(r, g, b, 0f).setUv(u2, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(newMatrix, 0, 0, -size).setColor(r, g, b, alpha).setUv(u1, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, 0, size).setColor(r, g, b, alpha).setUv(u2, v1).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                vertexConsumer.addVertex(matrix, 0, radius, size).setColor(r, g, b, 0f).setUv(u1, v2).setOverlay(overlayUV).setLight(light).setNormal(matrixEntry, 0, 0, 0);
                poseStack.popPose();
            }
        }
    }

    public static float blend(float min, float max, float delta) {
        return min + (max - min) * delta;
    }

    public static class BarrierRenderState extends EntityRenderState {
        public boolean isAlive;
        public BarrierEntity barrierEntity;
    }
}
