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
import com.ultra.megamod.feature.dungeons.client.model.animations.SpawnerCarrierAnimations;
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

public class SpawnerCarrierModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"spawner_carrier"), "main");
    public final ModelPart leg;
    public final ModelPart leg2;
    public final ModelPart leg3;
    public final ModelPart leg4;
    public final ModelPart body;
    public final ModelPart eye;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;

    public SpawnerCarrierModel(ModelPart root) {
        super(root);
        this.leg = root.getChild("leg");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.body = root.getChild("body");
        this.eye = root.getChild("eye");
        this.walkAnimation = SpawnerCarrierAnimations.walk.bake(root);
        this.attackAnimation = SpawnerCarrierAnimations.attack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition leg = partdefinition.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(81, 29).addBox(-3.5f, -3.0f, -3.5f, 7.0f, 18.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset((float)12.5f, (float)9.0f, (float)-12.5f));

        PartDefinition leg2 = partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(81, 29).addBox(-3.5f, -3.0f, -3.5f, 7.0f, 18.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset((float)12.5f, (float)9.0f, (float)12.5f));

        PartDefinition leg3 = partdefinition.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(81, 29).addBox(-3.5f, -3.0f, -3.5f, 7.0f, 18.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-12.5f, (float)9.0f, (float)12.5f));

        PartDefinition leg4 = partdefinition.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(81, 29).addBox(-3.5f, -3.0f, -3.5f, 7.0f, 18.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-12.5f, (float)9.0f, (float)-12.5f));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 59).addBox(-8.0f, -8.1667f, -8.0f, 16.0f, 8.0f, 16.0f, new CubeDeformation(0.0f))
                .texOffs(0, 36).addBox(-10.0f, -3.1667f, -10.0f, 20.0f, 3.0f, 20.0f, new CubeDeformation(0.0f))
                .texOffs(0, 0).addBox(-11.0f, -1.1667f, -11.0f, 22.0f, 14.0f, 22.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)3.1667f, (float)0.0f));

        PartDefinition eye = partdefinition.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(24, 105).addBox(-6.5f, -4.5f, -0.5f, 13.0f, 9.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)9.0f, (float)-10.0f));

        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)128, (int)128);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        if (state instanceof DungeonEntityRenderers.DungeonBossRenderState) {
            DungeonEntityRenderers.DungeonBossRenderState bossState = (DungeonEntityRenderers.DungeonBossRenderState)state;
            this.attackAnimation.apply(bossState.attackAnimationState, state.ageInTicks);
        }
        this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 4.0f, 4.5f);
    }
}
