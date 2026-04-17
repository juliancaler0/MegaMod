package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AbstractFadeModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AdjustmentModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.MirrorModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.SpeedModifier;
import com.ultra.megamod.lib.playeranim.core.easing.EasingType;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import com.ultra.megamod.lib.playeranim.core.math.Vec3f;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimationController;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationAccess;
import com.ultra.megamod.lib.playeranim.minecraft.animation.AvatarAnimManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Manages spell casting and combat animations per player using the PlayerAnimationLib system.
 * Ported from SpellEngine's AbstractClientPlayerEntityMixin animation logic.
 * <p>
 * Animation priorities:
 * - 950: Release animations (highest priority)
 * - 900: Casting/charging animations
 * - 200: Misc animations (dodge, etc.)
 */
public class SpellAnimationManager {

    private static final WeakHashMap<AbstractClientPlayer, PlayerAnimState> PLAYER_STATES = new WeakHashMap<>();

    private static final int CASTING_PRIORITY = 900;
    private static final int RELEASE_PRIORITY = 950;
    private static final int MISC_PRIORITY = 200;

    private static boolean eventRegistered = false;

    /**
     * Per-player animation state tracking using PlayerAnimationController instances.
     */
    private static class PlayerAnimState {
        final PlayerAnimationController castingController;
        final PlayerAnimationController releaseController;
        final PlayerAnimationController miscController;

        final SpeedModifier castingSpeed;
        final MirrorModifier castingMirror;
        final SpeedModifier releaseSpeed;
        final MirrorModifier releaseMirror;
        final SpeedModifier miscSpeed;
        final MirrorModifier miscMirror;

        boolean pitchEnabled = false;
        AbstractClientPlayer playerRef;

        // Dedup tracking: last (animId, speed, mirror) played per channel.
        // Prevents shimmy when the same animation is re-triggered via multiple code
        // paths (local mixin + server payload) — each replaceAnimationWithFade call
        // stacks a fresh fade modifier, producing perpetual fade-in. (Task #44)
        Identifier lastCastingId;      float lastCastingSpeed;  boolean lastCastingMirror;
        Identifier lastReleaseId;      float lastReleaseSpeed;  boolean lastReleaseMirror;
        Identifier lastMiscId;         float lastMiscSpeed;     boolean lastMiscMirror;

        PlayerAnimState(AbstractClientPlayer player) {
            this.playerRef = player;

            // All controllers use a simple pass-through handler
            var handler = (com.ultra.megamod.lib.playeranim.core.animation.AnimationController.AnimationStateHandler)
                    (controller, data, setter) -> PlayState.CONTINUE;

            // Casting controller with pitch adjustment
            castingController = new PlayerAnimationController(player, handler);
            castingSpeed = new SpeedModifier(1.0f);
            castingMirror = new MirrorModifier();
            castingMirror.enabled = false;
            castingController.addModifier(castingMirror, 0);
            castingController.addModifier(castingSpeed, 0);
            castingController.addModifierLast(createPitchAdjustment());

            // Release controller
            releaseController = new PlayerAnimationController(player, handler);
            releaseSpeed = new SpeedModifier(1.0f);
            releaseMirror = new MirrorModifier();
            releaseMirror.enabled = false;
            releaseController.addModifier(releaseMirror, 0);
            releaseController.addModifier(releaseSpeed, 0);

            // Misc controller
            miscController = new PlayerAnimationController(player, handler);
            miscSpeed = new SpeedModifier(1.0f);
            miscMirror = new MirrorModifier();
            miscMirror.enabled = false;
            miscController.addModifier(miscMirror, 0);
            miscController.addModifier(miscSpeed, 0);
        }

