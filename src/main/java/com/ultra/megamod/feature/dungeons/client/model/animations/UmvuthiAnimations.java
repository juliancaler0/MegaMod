/*
 * UmvuthiAnimations - Idle, walk, attack, death animations for the Umvuthi mask lord summoner boss.
 * Fat avian creature with tribal mask, staff, and bird-like legs.
 * Idle has belly jiggle, head bob, feather sway, slight body rotation.
 * Walk has bird-like high-knee leg motion, body rocks.
 * Attack thrusts staff forward, body leans into it.
 * Death topples over.
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class UmvuthiAnimations {

    // Idle: belly jiggle, head bob, feather sway, slight body rotation
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(4.0f).looping()
            // Body - gentle rotation/sway
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(1.5f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-1.0f, -2.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach - belly jiggle (breathing + fat wobble)
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.5f, 0.3f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.6f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.3f, -0.2f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.4f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.4f, 0.2f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.2f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.2f, -0.1f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(-1.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(1.5f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Chest - breathing
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head - bobbing, looking around
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.4f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, -3.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.2f, (Vector3fc) KeyframeAnimations.degreeVec(-4.0f, -6.0f, -1.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head - vertical bob
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.3f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.3f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail feathers - gentle sway
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-2.0f, -4.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm (staff arm) - gentle idle hold
            .addAnimation("leftArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-2.0f, 0.0f, 1.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right arm - relaxed sway
            .addAnimation("rightArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8f, (Vector3fc) KeyframeAnimations.degreeVec(4.0f, 0.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.2f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, -1.5f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Walk: bird-like walk with high knees, body rocks, belly wobbles
    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.2f).looping()
            // Body - vertical bob and side rock
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach - belly wobble during walk
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-1.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(-1.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left thigh - high bird-knee stride
            .addAnimation("leftThigh", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(-30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right thigh - opposite phase
            .addAnimation("rightThigh", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(-30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left calf - knee bend during lift
            .addAnimation("leftCalf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.15f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right calf
            .addAnimation("rightCalf", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head stabilizes (bird-like head lock)
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm (staff) swings opposite to legs
            .addAnimation("leftArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right arm swings
            .addAnimation("rightArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail sways during walk
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Attack: staff thrust forward - body leans, left arm extends, head tracks target
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength(0.8333f)
            // Body leans forward into thrust
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(-8.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 1.0f, 1.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -2.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -1.0f, -1.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Chest tilts with thrust
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(12.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm (staff arm) - winds back then thrusts forward hard
            .addAnimation("leftArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(-60.0f, -15.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.35f, (Vector3fc) KeyframeAnimations.degreeVec(45.0f, 10.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, 8.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left lower arm extends during thrust
            .addAnimation("leftLowerArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(-30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right arm braces
            .addAnimation("rightArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 10.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, -5.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head follows the thrust
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(-12.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach wobbles from the thrust
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8333f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Death: topples over - body pitches forward, arms go limp, staff drops, legs crumble
    public static final AnimationDefinition death = AnimationDefinition.Builder.withLength(3.0f)
            // Body tips forward and crashes down
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 8.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(55.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(65.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 2.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 6.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 14.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 18.0f, -12.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Chest sags forward
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(-8.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(12.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head drops and lolls
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 10.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, -5.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, -5.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Stomach wobbles then sags
            .addAnimation("stomach", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(8.0f, 0.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(12.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm (staff arm) goes limp - staff drops
            .addAnimation("leftArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, -10.0f, -15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 15.0f, -25.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(55.0f, 20.0f, -35.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right arm drops limp
            .addAnimation("rightArmJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 10.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(30.0f, -15.0f, 25.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, -20.0f, 35.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Legs buckle
            .addAnimation("leftThigh", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-25.0f, 0.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("rightThigh", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Tail droops
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();
}
