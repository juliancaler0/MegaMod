package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.bones.AdvancedPlayerAnimBone;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Utility for enabling/disabling specific animation axes per bone.
 * Used to configure which axes are animated (e.g., disable legs when mounted).
 * Rewritten for the new PlayerAnimationLib API using AdvancedPlayerAnimBone.
 *
 * Typically used inside a controller's setPostAnimationSetupConsumer callback:
 *   controller.setPostAnimationSetupConsumer(func -> {
 *       StateCollectionHelper.disableLegs(func);
 *   });
 */
public class StateCollectionHelper {

    public static void configurePitch(@Nullable AdvancedPlayerAnimBone bone, boolean enabled) {
        if (bone != null) bone.rotXEnabled = enabled;
    }

    public static void configureYaw(@Nullable AdvancedPlayerAnimBone bone, boolean enabled) {
        if (bone != null) bone.rotYEnabled = enabled;
    }

    public static void configureRoll(@Nullable AdvancedPlayerAnimBone bone, boolean enabled) {
        if (bone != null) bone.rotZEnabled = enabled;
    }

    public static void configurePosition(@Nullable AdvancedPlayerAnimBone bone, boolean enabled) {
        if (bone != null) {
            bone.positionXEnabled = enabled;
            bone.positionYEnabled = enabled;
            bone.positionZEnabled = enabled;
        }
    }

    public static void configureRotation(@Nullable AdvancedPlayerAnimBone bone, boolean enabled) {
        configurePitch(bone, enabled);
        configureYaw(bone, enabled);
        configureRoll(bone, enabled);
    }

    public static void configureAll(@Nullable AdvancedPlayerAnimBone bone, boolean enabled) {
        configurePosition(bone, enabled);
        configureRotation(bone, enabled);
    }

    /**
     * Disables leg animation channels -- used when the player is mounted.
     * Pass the bone lookup function from setPostAnimationSetupConsumer.
     */
    public static void disableLegs(Function<String, AdvancedPlayerAnimBone> boneLookup) {
        var leftLeg = boneLookup.apply("left_leg");
        var rightLeg = boneLookup.apply("right_leg");
        configureAll(leftLeg, false);
        configureAll(rightLeg, false);
    }

    /**
     * Configures head animation -- typically disable pitch (let vanilla handle it)
     * but keep yaw for body rotation during casting.
     */
    public static void configureHead(Function<String, AdvancedPlayerAnimBone> boneLookup, boolean pitchEnabled) {
        var head = boneLookup.apply("head");
        if (head != null) {
            configurePitch(head, pitchEnabled);
        }
    }
}
