package com.ultra.megamod.feature.adminmodules.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class TrajectoriesModule extends AdminModule {
    private ModuleSetting.IntSetting maxSteps;

    public TrajectoriesModule() {
        super("trajectories", "Trajectories", "Shows projectile path prediction for bows, crossbows, tridents, ender pearls, and snowballs", ModuleCategory.RENDER);
    }

    @Override
    protected void initSettings() {
        maxSteps = integer("Steps", 120, 30, 300, "Max simulation steps for trajectory line");
    }

    @Override public boolean isServerSide() { return false; }
    @Override public boolean isClientSide() { return true; }

    @Override
    public void onRenderWorld(Object eventObj) {
        if (!(eventObj instanceof RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        var mainHand = mc.player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        Item item = mainHand.getItem();
        boolean isProjectile = item instanceof BowItem
            || item instanceof CrossbowItem
            || item instanceof TridentItem
            || item instanceof EnderpearlItem
            || item instanceof SnowballItem
            || item instanceof EggItem;
        if (!isProjectile) return;

        PoseStack poseStack = event.getPoseStack();
        try {
            // Determine projectile physics based on item type
            double speed;
            double gravity;
            double drag;

            if (item instanceof BowItem) {
                speed = 3.0;
                gravity = 0.05;
                drag = 0.99;
            } else if (item instanceof CrossbowItem) {
                speed = 3.15;
                gravity = 0.05;
                drag = 0.99;
            } else if (item instanceof TridentItem) {
                speed = 2.5;
                gravity = 0.05;
                drag = 0.99;
            } else {
                speed = 1.5;
                gravity = 0.03;
                drag = 0.99;
            }

            Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
            Vec3 pos = mc.player.getEyePosition();
            Vec3 look = mc.player.getLookAngle();
            Vec3 vel = look.scale(speed);

            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

            poseStack.pushPose();
            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

            VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());
            Matrix4f matrix = poseStack.last().pose();

            int steps = maxSteps.getValue();
            for (int i = 0; i < steps; i++) {
                Vec3 next = pos.add(vel);
                vel = new Vec3(vel.x * drag, (vel.y - gravity) * drag, vel.z * drag);

                float dx = (float)(next.x - pos.x);
                float dy = (float)(next.y - pos.y);
                float dz = (float)(next.z - pos.z);
                float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                float nx = len < 0.001f ? 0 : dx / len;
                float ny = len < 0.001f ? 1 : dy / len;
                float nz = len < 0.001f ? 0 : dz / len;

                consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                    .setColor(1.0f, 1.0f, 0.0f, 0.8f).setNormal(nx, ny, nz).setLineWidth(1.0f);
                consumer.addVertex(matrix, (float) next.x, (float) next.y, (float) next.z)
                    .setColor(1.0f, 1.0f, 0.0f, 0.8f).setNormal(nx, ny, nz).setLineWidth(1.0f);

                BlockPos blockPos = BlockPos.containing(next);
                if (!mc.level.getBlockState(blockPos).isAir()) break;

                pos = next;
                if (pos.y < mc.level.getMinY()) break;
            }

            poseStack.popPose();
            bufferSource.endBatch();
        } catch (Exception e) {
            try { poseStack.popPose(); } catch (Exception ignored) {}
        }
    }
}
