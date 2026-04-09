package com.ultra.megamod.feature.combat.spell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.spell.SpellSchool;
import com.ultra.megamod.feature.combat.spell.client.render.CustomLayers;
import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Multi-layer quad-based beam renderer with texture scrolling.
 * Ported 1:1 from SpellEngine's BeamRenderer.
 *
 * Renders beams as 4-face quad tubes with:
 * - Inner colored core (full alpha)
 * - Outer colored envelope (75% alpha, 1.5x width)
 * - Wide diffuse halo (33% alpha, 2x width)
 * All with animated UV scrolling for flow effect.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class BeamRenderer {

    private static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/spell_projectile.png");

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        var beams = BeamRenderState.getActiveBeams();
        if (beams.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTick = mc.getDeltaTracker().getRealtimeDeltaTicks();
        Vec3 camPos = mc.player.getEyePosition(partialTick);
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        long worldTime = mc.level.getGameTime();

        for (var beam : beams) {
            float progress = beam.progress();
            if (progress >= 1.0f) continue;

            float opacity = progress < 0.6f ? 1.0f : 1.0f - (progress - 0.6f) / 0.4f;
            SpellSchool school = schoolFromOrdinal(beam.schoolOrdinal());

            Color.IntFormat innerColor = Color.from(school.color).toIntFormat();
            Color.IntFormat outerColor = Color.from(school.color).alpha(0.75f).toIntFormat();

            float width = 0.15f;
            RenderType innerLayer = CustomLayers.beam(BEAM_TEXTURE, false, false);
            RenderType outerLayer = CustomLayers.beam(BEAM_TEXTURE, false, true);

            renderBeamInWorld(poseStack, bufferSource,
                    beam.start().x, beam.start().y, beam.start().z,
                    beam.end().x, beam.end().y, beam.end().z,
                    innerColor, outerColor, innerLayer, outerLayer,
                    width, opacity, worldTime, partialTick, 1.0f);
        }

        bufferSource.endBatch();
        poseStack.popPose();

        BeamRenderState.tick();
    }

    private static void renderBeamInWorld(PoseStack matrices, MultiBufferSource bufferSource,
                                           double startX, double startY, double startZ,
                                           double endX, double endY, double endZ,
                                           Color.IntFormat innerColor, Color.IntFormat outerColor,
                                           RenderType innerLayer, RenderType outerLayer,
                                           float width, float opacity, long time, float tickDelta, float flow) {
        matrices.pushPose();
        matrices.translate(startX, startY, startZ);

        double dx = endX - startX, dy = endY - startY, dz = endZ - startZ;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.01f) { matrices.popPose(); return; }

        dx /= length; dy /= length; dz /= length;

        float pitch = (float) Math.acos(dy);
        float yaw = (float) Math.atan2(dz, dx);
        matrices.mulPose(Axis.YP.rotationDegrees((1.5707964f - (float) yaw) * 57.295776f));
        matrices.mulPose(Axis.XP.rotationDegrees(pitch * 57.295776f));

        float absoluteTime = (float) Math.floorMod(time, 40) + tickDelta;
        matrices.mulPose(Axis.YP.rotationDegrees(absoluteTime * 2.25f - 45.0f));

        float shift = absoluteTime;
        float uvOffset = Mth.frac(shift * 0.2f - (float) Mth.floor(shift * 0.1f)) * (-flow);

        int innerA = (int) (innerColor.alpha() * opacity);
        int outerA = (int) (outerColor.alpha() * opacity);

        // Inner core
        renderBeamLayer(matrices, bufferSource.getBuffer(innerLayer),
                innerColor.red(), innerColor.green(), innerColor.blue(), innerA,
                0, length, width, 0, 1f, length, uvOffset);

        // Outer bright (1.5x)
        renderBeamLayer(matrices, bufferSource.getBuffer(outerLayer),
                outerColor.red(), outerColor.green(), outerColor.blue(), (int) (outerA * 0.75),
                0, length, width * 1.5f, 0, 1f, length, uvOffset * 0.9f);

        // Wide halo (2x)
        renderBeamLayer(matrices, bufferSource.getBuffer(outerLayer),
                outerColor.red(), outerColor.green(), outerColor.blue(), outerA / 3,
                0, length, width * 2f, 0, 1f, length, uvOffset * 0.8f);

        matrices.popPose();
    }

    private static void renderBeamLayer(PoseStack matrices, VertexConsumer vertices,
                                         int r, int g, int b, int a,
                                         float yOffset, float height, float w,
                                         float u1, float u2, float v1, float v2) {
        PoseStack.Pose pose = matrices.last();
        renderBeamFace(pose, vertices, r, g, b, a, yOffset, height, 0, w, w, 0, u1, u2, v1, v2);
        renderBeamFace(pose, vertices, r, g, b, a, yOffset, height, 0, -w, -w, 0, u1, u2, v1, v2);
        renderBeamFace(pose, vertices, r, g, b, a, yOffset, height, w, 0, 0, -w, u1, u2, v1, v2);
        renderBeamFace(pose, vertices, r, g, b, a, yOffset, height, -w, 0, 0, w, u1, u2, v1, v2);
    }

    private static void renderBeamFace(PoseStack.Pose pose, VertexConsumer vertices,
                                        int r, int g, int b, int a,
                                        float yOff, float height,
                                        float x1, float z1, float x2, float z2,
                                        float u1, float u2, float v1, float v2) {
        beamVertex(pose, vertices, r, g, b, a, height, x1, z1, u2, v1);
        beamVertex(pose, vertices, r, g, b, a, yOff, x1, z1, u2, v2);
        beamVertex(pose, vertices, r, g, b, a, yOff, x2, z2, u1, v2);
        beamVertex(pose, vertices, r, g, b, a, height, x2, z2, u1, v1);
    }

    private static void beamVertex(PoseStack.Pose pose, VertexConsumer vertices,
                                    int r, int g, int b, int a,
                                    float y, float x, float z, float u, float v) {
        vertices.addVertex(pose.pose(), x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
    }

    private static SpellSchool schoolFromOrdinal(int ordinal) {
        SpellSchool[] values = SpellSchool.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : SpellSchool.ARCANE;
    }
}
