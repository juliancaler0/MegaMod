package com.ultra.megamod.reliquary.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.ultra.megamod.reliquary.block.tile.PassivePedestalBlockEntity;

/**
 * Floats and slow-rotates the held ItemStack above a Passive Pedestal. Ported
 * from the render-to-MultiBufferSource style of the upstream Reliquary ref
 * onto 1.21.11's SubmitNodeCollector pipeline.
 */
public class PassivePedestalRenderer implements BlockEntityRenderer<PassivePedestalBlockEntity, PassivePedestalRenderer.PedestalRenderState> {

	public PassivePedestalRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public PedestalRenderState createRenderState() {
		return new PedestalRenderState();
	}

	@Override
	public void extractRenderState(PassivePedestalBlockEntity blockEntity, PedestalRenderState state, float partialTick,
								   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);

		ItemStack stack = blockEntity.getItem();
		state.hasItem = !stack.isEmpty();
		if (state.hasItem) {
			Minecraft mc = Minecraft.getInstance();
			if (state.itemRenderState == null) {
				state.itemRenderState = new ItemStackRenderState();
			}
			mc.getItemModelResolver().updateForTopItem(state.itemRenderState, stack, ItemDisplayContext.GROUND, mc.level, null, 0);
		}

		long time = System.currentTimeMillis() % 86400000L;
		state.yDiff = Mth.sin(time / 1000F) * 0.1F + 0.1F;
		state.yRotation = (time / 2000F) * (180F / (float) Math.PI);
	}

	@Override
	public void submit(PedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		if (!state.hasItem || state.itemRenderState == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.9D + state.yDiff, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
		poseStack.scale(0.75F, 0.75F, 0.75F);
		state.itemRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	public static class PedestalRenderState extends BlockEntityRenderState {
		public boolean hasItem;
		public ItemStackRenderState itemRenderState;
		public float yDiff;
		public float yRotation;
	}
}
