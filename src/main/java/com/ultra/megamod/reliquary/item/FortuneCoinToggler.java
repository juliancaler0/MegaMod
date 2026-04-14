package com.ultra.megamod.reliquary.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.util.PlayerInventoryProvider;

/**
 * TODO: 1.21.11 port - the client-side keybind registration lives in the pruned
 * client.handler.ClientEventHandler. Until the client layer is rebuilt this class is a
 * stub; the keybind hook and server-bound toggle payload will be re-wired later.
 */
public class FortuneCoinToggler {
	private static final FortuneCoinToggler INSTANCE = new FortuneCoinToggler();

	@SuppressWarnings({"squid:S1172", "unused"}) //used in addListener reflection code
	public static void handleKeyInputEvent(ClientTickEvent.Pre event) {
		// TODO: 1.21.11 port - FORTUNE_COIN_TOGGLE_KEYBIND not yet reintroduced; no-op.
	}

	public boolean findAndToggle() {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return false;
		}
		return PlayerInventoryProvider.get().getFromPlayerInventoryHandlers(player,
				(slotStack, handler, handlerName, identifier, slot, ret) -> {
					if (slotStack.getItem() == ModItems.FORTUNE_COIN.get()) {
						// TODO: 1.21.11 port - FortuneCoinTogglePressedPayload routing via
						// ClientPacketDistributor pending. Toggle locally for now.
						ModItems.FORTUNE_COIN.get().toggle(slotStack);
						handler.setStackInSlot(player, identifier, slot, slotStack);
						return true;
					}
					return false;
				}, ret -> ret, () -> false);
	}
}
