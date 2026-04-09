package com.ultra.megamod.feature.combat.spell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.combat.spell.SpellCloudEntity;
import com.ultra.megamod.feature.combat.spell.SpellSchool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Renders spell clouds as translucent circular discs on the ground with pulsing opacity
 * and school-colored ambient particles. The disc radius matches the synched cloud radius.
 */
public class SpellCloudRenderer extends EntityRenderer<SpellCloudEntity, SpellCloudRenderer.SpellCloudRenderState> {

    private static final Identifier CLOUD_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/spell_cloud.png");

    /** Number of segments used to approximate the circle. More = smoother. */
    private static final int CIRCLE_SEGMENTS = 32;

    public SpellCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SpellCloudRenderState createRenderState() {
        return new SpellCloudRenderState();
    }

    @Override
    public void extractRenderState(SpellCloudEntity entity, SpellCloudRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.schoolOrdinal = entity.getSchoolOrdinal();
        state.radius = entity.getCloudRadius();
        state.age = entity.tickCount;
        state.partialTick = partialTick;
        state.posX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        state.posY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        state.posZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
    }

    public void render(SpellCloudRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        SpellSchool school = schoolFromOrdinal(state.schoolOrdinal);
        int color = school.color;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float time = state.age + state.partialTick;
        float radius = Math.max(0.5f, state.radius);

        // Pulsing opacity: cycles between 0.25 and 0.55 alpha
        float pulseAlpha = 0.40f + 0.15f * Mth.sin(time * 0.08f);
        int baseAlpha = (int)(pulseAlpha * 255);

        poseStack.pushPose();

        // Slightly above ground to prevent z-fighting
        poseStack.translate(0, 0.06, 0);

        // Slow rotation for visual movement
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 0.5f));

        // === Render the ground disc as a circle of triangle-fan quads ===
        // Use entityCutoutNoCull for transparency support (rendered double-sided)
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.entityCutoutNoCull(CLOUD_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        int light = 15728880; // full brightness

        // Triangle fan: center + ring vertices, rendered as quads (groups of 4)
        // Each "slice" is a quad: center, edge[i], edge[i+1], center-duplicate
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            float angle0 = (float)(2 * Math.PI * i / CIRCLE_SEGMENTS);
            float angle1 = (float)(2 * Math.PI * (i + 1) / CIRCLE_SEGMENTS);

            float x0 = Mth.cos(angle0) * radius;
            float z0 = Mth.sin(angle0) * radius;
            float x1 = Mth.cos(angle1) * radius;
            float z1 = Mth.sin(angle1) * radius;

            // UV: map circle to texture coordinates
            float u0 = 0.5f + Mth.cos(angle0) * 0.5f;
            float v0 = 0.5f + Mth.sin(angle0) * 0.5f;
            float u1 = 0.5f + Mth.cos(angle1) * 0.5f;
            float v1 = 0.5f + Mth.sin(angle1) * 0.5f;

            // Edge alpha fades out (center is brighter, edges more transparent)
            int edgeAlpha = (int)(baseAlpha * 0.5f);

            // Vertex 1: center
            consumer.addVertex(matrix, 0, 0, 0)
                    .setColor(r, g, b, baseAlpha)
                    .setUv(0.5f, 0.5f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);

            // Vertex 2: edge point i
            consumer.addVertex(matrix, x0, 0, z0)
                    .setColor(r, g, b, edgeAlpha)
                    .setUv(u0, v0)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);

            // Vertex 3: edge point i+1
            consumer.addVertex(matrix, x1, 0, z1)
                    .setColor(r, g, b, edgeAlpha)
                    .setUv(u1, v1)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);

            // Vertex 4: duplicate of center (quads require 4 vertices)
            consumer.addVertex(matrix, 0, 0, 0)
                    .setColor(r, g, b, baseAlpha)
                    .setUv(0.5f, 0.5f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);
        }

        // === Second ring layer: outer glow ring ===
        float outerRadius = radius * 1.15f;
        int outerAlpha = (int)(baseAlpha * 0.25f);
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            float angle0 = (float)(2 * Math.PI * i / CIRCLE_SEGMENTS);
            float angle1 = (float)(2 * Math.PI * (i + 1) / CIRCLE_SEGMENTS);

            float ix0 = Mth.cos(angle0) * radius;
            float iz0 = Mth.sin(angle0) * radius;
            float ix1 = Mth.cos(angle1) * radius;
            float iz1 = Mth.sin(angle1) * radius;

            float ox0 = Mth.cos(angle0) * outerRadius;
            float oz0 = Mth.sin(angle0) * outerRadius;
            float ox1 = Mth.cos(angle1) * outerRadius;
            float oz1 = Mth.sin(angle1) * outerRadius;

            float ui0 = 0.5f + Mth.cos(angle0) * 0.45f;
            float vi0 = 0.5f + Mth.sin(angle0) * 0.45f;
            float ui1 = 0.5f + Mth.cos(angle1) * 0.45f;
            float vi1 = 0.5f + Mth.sin(angle1) * 0.45f;

            consumer.addVertex(matrix, ix0, 0, iz0)
                    .setColor(r, g, b, outerAlpha)
                    .setUv(ui0, vi0)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, ox0, 0, oz0)
                    .setColor(r, g, b, 0)
                    .setUv(1f, vi0)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, ox1, 0, oz1)
                    .setColor(r, g, b, 0)
                    .setUv(1f, vi1)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, ix1, 0, iz1)
                    .setColor(r, g, b, outerAlpha)
                    .setUv(ui1, vi1)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, 0, 1, 0);
        }

        poseStack.popPose();

        // === Ambient particles ===
        spawnAmbientParticles(state, school);
    }

    /**
     * Spawns ambient particles within the cloud area, rising upward.
     */
    private void spawnAmbientParticles(SpellCloudRenderState state, SpellSchool school) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        // Only spawn particles every few ticks to prevent overload
        if (state.age % 3 != 0) return;

        float radius = Math.max(0.5f, state.radius);

        var particleType = switch (school) {
            case FIRE -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case ARCANE -> ParticleTypes.ENCHANT;
            case HEALING -> ParticleTypes.HAPPY_VILLAGER;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
            default -> ParticleTypes.ENCHANT;
        };

        // Spawn 2-3 particles at random positions within the disc
        for (int i = 0; i < 2; i++) {
            double angle = level.random.nextDouble() * 2 * Math.PI;
            double dist = level.random.nextDouble() * radius;
            double px = state.posX + Math.cos(angle) * dist;
            double pz = state.posZ + Math.sin(angle) * dist;
            double py = state.posY + 0.1 + level.random.nextDouble() * 0.3;

            level.addParticle(particleType, px, py, pz, 0, 0.03, 0);
        }
    }

    /**
     * Safely converts an ordinal back to SpellSchool, defaulting to ARCANE if out of range.
     */
    private static SpellSchool schoolFromOrdinal(int ordinal) {
        SpellSchool[] values = SpellSchool.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return SpellSchool.ARCANE;
    }

    public static class SpellCloudRenderState extends EntityRenderState {
        public int schoolOrdinal;
        public float radius = 3.0f;
        public int age;
        public float partialTick;
        public double posX, posY, posZ;
    }
}
