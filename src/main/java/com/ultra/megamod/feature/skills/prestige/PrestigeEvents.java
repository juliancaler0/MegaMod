package com.ultra.megamod.feature.skills.prestige;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/// Applies prestige attribute modifiers at login and persists the file on shutdown.
@EventBusSubscriber(modid = MegaMod.MODID)
public class PrestigeEvents {

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer sp)) return;
		ServerLevel level = (ServerLevel) sp.level();
		PrestigeManager.get(level).reapplyModifiers(sp);
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		ServerLevel overworld = event.getServer().overworld();
		PrestigeManager mgr = PrestigeManager.get(overworld);
		mgr.saveToDisk(overworld);
		PrestigeManager.reset();
	}
}
