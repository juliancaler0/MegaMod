package com.ultra.megamod.feature.backpacks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.backpacks.BackpackWearableManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Renders the equipped backpack on the player's back using the 64x64 block textures
 * and UV coordinates matching the Blockbench model (backpack_base.json).
 */
public class BackpackLayerRenderer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
    extends RenderLayer<S, M> {

    private static final Identifier FALLBACK_TEXTURE =
        Identifier.fromNamespaceAndPath("megamod", "textures/block/backpack/standard.png");

    private static final java.util.Map<String, Identifier> textureCache = new java.util.concurrent.ConcurrentHashMap<>();

    private static Identifier getBackpackTexture(int entityId) {
        String backpackId = BackpackWearableManager.getClientBackpackId(entityId);
        if (backpackId.isEmpty()) return FALLBACK_TEXTURE;

        // Bound the cache to prevent unbounded memory growth
        if (textureCache.size() > 256) {
            textureCache.clear();
        }

        return textureCache.computeIfAbsent(backpackId, id -> {
            String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            // Convert registry name to block texture name: "iron_backpack" -> "iron"
            if (path.endsWith("_backpack")) {
                path = path.substring(0, path.length() - 9);
            }
            return Identifier.fromNamespaceAndPath("megamod", "textures/block/backpack/" + path + ".png");
        });
    }

    public BackpackLayerRenderer(RenderLayerParent<S, M> renderer, EntityRendererProvider.Context context) {
        super(renderer);
    }

    @Override
    public void submit(@Nonnull PoseStack poseStack,
                       @Nonnull SubmitNodeCollector nodeCollector,
                       int packedLight,
                       @Nonnull S renderState,
                       float vertRot,
                       float horizRot) {

        int entityId = BackpackRenderContext.getEntityId();
        // Clear the ThreadLocal after reading to prevent stale data and memory leaks
        BackpackRenderContext.clear();
        if (entityId < 0) return;
        if (!BackpackWearableManager.isClientWearing(entityId)) return;

        poseStack.pushPose();

        M model = this.getParentModel();
        if (model instanceof HumanoidModel<?> humanoid) {
            humanoid.body.translateAndRotate(poseStack);
        }

        // Body back surface is at Z = 2/16 = 0.125 in body-local space
        poseStack.translate(0.0f, 0.0f, 0.125f);

        renderBackpack(poseStack, nodeCollector, packedLight, entityId);

        poseStack.popPose();
    }

    // Block model coordinate -> body-local coordinate conversions:
    // X: centered at block X=8
    private static float cx(float bx) { return (bx - 8.0f) / 16.0f; }
    // Y: inverted (block Y up -> body Y down), with margin to center on 12-unit body
    private static float cy(float by) { return (10.1f - by + 1.0f) / 16.0f; }
    // Z: strap back at blockZ=4.2 maps to body surface (Z=0 after translation)
    private static float cz(float bz) { return (bz - 4.2f) / 16.0f; }

    /** Convert blockbench UV to normalized 0-1 (texture_size [64,64]: 1 bb unit = 4 pixels) */
    private static float[] uv(float u0, float v0, float u1, float v1) {
        return new float[]{u0 / 16.0f, v0 / 16.0f, u1 / 16.0f, v1 / 16.0f};
    }

    private void renderBackpack(PoseStack poseStack, SubmitNodeCollector nodeCollector,
                                int packedLight, int entityId) {
        Identifier texture = getBackpackTexture(entityId);
        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(texture),
            (pose, consumer) -> {
                Matrix4f m = pose.pose();
                int li = packedLight;

                // MainBody: [4.1, 0.8, 5] to [11.9, 7.8, 8.9]
                box(consumer, m, pose, li,
                    cx(4.1f), cy(7.8f), cz(5f), cx(11.9f), cy(0.8f), cz(8.9f),
                    uv(1.25f, 3.5f, 3.75f, 5.75f),     // north
                    uv(5f, 3.5f, 7.5f, 5.75f),          // south
                    uv(0f, 3.5f, 1.25f, 5.75f),         // east
                    uv(3.75f, 3.5f, 5f, 5.75f),         // west
                    null,                                 // up (hidden by Top)
                    uv(3.75f, 2.255f, 6.25f, 3.505f));  // down

                // Top: [4.1, 7.8, 5] to [11.9, 10.1, 8.9]
                box(consumer, m, pose, li,
                    cx(4.1f), cy(10.1f), cz(5f), cx(11.9f), cy(7.8f), cz(8.9f),
                    uv(1.25f, 1.25f, 3.75f, 2f),        // north
                    uv(5f, 1.25f, 7.5f, 2f),             // south
                    uv(0f, 1.25f, 1.25f, 2f),            // east
                    uv(3.75f, 1.25f, 5f, 2f),            // west
                    uv(3.75f, 1.25f, 1.25f, 0f),         // up (flipped per blockbench)
                    null);                                // down (hidden by MainBody)

                // PocketFace: [4.9, 2.4, 8.9] to [11.1, 7.1, 10.5]
                box(consumer, m, pose, li,
                    cx(4.9f), cy(7.1f), cz(8.9f), cx(11.1f), cy(2.4f), cz(10.5f),
                    null,                                 // north (hidden by MainBody)
                    uv(3f, 6.5f, 5f, 8f),                // south
                    uv(0f, 6.5f, 0.5f, 8f),              // east
                    uv(2.5f, 6.5f, 3f, 8f),              // west
                    uv(0.5f, 6f, 2.5f, 6.5f),            // up
                    uv(4.5f, 6f, 2.5f, 6.5f));           // down (flipped per blockbench)

                // LeftStrap: [4.8, 1.6, 4.2] to [5.6, 7.8, 5]
                box(consumer, m, pose, li,
                    cx(4.8f), cy(7.8f), cz(4.2f), cx(5.6f), cy(1.6f), cz(5f),
                    uv(5.5f, 6.25f, 5.75f, 8.25f),      // north
                    null,                                  // south (hidden)
                    uv(5.25f, 6.25f, 5.5f, 8.25f),      // east
                    uv(5.75f, 6.25f, 6f, 8.25f),         // west
                    uv(5.5f, 6f, 5.75f, 6.25f),          // up
                    uv(5.75f, 6f, 6f, 6.25f));           // down

                // RightStrap: [10.4, 1.6, 4.2] to [11.2, 7.8, 5]
                box(consumer, m, pose, li,
                    cx(10.4f), cy(7.8f), cz(4.2f), cx(11.2f), cy(1.6f), cz(5f),
                    uv(6.75f, 6.25f, 7f, 8.25f),        // north
                    null,                                  // south (hidden)
                    uv(6.5f, 6.25f, 6.75f, 8.25f),      // east
                    uv(7f, 6.25f, 7.25f, 8.25f),         // west
                    uv(6.75f, 6f, 7f, 6.25f),            // up
                    uv(7f, 6f, 7.25f, 6.25f));           // down

                // Bottom: [4.1, 0, 5] to [11.9, 0.8, 8.1]
                box(consumer, m, pose, li,
                    cx(4.1f), cy(0.8f), cz(5f), cx(11.9f), cy(0f), cz(8.1f),
                    uv(1f, 9.5f, 3.5f, 9.75f),          // north
                    uv(4.5f, 9.5f, 7f, 9.75f),           // south
                    uv(0f, 9.5f, 1f, 9.75f),             // east
                    uv(3.5f, 9.5f, 4.5f, 9.75f),         // west
                    null,                                  // up (hidden by MainBody)
                    uv(3.5f, 8.5f, 6f, 9.5f));           // down
            });
    }

    /**
     * Render a textured box with per-face UVs from the Blockbench model.
     * x0 < x1, y0 < y1, z0 < z1. Null UV = skip that face.
     */
    private static void box(VertexConsumer c, Matrix4f m, PoseStack.Pose pose, int light,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            float[] north, float[] south, float[] east, float[] west,
                            float[] up, float[] down) {
        int ov = 655360;

        if (north != null) { // -Z face
            c.addVertex(m, x1, y0, z0).setColor(255,255,255,255).setUv(north[0], north[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, -1);
            c.addVertex(m, x0, y0, z0).setColor(255,255,255,255).setUv(north[2], north[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, -1);
            c.addVertex(m, x0, y1, z0).setColor(255,255,255,255).setUv(north[2], north[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, -1);
            c.addVertex(m, x1, y1, z0).setColor(255,255,255,255).setUv(north[0], north[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, -1);
        }
        if (south != null) { // +Z face
            c.addVertex(m, x0, y0, z1).setColor(255,255,255,255).setUv(south[0], south[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, 1);
            c.addVertex(m, x1, y0, z1).setColor(255,255,255,255).setUv(south[2], south[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, 1);
            c.addVertex(m, x1, y1, z1).setColor(255,255,255,255).setUv(south[2], south[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, 1);
            c.addVertex(m, x0, y1, z1).setColor(255,255,255,255).setUv(south[0], south[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, 0, 1);
        }
        if (east != null) { // +X face
            c.addVertex(m, x1, y0, z1).setColor(255,255,255,255).setUv(east[0], east[1]).setOverlay(ov).setLight(light).setNormal(pose, 1, 0, 0);
            c.addVertex(m, x1, y0, z0).setColor(255,255,255,255).setUv(east[2], east[1]).setOverlay(ov).setLight(light).setNormal(pose, 1, 0, 0);
            c.addVertex(m, x1, y1, z0).setColor(255,255,255,255).setUv(east[2], east[3]).setOverlay(ov).setLight(light).setNormal(pose, 1, 0, 0);
            c.addVertex(m, x1, y1, z1).setColor(255,255,255,255).setUv(east[0], east[3]).setOverlay(ov).setLight(light).setNormal(pose, 1, 0, 0);
        }
        if (west != null) { // -X face
            c.addVertex(m, x0, y0, z0).setColor(255,255,255,255).setUv(west[0], west[1]).setOverlay(ov).setLight(light).setNormal(pose, -1, 0, 0);
            c.addVertex(m, x0, y0, z1).setColor(255,255,255,255).setUv(west[2], west[1]).setOverlay(ov).setLight(light).setNormal(pose, -1, 0, 0);
            c.addVertex(m, x0, y1, z1).setColor(255,255,255,255).setUv(west[2], west[3]).setOverlay(ov).setLight(light).setNormal(pose, -1, 0, 0);
            c.addVertex(m, x0, y1, z0).setColor(255,255,255,255).setUv(west[0], west[3]).setOverlay(ov).setLight(light).setNormal(pose, -1, 0, 0);
        }
        if (up != null) { // -Y face (toward head)
            c.addVertex(m, x0, y0, z0).setColor(255,255,255,255).setUv(up[0], up[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, -1, 0);
            c.addVertex(m, x0, y0, z1).setColor(255,255,255,255).setUv(up[0], up[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, -1, 0);
            c.addVertex(m, x1, y0, z1).setColor(255,255,255,255).setUv(up[2], up[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, -1, 0);
            c.addVertex(m, x1, y0, z0).setColor(255,255,255,255).setUv(up[2], up[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, -1, 0);
        }
        if (down != null) { // +Y face (toward waist)
            c.addVertex(m, x0, y1, z1).setColor(255,255,255,255).setUv(down[0], down[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, 1, 0);
            c.addVertex(m, x0, y1, z0).setColor(255,255,255,255).setUv(down[0], down[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, 1, 0);
            c.addVertex(m, x1, y1, z0).setColor(255,255,255,255).setUv(down[2], down[3]).setOverlay(ov).setLight(light).setNormal(pose, 0, 1, 0);
            c.addVertex(m, x1, y1, z1).setColor(255,255,255,255).setUv(down[2], down[1]).setOverlay(ov).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }
}
