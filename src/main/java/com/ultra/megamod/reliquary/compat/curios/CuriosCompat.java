package com.ultra.megamod.reliquary.compat.curios;

import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.reference.Compatibility;
import com.ultra.megamod.reliquary.util.PlayerInventoryProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

/**
 * Reliquary ↔ Curios bridge.
 *
 * <p>Publishes a {@link PlayerInventoryProvider} handler keyed on
 * {@link Compatibility.ModIds#CURIOS} so Reliquary's inventory-walking code
 * transparently sees items equipped in Curios slots, and registers the
 * {@link CuriosBaubleItemWrapper} for Reliquary accessory-shaped items so
 * Curios knows how to treat them when equipped.
 */
public class CuriosCompat {

	public CuriosCompat(IEventBus modBus) {
		modBus.addListener(this::onRegisterCapabilities);

		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			CuriosCompatClient.registerLayerDefinitions(modBus);
		}

		addPlayerInventoryHandlers();
	}

	private void addPlayerInventoryHandlers() {
		PlayerInventoryProvider.get().addPlayerInventoryHandler(
				Compatibility.ModIds.CURIOS,
				player -> CuriosApi.getSlots(false).keySet(),
				(player, identifier) -> getFromCuriosSlotStackHandler(player, identifier, ICurioStacksHandler::getSlots, 0),
				(player, identifier, slot) -> getFromCuriosSlotStackHandler(player, identifier, sh -> sh.getStacks().getStackInSlot(slot), ItemStack.EMPTY),
				(player, identifier, slot, stack) -> CuriosApi.getCuriosInventory(player)
						.flatMap(h -> h.getStacksHandler(identifier))
						.ifPresent(sh -> sh.getStacks().setStackInSlot(slot, stack)),
				true);
	}

	public static <T> T getFromCuriosSlotStackHandler(LivingEntity livingEntity, String identifier, Function<ICurioStacksHandler, T> getFromHandler, T defaultValue) {
		return CuriosApi.getCuriosInventory(livingEntity)
				.map(h -> h.getStacksHandler(identifier).map(getFromHandler).orElse(defaultValue))
				.orElse(defaultValue);
	}

	public void onRegisterCapabilities(RegisterCapabilitiesEvent evt) {
		evt.registerItem(
				CuriosCapability.ITEM,
				(itemStack, unused) -> new CuriosBaubleItemWrapper(itemStack),
				ModItems.FORTUNE_COIN.get(),
				ModItems.MOB_CHARM_BELT.get(),
				ModItems.TWILIGHT_CLOAK.get());
	}

	public static Optional<ItemStack> getStackInSlot(LivingEntity entity, String slotName, int slot) {
		return CuriosApi.getCuriosInventory(entity)
				.flatMap(handler -> handler.getStacksHandler(slotName)
						.map(sh -> sh.getStacks().getStackInSlot(slot)));
	}

	public static void setStackInSlot(LivingEntity entity, String slotName, int slot, ItemStack stack) {
		CuriosApi.getCuriosInventory(entity)
				.flatMap(handler -> handler.getStacksHandler(slotName))
				.ifPresent(sh -> sh.getStacks().setStackInSlot(slot, stack));
	}

	/**
	 * Lightweight empty Curios dynamic stack handler kept for parity with the
	 * upstream compat code — exposed as a nested helper so future shims that
	 * need a non-backing Curios handler can reuse it.
	 */
	@SuppressWarnings("unused")
	private static class EmptyCuriosHandler extends ItemStackHandler implements IDynamicStackHandler {
		@Override
		public void setPreviousStackInSlot(int i, @Nonnull ItemStack itemStack) {
			//noop
		}

		@Override
		public ItemStack getPreviousStackInSlot(int i) {
			return ItemStack.EMPTY;
		}

		@Override
		public void grow(int i) {
			//noop
		}

		@Override
		public void shrink(int i) {
			//noop
		}
	}
}
