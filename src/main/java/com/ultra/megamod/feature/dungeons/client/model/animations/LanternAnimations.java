/*
 * Animations for the Lantern (floating jellyfish creature).
 * Converted from MowziesMobs animator-based system to vanilla AnimationDefinition.
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class LanternAnimations {
    // Idle: body bobs up/down sinusoidally, bottomBits sway gently, bubbles drift, leaves flutter
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength((float)4.0f).looping()
        // Body bobs up and down
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Body rotates very slightly
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)2.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-2.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Center bobs with body
        .addAnimation("center", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Bubbles bob with body
        .addAnimation("bubbles", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-2.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // BottomBit1 sways (X rotation oscillation)
        .addAnimation("bottomBit1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)-5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // BottomBit2 sways with offset
        .addAnimation("bottomBit2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // BottomBit3 sways with another offset
        .addAnimation("bottomBit3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)-5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-15.0f, (float)0.0f, (float)5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // BottomBit4 sways with yet another offset
        .addAnimation("bottomBit4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)10.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-10.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Leaf1 flutters
        .addAnimation("leaf1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Leaf2 flutters (roll)
        .addAnimation("leaf2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Leaf3 flutters
        .addAnimation("leaf3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Leaf4 flutters (roll)
        .addAnimation("leaf4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)5.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Stem2 slight sway
        .addAnimation("stem2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)3.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .build();

    // Attack (puff): body pulses outward then contracts, bottomBits extend downward
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength((float)0.85f)
        // Body rises then drops (puff motion)
        .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-3.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)4.5f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Center follows body
        .addAnimation("center", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-3.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)4.5f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Bubbles follow body
        .addAnimation("bubbles", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-3.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)4.5f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Stem pushes upward during puff then drops back
        .addAnimation("stem", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)3.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-4.5f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // BottomBits drop during puff then retract
        .addAnimation("bottomBits", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-3.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)4.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // BottomBit1 extends outward during puff
        .addAnimation("bottomBit1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-51.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("bottomBit2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-51.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("bottomBit3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-51.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("bottomBit4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-51.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Leaves spread during puff then close back
        .addAnimation("leaf1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)-11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)17.2f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)11.5f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-17.2f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)-11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)17.2f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.35f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)-11.5f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)17.2f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.85f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .build();
}
