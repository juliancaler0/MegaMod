package com.ultra.megamod.feature.adminmodules.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class TracersModule extends AdminModule {
    private ModuleSetting.DoubleSetting range;
    private ModuleSetting.BoolSetting tracePlayers;
    private ModuleSetting.BoolSetting traceHostiles;

    // Cached entity list (rescanned every few frames to avoid per-frame entity scans)
    private int entityScanTick = 0;
    private static final int ENTITY_SCAN_INTERVAL = 4;
    private List<Entity> cachedEntities = new ArrayList<>();

    public TracersModule() {
        super("tracers", "Tracers", "Draws lines from camera to entities", ModuleCategory.RENDER);
    }

    @Override
    protected void initSettings() {
        range = decimal("Range", 64.0, 8.0, 256.0, "Tracer range");
        tracePlayers = bool("Players", true, "Trace to players");
        traceHostiles = bool("Hostiles", true, "Trace to hostile mobs");
    }

    @Override public boolean isServerSide() { return false; }
    @Override public boolean isClientSide() { return true; }

    @Override
    public void onRenderWorld(Object eventObj) {
        if (!(eventObj instanceof RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        try {
            float partialTick = mc.getDeltaTracker().getRealtimeDeltaTicks();
            Vec3 camPos = mc.player.getEyePosition(partialTick);
            double r = range.getValue();

            entityScanTick++;
            if (entityScanTick >= ENTITY_SCAN_INTERVAL || cachedEntities.isEmpty()) {
                entityScanTick = 0;
                cachedEntities = mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(r), e -> {
                    if (e == mc.player || !e.isAlive()) return false;
                    if (e instanceof Player && !tracePlayers.getValue()) return false;
                    if (e instanceof Monster && !traceHostiles.getValue()) return false;
                    return e instanceof LivingEntity;
                });
            }
            List<Entity> entities = cachedEntities;

            if (entities.isEmpty()) return;

            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
            Matrix4f matrix = poseStack.last().pose();

            for (Entity e : entities) {
                float cr, cg, cb;
                if (e instanceof Player) { cr = 1.0f; cg = 0.2f; cb = 0.2f; }
                else if (e instanceof Monster) { cr = 1.0f; cg = 0.5f; cb = 0.0f; }
                else { cr = 0.2f; cg = 1.0f; cb = 0.2f; }

                // Interpolate entity eye position for smooth rendering
                double ix = e.xOld + (e.getX() - e.xOld) * partialTick;
                double iy = e.yOld + (e.getY() - e.yOld) * partialTick;
                double iz = e.zOld + (e.getZ() - e.zOld) * partialTick;
                float eyeOffset = e.getEyeHeight();
                Vec3 ePos = new Vec3(ix, iy + eyeOffset, iz);

                float dx = (float)(ePos.x - camPos.x);
                float dy = (float)(ePos.y - camPos.y);
                float dz = (float)(ePos.z - camPos.z);
                float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float nx = len < 0.001f ? 0 : dx / len;
                float ny = len < 0.001f ? 1 : dy / len;
                float nz = len < 0.001f ? 0 : dz / len;

                consumer.addVertex(matrix, (float) camPos.x, (float) camPos.y, (float) camPos.z)
                    .setColor(cr, cg, cb, 0.7f).setNormal(nx, ny, nz).setLineWidth(1.0f);
                consumer.addVertex(matrix, (float) ePos.x, (float) ePos.y, (float) ePos.z)
                    .setColor(cr, cg, cb, 0.7f).setNormal(nx, ny, nz).setLineWidth(1.0f);
            }

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }
}
