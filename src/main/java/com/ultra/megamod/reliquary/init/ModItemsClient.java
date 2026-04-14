package com.ultra.megamod.reliquary.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.ultra.megamod.reliquary.client.gui.AlkahestryTomeScreen;
import com.ultra.megamod.reliquary.client.gui.MobCharmBeltScreen;

public class ModItemsClient {

	public static void init(IEventBus modBus) {
		modBus.addListener(ModItemsClient::onMenuScreenRegister);
	}

	private static void onMenuScreenRegister(RegisterMenuScreensEvent event) {
		event.register(ModItems.ALKAHEST_TOME_MENU_TYPE.get(), AlkahestryTomeScreen::new);
		event.register(ModItems.MOB_CHAR_BELT_MENU_TYPE.get(), MobCharmBeltScreen::new);
	}
}
