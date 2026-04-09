package com.ultra.megamod.feature.combat.animation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.SwingDirection;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Quaternionf;

/**
 * Client-side renderer that modifies first-person hand rendering during weapon swings.
 * <p>
 * Hooks into {@link RenderHandEvent} to apply PoseStack transformations that visually
 * represent different swing directions. Each direction has a distinct three-phase animation:
 * <ol>
 *   <li><b>Wind-up (0.0-0.3):</b> Arm moves to the starting position of the swing</li>
 *   <li><b>Attack (0.3-0.7):</b> Arm sweeps through the attack arc</li>
 *   <li><b>Recovery (0.7-1.0):</b> Arm returns to the neutral idle position</li>
 * </ol>
 * <p>
 * Transformations are applied via {@link PoseStack#mulPose(Quaternionf)} using axis-angle
 * rotations, with smooth easing to avoid jerky transitions between phases.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SwingAnimationRenderer {

    // ── Constants ─────────────────────────────────────────────────────────

    /** Maximum rotation angle in degrees for slash animations. */
    private static final float SLASH_MAX_ANGLE = 80f;
    /** Maximum forward thrust distance for stab animations. */
    private static final float STAB_MAX_DISTANCE = 0.6f;
    /** Maximum vertical lift for uppercut animations. */
    private static final float UPPERCUT_MAX_ANGLE = 90f;
    /** Degrees of rotation per full spin cycle. */
    private static final float SPIN_FULL_ROTATION = 360f;

    // ── First-person hand rendering ───────────────────────────────────────

    /**
     * Intercepts first-person hand rendering to apply swing animation transforms.
     * Only modifies the pose when the local player has an active swing animation.
     * <p>
     * Also suppresses vanilla's built-in swing animation by resetting
     * {@code swingTime} on the player when our custom animation is active.
     * This prevents double-animation from the server-side {@code player.attack()}
     * call which triggers a vanilla swing sync packet.
     */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int entityId = mc.player.getId();
        SwingAnimationState.ActiveSwing swing = SwingAnimationState.getActiveSwing(entityId);
        if (swing == null || swing.isExpired()) return;

        // Suppress vanilla's swing animation every render frame while our custom
        // swing is active. The server's player.attack() call sends a
        // ClientboundAnimatePacket that sets swingTime/swinging on the client.
        // We override those here so vanilla's arm-bob doesn't interfere.
        mc.player.swingTime = 0;
        mc.player.swinging = false;

        // Only modify the hand matching the swing's hand
        boolean isMainHand = event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND;
        if (swing.isOffHand() == isMainHand) return;

        float progress = swing.progress();
        PoseStack poseStack = event.getPoseStack();

        // Apply transforms directly to the current pose stack entry so they
        // affect the vanilla hand render that follows. This is the same pattern
        // used by AdminModuleClientHooks.onRenderHand for HandView transforms.
        applySwingTransform(poseStack, swing.direction(), progress, swing.isOffHand(), swing.twoHanded());
    }

    // ── Third-person model animation via client tick ──────────────────────

    /**
     * Every client tick, applies arm pose overrides to nearby players who have active swings.
     * This drives the third-person visual by adjusting arm rotation targets that the
     * player model interpolates toward.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Prune expired swings
        SwingAnimationState.tick();
    }

    // ── Transform application per swing direction ─────────────────────────

    /**
     * Applies PoseStack rotations/translations for the given swing direction and progress.
     *
     * @param poseStack The current pose stack to transform.
     * @param direction The swing direction determining the animation arc.
     * @param progress  Animation progress from 0.0 (start) to 1.0 (complete).
     * @param offHand   True if the off-hand is swinging (mirrors horizontal movements).
     * @param twoHanded True if both arms should move (increases translation slightly).
     */
    private static void applySwingTransform(PoseStack poseStack, SwingDirection direction,
                                            float progress, boolean offHand, boolean twoHanded) {
        float mirror = offHand ? -1f : 1f;
        float twoHandedScale = twoHanded ? 1.15f : 1f;

        switch (direction) {
            case SLASH_RIGHT -> applySlashRight(poseStack, progress, mirror, twoHandedScale);
            case SLASH_LEFT -> applySlashLeft(poseStack, progress, mirror, twoHandedScale);
            case SLASH_DOWN -> applySlashDown(poseStack, progress, twoHandedScale);
            case STAB -> applyStab(poseStack, progress, twoHandedScale);
            case UPPERCUT -> applyUppercut(poseStack, progress, twoHandedScale);
            case SPIN -> applySpin(poseStack, progress, mirror);
        }
    }

    /**
     * SLASH_RIGHT: Arm sweeps from upper-right to lower-left in a diagonal arc.
     * The hand starts raised to the right and swings down across the body.
     */
    private static void applySlashRight(PoseStack ps, float progress, float mirror, float scale) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Raise arm to upper-right starting position
            float windupAngle = easeOutQuad(phase) * SLASH_MAX_ANGLE * 0.4f * scale;
            ps.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-windupAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-windupAngle * 0.3f)));
            ps.translate(0.0, -0.05 * easeOutQuad(phase) * scale, 0.0);

        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Sweep from upper-right to lower-left
            float attackAngle = easeInOutQuad(phase) * SLASH_MAX_ANGLE * scale;
            float startAngle = SLASH_MAX_ANGLE * 0.4f * scale;
            float currentAngle = startAngle - attackAngle;
            ps.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(currentAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(attackAngle * 0.25f)));
            ps.translate(0.05 * phase * mirror, 0.08 * easeInOutQuad(phase) * scale, -0.04 * phase);

        } else {
            // Recovery: Return to neutral
            float remaining = 1f - easeOutQuad(phase);
            float recoveryAngle = -(SLASH_MAX_ANGLE * 0.6f) * remaining * scale;
            ps.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(recoveryAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(SLASH_MAX_ANGLE * 0.25f * remaining)));
            ps.translate(0.05 * remaining * mirror, 0.08 * remaining * scale, -0.04 * remaining);
        }
    }

    /**
     * SLASH_LEFT: Arm sweeps from upper-left to lower-right — the mirror of SLASH_RIGHT.
     * The hand starts raised to the left and swings diagonally downward.
     */
    private static void applySlashLeft(PoseStack ps, float progress, float mirror, float scale) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Raise arm to upper-left starting position
            float windupAngle = easeOutQuad(phase) * SLASH_MAX_ANGLE * 0.4f * scale;
            ps.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(windupAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-windupAngle * 0.3f)));
            ps.translate(0.0, -0.05 * easeOutQuad(phase) * scale, 0.0);

        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Sweep from upper-left to lower-right
            float attackAngle = easeInOutQuad(phase) * SLASH_MAX_ANGLE * scale;
            float startAngle = SLASH_MAX_ANGLE * 0.4f * scale;
            float currentAngle = -(startAngle - attackAngle);
            ps.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(currentAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(attackAngle * 0.25f)));
            ps.translate(-0.05 * phase * mirror, 0.08 * easeInOutQuad(phase) * scale, -0.04 * phase);

        } else {
            // Recovery: Return to neutral
            float remaining = 1f - easeOutQuad(phase);
            float recoveryAngle = (SLASH_MAX_ANGLE * 0.6f) * remaining * scale;
            ps.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(recoveryAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(SLASH_MAX_ANGLE * 0.25f * remaining)));
            ps.translate(-0.05 * remaining * mirror, 0.08 * remaining * scale, -0.04 * remaining);
        }
    }

    /**
     * SLASH_DOWN: Overhead chop — arm raises above head then swings straight down.
     * A powerful, vertical strike motion.
     */
    private static void applySlashDown(PoseStack ps, float progress, float scale) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Raise arm overhead
            float windupAngle = easeOutQuad(phase) * 70f * scale;
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-windupAngle)));
            ps.translate(0.0, -0.12 * easeOutQuad(phase) * scale, -0.06 * easeOutQuad(phase));

        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Chop downward through the arc
            float attackAngle = easeInQuad(phase) * 140f * scale;
            float currentAngle = -70f * scale + attackAngle;
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(currentAngle)));
            float verticalOffset = -0.12f * scale + 0.2f * easeInQuad(phase) * scale;
            ps.translate(0.0, verticalOffset, -0.06 + 0.15 * easeInQuad(phase));

        } else {
            // Recovery: Return from low position back to idle
            float remaining = 1f - easeOutQuad(phase);
            float recoveryAngle = 70f * remaining * scale;
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(recoveryAngle)));
            ps.translate(0.0, 0.08 * remaining * scale, 0.09 * remaining);
        }
    }

    /**
     * STAB: Forward thrust — arm pulls back then lunges forward along the Z axis.
     * Narrow, linear motion ideal for rapiers, spears, and daggers.
     */
    private static void applyStab(PoseStack ps, float progress, float scale) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Pull arm back
            float pullback = easeOutQuad(phase) * STAB_MAX_DISTANCE * 0.5f * scale;
            ps.translate(0.0, 0.0, pullback);
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-8f * easeOutQuad(phase))));

        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Thrust forward past neutral
            float thrust = easeInOutQuad(phase) * STAB_MAX_DISTANCE * 1.5f * scale;
            float offset = STAB_MAX_DISTANCE * 0.5f * scale - thrust;
            ps.translate(0.0, -0.03 * phase, offset);
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-8f + 16f * easeInOutQuad(phase))));

        } else {
            // Recovery: Retract to idle
            float remaining = 1f - easeOutQuad(phase);
            float retract = -STAB_MAX_DISTANCE * scale * remaining;
            ps.translate(0.0, -0.03 * remaining, retract);
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(8f * remaining)));
        }
    }

    /**
     * UPPERCUT: Upward sweep — arm starts low and swings up through the target.
     * A rising strike that lifts from hip level to above the head.
     */
    private static void applyUppercut(PoseStack ps, float progress, float scale) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Drop arm to low starting position
            float dropAngle = easeOutQuad(phase) * 45f * scale;
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(dropAngle)));
            ps.translate(0.0, 0.06 * easeOutQuad(phase) * scale, 0.0);

        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Sweep upward through the full arc
            float sweepAngle = easeInOutQuad(phase) * UPPERCUT_MAX_ANGLE * 1.5f * scale;
            float currentAngle = 45f * scale - sweepAngle;
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(currentAngle)));
            float verticalOffset = 0.06f * scale - 0.18f * easeInOutQuad(phase) * scale;
            ps.translate(0.0, verticalOffset, -0.08 * easeInOutQuad(phase));

        } else {
            // Recovery: Return from raised position to idle
            float remaining = 1f - easeOutQuad(phase);
            float returnAngle = -(UPPERCUT_MAX_ANGLE * 1.5f - 45f) * remaining * scale;
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(returnAngle)));
            ps.translate(0.0, -0.12 * remaining * scale, -0.08 * remaining);
        }
    }

    /**
     * SPIN: Full 360-degree rotation of the view/arm around the Y axis.
     * A dramatic sweeping attack that hits everything around the player.
     */
    private static void applySpin(PoseStack ps, float progress, float mirror) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Slight pull and initial rotation
            float windupRot = easeOutQuad(phase) * 30f * mirror;
            ps.mulPose(new Quaternionf().rotateY((float) Math.toRadians(windupRot)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-10f * easeOutQuad(phase))));
            ps.translate(0.0, -0.03 * easeOutQuad(phase), 0.0);

        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Full spin rotation
            float spinAngle = 30f + easeInOutQuad(phase) * (SPIN_FULL_ROTATION - 60f);
            ps.mulPose(new Quaternionf().rotateY((float) Math.toRadians(spinAngle * mirror)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-10f + 5f * (float) Math.sin(phase * Math.PI))));
            ps.translate(0.0, -0.03 + 0.01 * Math.sin(phase * Math.PI), -0.02 * phase);

        } else {
            // Recovery: Complete the rotation and settle
            float remaining = 1f - easeOutQuad(phase);
            float finalAngle = (SPIN_FULL_ROTATION - 30f) * mirror;
            float recoveryAngle = finalAngle + 30f * easeOutQuad(phase) * mirror;
            ps.mulPose(new Quaternionf().rotateY((float) Math.toRadians(recoveryAngle)));
            ps.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-5f * remaining)));
            ps.translate(0.0, -0.03 * remaining, -0.02 * remaining);
        }
    }

    // ── Easing functions ──────────────────────────────────────────────────

    /**
     * Returns the progress within the current animation phase (0.0-1.0).
     * Splits the overall progress into wind-up, attack, and recovery phases.
     */
    private static float getPhaseProgress(float overallProgress) {
        if (overallProgress < SwingAnimationState.WINDUP_END) {
            return overallProgress / SwingAnimationState.WINDUP_END;
        } else if (overallProgress < SwingAnimationState.ATTACK_END) {
            return (overallProgress - SwingAnimationState.WINDUP_END)
                    / (SwingAnimationState.ATTACK_END - SwingAnimationState.WINDUP_END);
        } else {
            return (overallProgress - SwingAnimationState.ATTACK_END)
                    / (1f - SwingAnimationState.ATTACK_END);
        }
    }

    /** Quadratic ease-out: decelerates toward the end. */
    private static float easeOutQuad(float t) {
        return t * (2f - t);
    }

    /** Quadratic ease-in: accelerates from the start. */
    private static float easeInQuad(float t) {
        return t * t;
    }

    /** Quadratic ease-in-out: smooth acceleration and deceleration. */
    private static float easeInOutQuad(float t) {
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }
}