        private AdjustmentModifier createPitchAdjustment() {
            return new AdjustmentModifier((partName, data) -> {
                if (!pitchEnabled || playerRef == null) return Optional.empty();
                float pitch = (float) Math.toRadians(playerRef.getXRot());

                boolean firstPerson = data.isFirstPersonPass();
                if (firstPerson) {
                    return switch (partName) {
                        case "right_arm", "left_arm" ->
                                Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(pitch, 0, 0), Vec3f.ZERO));
                        default -> Optional.empty();
                    };
                } else {
                    float halfPitch = pitch / 2f;
                    return switch (partName) {
                        case "body" ->
                                Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(-halfPitch, 0, 0), Vec3f.ZERO));
                        case "right_arm", "left_arm" ->
                                Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(halfPitch, 0, 0), Vec3f.ZERO));
                        case "right_leg", "left_leg" ->
                                Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(-halfPitch, 0, 0), Vec3f.ZERO));
                        default -> Optional.empty();
                    };
                }
            });
        }
    }

    /**
     * Register the animation event listener. Call once during client init.
     */
    public static void registerFactories() {
        if (eventRegistered) return;
        eventRegistered = true;

        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((avatar, manager) -> {
            if (avatar instanceof AbstractClientPlayer clientPlayer) {
                PlayerAnimState state = new PlayerAnimState(clientPlayer);
                PLAYER_STATES.put(clientPlayer, state);

                manager.addAnimLayer(MISC_PRIORITY, state.miscController);
                manager.addAnimLayer(CASTING_PRIORITY, state.castingController);
                manager.addAnimLayer(RELEASE_PRIORITY, state.releaseController);
            }
        });
    }

    /**
     * Get or lazily create the animation state for a player.
     */
    private static @Nullable PlayerAnimState getState(AbstractClientPlayer player) {
        PlayerAnimState state = PLAYER_STATES.get(player);
        if (state == null) {
            state = new PlayerAnimState(player);
            PLAYER_STATES.put(player, state);
            try {
                AvatarAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager(player);
                manager.addAnimLayer(MISC_PRIORITY, state.miscController);
                manager.addAnimLayer(CASTING_PRIORITY, state.castingController);
                manager.addAnimLayer(RELEASE_PRIORITY, state.releaseController);
            } catch (IllegalArgumentException e) {
                PLAYER_STATES.remove(player);
                return null;
            }
        }
        return state;
    }

    /**
     * Play a spell animation on a player.
     *
     * @param player the player
     * @param type animation type (CASTING, RELEASE, MISC)
     * @param animationId identifier like "megamod:one_handed_projectile_charge"
     * @param speed playback speed multiplier
     * @param mirror whether to mirror the animation (left-handed)
     */
    public static void playAnimation(AbstractClientPlayer player, AnimationType type,
                                     Identifier animationId, float speed, boolean mirror) {
        PlayerAnimState state = getState(player);
        if (state == null) return;
        if (!PlayerAnimResources.hasAnimation(animationId)) {
            MegaMod.LOGGER.warn("[Spell] Animation not found: {} (caster: {}, type: {})",
                    animationId, player.getGameProfile().name(), type);
            return;
        }

        // Dedup guard: if the same animation is already playing at the same speed/mirror
        // on this channel, skip. Without this, repeated calls (e.g. server payload echo
        // back to caster on top of local mixin drive) stack fade modifiers → shimmy. (Task #44)
        switch (type) {
            case CASTING -> {
                if (animationId.equals(state.lastCastingId) && speed == state.lastCastingSpeed && mirror == state.lastCastingMirror) return;
                state.lastCastingId = animationId; state.lastCastingSpeed = speed; state.lastCastingMirror = mirror;
            }
            case RELEASE -> {
                if (animationId.equals(state.lastReleaseId) && speed == state.lastReleaseSpeed && mirror == state.lastReleaseMirror) return;
                state.lastReleaseId = animationId; state.lastReleaseSpeed = speed; state.lastReleaseMirror = mirror;
            }
            case MISC -> {
                if (animationId.equals(state.lastMiscId) && speed == state.lastMiscSpeed && mirror == state.lastMiscMirror) return;
                state.lastMiscId = animationId; state.lastMiscSpeed = speed; state.lastMiscMirror = mirror;
            }
        }

        PlayerAnimationController controller = switch (type) {
            case CASTING -> state.castingController;
            case RELEASE -> state.releaseController;
            case MISC -> state.miscController;
        };

        SpeedModifier speedMod = switch (type) {
            case CASTING -> state.castingSpeed;
            case RELEASE -> state.releaseSpeed;
            case MISC -> state.miscSpeed;
        };

        MirrorModifier mirrorMod = switch (type) {
            case CASTING -> state.castingMirror;
            case RELEASE -> state.releaseMirror;
            case MISC -> state.miscMirror;
        };

        // Enable pitch tracking for casting animations
        state.pitchEnabled = (type == AnimationType.CASTING);

        // Set modifiers
        speedMod.speed = speed;
        mirrorMod.enabled = mirror;

        // Match source SpellEngine: use triggerAnimation (one-shot, no fade-stacking).
        // replaceAnimationWithFade stacked fade modifiers on each re-trigger, which is
        // exactly what the animation jitter looks like (fade-in restarting per tick).
        controller.triggerAnimation(animationId);
    }

    /**
     * Stop a specific animation type for a player.
     */
    public static void stopAnimation(AbstractClientPlayer player, AnimationType type) {
        PlayerAnimState state = PLAYER_STATES.get(player);
        if (state == null) return;

        PlayerAnimationController controller = switch (type) {
            case CASTING -> state.castingController;
            case RELEASE -> state.releaseController;
            case MISC -> state.miscController;
        };

        // Clear dedup memory so the next playAnimation call isn't suppressed. (Task #44)
        switch (type) {
            case CASTING -> { state.lastCastingId = null; state.lastCastingSpeed = 0f; state.lastCastingMirror = false; }
            case RELEASE -> { state.lastReleaseId = null; state.lastReleaseSpeed = 0f; state.lastReleaseMirror = false; }
            case MISC    -> { state.lastMiscId = null;    state.lastMiscSpeed = 0f;    state.lastMiscMirror = false; }
        }

        controller.stop();
    }

    /**
     * Returns {@code true} if any spell animation channel (casting/release/misc)
     * is currently playing for the given player. Used by pose stack suppression (task #42).
     */
    public static boolean isAnyActive(AbstractClientPlayer player) {
        PlayerAnimState state = PLAYER_STATES.get(player);
        if (state == null) return false;
        return state.castingController.isActive()
                || state.releaseController.isActive()
                || state.miscController.isActive();
    }

    /**
     * Stop all animations for a player.
     */
    public static void stopAll(AbstractClientPlayer player) {
        stopAnimation(player, AnimationType.CASTING);
        stopAnimation(player, AnimationType.RELEASE);
        stopAnimation(player, AnimationType.MISC);
    }

    /**
     * Update casting animation speed (e.g., when haste changes).
     */
    public static void updateCastSpeed(AbstractClientPlayer player, float speed) {
        PlayerAnimState state = PLAYER_STATES.get(player);
        if (state != null) {
            state.castingSpeed.speed = speed;
        }
    }

    /**
     * Play a dodge animation.
     */
    public static void playDodge(AbstractClientPlayer player) {
        playAnimation(player, AnimationType.MISC,
                Identifier.fromNamespaceAndPath("megamod", "dodge"), 1.0f, false);
    }

    public enum AnimationType {
        CASTING,
        RELEASE,
        MISC
    }
}
