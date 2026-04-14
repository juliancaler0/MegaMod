package com.ultra.megamod.reliquary.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.reliquary.client.gui.components.Component;
import com.ultra.megamod.reliquary.client.gui.components.ItemStackCountPane;
import com.ultra.megamod.reliquary.reference.Colors;
import com.ultra.megamod.reliquary.util.InventoryHelper;

import java.util.function.Function;

public class ChargePane extends Component {
	private final Item mainItem;
	private final ItemStackCountPane chargeablePane;
	private final Function<ItemStack, Integer> getCount;

	public ChargePane(Item mainItem, ItemStack chargeItem, Function<ItemStack, Integer> getCount) {
		this(mainItem, chargeItem, getCount, Colors.get(Colors.PURE));
	}

	public ChargePane(Item mainItem, ItemStack chargeItem, Function<ItemStack, Integer> getCount, int textColor) {
		this.mainItem = mainItem;
		this.getCount = getCount;

		chargeablePane = new ItemStackCountPane(chargeItem, 0, textColor);
	}

	@Override
	public int getHeightInternal() {
		return chargeablePane.getHeight();
	}

	@Override
	public int getWidthInternal() {
		return chargeablePane.getWidth();
	}

	@Override
	public int getPadding() {
		return chargeablePane.getPadding();
	}

	@Override
	public void renderInternal(GuiGraphics guiGraphics, int x, int y) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		ItemStack itemStack = InventoryHelper.getCorrectItemFromEitherHand(player, mainItem);

		if (itemStack.isEmpty()) {
			return;
		}

		chargeablePane.setCount(getCount.apply(itemStack));
		chargeablePane.render(guiGraphics, x, y);
	}
}
