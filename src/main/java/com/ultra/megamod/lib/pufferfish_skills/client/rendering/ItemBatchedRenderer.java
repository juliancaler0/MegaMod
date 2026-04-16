package com.ultra.megamod.lib.pufferfish_skills.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBatchedRenderer {

	private final Map<ComparableItemStack, List<int[]>> batch = new HashMap<>();

	public static List<Matrix4f> EMITS;

	public void emitItem(GuiGraphics context, ItemStack item, int x, int y) {
		var emits = batch.computeIfAbsent(
				new ComparableItemStack(item),
				key -> new ArrayList<>()
		);
		emits.add(new int[]{x, y});
	}

	public void draw(GuiGraphics context) {
		var client = Minecraft.getInstance();

		for (var entry : batch.entrySet()) {
			var itemStack = entry.getKey().itemStack;

			for (var pos : entry.getValue()) {
				context.renderItem(itemStack, pos[0], pos[1]);
			}
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
