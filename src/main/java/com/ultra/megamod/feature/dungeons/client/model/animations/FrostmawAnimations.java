/*
 * FrostmawAnimations - Idle, walk, attack, death animations for the Frostmaw ice titan boss.
 * Uses KeyframeAnimations with CATMULLROM interpolation for smooth organic movement.
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class FrostmawAnimations {

    // Idle: gentle breathing, arm sway, jaw slight open/close, body rock
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(4.0f).looping()
            // Waist - gentle breathing sway
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Chest - breathing scale effect via position
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -0.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.5f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head joint - slight look around
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, -3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Jaw - slight open/close (breathing)
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left arm - gentle sway
            .addAnimation("armLeft1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.8f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right arm - gentle sway (offset timing)
            .addAnimation("armRight1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-3.0f, 0.0f, 3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.2f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left hand - slight curl
            .addAnimation("leftHand", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right hand - slight curl
            .addAnimation("rightHand", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Walk: heavy quadruped-style stomping, body rocks, arms swing
    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.5f).looping()
            // Root - vertical bob
            .addAnimation("root", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -2.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.125f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -2.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Waist - lean forward while walking
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(8.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Left leg - stride forward
            .addAnimation("legLeft1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.125f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Right leg - stride (opposite phase)
            .addAnimation("legRight1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.125f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Lower legs - knee bend during stride
            .addAnimation("legLeft2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("legRight2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.125f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Arms swing during walk
            .addAnimation("armLeft1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("armRight1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head stabilizes during walk
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, (Vector3fc) KeyframeAnimations.degreeVec(-8.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(-5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Attack: massive arm slam - arms raise up, then slam down together, jaw opens wide
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength(1.0f)
            // Waist - lean back then forward for slam
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 3.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -4.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -2.0f, -1.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Chest bows forward during slam
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Arms raise up, then slam down
            .addAnimation("armLeft1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-70.0f, -15.0f, -20.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(45.0f, 10.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.7f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, 10.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("armRight1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-70.0f, 15.0f, 20.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(45.0f, -10.0f, -15.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.7f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, -10.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Forearms extend during slam
            .addAnimation("armLeft2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("armRight2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Jaw opens wide during roar/slam
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.2f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head recoils with slam
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();

    // Death: collapses forward, arms go limp, jaw drops open
    public static final AnimationDefinition death = AnimationDefinition.Builder.withLength(3.0f)
            // Waist tips forward and down
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, -3.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(35.0f, 5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(55.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("waist", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 2.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 5.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 15.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 18.0f, -12.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Chest sags
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(-10.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Head drops
            .addAnimation("headJoint", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, -5.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Jaw drops open and stays
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(45.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(50.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Arms go limp and spread
            .addAnimation("armLeft1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-30.0f, -10.0f, -20.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 15.0f, -30.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, 20.0f, -40.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("armRight1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-30.0f, 10.0f, 20.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.5f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, -15.0f, 30.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, -20.0f, 40.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            // Legs buckle
            .addAnimation("legLeft1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, -5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-25.0f, 0.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .addAnimation("legRight1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe[]{
                            new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(-25.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM)
                    }))
            .build();
}
