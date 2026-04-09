/*
 * WroughtnautModel - Converted from MowziesMobs ModelWroughtnaut (LLibrary AdvancedModelRenderer)
 * to NeoForge 1.21.11 EntityModel format. Hulking iron golem/warden with massive axe.
 * Original by BobMowzie, adapted for MegaMod.
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.WroughtnautAnimations;
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

public class WroughtnautModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("megamod", "wroughtnaut"), "main");
    public final ModelPart waist;
    public final ModelPart stomach;
    public final ModelPart chest;
    public final ModelPart neck;
    public final ModelPart head;
    public final ModelPart shoulderRight;
    public final ModelPart upperArmRight;
    public final ModelPart lowerArmRight;
    public final ModelPart handRight;
    public final ModelPart axeHandle;
    public final ModelPart shoulderLeft;
    public final ModelPart upperArmLeft;
    public final ModelPart lowerArmLeft;
    public final ModelPart handLeft;
    public final ModelPart groin;
    public final ModelPart thighRight;
    public final ModelPart calfRight;
    public final ModelPart footRight;
    public final ModelPart thighLeft;
    public final ModelPart calfLeft;
    public final ModelPart footLeft;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public WroughtnautModel(ModelPart root) {
        super(root);
        ModelPart rootBox = root.getChild("rootBox");
        this.waist = rootBox.getChild("waist");
        ModelPart stomachJoint = this.waist.getChild("stomachJoint");
        this.stomach = stomachJoint.getChild("stomach");
        ModelPart chestJoint = this.stomach.getChild("chestJoint");
        this.chest = chestJoint.getChild("chest");
        this.neck = this.chest.getChild("neck");
        this.head = this.neck.getChild("head");
        ModelPart shoulderRightJoint = this.chest.getChild("shoulderRightJoint");
        this.shoulderRight = shoulderRightJoint.getChild("shoulderRight");
        ModelPart upperArmRightJoint = this.shoulderRight.getChild("upperArmRightJoint");
        this.upperArmRight = upperArmRightJoint.getChild("upperArmRight");
        ModelPart lowerArmRightJoint = this.upperArmRight.getChild("lowerArmRightJoint");
        this.lowerArmRight = lowerArmRightJoint.getChild("lowerArmRight");
        ModelPart handRightJoint = this.lowerArmRight.getChild("handRightJoint");
        this.handRight = handRightJoint.getChild("handRight");
        ModelPart axeBase = this.handRight.getChild("axeBase");
        this.axeHandle = axeBase.getChild("axeHandle");
        ModelPart shoulderLeftJoint = this.chest.getChild("shoulderLeftJoint");
        this.shoulderLeft = shoulderLeftJoint.getChild("shoulderLeft");
        ModelPart upperArmLeftJoint = this.shoulderLeft.getChild("upperArmLeftJoint");
        this.upperArmLeft = upperArmLeftJoint.getChild("upperArmLeft");
        ModelPart lowerArmLeftJoint = this.upperArmLeft.getChild("lowerArmLeftJoint");
        this.lowerArmLeft = lowerArmLeftJoint.getChild("lowerArmLeft");
        ModelPart handLeftJoint = this.lowerArmLeft.getChild("handLeftJoint");
        this.handLeft = handLeftJoint.getChild("handLeft");
        ModelPart groinJoint = this.waist.getChild("groinJoint");
        this.groin = groinJoint.getChild("groin");
        ModelPart thighRightJoint = this.groin.getChild("thighRightJoint");
        ModelPart thighRightJoint2 = thighRightJoint.getChild("thighRightJoint2");
        this.thighRight = thighRightJoint2.getChild("thighRight");
        ModelPart calfRightJoint = this.thighRight.getChild("calfRightJoint");
        this.calfRight = calfRightJoint.getChild("calfRight");
        ModelPart footRightJoint = this.calfRight.getChild("footRightJoint");
        this.footRight = footRightJoint.getChild("footRight");
        ModelPart thighLeftJoint = this.groin.getChild("thighLeftJoint");
        ModelPart thighLeftJoint2 = thighLeftJoint.getChild("thighLeftJoint2");
        this.thighLeft = thighLeftJoint2.getChild("thighLeft");
        ModelPart calfLeftJoint = this.thighLeft.getChild("calfLeftJoint");
        this.calfLeft = calfLeftJoint.getChild("calfLeft");
        ModelPart footLeftJoint = this.calfLeft.getChild("footLeftJoint");
        this.footLeft = footLeftJoint.getChild("footLeft");
        this.idleAnimation = WroughtnautAnimations.idle.bake(root);
        this.walkAnimation = WroughtnautAnimations.walk.bake(root);
        this.attackAnimation = WroughtnautAnimations.attack.bake(root);
        this.deathAnimation = WroughtnautAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // rootBox at (0, -1, 0)
        PartDefinition rootBox = partdefinition.addOrReplaceChild("rootBox", CubeListBuilder.create(),
                PartPose.offset(0.0f, -1.0f, 0.0f));

        // Waist - wide iron belt (16x6x16, rotated 45 deg)
        // Original: setRotationPoint(0,0,0), addBox(-8,0,-8, 16,6,16), rotY=0.7854
        PartDefinition waist = rootBox.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(64, 41).addBox(-8.0f, 0.0f, -8.0f, 16.0f, 6.0f, 16.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // Stomach joint → stomach (12x17x12, rotated 45 deg diamond)
        // stomachJoint at (0,0,0), rotX=0.2618, rotY=-0.7854
        PartDefinition stomachJoint = waist.addOrReplaceChild("stomachJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.2618f, -0.7854f, 0.0f));

        PartDefinition stomach = stomachJoint.addOrReplaceChild("stomach", CubeListBuilder.create()
                        .texOffs(80, 63).addBox(-6.0f, -13.7f, -6.0f, 12.0f, 17.0f, 12.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // Sword on back (decorative, attached to stomach)
        stomach.addOrReplaceChild("swordJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -3.0f, 10.0f, 0.0f, -0.7854f, 0.0f));

        // Chest joint → chest (28x18x18, rotated 45 deg)
        // chestJoint at (0, -14, 0), rotY=-0.7854
        PartDefinition chestJoint = stomach.addOrReplaceChild("chestJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -14.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        PartDefinition chest = chestJoint.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(36, 92).addBox(-14.0f, 0.0f, 0.0f, 28.0f, 18.0f, 18.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, -9.0f, 0.7854f, 0.0f, 0.0f));

        // Neck → Head (6x10x6, 45 deg rotated diamond shape)
        // neck at (0, -1.4, 15.1), rotX=-1.0472
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -1.4f, 15.1f, -1.0472f, 0.0f, 0.0f));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -9.0f, -3.0f, 6.0f, 10.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // Helmet over head (8x12x8)
        PartDefinition helmet = head.addOrReplaceChild("helmet", CubeListBuilder.create()
                        .texOffs(32, 20).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 12.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Tusks on helmet
        PartDefinition tuskRight1 = helmet.addOrReplaceChild("tuskRight1", CubeListBuilder.create()
                        .texOffs(64, 63).addBox(0.0f, -1.5f, -1.5f, 6.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(3.0f, 0.5f, 2.5f, 0.0f, 0.0f, 0.4363f));

        tuskRight1.addOrReplaceChild("tuskRight2", CubeListBuilder.create()
                        .texOffs(110, 97).addBox(0.0f, -2.0f, -1.0f, 7.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(6.0f, 1.5f, 0.0f, 0.0f, 0.0f, -0.8727f));

        PartDefinition tuskLeft1 = helmet.addOrReplaceChild("tuskLeft1", CubeListBuilder.create()
                        .texOffs(13, 60).addBox(-1.5f, -1.5f, -6.0f, 3.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-2.5f, 0.5f, -3.0f, 0.4363f, 0.0f, 0.0f));

        tuskLeft1.addOrReplaceChild("tuskLeft2", CubeListBuilder.create()
                        .texOffs(110, 101).addBox(0.0f, -2.0f, -7.0f, 2.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-1.0f, 1.5f, -6.0f, -0.8727f, 0.0f, 0.0f));

        // Horns on helmet
        PartDefinition hornRight1 = helmet.addOrReplaceChild("hornRight1", CubeListBuilder.create()
                        .texOffs(34, 13).addBox(0.0f, -1.5f, -1.5f, 8.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(3.0f, -8.05f, 2.5f, 0.0f, 0.0f, -0.3491f));

        hornRight1.addOrReplaceChild("hornRight2", CubeListBuilder.create()
                        .texOffs(16, 44).addBox(0.0f, -2.0f, -1.0f, 6.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(8.0f, 1.5f, 0.0f, 0.0f, 0.0f, -1.2217f));

        PartDefinition hornLeft1 = helmet.addOrReplaceChild("hornLeft1", CubeListBuilder.create()
                        .texOffs(12, 17).addBox(-1.5f, -1.5f, -8.0f, 3.0f, 3.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-2.5f, -8.05f, -3.0f, -0.3491f, 0.0f, 0.0f));

        hornLeft1.addOrReplaceChild("hornLeft2", CubeListBuilder.create()
                        .texOffs(30, 0).addBox(0.0f, -2.0f, -11.0f, 2.0f, 2.0f, 11.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-1.0f, 1.5f, -8.0f, -1.2217f, 0.0f, 0.0f));

        // Eyes
        head.addOrReplaceChild("eyeLeft", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-4.0f, -4.0f, 4.0f));

        head.addOrReplaceChild("eyeRight", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, -0.5f, 0.0f, 2.0f, 1.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offset(4.0f, -4.0f, 4.0f));

        // Right shoulder → upperArmRight (20x8x8) → lowerArmRight (15x6x6) → handRight → axe
        // shoulderRightJoint at (10, 4, 14.9), rotX=-1.0472
        PartDefinition shoulderRightJoint = chest.addOrReplaceChild("shoulderRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(10.0f, 4.0f, 14.9f, -1.0472f, 0.0f, 0.0f));

        PartDefinition shoulderRight = shoulderRightJoint.addOrReplaceChild("shoulderRight", CubeListBuilder.create()
                        .texOffs(21, 56).addBox(-4.0f, -7.0f, -5.5f, 15.0f, 10.0f, 13.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 1.0f));

        // upperArmRightJoint at (5, 1, 1) inside shoulder
        PartDefinition upperArmRightJoint = shoulderRight.addOrReplaceChild("upperArmRightJoint", CubeListBuilder.create(),
                PartPose.offset(5.0f, 1.0f, 1.0f));

        PartDefinition upperArmRight = upperArmRightJoint.addOrReplaceChild("upperArmRight", CubeListBuilder.create()
                        .texOffs(24, 40).addBox(-5.0f, -4.0f, -4.0f, 20.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.7854f, 0.0f, 0.0f));

        // Elbow
        upperArmRight.addOrReplaceChild("elbowRight", CubeListBuilder.create()
                        .texOffs(70, 24).addBox(-3.0f, -3.0f, 0.0f, 6.0f, 6.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(15.0f, 1.6f, 1.7f, -0.7854f, 0.0f, 0.0f));

        // lowerArmRightJoint at (15, 0, 0), rotX=0.7854
        PartDefinition lowerArmRightJoint = upperArmRight.addOrReplaceChild("lowerArmRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(15.0f, 0.0f, 0.0f, 0.7854f, 0.0f, 0.0f));

        PartDefinition lowerArmRight = lowerArmRightJoint.addOrReplaceChild("lowerArmRight", CubeListBuilder.create()
                        .texOffs(86, 29).addBox(0.0f, -4.0f, -4.0f, 15.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 1.5f, 0.0f, -0.7854f, 0.0f, 0.0f));

        // handRightJoint at (16, -1, -1), rotX=0.7854
        PartDefinition handRightJoint = lowerArmRight.addOrReplaceChild("handRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(16.0f, -1.0f, -1.0f, 0.7854f, 0.0f, 0.0f));

        PartDefinition handRight = handRightJoint.addOrReplaceChild("handRight", CubeListBuilder.create()
                        .texOffs(98, 14).addBox(-2.0f, -4.0f, -2.0f, 8.0f, 8.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // Axe (in right hand)
        PartDefinition axeBase = handRight.addOrReplaceChild("axeBase", CubeListBuilder.create(),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        PartDefinition axeHandle = axeBase.addOrReplaceChild("axeHandle", CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-1.5f, -44.0f, -1.5f, 3.0f, 50.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offset(3.0f, 0.0f, 1.0f));

        // Axe blade right side
        PartDefinition axeBladeRight = axeHandle.addOrReplaceChild("axeBladeRight", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -37.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        axeBladeRight.addOrReplaceChild("axeBladeRight1", CubeListBuilder.create()
                        .texOffs(84, 0).addBox(0.0f, -4.5f, -1.0f, 10.0f, 8.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        axeBladeRight.addOrReplaceChild("axeBladeRight2", CubeListBuilder.create()
                        .texOffs(56, 0).mirror().addBox(-5.5f, 0.0f, -1.0f, 11.0f, 17.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(17.7f, -3.2f, 0.01f, 0.0f, 0.0f, 0.5236f));

        axeBladeRight.addOrReplaceChild("axeBladeRight3", CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-5.5f, 0.0f, -1.0f, 11.0f, 17.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(17.7f, 2.3f, -0.01f, 0.0f, 0.0f, 2.618f));

        // Axe blade left side
        PartDefinition axeBladeLeft = axeHandle.addOrReplaceChild("axeBladeLeft", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -37.0f, 0.0f, 0.0f, -3.927f, 0.0f));

        axeBladeLeft.addOrReplaceChild("axeBladeLeft1", CubeListBuilder.create()
                        .texOffs(84, 0).addBox(0.0f, -4.5f, -1.0f, 10.0f, 8.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        axeBladeLeft.addOrReplaceChild("axeBladeLeft2", CubeListBuilder.create()
                        .texOffs(56, 0).mirror().addBox(-5.5f, 0.0f, -1.0f, 11.0f, 17.0f, 2.0f, new CubeDeformation(0.01f)).mirror(false),
                PartPose.offsetAndRotation(17.7f, -3.2f, 0.01f, 0.0f, 0.0f, 0.5236f));

        axeBladeLeft.addOrReplaceChild("axeBladeLeft3", CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-5.5f, 0.0f, -1.0f, 11.0f, 17.0f, 2.0f, new CubeDeformation(0.01f)),
                PartPose.offsetAndRotation(17.7f, 2.3f, -0.01f, 0.0f, 0.0f, 2.618f));

        // Left shoulder (mirrored, no axe)
        PartDefinition shoulderLeftJoint = chest.addOrReplaceChild("shoulderLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-10.0f, 4.0f, 14.9f, -1.0472f, 0.0f, 0.0f));

        PartDefinition shoulderLeft = shoulderLeftJoint.addOrReplaceChild("shoulderLeft", CubeListBuilder.create()
                        .texOffs(21, 56).mirror().addBox(-4.0f, -7.0f, -7.5f, 15.0f, 10.0f, 13.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 1.0f, 0.0f, 3.1416f, 0.0f));

        PartDefinition upperArmLeftJoint = shoulderLeft.addOrReplaceChild("upperArmLeftJoint", CubeListBuilder.create(),
                PartPose.offset(5.0f, 1.0f, -1.0f));

        PartDefinition upperArmLeft = upperArmLeftJoint.addOrReplaceChild("upperArmLeft", CubeListBuilder.create()
                        .texOffs(24, 40).addBox(-5.0f, -4.0f, -4.0f, 20.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.7854f, 0.0f, 0.0f));

        upperArmLeft.addOrReplaceChild("elbowLeft", CubeListBuilder.create()
                        .texOffs(70, 24).addBox(-3.0f, -3.0f, 0.0f, 6.0f, 6.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(15.0f, -1.6f, -1.7f, -0.7854f, 0.0f, 0.0f));

        PartDefinition lowerArmLeftJoint = upperArmLeft.addOrReplaceChild("lowerArmLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(15.0f, 0.0f, 0.0f, 0.7854f, 0.0f, 0.0f));

        PartDefinition lowerArmLeft = lowerArmLeftJoint.addOrReplaceChild("lowerArmLeft", CubeListBuilder.create()
                        .texOffs(86, 29).addBox(0.0f, -2.0f, -2.0f, 15.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -1.5f, 0.0f, -0.7854f, 0.0f, 0.0f));

        PartDefinition handLeftJoint = lowerArmLeft.addOrReplaceChild("handLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(16.0f, 1.0f, 1.0f, 0.7854f, 0.0f, 0.0f));

        PartDefinition handLeft = handLeftJoint.addOrReplaceChild("handLeft", CubeListBuilder.create()
                        .texOffs(98, 14).addBox(-2.0f, -4.0f, -2.0f, 8.0f, 8.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // Groin (connected to waist via groinJoint)
        // groin at (0, 4, 0), addBox(-3, -5.5, -5.5, 6, 11, 11), rotX=-0.7854
        // groinJoint at (0, 6, 0), rotY=-0.7854
        PartDefinition groinJoint = waist.addOrReplaceChild("groinJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 6.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        PartDefinition groin = groinJoint.addOrReplaceChild("groin", CubeListBuilder.create()
                        .texOffs(0, 106).addBox(-3.0f, -5.5f, -5.5f, 6.0f, 11.0f, 11.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 4.0f, 0.0f, -1.5708f, 0.0f, 0.0f));

        // Groin front/back flaps
        groin.addOrReplaceChild("groinFront", CubeListBuilder.create()
                        .texOffs(0, 92).addBox(-5.0f, 0.0f, -1.0f, 10.0f, 12.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, -7.0f, -0.1745f, 0.0f, 0.0f));

        groin.addOrReplaceChild("groinBack", CubeListBuilder.create()
                        .texOffs(0, 92).addBox(-5.0f, 0.0f, -1.0f, 10.0f, 12.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 7.0f, 0.1745f, 0.0f, 0.0f));

        // Right leg: thighRightJoint → thighRight (7x13x7) → calfRight (5x12x5) → footRight (6x3x10)
        // thighRightJoint at (5, 0, 0), rotY=-0.7854
        PartDefinition thighRightJoint = groin.addOrReplaceChild("thighRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f));

        // thighRightJoint2 at (0,0,0), rotX=-0.8727
        PartDefinition thighRightJoint2 = thighRightJoint.addOrReplaceChild("thighRightJoint2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.8727f, 0.0f, 0.0f));

        PartDefinition thighRight = thighRightJoint2.addOrReplaceChild("thighRight", CubeListBuilder.create()
                        .texOffs(26, 90).addBox(-3.5f, 0.0f, -3.5f, 7.0f, 13.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // Knee decorations
        thighRight.addOrReplaceChild("kneeRight", CubeListBuilder.create()
                        .texOffs(24, 80).addBox(-3.0f, -1.7f, -3.0f, 6.0f, 4.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 13.0f, 0.0f, -0.5236f, -0.7854f, 0.0f));

        // calfRightJoint at (0, 14.5, 0), rotX=1.5708, rotY=-0.7854
        PartDefinition calfRightJoint = thighRight.addOrReplaceChild("calfRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 14.5f, 0.0f, 1.5708f, -0.7854f, 0.0f));

        PartDefinition calfRight = calfRightJoint.addOrReplaceChild("calfRight", CubeListBuilder.create()
                        .texOffs(0, 75).addBox(-4.5f, 0.0f, -0.5f, 5.0f, 12.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        // footRightJoint at (-2, 11, 2), rotY=-0.7854
        PartDefinition footRightJoint = calfRight.addOrReplaceChild("footRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.0f, 11.0f, 2.0f, 0.0f, -0.7854f, 0.0f));

        PartDefinition footRight = footRightJoint.addOrReplaceChild("footRight", CubeListBuilder.create()
                        .texOffs(48, 79).addBox(-3.0f, 0.0f, -8.0f, 6.0f, 3.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.6981f, 0.0f, 0.0f));

        // Left leg (mirrored)
        PartDefinition thighLeftJoint = groin.addOrReplaceChild("thighLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        PartDefinition thighLeftJoint2 = thighLeftJoint.addOrReplaceChild("thighLeftJoint2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.8727f, 0.0f, 0.0f));

        PartDefinition thighLeft = thighLeftJoint2.addOrReplaceChild("thighLeft", CubeListBuilder.create()
                        .texOffs(26, 90).addBox(-3.5f, 0.0f, -3.5f, 7.0f, 13.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        thighLeft.addOrReplaceChild("kneeLeft", CubeListBuilder.create()
                        .texOffs(24, 80).addBox(-3.0f, -1.7f, -3.0f, 6.0f, 4.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 13.0f, 0.0f, -0.5236f, -0.7854f, 0.0f));

        PartDefinition calfLeftJoint = thighLeft.addOrReplaceChild("calfLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 14.5f, 0.0f, 1.5708f, -0.7854f, 0.0f));

        PartDefinition calfLeft = calfLeftJoint.addOrReplaceChild("calfLeft", CubeListBuilder.create()
                        .texOffs(0, 75).addBox(-4.5f, 0.0f, -0.5f, 5.0f, 12.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.7854f, 0.0f));

        PartDefinition footLeftJoint = calfLeft.addOrReplaceChild("footLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.0f, 11.0f, 2.0f, 0.0f, -0.7854f, 0.0f));

        PartDefinition footLeft = footLeftJoint.addOrReplaceChild("footLeft", CubeListBuilder.create()
                        .texOffs(48, 79).addBox(-3.0f, 0.0f, -8.0f, 6.0f, 3.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.6981f, 0.0f, 0.0f));

        return LayerDefinition.create(meshdefinition, 128, 128);
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
