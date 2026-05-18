package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes.SwingDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.UUID;

/**
 * Applies third-person weapon swing animations to player models as a FALLBACK
 * for cases where no PlayerAnimationLib keyframe animation is currently active.
 * <p>
 * The entity ID of the player being rendered is captured by {@code AvatarRendererMixin}
 * during {@code extractRenderState} and stored in a ThreadLocal. After vanilla's
 * {@code setupAnim} sets default poses, {@code HumanoidModelSwingMixin} calls
 * {@link #applySwingIfActive} which modifies the model's arm and body rotations
 * to match the active swing direction. Then PAL's {@code PlayerModelMixin} runs
 * at {@code setupAnim} RETURN (priority 2001) and overwrites those rotations
 * with the active keyframe animation if any. Net effect: PAL animations win
 * when present, this fallback shows when nothing else is playing (e.g. for
 * weapons without a BetterCombat attack animation registered).
 * <p>
 * Spell-cast poses are NOT handled here — spell animations are driven entirely
 * by PAL controllers via {@link SpellAnimationManager}, which is layered onto
 * the PAL animation manager for every player.
 */
public class ThirdPersonSwingAnimator {

    /**
     * ThreadLocal storing the entity ID of the player currently being rendered.
     * Set by AvatarRendererMixin.extractRenderState, consumed by applySwingIfActive.
     * Value of -1 means no player is being rendered (prevents stale reads for
     * non-player humanoid models like zombies/villagers that share HumanoidModel).
     */
    private static final ThreadLocal<Integer> RENDERING_ENTITY_ID = ThreadLocal.withInitial(() -> -1);
    private static final ThreadLocal<UUID> RENDERING_ENTITY_UUID = new ThreadLocal<>();

    /**
     * Called from AvatarRendererMixin.extractRenderState to capture which player is being rendered.
     */
    public static void setRenderingEntityId(int id) {
        RENDERING_ENTITY_ID.set(id);
    }

    public static void setRenderingEntityUUID(UUID uuid) {
        RENDERING_ENTITY_UUID.set(uuid);
    }

    /**
     * Called from HumanoidModelSwingMixin at the TAIL of setupAnim.
     * Checks if the currently-rendering player has an active swing and applies arm/body overrides.
     *
     * @param modelObj the HumanoidModel (cast needed due to mixin generics)
     */
    public static void applySwingIfActive(Object modelObj) {
        int entityId = RENDERING_ENTITY_ID.get();
        if (entityId < 0) return;
        // Consume both thread-locals so the next setupAnim on a non-player humanoid
        // (zombie/villager/illager/…) reads -1 and harmlessly bails out instead of
        // inheriting this player's swing pose.
        RENDERING_ENTITY_ID.set(-1);
        RENDERING_ENTITY_UUID.set(null);

        if (!(modelObj instanceof HumanoidModel<?> model)) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isLocalFirstPerson = mc.player != null && entityId == mc.player.getId()
                && mc.options.getCameraType().isFirstPerson();

        // First-person camera: PAL's first-person config controls visibility; no
        // need (and undesirable) to overlay a third-person swing on the FP model.
        if (isLocalFirstPerson) return;

        SwingAnimationState.ActiveSwing swing = SwingAnimationState.getActiveSwing(entityId);
        if (swing != null && !swing.isExpired()) {
            applyArmAnimation(model, swing.direction(), swing.progress(), swing.isOffHand(), swing.twoHanded());
        }
    }

    // ── Core dispatch ────────────────────────────────────────────────────

    private static void applyArmAnimation(HumanoidModel<?> model, SwingDirection direction,
                                           float progress, boolean offHand, boolean twoHanded) {
        ModelPart mainArm = offHand ? model.leftArm : model.rightArm;
        ModelPart otherArm = offHand ? model.rightArm : model.leftArm;
        float mirror = offHand ? -1f : 1f;

        switch (direction) {
            case SLASH_RIGHT -> slashHorizontal(model, mainArm, otherArm, progress, mirror, twoHanded, false);
            case SLASH_LEFT  -> slashHorizontal(model, mainArm, otherArm, progress, mirror, twoHanded, true);
            case SLASH_DOWN  -> slashDown(model, mainArm, otherArm, progress, twoHanded);
            case STAB        -> stab(model, mainArm, otherArm, progress, twoHanded);
            case UPPERCUT    -> uppercut(model, mainArm, otherArm, progress, twoHanded);
            case SPIN        -> spin(model, mainArm, otherArm, progress, mirror);
        }
    }

