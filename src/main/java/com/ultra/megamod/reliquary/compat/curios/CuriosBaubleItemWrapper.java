package com.ultra.megamod.reliquary.compat.curios;

import com.ultra.megamod.reliquary.item.util.ICuriosItem;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Wraps a Reliquary accessory {@link ItemStack} (whose item implements
 * {@link ICuriosItem}) as a Curios {@link ICurio}. Delegates equip / worn-tick
 * callbacks onto the Reliquary item so its stat, tick and equip effects keep
 * firing when the item lives in a Curios slot rather than the main inventory.
 */
class CuriosBaubleItemWrapper implements ICurio {
	private final ItemStack baubleStack;
	private final ICuriosItem curiosItem;

	CuriosBaubleItemWrapper(ItemStack baubleStack) {
		this.baubleStack = baubleStack;
		this.curiosItem = (ICuriosItem) baubleStack.getItem();
	}

	@Override
	public ItemStack getStack() {
		return baubleStack;
	}

	@Override
	public void onEquip(SlotContext slotContext, ItemStack prevStack) {
		curiosItem.onEquipped(slotContext.identifier(), slotContext.entity());
	}

	@Override
	public void curioTick(SlotContext slotContext) {
		CuriosCompat.getStackInSlot(slotContext.entity(), slotContext.identifier(), slotContext.index())
				.ifPresent(stack -> curiosItem.onWornTick(stack, slotContext.entity()));
	}

	@Override
	public boolean canEquip(SlotContext slotContext) {
		return curiosItem.getCuriosType().getIdentifier().equals(slotContext.identifier());
	}
}
