package com.ultra.megamod.feature.combat.spell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellProjectileEntity;
import com.ultra.megamod.feature.combat.spell.SpellSchool;
import com.ultra.megamod.feature.combat.spell.client.render.LightEmission;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders spell projectiles in NeoForge 1.21.11.
 *
 * <p>The 1.21.11 entity rendering pipeline replaced {@code render(state, PoseStack, MultiBufferSource,
 * packedLight)} with {@link #submit(EntityRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)}.
 * An earlier version of this class still overrode the old method — which meant the renderer emitted
 * <em>nothing</em> every frame (the method was dead code with no {@code @Override} pulling it into the
 * pipeline). That is why spell projectiles were invisible despite entity spawn + visuals wiring being
 * correct.</p>
 *
 * <p>Model rendering path: if the projectile has a {@code projectile_model_id} whose identifier also
 * happens to match a registered {@code Item} (e.g. a spell_projectile dummy item), we populate an
 * {@link ItemStackRenderState} via {@code ItemModelResolver.updateForTopItem} and submit it. Until the
 * per-projectile items are registered this path yields an empty render state and the glow-quad drawn
 * via {@link SubmitNodeCollector#submitCustomGeometry} is what shows — that's the minimum source-parity
 * visual (BetterCombat / SpellEngine also composite a glow behind the model).</p>
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

        Vec3 vel = entity.getDeltaMovement();
        Vec3 prevVel = entity.previousVelocity;
        if (prevVel != null) {
            state.velX = Mth.lerp(partialTick, prevVel.x, vel.x);
            state.velY = Mth.lerp(partialTick, prevVel.y, vel.y);
            state.velZ = Mth.lerp(partialTick, prevVel.z, vel.z);
        } else {
            state.velX = vel.x;
            state.velY = vel.y;
            state.velZ = vel.z;
        }

        state.posX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        state.posY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        state.posZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        state.visuals = entity.renderVisuals;

        // Resolve item-backed projectile model on the server→client extract pass so the render state
        // is fully prepared before submit(). If the model identifier matches a registered Item
        // (modders can register dummy items with path "spell_projectile/<name>") we populate the
        // ItemStackRenderState here; otherwise it stays empty and submit() falls through to the glow.
        state.modelStack = ItemStack.EMPTY;
        if (state.visuals != null) {
            String modelId = state.visuals.projectileModelId();
            if (modelId != null && !modelId.isEmpty()) {
                Identifier id = Identifier.fromNamespaceAndPath("megamod", modelId);
                var itemOpt = BuiltInRegistries.ITEM.getOptional(id);
                if (itemOpt.isPresent()) {
                    state.modelStack = itemOpt.get().getDefaultInstance();
                }
            }
        }
    }

    @Override
    public void submit(SpellProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (state.age < 2) return; // Skip first frames (too close to caster)

        SpellSchool school = schoolFromOrdinal(state.schoolOrdinal);
        int color = school.color;
        float time = state.age + state.partialTick;

        String orientation = "TOWARDS_CAMERA";
        float modelScale = state.renderScale;
        float rotateDeg = 3.0f;
        LightEmission emission = LightEmission.GLOW;

        if (state.visuals != null) {
            if (state.visuals.projectileOrientation() != null) orientation = state.visuals.projectileOrientation();
            if (state.visuals.projectileModelScale() > 0) modelScale = state.visuals.projectileModelScale();
            rotateDeg = state.visuals.projectileRotateDeg();
            emission = parseLightEmission(state.visuals.lightEmission());
        }

        poseStack.pushPose();
        float pulse = 1.0f + 0.15f * Mth.sin(time * 0.3f);
        float finalScale = modelScale * pulse;
        poseStack.scale(finalScale, finalScale, finalScale);

        switch (orientation) {
            case "TOWARDS_MOTION", "ALONG_MOTION" -> {
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
            default -> {
                Quaternionf cameraRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
                poseStack.mulPose(new Quaternionf(cameraRot));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            }
        }

        if (rotateDeg != 0) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * rotateDeg));
        }

        // 1. If a registered item backs the projectile model, submit it through the new
        //    ItemStackRenderState pipeline so the baked model (with textures + rotations
        //    authored in Blockbench) renders properly.
        if (!state.modelStack.isEmpty()) {
            var mc = Minecraft.getInstance();
            var renderState = new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(renderState, state.modelStack, ItemDisplayContext.FIXED, mc.level, null, state.age);
            poseStack.pushPose();
            poseStack.translate(-0.5, -0.5, -0.5);
            renderState.submit(poseStack, nodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // 2. Glow composite — matches source SpellEngine where a school-colored additive glow
        //    sits behind the projectile model. Always emitted: with no model this IS the
        //    projectile visual, and with a model it supplies the magical halo. Submitted
        //    via submitCustomGeometry since 1.21.11 no longer exposes MultiBufferSource
        //    directly in entity renderers.
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        RenderType glowLayer = RenderTypes.eyes(GLOW_TEXTURE);
        nodeCollector.submitCustomGeometry(poseStack, glowLayer, (pose, consumer) -> {
            emitGlowQuad(consumer, pose, 0.8f, r, g, b, 88);
            emitGlowQuad(consumer, pose, 0.5f, 255, 255, 255, 200);
            emitGlowQuad(consumer, pose, 0.6f, r, g, b, 220);
        });

        poseStack.popPose();

        spawnTrailParticles(state, school);
    }

    private static void emitGlowQuad(VertexConsumer consumer, PoseStack.Pose pose, float halfSize,
                                     int r, int g, int b, int a) {
        Matrix4f matrix = pose.pose();
        int light = 0xF000F0;
        consumer.addVertex(matrix, -halfSize, -halfSize, 0).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix,  halfSize, -halfSize, 0).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix,  halfSize,  halfSize, 0).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, -halfSize,  halfSize, 0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
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
        public ItemStack modelStack = ItemStack.EMPTY;
    }
}
