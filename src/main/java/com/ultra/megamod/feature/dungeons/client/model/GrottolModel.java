package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.GrottolAnimations;
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

public class GrottolModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"grottol"), "main");
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart crystal1;
    public final ModelPart crystal2;
    public final ModelPart crystal3;
    public final ModelPart crystal4;
    public final ModelPart crystal5;
    public final ModelPart crystal6;
    public final ModelPart crystal7;
    public final ModelPart clawLeftJoint;
    public final ModelPart clawLeftUpper;
    public final ModelPart clawLeftLower;
    public final ModelPart clawLeft;
    public final ModelPart clawRightJoint;
    public final ModelPart clawRightUpper;
    public final ModelPart clawRightLower;
    public final ModelPart clawRight;
    public final ModelPart leg1LeftJoint;
    public final ModelPart leg1LeftUpper;
    public final ModelPart leg1LeftLower;
    public final ModelPart foot1Left;
    public final ModelPart leg2LeftJoint;
    public final ModelPart leg2LeftUpper;
    public final ModelPart leg2LeftLower;
    public final ModelPart foot2Left;
    public final ModelPart leg3LeftJoint;
    public final ModelPart leg3LeftUpper;
    public final ModelPart leg3LeftLower;
    public final ModelPart foot3Left;
    public final ModelPart leg1RightJoint;
    public final ModelPart leg1RightUpper;
    public final ModelPart leg1RightLower;
    public final ModelPart foot1Right;
    public final ModelPart leg2RightJoint;
    public final ModelPart leg2RightUpper;
    public final ModelPart leg2RightLower;
    public final ModelPart foot2Right;
    public final ModelPart leg3RightJoint;
    public final ModelPart leg3RightUpper;
    public final ModelPart leg3RightLower;
    public final ModelPart foot3Right;
    public final ModelPart eyeLeft;
    public final ModelPart eyeRight;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;

    public GrottolModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.eyeLeft = this.head.getChild("eyeLeft");
        this.eyeRight = this.head.getChild("eyeRight");
        this.crystal1 = this.body.getChild("crystal1");
        this.crystal2 = this.body.getChild("crystal2");
        this.crystal3 = this.body.getChild("crystal3");
        this.crystal4 = this.body.getChild("crystal4");
        this.crystal5 = this.body.getChild("crystal5");
        this.crystal6 = this.body.getChild("crystal6");
        this.crystal7 = this.body.getChild("crystal7");
        this.clawLeftJoint = this.body.getChild("clawLeftJoint");
        this.clawLeftUpper = this.clawLeftJoint.getChild("clawLeftUpper");
        this.clawLeftLower = this.clawLeftUpper.getChild("clawLeftLower");
        this.clawLeft = this.clawLeftLower.getChild("clawLeft");
        this.clawRightJoint = this.body.getChild("clawRightJoint");
        this.clawRightUpper = this.clawRightJoint.getChild("clawRightUpper");
        this.clawRightLower = this.clawRightUpper.getChild("clawRightLower");
        this.clawRight = this.clawRightLower.getChild("clawRight");
        this.leg1LeftJoint = this.body.getChild("leg1LeftJoint");
        this.leg1LeftUpper = this.leg1LeftJoint.getChild("leg1LeftUpper");
        this.leg1LeftLower = this.leg1LeftUpper.getChild("leg1LeftLower");
        this.foot1Left = this.leg1LeftLower.getChild("foot1Left");
        this.leg2LeftJoint = this.body.getChild("leg2LeftJoint");
        this.leg2LeftUpper = this.leg2LeftJoint.getChild("leg2LeftUpper");
        this.leg2LeftLower = this.leg2LeftUpper.getChild("leg2LeftLower");
        this.foot2Left = this.leg2LeftLower.getChild("foot2Left");
        this.leg3LeftJoint = this.body.getChild("leg3LeftJoint");
        this.leg3LeftUpper = this.leg3LeftJoint.getChild("leg3LeftUpper");
        this.leg3LeftLower = this.leg3LeftUpper.getChild("leg3LeftLower");
        this.foot3Left = this.leg3LeftLower.getChild("foot3Left");
        this.leg1RightJoint = this.body.getChild("leg1RightJoint");
        this.leg1RightUpper = this.leg1RightJoint.getChild("leg1RightUpper");
        this.leg1RightLower = this.leg1RightUpper.getChild("leg1RightLower");
        this.foot1Right = this.leg1RightLower.getChild("foot1Right");
        this.leg2RightJoint = this.body.getChild("leg2RightJoint");
        this.leg2RightUpper = this.leg2RightJoint.getChild("leg2RightUpper");
        this.leg2RightLower = this.leg2RightUpper.getChild("leg2RightLower");
        this.foot2Right = this.leg2RightLower.getChild("foot2Right");
        this.leg3RightJoint = this.body.getChild("leg3RightJoint");
        this.leg3RightUpper = this.leg3RightJoint.getChild("leg3RightUpper");
        this.leg3RightLower = this.leg3RightUpper.getChild("leg3RightLower");
        this.foot3Right = this.leg3RightLower.getChild("foot3Right");
        this.idleAnimation = GrottolAnimations.idle.bake(root);
        this.walkAnimation = GrottolAnimations.walk.bake(root);
        this.attackAnimation = GrottolAnimations.attack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // body: setRotationPoint(0, 19, 0), addBox(-6, -3, -6, 12, 5, 10), rotation(-0.1745, 0, 0)
        PartDefinition body = partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -3.0f, -6.0f, 12.0f, 5.0f, 10.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)19.0f, (float)0.0f, (float)-0.17453292519943295f, (float)0.0f, (float)0.0f));

        // head: setRotationPoint(0, 1.5, -5), addBox(-2, -1, -3, 4, 3, 2), rotation(0.1745, 0, 0)
        PartDefinition head = body.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(34, 0).addBox(-2.0f, -1.0f, -3.0f, 4.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)1.5f, (float)-5.0f, (float)0.17453292519943295f, (float)0.0f, (float)0.0f));

        // eyeLeft: setRotationPoint(2, -0.5, -2.5), addBox(-1, -1, -1, 2, 2, 2), rotation(-1.0472, 1.7453, 2.5307), mirror=true
        PartDefinition eyeLeft = head.addOrReplaceChild("eyeLeft",
            CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-1.0f, -1.0f, -1.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false),
            PartPose.offsetAndRotation((float)2.0f, (float)-0.5f, (float)-2.5f, (float)-1.0471975511965976f, (float)1.7453292519943295f, (float)2.530727415391778f));

        // eyeRight: setRotationPoint(-2, -0.5, -2.5), addBox(-1, -1, -1, 2, 2, 2), rotation(-1.0472, -1.7453, -2.5307)
        PartDefinition eyeRight = head.addOrReplaceChild("eyeRight",
            CubeListBuilder.create().texOffs(0, 4).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-2.0f, (float)-0.5f, (float)-2.5f, (float)-1.0471975511965976f, (float)-1.7453292519943295f, (float)-2.530727415391778f));

        // crystal1: setRotationPoint(2, -4, -1), addBox(-2, -11, -2, 4, 13, 4), rotation(0.0456, -0.6829, 0.2276)
        PartDefinition crystal1 = body.addOrReplaceChild("crystal1",
            CubeListBuilder.create().texOffs(12, 15).addBox(-2.0f, -11.0f, -2.0f, 4.0f, 13.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)2.0f, (float)-4.0f, (float)-1.0f, (float)0.045553093477052f, (float)-0.6829473363053812f, (float)0.22759093446006054f));

        // crystal2: setRotationPoint(-2, -3, 1), addBox(-1.5, -10, -1.5, 3, 12, 3), rotation(0.6374, 2.0033, 0.4098)
        PartDefinition crystal2 = body.addOrReplaceChild("crystal2",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.5f, -10.0f, -1.5f, 3.0f, 12.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-2.0f, (float)-3.0f, (float)1.0f, (float)0.6373942428283291f, (float)2.0032889154390916f, (float)0.40980330836826856f));

        // crystal3: setRotationPoint(0, -3, -4.8), addBox(-1.5, -7, -1.5, 3, 9, 3), rotation(0.5918, 0.3187, 0.2276)
        PartDefinition crystal3 = body.addOrReplaceChild("crystal3",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.5f, -7.0f, -1.5f, 3.0f, 9.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)-3.0f, (float)-4.8f, (float)0.5918411493512771f, (float)0.31869712141416456f, (float)0.22759093446006054f));

        // crystal4: setRotationPoint(-4.5, -3, -2), addBox(-1.5, -5, -1.5, 3, 7, 3), rotation(0.2276, -0.3187, -0.5463)
        PartDefinition crystal4 = body.addOrReplaceChild("crystal4",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.5f, -5.0f, -1.5f, 3.0f, 7.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-4.5f, (float)-3.0f, (float)-2.0f, (float)0.22759093446006054f, (float)-0.31869712141416456f, (float)-0.5462880558742251f));

        // crystal5: setRotationPoint(4, -3, -3.5), addBox(-1.5, -4.5, -1.5, 3, 6, 3), rotation(0.5562, 0.8135, 1.3924)
        PartDefinition crystal5 = body.addOrReplaceChild("crystal5",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.5f, -4.5f, -1.5f, 3.0f, 6.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)4.0f, (float)-3.0f, (float)-3.5f, (float)0.5562364326105927f, (float)0.8134979643545569f, (float)1.392423677241076f));

        // crystal6: setRotationPoint(2, -3, 3), addBox(-1.5, -5, -1.5, 3, 8, 3), rotation(-0.5918, -0.3187, 0.5463)
        PartDefinition crystal6 = body.addOrReplaceChild("crystal6",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.5f, -5.0f, -1.5f, 3.0f, 8.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)2.0f, (float)-3.0f, (float)3.0f, (float)-0.5918411493512771f, (float)-0.31869712141416456f, (float)0.5462880558742251f));

        // crystal7: setRotationPoint(-3.6, -2.4, 1.7), addBox(-1.5, -5, -1.5, 3, 8, 3), rotation(-1.3203, 0.9105, -1.9577)
        PartDefinition crystal7 = body.addOrReplaceChild("crystal7",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.5f, -5.0f, -1.5f, 3.0f, 8.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.6f, (float)-2.4f, (float)1.7f, (float)-1.3203415791337103f, (float)0.9105382707654417f, (float)-1.9577358219620393f));

        // --- Left Claw ---
        // clawLeftJoint: setRotationPoint(2, 2, -3.5), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, 0.7741, 0)
        PartDefinition clawLeftJoint = body.addOrReplaceChild("clawLeftJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)2.0f, (float)2.0f, (float)-3.5f, (float)0.17453292519943295f, (float)0.7740535232594852f, (float)0.0f));

        // clawLeftUpper: setRotationPoint(0, 0, 0), addBox(-1, -1, -0.5, 4, 1, 1), rotation(0, 0, 0.6374)
        PartDefinition clawLeftUpper = clawLeftJoint.addOrReplaceChild("clawLeftUpper",
            CubeListBuilder.create().texOffs(0, 2).addBox(-1.0f, -1.0f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.6373942428283291f));

        // clawLeftLower: setRotationPoint(3, -0.5, 0), addBox(0, -0.5, -0.5, 4, 1, 1), rotation(0, 0, -0.9105)
        PartDefinition clawLeftLower = clawLeftUpper.addOrReplaceChild("clawLeftLower",
            CubeListBuilder.create().texOffs(0, 2).addBox(0.0f, -0.5f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.0f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.9105382707654417f));

        // clawLeft: setRotationPoint(3.5, 0, 0), addBox(-1, -1.5, -5, 2, 3, 6), rotation(0.2731, 0.2731, -0.0456)
        PartDefinition clawLeft = clawLeftLower.addOrReplaceChild("clawLeft",
            CubeListBuilder.create().texOffs(42, 0).addBox(-1.0f, -1.5f, -5.0f, 2.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.5f, (float)0.0f, (float)0.0f, (float)0.27314402793711257f, (float)0.27314402793711257f, (float)-0.045553093477052f));

        // --- Right Claw ---
        // clawRightJoint: setRotationPoint(-2, 2, -3.5), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, -0.7741, 0)
        PartDefinition clawRightJoint = body.addOrReplaceChild("clawRightJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)-2.0f, (float)2.0f, (float)-3.5f, (float)0.17453292519943295f, (float)-0.7740535232594852f, (float)0.0f));

        // clawRightUpper: setRotationPoint(0, 0, 0), addBox(-3, -1, -0.5, 4, 1, 1), rotation(0, 0, -0.6374)
        PartDefinition clawRightUpper = clawRightJoint.addOrReplaceChild("clawRightUpper",
            CubeListBuilder.create().texOffs(0, 2).addBox(-3.0f, -1.0f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.6373942428283291f));

        // clawRightLower: setRotationPoint(-3, -0.5, 0), addBox(-4, -0.5, -0.5, 4, 1, 1), rotation(0, 0, 0.9105)
        PartDefinition clawRightLower = clawRightUpper.addOrReplaceChild("clawRightLower",
            CubeListBuilder.create().texOffs(0, 2).addBox(-4.0f, -0.5f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.0f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.9105382707654417f));

        // clawRight: setRotationPoint(-3.5, 0, 0), addBox(-1, -1.5, -5, 2, 3, 6), rotation(0.2731, -0.2731, 0.0456)
        PartDefinition clawRight = clawRightLower.addOrReplaceChild("clawRight",
            CubeListBuilder.create().texOffs(42, 0).addBox(-1.0f, -1.5f, -5.0f, 2.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.5f, (float)0.0f, (float)0.0f, (float)0.27314402793711257f, (float)-0.27314402793711257f, (float)0.045553093477052f));

        // --- Leg 1 Left ---
        // leg1LeftJoint: setRotationPoint(3.2, 2, -1.8), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, 0.4538, 0)
        PartDefinition leg1LeftJoint = body.addOrReplaceChild("leg1LeftJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)3.2f, (float)2.0f, (float)-1.8f, (float)0.17453292519943295f, (float)0.45378560551852565f, (float)0.0f));

        // leg1LeftUpper: setRotationPoint(0, 0, 0), addBox(0, -1, -0.5, 3, 1, 1), rotation(0, 0, 0.6374)
        PartDefinition leg1LeftUpper = leg1LeftJoint.addOrReplaceChild("leg1LeftUpper",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -1.0f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.6373942428283291f));

        // leg1LeftLower: setRotationPoint(3, -0.5, 0), addBox(0, -0.5, -0.5, 3, 1, 1), rotation(0, 0, -1.2748)
        PartDefinition leg1LeftLower = leg1LeftUpper.addOrReplaceChild("leg1LeftLower",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.0f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-1.2747884856566583f));

        // foot1Left: setRotationPoint(3, 0, 0), addBox(-1, -1, -1, 2, 5, 2), rotation(0, 0, 0.2731)
        PartDefinition foot1Left = leg1LeftLower.addOrReplaceChild("foot1Left",
            CubeListBuilder.create().texOffs(44, 9).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.27314402793711257f));

        // --- Leg 2 Left ---
        // leg2LeftJoint: setRotationPoint(3.8, 2, -0.1), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, -0.0436, 0)
        PartDefinition leg2LeftJoint = body.addOrReplaceChild("leg2LeftJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)3.8f, (float)2.0f, (float)-0.1f, (float)0.17453292519943295f, (float)-0.04363323129985824f, (float)0.0f));

        // leg2LeftUpper: setRotationPoint(0, 0, 0), addBox(-0.7, -1, -0.5, 4, 1, 1), rotation(0, 0, 0.5061)
        PartDefinition leg2LeftUpper = leg2LeftJoint.addOrReplaceChild("leg2LeftUpper",
            CubeListBuilder.create().texOffs(0, 2).addBox(-0.7f, -1.0f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.5061454830783556f));

        // leg2LeftLower: setRotationPoint(3.3, -0.5, 0), addBox(0, -0.5, -0.5, 3, 1, 1), rotation(0, 0, -1.5097)
        PartDefinition leg2LeftLower = leg2LeftUpper.addOrReplaceChild("leg2LeftLower",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.3f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-1.509709802975095f));

        // foot2Left: setRotationPoint(3.3, 0, 0), addBox(-1, -1, -1, 2, 6, 2), rotation(0, 0, 0.3543)
        PartDefinition foot2Left = leg2LeftLower.addOrReplaceChild("foot2Left",
            CubeListBuilder.create().texOffs(52, 9).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.3543018381548489f));

        // --- Leg 3 Left ---
        // leg3LeftJoint: setRotationPoint(3.5, 2, 1.5), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, -0.6829, 0)
        PartDefinition leg3LeftJoint = body.addOrReplaceChild("leg3LeftJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)3.5f, (float)2.0f, (float)1.5f, (float)0.17453292519943295f, (float)-0.6829473363053812f, (float)0.0f));

        // leg3LeftUpper: setRotationPoint(0, 0, 0), addBox(-0.7, -1, -0.5, 4, 1, 1), rotation(0, 0, 0.4189)
        PartDefinition leg3LeftUpper = leg3LeftJoint.addOrReplaceChild("leg3LeftUpper",
            CubeListBuilder.create().texOffs(0, 2).addBox(-0.7f, -1.0f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.41887902047863906f));

        // leg3LeftLower: setRotationPoint(3.3, -0.5, 0), addBox(0, -0.5, -0.5, 3, 1, 1), rotation(0, 0, -1.4486)
        PartDefinition leg3LeftLower = leg3LeftUpper.addOrReplaceChild("leg3LeftLower",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.3f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-1.4486232791552935f));

        // foot3Left: setRotationPoint(3.2, 0, 0), addBox(-1, -1, -1, 2, 5, 2), rotation(0, 0, 0.4538)
        PartDefinition foot3Left = leg3LeftLower.addOrReplaceChild("foot3Left",
            CubeListBuilder.create().texOffs(44, 9).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)3.2f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.45378560551852565f));

        // --- Leg 1 Right ---
        // leg1RightJoint: setRotationPoint(-3.2, 2, -2), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, -0.4014, 0)
        PartDefinition leg1RightJoint = body.addOrReplaceChild("leg1RightJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)-3.2f, (float)2.0f, (float)-2.0f, (float)0.17453292519943295f, (float)-0.40142572795869574f, (float)0.0f));

        // leg1RightUpper: setRotationPoint(0, 0, 0), addBox(-3, -1, -0.5, 3, 1, 1), rotation(0, 0, -0.6374)
        PartDefinition leg1RightUpper = leg1RightJoint.addOrReplaceChild("leg1RightUpper",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -1.0f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.6373942428283291f));

        // leg1RightLower: setRotationPoint(-3, -0.5, 0), addBox(-3, -0.5, -0.5, 3, 1, 1), rotation(0, 0, 1.2748)
        PartDefinition leg1RightLower = leg1RightUpper.addOrReplaceChild("leg1RightLower",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.0f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)1.2747884856566583f));

        // foot1Right: setRotationPoint(-3, 0, 0), addBox(-1, -1, -1, 2, 5, 2), rotation(0, 0, -0.2731)
        PartDefinition foot1Right = leg1RightLower.addOrReplaceChild("foot1Right",
            CubeListBuilder.create().texOffs(44, 9).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.27314402793711257f));

        // --- Leg 2 Right ---
        // leg2RightJoint: setRotationPoint(-3.8, 2, -0.1), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, 0.0436, 0)
        PartDefinition leg2RightJoint = body.addOrReplaceChild("leg2RightJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)-3.8f, (float)2.0f, (float)-0.1f, (float)0.17453292519943295f, (float)0.04363323129985824f, (float)0.0f));

        // leg2RightUpper: setRotationPoint(0, 0, 0), addBox(-3.3, -1, -0.5, 4, 1, 1), rotation(0, 0, -0.5061)
        PartDefinition leg2RightUpper = leg2RightJoint.addOrReplaceChild("leg2RightUpper",
            CubeListBuilder.create().texOffs(0, 2).addBox(-3.3f, -1.0f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.5061454830783556f));

        // leg2RightLower: setRotationPoint(-3.3, -0.5, 0), addBox(-3, -0.5, -0.5, 3, 1, 1), rotation(0, 0, 1.5097)
        PartDefinition leg2RightLower = leg2RightUpper.addOrReplaceChild("leg2RightLower",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.3f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)1.509709802975095f));

        // foot2Right: setRotationPoint(-3.3, 0, 0), addBox(-1, -1, -1, 2, 6, 2), rotation(0, 0, -0.3508)
        PartDefinition foot2Right = leg2RightLower.addOrReplaceChild("foot2Right",
            CubeListBuilder.create().texOffs(52, 9).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.3f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.3508111796508603f));

        // --- Leg 3 Right ---
        // leg3RightJoint: setRotationPoint(-3.5, 2, 1.5), addBox(0, -1, -0.5, 0, 0, 0), rotation(0.1745, 0.6829, 0)
        PartDefinition leg3RightJoint = body.addOrReplaceChild("leg3RightJoint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation((float)-3.5f, (float)2.0f, (float)1.5f, (float)0.17453292519943295f, (float)0.6829473363053812f, (float)0.0f));

        // leg3RightUpper: setRotationPoint(0, 0, 0), addBox(-3.3, -1, -0.5, 4, 1, 1), rotation(0, 0, -0.4189)
        PartDefinition leg3RightUpper = leg3RightJoint.addOrReplaceChild("leg3RightUpper",
            CubeListBuilder.create().texOffs(0, 2).addBox(-3.3f, -1.0f, -0.5f, 4.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.41887902047863906f));

        // leg3RightLower: setRotationPoint(-3.3, -0.5, 0), addBox(-3, -0.5, -0.5, 3, 1, 1), rotation(0, 0, 1.4486)
        PartDefinition leg3RightLower = leg3RightUpper.addOrReplaceChild("leg3RightLower",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -0.5f, -0.5f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.3f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0f, (float)1.4486232791552935f));

        // foot3Right: setRotationPoint(-3.2, 0, 0), addBox(-1, -1, -1, 2, 5, 2), rotation(0, 0, -0.4538)
        PartDefinition foot3Right = leg3RightLower.addOrReplaceChild("foot3Right",
            CubeListBuilder.create().texOffs(44, 9).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation((float)-3.2f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.45378560551852565f));

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
