package com.ultra.megamod.feature.attributes.network;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CombatTextRenderer {
    private static final List<FloatingText> ACTIVE_TEXTS = new ArrayList<>();
    private static final long DURATION_MS = 1500;
    // Minimum vertical screen-space gap between overlapping texts
    private static final int OVERLAP_THRESHOLD = 12;

    public static void addText(CombatTextPayload.CombatTextEntry entry) {
        // Random horizontal spread so texts from the same source don't stack exactly
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double offsetX = rng.nextDouble(-0.35, 0.35);
        double offsetZ = rng.nextDouble(-0.35, 0.35);

        ACTIVE_TEXTS.add(new FloatingText(
            entry.x() + offsetX, entry.y(), entry.z() + offsetZ,
            entry.text(), entry.color(), entry.scale(),
            System.currentTimeMillis()
        ));
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "combat_text"),
            CombatTextRenderer::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();
        Font font = mc.font;
        Camera camera = mc.gameRenderer.getMainCamera();
        var camPosVec = camera.position();
        double camX = camPosVec.x();
        double camY = camPosVec.y();
        double camZ = camPosVec.z();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // First pass: project all texts to screen coords and collect visible ones
        List<ScreenText> visible = new ArrayList<>();

        Iterator<FloatingText> it = ACTIVE_TEXTS.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            long elapsed = now - ft.startTime;
            if (elapsed > DURATION_MS) {
                it.remove();
                continue;
            }

            float progress = (float) elapsed / DURATION_MS;
            float alpha = progress < 0.5f ? 1.0f : 1.0f - ((progress - 0.5f) / 0.5f);
            int alphaInt = (int) (alpha * 255) & 0xFF;
            if (alphaInt < 8) continue;

            double worldY = ft.y + (elapsed * 0.001 * 0.05);

            int[] screen = projectToScreen(ft.x - camX, worldY - camY, ft.z - camZ, camera, screenW, screenH);
            if (screen == null) continue;

            int sx = screen[0];
            int sy = screen[1] - (int) ((1.0f - progress) * 10);

            float popScale = 1.0f;
            if (elapsed < 200) {
                float t = (float) elapsed / 200.0f;
                popScale = 1.0f + 0.35f * (1.0f - t * t);
            }

            visible.add(new ScreenText(ft, sx, sy, alphaInt, ft.scale * popScale));
        }

        // Second pass: de-overlap — push texts apart vertically when too close
        for (int i = 0; i < visible.size(); i++) {
            for (int j = i + 1; j < visible.size(); j++) {
                ScreenText a = visible.get(i);
                ScreenText b = visible.get(j);
                int dx = Math.abs(a.sx - b.sx);
                int dy = Math.abs(a.sy - b.sy);
                // Only spread if both horizontally and vertically close
                if (dx < 40 && dy < OVERLAP_THRESHOLD) {
                    int nudge = (OVERLAP_THRESHOLD - dy) / 2 + 1;
                    if (a.sy <= b.sy) {
                        a.sy -= nudge;
                        b.sy += nudge;
                    } else {
                        a.sy += nudge;
                        b.sy -= nudge;
                    }
                }
            }
        }

        // Third pass: render
        for (ScreenText st : visible) {
            int color = (st.ft.color & 0x00FFFFFF) | (st.alphaInt << 24);
            int outlineAlpha = Math.min(st.alphaInt, (int) (st.alphaInt * 0.8f));
            int outlineColor = outlineAlpha << 24;

            String text = st.ft.text;
            int textWidth = font.width(text);

            int drawX = st.sx - textWidth / 2;
            int drawY = st.sy - font.lineHeight / 2;

            // Bold outline at 8 offsets
            graphics.drawString(font, text, drawX - 1, drawY, outlineColor, false);
            graphics.drawString(font, text, drawX + 1, drawY, outlineColor, false);
            graphics.drawString(font, text, drawX, drawY - 1, outlineColor, false);
            graphics.drawString(font, text, drawX, drawY + 1, outlineColor, false);
            graphics.drawString(font, text, drawX - 1, drawY - 1, outlineColor, false);
            graphics.drawString(font, text, drawX + 1, drawY - 1, outlineColor, false);
            graphics.drawString(font, text, drawX - 1, drawY + 1, outlineColor, false);
            graphics.drawString(font, text, drawX + 1, drawY + 1, outlineColor, false);

            // Main text
            graphics.drawString(font, text, drawX, drawY, color, false);
        }
    }

    private static int[] projectToScreen(double relX, double relY, double relZ, Camera camera, int screenW, int screenH) {
        Vector3f pos = new Vector3f((float) relX, (float) relY, (float) relZ);

        Quaternionf invRot = camera.rotation().conjugate(new Quaternionf());
        pos.rotate(invRot);

        if (pos.z >= 0) return null;

        Minecraft mc = Minecraft.getInstance();
        float fov = (float) Math.toRadians(mc.options.fov().get());
        float aspect = (float) screenW / screenH;
        float tanHalfFov = (float) Math.tan(fov / 2.0);

        float ndcX = -pos.x / (-pos.z * tanHalfFov * aspect);
        float ndcY = pos.y / (-pos.z * tanHalfFov);

        float sx = (ndcX + 1.0f) * 0.5f * screenW;
        float sy = (1.0f - ndcY) * 0.5f * screenH;

        if (sx < -50 || sx > screenW + 50 || sy < -50 || sy > screenH + 50) return null;

        return new int[]{(int) sx, (int) sy};
    }

    private static class FloatingText {
        final double x, y, z;
        final String text;
        final int color;
        final float scale;
        final long startTime;

        FloatingText(double x, double y, double z, String text, int color, float scale, long startTime) {
            this.x = x; this.y = y; this.z = z;
            this.text = text; this.color = color; this.scale = scale;
            this.startTime = startTime;
        }
    }

    /** Mutable screen-space representation for de-overlap pass. */
    private static class ScreenText {
        final FloatingText ft;
        int sx, sy;
        final int alphaInt;
        final float scale;

        ScreenText(FloatingText ft, int sx, int sy, int alphaInt, float scale) {
            this.ft = ft; this.sx = sx; this.sy = sy;
            this.alphaInt = alphaInt; this.scale = scale;
        }
    }
}
