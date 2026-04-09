/*
 * Converted from MowziesMobs ModelFoliaath (LLibrary AdvancedModelRenderer)
 * to vanilla 1.21.11 NeoForge EntityModel format.
 *
 * Hierarchy:
 *   root
 *     stem1Joint (empty pivot)
 *       stem1Base (3x15x3 stalk)
 *         stem2 (3x15x3)
 *           stem3 (3x13x3)
 *             stem4 (3x10x3)
 *               headBase (6x6x2 head joint)
 *                 mouthTop1 (12x4x12 upper jaw)
 *                   teethTop1 (12x3x12)
 *                   mouthTop2 (6x2x7 tip)
 *                     teethTop2 (6x3x7)
 *                 mouthBottom1 (12x4x12 lower jaw, flipped PI on Z)
 *                   teethBottom1 (12x3x12)
 *                   mouthBottom2 (6x2x7 tip)
 *                     teethBottom2 (6x3x7)
 *                 mouthBack (12x9x2)
 *                 tongue1Base (6x2x6)
 *                   tongue2 (6x2x6)
 *                     tongue3 (4x2x5)
 *                 leaf1Head..leaf8Head (7x19x0 flat planes, 8 around head)
 *     bigLeaf1Base..bigLeaf4Base (8x14x0 ground leaves, 4 at ground level)
 *       bigLeaf1End..bigLeaf4End (8x14x0 leaf tips)
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.FoliaathAnimations;
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

public class FoliaathModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"foliaath"), "main");
    public final ModelPart stem1Joint;
    public final ModelPart stem1Base;
    public final ModelPart stem2;
    public final ModelPart stem3;
    public final ModelPart stem4;
    public final ModelPart headBase;
    public final ModelPart mouthTop1;
    public final ModelPart mouthTop2;
    public final ModelPart teethTop1;
    public final ModelPart teethTop2;
    public final ModelPart mouthBottom1;
    public final ModelPart mouthBottom2;
    public final ModelPart teethBottom1;
    public final ModelPart teethBottom2;
    public final ModelPart mouthBack;
    public final ModelPart tongue1Base;
    public final ModelPart tongue2;
    public final ModelPart tongue3;
    public final ModelPart leaf1Head;
    public final ModelPart leaf2Head;
    public final ModelPart leaf3Head;
    public final ModelPart leaf4Head;
    public final ModelPart leaf5Head;
    public final ModelPart leaf6Head;
    public final ModelPart leaf7Head;
    public final ModelPart leaf8Head;
    public final ModelPart bigLeaf1Base;
    public final ModelPart bigLeaf1End;
    public final ModelPart bigLeaf2Base;
    public final ModelPart bigLeaf2End;
    public final ModelPart bigLeaf3Base;
    public final ModelPart bigLeaf3End;
    public final ModelPart bigLeaf4Base;
    public final ModelPart bigLeaf4End;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation attackAnimation;

    public FoliaathModel(ModelPart root) {
        super(root);
        this.stem1Joint = root.getChild("stem1Joint");
        this.stem1Base = this.stem1Joint.getChild("stem1Base");
        this.stem2 = this.stem1Base.getChild("stem2");
        this.stem3 = this.stem2.getChild("stem3");
        this.stem4 = this.stem3.getChild("stem4");
        this.headBase = this.stem4.getChild("headBase");
        this.mouthTop1 = this.headBase.getChild("mouthTop1");
        this.mouthTop2 = this.mouthTop1.getChild("mouthTop2");
        this.teethTop1 = this.mouthTop1.getChild("teethTop1");
        this.teethTop2 = this.mouthTop2.getChild("teethTop2");
        this.mouthBottom1 = this.headBase.getChild("mouthBottom1");
        this.mouthBottom2 = this.mouthBottom1.getChild("mouthBottom2");
        this.teethBottom1 = this.mouthBottom1.getChild("teethBottom1");
        this.teethBottom2 = this.mouthBottom2.getChild("teethBottom2");
        this.mouthBack = this.headBase.getChild("mouthBack");
        this.tongue1Base = this.headBase.getChild("tongue1Base");
        this.tongue2 = this.tongue1Base.getChild("tongue2");
        this.tongue3 = this.tongue2.getChild("tongue3");
        this.leaf1Head = this.headBase.getChild("leaf1Head");
        this.leaf2Head = this.headBase.getChild("leaf2Head");
        this.leaf3Head = this.headBase.getChild("leaf3Head");
        this.leaf4Head = this.headBase.getChild("leaf4Head");
        this.leaf5Head = this.headBase.getChild("leaf5Head");
        this.leaf6Head = this.headBase.getChild("leaf6Head");
        this.leaf7Head = this.headBase.getChild("leaf7Head");
        this.leaf8Head = this.headBase.getChild("leaf8Head");
        this.bigLeaf1Base = root.getChild("bigLeaf1Base");
        this.bigLeaf1End = this.bigLeaf1Base.getChild("bigLeaf1End");
        this.bigLeaf2Base = root.getChild("bigLeaf2Base");
        this.bigLeaf2End = this.bigLeaf2Base.getChild("bigLeaf2End");
        this.bigLeaf3Base = root.getChild("bigLeaf3Base");
        this.bigLeaf3End = this.bigLeaf3Base.getChild("bigLeaf3End");
        this.bigLeaf4Base = root.getChild("bigLeaf4Base");
        this.bigLeaf4End = this.bigLeaf4Base.getChild("bigLeaf4End");
        this.idleAnimation = FoliaathAnimations.idle.bake(root);
        this.attackAnimation = FoliaathAnimations.attack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // stem1Joint: empty pivot at ground level (0, 26, 0)
        // Original: (0, 24, 0) + rotPointY += 2 => (0, 26, 0), rotX += 0.05 => 0.05
        PartDefinition stem1Joint = partdefinition.addOrReplaceChild("stem1Joint",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0f, 26.0f, 0.0f, 0.05f, (float) Math.PI, 0.0f));

        // stem1Base: main stalk (3x15x3), child of stem1Joint
        // Original: rotPoint(0, 24, 0) then set to (0, 0, 0) by line 228
        // rotX = 0.136659 + 0.3 (line 225 stem2.rotateAngleX += 0.3 -- but that's stem2; stem1Base's own adjustments: none extra beyond stem1Joint)
        // Actually line 225-227 adjust stem2, stem3, headBase -- not stem1Base directly
        // stem1Base.setRotationPoint(0, 0, 0) at line 228
        PartDefinition stem1Base = stem1Joint.addOrReplaceChild("stem1Base",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -15.0f, -1.5f, 3.0f, 15.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.136659280431156f, 0.0f, 0.0f));

        // stem2: second stalk segment (3x15x3), child of stem1Base
        // rotX = 0.36425 + 0.3 = 0.66425
        PartDefinition stem2 = stem1Base.addOrReplaceChild("stem2",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -15.0f, -1.5f, 3.0f, 15.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -15.0f, 0.0f, 0.66425021489121656f, 0.0f, 0.0f));

        // stem3: third stalk segment (3x13x3), child of stem2
        // rotX = -1.1383 + (-0.1) = -1.2383
        PartDefinition stem3 = stem2.addOrReplaceChild("stem3",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -13.0f, -1.5f, 3.0f, 13.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -15.0f, 0.0f, -1.2383037381507017f, 0.0f, 0.0f));

        // stem4: top stalk segment (3x10x3), child of stem3
        PartDefinition stem4 = stem3.addOrReplaceChild("stem4",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -10.0f, -1.5f, 3.0f, 10.0f, 3.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -13.0f, 0.0f, -0.9105382707654417f, 0.0f, 0.0f));

        // headBase: head joint (6x6x2), child of stem4
        // rotX = 1.3659 + (-0.35) = 1.0159
        PartDefinition headBase = stem4.addOrReplaceChild("headBase",
            CubeListBuilder.create().texOffs(80, 15).addBox(-3.0f, -3.0f, 0.0f, 6.0f, 6.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -10.0f, 0.0f, 1.0158946726107624f, 0.0f, 0.0f));

        // mouthTop1: upper jaw (12x4x12), child of headBase
        PartDefinition mouthTop1 = headBase.addOrReplaceChild("mouthTop1",
            CubeListBuilder.create().texOffs(16, 0).addBox(-6.0f, -4.0f, 0.0f, 12.0f, 4.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, -2.0f, 2.0f));

        // teethTop1: upper teeth (12x3x12), child of mouthTop1
        PartDefinition teethTop1 = mouthTop1.addOrReplaceChild("teethTop1",
            CubeListBuilder.create().texOffs(80, 0).addBox(-6.0f, 0.0f, 0.0f, 12.0f, 3.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // mouthTop2: upper jaw tip (6x2x7), child of mouthTop1
        PartDefinition mouthTop2 = mouthTop1.addOrReplaceChild("mouthTop2",
            CubeListBuilder.create().texOffs(36, 16).addBox(-3.0f, -2.0f, 0.0f, 6.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 12.0f));

        // teethTop2: upper tip teeth (6x3x7), child of mouthTop2
        PartDefinition teethTop2 = mouthTop2.addOrReplaceChild("teethTop2",
            CubeListBuilder.create().texOffs(15, 22).addBox(-3.0f, 0.0f, 0.0f, 6.0f, 3.0f, 7.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // mouthBottom1: lower jaw (12x4x12), child of headBase, flipped (PI on Z)
        PartDefinition mouthBottom1 = headBase.addOrReplaceChild("mouthBottom1",
            CubeListBuilder.create().texOffs(16, 0).addBox(-6.0f, -4.0f, 0.0f, 12.0f, 4.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 1.0f, 2.0f, 0.0f, 0.0f, 3.141592653589793f));

        // teethBottom1: lower teeth (12x3x12), child of mouthBottom1
        PartDefinition teethBottom1 = mouthBottom1.addOrReplaceChild("teethBottom1",
            CubeListBuilder.create().texOffs(80, 0).addBox(-6.0f, 0.0f, 0.0f, 12.0f, 3.0f, 12.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // mouthBottom2: lower jaw tip (6x2x7), child of mouthBottom1
        PartDefinition mouthBottom2 = mouthBottom1.addOrReplaceChild("mouthBottom2",
            CubeListBuilder.create().texOffs(36, 16).addBox(-3.0f, -2.0f, 0.0f, 6.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 12.0f));

        // teethBottom2: lower tip teeth (6x3x7), child of mouthBottom2
        PartDefinition teethBottom2 = mouthBottom2.addOrReplaceChild("teethBottom2",
            CubeListBuilder.create().texOffs(15, 22).addBox(-3.0f, 0.0f, 0.0f, 6.0f, 3.0f, 7.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 0.0f));

        // mouthBack: back plate (12x9x2), child of headBase
        PartDefinition mouthBack = headBase.addOrReplaceChild("mouthBack",
            CubeListBuilder.create().texOffs(54, 37).addBox(-6.0f, -4.5f, 0.0f, 12.0f, 9.0f, 2.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, -0.5f, 2.0f));

        // tongue1Base: base tongue segment (6x2x6), child of headBase
        PartDefinition tongue1Base = headBase.addOrReplaceChild("tongue1Base",
            CubeListBuilder.create().texOffs(40, 26).addBox(-3.0f, -1.0f, 0.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 2.0f));

        // tongue2: mid tongue segment (6x2x6), child of tongue1Base
        PartDefinition tongue2 = tongue1Base.addOrReplaceChild("tongue2",
            CubeListBuilder.create().texOffs(40, 26).addBox(-3.0f, -1.0f, 0.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 6.0f));

        // tongue3: tip tongue segment (4x2x5), child of tongue2
        PartDefinition tongue3 = tongue2.addOrReplaceChild("tongue3",
            CubeListBuilder.create().texOffs(80, 24).addBox(-2.0f, -1.0f, 0.0f, 4.0f, 2.0f, 5.0f, new CubeDeformation(0.0f)),
            PartPose.offset(0.0f, 0.0f, 6.0f));

        // 8 leaf planes around the head, each (7x19x0), child of headBase
        // All share rotPoint (0, 0, 2), rotX = 0.6829, differ by Z rotation (0, 45, 90, 135, 180, 225, 270, 315 degrees)
        PartDefinition leaf1Head = headBase.addOrReplaceChild("leaf1Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 0.0f));

        PartDefinition leaf2Head = headBase.addOrReplaceChild("leaf2Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 0.7853981633974483f));

        PartDefinition leaf3Head = headBase.addOrReplaceChild("leaf3Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 1.5707963267948966f));

        PartDefinition leaf4Head = headBase.addOrReplaceChild("leaf4Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 2.356194490192345f));

        PartDefinition leaf5Head = headBase.addOrReplaceChild("leaf5Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 3.141592653589793f));

        PartDefinition leaf6Head = headBase.addOrReplaceChild("leaf6Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 3.9269908169872414f));

        PartDefinition leaf7Head = headBase.addOrReplaceChild("leaf7Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 4.71238898038469f));

        PartDefinition leaf8Head = headBase.addOrReplaceChild("leaf8Head",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5f, -19.0f, 0.0f, 7.0f, 19.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.6829473363053812f, 0.0f, 5.497787143782138f));

        // 4 big ground leaves, each (8x14x0), root children at ground level
        // bigLeaf1Base: Y=24, rotX=-0.6829, rotY=PI/4
        PartDefinition bigLeaf1Base = partdefinition.addOrReplaceChild("bigLeaf1Base",
            CubeListBuilder.create().texOffs(64, 14).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, -0.6829473363053812f, 0.7853981633974483f, 0.0f));

        PartDefinition bigLeaf1End = bigLeaf1Base.addOrReplaceChild("bigLeaf1End",
            CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -14.0f, 0.0f, -1.2292353921796064f, 0.0f, 0.0f));

        // bigLeaf2Base: rotY=3PI/4
        PartDefinition bigLeaf2Base = partdefinition.addOrReplaceChild("bigLeaf2Base",
            CubeListBuilder.create().texOffs(64, 14).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, -0.6829473363053812f, 2.356194490192345f, 0.0f));

        PartDefinition bigLeaf2End = bigLeaf2Base.addOrReplaceChild("bigLeaf2End",
            CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -14.0f, 0.0f, -1.2292353921796064f, 0.0f, 0.0f));

        // bigLeaf3Base: rotY=5PI/4
        PartDefinition bigLeaf3Base = partdefinition.addOrReplaceChild("bigLeaf3Base",
            CubeListBuilder.create().texOffs(64, 14).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, -0.6829473363053812f, 3.9269908169872414f, 0.0f));

        PartDefinition bigLeaf3End = bigLeaf3Base.addOrReplaceChild("bigLeaf3End",
            CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -14.0f, 0.0f, -1.2292353921796064f, 0.0f, 0.0f));

        // bigLeaf4Base: rotY=7PI/4
        PartDefinition bigLeaf4Base = partdefinition.addOrReplaceChild("bigLeaf4Base",
            CubeListBuilder.create().texOffs(64, 14).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, -0.6829473363053812f, 5.497787143782138f, 0.0f));

        PartDefinition bigLeaf4End = bigLeaf4Base.addOrReplaceChild("bigLeaf4End",
            CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, -14.0f, 0.0f, 8.0f, 14.0f, 0.0f, new CubeDeformation(0.0f)),
            PartPose.offsetAndRotation(0.0f, -14.0f, 0.0f, -1.2292353921796064f, 0.0f, 0.0f));

        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)128, (int)64);
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
