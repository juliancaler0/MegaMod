package com.ultra.megamod.reliquary.compat.jade;

import com.ultra.megamod.reliquary.block.AlkahestryAltarBlock;
import com.ultra.megamod.reliquary.block.ApothecaryCauldronBlock;
import com.ultra.megamod.reliquary.block.ApothecaryMortarBlock;
import com.ultra.megamod.reliquary.block.PedestalBlock;
import com.ultra.megamod.reliquary.block.tile.AlkahestryAltarBlockEntity;
import com.ultra.megamod.reliquary.block.tile.ApothecaryMortarBlockEntity;
import com.ultra.megamod.reliquary.compat.jade.provider.DataProviderAltar;
import com.ultra.megamod.reliquary.compat.jade.provider.DataProviderCauldron;
import com.ultra.megamod.reliquary.compat.jade.provider.DataProviderMortar;
import com.ultra.megamod.reliquary.compat.jade.provider.DataProviderPedestal;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(new DataProviderMortar(), ApothecaryMortarBlock.class);
		registration.registerBlockComponent(new DataProviderCauldron(), ApothecaryCauldronBlock.class);
		registration.registerBlockComponent(new DataProviderAltar(), AlkahestryAltarBlock.class);
		registration.registerBlockComponent(new DataProviderPedestal(), PedestalBlock.class);
	}

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(new DataProviderMortar(), ApothecaryMortarBlockEntity.class);
		registration.registerBlockDataProvider(new DataProviderAltar(), AlkahestryAltarBlockEntity.class);
	}
}
