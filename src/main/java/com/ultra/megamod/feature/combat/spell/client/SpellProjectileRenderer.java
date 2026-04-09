package com.ultra.megamod.feature.combat.spell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellProjectileEntity;
import com.ultra.megamod.feature.combat.spell.SpellSchool;
import com.ultra.megamod.feature.combat.spell.client.render.CustomLayers;
import com.ultra.megamod.feature.combat.spell.client.render.CustomModels;
import com.ultra.megamod.feature.combat.spell.client.render.LightEmission;
import com.ultra.megamod.feature.combat.spell.client.render.SpellModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders spell projectiles with orientation modes, model rendering, spin, and trail particles.
 * Ported 1:1 from SpellEngine's SpellProjectileRenderer approach.
 *
 * Supports 3 orientation modes from SpellVisuals:
 * - TOWARDS_CAMERA: Billboard facing camera (default)
 * - TOWARDS_MOTION: Rotates to face velocity direction
 * - ALONG_MOTION: Like TOWARDS_MOTION but with 90-degree Y offset
 *
 * Uses SpellVisuals for model ID, scale, spin rotation, and light emission.
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectileEntity, SpellProjectileRenderer.SpellProjectileRenderState> {

    private static final Identifier GLOW_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/entity/spell_projectile.png");

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void extractRenderState(SpellProjectileEntity entity, SpellProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.schoolOrdinal = entity.getSchoolOrdinal();
        state.renderScale = entity.getRenderScale();
        state.age = entity.tickCount;
        state.partialTick = partialTick;

        // Velocity for orientation and trails
        Vec3 vel = entity.getDeltaMovement();
        Vec3 prevVel = entity.previousVelocity;
        if (prevVel != null) {
            // Smooth interpolation between previous and current velocity
            state.velX = Mth.lerp(partialTick, prevVel.x, vel.x);
            state.velY = Mth.lerp(partialTick, prevVel.y, vel.y);
            state.velZ = Mth.lerp(partialTick, prevVel.z, vel.z);
        } else {
            state.velX = vel.x;
            state.velY = vel.y;
            state.velZ = vel.z;
        }

        // World position for particles
        state.posX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        state.posY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        state.posZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        // Visual configuration
        state.visuals = entity.renderVisuals;
    }

    public void render(SpellProjectileRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (state.age < 2) return; // Skip first frames (too close to caster)

        SpellSchool school = schoolFromOrdinal(state.schoolOrdinal);
        int color = school.color;
        float time = state.age + state.partialTick;

        // Get visual config
        String orientation = "TOWARDS_CAMERA";
        float modelScale = state.renderScale;
        float rotateDeg = 3.0f; // Default slow spin
        LightEmission emission = LightEmission.GLOW;

        if (state.visuals != null) {
            if (state.visuals.projectileOrientation() != null) orientation = state.visuals.projectileOrientation();
            if (state.visuals.projectileModelScale() > 0) modelScale = state.visuals.projectileModelScale();
            rotateDeg = state.visuals.projectileRotateDeg();
            emission = parseLightEmission(state.visuals.lightEmission());
        }

        poseStack.pushPose();

        // Apply scale
        float pulse = 1.0f + 0.15f * Mth.sin(time * 0.3f);
        float finalScale = modelScale * pulse;
        poseStack.scale(finalScale, finalScale, finalScale);

        // Apply orientation
        switch (orientation) {
            case "TOWARDS_MOTION", "ALONG_MOTION" -> {
                // Compute yaw/pitch from interpolated velocity
                double vx = state.velX, vy = state.velY, vz = state.velZ;
                double len = Math.sqrt(vx * vx + vy * vy + vz * vz);
                if (len > 0.001) {
                    vx /= len; vy /= len; vz /= len;
                    float dirYaw = (float) Math.toDegrees(Math.atan2(vx, vz)) + 180F;
                    if ("ALONG_MOTION".equals(orientation)) dirYaw += 90;
                    float dirPitch = (float) Math.toDegrees(Math.asin(vy));
                    poseStack.mulPose(Axis.YP.rotationDegrees(dirYaw));
                    poseStack.mulPose(Axis.XP.rotationDegrees(dirPitch));
                }
            }
            default -> { // TOWARDS_CAMERA — billboard
                Quaternionf cameraRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
                poseStack.mulPose(new Quaternionf(cameraRot));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            }
        }

        // Apply spin rotation
        if (rotateDeg != 0) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * rotateDeg));
        }

        // === Render the projectile ===
        // Try model-based rendering if a model ID is specified
        String modelId = state.visuals != null ? state.visuals.projectileModelId() : null;
        if (modelId != null && !modelId.isEmpty()) {
            // Model-based rendering via CustomModels
            Identifier id = Identifier.fromNamespaceAndPath("megamod", modelId);
            RenderType layer = SpellModelHelper.LAYERS.getOrDefault(emission, SpellModelHelper.LAYERS.get(LightEmission.GLOW));
            CustomModels.render(layer, id, poseStack, bufferSource, 0xF000F0, state.age);
        } else {
            // Fallback: glow quad rendering (original simplified approach)
            int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
            VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.eyes(GLOW_TEXTURE));

            // Outer glow
            renderQuad(consumer, poseStack.last(), 0.8f, r, g, b, 88);
            // Inner core
            renderQuad(consumer, poseStack.last(), 0.5f, 255, 255, 255, 200);
            // Color layer
            renderQuad(consumer, poseStack.last(), 0.6f, r, g, b, 220);
        }

        poseStack.popPose();

        // Trail particles
        spawnTrailParticles(state, school);
    }

    private void renderQuad(VertexConsumer consumer, PoseStack.Pose pose, float halfSize,
                            int r, int g, int b, int a) {
        Matrix4f matrix = pose.pose();
        int light = 0xF000F0;
        consumer.addVertex(matrix, -halfSize, -halfSize, 0).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, halfSize, -halfSize, 0).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, halfSize, halfSize, 0).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, -halfSize, halfSize, 0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private void spawnTrailParticles(SpellProjectileRenderState state, SpellSchool school) {
        if (state.age < 2) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        double trailX = state.posX - state.velX * 0.5;
        double trailY = state.posY - state.velY * 0.5;
        double trailZ = state.posZ - state.velZ * 0.5;

        var particleType = switch (school) {
            case FIRE -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case ARCANE -> ParticleTypes.ENCHANT;
            case HEALING -> ParticleTypes.HAPPY_VILLAGER;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case SOUL -> ParticleTypes.SOUL;
            default -> ParticleTypes.END_ROD;
        };

        double spread = 0.05;
        level.addParticle(particleType,
                trailX + (level.random.nextDouble() - 0.5) * spread,
                trailY + (level.random.nextDouble() - 0.5) * spread,
                trailZ + (level.random.nextDouble() - 0.5) * spread, 0, 0, 0);
    }

    private static SpellSchool schoolFromOrdinal(int ordinal) {
        SpellSchool[] values = SpellSchool.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : SpellSchool.ARCANE;
    }

    private static LightEmission parseLightEmission(String name) {
        if (name == null) return LightEmission.GLOW;
        try { return LightEmission.valueOf(name); }
        catch (Exception e) { return LightEmission.GLOW; }
    }

    public static class SpellProjectileRenderState extends EntityRenderState {
        public int schoolOrdinal;
        public float renderScale = 1.0f;
        public int age;
        public float partialTick;
        public double velX, velY, velZ;
        public double posX, posY, posZ;
        public SpellDefinition.SpellVisuals visuals;
    }
}
