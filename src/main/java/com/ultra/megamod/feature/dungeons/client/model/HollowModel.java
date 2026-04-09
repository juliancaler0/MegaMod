/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
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

public class HollowModel
extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath((String)"megamod", (String)"hollow"), "main");
    public final ModelPart right_hand;
    public final ModelPart left_hand;
    public final ModelPart head;

    public HollowModel(ModelPart root) {
        super(root);
        this.right_hand = root.getChild("right_hand");
        this.left_hand = root.getChild("left_hand");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition right_hand = partdefinition.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 10).addBox(-10.0f, -10.0f, -5.0f, 3.0f, 4.0f, 3.0f, new CubeDeformation(0.0f)), PartPose.offset((float)4.0f, (float)24.0f, (float)-4.0f));

        PartDefinition left_hand = partdefinition.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(40, 0).addBox(-1.0f, -12.0f, -6.0f, 4.0f, 5.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset((float)4.0f, (float)24.0f, (float)-4.0f));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset((float)4.0f, (float)24.0f, (float)-4.0f));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -6.5f, -5.0f, 10.0f, 13.0f, 10.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-4.0f, (float)-8.5f, (float)4.0f, (float)0.0f, (float)1.5708f, (float)0.0f));

        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        // Sinusoidal bobbing on head and hands with different phases
        float f = state.ageInTicks * 0.1f;
        float f1 = Mth.sin(f);
        float f2 = Mth.cos(f);
        float f3 = Mth.cos(f + Mth.PI * 0.5f);
        this.head.y += 2.0f * f1;
        this.left_hand.y += 2.0f * f2;
        this.right_hand.y += 2.0f * f3;
    }
}
