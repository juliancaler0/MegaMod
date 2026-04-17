package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.lib.playeranim.core.animation.AnimationData;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.AdjustmentModifier;
import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.MirrorModifier;
import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonConfiguration;
import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonMode;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import com.ultra.megamod.lib.playeranim.core.math.Vec3f;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimationController;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Attack animation controller extending the REAL PlayerAnimationController.
 * Ported 1:1 from BetterCombat's AttackAnimationStack.
 * Uses triggerAnimation() for proper player body animation.
 */
public class AttackAnimationStack extends PlayerAnimationController {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("megamod", "attack");
    public static final int PRIORITY = 2000;

    public final TransmissionSpeedModifier speed = new TransmissionSpeedModifier(1F);
    public final MirrorModifier mirror = new MirrorModifier();
    public FirstPersonConfiguration activeFirstPersonConfig = new FirstPersonConfiguration();

    public AttackAnimationStack(Player player, AnimationStateHandler animationHandler) {
        super(player, animationHandler);
        postInit();
    }

    private void postInit() {
        this.addModifier(mirror, 0);
        this.addModifier(speed, 0);
        this.addModifierLast(createAttackAdjustment());

        this.firstPersonMode = (controller) -> FirstPersonAnimationCompat.firstPersonMode();
        this.firstPersonConfiguration = (controller) -> this.activeFirstPersonConfig;

        this.setPostAnimationSetupConsumer((func) -> {
            func.apply("torso").setEnabled(true);
            func.apply("head").rotXEnabled = false;

            var player = this.getAvatar();
            boolean disableLegs = false;

            if (player.getPose() == Pose.SWIMMING) disableLegs = true;
            if (player.getVehicle() != null) disableLegs = true;

            if (!disableLegs && BetterCombatConfig.legAnimationThreshold > 0) {
                var vel = player.getDeltaMovement();
                if (vel.horizontalDistanceSqr() > BetterCombatConfig.legAnimationThreshold * BetterCombatConfig.legAnimationThreshold) {
                    disableLegs = true;
                }
            }

            if (disableLegs) {
                func.apply("right_leg").setEnabled(false);
                func.apply("left_leg").setEnabled(false);
            }
        });
    }

    private AdjustmentModifier createAttackAdjustment() {
        return new AdjustmentModifier((partName, data) -> {
            var player = this.getAvatar();
            float pitch = player.getXRot();

            if (data.isFirstPersonPass()) {
                pitch = (float) Math.toRadians(pitch);
                if ("body".equals(partName)) {
                    float offsetY = 0, offsetZ = 0;
                    if (pitch < 0) {
                        float offset = (float) Math.abs(Math.sin(pitch));
                        offsetY = offset * 0.5f;
                        offsetZ = -offset;
                    }
                    return Optional.of(new AdjustmentModifier.PartModifier(
                            new Vec3f(pitch, 0, 0), new Vec3f(0, offsetY, offsetZ)));
                }
                return Optional.empty();
            } else {
                pitch = (float) Math.toRadians(pitch);
                if ("body".equals(partName))
                    return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(pitch * 0.75f, 0, 0), Vec3f.ZERO));
                else if (isArm(partName))
                    return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(pitch * 0.25f, 0, 0), Vec3f.ZERO));
                else if (isLeg(partName))
                    return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(-pitch * 0.75f, 0, 0), Vec3f.ZERO));
                return Optional.empty();
            }
        });
    }

    private boolean isArm(String n) { return "right_arm".equals(n) || "left_arm".equals(n); }
    private boolean isLeg(String n) { return "right_leg".equals(n) || "left_leg".equals(n); }

    /**
     * Play an attack animation with the given parameters.
     *
     * @param name      animation name (e.g., "one_handed_slash_horizontal_right")
     * @param mirror    whether to mirror for off-hand/left-handed
     * @param baseSpeed base playback speed
     * @param gears     transmission gear shifts for upswing/downswing
     * @param fpConfig  first-person visibility configuration
     */
    public void playAnimation(String name, boolean mirror, float baseSpeed,
                               java.util.List<TransmissionSpeedModifier.Gear> gears,
                               FirstPersonConfiguration fpConfig) {
        // Animation name may already include namespace (e.g., "megamod:one_handed_slash")
        // or be plain (e.g., "one_handed_slash"). Identifier.parse handles both.
        Identifier animId = name.contains(":") ? Identifier.parse(name) : Identifier.fromNamespaceAndPath("megamod", name);
        if (!com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources.hasAnimation(animId)) {
            com.ultra.megamod.MegaMod.LOGGER.warn("[BetterCombat] Animation NOT FOUND: {}", animId);
            return;
        }

        this.mirror.enabled = mirror;
        this.speed.set(baseSpeed, gears);
        this.activeFirstPersonConfig = fpConfig;

        // Match source BetterCombat: use triggerAnimation (one-shot, no fade-stacking).
        // Previously used replaceAnimationWithFade which stacked fade modifiers on each call,
        // causing the visible animation jitter.
        triggerAnimation(animId);
    }

    /**
     * Stop the attack animation. Matches source BetterCombat exactly — hard stop, no fade.
     * Previously tried to fade but that caused hammer body shake / post-swing jitter
     * because fade modifier fought with pose layer reassertion.
     */
    public void stopAnimation(float length) {
        if (isActive()) {
            stop();
        }
    }
}
