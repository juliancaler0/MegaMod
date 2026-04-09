package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class SculptorAnimations {
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength(4.0f).looping()
            // Slow breathing — chest rises and falls
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(3.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            // Head slight sway
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, -3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(-1.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(3.0f, (Vector3fc) KeyframeAnimations.degreeVec(2.0f, 3.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            // Arms hang with slight sway
            .addAnimation("rightUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leftUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(5.0f, 0.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(4.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();

    public static final AnimationDefinition walk = AnimationDefinition.Builder.withLength(1.0f).looping()
            // Legs alternate forward/back
            .addAnimation("rightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(20.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            // Arms swing opposite to legs
            .addAnimation("rightUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leftUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            // Body lean with steps
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 2.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, -2.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();

    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength(1.2f)
            // Wind up — both arms raise
            .addAnimation("rightUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-100.0f, 0.0f, -20.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, 0.0f, 10.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leftUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-100.0f, 0.0f, 20.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, 0.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.8f, (Vector3fc) KeyframeAnimations.degreeVec(60.0f, 0.0f, -10.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            // Body leans forward on slam
            .addAnimation("chest", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3f, (Vector3fc) KeyframeAnimations.degreeVec(-15.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(25.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.2f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();

    public static final AnimationDefinition death = AnimationDefinition.Builder.withLength(2.0f)
            // Crumble: body tips forward, limbs go limp
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5f, (Vector3fc) KeyframeAnimations.degreeVec(10.0f, 0.0f, 5.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(30.0f, 10.0f, 15.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(90.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.posVec(0.0f, -8.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(1.0f, (Vector3fc) KeyframeAnimations.degreeVec(-20.0f, 15.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(30.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("rightUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 0.0f, -30.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .addAnimation("leftUpperArm", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
                    new Keyframe(0.0f, (Vector3fc) KeyframeAnimations.degreeVec(0.0f, 0.0f, 0.0f), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(2.0f, (Vector3fc) KeyframeAnimations.degreeVec(40.0f, 0.0f, 30.0f), AnimationChannel.Interpolations.CATMULLROM)
            }))
            .build();
}
