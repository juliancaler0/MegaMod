/*
 * FrostmawModel - Converted from MowziesMobs ModelFrostmaw (LLibrary AdvancedModelRenderer)
 * to NeoForge 1.21.11 EntityModel format. Simplified but faithful to the original ice titan shape.
 * Original by BobMowzie, adapted for MegaMod.
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.FrostmawAnimations;
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

public class FrostmawModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("megamod", "frostmaw"), "main");
    public final ModelPart root;
    public final ModelPart waist;
    public final ModelPart chest;
    public final ModelPart headJoint;
    public final ModelPart head;
    public final ModelPart jaw;
    public final ModelPart armLeftJoint;
    public final ModelPart armLeft1;
    public final ModelPart armLeft2;
    public final ModelPart leftHand;
    public final ModelPart armRightJoint;
    public final ModelPart armRight1;
    public final ModelPart armRight2;
    public final ModelPart rightHand;
    public final ModelPart legLeftJoint;
    public final ModelPart legLeft1;
    public final ModelPart legLeft2;
    public final ModelPart leftFoot;
    public final ModelPart legRightJoint;
    public final ModelPart legRight1;
    public final ModelPart legRight2;
    public final ModelPart rightFoot;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public FrostmawModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.waist = this.root.getChild("waist");
        this.chest = this.waist.getChild("chest");
        this.headJoint = this.chest.getChild("headJoint");
        ModelPart headRotator = this.headJoint.getChild("headRotator");
        this.head = headRotator.getChild("head");
        ModelPart jawJoint = this.head.getChild("jawJoint");
        ModelPart jawRotator = jawJoint.getChild("jawRotator");
        this.jaw = jawRotator.getChild("jaw");
        this.armLeftJoint = this.chest.getChild("armLeftJoint");
        this.armLeft1 = this.armLeftJoint.getChild("armLeft1");
        ModelPart armLeftJoint2 = this.armLeft1.getChild("armLeftJoint2");
        this.armLeft2 = armLeftJoint2.getChild("armLeft2");
        ModelPart leftHandJoint = this.armLeft2.getChild("leftHandJoint");
        this.leftHand = leftHandJoint.getChild("leftHand");
        this.armRightJoint = this.chest.getChild("armRightJoint");
        this.armRight1 = this.armRightJoint.getChild("armRight1");
        ModelPart armRightJoint2 = this.armRight1.getChild("armRightJoint2");
        this.armRight2 = armRightJoint2.getChild("armRight2");
        ModelPart rightHandJoint = this.armRight2.getChild("rightHandJoint");
        this.rightHand = rightHandJoint.getChild("rightHand");
        this.legLeftJoint = this.waist.getChild("legLeftJoint");
        this.legLeft1 = this.legLeftJoint.getChild("legLeft1");
        this.legLeft2 = this.legLeft1.getChild("legLeft2");
        this.leftFoot = this.legLeft2.getChild("leftFoot");
        this.legRightJoint = this.waist.getChild("legRightJoint");
        this.legRight1 = this.legRightJoint.getChild("legRight1");
        this.legRight2 = this.legRight1.getChild("legRight2");
        this.rightFoot = this.legRight2.getChild("rightFoot");
        this.idleAnimation = FrostmawAnimations.idle.bake(root);
        this.walkAnimation = FrostmawAnimations.walk.bake(root);
        this.attackAnimation = FrostmawAnimations.attack.bake(root);
        this.deathAnimation = FrostmawAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Root positioned at ground level
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0f, 24.0f, 10.0f));

        // Waist - main body trunk, tilted forward (large yeti torso)
        // Original: setRotationPoint(0, -30, 5), addBox(-11.5, -23, -8.5, 23, 30, 17), rotX=0.6981
        PartDefinition waist = root.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-11.5f, -23.0f, -8.5f, 23.0f, 30.0f, 17.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -30.0f, 5.0f, 0.6981f, 0.0f, 0.0f));

        // Chest - massive upper body (60x37x40), connected via chestJoint
        // chestJoint at (0, -24.96, -1), rotX=-0.7854 (45 deg back to counter waist lean)
        // chest at (0,0,0) inside chestJoint, addBox(-30, -25, -30, 60, 37, 40)
        PartDefinition chest = waist.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-30.0f, -25.0f, -30.0f, 60.0f, 37.0f, 40.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -24.96f, -1.0f, -0.7854f, 0.0f, 0.0f));

        // Back hair/mane
        chest.addOrReplaceChild("backHair", CubeListBuilder.create()
                        .texOffs(374, 180).addBox(-9.5f, -8.244f, -4.873f, 18.0f, 25.0f, 51.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.5f, -24.537f, -33.343f, -0.1309f, 0.0f, 0.0f));

        // Head joint - connects head to chest
        // headJoint at (0, -10, -30), rotX=0.3491
        PartDefinition headJoint = chest.addOrReplaceChild("headJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -10.0f, -30.0f, 0.3491f, 0.0f, 0.0f));

        // Head hair
        headJoint.addOrReplaceChild("headHair", CubeListBuilder.create()
                        .texOffs(266, 192).addBox(-9.0f, -9.0f, -1.0f, 18.0f, 23.0f, 36.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -3.0f, -24.0f, 0.2182f, 0.0f, 0.0f));

        // Head rotator at (0, -6, -16), rotated 45 deg (diamond shape)
        // head inside: addBox(-2, -5, -30, 32, 17, 32) at (-13, 5.18, 13)
        PartDefinition headRotator = headJoint.addOrReplaceChild("headRotator", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -6.0f, -16.0f, 0.0f, 0.7854f, 0.0f));

        PartDefinition head = headRotator.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(280, 0).addBox(-2.0f, -5.0f, -30.0f, 32.0f, 17.0f, 32.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-13.0f, 5.18f, 13.0f));

        // Head back plate
        head.addOrReplaceChild("headBack", CubeListBuilder.create()
                        .texOffs(0, 139).addBox(-16.0f, 0.0f, -16.0f, 32.0f, 6.0f, 32.0f, new CubeDeformation(0.0f)),
                PartPose.offset(14.0f, 12.0f, -14.0f));

        // Upper teeth
        head.addOrReplaceChild("teethUpper", CubeListBuilder.create()
                        .texOffs(376, 0).addBox(-13.0f, 0.0f, -13.0f, 26.0f, 6.0f, 26.0f, new CubeDeformation(0.0f)),
                PartPose.offset(14.0f, 12.0f, -14.0f));

        // Jaw joint → jaw rotator → jaw (opens/closes)
        // jawJoint at (6.41, 11, -6.41), rotX=-0.1745, rotY=-0.7854
        PartDefinition jawJoint = head.addOrReplaceChild("jawJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(6.41f, 11.0f, -6.41f, -0.1745f, -0.7854f, 0.0f));

        PartDefinition jawRotator = jawJoint.addOrReplaceChild("jawRotator", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 7.354f, -12.249f, 0.0f, 0.7854f, 0.0f));

        PartDefinition jaw = jawRotator.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(242, 52).addBox(-19.0f, 0.0f, -19.0f, 38.0f, 14.0f, 38.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -5.0f, 0.0f));

        // Jaw spikes (two layers of increasingly large spikes)
        jaw.addOrReplaceChild("jawSpikes1", CubeListBuilder.create()
                        .texOffs(380, 48).addBox(-14.0f, 0.0f, -14.0f, 28.0f, 14.0f, 28.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 14.0f, 0.0f));

        jaw.addOrReplaceChild("jawSpikes2", CubeListBuilder.create()
                        .texOffs(212, 104).addBox(-19.0f, 0.0f, -19.0f, 38.0f, 9.0f, 38.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 14.0f, 0.0f));

        // Lower teeth
        jaw.addOrReplaceChild("teethLower", CubeListBuilder.create()
                        .texOffs(383, 120).addBox(-14.0f, -6.0f, -17.0f, 31.0f, 6.0f, 31.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Tusks (left and right, on jaw)
        PartDefinition tuskRightJoint = jaw.addOrReplaceChild("tuskRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-12.0f, 0.0f, -19.0f, 0.0f, 1.5708f, 0.0f));

        PartDefinition tuskRight1 = tuskRightJoint.addOrReplaceChild("tuskRight1", CubeListBuilder.create()
                        .texOffs(68, 109).addBox(-3.0f, -8.0f, -3.0f, 6.0f, 13.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0944f));

        tuskRight1.addOrReplaceChild("tuskRight2", CubeListBuilder.create()
                        .texOffs(0, 80).addBox(-10.0f, -2.0f, -2.0f, 11.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, -0.8727f, 1.0908f, 0.5236f));

        PartDefinition tuskLeftJoint = jaw.addOrReplaceChild("tuskLeftJoint", CubeListBuilder.create(),
                PartPose.offset(19.0f, 0.0f, 12.0f));

        PartDefinition tuskLeft1 = tuskLeftJoint.addOrReplaceChild("tuskLeft1", CubeListBuilder.create()
                        .texOffs(68, 109).addBox(-3.0f, -8.0f, -3.0f, 6.0f, 13.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0944f));

        tuskLeft1.addOrReplaceChild("tuskLeft2", CubeListBuilder.create()
                        .texOffs(0, 80).addBox(-10.0f, -2.0f, -2.0f, 11.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.8727f, -1.0908f, 0.5236f));

        // Ears
        headJoint.addOrReplaceChild("earL", CubeListBuilder.create()
                        .texOffs(396, 102).addBox(-23.0f, -2.0f, -1.0f, 23.0f, 17.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-19.0f, 0.0f, -14.0f, -0.3831f, 0.4174f, -0.3721f));

        headJoint.addOrReplaceChild("earR", CubeListBuilder.create()
                        .texOffs(396, 102).mirror().addBox(0.0f, -2.0f, -1.0f, 23.0f, 17.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(19.0f, 0.0f, -14.0f, -0.3831f, -0.4174f, 0.3721f));

        // Horns - Right side chain: hornR1 → hornR2 → hornR3/hornR6
        PartDefinition hornR1 = headJoint.addOrReplaceChild("hornR1", CubeListBuilder.create()
                        .texOffs(72, 177).addBox(-7.5f, -21.0f, -9.5f, 17.0f, 23.0f, 17.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(8.5f, 0.0f, -10.5f, -0.2327f, 0.7396f, 0.5849f));

        PartDefinition hornR2 = hornR1.addOrReplaceChild("hornR2", CubeListBuilder.create()
                        .texOffs(140, 169).addBox(-4.5f, -20.0f, -9.5f, 14.0f, 22.0f, 14.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -14.0f, 2.0f, -0.3286f, -0.0089f, -0.665f));

        PartDefinition hornR3 = hornR2.addOrReplaceChild("hornR3", CubeListBuilder.create()
                        .texOffs(0, 226).addBox(-0.5f, -20.0f, -9.5f, 10.0f, 20.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-3.0f, -14.0f, 2.0f, -0.3356f, -0.0462f, -0.1705f));

        hornR3.addOrReplaceChild("hornR4", CubeListBuilder.create()
                        .texOffs(455, 0).addBox(-4.626f, -19.39f, -0.601f, 6.0f, 18.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(6.0f, -15.0f, -5.0f, 0.4363f, -0.3927f, 0.2618f));

        hornR3.addOrReplaceChild("hornR5", CubeListBuilder.create()
                        .texOffs(405, 193).addBox(-4.626f, -12.39f, -0.601f, 5.0f, 17.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(4.0f, -9.0f, -9.0f, 0.8134f, 0.4571f, -0.1047f));

        PartDefinition hornR6 = hornR2.addOrReplaceChild("hornR6", CubeListBuilder.create()
                        .texOffs(0, 226).addBox(-0.5f, -16.0f, -9.5f, 10.0f, 19.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.2003f, 0.5395f, 3.3145f, -0.397f, 0.3402f, 0.428f));

        hornR6.addOrReplaceChild("hornR7", CubeListBuilder.create()
                        .texOffs(455, 0).addBox(-4.626f, -15.39f, -1.601f, 6.0f, 18.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(6.0f, -13.0f, -5.0f, 0.546f, 0.0303f, 0.2416f));

        // Horns - Left side chain (mirrored)
        PartDefinition hornL1 = headJoint.addOrReplaceChild("hornL1", CubeListBuilder.create()
                        .texOffs(72, 177).mirror().addBox(-9.5f, -21.0f, -9.5f, 17.0f, 23.0f, 17.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-8.5f, 0.0f, -10.5f, -0.2327f, -0.7396f, -0.5849f));

        PartDefinition hornL2 = hornL1.addOrReplaceChild("hornL2", CubeListBuilder.create()
                        .texOffs(140, 169).mirror().addBox(-9.5f, -20.0f, -9.5f, 14.0f, 22.0f, 14.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, -14.0f, 2.0f, -0.3286f, 0.0089f, 0.665f));

        PartDefinition hornL3 = hornL2.addOrReplaceChild("hornL3", CubeListBuilder.create()
                        .texOffs(0, 226).mirror().addBox(-9.5f, -20.0f, -9.5f, 10.0f, 20.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(3.0f, -14.0f, 2.0f, -0.3356f, 0.0462f, 0.1705f));

        hornL3.addOrReplaceChild("hornL4", CubeListBuilder.create()
                        .texOffs(455, 0).mirror().addBox(-1.374f, -19.39f, -0.601f, 6.0f, 18.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-6.0f, -15.0f, -5.0f, 0.4363f, 0.3927f, -0.2618f));

        hornL3.addOrReplaceChild("hornL5", CubeListBuilder.create()
                        .texOffs(405, 193).mirror().addBox(-0.374f, -12.39f, -0.601f, 5.0f, 17.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-4.0f, -9.0f, -9.0f, 0.8134f, -0.4571f, 0.1047f));

        PartDefinition hornL6 = hornL2.addOrReplaceChild("hornL6", CubeListBuilder.create()
                        .texOffs(0, 226).mirror().addBox(-9.5f, -16.0f, -9.5f, 10.0f, 19.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-0.2003f, 0.5395f, 3.3145f, -0.397f, -0.3402f, -0.428f));

        hornL6.addOrReplaceChild("hornL7", CubeListBuilder.create()
                        .texOffs(455, 0).mirror().addBox(-1.374f, -15.39f, -1.601f, 6.0f, 18.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-6.0f, -13.0f, -5.0f, 0.546f, -0.0303f, -0.2416f));

        // Left arm chain: armLeftJoint → armLeft1 (17x34x17) → armLeft2 (22x38x22) → leftHand (20x20x9)
        PartDefinition armLeftJoint = chest.addOrReplaceChild("armLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(28.0f, 0.0f, -15.0f, 0.0873f, 0.0f, 0.0f));

        PartDefinition armLeft1 = armLeftJoint.addOrReplaceChild("armLeft1", CubeListBuilder.create()
                        .texOffs(0, 88).mirror().addBox(-8.5f, -10.0f, -8.5f, 17.0f, 34.0f, 17.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.2276f, 0.0f, -0.7285f));

        // armLeftJoint2 at (0, 24, 0)
        PartDefinition armLeftJoint2 = armLeft1.addOrReplaceChild("armLeftJoint2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, -0.171f, 0.1508f, 0.7156f));

        PartDefinition armLeft2 = armLeftJoint2.addOrReplaceChild("armLeft2", CubeListBuilder.create()
                        .texOffs(112, 109).mirror().addBox(-11.0f, -15.0f, -11.0f, 22.0f, 38.0f, 22.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.8727f, 0.4363f, -0.0873f));

        // Arm fur
        armLeft2.addOrReplaceChild("armLeft2Fur", CubeListBuilder.create()
                        .texOffs(326, 113).addBox(-11.0f, -5.0f, -11.0f, 22.0f, 7.0f, 22.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 28.0f, 0.0f));

        // Fur clumps on left arm
        armLeft2.addOrReplaceChild("armLeft2FurClump1", CubeListBuilder.create()
                        .texOffs(0, 179).addBox(-18.0f, -26.0f, -18.0f, 18.0f, 26.0f, 18.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(10.0f, 23.96f, 10.0f, -0.1571f, 0.0f, 0.2269f));

        armLeft2.addOrReplaceChild("armLeft2FurClump2", CubeListBuilder.create()
                        .texOffs(40, 223).addBox(0.0f, -16.0f, -18.0f, 18.0f, 15.0f, 18.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-10.0f, 23.96f, 10.0f, -0.3054f, -0.0436f, -0.2531f));

        // Left hand at (0, 20, 0) via leftHandJoint
        PartDefinition leftHandJoint = armLeft2.addOrReplaceChild("leftHandJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 20.0f, 0.0f, 0.8727f, -0.3491f, -0.2618f));

        PartDefinition leftHand = leftHandJoint.addOrReplaceChild("leftHand", CubeListBuilder.create()
                        .texOffs(240, 0).mirror().addBox(-10.0f, -2.0f, -7.5f, 20.0f, 20.0f, 9.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-2.0f, 1.0f, -4.5f, 0.0f, 0.3491f, 0.0f));

        // Left fingers
        PartDefinition leftFingersJoint = leftHand.addOrReplaceChild("leftFingersJoint", CubeListBuilder.create()
                        .texOffs(0, 47).mirror().addBox(-10.0f, -2.5f, 0.0f, 20.0f, 5.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 15.5f, -3.0f));

        leftFingersJoint.addOrReplaceChild("leftFingers", CubeListBuilder.create()
                        .texOffs(0, 62).mirror().addBox(-10.0f, -7.5f, -2.5f, 20.0f, 10.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 0.0f, 7.0f));

        // Left thumb
        leftHand.addOrReplaceChild("leftThumb", CubeListBuilder.create()
                        .texOffs(63, 0).addBox(-12.0f, -2.5f, -2.5f, 12.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-10.0f, 0.5f, 4.0f, 0.0f, 3.1416f, 0.0f));

        // Right arm chain (mirrored)
        PartDefinition armRightJoint = chest.addOrReplaceChild("armRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-28.0f, 0.0f, -15.0f, 0.0873f, 0.0f, 0.0f));

        PartDefinition armRight1 = armRightJoint.addOrReplaceChild("armRight1", CubeListBuilder.create()
                        .texOffs(0, 88).addBox(-8.5f, -10.0f, -8.5f, 17.0f, 34.0f, 17.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.2276f, 0.0f, 0.7285f));

        PartDefinition armRightJoint2 = armRight1.addOrReplaceChild("armRightJoint2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, -0.171f, -0.1508f, -0.7156f));

        PartDefinition armRight2 = armRightJoint2.addOrReplaceChild("armRight2", CubeListBuilder.create()
                        .texOffs(112, 109).addBox(-11.0f, -15.0f, -11.0f, 22.0f, 38.0f, 22.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.8727f, -0.4363f, 0.0873f));

        armRight2.addOrReplaceChild("armRight2Fur", CubeListBuilder.create()
                        .texOffs(326, 113).addBox(-11.0f, -5.0f, -11.0f, 22.0f, 7.0f, 22.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 28.0f, 0.0f));

        // Fur clumps on right arm
        armRight2.addOrReplaceChild("armRight2FurClump1", CubeListBuilder.create()
                        .texOffs(0, 179).mirror().addBox(0.0f, -26.0f, -18.0f, 18.0f, 26.0f, 18.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-10.0f, 23.96f, 10.0f, -0.1571f, 0.0f, -0.2269f));

        armRight2.addOrReplaceChild("armRight2FurClump2", CubeListBuilder.create()
                        .texOffs(40, 223).mirror().addBox(-18.0f, -16.0f, -18.0f, 18.0f, 15.0f, 18.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(10.0f, 23.96f, 10.0f, -0.3054f, 0.0436f, 0.2531f));

        PartDefinition rightHandJoint = armRight2.addOrReplaceChild("rightHandJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 20.0f, 0.0f, 0.8727f, 0.3491f, 0.2618f));

        PartDefinition rightHand = rightHandJoint.addOrReplaceChild("rightHand", CubeListBuilder.create()
                        .texOffs(240, 0).addBox(-10.0f, -2.0f, -7.52f, 20.0f, 20.0f, 9.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(2.0f, 1.0f, -4.5f, 0.0f, -0.3491f, 0.0f));

        PartDefinition rightFingersJoint = rightHand.addOrReplaceChild("rightFingersJoint", CubeListBuilder.create()
                        .texOffs(0, 47).addBox(-10.0f, -2.5f, 0.0f, 20.0f, 5.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 15.5f, -3.0f));

        rightFingersJoint.addOrReplaceChild("rightFingers", CubeListBuilder.create()
                        .texOffs(0, 62).addBox(-10.0f, -7.5f, -2.5f, 20.0f, 10.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, 7.0f));

        rightHand.addOrReplaceChild("rightThumb", CubeListBuilder.create()
                        .texOffs(63, 0).mirror().addBox(0.0f, -2.5f, -2.5f, 12.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(10.0f, 0.5f, 4.0f, 0.0f, 3.1416f, 0.0f));

        // Left leg: legLeftJoint → legLeft1 (9x11x21) → legLeft2 (14x14x16) → leftFoot (13x20x6)
        PartDefinition legLeftJoint = waist.addOrReplaceChild("legLeftJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(9.0f, 3.14f, 0.0f, -0.6981f, 0.0f, 0.0f));

        PartDefinition legLeft1 = legLeftJoint.addOrReplaceChild("legLeft1", CubeListBuilder.create()
                        .texOffs(37, 56).addBox(-4.5f, -4.5f, -17.0f, 9.0f, 11.0f, 21.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.7854f, -0.6981f, 0.0f));

        PartDefinition legLeft2 = legLeft1.addOrReplaceChild("legLeft2", CubeListBuilder.create()
                        .texOffs(81, 77).addBox(-7.0f, -6.0f, -12.0f, 14.0f, 14.0f, 16.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, -15.0f, 1.2217f, 0.0f, 0.0f));

        legLeft2.addOrReplaceChild("legLeftFur", CubeListBuilder.create()
                        .texOffs(144, 77).addBox(-7.0f, -6.0f, -12.0f, 14.0f, 14.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 0.0f, -3.0f));

        PartDefinition leftFoot = legLeft2.addOrReplaceChild("leftFoot", CubeListBuilder.create()
                        .texOffs(80, 12).mirror().addBox(-6.5f, -14.75f, -6.3f, 13.0f, 20.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.2f, -12.0f, -0.4363f, 0.0f, 0.0f));

        // Right leg (mirrored)
        PartDefinition legRightJoint = waist.addOrReplaceChild("legRightJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-9.0f, 3.14f, 0.0f, -0.6981f, 0.0f, 0.0f));

        PartDefinition legRight1 = legRightJoint.addOrReplaceChild("legRight1", CubeListBuilder.create()
                        .texOffs(37, 56).mirror().addBox(-4.5f, -4.5f, -17.0f, 9.0f, 11.0f, 21.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.7854f, 0.6981f, 0.0f));

        PartDefinition legRight2 = legRight1.addOrReplaceChild("legRight2", CubeListBuilder.create()
                        .texOffs(81, 77).mirror().addBox(-7.0f, -6.0f, -12.0f, 14.0f, 14.0f, 16.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, -15.0f, 1.2217f, 0.0f, 0.0f));

        legRight2.addOrReplaceChild("legRightFur", CubeListBuilder.create()
                        .texOffs(144, 77).mirror().addBox(-7.0f, -6.0f, -12.0f, 14.0f, 14.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 0.0f, -3.0f));

        PartDefinition rightFoot = legRight2.addOrReplaceChild("rightFoot", CubeListBuilder.create()
                        .texOffs(80, 12).addBox(-6.5f, -14.75f, -6.3f, 13.0f, 20.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.2f, -12.0f, -0.4363f, 0.0f, 0.0f));

        return LayerDefinition.create(meshdefinition, 512, 256);
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
