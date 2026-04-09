package com.ultra.megamod.feature.dungeons.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class BabyFoliaathModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("megamod", "baby_foliaath"), "main");

    private final ModelPart stem;
    private final ModelPart head;
    private final ModelPart jaw;

    public BabyFoliaathModel(ModelPart root) {
        super(root);
        this.stem = root.getChild("stem");
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("stem", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0f, -8.0f, -1.0f, 2.0f, 8.0f, 2.0f),
                PartPose.offset(0.0f, 24.0f, 0.0f));

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-3.0f, -4.0f, -3.0f, 6.0f, 4.0f, 6.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        root.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(8, 10).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 2.0f, 6.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        // Simple head sway
        float sway = (float) Math.sin(state.ageInTicks * 0.1f) * 0.15f;
        this.head.yRot = sway;
        // Jaw opens and closes slowly
        float jawOpen = Math.max(0, (float) Math.sin(state.ageInTicks * 0.08f)) * 0.2f;
        this.jaw.xRot = jawOpen;
    }
}
