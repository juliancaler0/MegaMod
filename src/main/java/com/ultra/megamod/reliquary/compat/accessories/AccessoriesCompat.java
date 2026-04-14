package com.ultra.megamod.reliquary.compat.accessories;

import com.ultra.megamod.lib.accessories.api.AccessoriesAPI;
import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.api.AccessoriesContainer;
import com.ultra.megamod.lib.accessories.api.core.Accessory;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.reference.Compatibility;
import com.ultra.megamod.reliquary.util.PlayerInventoryProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Reliquary ↔ Accessories bridge, ported to MegaMod's internal
 * {@code com.ultra.megamod.lib.accessories} fork.
 *
 * <p>Wires accessory-shaped Reliquary items (mob charm belt, fortune coin,
 * hero medallion) into the fork's {@link AccessoriesAPI} so they count as
 * equipped accessories for the fork's slot container checks, and publishes
 * a {@link PlayerInventoryProvider} handler so Reliquary's inventory-walking
 * code can see anything living in those fork slots.
 */
public class AccessoriesCompat {
	private final Set<String> containerNames = new CopyOnWriteArraySet<>();
	private long lastTagsRefresh = -1;
	private static final int TAGS_REFRESH_COOLDOWN = 100;

	/**
	 * Marker accessory that refuses quick-equip-on-use. Everything else
	 * (capability access, tick, unequip) falls through to the fork's default
	 * {@link Accessory} implementation.
	 */
	private static final Accessory NO_QUICK_EQUIP_ACCESSORY = new Accessory() {
		@Override
		public boolean canEquipFromUse(ItemStack stack) {
			return false;
		}
	};

	public AccessoriesCompat(IEventBus modBus) {
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			AccessoriesCompatClient.registerRenderers(modBus);
		}
		modBus.addListener(this::onSetup);
		addPlayerInventoryHandlers();
	}

	private void addPlayerInventoryHandlers() {
		PlayerInventoryProvider.get().addPlayerInventoryHandler(
				Compatibility.ModIds.ACCESSORIES,
				this::getAccessoriesSlotTags,
				AccessoriesCompat::getSize,
				AccessoriesCompat::getStackInSlot,
				AccessoriesCompat::setStackInSlot,
				true);
	}

	private void onSetup(FMLCommonSetupEvent event) {
		AccessoriesAPI.registerAccessory(ModItems.MOB_CHARM_BELT.get(), NO_QUICK_EQUIP_ACCESSORY);
		AccessoriesAPI.registerAccessory(ModItems.FORTUNE_COIN.get(), NO_QUICK_EQUIP_ACCESSORY);
		AccessoriesAPI.registerAccessory(ModItems.HERO_MEDALLION.get(), NO_QUICK_EQUIP_ACCESSORY);
	}

	public static ItemStack getStackInSlot(LivingEntity entity, String slotName, int slot) {
		return AccessoriesCapability.getOptionally(entity).map(cap -> {
			AccessoriesContainer container = cap.getContainers().get(slotName);
			if (container == null) {
				return ItemStack.EMPTY;
			}
			// ExpandedContainer extends vanilla Container and exposes getItem(int).
			return container.getAccessories().getItem(slot);
		}).orElse(ItemStack.EMPTY);
	}

	public static void setStackInSlot(LivingEntity entity, String slotName, int slot, ItemStack stack) {
		AccessoriesCapability.getOptionally(entity).ifPresent(cap -> SlotReference.of(entity, slotName, slot).setStack(stack));
	}

	private static int getSize(LivingEntity entity, String slotName) {
		return AccessoriesCapability.getOptionally(entity)
				.map(cap -> {
					AccessoriesContainer container = cap.getContainers().get(slotName);
					if (container == null) {
						return 0;
					}
					return container.getSize();
				}).orElse(0);
	}

	private Set<String> getAccessoriesSlotTags(Player player) {
		long gameTime = player.level().getGameTime();
		if (lastTagsRefresh + TAGS_REFRESH_COOLDOWN < gameTime) {
			lastTagsRefresh = gameTime;
			containerNames.clear();
			containerNames.addAll(AccessoriesCapability.getOptionally(player)
					.map(capability -> capability.getContainers().keySet())
					.orElse(Collections.emptySet()));
		}
		return containerNames;
	}
}
