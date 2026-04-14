package com.ultra.megamod.reliquary.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.util.PlayerInventoryProvider;

/**
 * TODO: once the client-layer agent restores FORTUNE_COIN_TOGGLE_KEYBIND (expected in
 * com.ultra.megamod.reliquary.client.handler.ClientEventHandler or client.registry.ModKeyBindings),
 * hook handleKeyInputEvent below into that keybind so pressing it dispatches the server-bound
 * FortuneCoinTogglePressedPayload (see findAndToggle).
 */
public class FortuneCoinToggler {
	private static final FortuneCoinToggler INSTANCE = new FortuneCoinToggler();

	@SuppressWarnings({"squid:S1172", "unused"}) //used in addListener reflection code
	public static void handleKeyInputEvent(ClientTickEvent.Pre event) {
		// TODO: check FORTUNE_COIN_TOGGLE_KEYBIND.consumeClick() once the client keybind is
		// re-registered — on each consumed click, call INSTANCE.findAndToggle().
	}

	public boolean findAndToggle() {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return false;
		}
		return PlayerInventoryProvider.get().getFromPlayerInventoryHandlers(player,
				(slotStack, handler, handlerName, identifier, slot, ret) -> {
					if (slotStack.getItem() == ModItems.FORTUNE_COIN.get()) {
						// TODO: once the client dispatcher is reintroduced, replace the local
						// toggle with ClientPacketDistributor.sendToServer(new
						// FortuneCoinTogglePressedPayload(...)) so the server is authoritative.
						ModItems.FORTUNE_COIN.get().toggle(slotStack);
						handler.setStackInSlot(player, identifier, slot, slotStack);
						return true;
					}
					return false;
				}, ret -> ret, () -> false);
	}
}
