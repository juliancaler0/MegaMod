package com.ultra.megamod.feature.baritone.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Smooth head rotation toward targets. The bot looks where it's going
 * instead of snapping instantly, producing natural-looking movement.
 */
public class LookBehavior {
    private ServerPlayer player;
    private Vec3 targetLook = null;
    private boolean forceActive = false;
    private float maxRotSpeed = 8.0f; // degrees per tick — smooth head turns

    public LookBehavior(ServerPlayer player) {
        this.player = player;
    }

    public void updatePlayer(ServerPlayer player) {
        this.player = player;
    }

    /**
     * Set target to look at a block position (center of block).
     */
    public void lookAt(BlockPos pos) {
        lookAt(Vec3.atCenterOf(pos));
    }

    /**
     * Set target to look at an arbitrary world position.
     */
    public void lookAt(Vec3 target) {
        this.targetLook = target;
        this.forceActive = true;
    }

    /**
     * Clear the look target — stop rotating.
     */
    public void clear() {
        this.targetLook = null;
        this.forceActive = false;
    }

    /**
     * Called each tick. Smoothly interpolates yaw/pitch toward the target
     * at a maximum rate of {@link #maxRotSpeed} degrees per tick.
     * Also syncs yHeadRot and previous-tick rotation for smooth client interpolation.
     */
    public void tick() {
        if (!forceActive || targetLook == null || player == null) return;

        Vec3 eye = player.getEyePosition();
        double dx = targetLook.x - eye.x;
        double dy = targetLook.y - eye.y;
        double dz = targetLook.z - eye.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Target angles
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDist));
        targetPitch = Math.max(-90, Math.min(90, targetPitch));

        // Current angles
        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        // Shortest-arc yaw difference
        float yawDiff = targetYaw - currentYaw;
        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;
        float pitchDiff = targetPitch - currentPitch;

        // Ease-out: slow down as we approach the target for smoother feel
        float yawStep, pitchStep;
        if (Math.abs(yawDiff) < maxRotSpeed * 2) {
            // Within close range — use proportional easing (30% of remaining)
            yawStep = yawDiff * 0.3f;
            pitchStep = pitchDiff * 0.3f;
        } else {
            // Far away — use clamped max speed
            yawStep = Math.max(-maxRotSpeed, Math.min(maxRotSpeed, yawDiff));
            pitchStep = Math.max(-maxRotSpeed, Math.min(maxRotSpeed, pitchDiff));
        }

        float newYaw = currentYaw + yawStep;
        float newPitch = currentPitch + pitchStep;

        // Snap to exact target if close enough (prevents oscillation)
        if (Math.abs(yawDiff) < 0.5f && Math.abs(pitchDiff) < 0.5f) {
            newYaw = targetYaw;
            newPitch = targetPitch;
        }

        // Set previous-tick rotation BEFORE updating current for smooth client interpolation
        player.yRotO = currentYaw;
        player.xRotO = currentPitch;

        player.setYRot(newYaw);
        player.setXRot(newPitch);
        player.setYHeadRot(newYaw);
    }

    /**
     * Whether a look target is currently set.
     */
    public boolean isActive() {
        return forceActive;
    }

    /**
     * Adjust the maximum rotation speed (degrees per tick).
     * Default is 8.
     */
    public void setMaxRotSpeed(float degreesPerTick) {
        this.maxRotSpeed = degreesPerTick;
    }

    /**
     * Check if the player's look direction is within a tolerance cone of a target position.
     *
     * @param target    world position to check against
     * @param tolerance angle in degrees — how far off the player can be
     * @return true if the player is looking close enough
     */
    public boolean isLookingAt(Vec3 target, float tolerance) {
        if (player == null) return false;
        Vec3 look = player.getLookAngle();
        Vec3 toTarget = target.subtract(player.getEyePosition()).normalize();
        double dot = look.dot(toTarget);
        return dot > Math.cos(Math.toRadians(tolerance));
    }

    /**
     * Convenience: check if the player is looking at a block position within tolerance.
     */
    public boolean isLookingAt(BlockPos pos, float tolerance) {
        return isLookingAt(Vec3.atCenterOf(pos), tolerance);
    }
}
