package com.ultra.megamod.reliquary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import com.ultra.megamod.reliquary.entity.shot.ShotBase;

/**
 * Renders {@link ShotBase} projectiles as a billboarded textured quad facing
 * the camera. Ported onto 1.21.11's render-state + SubmitNodeCollector
 * pipeline — vanilla's old render(entity, yaw, partialTicks, pose, buffer,
 * light) signature is gone.
 */
public class ShotRenderer<T extends ShotBase> extends EntityRenderer<T, ShotRenderer.ShotRenderState> {
	public ShotRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ShotRenderState createRenderState() {
		return new ShotRenderState();
	}

	@Override
	public void extractRenderState(T entity, ShotRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.texture = entity.getShotTexture();
	}

	@Override
	public void submit(ShotRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		if (state.texture == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.scale(0.1F, 0.1F, 0.1F);
		// 1.21.11: camera orientation lives on the CameraRenderState, not on EntityRenderDispatcher.
		if (cameraState.orientation != null) {
			poseStack.mulPose(cameraState.orientation);
		}

		RenderType renderType = RenderTypes.entityCutout(state.texture);
		collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
			Matrix4f matrix = pose.pose();
			addVertex(consumer, pose, matrix, state.lightCoords, -0.5F, -0.25F, 0, 1);
			addVertex(consumer, pose, matrix, state.lightCoords, 0.5F, -0.25F, 1, 1);
			addVertex(consumer, pose, matrix, state.lightCoords, 0.5F, 0.75F, 1, 0);
			addVertex(consumer, pose, matrix, state.lightCoords, -0.5F, 0.75F, 0, 0);
		});

		poseStack.popPose();

		super.submit(state, poseStack, collector, cameraState);
	}

	private static void addVertex(com.mojang.blaze3d.vertex.VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix, int packedLight, float x, float y, int u, int v) {
		consumer.addVertex(matrix, x, y, 0.0F)
				.setColor(255, 255, 255, 255)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(packedLight)
				.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	public static class ShotRenderState extends EntityRenderState {
		public Identifier texture;
	}
}
