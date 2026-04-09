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
import com.ultra.megamod.feature.dungeons.client.model.animations.WraithAnimations;
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

public class WraithModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"wraith"), "main");
    public final ModelPart body;
    public final ModelPart h_head;
    public final ModelPart upper_body;
    public final ModelPart upper_body_cloth;
    public final ModelPart left_arm;
    public final ModelPart left_arm_bone;
    public final ModelPart right_arm;
    public final ModelPart right_arm_bone;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public WraithModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.h_head = this.body.getChild("h_head");
        this.upper_body = this.body.getChild("upper_body");
        this.upper_body_cloth = this.upper_body.getChild("upper_body_cloth");
        this.left_arm = this.upper_body.getChild("left_arm");
        this.left_arm_bone = this.left_arm.getChild("left_arm_bone");
        this.right_arm = this.upper_body.getChild("right_arm");
        this.right_arm_bone = this.right_arm.getChild("right_arm_bone");
        this.idleAnimation = WraithAnimations.idle.bake(root);
        this.walkAnimation = WraithAnimations.walk.bake(root);
        this.attackAnimation = WraithAnimations.attack.bake(root);
        this.deathAnimation = WraithAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)10.0f, (float)0.0f));
        PartDefinition h_head = body.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(13, 22).mirror().addBox(-3.9998f, -7.8271f, -4.3423f, 8.0f, 6.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(23, 42).mirror().addBox(-2.9998f, -1.8271f, -4.3423f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(23, 42).addBox(1.0002f, -1.8271f, -4.3423f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(14, 34).addBox(-3.9998f, -1.8271f, -2.3423f, 8.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(39, 35).addBox(4.5002f, -8.3271f, -4.8423f, 0.05f, 10.0f, 9.0f, new CubeDeformation(0.0f)).texOffs(30, 21).addBox(-4.5008f, -8.3261f, -4.8423f, 9.0f, 0.05f, 9.0f, new CubeDeformation(0.0f)).texOffs(39, 31).mirror().addBox(-4.5008f, -8.3261f, -4.8423f, 9.0f, 3.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false).texOffs(33, 9).addBox(-4.5008f, -8.3261f, 4.1577f, 9.0f, 10.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(60, 37).addBox(1.0002f, -4.8271f, -4.4423f, 2.0f, 2.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(20, 21).addBox(1.5002f, -4.8271f, -4.5173f, 1.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(39, 33).addBox(-4.4998f, -8.3271f, -4.8423f, 0.05f, 10.0f, 9.0f, new CubeDeformation(0.0f)).texOffs(58, 38).mirror().addBox(-2.9998f, -4.8271f, -4.4423f, 2.0f, 2.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false).texOffs(19, 21).mirror().addBox(-2.4998f, -4.8271f, -4.5173f, 1.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)-2.0E-4f, (float)-2.1729f, (float)1.3423f));
        PartDefinition upper_body = body.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(25, 0).addBox(-1.0f, -0.2188f, -0.9375f, 2.0f, 7.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(32, 35).addBox(0.75f, 0.2813f, -3.9375f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(26, 47).addBox(3.75f, 0.2813f, -3.9375f, 0.05f, 1.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(24, 29).addBox(0.75f, 0.2813f, 0.0625f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(32, 36).mirror().addBox(-3.75f, 0.2813f, -3.9375f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false).texOffs(24, 29).mirror().addBox(-3.75f, 0.2813f, 0.0625f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false).texOffs(30, 54).addBox(-3.75f, 0.2813f, -3.9375f, 0.05f, 1.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(24, 29).addBox(0.75f, 2.2813f, 0.0625f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(24, 51).addBox(3.75f, 2.2813f, -3.9375f, 0.05f, 1.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(32, 36).addBox(0.75f, 2.2813f, -3.9375f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(24, 29).mirror().addBox(-3.75f, 2.2813f, 0.0625f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false).texOffs(27, 51).addBox(-3.75f, 2.2813f, -3.9375f, 0.05f, 1.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(31, 37).mirror().addBox(-3.75f, 2.2813f, -3.9375f, 3.0f, 1.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)0.0f, (float)-1.7859f, (float)1.8554f));
        PartDefinition upper_body_cloth = upper_body.addOrReplaceChild("upper_body_cloth", CubeListBuilder.create().texOffs(6, 19).addBox(-4.0f, 0.0f, -6.0f, 0.05f, 8.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(40, 49).addBox(-4.0f, 0.0f, 0.0f, 8.0f, 8.0f, 0.05f, new CubeDeformation(0.0f)).texOffs(42, 26).addBox(4.0f, 0.0f, -6.0f, 0.05f, 8.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)-0.2188f, (float)3.0625f));
        PartDefinition left_arm = upper_body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset((float)5.4288f, (float)1.086f, (float)1.163f));
        PartDefinition cube_r1 = left_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 53).addBox(-1.5f, -2.5f, 0.0f, 3.0f, 5.0f, 0.05f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-0.0333f, (float)0.7f, (float)-0.0055f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition cube_r2 = left_arm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(41, 39).mirror().addBox(-1.5f, 0.0f, -3.0f, 3.0f, 0.05f, 6.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)1.1148f, (float)-1.8f, (float)-2.7772f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition cube_r3 = left_arm.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(45, 53).addBox(0.0f, -2.5f, -3.0f, 0.05f, 5.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)2.5006f, (float)0.7f, (float)-2.2031f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition cube_r4 = left_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(45, 53).addBox(0.0f, -2.5f, -3.0f, 0.05f, 5.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-0.271f, (float)0.7f, (float)-3.3512f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition left_arm_bone = left_arm.addOrReplaceChild("left_arm_bone", CubeListBuilder.create(), PartPose.offset((float)0.4388f, (float)-0.3f, (float)-1.163f));
        PartDefinition cube_r5 = left_arm_bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(20, 46).addBox(-1.0f, -1.0f, -3.5f, 2.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)1.25f, (float)0.0f, (float)-3.0f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition right_arm = upper_body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset((float)-5.4288f, (float)1.086f, (float)1.163f));
        PartDefinition cube_r6 = right_arm.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 53).mirror().addBox(-1.5f, -2.5f, 0.0f, 3.0f, 5.0f, 0.05f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)0.0333f, (float)0.7f, (float)-0.0055f, (float)0.0f, (float)0.3927f, (float)0.0f));
        PartDefinition cube_r7 = right_arm.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(41, 39).addBox(-1.5f, 0.0f, -3.0f, 3.0f, 0.05f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-1.1148f, (float)-1.8f, (float)-2.7772f, (float)0.0f, (float)0.3927f, (float)0.0f));
        PartDefinition cube_r8 = right_arm.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(45, 53).addBox(0.0f, -2.5f, -3.0f, 0.05f, 5.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-2.5006f, (float)0.7f, (float)-2.2031f, (float)0.0f, (float)0.3927f, (float)0.0f));
        PartDefinition cube_r9 = right_arm.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(45, 53).addBox(0.0f, -2.5f, -3.0f, 0.05f, 5.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.271f, (float)0.7f, (float)-3.3512f, (float)0.0f, (float)0.3927f, (float)0.0f));
        PartDefinition right_arm_bone = right_arm.addOrReplaceChild("right_arm_bone", CubeListBuilder.create(), PartPose.offset((float)-0.4388f, (float)-0.3f, (float)-1.163f));
        PartDefinition cube_r10 = right_arm_bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(20, 46).mirror().addBox(-1.0f, -1.0f, -3.5f, 2.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)-1.25f, (float)0.0f, (float)-3.0f, (float)0.0f, (float)0.3927f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
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

