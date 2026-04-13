package com.ultra.megamod.lib.spellengine.client.render;



import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.api.render.CustomLayers;
import com.ultra.megamod.lib.spellengine.api.render.LightEmission;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.client.beam.BeamEmitterEntity;
import com.ultra.megamod.lib.spellengine.client.compatibility.ShaderCompatibility;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.internals.delivery.Beam;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.utils.TargetHelper;

import java.util.HashMap;
import java.util.Map;

public class BeamRenderer {
    public record LayerSet(RenderType inner, RenderType outer) { }
    private static final Map<String, LayerSet> layerCache = new HashMap<>();
    public static LayerSet layerSetFor(Identifier texture, Spell.Target.Beam.Luminance luminance) {
        var key = texture.toString() + luminance.toString();
        if (layerCache.containsKey(key)) {
            return layerCache.get(key);
        } else {
            LayerSet layerSet;
            switch (luminance) {
                case LOW -> {
                    layerSet = low(texture);
                }
                case MEDIUM -> {
                    layerSet = medium(texture);
                }
                case HIGH -> {
                    layerSet = high(texture);
                }
                default -> layerSet = low(texture);
            }
            layerCache.put(key, layerSet);
            return layerSet;
        }
    }
    public static LayerSet vanilla(Identifier texture) {
        return new LayerSet(
                CustomLayers.beam(texture, false, true),
                CustomLayers.spellObject(texture, LightEmission.GLOW, true)
        );
    }
    public static LayerSet low(Identifier texture) {
        return new LayerSet(
                CustomLayers.beam(texture, false, false),
                CustomLayers.beam(texture, false, true)
        );
    }
    public static LayerSet medium(Identifier texture) {
        return new LayerSet(
                CustomLayers.spellObject(texture, LightEmission.RADIATE, false),
                CustomLayers.beam(texture, false, true)
        );
    }
    public static LayerSet high(Identifier texture) {
        return new LayerSet(
                CustomLayers.spellObject(texture, LightEmission.RADIATE, false),
                CustomLayers.spellObject(texture, LightEmission.RADIATE, true)
        );
    }

    public static void setup() {
        // In NeoForge 1.21.11, world render events are registered via NeoForge event bus
        // Registration should be done externally via RenderLevelStageEvent
    }

    public static void renderAllInWorld(PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, Camera camera, int light, float delta) {
        var focusedEntity = camera.entity();
        if (focusedEntity == null) {
            return;
        }

        var renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 24; // 24 = 16 * 1.5F
        var squaredRenderDistance = renderDistance * renderDistance;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var players = level.players()
                .stream().filter(player ->
                        player.distanceToSqr(focusedEntity) < squaredRenderDistance
                && ((SpellCasterEntity)player).getBeam() != null)
                .toList();
        if (players.isEmpty()) {
            return;
        }

        matrices.pushPose();
        Vec3 camPos = camera.position();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        for (var livingEntity : players) {
            var launchHeight = SpellHelper.launchHeight(livingEntity);
            var offset = new Vec3(0.0, launchHeight, SpellHelper.launchPointOffsetDefault);
            SpellCasterEntity caster = (SpellCasterEntity)livingEntity;
            matrices.pushPose();
            var pos = new Vec3(livingEntity.xOld, livingEntity.yOld, livingEntity.zOld)
                    .lerp(livingEntity.position(), delta);
            matrices.translate(pos.x, pos.y, pos.z);

            Vec3 from = livingEntity.position().add(0, launchHeight, 0);
            var lookVector = Vec3.ZERO;
            if (livingEntity == Minecraft.getInstance().player) {
                // No lerp for local player
                lookVector = Vec3.directionFromRotation(livingEntity.getXRot(), livingEntity.getYRot());
            } else {
                lookVector = Vec3.directionFromRotation(livingEntity.xRotO, livingEntity.yRotO);
                lookVector = lookVector.lerp(Vec3.directionFromRotation(livingEntity.getXRot(), livingEntity.getYRot()), delta);
            }
            lookVector = lookVector.normalize();
            var beamPosition = TargetHelper.castBeam(livingEntity, lookVector, 32);
            lookVector = lookVector.scale(beamPosition.length());
            Vec3 to = from.add(lookVector);

            var beamAppearance = caster.getBeam();
            renderBeamFromPlayer(matrices, vertexConsumers, beamAppearance,
                    from, to, offset, livingEntity.level().getGameTime(), delta);
            ((BeamEmitterEntity)livingEntity).setLastRenderedBeam(new Beam.Rendered(beamPosition, beamAppearance));
            matrices.popPose();
        }
        vertexConsumers.endBatch();
        matrices.popPose();
    }

