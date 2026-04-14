package com.ultra.megamod.reliquary.handler;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.ultra.megamod.reliquary.block.PassivePedestalBlock;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.item.HarvestRodItem;
import com.ultra.megamod.reliquary.item.RendingGaleItem;
import com.ultra.megamod.reliquary.pedestal.PedestalRegistry;
import com.ultra.megamod.reliquary.util.FakePlayerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommonEventHandler {
	private CommonEventHandler() {
	}

	private static final Set<IPlayerHurtHandler> playerHurtHandlers = Sets.newTreeSet(new HandlerPriorityComparator());
	private static final Set<IPlayerDeathHandler> playerDeathHandlers = Sets.newTreeSet(new HandlerPriorityComparator());

	private static final Map<UUID, Boolean> playersFlightStatus = new HashMap<>();

	public static void registerPlayerHurtHandler(IPlayerHurtHandler handler) {
		playerHurtHandlers.add(handler);
	}

	public static void registerPlayerDeathHandler(IPlayerDeathHandler handler) {
		playerDeathHandlers.add(handler);
	}

	public static void registerEventBusListeners(IEventBus eventBus) {
		eventBus.addListener(PassivePedestalBlock::onRightClicked);
		eventBus.addListener(CommonEventHandler::preventMendingAndUnbreaking);
		eventBus.addListener(CommonEventHandler::blameDrullkus);
		eventBus.addListener(CommonEventHandler::beforePlayerHurt);
		eventBus.addListener(CommonEventHandler::beforePlayerDeath);
		eventBus.addListener(CommonEventHandler::onDimensionUnload);
		eventBus.addListener(CommonEventHandler::onPlayerTick);
		eventBus.addListener(CommonEventHandler::onHarvestRodBreakBlock);
		eventBus.addListener(PedestalRegistry::serverStopping);
	}

	// 1.21.11 port: Item#canAttackBlock was removed, so the Harvest Rod's
	// "breaking one crop breaks all crops in range" behaviour is routed through
	// BlockEvent.BreakEvent instead. Triggers only when the player is currently
	// holding the Harvest Rod in either hand.
	public static void onHarvestRodBreakBlock(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
		if (player == null || player.level().isClientSide()) {
			return;
		}
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		ItemStack rod;
		if (main.getItem() == ModItems.HARVEST_ROD.get()) {
			rod = main;
		} else if (off.getItem() == ModItems.HARVEST_ROD.get()) {
			rod = off;
		} else {
			return;
		}
		HarvestRodItem harvestRod = (HarvestRodItem) rod.getItem();
		// Delegate to the existing AoE break routine. It will break all crops in the
		// configured radius around the broken block and return !broken; we don't cancel
		// the triggering break, we just fan out the rest.
		harvestRod.canAttackBlock(event.getState(), (net.minecraft.world.level.Level) event.getLevel(), event.getPos(), player);
	}

	public static void preventMendingAndUnbreaking(AnvilUpdateEvent event) {
		if (event.getLeft().isEmpty() || event.getRight().isEmpty()) {
			return;
		}

		if (event.getLeft().getItem() != ModItems.MOB_CHARM.get() && event.getLeft().getItem() != ModItems.ALKAHESTRY_TOME.get()) {
			return;
		}

		HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = event.getPlayer().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		if (event.getRight().getEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.MENDING)) > 0 || event.getRight().getEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING)) > 0) {
			event.setCanceled(true);
		}
	}

	public static void blameDrullkus(PlayerEvent.PlayerLoggedInEvent event) {
		// Thanks for the Witch's Hat texture! Also, blame Drullkus for making me add this. :P
		if (event.getEntity().getGameProfile().name().equals("Drullkus")
				&& !event.getEntity().getPersistentData().contains("gift")
				&& event.getEntity().getInventory().add(new ItemStack(ModItems.WITCH_HAT.get()))) {
			event.getEntity().getPersistentData().putBoolean("gift", true);
		}
	}

	public static void beforePlayerHurt(LivingIncomingDamageEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player player)) {
			return;
		}

		boolean cancel = false;
		for (IPlayerHurtHandler handler : playerHurtHandlers) {
			if (handler.canApply(player, event) && handler.apply(player, event)) {
				cancel = true;
				break;
			}
		}

		if (cancel) {
			event.setCanceled(true);
		}
	}

	public static void beforePlayerDeath(LivingDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player player)) {
			return;
		}

		boolean cancel = false;
		for (IPlayerDeathHandler handler : playerDeathHandlers) {
			if (handler.canApply(player, event) && handler.apply(player, event)) {
				cancel = true;
				break;
			}
		}

		if (cancel) {
			event.setCanceled(true);
		}
	}

	public static void onDimensionUnload(LevelEvent.Unload event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			FakePlayerFactory.unloadWorld(serverLevel);
		}
	}

	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) {
			return;
		}


		if (player.isUsingItem() && player.getUseItem().getItem() == ModItems.RENDING_GALE.get() && ModItems.RENDING_GALE.get().getMode(player.getUseItem()) == RendingGaleItem.Mode.FLIGHT && ModItems.RENDING_GALE.get().hasFlightCharge(player.getUseItem())) {
			playersFlightStatus.put(player.getGameProfile().id(), true);
			AttributeInstance creativeFlightAttribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
			if (creativeFlightAttribute != null) {
				creativeFlightAttribute.setBaseValue(1);
			}
			((ServerPlayer) player).connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
		} else {
			if (!playersFlightStatus.containsKey(player.getGameProfile().id())) {
				playersFlightStatus.put(player.getGameProfile().id(), false);
				return;
			}
			boolean isFlying = playersFlightStatus.get(player.getGameProfile().id());
			if (isFlying) {
				playersFlightStatus.put(player.getGameProfile().id(), false);
				if (!player.isCreative()) {
					AttributeInstance creativeFlightAttribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
					if (creativeFlightAttribute != null) {
						creativeFlightAttribute.setBaseValue(0);
					}
					player.getAbilities().flying = false;
					((ServerPlayer) player).connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
				}
			}
		}
	}
}
