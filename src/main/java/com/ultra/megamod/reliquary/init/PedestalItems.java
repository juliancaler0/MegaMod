package com.ultra.megamod.reliquary.init;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import com.ultra.megamod.reliquary.item.HarvestRodItem;
import com.ultra.megamod.reliquary.item.RendingGaleItem;
import com.ultra.megamod.reliquary.item.RodOfLyssaItem;
import com.ultra.megamod.reliquary.pedestal.PedestalRegistry;
import com.ultra.megamod.reliquary.pedestal.wrappers.*;

public class PedestalItems {
	private PedestalItems() {}

	public static void init() {
		// TODO: 1.21.11 port - SwordItem class was removed; the "is a sword" check is now
		// data-component based (Items' TOOL / WEAPON components). Sword pedestal wrapper
		// registration is skipped until we find the right analog.
		PedestalRegistry.registerItemWrapper(BucketItem.class, PedestalBucketWrapper::new);
		PedestalRegistry.registerItemWrapper(ShearsItem.class, PedestalShearsWrapper::new);
		PedestalRegistry.registerItemWrapper(RendingGaleItem.class, PedestalRendingGaleWrapper::new);
		PedestalRegistry.registerItemWrapper(HarvestRodItem.class, PedestalHarvestRodWrapper::new);
		PedestalRegistry.registerItemBlockWrapper(RedStoneWireBlock.class, PedestalRedstoneWrapper.Toggleable::new);
		PedestalRegistry.registerItemBlockWrapper(PoweredBlock.class, PedestalRedstoneWrapper.AlwaysOn::new);
		PedestalRegistry.registerItemWrapper(FishingRodItem.class, PedestalFishingRodWrapper::new);
		PedestalRegistry.registerItemWrapper(RodOfLyssaItem.class, PedestalFishingRodWrapper::new);
	}
}
