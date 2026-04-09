package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class GrottolAnimations {

    // Idle animation: subtle crystal sway, head look, claw fidget (~3.5s loop)
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength((float)3.5f).looping()
        // Head subtle look around
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)8.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)3.0f, (float)-10.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.8f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)-5.0f, (float)-3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Crystal1 (tallest) sway
        .addAnimation("crystal1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.0f, (float)0.0f, (float)3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-1.5f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)1.0f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Crystal2 sway (offset phase)
        .addAnimation("crystal2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.8f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)0.0f, (float)-2.5f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.8f, (Vector3fc)KeyframeAnimations.degreeVec((float)1.5f, (float)0.0f, (float)3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.8f, (Vector3fc)KeyframeAnimations.degreeVec((float)-1.0f, (float)0.0f, (float)-1.5f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Crystal3 sway
        .addAnimation("crystal3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)3.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)0.0f, (float)2.5f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Crystal5 sway
        .addAnimation("crystal5", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.9f, (Vector3fc)KeyframeAnimations.degreeVec((float)-1.5f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.0f, (float)0.0f, (float)-3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Body subtle bob
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.3f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.8f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Right claw fidget (pinch)
        .addAnimation("clawRightUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)12.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)12.0f, (float)-3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.9f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)12.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)12.0f, (float)-3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)12.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawRightLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.9f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-3.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Eye blink (left)
        .addAnimation("eyeLeft", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(2.05f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)0.1, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(2.15f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR)
        }))
        // Eye blink (right)
        .addAnimation("eyeRight", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(2.05f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)0.1, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(2.15f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.scaleVec((double)1.0, (double)1.0, (double)1.0), AnimationChannel.Interpolations.LINEAR)
        }))
        .build();

    // Walk animation: alternating spider-like leg swing (0.6s loop)
    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength((float)0.6f).looping()
        // Body side-to-side sway
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Body bounce
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // --- Left legs: swing forward at 0.0, back at 0.3 ---
        // Leg1 Left Joint (Y-axis swing for forward/back)
        .addAnimation("leg1LeftJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Leg1 Left Upper (Z-axis flap for lift)
        .addAnimation("leg1LeftUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg1LeftLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("foot1Left", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Leg2 Left (offset by half cycle)
        .addAnimation("leg2LeftJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg2LeftUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg2LeftLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("foot2Left", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Leg3 Left (same phase as leg1)
        .addAnimation("leg3LeftJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg3LeftUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg3LeftLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("foot3Left", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // --- Right legs: opposite phase from left ---
        // Leg1 Right (opposite of leg1 left)
        .addAnimation("leg1RightJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg1RightUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg1RightLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("foot1Right", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Leg2 Right (opposite of leg2 left, same phase as leg1 left)
        .addAnimation("leg2RightJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg2RightUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg2RightLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("foot2Right", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Leg3 Right (opposite of leg3 left)
        .addAnimation("leg3RightJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg3RightUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-8.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("leg3RightLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)12.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-12.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("foot3Right", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Claws swing forward during walk
        .addAnimation("clawLeftJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawRightJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)-10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .build();

    // Attack animation: claws snap forward aggressively (0.5s, non-looping)
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength((float)0.5f)
        // Body lunge forward
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.5f, (float)0.5f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Left claw: wind up then snap forward
        .addAnimation("clawLeftJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)25.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)-15.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)-15.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawLeftUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)25.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)25.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawLeftLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-30.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-30.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)-20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)-20.0f, (float)0.0f, (float)10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)-20.0f, (float)0.0f, (float)10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Right claw: wind up then snap forward
        .addAnimation("clawRightJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)-25.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)15.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)15.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawRightUpper", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)15.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-25.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-25.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawRightLower", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)30.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)30.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("clawRight", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)20.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)-20.0f, (float)0.0f, (float)-10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)-20.0f, (float)0.0f, (float)-10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Head recoil
        .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)-10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        // Crystals shake on impact
        .addAnimation("crystal1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)8.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-3.0f, (float)0.0f, (float)-5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4f, (Vector3fc)KeyframeAnimations.degreeVec((float)1.0f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .addAnimation("crystal2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)-4.0f, (float)0.0f, (float)-6.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)3.0f, (float)0.0f, (float)4.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.4f, (Vector3fc)KeyframeAnimations.degreeVec((float)-1.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
        }))
        .build();
}
