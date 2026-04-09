/*
 * NagaAnimations - Idle, walk, attack, death animations for the Naga serpent/dragon boss.
 * Serpentine body motion with wing movements and tail undulation.
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class NagaAnimations {

    // Idle: body sway, tail undulation (segments wave), wings fold/unfold slightly, head scans
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(4.0f).looping()
            // Body - gentle side-to-side sway
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 3.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -3.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Neck - slight bob
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, -3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head - scan left/right
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Jaw - slight idle breathing
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(1.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail segments - wave pattern (each offset in phase)
            .addAnimation("tail1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-2.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -4.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 4.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -4.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail5", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -6.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 6.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -6.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Wings - gentle fold/unfold
            .addAnimation("shoulder_L", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("shoulder_R", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Walk: serpentine body wave, tail chain undulates more strongly, wings partially spread
    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.5f).looping()
            // Body - strong serpentine wave
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 8.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, -8.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.125f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 8.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.125f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Neck counter-sway
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head stabilizes
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, -3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail segments wave with increasing amplitude
            .addAnimation("tail1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, -8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, -8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 12.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -12.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 12.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -15.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 15.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -15.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail5", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 18.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -18.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 18.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Wings spread slightly during movement
            .addAnimation("shoulder_L", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, -10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, -15.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, -10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("shoulder_R", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 15.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Attack: lunge forward, jaw opens wide, wings spread aggressively
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength(0.75f)
            // Body lunges forward
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 1.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.0f, -4.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Neck extends forward for strike
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head dips down for bite
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Jaw snaps open then shut
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.1f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Wings spread aggressively
            .addAnimation("shoulder_L", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, -30.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, -40.0f, -15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, -10.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("shoulder_R", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 30.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 40.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 10.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail coils during strike
            .addAnimation("tail1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Death: collapse, wings fold in, tail goes limp, head drops
    public static final AnimationDefinition death = AnimationDefinition.Builder.withLength(2.5f)
            // Body drops
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 5.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 0.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 7.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 9.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Neck goes limp
            .addAnimation("neck", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(30.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head drops
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Jaw drops open
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(45.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Wings fold in and droop
            .addAnimation("shoulder_L", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, -15.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 30.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 35.0f, 25.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("shoulder_R", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 15.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, -30.0f, -15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, -35.0f, -25.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail segments go limp progressively
            .addAnimation("tail1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, -3.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, -10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 5.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(8.0f, 12.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, -8.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("tail5", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, -15.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 10.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();
}
