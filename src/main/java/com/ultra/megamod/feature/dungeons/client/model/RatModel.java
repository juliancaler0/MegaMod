/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.animation.KeyframeAnimation
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.client.renderer.entity.state.LivingEntityRenderState
 *  net.minecraft.resources.Identifier
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.RatAnimations;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class RatModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"rat"), "main");
    public final ModelPart plague_rat_white;
    public final ModelPart body;
    public final ModelPart torso;
    public final ModelPart h_head;
    public final ModelPart h_jaw;
    public final ModelPart h_left_ear;
    public final ModelPart h_right_ear;
    public final ModelPart hurtbox;
    public final ModelPart left_front_leg;
    public final ModelPart left_front_elbow;
    public final ModelPart left_front_foot;
    public final ModelPart right_front_leg;
    public final ModelPart right_front_elbow;
    public final ModelPart right_front_foot;
    public final ModelPart tail;
    public final ModelPart tail2;
    public final ModelPart tail3;
    public final ModelPart left_back_leg;
    public final ModelPart left_back_heel;
    public final ModelPart left_back_foot;
    public final ModelPart right_back_leg;
    public final ModelPart right_back_heel;
    public final ModelPart right_back_foot;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;

    public RatModel(ModelPart root) {
        super(root);
        this.plague_rat_white = root.getChild("plague_rat_white");
        this.body = this.plague_rat_white.getChild("body");
        this.torso = this.body.getChild("torso");
        this.h_head = this.torso.getChild("h_head");
        this.h_jaw = this.h_head.getChild("h_jaw");
        this.h_left_ear = this.h_head.getChild("h_left_ear");
        this.h_right_ear = this.h_head.getChild("h_right_ear");
        this.hurtbox = this.h_head.getChild("hurtbox");
        this.left_front_leg = this.torso.getChild("left_front_leg");
        this.left_front_elbow = this.left_front_leg.getChild("left_front_elbow");
        this.left_front_foot = this.left_front_elbow.getChild("left_front_foot");
        this.right_front_leg = this.torso.getChild("right_front_leg");
        this.right_front_elbow = this.right_front_leg.getChild("right_front_elbow");
        this.right_front_foot = this.right_front_elbow.getChild("right_front_foot");
        this.tail = this.torso.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.left_back_leg = this.body.getChild("left_back_leg");
        this.left_back_heel = this.left_back_leg.getChild("left_back_heel");
        this.left_back_foot = this.left_back_heel.getChild("left_back_foot");
        this.right_back_leg = this.body.getChild("right_back_leg");
        this.right_back_heel = this.right_back_leg.getChild("right_back_heel");
        this.right_back_foot = this.right_back_heel.getChild("right_back_foot");
        this.idleAnimation = RatAnimations.idle.bake(root);
        this.walkAnimation = RatAnimations.walk.bake(root);
        this.attackAnimation = RatAnimations.attack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition plague_rat_white = partdefinition.addOrReplaceChild("plague_rat_white", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)24.0f, (float)0.0f));
        PartDefinition body = plague_rat_white.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-9.5f, (float)9.5f));
        PartDefinition torso = body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -5.5f, -8.0f, 12.0f, 11.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(0, 2).addBox(-5.0f, -4.5f, -14.0f, 10.0f, 11.0f, 11.0f, new CubeDeformation(0.0f)).texOffs(3, 22).addBox(-3.5f, -3.5f, -17.0f, 7.0f, 8.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition h_head = torso.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(14, 34).mirror().addBox(-2.0f, 0.5f, -13.0f, 4.0f, 3.0f, 5.0f, new CubeDeformation(0.01f)).mirror(false).texOffs(57, 31).addBox(2.0f, -1.7f, -8.1f, 1.0f, 2.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(58, 31).addBox(-2.6f, -1.7f, -8.1f, 1.0f, 2.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.0f, -3.0f, -8.0f, 8.0f, 7.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)-0.5f, (float)-17.0f));
        PartDefinition h_head_r1 = h_head.addOrReplaceChild("h_head_r1", CubeListBuilder.create().texOffs(45, 0).addBox(-2.0f, 0.0f, 0.0f, 0.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(45, 0).addBox(2.0f, 0.0f, 0.0f, 0.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-4.0f, (float)-3.0f, (float)0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition h_head_r2 = h_head.addOrReplaceChild("h_head_r2", CubeListBuilder.create().texOffs(41, -3).addBox(0.0f, -5.0f, 0.0f, 0.0f, 5.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-3.0f, (float)-3.0f, (float)-0.7854f, (float)0.0f, (float)0.0f));
        PartDefinition h_head_r3 = h_head.addOrReplaceChild("h_head_r3", CubeListBuilder.create().texOffs(28, 0).addBox(0.0f, -3.0f, 0.0f, 0.0f, 6.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-4.0f, (float)0.0f, (float)-3.0f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition h_head_r4 = h_head.addOrReplaceChild("h_head_r4", CubeListBuilder.create().texOffs(27, 0).addBox(0.0f, -3.0f, 0.0f, 0.0f, 6.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)4.0f, (float)0.0f, (float)-3.0f, (float)0.0f, (float)0.3927f, (float)0.0f));
        PartDefinition h_head_r5 = h_head.addOrReplaceChild("h_head_r5", CubeListBuilder.create().texOffs(24, 37).addBox(-3.0f, 0.0f, 0.0f, 6.0f, 2.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)4.0f, (float)-7.5f, (float)0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition h_head_r6 = h_head.addOrReplaceChild("h_head_r6", CubeListBuilder.create().texOffs(56, 54).addBox(0.35f, 0.35f, 0.0f, 1.0f, 1.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-1.9f, (float)3.5f, (float)-12.9f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        PartDefinition h_head_r7 = h_head.addOrReplaceChild("h_head_r7", CubeListBuilder.create().texOffs(56, 55).addBox(-1.35f, 0.35f, 0.0f, 1.0f, 1.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)1.9f, (float)3.5f, (float)-12.9f, (float)0.0f, (float)0.0f, (float)0.7854f));
        PartDefinition h_head_r8 = h_head.addOrReplaceChild("h_head_r8", CubeListBuilder.create().texOffs(36, 57).addBox(-1.0f, -1.25f, -0.75f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)1.0f, (float)-13.0f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition h_jaw = h_head.addOrReplaceChild("h_jaw", CubeListBuilder.create().texOffs(13, 24).addBox(-2.0f, -1.0f, -5.0f, 4.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)4.5f, (float)-7.5f));
        PartDefinition h_jaw_r1 = h_jaw.addOrReplaceChild("h_jaw_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.25f, -1.25f, 0.0f, 1.0f, 1.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-1.75f, (float)-1.0f, (float)-4.75f, (float)0.0f, (float)0.0f, (float)0.7854f));
        PartDefinition h_jaw_r2 = h_jaw.addOrReplaceChild("h_jaw_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.25f, -1.25f, 0.0f, 1.0f, 1.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)1.75f, (float)-1.0f, (float)-4.75f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        PartDefinition h_left_ear = h_head.addOrReplaceChild("h_left_ear", CubeListBuilder.create().texOffs(36, 22).mirror().addBox(-2.0f, -3.0f, -0.5f, 4.0f, 5.0f, 1.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)4.0f, (float)-3.0f, (float)-4.5f, (float)0.3927f, (float)0.0f, (float)0.3927f));
        PartDefinition h_right_ear = h_head.addOrReplaceChild("h_right_ear", CubeListBuilder.create().texOffs(63, 20).addBox(-2.0f, -3.0f, -0.5f, 4.0f, 5.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-4.0f, (float)-3.0f, (float)-4.5f, (float)0.3927f, (float)0.0f, (float)-0.3927f));
        PartDefinition hurtbox = h_head.addOrReplaceChild("hurtbox", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)4.0f, (float)-12.5f));
        PartDefinition left_front_leg = torso.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0f, -1.5f, -1.5f, 2.0f, 6.0f, 3.0f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)4.5f, (float)-0.25f, (float)-14.5f, (float)0.1745f, (float)0.0f, (float)-0.1309f));
        PartDefinition left_front_elbow = left_front_leg.addOrReplaceChild("left_front_elbow", CubeListBuilder.create().texOffs(7, 47).mirror().addBox(-1.0f, 0.0f, -2.0f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)0.0f, (float)4.5f, (float)1.0f, (float)-0.4363f, (float)0.0f, (float)0.0f));
        PartDefinition left_front_foot = left_front_elbow.addOrReplaceChild("left_front_foot", CubeListBuilder.create().texOffs(4, 55).addBox(-1.5f, 0.0f, -3.0f, 3.0f, 1.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)-1.0f, (float)0.2618f, (float)0.0f, (float)0.1309f));
        PartDefinition right_front_leg = torso.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(15, 27).mirror().addBox(-1.0f, -1.5f, -1.5f, 2.0f, 6.0f, 3.0f, new CubeDeformation(0.25f)).mirror(false), PartPose.offsetAndRotation((float)-4.5f, (float)-0.25f, (float)-14.5f, (float)0.1745f, (float)0.0f, (float)0.1309f));
        PartDefinition right_front_elbow = right_front_leg.addOrReplaceChild("right_front_elbow", CubeListBuilder.create().texOffs(7, 46).addBox(-1.0f, 0.0f, -2.0f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)4.5f, (float)1.0f, (float)-0.4363f, (float)0.0f, (float)0.0f));
        PartDefinition right_front_foot = right_front_elbow.addOrReplaceChild("right_front_foot", CubeListBuilder.create().texOffs(2, 59).mirror().addBox(-1.5f, 0.0f, -3.0f, 3.0f, 1.0f, 4.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)-1.0f, (float)0.2618f, (float)0.0f, (float)-0.1309f));
        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 53).mirror().addBox(-1.0f, -1.5f, 0.0f, 2.0f, 3.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)0.0f, (float)2.0f, (float)3.0f));
        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 55).addBox(-0.5f, -1.0f, 0.0f, 1.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.0f, (float)7.5f));
        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(0, 51).addBox(-1.0f, -1.0f, 0.5f, 1.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.5f, (float)0.0f, (float)6.5f));
        PartDefinition left_back_leg = body.addOrReplaceChild("left_back_leg", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-2.0f, -2.5f, -2.5f, 4.0f, 8.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)6.0f, (float)0.0f, (float)0.0f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition left_back_heel = left_back_leg.addOrReplaceChild("left_back_heel", CubeListBuilder.create().texOffs(7, 46).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)4.5f, (float)2.5f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition left_back_foot = left_back_heel.addOrReplaceChild("left_back_foot", CubeListBuilder.create().texOffs(3, 57).addBox(-1.5f, -1.0f, -3.0f, 3.0f, 2.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)0.0f, (float)0.7854f, (float)0.0f, (float)0.0f));
        PartDefinition right_back_leg = body.addOrReplaceChild("right_back_leg", CubeListBuilder.create().texOffs(0, 21).mirror().addBox(-2.0f, -2.5f, -2.5f, 4.0f, 8.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)-6.0f, (float)0.0f, (float)0.0f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition right_back_heel = right_back_leg.addOrReplaceChild("right_back_heel", CubeListBuilder.create().texOffs(7, 46).mirror().addBox(-1.0f, -1.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)0.0f, (float)4.5f, (float)2.5f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition right_back_foot = right_back_heel.addOrReplaceChild("right_back_foot", CubeListBuilder.create().texOffs(2, 57).mirror().addBox(-1.5f, -1.0f, -3.0f, 3.0f, 2.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)0.0f, (float)0.7854f, (float)0.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonMobRenderState) {
            DungeonEntityRenderers.DungeonMobRenderState mobState = (DungeonEntityRenderers.DungeonMobRenderState)state;
            this.idleAnimation.apply(mobState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(mobState.attackAnimationState, state.ageInTicks);
        }
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0f, 1.0f);
    }
}

