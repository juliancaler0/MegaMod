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

public class SpawnerCarrierAnimations {
    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength((float)2.0f).looping()
            .addAnimation("leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg3", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-25.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg4", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-1.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("eye", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.125f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0833f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.2f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();

    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength((float)1.5f)
            .addAnimation("leg", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.degreeVec((float)-30.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)13.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)-1.5f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)7.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg2", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)1.5f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)7.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg3", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)1.5f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.degreeVec((float)-30.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)13.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leg4", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)-1.5f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.0f, (float)-4.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("eye", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.5f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)2.5f, (float)-4.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("eye", new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.scaleVec((float)1.0f, (float)1.0f, (float)1.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667f, (Vector3fc)KeyframeAnimations.scaleVec((float)0.6f, (float)0.6f, (float)0.6f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.scaleVec((float)1.3f, (float)1.3f, (float)1.3f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.scaleVec((float)1.0f, (float)1.0f, (float)1.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.scaleVec((float)1.0f, (float)1.0f, (float)1.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();
}
