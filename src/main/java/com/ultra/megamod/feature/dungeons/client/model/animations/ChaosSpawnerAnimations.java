/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.client.animation.AnimationChannel
 *  net.minecraft.client.animation.AnimationChannel$Interpolations
 *  net.minecraft.client.animation.AnimationChannel$Targets
 *  net.minecraft.client.animation.AnimationDefinition
 *  net.minecraft.client.animation.AnimationDefinition$Builder
 *  net.minecraft.client.animation.Keyframe
 *  net.minecraft.client.animation.KeyframeAnimations
 *  org.joml.Vector3fc
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class ChaosSpawnerAnimations {
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength((float)3.0f).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.75f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.5f, (float)-2.5f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.25f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.5f, (float)2.5f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("lower_jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)7.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("chaos_hexahedron", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)180.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)360.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR)
            }))
            .addAnimation("chain", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)5.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-5.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();

    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength((float)1.0f)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-3.0f, (float)-4.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-3.0f, (float)-4.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.6f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("lower_jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1f, (Vector3fc)KeyframeAnimations.degreeVec((float)35.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("shockwave", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.scaleVec((float)0.0f, (float)1.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25f, (Vector3fc)KeyframeAnimations.scaleVec((float)0.0f, (float)1.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.scaleVec((float)1.0f, (float)1.0f, (float)1.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.scaleVec((float)2.0f, (float)1.0f, (float)2.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.scaleVec((float)0.0f, (float)1.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR)
            }))
            .build();

    public static final AnimationDefinition death = AnimationDefinition.Builder.withLength((float)2.5f)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)10.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)-15.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)5.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)5.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)10.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)12.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("lower_jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("chaos_hexahedron", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.scaleVec((float)1.0f, (float)1.0f, (float)1.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.scaleVec((float)2.0f, (float)2.0f, (float)2.0f), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.scaleVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR)
            }))
            .addAnimation("chain", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)5.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)8.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();
}
