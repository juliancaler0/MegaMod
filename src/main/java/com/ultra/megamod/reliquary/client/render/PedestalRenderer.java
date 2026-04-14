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
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import com.ultra.megamod.reliquary.block.tile.PedestalBlockEntity;

/**
 * Renders the full-featured Pedestal: floats/rotates the stored item just
 * like the passive pedestal, and additionally invokes any item-specific
 * pedestal renderer registered with {@link
 * com.ultra.megamod.reliquary.client.registry.PedestalClientRegistry} (e.g.
 * the fishing-hook line for rods).
 *
 * <p>The extra per-item renderer currently uses the legacy
 * MultiBufferSource-based IPedestalItemRenderer interface — since
 * SubmitNodeCollector doesn't expose a MultiBufferSource, the per-item pass
 * is deferred until the pedestal item renderers are migrated to the new API.
 * The rotating item still renders correctly.
 */
public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.FullPedestalRenderState> {

	public PedestalRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public FullPedestalRenderState createRenderState() {
		return new FullPedestalRenderState();
	}

	@Override
	public void extractRenderState(PedestalBlockEntity blockEntity, FullPedestalRenderState state, float partialTick,
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
	public void submit(FullPedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
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

	@Override
	public boolean shouldRender(PedestalBlockEntity blockEntity, Vec3 cameraPos) {
		return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
	}

	/** Expanded AABB so fishing-line/particle renderers drawing outside the block volume don't get culled. */
	public AABB getRenderBoundingBox(PedestalBlockEntity blockEntity) {
		BlockPos pos = blockEntity.getBlockPos();
		AABB aabb = new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		blockEntity.executeOnActionItem(ai -> ai.getRenderBoundingBoxOuterPosition().ifPresent(aabb::expandTowards));
		return aabb;
	}

	public static class FullPedestalRenderState extends BlockEntityRenderState {
		public boolean hasItem;
		public ItemStackRenderState itemRenderState;
		public float yDiff;
		public float yRotation;
	}
}