    private static void renderBeamFromPlayer(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider,
                                             Spell.Target.Beam beam,
                                             Vec3 from, Vec3 to, Vec3 offset, long time, float tickDelta) {
        var absoluteTime = (float)Math.floorMod(time, 40) + tickDelta;

        matrixStack.pushPose();
        matrixStack.translate(0, offset.y, 0);

        Vec3 beamVector = to.subtract(from);
        float length = (float)beamVector.length();

        // Perform some rotation
        beamVector = beamVector.normalize();
        float n = (float)Math.acos(beamVector.y);
        float o = (float)Math.atan2(beamVector.z, beamVector.x);
        matrixStack.mulPose(Axis.YP.rotationDegrees((1.5707964F - o) * 57.295776F));
        matrixStack.mulPose(Axis.XP.rotationDegrees(n * 57.295776F));
        matrixStack.translate(0, offset.z, 0); // At this point everything is so rotated, we need to translate along y to move along z

        matrixStack.mulPose(Axis.YP.rotationDegrees(absoluteTime * 2.25F - 45.0F));

        var texture = Identifier.parse(beam.texture_id);
        var outerColor = Color.IntFormat.fromLongRGBA(beam.color_rgba);
        var innerColor = Color.IntFormat.fromLongRGBA(beam.inner_color_rgba);

        LayerSet renderLayers;
        if (ShaderCompatibility.isVanillaRenderSystem()) {
            renderLayers = vanilla(texture);
        } else {
            var luminance = ShaderCompatibility.isShaderPackInUse()
                    ? (SpellEngineClient.config.renderBeamsHighLuminance ? beam.luminance : Spell.Target.Beam.Luminance.MEDIUM)
                    : Spell.Target.Beam.Luminance.LOW;
            renderLayers = layerSetFor(texture, luminance);
        }
        BeamRenderer.renderBeam(matrixStack, vertexConsumerProvider,
                time, tickDelta, beam.flow, true,
                innerColor, outerColor, renderLayers,
                0, length, beam.width);

        matrixStack.popPose();
    }

    // RenderType extension removed - no longer possible in MC 1.21.11

    public static void renderBeam(PoseStack matrices, MultiBufferSource vertexConsumers,
                                  long time, float tickDelta, float direction, boolean center,
                                  Color.IntFormat innerColor, Color.IntFormat outerColor, LayerSet renderLayers,
                                  float yOffset, float height, float width) {
        matrices.pushPose();

        float shift = (float)Math.floorMod(time, 40) + tickDelta;
        float offset = Mth.frac(shift * 0.2f - (float)Mth.floor(shift * 0.1f)) * (- direction);

        var originalWidth = width;
        if (center) {
            renderBeamLayer(matrices, vertexConsumers.getBuffer(renderLayers.inner()),
                    innerColor.red(), innerColor.green(), innerColor.blue(), innerColor.alpha(),
                    yOffset, height,
                    0.0f, width, width, 0.0f, -width, 0.0f, 0.0f, -width,
                    0.0f, 1f, height, offset);
        }

        width = originalWidth * 1.5F;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(renderLayers.outer()),
                outerColor.red(), outerColor.green(), outerColor.blue(), (int) (outerColor.alpha() * 0.75F),
                yOffset, height,
                0.0f, width, width, 0.0f, -width, 0.0f, 0.0f, -width,
                0.0f, 1.0f, height, offset * 0.9F);

        width = originalWidth * 2F;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(renderLayers.outer()),
                outerColor.red(), outerColor.green(), outerColor.blue(), outerColor.alpha() / 3,
                yOffset, height,
                0.0f, width, width, 0.0f, -width, 0.0f, 0.0f, -width,
                0.0f, 1.0f, height, offset * 0.8F);
        matrices.popPose();
    }

    private static void renderBeamLayer(PoseStack matrices, VertexConsumer vertices,
                                        int red, int green, int blue, int alpha,
                                        float yOffset, float height,
                                        float x1, float z1, float x2, float z2, float x3, float z3, float x4,
                                        float z4, float u1, float u2, float v1, float v2) {
        PoseStack.Pose matrix = matrices.last();
        renderBeamFace(matrix, vertices, red, green, blue, alpha, yOffset, height, x1, z1, x2, z2, u1, u2, v1, v2);
        renderBeamFace(matrix, vertices, red, green, blue, alpha, yOffset, height, x4, z4, x3, z3, u1, u2, v1, v2);
        renderBeamFace(matrix, vertices, red, green, blue, alpha, yOffset, height, x2, z2, x4, z4, u1, u2, v1, v2);
        renderBeamFace(matrix, vertices, red, green, blue, alpha, yOffset, height, x3, z3, x1, z1, u1, u2, v1, v2);
    }


    private static void renderBeamFace(PoseStack.Pose matrix, VertexConsumer vertices, int red, int green, int blue, int alpha, float yOffset, float height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        renderBeamVertex(matrix, vertices, red, green, blue, alpha, height, x1, z1, u2, v1);
        renderBeamVertex(matrix, vertices, red, green, blue, alpha, yOffset, x1, z1, u2, v2);
        renderBeamVertex(matrix, vertices, red, green, blue, alpha, yOffset, x2, z2, u1, v2);
        renderBeamVertex(matrix, vertices, red, green, blue, alpha, height, x2, z2, u1, v1);
    }

    /**
     * @param v the top-most coordinate of the texture region
     * @param u the left-most coordinate of the texture region
     */
    private static void renderBeamVertex(PoseStack.Pose matrix, VertexConsumer vertices, int red, int green, int blue, int alpha, float y, float x, float z, float u, float v) {
        vertices.addVertex(matrix.pose(), x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(matrix, 0.0F, 1.0F, 0.0F);
    }
}
