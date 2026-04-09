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
import com.ultra.megamod.feature.dungeons.client.model.animations.SkeletonMinionAnimations;
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

public class SkeletonMinionModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"skeleton_minion"), "main");
    public final ModelPart rootPart;
    public final ModelPart torso;
    public final ModelPart main_torso;
    public final ModelPart hi_head;
    public final ModelPart left_arm;
    public final ModelPart right_arm;
    public final ModelPart right_leg;
    public final ModelPart left_leg;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;

    public SkeletonMinionModel(ModelPart root) {
        super(root);
        this.rootPart = root.getChild("root");
        this.torso = this.rootPart.getChild("torso");
        this.main_torso = this.torso.getChild("main_torso");
        this.hi_head = this.main_torso.getChild("hi_head");
        this.left_arm = this.torso.getChild("left_arm");
        this.right_arm = this.torso.getChild("right_arm");
        this.right_leg = this.rootPart.getChild("right_leg");
        this.left_leg = this.rootPart.getChild("left_leg");
        this.idleAnimation = SkeletonMinionAnimations.idle.bake(root);
        this.walkAnimation = SkeletonMinionAnimations.walk.bake(root);
        this.attackAnimation = SkeletonMinionAnimations.attack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)26.0f, (float)0.0f));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-13.0f, (float)0.0f));
        PartDefinition main_torso = torso.addOrReplaceChild("main_torso", CubeListBuilder.create().texOffs(12, 1).addBox(-4.0f, -2.0f, -3.0f, 8.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(3, 27).mirror().addBox(-1.0f, -13.0f, 1.0f, 2.0f, 11.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(16, 5).addBox(-4.0f, -10.0f, 1.5f, 8.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(8, 44).addBox(-4.0f, -10.0f, -2.5f, 2.0f, 2.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(22, 26).addBox(2.0f, -10.0f, -2.5f, 2.0f, 2.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(26, 8).addBox(2.0f, -10.0f, -2.35f, 2.0f, 2.0f, 3.0f, new CubeDeformation(0.3f)).texOffs(24, 2).addBox(-4.0f, -10.0f, -2.35f, 2.0f, 2.0f, 3.0f, new CubeDeformation(0.3f)).texOffs(28, 8).addBox(-4.0f, -6.0f, -3.35f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.3f)).texOffs(31, 8).addBox(1.0f, -6.0f, -3.35f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.3f)).texOffs(49, 48).addBox(-4.0f, -6.0f, -2.5f, 2.0f, 2.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(4, 49).addBox(2.0f, -6.0f, -2.5f, 2.0f, 2.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(38, 42).addBox(-4.0f, -6.0f, 1.5f, 8.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(16, 5).addBox(-4.0f, -6.0f, 1.25f, 8.0f, 2.0f, 2.0f, new CubeDeformation(0.3f)).texOffs(32, 8).addBox(2.0f, -6.0f, -1.75f, 2.0f, 2.0f, 2.4f, new CubeDeformation(0.3f)).texOffs(19, 5).addBox(-4.0f, -6.0f, -1.75f, 2.0f, 2.0f, 2.4f, new CubeDeformation(0.3f)), PartPose.offset((float)0.0f, (float)0.0f, (float)0.0f));
        PartDefinition hi_head = main_torso.addOrReplaceChild("hi_head", CubeListBuilder.create().texOffs(11, 0).addBox(-4.0f, -6.0f, -5.0f, 8.0f, 6.0f, 7.0f, new CubeDeformation(0.0f)).texOffs(27, 0).addBox(-4.0f, -6.0f, -5.0f, 8.0f, 6.0f, 7.0f, new CubeDeformation(-0.3f)), PartPose.offset((float)0.0f, (float)-13.0f, (float)1.0f));
        PartDefinition hi_head_r1 = hi_head.addOrReplaceChild("hi_head_r1", CubeListBuilder.create().texOffs(11, 0).addBox(-4.0f, 2.5f, -3.5f, 8.0f, 1.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-2.2f, (float)-1.6f, (float)0.0524f, (float)0.0f, (float)0.0f));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(1, 27).addBox(-0.5f, -0.5f, -1.5f, 3.0f, 11.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offset((float)4.5f, (float)-10.5f, (float)0.5f));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 27).mirror().addBox(-2.5f, -0.5f, -1.5f, 3.0f, 11.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)-4.5f, (float)-10.5f, (float)0.5f));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(1, 27).addBox(-1.5f, 1.0f, -1.5f, 3.0f, 11.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-2.5f, (float)-14.0f, (float)0.0f));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(1, 27).addBox(-1.5f, 1.0f, -1.5f, 3.0f, 11.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offset((float)2.5f, (float)-14.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.MinionRenderState) {
            DungeonEntityRenderers.MinionRenderState minionState = (DungeonEntityRenderers.MinionRenderState)state;
            this.idleAnimation.apply(minionState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(minionState.attackAnimationState, state.ageInTicks);
        }
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0f, 1.0f);
    }
}