    // ── Swing direction implementations ──────────────────────────────────

    /**
     * Horizontal slash — arm sweeps sideways across the body.
     * Used for SLASH_RIGHT (reversed=false) and SLASH_LEFT (reversed=true).
     */
    private static void slashHorizontal(HumanoidModel<?> model, ModelPart mainArm, ModelPart otherArm,
                                         float progress, float mirror, boolean twoHanded, boolean reversed) {
        float phase = getPhaseProgress(progress);
        float dir = reversed ? -1f : 1f;

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Arm raises to starting position
            float ease = easeOutQuad(phase);
            mainArm.xRot = lerp(mainArm.xRot, -1.8f, ease);             // Arm up ~103°
            mainArm.zRot = lerp(mainArm.zRot, -0.6f * dir * mirror, ease); // Arm out to side
            model.body.yRot += 0.35f * dir * mirror * ease;                // Body winds up

            if (twoHanded) {
                otherArm.xRot = lerp(otherArm.xRot, -1.5f, ease * 0.7f);
                otherArm.zRot = lerp(otherArm.zRot, 0.3f * dir * mirror, ease * 0.5f);
            }
        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Arm sweeps across body
            float ease = easeInOutQuad(phase);
            mainArm.xRot = -1.5f + 0.3f * ease;                          // Stays roughly horizontal
            mainArm.zRot = lerp(-0.6f * dir * mirror, 0.7f * dir * mirror, ease); // Sweeps across
            model.body.yRot += lerp(0.35f, -0.45f, ease) * dir * mirror;  // Body follows through

            if (twoHanded) {
                otherArm.xRot = -1.3f + 0.2f * ease;
                otherArm.zRot = lerp(0.3f * dir * mirror, -0.4f * dir * mirror, ease);
            }
        } else {
            // Recovery: Return to neutral
            float ease = easeOutQuad(phase);
            float remaining = 1f - ease;
            mainArm.xRot = lerp(0f, -1.2f, remaining);
            mainArm.zRot = lerp(0f, 0.7f * dir * mirror, remaining);
            model.body.yRot += -0.45f * dir * mirror * remaining;

            if (twoHanded) {
                otherArm.xRot = lerp(0f, -1.1f, remaining);
                otherArm.zRot = lerp(0f, -0.4f * dir * mirror, remaining);
            }
        }
    }

    /**
     * Overhead chop — arm raises above head then slams down.
     */
    private static void slashDown(HumanoidModel<?> model, ModelPart mainArm, ModelPart otherArm,
                                   float progress, boolean twoHanded) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Raise arm overhead
            float ease = easeOutQuad(phase);
            mainArm.xRot = lerp(mainArm.xRot, -2.8f, ease);  // Arm almost straight up
            mainArm.zRot = lerp(mainArm.zRot, 0f, ease);
            model.body.xRot -= 0.1f * ease; // Slight lean back

            if (twoHanded) {
                otherArm.xRot = lerp(otherArm.xRot, -2.6f, ease * 0.8f);
            }
        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Chop downward
            float ease = easeInQuad(phase);
            mainArm.xRot = lerp(-2.8f, -0.3f, ease);  // Arm swings down past horizontal
            model.body.xRot += lerp(-0.1f, 0.15f, ease); // Lean forward

            if (twoHanded) {
                otherArm.xRot = lerp(-2.6f, -0.5f, ease);
            }
        } else {
            // Recovery
            float remaining = 1f - easeOutQuad(phase);
            mainArm.xRot = lerp(0f, -0.3f, remaining);
            model.body.xRot += 0.15f * remaining;

            if (twoHanded) {
                otherArm.xRot = lerp(0f, -0.5f, remaining);
            }
        }
    }

    /**
     * Forward thrust — arm stabs forward along the Z axis.
     */
    private static void stab(HumanoidModel<?> model, ModelPart mainArm, ModelPart otherArm,
                              float progress, boolean twoHanded) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Pull arm back
            float ease = easeOutQuad(phase);
            mainArm.xRot = lerp(mainArm.xRot, -0.8f, ease);  // Arm slightly raised back
            model.body.xRot -= 0.06f * ease; // Slight lean back

            if (twoHanded) {
                otherArm.xRot = lerp(otherArm.xRot, -0.6f, ease * 0.6f);
            }
        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Thrust forward
            float ease = easeInOutQuad(phase);
            mainArm.xRot = lerp(-0.8f, -1.57f, ease);  // Arm extends straight forward (-PI/2)
            model.body.xRot += lerp(-0.06f, 0.12f, ease); // Lean forward into thrust

            if (twoHanded) {
                otherArm.xRot = lerp(-0.6f, -1.4f, ease);
            }
        } else {
            // Recovery
            float remaining = 1f - easeOutQuad(phase);
            mainArm.xRot = lerp(0f, -1.57f, remaining);
            model.body.xRot += 0.12f * remaining;

            if (twoHanded) {
                otherArm.xRot = lerp(0f, -1.4f, remaining);
            }
        }
    }

    /**
     * Upward sweep — arm swings from low to high.
     */
    private static void uppercut(HumanoidModel<?> model, ModelPart mainArm, ModelPart otherArm,
                                  float progress, boolean twoHanded) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Drop arm low
            float ease = easeOutQuad(phase);
            mainArm.xRot = lerp(mainArm.xRot, 0.4f, ease);  // Arm hangs below neutral
            mainArm.zRot = lerp(mainArm.zRot, -0.2f, ease);
            model.body.xRot += 0.08f * ease; // Slight crouch

            if (twoHanded) {
                otherArm.xRot = lerp(otherArm.xRot, 0.3f, ease * 0.7f);
            }
        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Sweep upward
            float ease = easeInOutQuad(phase);
            mainArm.xRot = lerp(0.4f, -2.5f, ease);  // Arm sweeps up past vertical
            mainArm.zRot = lerp(-0.2f, 0f, ease);
            model.body.xRot += lerp(0.08f, -0.1f, ease); // Stand up / lean back

            if (twoHanded) {
                otherArm.xRot = lerp(0.3f, -2.2f, ease);
            }
        } else {
            // Recovery
            float remaining = 1f - easeOutQuad(phase);
            mainArm.xRot = lerp(0f, -2.5f, remaining);
            model.body.xRot -= 0.1f * remaining;

            if (twoHanded) {
                otherArm.xRot = lerp(0f, -2.2f, remaining);
            }
        }
    }

    /**
     * Full 360° spin — body rotates, arms extend outward.
     */
    private static void spin(HumanoidModel<?> model, ModelPart mainArm, ModelPart otherArm,
                              float progress, float mirror) {
        float phase = getPhaseProgress(progress);

        if (progress < SwingAnimationState.WINDUP_END) {
            // Wind-up: Arms spread, slight body wind-up
            float ease = easeOutQuad(phase);
            mainArm.xRot = lerp(mainArm.xRot, -1.2f, ease);
            mainArm.zRot = lerp(mainArm.zRot, -0.5f * mirror, ease);
            otherArm.xRot = lerp(otherArm.xRot, -1.2f, ease);
            otherArm.zRot = lerp(otherArm.zRot, 0.5f * mirror, ease);
            model.body.yRot += 0.4f * mirror * ease;
        } else if (progress < SwingAnimationState.ATTACK_END) {
            // Attack: Full spin
            float spinAngle = (float) (ease(phase) * Math.PI * 2.0); // 0 to 2PI
            mainArm.xRot = -1.2f;
            mainArm.zRot = -0.6f * mirror;
            otherArm.xRot = -1.2f;
            otherArm.zRot = 0.6f * mirror;
            model.body.yRot += spinAngle * mirror;
        } else {
            // Recovery: Settle
            float remaining = 1f - easeOutQuad(phase);
            mainArm.xRot = lerp(0f, -1.2f, remaining);
            mainArm.zRot = lerp(0f, -0.6f * mirror, remaining);
            otherArm.xRot = lerp(0f, -1.2f, remaining);
            otherArm.zRot = lerp(0f, 0.6f * mirror, remaining);
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────

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

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    private static float easeOutQuad(float t) {
        return t * (2f - t);
    }

    private static float easeInQuad(float t) {
        return t * t;
    }

    private static float easeInOutQuad(float t) {
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }

    /** Smooth ease for spin rotation. */
    private static float ease(float t) {
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
    }
}
