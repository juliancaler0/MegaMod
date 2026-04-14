package com.ultra.megamod.reliquary.client.init;

import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import com.ultra.megamod.reliquary.block.tile.ApothecaryCauldronBlockEntity;
import com.ultra.megamod.reliquary.init.ModBlocks;

public class ModBlockColors {
	private ModBlockColors() {
	}

	public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
		event.register((state, level, pos, tintIndex) -> {
			if (level != null && pos != null && level.getBlockEntity(pos) instanceof ApothecaryCauldronBlockEntity cauldron && cauldron.getLiquidLevel() > 0) {
				return cauldron.getColorMultiplier();
			}
			return -1;
		}, ModBlocks.APOTHECARY_CAULDRON.get());
	}
}
