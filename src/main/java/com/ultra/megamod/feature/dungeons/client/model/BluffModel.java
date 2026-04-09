package com.ultra.megamod.feature.dungeons.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class BluffModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("megamod", "bluff"), "main");

    private final ModelPart body;
    private final ModelPart rightFin;
    private final ModelPart leftFin;
    private final ModelPart tail;

    public BluffModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.rightFin = root.getChild("right_fin");
        this.leftFin = root.getChild("left_fin");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.offset(0.0f, 20.0f, 0.0f));

        root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-1.0f, -1.0f, -1.0f, 1.0f, 3.0f, 2.0f),
                PartPose.offset(-4.0f, 20.0f, 0.0f));

        root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(24, 0).addBox(0.0f, -1.0f, -1.0f, 1.0f, 3.0f, 2.0f).mirror(),
                PartPose.offset(4.0f, 20.0f, 0.0f));

        root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(24, 5).addBox(-2.0f, -2.0f, 0.0f, 4.0f, 4.0f, 3.0f),
                PartPose.offset(0.0f, 20.0f, 4.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float bob = (float) Math.sin(state.ageInTicks * 0.15f) * 0.1f;
        this.body.y = 20.0f + bob * 3.0f;
        float finFlap = (float) Math.sin(state.ageInTicks * 0.3f) * 0.3f;
        this.rightFin.zRot = -finFlap;
        this.leftFin.zRot = finFlap;
    }
}
