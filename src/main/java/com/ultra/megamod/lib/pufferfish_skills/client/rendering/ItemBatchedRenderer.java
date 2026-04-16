package com.ultra.megamod.lib.pufferfish_skills.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Lighting;
import net.minecraft.client.renderer.OverlayTexture;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.lib.pufferfish_skills.access.MinecraftClientAccess;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBatchedRenderer {

	private final Map<ComparableItemStack, List<Matrix4f>> batch = new HashMap<>();
	private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

	public static List<Matrix4f> EMITS;

	public void emitItem(GuiGraphics context, ItemStack item, int x, int y) {
		var emits = batch.computeIfAbsent(
				new ComparableItemStack(item),
				key -> new ArrayList<>()
		);

		emits.add(new Matrix4f(
				context.pose().last().pose()
		).translate(x, y, 0));
	}

	public void draw() {
		var client = Minecraft.getInstance();

		var clientAccess = (MinecraftClientAccess) client;
		var immediate = clientAccess.getBufferBuilders().getEntityVertexConsumers();

		immediate.draw();

		var matrices = new MatrixStack();
		matrices.translate(0, 0, 150);
		matrices.multiplyPositionMatrix(new Matrix4f().scaling(1f, -1f, 1f));
		matrices.scale(16f, 16f, 16f);

		for (var entry : batch.entrySet()) {
			var itemStack = entry.getKey().itemStack;

			client.getItemModelResolver().clearAndUpdate(
					itemRenderState,
					itemStack,
					ItemDisplayContext.GUI,
					client.level,
					client.player,
					0
			);

			if (itemRenderState.isSideLit()) {
				Lighting.enableGuiDepthLighting();
			} else {
				Lighting.disableGuiDepthLighting();
			}

			EMITS = entry.getValue();

			itemRenderState.render(
					matrices,
					immediate,
					0xF000F0,
					OverlayTexture.DEFAULT_UV
			);

			immediate.draw();

			EMITS = null;
		}
		batch.clear();
	}

	private record ComparableItemStack(ItemStack itemStack) {
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			return ItemStack.isSameItemSameComponents(this.itemStack, ((ComparableItemStack) o).itemStack);
		}

		@Override
		public int hashCode() {
			return itemStack.getItem().hashCode();
		}
	}
}
