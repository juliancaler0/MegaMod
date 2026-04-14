package com.ultra.megamod.reliquary.item.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import com.ultra.megamod.reliquary.init.ModBlocks;
import com.ultra.megamod.reliquary.item.ICreativeTabItemGenerator;

import java.util.function.Consumer;

public class InterdictionTorchItem extends StandingAndWallBlockItem implements ICreativeTabItemGenerator {
	public InterdictionTorchItem(Item.Properties properties) {
		super(ModBlocks.INTERDICTION_TORCH.get(), ModBlocks.WALL_INTERDICTION_TORCH.get(), Direction.DOWN, properties);
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (getBlock() instanceof ICreativeTabItemGenerator creativeTabItemGenerator) {
			creativeTabItemGenerator.addCreativeTabItems(itemConsumer);
		}
	}
}
