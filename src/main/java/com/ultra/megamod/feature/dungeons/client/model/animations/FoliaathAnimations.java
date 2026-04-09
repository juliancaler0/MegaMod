/*
 * Animations for the Foliaath (carnivorous plant).
 * Converted from MowziesMobs LLibrary animator system to vanilla AnimationDefinition.
 *
 * idle: stem sways side to side, head leaves flutter slightly
 * attack: mouth opens wide (stem lunges forward), then snaps shut
 */
package com.ultra.megamod.feature.dungeons.client.model.animations;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3fc;

public class FoliaathAnimations {
    // Idle: stem sways, leaves flutter, tongue wiggles
    public static final AnimationDefinition idle = AnimationDefinition.Builder.withLength((float)4.0f).looping()
        // stem1Base sways side to side (Z rotation = flap) and slight forward/back (X rotation = walk)
        .addAnimation("stem1Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.9f, (float)0.0f, (float)8.6f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.9f, (float)0.0f, (float)-8.6f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // stem2 waves with offset
        .addAnimation("stem2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // stem3 waves with offset
        .addAnimation("stem3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)4.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-4.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // stem4 waves with offset
        .addAnimation("stem4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // headBase counter-sway and slight Y rotation
        .addAnimation("headBase", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-8.6f, (float)8.6f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)8.6f, (float)-8.6f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-8.6f, (float)8.6f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // All 8 head leaves flutter (X rotation oscillation with offset from headBase)
        .addAnimation("leaf1Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf2Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf3Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf4Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf5Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf6Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf7Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("leaf8Head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Big ground leaves flutter gently
        .addAnimation("bigLeaf1Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-3.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)3.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("bigLeaf2Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("bigLeaf3Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-3.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)3.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("bigLeaf4Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)-2.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // Tongue wiggles
        .addAnimation("tongue1Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)5.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)-5.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("tongue2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.75f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)7.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.25f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)-7.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .addAnimation("tongue3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(1.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(2.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(3.5f, (Vector3fc)KeyframeAnimations.degreeVec((float)5.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(4.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .build();

    // Attack: mouth opens wide, stem lunges forward, then snaps shut
    // Based on MowziesMobs ATTACK_ANIMATION: 3 ticks wind-up, 1 static, 2 ticks lunge, 3 static, 5 ticks reset
    // Converted to seconds: ~0.15s wind-up, 0.05s hold, 0.1s lunge, 0.15s hold, 0.25s reset = 0.7s total
    public static final AnimationDefinition attack = AnimationDefinition.Builder.withLength((float)0.7f)
        // stem1Base: wind-up tilts back, then lunges forward
        .addAnimation("stem1Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)22.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)22.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-34.4f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)-34.4f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // stem2: wind-up bends, then straightens for lunge
        .addAnimation("stem2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)-17.2f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)-17.2f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-68.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)-68.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // stem3: wind-up and lunge
        .addAnimation("stem3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // stem4: wind-up and lunge
        .addAnimation("stem4", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // headBase: tilts back then forward with lunge
        .addAnimation("headBase", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)-34.4f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)-34.4f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)22.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)22.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // mouthTop1: opens wide during wind-up, snaps shut on lunge
        .addAnimation("mouthTop1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // mouthBottom1: opens wide (mirrors top), snaps shut on lunge
        .addAnimation("mouthBottom1", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.2f, (Vector3fc)KeyframeAnimations.degreeVec((float)45.8f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)-5.7f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // mouthTop2: slight counter-rotation during lunge
        .addAnimation("mouthTop2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)8.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)8.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // mouthBottom2: slight counter-rotation during lunge
        .addAnimation("mouthBottom2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)8.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.45f, (Vector3fc)KeyframeAnimations.degreeVec((float)8.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.LINEAR),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // tongue1Base: pulls back during wind-up
        .addAnimation("tongue1Base", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)-11.5f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // tongue2: curls during wind-up
        .addAnimation("tongue2", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)-28.6f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // tongue2 position: shifts slightly during wind-up
        .addAnimation("tongue2", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)-0.3f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.posVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        // tongue3: tip curls opposite during wind-up
        .addAnimation("tongue3", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe[]{
            new Keyframe(0.0f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.15f, (Vector3fc)KeyframeAnimations.degreeVec((float)22.9f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.3f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM),
            new Keyframe(0.7f, (Vector3fc)KeyframeAnimations.degreeVec((float)0.0f, (float)0.0f, (float)0.0f), AnimationChannel.Interpolations.CATMULLROM)}))
        .build();
}
