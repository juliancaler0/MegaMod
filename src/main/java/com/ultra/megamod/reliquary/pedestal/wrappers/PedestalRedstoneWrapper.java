package com.ultra.megamod.reliquary.pedestal.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.ultra.megamod.reliquary.api.IPedestal;
import com.ultra.megamod.reliquary.api.IPedestalRedstoneItemWrapper;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.WorldHelper;

import java.util.List;

public class PedestalRedstoneWrapper implements IPedestalRedstoneItemWrapper {
	private final boolean powered;

	public static class AlwaysOn extends PedestalRedstoneWrapper {
		public AlwaysOn() {
			super(true);
		}
	}

	public static class Toggleable extends PedestalRedstoneWrapper {
		public Toggleable() {
			super(false);
		}
	}

	private PedestalRedstoneWrapper(boolean powered) {
		this.powered = powered;
	}

	@Override
	public void updateRedstone(ItemStack stack, Level level, IPedestal pedestal) {
		List<BlockPos> pedestalsInRange = pedestal.getPedestalsInRange(level, Config.COMMON.blocks.pedestal.redstoneWrapperRange.get());
		BlockPos thisPos = pedestal.getBlockPosition();

		boolean buttonEnabled = pedestal.switchedOn();

		for (BlockPos pos : pedestalsInRange) {
			if (pos.equals(thisPos)) {
				continue;
			}

			WorldHelper.getBlockEntity(level, pos, IPedestal.class).ifPresent(pedestalInRange -> {
				if (powered || buttonEnabled || level.hasNeighborSignal(pedestal.getBlockPosition())) {
					pedestalInRange.switchOn(level, thisPos);
				} else {
					pedestalInRange.switchOff(level, thisPos);
				}
			});
		}
	}

	@Override
	public void onRemoved(ItemStack stack, Level level, IPedestal pedestal) {
		List<BlockPos> pedestalsInRange = pedestal.getPedestalsInRange(level, Config.COMMON.blocks.pedestal.redstoneWrapperRange.get());
		BlockPos thisPos = pedestal.getBlockPosition();

		for (BlockPos pos : pedestalsInRange) {
			IPedestal ped = (IPedestal) level.getBlockEntity(pos);
			if (ped != null) {
				ped.switchOff(level, thisPos);
			}
		}
	}
}
