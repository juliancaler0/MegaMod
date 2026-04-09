/*
 * WroughtnautAnimations - Idle, walk, attack, death animations for the Wroughtnaut iron warden boss.
 * Heavy, stomping movements with axe slam attack.
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class WroughtnautAnimations {

    // Idle: slight body sway, axe arm idle, heavy breathing presence
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(3.5f).looping()
            // Waist - very subtle sway (heavy creature)
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.75f, (Vector3fc) KeyframeAnimations.degreeVec(1.5f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach - breathing
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.3f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head - slow scan left/right
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-2.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right arm (axe arm) - slight idle sway
            .addAnimation("shoulderRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.75f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm - slight sway
            .addAnimation("shoulderLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Axe handle - subtle rock
            .addAnimation("axeHandle", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.75f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Walk: heavy stomping, body rocks side to side, arms swing ponderously
    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.8f).looping()
            // Root - vertical stomp bob
            .addAnimation("rootBox", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.45f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.15f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.35f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Waist - lean forward and rock
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.45f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.35f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right thigh - stride
            .addAnimation("thighRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.45f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.35f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left thigh - stride (opposite)
            .addAnimation("thighLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.45f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.35f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Calves bend during stride
            .addAnimation("calfRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.45f, (Vector3fc) KeyframeAnimations.degreeVec(-8.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("calfLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.35f, (Vector3fc) KeyframeAnimations.degreeVec(-8.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Shoulders swing
            .addAnimation("shoulderRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("shoulderLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head stabilizes
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Attack: axe arm winds up behind, then slams down forward - devastating overhead chop
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength(0.8333f)
            // Waist leans back for windup, then forward for slam
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.degreeVec(-8.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.degreeVec(12.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 1.0f, 1.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -2.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.0f, -1.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach tilts with the swing
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right shoulder (axe arm) - winds up high behind, slams forward and down
            .addAnimation("shoulderRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.degreeVec(-55.0f, -20.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, 15.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5833f, (Vector3fc) KeyframeAnimations.degreeVec(55.0f, 10.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Upper arm extends during slam
            .addAnimation("upperArmRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm braces
            .addAnimation("shoulderLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head follows the swing
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.25f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Death: collapses heavily, axe drops, body crumbles
    public static final AnimationDefinition death = AnimationDefinition.Builder.withLength(2.5f)
            // Waist collapses - stumble then fall
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 8.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.7f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 10.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 15.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, 10.0f, 20.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.7f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 3.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 8.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 15.0f, -8.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach crumbles
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head drops forward
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Axe arm drops limp
            .addAnimation("shoulderRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, 10.0f, 30.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(70.0f, 15.0f, 40.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm drops
            .addAnimation("shoulderLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, -15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, -10.0f, -30.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, -15.0f, -40.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Legs buckle
            .addAnimation("thighRight", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("thighLeft", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();
}
