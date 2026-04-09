/*
 * NagaModel - Converted from MowziesMobs ModelNaga (LLibrary AdvancedModelRenderer)
 * to NeoForge 1.21.11 EntityModel format. Serpent/dragon creature with wings and segmented tail.
 * Original by BobMowzie, adapted for MegaMod.
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.NagaAnimations;
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

public class NagaModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("megamod", "naga"), "main");
    public final ModelPart body;
    public final ModelPart neck;
    public final ModelPart headJoint;
    public final ModelPart head;
    public final ModelPart jaw;
    public final ModelPart shoulder_L;
    public final ModelPart upperArm_L;
    public final ModelPart lowerArm_L;
    public final ModelPart hand_L;
    public final ModelPart shoulder_R;
    public final ModelPart upperArm_R;
    public final ModelPart lowerArm_R;
    public final ModelPart hand_R;
    public final ModelPart tail1;
    public final ModelPart tail2;
    public final ModelPart tail3;
    public final ModelPart tail4;
    public final ModelPart tail5;
    public final ModelPart tail6;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public NagaModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.headJoint = this.neck.getChild("headJoint");
        this.head = this.headJoint.getChild("head");
        this.jaw = this.headJoint.getChild("jaw");
        ModelPart shoulderLJoint = this.body.getChild("shoulderLJoint");
        this.shoulder_L = shoulderLJoint.getChild("shoulder_L");
        ModelPart upperArmJoint_L = this.shoulder_L.getChild("upperArmJoint_L");
        this.upperArm_L = upperArmJoint_L.getChild("upperArm_L");
        ModelPart lowerArmJoint_L = this.upperArm_L.getChild("lowerArmJoint_L");
        this.lowerArm_L = lowerArmJoint_L.getChild("lowerArm_L");
        ModelPart handJoint_L = this.lowerArm_L.getChild("handJoint_L");
        this.hand_L = handJoint_L.getChild("hand_L");
        ModelPart shoulderRJoint = this.body.getChild("shoulderRJoint");
        this.shoulder_R = shoulderRJoint.getChild("shoulder_R");
        ModelPart upperArmJoint_R = this.shoulder_R.getChild("upperArmJoint_R");
        this.upperArm_R = upperArmJoint_R.getChild("upperArm_R");
        ModelPart lowerArmJoint_R = this.upperArm_R.getChild("lowerArmJoint_R");
        this.lowerArm_R = lowerArmJoint_R.getChild("lowerArm_R");
        ModelPart handJoint_R = this.lowerArm_R.getChild("handJoint_R");
        this.hand_R = handJoint_R.getChild("hand_R");
        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.tail4 = this.tail3.getChild("tail4");
        this.tail5 = this.tail4.getChild("tail5");
        this.tail6 = this.tail5.getChild("tail6");
        this.idleAnimation = NagaAnimations.idle.bake(root);
        this.walkAnimation = NagaAnimations.walk.bake(root);
        this.attackAnimation = NagaAnimations.attack.bake(root);
        this.deathAnimation = NagaAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Body - main serpent body (21x13x21), positioned up and back
        // Original: root at (0,0,0), body at (0, -9, -9), addBox(-10.5, -4, -3, 21, 13, 21)
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-10.5f, -4.0f, -3.0f, 21.0f, 13.0f, 21.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 15.0f, -9.0f));

        // Spike ridges on body (5 spikes along the spine)
        // spike1joint at (0, 1, -12), rotX=0.5236
        PartDefinition spike1joint = body.addOrReplaceChild("spike1joint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 1.0f, -12.0f, 0.5236f, 0.0f, 0.0f));

        spike1joint.addOrReplaceChild("spike1", CubeListBuilder.create()
                        .texOffs(0, 54).addBox(0.0f, 0.0f, 0.0f, 15.0f, 4.0f, 15.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        PartDefinition spike2joint = body.addOrReplaceChild("spike2joint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 1.0f, -4.0f, 0.5236f, 0.0f, 0.0f));

        spike2joint.addOrReplaceChild("spike2", CubeListBuilder.create()
                        .texOffs(0, 54).addBox(0.0f, 0.0f, 0.0f, 15.0f, 4.0f, 15.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        PartDefinition spike3joint = body.addOrReplaceChild("spike3joint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 1.0f, 4.0f, 0.5236f, 0.0f, 0.0f));

        spike3joint.addOrReplaceChild("spike3", CubeListBuilder.create()
                        .texOffs(0, 54).addBox(0.0f, 0.0f, 0.0f, 15.0f, 4.0f, 15.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        spike3joint.addOrReplaceChild("spike3Bottom", CubeListBuilder.create()
                        .texOffs(78, 45).addBox(0.0f, 0.0f, 0.0f, 11.0f, 4.0f, 11.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 2.69f, 5.66f, 0.0f, -0.7854f, 0.0f));

        // Back fin on body
        body.addOrReplaceChild("backFin1", CubeListBuilder.create()
                        .texOffs(120, 103).addBox(0.0f, -15.0f, -4.0f, 0.0f, 15.0f, 25.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -4.0f, -3.0f));

        // Neck - connects body to head
        // neck at (0, 1, -1), addBox(-6.5, -4, -12.2, 13, 8, 15), rotX=0.1745
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(84, 0).addBox(-6.5f, -4.0f, -12.2f, 13.0f, 8.0f, 15.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 1.0f, -1.0f, 0.1745f, 0.0f, 0.0f));

        // Head joint at (0, 1, -13), rotX=0.4363
        PartDefinition headJoint = neck.addOrReplaceChild("headJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 1.0f, -13.0f, 0.4363f, 0.0f, 0.0f));

        // Head (16x6x16, rotated 45 deg diamond shape)
        PartDefinition head = headJoint.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(68, 23).addBox(-8.0f, -3.7f, -8.0f, 16.0f, 6.0f, 16.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -2.0f, -1.0f, 0.0f, 0.7854f, 0.0f));

        // Under head
        headJoint.addOrReplaceChild("underHead", CubeListBuilder.create()
                        .texOffs(122, 51).addBox(-4.5f, 1.12f, -4.03f, 9.0f, 4.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -1.82f, -1.59f, -0.1745f, 0.0f, 0.0f));

        // Upper teeth
        headJoint.addOrReplaceChild("teethUpper", CubeListBuilder.create()
                        .texOffs(171, 56).addBox(-4.0f, -4.0f, -3.0f, 8.0f, 8.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.3f, -6.65f, 1.5708f, 0.7854f, 0.0f));

        // Eyebrows
        PartDefinition eyebrowJoint_R = headJoint.addOrReplaceChild("eyebrowJoint_R", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.95f, -5.0f, -7.36f, 1.0345f, 0.318f, 0.5363f));

        eyebrowJoint_R.addOrReplaceChild("eyebrow_R", CubeListBuilder.create()
                        .texOffs(63, 0).mirror().addBox(-5.5f, 0.0f, 0.0f, 9.0f, 7.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1222f));

        PartDefinition eyebrowJoint_L = headJoint.addOrReplaceChild("eyebrowJoint_L", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.95f, -5.0f, -7.36f, 1.0345f, -0.318f, -0.5363f));

        eyebrowJoint_L.addOrReplaceChild("eyebrow_L", CubeListBuilder.create()
                        .texOffs(63, 0).addBox(-3.5f, 0.0f, 0.0f, 9.0f, 7.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1222f));

        // Jaw (opens/closes for attacks)
        PartDefinition jaw = headJoint.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(116, 62).addBox(-4.5f, 0.16f, -9.37f, 9.0f, 4.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -1.34f, -4.38f, -0.1745f, 0.0f, -0.005236f));

        // Lower teeth
        jaw.addOrReplaceChild("teethLower", CubeListBuilder.create()
                        .texOffs(125, 0).addBox(-4.5f, -6.0f, 0.0f, 9.0f, 6.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.14f, -9.4f, -0.5009f, 0.0f, 0.0f));

        // Left wing/arm: shoulder → upperArm → lowerArm → hand → wing frames
        // shoulder1_L at (0,0,0), addBox(-2, -3, -7, 12, 6, 9), rotY=-0.5236
        PartDefinition shoulderLJoint = body.addOrReplaceChild("shoulderLJoint", CubeListBuilder.create(),
                PartPose.offset(8.0f, -2.0f, -1.0f));

        PartDefinition shoulder_L = shoulderLJoint.addOrReplaceChild("shoulder_L", CubeListBuilder.create()
                        .texOffs(189, 0).addBox(-2.0f, -3.0f, -7.0f, 12.0f, 6.0f, 9.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.5236f, 0.0f));

        // upperArmJoint_L at (9, -1, -1), rotY=0.5236
        PartDefinition upperArmJoint_L = shoulder_L.addOrReplaceChild("upperArmJoint_L", CubeListBuilder.create(),
                PartPose.offsetAndRotation(9.0f, -1.0f, -1.0f, 0.0f, 0.5236f, 0.0f));

        PartDefinition upperArm_L = upperArmJoint_L.addOrReplaceChild("upperArm_L", CubeListBuilder.create()
                        .texOffs(106, 74).addBox(-2.0f, -2.0f, -3.0f, 18.0f, 4.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        // Wing webbing on upper arm
        upperArm_L.addOrReplaceChild("wingWebbing6_L", CubeListBuilder.create()
                        .texOffs(0, 94).addBox(-10.5f, 0.0f, 0.0f, 25.0f, 0.0f, 24.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.5f, 0.0f, -2.0f));

        // lowerArmJoint_L at (15, 0, -0.5)
        PartDefinition lowerArmJoint_L = upperArm_L.addOrReplaceChild("lowerArmJoint_L", CubeListBuilder.create(),
                PartPose.offset(15.0f, 0.0f, -0.5f));

        PartDefinition lowerArm_L = lowerArmJoint_L.addOrReplaceChild("lowerArm_L", CubeListBuilder.create()
                        .texOffs(102, 83).addBox(0.0f, -2.0f, -2.0f, 22.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, -0.5f));

        // Wing webbing on lower arm
        lowerArm_L.addOrReplaceChild("wingWebbing5_L", CubeListBuilder.create()
                        .texOffs(193, 128).addBox(-12.0f, 0.0f, 0.0f, 20.0f, 0.0f, 23.0f, new CubeDeformation(0.0f)),
                PartPose.offset(12.0f, 0.0f, 1.0f));

        // handJoint_L at (20, 0, 0)
        PartDefinition handJoint_L = lowerArm_L.addOrReplaceChild("handJoint_L", CubeListBuilder.create(),
                PartPose.offset(20.0f, 0.0f, 0.0f));

        PartDefinition hand_L = handJoint_L.addOrReplaceChild("hand_L", CubeListBuilder.create()
                        .texOffs(222, 0).addBox(0.0f, -2.0f, -2.0f, 11.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Wing claw
        hand_L.addOrReplaceChild("wingClaw_L", CubeListBuilder.create()
                        .texOffs(231, 8).addBox(0.0f, -2.0f, -2.0f, 9.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.5236f, 0.0f));

        // Wing frame 1 (longest, main wing strut)
        PartDefinition wingFrame1_L = hand_L.addOrReplaceChild("wingFrame1_L", CubeListBuilder.create()
                        .texOffs(50, 91).addBox(0.0f, -1.5f, -1.5f, 53.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offset(11.0f, 0.0f, -0.5f));

        wingFrame1_L.addOrReplaceChild("wingWebbing1_L", CubeListBuilder.create()
                        .texOffs(119, 97).addBox(0.0f, 0.0f, -16.0f, 53.0f, 0.0f, 31.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-2.0f, 0.0f, 0.0f, 0.0f, -0.3054f, 0.0f));

        // Wing frame 2
        PartDefinition wingFrame2_L = hand_L.addOrReplaceChild("wingFrame2_L", CubeListBuilder.create()
                        .texOffs(0, 79).addBox(0.0f, -1.5f, -1.5f, 50.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(7.0f, 0.0f, 0.0f, 0.0f, -0.6109f, 0.0f));

        wingFrame2_L.addOrReplaceChild("wingWebbing2_L", CubeListBuilder.create()
                        .texOffs(20, 98).addBox(0.0f, 0.0f, -15.0f, 50.0f, 0.0f, 30.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-1.5f, 0.0f, 0.0f, 0.0f, -0.3054f, 0.0f));

        // Wing frame 3
        PartDefinition wingFrame3_L = hand_L.addOrReplaceChild("wingFrame3_L", CubeListBuilder.create()
                        .texOffs(0, 73).addBox(0.0f, -1.5f, -1.5f, 43.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(4.0f, 0.0f, 0.0f, 0.0f, -1.2217f, 0.0f));

        wingFrame3_L.addOrReplaceChild("wingWebbing3_L", CubeListBuilder.create()
                        .texOffs(144, 71).addBox(0.0f, 0.0f, -13.0f, 43.0f, 0.0f, 26.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(1.0f, 0.0f, 0.0f, 0.0f, -0.3054f, 0.0f));

        // Wing frame 4 (shortest, innermost)
        PartDefinition wingFrame4_L = hand_L.addOrReplaceChild("wingFrame4_L", CubeListBuilder.create()
                        .texOffs(0, 85).addBox(0.0f, -1.5f, -1.5f, 38.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(2.0f, 0.0f, 0.0f, 0.0f, -1.8326f, 0.0f));

        wingFrame4_L.addOrReplaceChild("wingWebbing4_L", CubeListBuilder.create()
                        .texOffs(159, 36).addBox(0.0f, 0.0f, -22.0f, 31.0f, 0.0f, 35.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.5f, 0.0f, 0.0f, 0.0f, -0.6545f, 0.0f));

        // Right wing/arm (mirrored)
        PartDefinition shoulderRJoint = body.addOrReplaceChild("shoulderRJoint", CubeListBuilder.create(),
                PartPose.offset(-8.0f, -2.0f, -1.0f));

        PartDefinition shoulder_R = shoulderRJoint.addOrReplaceChild("shoulder_R", CubeListBuilder.create()
                        .texOffs(189, 0).mirror().addBox(-10.0f, -3.0f, -7.0f, 12.0f, 6.0f, 9.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.5236f, 0.0f));

        PartDefinition upperArmJoint_R = shoulder_R.addOrReplaceChild("upperArmJoint_R", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-9.0f, -1.0f, -1.0f, 0.0f, -0.5236f, 0.0f));

        PartDefinition upperArm_R = upperArmJoint_R.addOrReplaceChild("upperArm_R", CubeListBuilder.create()
                        .texOffs(106, 74).mirror().addBox(-16.0f, -2.0f, -3.0f, 18.0f, 4.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 2.0f, 0.0f));

        upperArm_R.addOrReplaceChild("wingWebbing6_R", CubeListBuilder.create()
                        .texOffs(0, 94).mirror().addBox(-14.5f, 0.0f, 0.0f, 25.0f, 0.0f, 24.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(-0.5f, 0.0f, -2.0f));

        PartDefinition lowerArmJoint_R = upperArm_R.addOrReplaceChild("lowerArmJoint_R", CubeListBuilder.create(),
                PartPose.offset(-15.0f, 0.0f, -0.5f));

        PartDefinition lowerArm_R = lowerArmJoint_R.addOrReplaceChild("lowerArm_R", CubeListBuilder.create()
                        .texOffs(102, 83).mirror().addBox(-22.0f, -2.0f, -2.0f, 22.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 0.0f, -0.5f));

        lowerArm_R.addOrReplaceChild("wingWebbing5_R", CubeListBuilder.create()
                        .texOffs(193, 128).mirror().addBox(-8.0f, 0.0f, 0.0f, 20.0f, 0.0f, 23.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(-12.0f, 0.0f, 1.0f));

        PartDefinition handJoint_R = lowerArm_R.addOrReplaceChild("handJoint_R", CubeListBuilder.create(),
                PartPose.offset(-20.0f, 0.0f, 0.0f));

        PartDefinition hand_R = handJoint_R.addOrReplaceChild("hand_R", CubeListBuilder.create()
                        .texOffs(222, 0).addBox(-11.0f, -2.0f, -2.0f, 11.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        hand_R.addOrReplaceChild("wingClaw_R", CubeListBuilder.create()
                        .texOffs(231, 8).mirror().addBox(-9.0f, -2.0f, -2.0f, 9.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.5236f, 0.0f));

        PartDefinition wingFrame1_R = hand_R.addOrReplaceChild("wingFrame1_R", CubeListBuilder.create()
                        .texOffs(50, 91).mirror().addBox(-53.0f, -1.5f, -1.5f, 53.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(-11.0f, 0.0f, -0.5f));

        wingFrame1_R.addOrReplaceChild("wingWebbing1_R", CubeListBuilder.create()
                        .texOffs(119, 97).mirror().addBox(-53.0f, 0.0f, -16.0f, 53.0f, 0.0f, 31.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(2.0f, 0.0f, 0.0f, 0.0f, 0.3054f, 0.0f));

        PartDefinition wingFrame2_R = hand_R.addOrReplaceChild("wingFrame2_R", CubeListBuilder.create()
                        .texOffs(0, 79).mirror().addBox(-50.0f, -1.5f, -1.5f, 50.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-7.0f, 0.0f, 0.0f, 0.0f, 0.6109f, 0.0f));

        wingFrame2_R.addOrReplaceChild("wingWebbing2_R", CubeListBuilder.create()
                        .texOffs(20, 98).mirror().addBox(-50.0f, 0.0f, -15.0f, 50.0f, 0.0f, 30.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(1.5f, 0.0f, 0.0f, 0.0f, 0.3054f, 0.0f));

        PartDefinition wingFrame3_R = hand_R.addOrReplaceChild("wingFrame3_R", CubeListBuilder.create()
                        .texOffs(0, 73).mirror().addBox(-43.0f, -1.5f, -1.5f, 43.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-4.0f, 0.0f, 0.0f, 0.0f, 1.2217f, 0.0f));

        wingFrame3_R.addOrReplaceChild("wingWebbing3_R", CubeListBuilder.create()
                        .texOffs(144, 71).mirror().addBox(-43.0f, 0.0f, -13.0f, 43.0f, 0.0f, 26.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-1.0f, 0.0f, 0.0f, 0.0f, 0.3054f, 0.0f));

        PartDefinition wingFrame4_R = hand_R.addOrReplaceChild("wingFrame4_R", CubeListBuilder.create()
                        .texOffs(0, 85).mirror().addBox(-38.0f, -1.5f, -1.5f, 38.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-2.0f, 0.0f, 0.0f, 0.0f, 1.8326f, 0.0f));

        wingFrame4_R.addOrReplaceChild("wingWebbing4_R", CubeListBuilder.create()
                        .texOffs(159, 36).mirror().addBox(-31.0f, 0.0f, -22.0f, 31.0f, 0.0f, 35.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-0.5f, 0.0f, 0.0f, 0.0f, 0.6545f, 0.0f));

        // Tail chain: tail1 → tail2 → tail3 → tail4 → tail5 → tail6
        // tail1 at (0, 1.5, 17), addBox(-7.5, -4.5, -3, 15, 9, 19)
        PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
                        .texOffs(140, 0).addBox(-7.5f, -4.5f, -3.0f, 15.0f, 9.0f, 19.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 1.5f, 17.0f));

        // Spike on tail1
        PartDefinition spike4joint = tail1.addOrReplaceChild("spike4joint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -1.0f, 0.55f, 0.5236f, 0.0f, 0.0f));

        spike4joint.addOrReplaceChild("spike4", CubeListBuilder.create()
                        .texOffs(45, 52).addBox(0.0f, 0.0f, 0.0f, 11.0f, 6.0f, 11.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        // Back fin on tail1
        tail1.addOrReplaceChild("backFin2", CubeListBuilder.create()
                        .texOffs(170, 109).addBox(0.0f, -14.0f, -3.0f, 0.0f, 14.0f, 19.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -4.5f, 0.0f));

        // tail2 at (0, -1, 15), addBox(-6.5, -3, -1, 13, 6, 17)
        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                        .texOffs(115, 28).addBox(-6.5f, -3.0f, -1.0f, 13.0f, 6.0f, 17.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -1.0f, 15.0f));

        // Spike on tail2
        PartDefinition spike5joint = tail2.addOrReplaceChild("spike5joint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 0.0f, -4.05f, 0.5236f, 0.0f, 0.0f));

        spike5joint.addOrReplaceChild("spike5", CubeListBuilder.create()
                        .texOffs(38, 36).addBox(0.0f, 0.0f, 0.0f, 9.0f, 4.0f, 9.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        // Back fin on tail2
        tail2.addOrReplaceChild("backFin3", CubeListBuilder.create()
                        .texOffs(170, 123).addBox(0.0f, -9.0f, -3.0f, 0.0f, 9.0f, 19.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -3.0f, 0.0f));

        // Back wings on tail2
        tail2.addOrReplaceChild("backWing_L", CubeListBuilder.create()
                        .texOffs(35, 128).addBox(0.0f, 0.0f, 0.0f, 30.0f, 0.0f, 25.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(4.0f, 0.0f, 4.0f, 0.0f, -0.5236f, 0.0f));

        tail2.addOrReplaceChild("backWing_R", CubeListBuilder.create()
                        .texOffs(35, 128).mirror().addBox(-30.0f, 0.0f, 0.0f, 30.0f, 0.0f, 25.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-4.0f, 0.0f, 4.0f, 0.0f, 0.5236f, 0.0f));

        // tail3 at (0, 0, 15), addBox(-5.5, -2.5, 0, 11, 4, 16)
        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                        .texOffs(0, 34).addBox(-5.5f, -2.5f, 0.0f, 11.0f, 4.0f, 16.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 15.0f));

        // tail4 at (0, -0.5, 15), addBox(-3.5, -1.5, 0, 7, 3, 15)
        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                        .texOffs(142, 52).addBox(-3.5f, -1.5f, 0.0f, 7.0f, 3.0f, 15.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -0.5f, 15.0f));

        // tail5 at (0, 0, 14), addBox(-1.5, -1, 0, 3, 2, 13)
        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                        .texOffs(162, 30).addBox(-1.5f, -1.0f, 0.0f, 3.0f, 2.0f, 13.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 14.0f));

        // tail6 (tip) at (0, 0, 13)
        PartDefinition tail6 = tail5.addOrReplaceChild("tail6", CubeListBuilder.create(),
                PartPose.offset(0.0f, 0.0f, 13.0f));

        // Tail fin
        tail6.addOrReplaceChild("tailFin", CubeListBuilder.create()
                        .texOffs(0, 128).addBox(-5.0f, 0.0f, -5.0f, 30.0f, 0.0f, 30.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonBossRenderState) {
            DungeonEntityRenderers.DungeonBossRenderState bossState = (DungeonEntityRenderers.DungeonBossRenderState) state;
            this.idleAnimation.apply(bossState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(bossState.attackAnimationState, state.ageInTicks);
            this.deathAnimation.apply(bossState.deathAnimationState, state.ageInTicks);
        }
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0f, 1.0f);
    }
}
