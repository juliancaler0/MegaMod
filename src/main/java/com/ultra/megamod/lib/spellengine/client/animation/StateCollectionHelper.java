package com.ultra.megamod.lib.spellengine.client.animation;

import com.ultra.megamod.lib.playeranim.core.bones.AdvancedPlayerAnimBone;

import javax.annotation.Nullable;

/**
 * Utility for configuring animation body part axes (rotation, position).
 * Adapted for the new PlayerAnimationLib API using AdvancedPlayerAnimBone.
 */
public class StateCollectionHelper {
    public static void configure(@Nullable AdvancedPlayerAnimBone bodyPart, boolean isRotationEnabled, boolean isOffsetEnabled) {
        if (bodyPart == null) return;
        bodyPart.rotXEnabled = isRotationEnabled;
        bodyPart.rotYEnabled = isRotationEnabled;
        bodyPart.rotZEnabled = isRotationEnabled;
        bodyPart.positionXEnabled = isOffsetEnabled;
        bodyPart.positionYEnabled = isOffsetEnabled;
        bodyPart.positionZEnabled = isOffsetEnabled;
    }
}
