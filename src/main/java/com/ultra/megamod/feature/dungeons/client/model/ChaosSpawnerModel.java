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
 *  net.minecraft.util.Mth
 */
package com.ultra.megamod.feature.dungeons.client.model;

import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.client.model.animations.ChaosSpawnerAnimations;
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
import net.minecraft.util.Mth;

public class ChaosSpawnerModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"chaos_spawner"), "main");
    public final ModelPart shockwave;
    public final ModelPart body;
    public final ModelPart chaos_hexahedron;
    public final ModelPart head;
    public final ModelPart lower_jaw;
    public final ModelPart chain;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public ChaosSpawnerModel(ModelPart root) {
        super(root);
        this.shockwave = root.getChild("shockwave");
        this.body = root.getChild("body");
        this.chaos_hexahedron = this.body.getChild("chaos_hexahedron");
        this.head = this.body.getChild("head");
        this.lower_jaw = this.body.getChild("lower_jaw");
        this.chain = this.body.getChild("chain");
        this.idleAnimation = ChaosSpawnerAnimations.idle.bake(root);
        this.attackAnimation = ChaosSpawnerAnimations.attack.bake(root);
        this.deathAnimation = ChaosSpawnerAnimations.death.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition shockwave = partdefinition.addOrReplaceChild("shockwave", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)31.0f, (float)0.0f));

        PartDefinition wave4 = shockwave.addOrReplaceChild("wave4", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)0.0f, (float)-28.0f));

        PartDefinition cube_r1 = wave4.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(-8, 62).addBox(-11.0f, 0.0f, 20.0f, 22.0f, 0.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)24.0f, (float)0.0f, (float)3.1416f, (float)0.0f));

        PartDefinition wave3 = shockwave.addOrReplaceChild("wave3", CubeListBuilder.create(), PartPose.offset((float)28.0f, (float)0.0f, (float)0.0f));

        PartDefinition cube_r2 = wave3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(-8, 62).addBox(-11.0f, 0.0f, 28.0f, 22.0f, 0.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-32.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)1.5708f, (float)0.0f));

        PartDefinition wave2 = shockwave.addOrReplaceChild("wave2", CubeListBuilder.create(), PartPose.offset((float)-28.0f, (float)0.0f, (float)0.0f));

        PartDefinition cube_r3 = wave2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(-8, 62).addBox(-15.0f, 0.0f, 24.0f, 22.0f, 0.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)28.0f, (float)0.0f, (float)4.0f, (float)0.0f, (float)-1.5708f, (float)0.0f));

        PartDefinition wave1 = shockwave.addOrReplaceChild("wave1", CubeListBuilder.create().texOffs(-8, 62).addBox(-11.0f, 0.0f, -4.0f, 22.0f, 0.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.0f, (float)28.0f));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)3.0f, (float)0.0f));

        PartDefinition chaos_hexahedron = body.addOrReplaceChild("chaos_hexahedron", CubeListBuilder.create().texOffs(32, 99).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)10.0f, (float)0.0f));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(48, 40).addBox(-7.6659f, -4.103f, -15.8096f, 16.0f, 4.0f, 16.0f, new CubeDeformation(0.0f))
                .texOffs(0, 0).addBox(-10.6659f, -17.103f, -18.8096f, 22.0f, 14.0f, 22.0f, new CubeDeformation(0.0f))
                .texOffs(14, 99).addBox(-1.6659f, -15.103f, -19.8096f, 4.0f, 4.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-0.3341f, (float)10.103f, (float)8.8096f));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 99).addBox(17.5f, -0.5f, -0.5f, 2.0f, 5.0f, 5.0f, new CubeDeformation(0.0f))
                .texOffs(0, 81).addBox(-17.5f, -2.5f, -2.5f, 35.0f, 9.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-0.1659f, (float)-11.603f, (float)-10.3096f, (float)0.0f, (float)-0.2182f, (float)-0.1309f));

        PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 99).addBox(-1.0f, -2.5f, -2.5f, 2.0f, 5.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-18.241f, (float)-7.2061f, (float)-12.3612f, (float)1.5708f, (float)0.2182f, (float)3.0107f));

        PartDefinition lower_jaw = body.addOrReplaceChild("lower_jaw", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0f, 0.0f, -16.0f, 16.0f, 4.0f, 16.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)10.0f, (float)9.0f));

        PartDefinition chain = body.addOrReplaceChild("chain", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)21.0f, (float)0.0f));

        PartDefinition cube_r6 = chain.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 109).addBox(-5.0f, -1.5f, 0.0f, 10.0f, 3.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-24.3827f, (float)-17.3385f, (float)-4.8502f, (float)0.0f, (float)0.2182f, (float)3.0107f));

        PartDefinition cube_r7 = chain.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 109).addBox(19.5f, 0.5f, 2.0f, 10.0f, 3.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-0.5f, (float)-22.5f, (float)-1.5f, (float)0.0f, (float)-0.2182f, (float)-0.1309f));

        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)128, (int)128);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonBossRenderState) {
            DungeonEntityRenderers.DungeonBossRenderState bossState = (DungeonEntityRenderers.DungeonBossRenderState)state;
            this.idleAnimation.apply(bossState.idleAnimationState, state.ageInTicks);
            this.attackAnimation.apply(bossState.attackAnimationState, state.ageInTicks);
            this.deathAnimation.apply(bossState.deathAnimationState, state.ageInTicks);
        }
        // Floating idle bob — sinusoidal Y offset on body
        float ageInSeconds = state.ageInTicks / 20.0f;
        float bobRate = Mth.sin(ageInSeconds * Mth.TWO_PI / 2.0f);
        this.body.y += bobRate;
    }
}
