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
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.ultra.megamod.reliquary.block.ApothecaryMortarBlock;
import com.ultra.megamod.reliquary.block.tile.ApothecaryMortarBlockEntity;

/**
 * Renders the three ingredient items floating inside an Apothecary Mortar,
 * tilted toward the mortar's center. Ported onto 1.21.11's render-state +
 * SubmitNodeCollector pipeline.
 */
public class ApothecaryMortarRenderer implements BlockEntityRenderer<ApothecaryMortarBlockEntity, ApothecaryMortarRenderer.MortarRenderState> {

	public ApothecaryMortarRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public MortarRenderState createRenderState() {
		return new MortarRenderState();
	}

	@Override
	public void extractRenderState(ApothecaryMortarBlockEntity blockEntity, MortarRenderState state, float partialTick,
								   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);

		Direction direction = blockEntity.getBlockState().getValue(ApothecaryMortarBlock.FACING);
		state.horizontalRotation = direction == Direction.UP ? 0F : direction.get2DDataValue() * 90F;

		NonNullList<ItemStack> mortarItems = blockEntity.getItemStacks();
		Minecraft mc = Minecraft.getInstance();
		for (int i = 0; i < 3 && i < mortarItems.size(); i++) {
			ItemStack stack = mortarItems.get(i);
			if (!stack.isEmpty()) {
				ItemStackRenderState renderState = new ItemStackRenderState();
				mc.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.GROUND, mc.level, null, 0);
				state.items[i] = renderState;
			} else {
				state.items[i] = null;
			}
		}
	}

	@Override
	public void submit(MortarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.3D, 0.5D);
		poseStack.mulPose(Axis.YN.rotationDegrees(state.horizontalRotation));

		if (state.items[0] != null) {
			renderMortarItem(poseStack, collector, state.lightCoords, () -> {
				poseStack.mulPose(Axis.ZP.rotationDegrees(40F));
				poseStack.mulPose(Axis.YP.rotationDegrees(90F));
			}, state.items[0], -0.09F, 0F);
		}

		if (state.items[1] != null) {
			renderMortarItem(poseStack, collector, state.lightCoords, () -> {
				poseStack.mulPose(Axis.XP.rotationDegrees(40F));
				poseStack.mulPose(Axis.YP.rotationDegrees(180F));
			}, state.items[1], 0F, 0.09F);
		}

		if (state.items[2] != null) {
			renderMortarItem(poseStack, collector, state.lightCoords, () -> {
				poseStack.mulPose(Axis.ZN.rotationDegrees(40F));
				poseStack.mulPose(Axis.YP.rotationDegrees(270F));
			}, state.items[2], 0.09F, 0F);
		}

		poseStack.popPose();
	}

	private void renderMortarItem(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, Runnable applyRotations, ItemStackRenderState renderState, float translateX, float translateZ) {
		poseStack.pushPose();
		poseStack.translate(translateX, 0F, translateZ);
		applyRotations.run();
		poseStack.scale(0.60F, 0.60F, 0.60F);
		renderState.submit(poseStack, collector, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0);
		poseStack.popPose();
	}

	public static class MortarRenderState extends BlockEntityRenderState {
		public float horizontalRotation;
		public final ItemStackRenderState[] items = new ItemStackRenderState[3];
	}
}
