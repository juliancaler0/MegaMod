/*
 * Converted from MowziesMobs ModelLantern (LLibrary AdvancedModelRenderer)
 * to vanilla 1.21.11 NeoForge EntityModel format.
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.LanternAnimations;
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

public class LanternModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"lantern"), "main");
    public final ModelPart body;
    public final ModelPart center;
    public final ModelPart bubbles;
    public final ModelPart bubble1;
    public final ModelPart bubble2;
    public final ModelPart bubble3;
    public final ModelPart bubble4;
    public final ModelPart bottomBits;
    public final ModelPart bottomBit1;
    public final ModelPart bottomBit2;
    public final ModelPart bottomBit3;
    public final ModelPart bottomBit4;
    public final ModelPart stem;
    public final ModelPart leaf1;
    public final ModelPart leaf2;
    public final ModelPart leaf3;
    public final ModelPart leaf4;
    public final ModelPart stem1;
    public final ModelPart stem2;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation attackAnimation;

    public LanternModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.center = root.getChild("center");
        this.bubbles = root.getChild("bubbles");
        this.bubble1 = this.bubbles.getChild("bubble1");
        this.bubble2 = this.bubbles.getChild("bubble2");
        this.bubble3 = this.bubbles.getChild("bubble3");
        this.bubble4 = this.bubbles.getChild("bubble4");
        this.bottomBits = this.body.getChild("bottomBits");
        this.bottomBit1 = this.bottomBits.getChild("bottomBit1");
        this.bottomBit2 = this.bottomBits.getChild("bottomBit2");
        this.bottomBit3 = this.bottomBits.getChild("bottomBit3");
        this.bottomBit4 = this.bottomBits.getChild("bottomBit4");
        this.stem = this.body.getChild("stem");
        this.leaf1 = this.stem.getChild("leaf1");
        this.leaf2 = this.stem.getChild("leaf2");
        this.leaf3 = this.stem.getChild("leaf3");
        this.leaf4 = this.stem.getChild("leaf4");
        this.stem1 = this.stem.getChild("stem1");
        this.stem2 = this.stem1.getChild("stem2");
        this.idleAnimation = LanternAnimations.idle.bake(root);
        this.attackAnimation = LanternAnimations.attack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // body: jellyfish orb (15x15x15), rotationPoint (0, 11, 0)
        PartDefinition body = partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(1, 0).addBox(-7.5f, -7.5f, -7.5f, 15.0f, 15.0f, 15.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 11.0f, 0.0f));

        // center: inner core (6x6x6), rotationPoint (0, 11, 0) -- root child, same position as body
        PartDefinition center = partdefinition.addOrReplaceChild("center",
            CubeListBuilder.create().texOffs(40, 51).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 11.0f, 0.0f));

        // bubbles: empty group at (0, 11, 0) -- root child
        PartDefinition bubbles = partdefinition.addOrReplaceChild("bubbles",
            CubeListBuilder.create(),
            PartPose.offset(0.0f, 11.0f, 0.0f));

        // bubble1: (4x4x4) at (2.6, 2.5, 2.8), rotated
        PartDefinition bubble1 = bubbles.addOrReplaceChild("bubble1",
            CubeListBuilder.create().texOffs(0, 7).addBox(-2.0f, -2.0f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(2.6f, 2.5f, 2.8f, 0.27314402793711257f, 0.6829473363053812f, 0.5462880558742251f));

        // bubble2: (4x4x4) at (-2.8, -3.0, 1.8), rotated
        PartDefinition bubble2 = bubbles.addOrReplaceChild("bubble2",
            CubeListBuilder.create().texOffs(0, 7).addBox(-2.0f, -2.0f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-2.8f, -3.0f, 1.8f, 1.3203415791337103f, 1.5025539530419183f, 0.5462880558742251f));

        // bubble3: (3x3x3) at (-2.0, 4.0, -2.9), rotated
        PartDefinition bubble3 = bubbles.addOrReplaceChild("bubble3",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.5f, -1.5f, 3.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-2.0f, 4.0f, -2.9f, -0.091106186954104f, 1.7756979809790308f, 0.40980330836826856f));

        // bubble4: (3x3x3) at (3.0, -1.8, -2.4), rotated
        PartDefinition bubble4 = bubbles.addOrReplaceChild("bubble4",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.5f, -1.5f, 3.0f, 3.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(3.0f, -1.8f, -2.4f, -0.7740535232594852f, 0.136659280431156f, 0.40980330836826856f));

        // bottomBits: empty group at (0, 7.5, 0), child of body, rotated 45deg Y
        PartDefinition bottomBits = body.addOrReplaceChild("bottomBits",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0f, 7.5f, 0.0f, 0.0f, 0.7853981633974483f, 0.0f));

        // bottomBit1: (4x7x4) tentacle extension, rotated 60deg X
        PartDefinition bottomBit1 = bottomBits.addOrReplaceChild("bottomBit1",
            CubeListBuilder.create().texOffs(46, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 1.0471975511965976f, 0.0f, 0.0f));

        // bottomBit2: same but rotated 90deg Y
        PartDefinition bottomBit2 = bottomBits.addOrReplaceChild("bottomBit2",
            CubeListBuilder.create().texOffs(46, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 1.0471975511965976f, 1.5707963267948966f, 0.0f));

        // bottomBit3: same but rotated 180deg Y
        PartDefinition bottomBit3 = bottomBits.addOrReplaceChild("bottomBit3",
            CubeListBuilder.create().texOffs(46, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 1.0471975511965976f, 3.141592653589793f, 0.0f));

        // bottomBit4: same but rotated 270deg Y
        PartDefinition bottomBit4 = bottomBits.addOrReplaceChild("bottomBit4",
            CubeListBuilder.create().texOffs(46, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 1.0471975511965976f, 4.71238898038469f, 0.0f));

        // stem: empty group at (0, -7.51, 0), child of body, rotated 45deg Y
        PartDefinition stem = body.addOrReplaceChild("stem",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0f, -7.51f, 0.0f, 0.0f, 0.7853981633974483f, 0.0f));

        // leaf1: flat leaf plane (12x0x16), mirror, child of stem
        // Original: rotPoint(0, 0, 2), adjusted +0.5 on X -> (0.5, 0, 2)
        PartDefinition leaf1 = stem.addOrReplaceChild("leaf1",
            CubeListBuilder.create().texOffs(-16, 42).mirror().addBox(-6.0f, 0.0f, -16.0f, 12.0f, 0.0f, 16.0f, new CubeDeformation(0.0f)).mirror(false),
            PartPose.offsetAndRotation(0.5f, 0.0f, 2.0f, -0.2617993877991494f, 3.141592653589793f, 0.0f));

        // leaf2: flat leaf plane (16x0x12), child of stem
        // Original: rotPoint(-2, 0, 0), adjusted -0.5 on Z -> (-2, 0, -0.5)
        PartDefinition leaf2 = stem.addOrReplaceChild("leaf2",
            CubeListBuilder.create().texOffs(8, 30).addBox(0.0f, 0.0f, -6.0f, 16.0f, 0.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(-2.0f, 0.0f, -0.5f, 0.0f, 3.141592653589793f, 0.2617993877991494f));

        // leaf3: flat leaf plane (12x0x16), child of stem
        // Original: rotPoint(0, 0, -2), adjusted +0.5 on X -> (0.5, 0, -2)
        PartDefinition leaf3 = stem.addOrReplaceChild("leaf3",
            CubeListBuilder.create().texOffs(-16, 42).addBox(-6.0f, 0.0f, -16.0f, 12.0f, 0.0f, 16.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.5f, 0.0f, -2.0f, -0.2617993877991494f, 0.0f, 0.0f));

        // leaf4: flat leaf plane (16x0x12), child of stem
        // Original: rotPoint(2, 0, 0), adjusted +0.5 on Z -> (2, 0, 0.5)
        PartDefinition leaf4 = stem.addOrReplaceChild("leaf4",
            CubeListBuilder.create().texOffs(8, 30).addBox(0.0f, 0.0f, -6.0f, 16.0f, 0.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(2.0f, 0.0f, 0.5f, 0.0f, 0.0f, -0.2617993877991494f));

        // stem1: cap block (6x3x6) at top, child of stem
        PartDefinition stem1 = stem.addOrReplaceChild("stem1",
            CubeListBuilder.create().texOffs(40, 42).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // stem2: upper stem plane (0x10x10), child of stem1
        // Original: rotPoint(0, -3, 0), adjusted +0.3536 on X and Z -> (0.3536, -3, 0.3536), rotated 45deg Y
        PartDefinition stem2 = stem1.addOrReplaceChild("stem2",
            CubeListBuilder.create().texOffs(0, 20).addBox(0.0f, -10.0f, -5.0f, 0.0f, 10.0f, 10.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.3536f, -3.0f, 0.3536f, 0.0f, 0.7853981633974483f, 0.0f));

        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonMobRenderState) {
            DungeonEntityRenderers.DungeonMobRenderState mobState = (DungeonEntityRenderers.DungeonMobRenderState)state;
            this.idleAnimation.apply(mobState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(mobState.attackAnimationState, state.ageInTicks);
        }
    }
}
