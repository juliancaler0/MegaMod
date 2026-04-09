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
import com.ultra.megamod.feature.dungeons.client.model.animations.SkeletonNinjaAnimations;
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

public class SkeletonNinjaModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"skeleton_ninja"), "main");
    public final ModelPart torso;
    public final ModelPart chest;
    public final ModelPart h_head;
    public final ModelPart h_jaw;
    public final ModelPart left_arm;
    public final ModelPart right_arm;
    public final ModelPart sword;
    public final ModelPart hurtbox;
    public final ModelPart left_leg;
    public final ModelPart leftt;
    public final ModelPart right_leg;
    public final ModelPart rightt;
    public final ModelPart mid;
    public final ModelPart left_belt_part;
    public final ModelPart right_belt_part;
    public final ModelPart back_tunic;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public SkeletonNinjaModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.chest = this.torso.getChild("chest");
        this.h_head = this.chest.getChild("h_head");
        this.h_jaw = this.h_head.getChild("h_jaw");
        this.left_arm = this.chest.getChild("left_arm");
        this.right_arm = this.chest.getChild("right_arm");
        this.sword = this.right_arm.getChild("sword");
        this.hurtbox = this.sword.getChild("hurtbox");
        this.left_leg = this.torso.getChild("left_leg");
        this.leftt = this.left_leg.getChild("leftt");
        this.right_leg = this.torso.getChild("right_leg");
        this.rightt = this.right_leg.getChild("rightt");
        this.mid = this.torso.getChild("mid");
        this.left_belt_part = this.mid.getChild("left_belt_part");
        this.right_belt_part = this.mid.getChild("right_belt_part");
        this.back_tunic = this.torso.getChild("back_tunic");
        this.idleAnimation = SkeletonNinjaAnimations.idle.bake(root);
        this.walkAnimation = SkeletonNinjaAnimations.walk.bake(root);
        this.attackAnimation = SkeletonNinjaAnimations.attack.bake(root);
        this.deathAnimation = SkeletonNinjaAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(35, 29).addBox(-3.75f, -1.25f, -2.5f, 7.5f, 2.5f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)2.0E-4f, (float)5.8308f, (float)-0.4328f, (float)0.0436f, (float)0.0f, (float)0.0f));
        PartDefinition chest = torso.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(12, 0).addBox(-1.2525f, -13.7036f, 2.6625f, 2.505f, 2.505f, 0.005f, new CubeDeformation(0.01f)).texOffs(9, 0).addBox(-1.2525f, -13.7036f, 1.4125f, 0.005f, 2.505f, 1.255f, new CubeDeformation(0.01f)).texOffs(9, 0).addBox(1.2475f, -13.7036f, 1.4125f, 0.005f, 2.505f, 1.255f, new CubeDeformation(0.01f)).texOffs(0, 23).addBox(-4.9975f, -11.185f, -1.9563f, 9.995f, -0.005f, 4.62f, new CubeDeformation(-0.01f)).texOffs(0, 0).addBox(-4.9975f, -11.21f, 2.6687f, 9.995f, 1.87f, -0.005f, new CubeDeformation(-0.01f)).texOffs(30, 0).addBox(-4.9725f, -11.21f, -1.9563f, -0.005f, 1.87f, 4.62f, new CubeDeformation(-0.01f)).texOffs(30, 0).addBox(4.9775f, -11.21f, -1.9563f, -0.005f, 1.87f, 4.62f, new CubeDeformation(-0.01f)).texOffs(25, 53).addBox(-2.5025f, -3.6111f, -0.212f, 5.005f, 3.755f, 0.005f, new CubeDeformation(0.01f)).texOffs(18, 0).addBox(-2.5025f, -3.6111f, -1.462f, 0.005f, 3.755f, 1.255f, new CubeDeformation(0.01f)).texOffs(18, 0).addBox(2.4975f, -3.6111f, -1.462f, 0.005f, 3.755f, 1.255f, new CubeDeformation(0.01f)), PartPose.offset((float)0.0f, (float)-1.3914f, (float)2.7095f));
        PartDefinition chest_r1 = chest.addOrReplaceChild("chest_r1", CubeListBuilder.create().texOffs(10, 45).addBox(-8.8125f, -6.3125f, -1.3125f, 0.125f, 7.625f, 5.125f, new CubeDeformation(0.25f)).texOffs(10, 45).addBox(1.1875f, -6.3125f, -1.3125f, 0.125f, 7.625f, 5.125f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)3.75f, (float)-4.9485f, (float)-3.1957f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition chest_r2 = chest.addOrReplaceChild("chest_r2", CubeListBuilder.create().texOffs(49, 0).addBox(-11.2525f, -5.0025f, -2.5025f, 0.005f, 7.505f, 5.005f, new CubeDeformation(0.01f)).texOffs(49, 0).addBox(-1.2525f, -5.0025f, -2.5025f, 0.005f, 7.505f, 5.005f, new CubeDeformation(0.01f)).texOffs(14, 35).addBox(-11.6525f, -5.0025f, -2.5025f, 10.005f, 7.505f, 0.005f, new CubeDeformation(0.01f)), PartPose.offsetAndRotation((float)6.25f, (float)-5.625f, (float)-1.5625f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition chest_r3 = chest.addOrReplaceChild("chest_r3", CubeListBuilder.create().texOffs(42, 0).addBox(-5.0025f, -5.0025f, 4.9975f, 10.005f, 7.505f, 0.005f, new CubeDeformation(0.01f)), PartPose.offsetAndRotation((float)0.3f, (float)-6.5817f, (float)-3.8722f, (float)-0.3927f, (float)0.0f, (float)0.0f));
        PartDefinition h_head = chest.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(7, 15).addBox(-4.3625f, -7.1074f, -9.688f, 9.0f, 8.0f, 1.5f, new CubeDeformation(-0.25f)).texOffs(20, 8).addBox(-4.9375f, -7.1074f, -8.438f, 1.0f, 9.0f, 8.0f, new CubeDeformation(-0.25f)).texOffs(19, 6).addBox(3.9375f, -7.4824f, -9.688f, 1.0f, 9.0f, 10.0f, new CubeDeformation(-0.25f)).texOffs(7, 7).addBox(-4.9375f, -8.1062f, -9.688f, 10.0f, 1.5f, 10.0f, new CubeDeformation(-0.25f)).texOffs(9, 7).addBox(-3.9375f, 0.3938f, -9.688f, 8.0f, 1.0f, 10.0f, new CubeDeformation(-0.25f)).texOffs(18, 7).addBox(-4.6375f, -7.4824f, -0.938f, 9.0f, 8.0f, 1.5f, new CubeDeformation(-0.25f)), PartPose.offsetAndRotation((float)0.0f, (float)-13.75f, (float)2.5f, (float)0.0873f, (float)0.0f, (float)0.0f));
        PartDefinition h_jaw = h_head.addOrReplaceChild("h_jaw", CubeListBuilder.create(), PartPose.offsetAndRotation((float)0.0f, (float)-0.9824f, (float)-3.438f, (float)0.1745f, (float)0.0f, (float)0.0f));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(6, 59).addBox(0.0f, -1.25f, -1.25f, 2.5f, 15.0f, 2.5f, new CubeDeformation(0.0f)).texOffs(41, 45).addBox(-0.0625f, -1.4375f, -1.3125f, 2.625f, 15.125f, 2.625f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)6.25f, (float)-10.625f, (float)-0.9375f, (float)0.0f, (float)0.0f, (float)-0.1309f));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(6, 59).addBox(-2.5f, -1.25f, -1.25f, 2.5f, 15.0f, 2.5f, new CubeDeformation(0.0f)).texOffs(39, 46).mirror().addBox(-2.5625f, -1.4375f, -1.3125f, 2.625f, 15.125f, 2.625f, new CubeDeformation(0.25f)).mirror(false), PartPose.offsetAndRotation((float)-6.25f, (float)-10.625f, (float)-0.9375f, (float)0.0f, (float)0.0f, (float)0.1309f));
        PartDefinition sword = right_arm.addOrReplaceChild("sword", CubeListBuilder.create().texOffs(2, 64).addBox(-1.25f, -1.25f, -2.5f, 1.25f, 2.5f, 8.75f, new CubeDeformation(0.0f)).texOffs(0, 67).addBox(-0.6275f, -1.2525f, -33.7525f, 0.005f, 2.505f, 30.005f, new CubeDeformation(0.01f)).texOffs(0, 64).addBox(-1.875f, -1.25f, 6.25f, 2.5f, 2.5f, 2.5f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-0.625f, (float)11.875f, (float)0.0f, (float)0.3491f, (float)0.0f, (float)0.0f));
        PartDefinition sword_r1 = sword.addOrReplaceChild("sword_r1", CubeListBuilder.create().texOffs(20, 95).addBox(-0.0027f, -1.2527f, -0.6278f, 0.0055f, 2.5055f, 2.5055f, new CubeDeformation(0.011f)), PartPose.offsetAndRotation((float)-0.625f, (float)-0.9649f, (float)-34.2044f, (float)-0.7854f, (float)0.0f, (float)0.0f));
        PartDefinition sword_r2 = sword.addOrReplaceChild("sword_r2", CubeListBuilder.create().texOffs(0, 64).addBox(-1.875f, -1.875f, -0.625f, 3.75f, 3.75f, 1.25f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-0.625f, (float)0.0f, (float)-3.125f, (float)0.0f, (float)0.0f, (float)-0.7854f));
        PartDefinition hurtbox = sword.addOrReplaceChild("hurtbox", CubeListBuilder.create(), PartPose.offset((float)-0.625f, (float)0.0f, (float)-29.125f));
        PartDefinition left_leg = torso.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(38, 49).addBox(-0.6563f, 1.9997f, -1.2297f, 2.625f, 15.125f, 2.625f, new CubeDeformation(0.25f)).texOffs(6, 59).addBox(-0.5938f, 2.0622f, -1.1672f, 2.5f, 15.0f, 2.5f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)3.0938f, (float)0.9214f, (float)0.4393f, (float)0.0f, (float)-0.0873f, (float)-0.1047f));
        PartDefinition leftt = left_leg.addOrReplaceChild("leftt", CubeListBuilder.create().texOffs(73, 22).addBox(-1.25f, 0.0f, -4.375f, 2.5f, 13.75f, 7.5f, new CubeDeformation(0.0f)).texOffs(94, 21).addBox(-1.3125f, -0.1875f, -4.4375f, 2.625f, 2.0f, 7.625f, new CubeDeformation(0.25f)).texOffs(95, 21).addBox(-1.3125f, 11.8125f, -4.4375f, 2.625f, 2.0f, 7.625f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)1.2813f, (float)-3.2503f, (float)1.0203f, (float)0.0f, (float)0.0f, (float)-0.0873f));
        PartDefinition right_leg = torso.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(36, 48).mirror().addBox(-1.9688f, 1.9997f, -1.2297f, 2.625f, 15.125f, 2.625f, new CubeDeformation(0.25f)).mirror(false).texOffs(6, 59).addBox(-1.9063f, 2.0622f, -1.1672f, 2.5f, 15.0f, 2.5f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-3.0938f, (float)0.9214f, (float)0.4393f, (float)0.0f, (float)0.0873f, (float)0.1047f));
        PartDefinition rightt = right_leg.addOrReplaceChild("rightt", CubeListBuilder.create().texOffs(73, 22).addBox(-1.25f, 0.0f, -4.375f, 2.5f, 13.75f, 7.5f, new CubeDeformation(0.0f)).texOffs(95, 21).mirror().addBox(-1.3125f, 11.8125f, -4.4375f, 2.625f, 2.0f, 7.625f, new CubeDeformation(0.25f)).mirror(false).texOffs(95, 21).mirror().addBox(-1.3125f, -0.1875f, -4.4375f, 2.625f, 2.0f, 7.625f, new CubeDeformation(0.25f)).mirror(false), PartPose.offsetAndRotation((float)-1.2813f, (float)-3.2503f, (float)1.0203f, (float)0.0f, (float)0.0f, (float)0.0873f));
        PartDefinition mid = torso.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(74, 35).addBox(-3.75f, -0.2865f, -0.3409f, 7.5f, 8.75f, 1.25f, new CubeDeformation(0.0f)).texOffs(67, 8).addBox(-3.6125f, -0.349f, -0.4034f, 7.625f, 8.875f, 1.375f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)-0.001f, (float)0.0962f, (float)-3.0052f, (float)-0.0873f, (float)0.0f, (float)0.0f));
        PartDefinition left_belt_part = mid.addOrReplaceChild("left_belt_part", CubeListBuilder.create(), PartPose.offsetAndRotation((float)3.2508f, (float)0.073f, (float)-1.312f, (float)0.0436f, (float)0.0f, (float)-0.0873f));
        PartDefinition right_belt_part = mid.addOrReplaceChild("right_belt_part", CubeListBuilder.create(), PartPose.offsetAndRotation((float)-3.2492f, (float)0.073f, (float)-1.312f, (float)0.0436f, (float)0.0f, (float)0.0873f));
        PartDefinition back_tunic = torso.addOrReplaceChild("back_tunic", CubeListBuilder.create().texOffs(73, 30).addBox(-3.75f, -0.625f, -0.625f, 7.5f, 8.75f, 1.25f, new CubeDeformation(0.0f)).texOffs(61, 17).addBox(-3.8125f, 0.3125f, -0.6875f, 7.625f, 8.875f, 1.375f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)-0.001f, (float)-0.0549f, (float)4.2765f, (float)0.1745f, (float)0.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)128, (int)128);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonBossRenderState) {
            DungeonEntityRenderers.DungeonBossRenderState bossState = (DungeonEntityRenderers.DungeonBossRenderState)state;
            this.idleAnimation.apply(bossState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(bossState.attackAnimationState, state.ageInTicks);
            this.deathAnimation.apply(bossState.deathAnimationState, state.ageInTicks);
        }
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0f, 1.0f);
    }
}

