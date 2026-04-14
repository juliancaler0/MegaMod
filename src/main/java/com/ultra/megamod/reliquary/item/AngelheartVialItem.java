package com.ultra.megamod.reliquary.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.ultra.megamod.reliquary.handler.CommonEventHandler;
import com.ultra.megamod.reliquary.handler.HandlerPriority;
import com.ultra.megamod.reliquary.handler.IPlayerDeathHandler;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.network.SpawnAngelheartVialParticlesPayload;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.EntityHelper;
import com.ultra.megamod.reliquary.util.InventoryHelper;

public class AngelheartVialItem extends ItemBase {
	public AngelheartVialItem() {
		super(new Properties());

		CommonEventHandler.registerPlayerDeathHandler(new IPlayerDeathHandler() {
			@Override
			public boolean canApply(Player player, LivingDeathEvent event) {
				return InventoryHelper.playerHasItem(player, ModItems.ANGELHEART_VIAL.get());
			}

			@SuppressWarnings({"java:S2440"})
			//instantiating the packet for its type to be used as identifier for the packet
			@Override
			public boolean apply(Player player, LivingDeathEvent event) {
				decreaseAngelHeartByOne(player);

				// player should see a vial "shatter" effect and hear the glass break to
				// let them know they lost a vial.
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SpawnAngelheartVialParticlesPayload(player.position()));

				// play some glass breaking effects at the player location
				player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, player.level().random.nextFloat() * 0.1F + 0.9F);

				// gives the player a few hearts, sparing them from death.
				float amountHealed = player.getMaxHealth() * (float) Config.COMMON.items.angelHeartVial.healPercentageOfMaxLife.get() / 100F;
				player.setHealth(amountHealed);

				// if the player had any negative status effects [vanilla only for now], remove them:
				if (Boolean.TRUE.equals(Config.COMMON.items.angelHeartVial.removeNegativeStatus.get())) {
					EntityHelper.removeNegativeStatusEffects(player);
				}

				return true;
			}

			@Override
			public HandlerPriority getPriority() {
				return HandlerPriority.LOW;
			}
		});
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	// TODO: 1.21.11 port - Item#getCraftingRemainingItem / hasCraftingRemainingItem were
	// removed; configure Properties#craftRemainder(Item) at registration instead.

	private static void decreaseAngelHeartByOne(Player player) {
		ItemStack stack = InventoryHelper.getItemFromAllPlayerHandlers(player, ModItems.ANGELHEART_VIAL.get());
		if (!stack.isEmpty()) {
			stack.shrink(1);
		}
	}
}
