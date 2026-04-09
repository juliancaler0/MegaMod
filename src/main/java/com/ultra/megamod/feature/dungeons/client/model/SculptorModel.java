/*
 * SculptorModel - Earth golem boss. Chunky stone body with mossy shoulders,
 * glowing crack lines, massive fists. Proper animation hierarchy.
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.SculptorAnimations;
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

public class SculptorModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("megamod", "sculptor"), "main");

    public final ModelPart body;
    public final ModelPart chest;
    public final ModelPart head;
    public final ModelPart rightUpperArm;
    public final ModelPart rightLowerArm;
    public final ModelPart rightFist;
    public final ModelPart leftUpperArm;
    public final ModelPart leftLowerArm;
    public final ModelPart leftFist;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public SculptorModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.rightUpperArm = this.chest.getChild("rightUpperArm");
        this.rightLowerArm = this.rightUpperArm.getChild("rightLowerArm");
        this.rightFist = this.rightLowerArm.getChild("rightFist");
        this.leftUpperArm = this.chest.getChild("leftUpperArm");
        this.leftLowerArm = this.leftUpperArm.getChild("leftLowerArm");
        this.leftFist = this.leftLowerArm.getChild("leftFist");
        this.rightLeg = this.body.getChild("rightLeg");
        this.leftLeg = this.body.getChild("leftLeg");
        this.idleAnimation = SculptorAnimations.idle.bake(root);
        this.walkAnimation = SculptorAnimations.walk.bake(root);
        this.attackAnimation = SculptorAnimations.attack.bake(root);
        this.deathAnimation = SculptorAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Body — hip/waist pivot, lower torso (16x10x10)
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-8.0f, -10.0f, -5.0f, 16.0f, 10.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 4.0f, 0.0f));

        // Chest — upper torso, broad and heavy (20x14x12)
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-10.0f, -14.0f, -6.0f, 20.0f, 14.0f, 12.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -10.0f, 0.0f));

        // Shoulder boulders (mossy rock masses on top of shoulders)
        chest.addOrReplaceChild("rightShoulder", CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-5.0f, -5.0f, -4.0f, 8.0f, 6.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-9.0f, -12.0f, 0.0f, 0.0f, 0.0f, 0.1745f));

        chest.addOrReplaceChild("leftShoulder", CubeListBuilder.create()
                        .texOffs(64, 0).mirror().addBox(-3.0f, -5.0f, -4.0f, 8.0f, 6.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offsetAndRotation(9.0f, -12.0f, 0.0f, 0.0f, 0.0f, -0.1745f));

        // Back ridge (jagged stone ridge down the spine)
        chest.addOrReplaceChild("backRidge", CubeListBuilder.create()
                        .texOffs(64, 14).addBox(-3.0f, -4.0f, 0.0f, 6.0f, 8.0f, 4.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(0.0f, -10.0f, 6.0f, 0.2618f, 0.0f, 0.0f));

        // Head — angular stone skull (10x8x10), sits in neck groove
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-5.0f, -8.0f, -5.0f, 10.0f, 8.0f, 10.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -14.0f, 0.0f));

        // Jaw — angular lower jaw
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(40, 60).addBox(-4.0f, 0.0f, -5.0f, 8.0f, 3.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -1.0f, 0.0f));

        // Brow ridge — jutting stone brow
        head.addOrReplaceChild("brow", CubeListBuilder.create()
                        .texOffs(40, 69).addBox(-6.0f, -2.0f, -1.0f, 12.0f, 3.0f, 2.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, -7.0f, -5.0f));

        // Right arm chain: upperArm (6x12x6) → lowerArm (5x10x5) → fist (7x6x7)
        PartDefinition rightUpperArm = chest.addOrReplaceChild("rightUpperArm", CubeListBuilder.create()
                        .texOffs(0, 78).addBox(-5.0f, 0.0f, -3.0f, 6.0f, 12.0f, 6.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-10.0f, -12.0f, 0.0f));

        PartDefinition rightLowerArm = rightUpperArm.addOrReplaceChild("rightLowerArm", CubeListBuilder.create()
                        .texOffs(24, 78).addBox(-3.0f, 0.0f, -2.5f, 5.0f, 10.0f, 5.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-2.0f, 12.0f, 0.0f));

        PartDefinition rightFist = rightLowerArm.addOrReplaceChild("rightFist", CubeListBuilder.create()
                        .texOffs(44, 78).addBox(-4.0f, 0.0f, -3.5f, 7.0f, 6.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 10.0f, 0.0f));

        // Left arm chain (mirrored)
        PartDefinition leftUpperArm = chest.addOrReplaceChild("leftUpperArm", CubeListBuilder.create()
                        .texOffs(0, 78).mirror().addBox(-1.0f, 0.0f, -3.0f, 6.0f, 12.0f, 6.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(10.0f, -12.0f, 0.0f));

        PartDefinition leftLowerArm = leftUpperArm.addOrReplaceChild("leftLowerArm", CubeListBuilder.create()
                        .texOffs(24, 78).mirror().addBox(-2.0f, 0.0f, -2.5f, 5.0f, 10.0f, 5.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(2.0f, 12.0f, 0.0f));

        PartDefinition leftFist = leftLowerArm.addOrReplaceChild("leftFist", CubeListBuilder.create()
                        .texOffs(44, 78).mirror().addBox(-3.0f, 0.0f, -3.5f, 7.0f, 6.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 10.0f, 0.0f));

        // Right leg — thick stone column (7x14x7)
        PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create()
                        .texOffs(72, 78).addBox(-3.5f, 0.0f, -3.5f, 7.0f, 14.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offset(-5.0f, 0.0f, 0.0f));

        // Right foot
        rightLeg.addOrReplaceChild("rightFoot", CubeListBuilder.create()
                        .texOffs(72, 99).addBox(-4.0f, 0.0f, -5.0f, 8.0f, 3.0f, 8.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 14.0f, 0.0f));

        // Left leg (mirrored)
        PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create()
                        .texOffs(72, 78).mirror().addBox(-3.5f, 0.0f, -3.5f, 7.0f, 14.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(5.0f, 0.0f, 0.0f));

        leftLeg.addOrReplaceChild("leftFoot", CubeListBuilder.create()
                        .texOffs(72, 99).mirror().addBox(-4.0f, 0.0f, -5.0f, 8.0f, 3.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.0f, 14.0f, 0.0f));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonBossRenderState bossState) {
            this.idleAnimation.apply(bossState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(bossState.attackAnimationState, state.ageInTicks);
            this.deathAnimation.apply(bossState.deathAnimationState, state.ageInTicks);
        }
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 1.0f, 1.0f);
    }
}
