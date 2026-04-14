package com.ultra.megamod.reliquary.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import com.ultra.megamod.reliquary.Reliquary;

/**
 * Belt geometry rendered over the player model when an accessories/curios
 * layer slot has a MobCharmBelt equipped. In 1.21.11 {@link HumanoidModel}
 * is generic over a {@link HumanoidRenderState} and {@code renderToBuffer}
 * is final on the Model base class — so the body-only visibility toggle is
 * done inside {@link #setupAnim} instead of overriding renderToBuffer.
 *
 * <p>The MegaMod accessories runtime owns the actual layer hook — this file
 * only carries the mesh + a convenience layer location constant.
 */
public class MobCharmBeltModel extends HumanoidModel<HumanoidRenderState> {
	public static final ModelLayerLocation MOB_CHARM_BELT_LAYER = new ModelLayerLocation(Reliquary.getRL("mob_charm_belt"), "main");

	public MobCharmBeltModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);
		body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -15.0F, -2.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.2F)).texOffs(8, 5).addBox(-1.5F, -16.0F, -2.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 8).addBox(-1.0F, -15.5F, 1.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(4, 5).addBox(-3.5F, -15.5F, -2.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 5).addBox(2.5F, -15.5F, -2.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(HumanoidRenderState state) {
		super.setupAnim(state);
		setAllVisible(false);
		body.visible = true;
	}
}
