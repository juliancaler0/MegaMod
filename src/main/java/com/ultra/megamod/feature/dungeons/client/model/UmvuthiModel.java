/*
 * UmvuthiModel - Converted from MowziesMobs GeckoLib geo/umvuthi.geo.json
 * to NeoForge 1.21.11 EntityModel format (vanilla ModelPart/LayerDefinition).
 * 118-bone model with 256x128 texture. Fat avian mask lord with detailed feathers,
 * tribal mask with halo and pinnacles, bird legs, tail fan, arm rings.
 * Original by BobMowzie, adapted for MegaMod.
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.UmvuthiAnimations;
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

public class UmvuthiModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("megamod", "umvuthi"), "main");
    public final ModelPart body;
    public final ModelPart chest;
    public final ModelPart stomach;
    public final ModelPart neck;
    public final ModelPart neck2;
    public final ModelPart headJoint;
    public final ModelPart head;
    public final ModelPart mask;
    public final ModelPart leftArmJoint;
    public final ModelPart leftUpperArm;
    public final ModelPart leftLowerArm;
    public final ModelPart leftHand;
    public final ModelPart rightArmJoint;
    public final ModelPart rightUpperArm;
    public final ModelPart rightLowerArm;
    public final ModelPart rightHand;
    public final ModelPart leftThigh;
    public final ModelPart leftCalf;
    public final ModelPart leftAnkle;
    public final ModelPart leftFoot;
    public final ModelPart rightThigh;
    public final ModelPart rightCalf;
    public final ModelPart rightAnkle;
    public final ModelPart rightFoot;
    public final ModelPart tail;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public UmvuthiModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        ModelPart neckJoint = this.chest.getChild("neckJoint");
        this.neck = neckJoint.getChild("neck");
        ModelPart neck2Joint = this.neck.getChild("neck2Joint");
        this.neck2 = neck2Joint.getChild("neck2");
        this.headJoint = this.neck2.getChild("headJoint");
        ModelPart headRotator = this.headJoint.getChild("headRotator");
        this.head = headRotator.getChild("head");
        ModelPart maskTwitcher = this.head.getChild("maskTwitcher");
        this.mask = maskTwitcher.getChild("mask");
        this.leftArmJoint = this.chest.getChild("leftArmJoint");
        this.leftUpperArm = this.leftArmJoint.getChild("leftUpperArm");
        this.leftLowerArm = this.leftUpperArm.getChild("leftLowerArm");
        this.leftHand = this.leftLowerArm.getChild("leftHand");
        this.rightArmJoint = this.chest.getChild("rightArmJoint");
        this.rightUpperArm = this.rightArmJoint.getChild("rightUpperArm");
        this.rightLowerArm = this.rightUpperArm.getChild("rightLowerArm");
        this.rightHand = this.rightLowerArm.getChild("rightHand");
        this.stomach = this.body.getChild("stomach");
        ModelPart leftThighJoint = this.body.getChild("leftThighJoint");
        this.leftThigh = leftThighJoint.getChild("leftThigh");
        this.leftCalf = this.leftThigh.getChild("leftCalf");
        this.leftAnkle = this.leftCalf.getChild("leftAnkle");
        this.leftFoot = this.leftAnkle.getChild("leftFoot");
        ModelPart rightThighJoint = this.body.getChild("rightThighJoint");
        this.rightThigh = rightThighJoint.getChild("rightThigh");
        this.rightCalf = this.rightThigh.getChild("rightCalf");
        this.rightAnkle = this.rightCalf.getChild("rightAnkle");
        this.rightFoot = this.rightAnkle.getChild("rightFoot");
        this.tail = this.body.getChild("tail");
        this.idleAnimation = UmvuthiAnimations.idle.bake(root);
        this.walkAnimation = UmvuthiAnimations.walk.bake(root);
        this.attackAnimation = UmvuthiAnimations.attack.bake(root);
        this.deathAnimation = UmvuthiAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Body - main root pivot at ground level
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0.0f, 24.0f, -3.0f));

        // Chest - upper torso, tilted forward
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-9.5f, -7.67063f, -8.69333f, 19.0f, 10.0f, 16.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -16.0f, 2.0f, 0.2618f, 0.0f, 0.0f));

        // Neck joint - counter-rotates chest tilt
        PartDefinition neckJoint = chest.addOrReplaceChild("neckJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -5.67063f, 1.30667f, -0.2618f, 0.0f, 0.0f));

        // Neck - thick avian neck with feather collar overlay
        PartDefinition neck = neckJoint.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(0, 77).addBox(-5.0f, -11.5f, -7.0f, 10.0f, 14.0f, 10.0f, new CubeDeformation(0.0f))
                        .texOffs(58, 66).addBox(-5.0f, -10.5f, -7.0f, 10.0f, 13.0f, 10.0f, new CubeDeformation(0.25f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.04363f, 0.0f, 0.0f));

        // Neck feathers (4 directions on lower neck)
        neck.addOrReplaceChild("neckFeathersFront4", CubeListBuilder.create()
                        .texOffs(150, 0).addBox(-5.0f, 0.0f, -4.0f, 10.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -11.5f, -7.0f, -1.13446f, 0.0f, 0.0f));

        neck.addOrReplaceChild("neckFeathersRight4", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, 0.0f, -5.5f, 4.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-5.0f, -11.5f, -1.5f, 0.0f, 0.0f, -0.95993f));

        neck.addOrReplaceChild("neckFeathersLeft4", CubeListBuilder.create()
                        .texOffs(0, 0).mirror().addBox(0.0f, 0.0f, -5.5f, 4.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(5.0f, -11.5f, -1.5f, 0.0f, 0.0f, 0.95993f));

        neck.addOrReplaceChild("neckFeathersBack4", CubeListBuilder.create()
                        .texOffs(94, 85).addBox(-5.0f, 0.0f, 0.0f, 10.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -11.5f, 3.0f, 1.13446f, 0.0f, 0.0f));

        // Neck2 joint
        PartDefinition neck2Joint = neck.addOrReplaceChild("neck2Joint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -11.00095f, -1.45638f, -0.04363f, 0.0f, 0.0f));

        // Neck2 - upper neck segment
        PartDefinition neck2 = neck2Joint.addOrReplaceChild("neck2", CubeListBuilder.create()
                        .texOffs(76, 96).addBox(-4.0f, -9.42388f, -3.61732f, 8.0f, 11.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.3927f, 0.0f, 0.0f));

        // Head joint
        PartDefinition headJoint = neck2.addOrReplaceChild("headJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -9.42388f, -0.11732f, 0.34907f, 0.0f, 0.0f));

        // Head rotator
        PartDefinition headRotator = headJoint.addOrReplaceChild("headRotator", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -6.5f, -4.5f, 0.04363f, 0.0f, 0.0f));

        // Head
        PartDefinition head = headRotator.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.04363f, 0.0f, 0.0f));

        // Halo joint - angled crown/sun disc behind head
        PartDefinition haloJoint = head.addOrReplaceChild("haloJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -0.17937f, -6.80667f, -0.5236f, 0.0f, 0.0f));

        // Halo - large decorative sun disc
        PartDefinition halo = haloJoint.addOrReplaceChild("halo", CubeListBuilder.create()
                        .texOffs(136, 28).mirror().addBox(11.5f, -16.9f, 4.0f, 8.0f, 26.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false)
                        .texOffs(67, 89).addBox(-11.5f, -20.9f, 4.0f, 23.0f, 7.0f, 0.0f, new CubeDeformation(0.0f))
                        .texOffs(136, 28).addBox(-19.5f, -16.9f, 4.0f, 8.0f, 26.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.ZERO);

        // Pinnacles - decorative spikes on halo
        PartDefinition pinnacles = halo.addOrReplaceChild("pinnacles", CubeListBuilder.create(),
                PartPose.offset(0.0f, 0.0f, -0.03125f));

        PartDefinition pinnaclesRight = pinnacles.addOrReplaceChild("pinnaclesRight", CubeListBuilder.create(),
                PartPose.ZERO);

        pinnaclesRight.addOrReplaceChild("pinnacleTopRight", CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-8.5f, -22.9f, 4.0f, 3.0f, 8.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.ZERO);

        pinnaclesRight.addOrReplaceChild("pinnacleMidRight", CubeListBuilder.create()
                        .texOffs(118, 85).addBox(-8.5f, -18.9f, 4.0f, 8.0f, 4.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-12.0f, 8.0f, 0.0f));

        pinnaclesRight.addOrReplaceChild("pinnacleBottomRight", CubeListBuilder.create()
                        .texOffs(126, 22).addBox(-16.5f, -12.9f, 4.0f, 4.0f, 6.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        PartDefinition pinnaclesLeft = pinnacles.addOrReplaceChild("pinnaclesLeft", CubeListBuilder.create(),
                PartPose.ZERO);

        pinnaclesLeft.addOrReplaceChild("pinnacleTopLeft", CubeListBuilder.create()
                        .texOffs(0, 8).mirror().addBox(5.5f, -22.9f, 4.0f, 3.0f, 8.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.ZERO);

        pinnaclesLeft.addOrReplaceChild("pinnacleMidLeft", CubeListBuilder.create()
                        .texOffs(118, 85).mirror().addBox(0.5f, -18.9f, 4.0f, 8.0f, 4.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(12.0f, 8.0f, 0.0f));

        pinnaclesLeft.addOrReplaceChild("pinnacleBottomLeft", CubeListBuilder.create()
                        .texOffs(126, 22).mirror().addBox(12.5f, -12.9f, 4.0f, 4.0f, 6.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        // Mask twitcher - allows mask to twitch independently
        PartDefinition maskTwitcher = head.addOrReplaceChild("maskTwitcher", CubeListBuilder.create(),
                PartPose.offset(0.0f, 1.0f, -8.5f));

        // Mask - main tribal mask
        PartDefinition mask = maskTwitcher.addOrReplaceChild("mask", CubeListBuilder.create(),
                PartPose.offset(0.0f, -1.0f, 0.75f));

        // Mask jaw
        PartDefinition maskJaw = mask.addOrReplaceChild("maskJaw", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, 0.91945f, 2.95682f, -0.21817f, 0.0f, 0.0f));

        // Lower beak on mask jaw
        maskJaw.addOrReplaceChild("beak2", CubeListBuilder.create()
                        .texOffs(124, 45).addBox(-0.99859f, 0.86434f, -0.82824f, 3.0f, 1.0f, 3.0f, new CubeDeformation(0.0f))
                        .texOffs(104, 0).addBox(-0.99859f, 0.86434f, -0.82824f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.90766f, -7.338f, 0.61087f, 0.0f, 0.0f));

        // Mask head - upper portion of tribal mask
        PartDefinition maskHead = mask.addOrReplaceChild("maskHead", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0f, -1.08055f, 4.95682f, -0.21817f, 0.0f, 0.0f));

        // Left side of mask
        PartDefinition leftSide = maskHead.addOrReplaceChild("leftSide", CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.50803f, -0.41009f, -6.51568f, 0.0f, 0.08727f, 0.0f));

        PartDefinition leftCheek = leftSide.addOrReplaceChild("leftCheek", CubeListBuilder.create()
                        .texOffs(132, 69).mirror().addBox(-4.4619f, 0.05269f, -2.92903f, 7.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false)
                        .texOffs(58, 71).mirror().addBox(-1.4619f, 0.05269f, -1.92903f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false)
                        .texOffs(152, 47).mirror().addBox(2.5381f, 0.05269f, -2.92903f, 3.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.74176f, 0.2618f, -0.20944f));

        PartDefinition leftEyelidJoint = leftCheek.addOrReplaceChild("leftEyelidJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.04112f, 0.06036f, 0.06781f, 0.89012f, 0.0f, 0.0f));

        leftEyelidJoint.addOrReplaceChild("leftEyelid", CubeListBuilder.create()
                        .texOffs(120, 69).mirror().addBox(-1.5f, -1.0f, -1.5f, 3.0f, 2.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(-4.5f, -1.0f, 0.5f));

        // Right side of mask (mirrored)
        PartDefinition rightSide = maskHead.addOrReplaceChild("rightSide", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-3.50803f, -0.41009f, -6.51568f, 0.0f, -0.08727f, 0.0f));

        PartDefinition rightCheek = rightSide.addOrReplaceChild("rightCheek", CubeListBuilder.create()
                        .texOffs(132, 69).addBox(-2.5381f, 0.05269f, -2.92903f, 7.0f, 3.0f, 3.0f, new CubeDeformation(0.0f))
                        .texOffs(58, 71).addBox(-0.5381f, 0.05269f, -1.92903f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f))
                        .texOffs(152, 47).addBox(-5.5381f, 0.05269f, -2.92903f, 3.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.74176f, -0.2618f, 0.20944f));

        PartDefinition rightEyelidJoint = rightCheek.addOrReplaceChild("rightEyelidJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.04112f, 0.06036f, 0.06781f, 0.89012f, 0.0f, 0.0f));

        rightEyelidJoint.addOrReplaceChild("rightEyelid", CubeListBuilder.create()
                        .texOffs(120, 69).addBox(-1.5f, -1.0f, -1.5f, 3.0f, 2.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offset(4.5f, -1.0f, 0.5f));

        // Mask center - central face plate with eyebrow ridges
        PartDefinition maskCenter = maskHead.addOrReplaceChild("maskCenter", CubeListBuilder.create()
                        .texOffs(136, 87).addBox(-3.5f, -2.08579f, -2.0f, 5.0f, 5.0f, 4.0f, new CubeDeformation(0.0f))
                        .texOffs(0, 64).addBox(-14.5f, -12.82769f, 1.34557f, 29.0f, 13.0f, 0.0f, new CubeDeformation(0.0f))
                        .texOffs(120, 64).addBox(-5.3466f, -0.96612f, 0.30837f, 7.0f, 3.0f, 2.0f, new CubeDeformation(0.0f))
                        .texOffs(120, 64).mirror().addBox(-1.6534f, -0.96612f, 0.30837f, 7.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, -2.41421f, -5.97f, 0.21817f, 0.0f, 0.0f));

        // Upper beak on mask
        maskCenter.addOrReplaceChild("beak", CubeListBuilder.create()
                        .texOffs(152, 37).addBox(-1.0f, -6.15f, -0.80834f, 3.0f, 7.0f, 3.0f, new CubeDeformation(0.0f))
                        .texOffs(152, 27).addBox(-1.0f, 0.85f, -0.80834f, 3.0f, 7.0f, 3.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 5.52933f, -2.65348f, 0.3927f, 0.0f, 0.0f));

        // Neck2 feathers - front
        neck2.addOrReplaceChild("neckFeathersFront1", CubeListBuilder.create()
                        .texOffs(132, 83).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -9.42388f, -3.61732f, -0.9163f, 0.0f, 0.0f));

        neck2.addOrReplaceChild("neckFeathersFront2", CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -8.42388f, -3.61732f, -0.95993f, 0.0f, 0.0f));

        neck2.addOrReplaceChild("neckFeathersFront3", CubeListBuilder.create()
                        .texOffs(109, 89).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -6.42388f, -3.61732f, -0.95993f, 0.0f, 0.0f));

        // Neck2 feathers - back
        neck2.addOrReplaceChild("neckFeathersBack1", CubeListBuilder.create()
                        .texOffs(132, 83).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -9.42388f, 3.38268f, 0.9163f, 0.0f, 0.0f));

        neck2.addOrReplaceChild("neckFeathersBack2", CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -8.42388f, 3.38268f, 0.95993f, 0.0f, 0.0f));

        neck2.addOrReplaceChild("neckFeathersBack5", CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -3.42388f, 3.38268f, 0.95993f, 0.0f, 0.0f));

        neck2.addOrReplaceChild("neckFeathersBack3", CubeListBuilder.create()
                        .texOffs(109, 89).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -6.42388f, 3.38268f, 0.95993f, 0.0f, 0.0f));

        // Neck2 feathers - right
        neck2.addOrReplaceChild("neckFeathersRight1", CubeListBuilder.create()
                        .texOffs(33, 87).addBox(-4.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.0f, -9.42388f, -0.11732f, 0.0f, 0.0f, -0.95993f));

        neck2.addOrReplaceChild("neckFeathersRight2", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-4.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.0f, -8.42388f, -0.11732f, 0.0f, 0.0f, -0.95993f));

        neck2.addOrReplaceChild("neckFeathersRight5", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-4.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.0f, -3.42388f, -0.11732f, 0.0f, 0.0f, -0.95993f));

        neck2.addOrReplaceChild("neckFeathersRight3", CubeListBuilder.create()
                        .texOffs(51, 64).addBox(-4.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.0f, -6.42388f, -0.11732f, 0.0f, 0.0f, -0.95993f));

        // Neck2 feathers - left (mirrored)
        neck2.addOrReplaceChild("neckFeathersLeft1", CubeListBuilder.create()
                        .texOffs(33, 87).mirror().addBox(0.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(4.0f, -9.42388f, -0.11732f, 0.0f, 0.0f, 0.95993f));

        neck2.addOrReplaceChild("neckFeathersLeft2", CubeListBuilder.create()
                        .texOffs(0, 10).mirror().addBox(0.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(4.0f, -8.42388f, -0.11732f, 0.0f, 0.0f, 0.95993f));

        neck2.addOrReplaceChild("neckFeathersLeft5", CubeListBuilder.create()
                        .texOffs(0, 10).mirror().addBox(0.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(4.0f, -3.42388f, -0.11732f, 0.0f, 0.0f, 0.95993f));

        neck2.addOrReplaceChild("neckFeathersLeft3", CubeListBuilder.create()
                        .texOffs(51, 64).mirror().addBox(0.0f, 0.0f, -3.5f, 4.0f, 0.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(4.0f, -6.42388f, -0.11732f, 0.0f, 0.0f, 0.95993f));

        // Left arm chain
        PartDefinition leftArmJoint = chest.addOrReplaceChild("leftArmJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(8.0f, -5.67063f, 0.30667f, -0.2618f, 0.0f, 0.0f));

        PartDefinition leftUpperArm = leftArmJoint.addOrReplaceChild("leftUpperArm", CubeListBuilder.create()
                        .texOffs(154, 10).mirror().addBox(-2.65894f, -1.62759f, -1.58832f, 3.0f, 14.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false)
                        .texOffs(165, 58).mirror().addBox(-2.65894f, -1.62759f, 1.41168f, 3.0f, 14.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.63565f, 0.65773f, -0.74922f));

        PartDefinition leftLowerArm = leftUpperArm.addOrReplaceChild("leftLowerArm", CubeListBuilder.create()
                        .texOffs(152, 69).mirror().addBox(-0.5472f, -1.41395f, -2.06106f, 3.0f, 11.0f, 3.0f, new CubeDeformation(0.0f)).mirror(false)
                        .texOffs(0, 38).mirror().addBox(-0.5472f, -1.41395f, 0.93894f, 3.0f, 11.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-2.15894f, 10.87241f, -0.08832f, 0.64697f, -0.53341f, 1.13319f));

        PartDefinition leftHand = leftLowerArm.addOrReplaceChild("leftHand", CubeListBuilder.create()
                        .texOffs(139, 56).mirror().addBox(-3.0f, -0.5f, -7.5f, 6.0f, 6.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.9528f, 9.58605f, -0.56106f, -0.94677f, 1.26104f, 0.48744f));

        leftHand.addOrReplaceChild("leftFinger", CubeListBuilder.create()
                        .texOffs(153, 56).addBox(-2.5f, 0.0f, -5.0f, 5.0f, 0.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.5f, 2.85902f, -5.64707f, -1.9635f, 0.0f, 0.0f));

        leftLowerArm.addOrReplaceChild("leftRing", CubeListBuilder.create()
                        .texOffs(104, 0).mirror().addBox(-3.5f, -2.0f, -1.75f, 8.0f, 2.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(1.29735f, 10.97108f, -0.56106f));

        // Right arm chain (mirrored)
        PartDefinition rightArmJoint = chest.addOrReplaceChild("rightArmJoint", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-8.0f, -5.67063f, 0.30667f, -0.2618f, 0.0f, 0.0f));

        PartDefinition rightUpperArm = rightArmJoint.addOrReplaceChild("rightUpperArm", CubeListBuilder.create()
                        .texOffs(154, 10).addBox(-0.34106f, -1.62759f, -1.58832f, 3.0f, 14.0f, 3.0f, new CubeDeformation(0.0f))
                        .texOffs(165, 58).addBox(-0.34106f, -1.62759f, 1.41168f, 3.0f, 14.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.63565f, -0.65773f, 0.74922f));

        PartDefinition rightLowerArm = rightUpperArm.addOrReplaceChild("rightLowerArm", CubeListBuilder.create()
                        .texOffs(152, 69).addBox(-2.4528f, -1.41395f, -2.06106f, 3.0f, 11.0f, 3.0f, new CubeDeformation(0.0f))
                        .texOffs(0, 38).addBox(-2.4528f, -1.41395f, 0.93894f, 3.0f, 11.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(2.15894f, 10.87241f, -0.08832f, 0.64697f, 0.53341f, -1.13319f));

        PartDefinition rightHand = rightLowerArm.addOrReplaceChild("rightHand", CubeListBuilder.create()
                        .texOffs(139, 56).addBox(-3.0f, -0.5f, -7.5f, 6.0f, 6.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-0.9528f, 9.58605f, -0.56106f, -0.94677f, -1.26104f, -0.48744f));

        rightHand.addOrReplaceChild("rightFinger", CubeListBuilder.create()
                        .texOffs(153, 56).mirror().addBox(-2.5f, 0.0f, -5.0f, 5.0f, 0.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-0.5f, 2.85902f, -5.64707f, -1.9635f, 0.0f, 0.0f));

        rightLowerArm.addOrReplaceChild("rightRing", CubeListBuilder.create()
                        .texOffs(104, 0).addBox(-4.5f, -2.0f, -1.75f, 8.0f, 2.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-1.29735f, 10.97108f, -0.56106f));

        // Chest collar and drapery
        PartDefinition chestCollar = chest.addOrReplaceChild("chestCollar", CubeListBuilder.create()
                        .texOffs(38, 38).addBox(-9.5f, -26.25f, -10.0f, 19.0f, 0.0f, 16.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 18.32937f, 1.30667f));

        chestCollar.addOrReplaceChild("chestDraperyFront", CubeListBuilder.create()
                        .texOffs(108, 60).addBox(-6.5f, 0.0f, 0.0f, 13.0f, 4.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -26.25f, -10.0f, 0.56723f, 0.0f, 0.0f));

        chestCollar.addOrReplaceChild("chestDraperyRight", CubeListBuilder.create()
                        .texOffs(154, -7).addBox(0.0f, 0.0f, -6.5f, 0.0f, 4.0f, 13.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-9.5f, -26.25f, -0.5f, 0.0f, 0.0f, 0.56723f));

        chestCollar.addOrReplaceChild("chestDraperyLeft", CubeListBuilder.create()
                        .texOffs(154, -7).addBox(0.0f, 0.0f, -6.5f, 0.0f, 4.0f, 13.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(9.5f, -26.25f, -0.5f, 0.0f, 0.0f, -0.56723f));

        chestCollar.addOrReplaceChild("chestDraperyBack", CubeListBuilder.create()
                        .texOffs(98, 79).addBox(-9.5f, 0.0f, 0.0f, 19.0f, 6.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -26.25f, 6.0f, -0.34907f, 0.0f, 0.0f));

        // Chest feathers - multiple layers of plumage
        chest.addOrReplaceChild("chestFeathersFront1", CubeListBuilder.create()
                        .texOffs(99, 96).addBox(-9.5f, 0.0f, 0.0f, 19.0f, 5.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -4.67063f, -8.69333f, 0.61087f, 0.0f, 0.0f));

        chest.addOrReplaceChild("chestFeathersLeft2", CubeListBuilder.create()
                        .texOffs(108, 54).addBox(-8.5f, 0.0f, 0.0f, 16.0f, 6.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(9.5f, -7.67063f, -0.19333f, 0.3927f, 1.5708f, 0.0f));

        chest.addOrReplaceChild("chestFeathersRight2", CubeListBuilder.create()
                        .texOffs(108, 54).mirror().addBox(-7.5f, 0.0f, 0.0f, 16.0f, 6.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-9.5f, -7.67063f, -0.19333f, 0.3927f, -1.5708f, 0.0f));

        chest.addOrReplaceChild("chestFeathersFront2", CubeListBuilder.create()
                        .texOffs(86, 38).addBox(-9.5f, 0.0f, -6.0f, 19.0f, 0.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -7.67063f, -8.69333f, -1.1781f, 0.0f, 0.0f));

        chest.addOrReplaceChild("chestFeathersLeft3", CubeListBuilder.create()
                        .texOffs(93, 74).addBox(-9.5f, 0.0f, -5.0f, 16.0f, 0.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(9.5f, -4.67063f, 0.80667f, -0.95993f, 1.5708f, 0.0f));

        chest.addOrReplaceChild("chestFeathersRight3", CubeListBuilder.create()
                        .texOffs(93, 74).mirror().addBox(-6.5f, 0.0f, -5.0f, 16.0f, 0.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-9.5f, -4.67063f, 0.80667f, -0.95993f, -1.5708f, 0.0f));

        chest.addOrReplaceChild("chestFeathersFront3", CubeListBuilder.create()
                        .texOffs(79, 65).addBox(-8.0f, 0.0f, -9.0f, 16.0f, 0.0f, 9.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -2.17063f, -8.69333f, -0.7854f, 0.0f, 0.0f));

        chest.addOrReplaceChild("chestFeathersLeft1", CubeListBuilder.create()
                        .texOffs(20, 77).mirror().addBox(-4.70257f, -0.14851f, -5.13479f, 13.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(8.0f, -0.92063f, -7.19333f, -1.26208f, 1.14362f, -0.79501f));

        chest.addOrReplaceChild("chestFeathersRight1", CubeListBuilder.create()
                        .texOffs(20, 77).addBox(-8.29743f, -0.14851f, -5.13479f, 13.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-8.0f, -0.92063f, -7.19333f, -1.26208f, -1.14362f, 0.79501f));

        // Right leg chain (digitigrade bird legs)
        PartDefinition rightThighJoint = body.addOrReplaceChild("rightThighJoint", CubeListBuilder.create(),
                PartPose.offset(-11.5f, -6.0f, 1.0f));

        PartDefinition rightThigh = rightThighJoint.addOrReplaceChild("rightThigh", CubeListBuilder.create()
                        .texOffs(154, 83).addBox(-0.76327f, -3.76741f, -0.80485f, 2.0f, 15.0f, 2.0f, new CubeDeformation(0.0f))
                        .texOffs(40, 89).addBox(-4.26327f, -1.76741f, -4.30485f, 9.0f, 9.0f, 9.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 1.39626f, -0.61087f, 0.0f));

        PartDefinition rightCalf = rightThigh.addOrReplaceChild("rightCalf", CubeListBuilder.create()
                        .texOffs(0, 77).addBox(-1.5f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.73673f, 11.23259f, 0.19515f, -1.70746f, 0.0f, 0.0f));

        PartDefinition rightAnkle = rightCalf.addOrReplaceChild("rightAnkle", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.5f, 0.02205f, -0.98912f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 6.0f, 0.0f, 1.35263f, 0.0f, 0.0f));

        rightAnkle.addOrReplaceChild("rightFoot", CubeListBuilder.create()
                        .texOffs(106, 10).addBox(-4.5f, -0.6897f, -0.64097f, 8.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 5.02205f, 0.01088f, -0.43633f, 0.0f, 0.0f));

        // Left leg chain (mirrored digitigrade)
        PartDefinition leftThighJoint = body.addOrReplaceChild("leftThighJoint", CubeListBuilder.create(),
                PartPose.offset(11.5f, -6.0f, 1.0f));

        PartDefinition leftThigh = leftThighJoint.addOrReplaceChild("leftThigh", CubeListBuilder.create()
                        .texOffs(154, 83).mirror().addBox(-1.23673f, -3.76741f, -0.80485f, 2.0f, 15.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false)
                        .texOffs(40, 89).mirror().addBox(-4.73673f, -1.76741f, -4.30485f, 9.0f, 9.0f, 9.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 1.39626f, 0.61087f, 0.0f));

        PartDefinition leftCalf = leftThigh.addOrReplaceChild("leftCalf", CubeListBuilder.create()
                        .texOffs(0, 77).mirror().addBox(-0.5f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(-0.73673f, 11.23259f, 0.19515f, -1.70746f, 0.0f, 0.0f));

        PartDefinition leftAnkle = leftCalf.addOrReplaceChild("leftAnkle", CubeListBuilder.create()
                        .texOffs(0, 0).mirror().addBox(-0.5f, 0.02205f, -0.98912f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 6.0f, 0.0f, 1.35263f, 0.0f, 0.0f));

        leftAnkle.addOrReplaceChild("leftFoot", CubeListBuilder.create()
                        .texOffs(106, 10).mirror().addBox(-3.5f, -0.6897f, -0.64097f, 8.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 5.02205f, 0.01088f, -0.43633f, 0.0f, 0.0f));

        // Tail - fan of feather planes
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(),
                PartPose.offset(0.0f, -2.0f, 10.0f));

        tail.addOrReplaceChild("tail1", CubeListBuilder.create()
                        .texOffs(136, 0).addBox(-4.5f, -31.0f, 0.0f, 9.0f, 28.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.ZERO);

        tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                        .texOffs(136, 0).addBox(-4.5f, -30.0f, 0.0f, 9.0f, 28.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.3927f));

        tail.addOrReplaceChild("tail5", CubeListBuilder.create()
                        .texOffs(136, 0).mirror().addBox(-4.5f, -30.0f, 0.0f, 9.0f, 28.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3927f));

        tail.addOrReplaceChild("tail3", CubeListBuilder.create()
                        .texOffs(136, 0).addBox(-4.5f, -27.0f, 0.0f, 9.0f, 28.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.7854f));

        tail.addOrReplaceChild("tail4", CubeListBuilder.create()
                        .texOffs(136, 0).mirror().addBox(-4.5f, -27.0f, 0.0f, 9.0f, 28.0f, 0.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7854f));

        // Stomach - large round belly
        PartDefinition stomach = body.addOrReplaceChild("stomach", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12.5f, -8.0f, -11.0f, 25.0f, 16.0f, 22.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -8.0f, 0.0f));

        // Belly feathers - front
        stomach.addOrReplaceChild("bellyFeathersFront1", CubeListBuilder.create()
                        .texOffs(87, 22).addBox(-7.0f, 0.0f, -8.0f, 14.0f, 0.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -1.0f, -11.0f, -0.69813f, 0.0f, 0.0f));

        stomach.addOrReplaceChild("bellyFeathersRight1", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-14.0f, 0.0f, -12.0f, 16.0f, 0.0f, 12.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-1.0f, -2.0f, -11.0f, -0.62218f, -0.62419f, 0.34177f));

        stomach.addOrReplaceChild("bellyFeathersRight4", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-14.0f, 0.0f, -12.0f, 16.0f, 0.0f, 12.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.0f, -6.0f, -10.0f, -0.43535f, -0.75737f, 0.11292f));

        stomach.addOrReplaceChild("bellyFeathersLeft4", CubeListBuilder.create()
                        .texOffs(60, 0).mirror().addBox(-2.0f, 0.0f, -12.0f, 16.0f, 0.0f, 12.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(4.0f, -6.0f, -10.0f, -0.43535f, 0.75737f, -0.11292f));

        stomach.addOrReplaceChild("bellyFeathersRight2", CubeListBuilder.create()
                        .texOffs(62, 12).addBox(-10.0f, 0.0f, -7.0f, 17.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-12.5f, -4.0f, -4.0f, -0.73484f, -1.27794f, 0.42029f));

        stomach.addOrReplaceChild("bellyFeathersRight5", CubeListBuilder.create()
                        .texOffs(62, 12).addBox(-7.63544f, -0.34088f, -5.91748f, 17.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-9.5f, -6.25f, 8.5f, 2.66875f, -0.80746f, -3.06171f));

        stomach.addOrReplaceChild("bellyFeathersLeft5", CubeListBuilder.create()
                        .texOffs(62, 12).mirror().addBox(-9.36456f, -0.34088f, -5.91748f, 17.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(9.5f, -6.25f, 8.5f, 2.66875f, 0.80746f, 3.06171f));

        stomach.addOrReplaceChild("bellyFeathersRight3", CubeListBuilder.create()
                        .texOffs(59, 54).addBox(-9.71964f, -0.48027f, -3.23951f, 19.0f, 0.0f, 11.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-9.5f, -3.0f, 8.0f, 0.59295f, 0.58219f, 0.36548f));

        stomach.addOrReplaceChild("bellyFeathersLeft3", CubeListBuilder.create()
                        .texOffs(59, 54).mirror().addBox(-9.28036f, -0.48027f, -3.23951f, 19.0f, 0.0f, 11.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(9.5f, -3.0f, 8.0f, 0.59295f, -0.58219f, -0.36548f));

        stomach.addOrReplaceChild("bellyFeathersLeft1", CubeListBuilder.create()
                        .texOffs(60, 0).mirror().addBox(-2.0f, 0.0f, -12.0f, 16.0f, 0.0f, 12.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(1.0f, -2.0f, -11.0f, -0.62217f, 0.62419f, -0.34177f));

        stomach.addOrReplaceChild("bellyFeathersLeft2", CubeListBuilder.create()
                        .texOffs(62, 12).mirror().addBox(-7.0f, 0.0f, -7.0f, 17.0f, 0.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(12.5f, -4.0f, -4.0f, -0.73484f, 1.27794f, -0.42029f));

        stomach.addOrReplaceChild("bellyFeathersFront2", CubeListBuilder.create()
                        .texOffs(83, 45).addBox(-8.0f, 0.0f, 1.0f, 16.0f, 0.0f, 9.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -5.5f, -9.0f, -2.48709f, 0.0f, -3.14159f));

        stomach.addOrReplaceChild("bellyFeathersFront3", CubeListBuilder.create()
                        .texOffs(89, 31).addBox(-9.5f, 0.0f, -5.0f, 19.0f, 0.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -8.0f, -11.0f, -0.69813f, 0.0f, 0.0f));

        return LayerDefinition.create(meshdefinition, 256, 128);
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
