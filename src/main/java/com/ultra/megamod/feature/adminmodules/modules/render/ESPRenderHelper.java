package com.ultra.megamod.feature.adminmodules.modules.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

/**
 * Shared rendering utilities for ESP and X-Ray admin modules.
 *
 * Creates custom RenderPipelines with NO_DEPTH_TEST so that wireframe
 * highlights render through all blocks (Meteor Client-style ESP).
 *
 * Pipelines must be registered via RegisterRenderPipelinesEvent — see
 * {@link #registerPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent)}.
 */
public class ESPRenderHelper {

    private ESPRenderHelper() {}

    // ── Custom pipelines (lines with no depth test) ────────────────────────

    /** Lines pipeline that always renders on top (no depth test, no depth write). */
    public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(Identifier.parse("megamod:pipeline/lines_no_depth"))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .build();

    // ── RenderTypes using the custom pipelines ─────────────────────────────

    /** Lines rendered through all blocks. Use for ESP/X-Ray highlights. */
    public static final RenderType LINES_SEE_THROUGH = RenderType.create(
        "megamod_esp_lines",
        RenderSetup.builder(LINES_NO_DEPTH_PIPELINE)
            .createRenderSetup()
    );

    /** Alias — same RenderType, thin lines are controlled by vertex data not pipeline. */
    public static final RenderType LINES_SEE_THROUGH_THIN = LINES_SEE_THROUGH;

    // ── Pipeline registration (call from MegaModClient) ────────────────────

    /** Register our custom pipelines. Call from RegisterRenderPipelinesEvent handler. */
    public static void registerPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(LINES_NO_DEPTH_PIPELINE);
    }

    // ── Drawing helpers ────────────────────────────────────────────────────

    /** Draws a wireframe box (12 edges = 24 vertices). */
    public static void drawWireBox(VertexConsumer c, Matrix4f m,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float r, float g, float b, float a) {
        // Bottom face edges
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        c.addVertex(m, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        c.addVertex(m, x2, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        // Top face edges
        c.addVertex(m, x1, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        c.addVertex(m, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 0, -1).setLineWidth(1.0f);
        // Vertical edges
        c.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
    }

    /** Draws a small cross marker (3 axes) at a position. */
    public static void drawCross(VertexConsumer c, Matrix4f m,
            float x, float y, float z, float size,
            float r, float g, float b, float a) {
        c.addVertex(m, x - size, y, z).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x + size, y, z).setColor(r, g, b, a).setNormal(1, 0, 0).setLineWidth(1.0f);
        c.addVertex(m, x, y, z - size).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        c.addVertex(m, x, y, z + size).setColor(r, g, b, a).setNormal(0, 0, 1).setLineWidth(1.0f);
        c.addVertex(m, x, y - size, z).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
        c.addVertex(m, x, y + size, z).setColor(r, g, b, a).setNormal(0, 1, 0).setLineWidth(1.0f);
    }
}
