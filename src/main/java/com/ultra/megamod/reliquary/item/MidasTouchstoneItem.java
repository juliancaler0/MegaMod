package com.ultra.megamod.reliquary.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import com.ultra.megamod.reliquary.init.ModDataComponents;
import com.ultra.megamod.reliquary.item.util.ICuriosItem;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.InventoryHelper;
import com.ultra.megamod.reliquary.util.RegistryHelper;
import com.ultra.megamod.reliquary.util.TooltipBuilder;

import javax.annotation.Nullable;
import java.util.List;

public class MidasTouchstoneItem extends ChargeableItem implements ICuriosItem {

	public MidasTouchstoneItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.EPIC));
	}

	private static boolean isRepairableByMidas(ItemStack stack) {
		// Gold / Netherite tools and armor via repair ingredient tags
		return stack.is(ItemTags.REPAIRS_GOLD_ARMOR) || stack.is(ItemTags.REPAIRS_NETHERITE_ARMOR)
				|| stack.is(Items.GOLDEN_PICKAXE) || stack.is(Items.GOLDEN_AXE) || stack.is(Items.GOLDEN_SHOVEL)
				|| stack.is(Items.GOLDEN_HOE) || stack.is(Items.GOLDEN_SWORD)
				|| stack.is(Items.NETHERITE_PICKAXE) || stack.is(Items.NETHERITE_AXE) || stack.is(Items.NETHERITE_SHOVEL)
				|| stack.is(Items.NETHERITE_HOE) || stack.is(Items.NETHERITE_SWORD)
				|| stack.is(Items.GOLDEN_HELMET) || stack.is(Items.GOLDEN_CHESTPLATE) || stack.is(Items.GOLDEN_LEGGINGS) || stack.is(Items.GOLDEN_BOOTS)
				|| stack.is(Items.NETHERITE_HELMET) || stack.is(Items.NETHERITE_CHESTPLATE) || stack.is(Items.NETHERITE_LEGGINGS) || stack.is(Items.NETHERITE_BOOTS);
	}

	@Override
	protected void addMoreInformation(ItemStack touchstone, @Nullable HolderLookup.Provider registries, TooltipBuilder tooltipBuilder) {
		tooltipBuilder.charge(this, ".tooltip2", getGlowstoneCharge(touchstone));
		if (isEnabled(touchstone)) {
			tooltipBuilder.absorbActive(Items.GLOWSTONE_DUST.getName(new ItemStack(Items.GLOWSTONE_DUST)).getString());
		} else {
			tooltipBuilder.absorb();
		}
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		if (!(entity instanceof Player player) || player.isSpectator() || level.getGameTime() % 10 != 0) {
			return;
		}

		//TODO legacy support, remove in future
		if (!stack.has(ModDataComponents.PARTIAL_CHARGES)) {
			getMigratedStoredCharge(stack, FIRST_SLOT);
		}

		if (isEnabled(stack)) {
			int glowstoneCharge = getGlowstoneCharge(stack);
			consumeAndCharge(stack, 0, player, getGlowstoneLimit() - glowstoneCharge, 1, 16);
		}

		doRepairAndDamageTouchstone(stack, player);
	}

	@Override
	protected boolean isItemValidForContainerSlot(ItemStack containerStack, int slot, ItemStack stack) {
		return stack.is(Items.GLOWSTONE_DUST);
	}

	public static int getGlowstoneCharge(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.GLOWSTONE, 0);
	}

	private void doRepairAndDamageTouchstone(ItemStack touchstone, Player player) {
		List<String> goldItems = Config.COMMON.items.midasTouchstone.getGoldItems();

		IItemHandler playerInventory = InventoryHelper.getItemHandlerFrom(player);
		if (playerInventory == null) {
			return;
		}
		for (int slot = 0; slot < playerInventory.getSlots(); slot++) {
			ItemStack stack = playerInventory.getStackInSlot(slot);
			Item item = stack.getItem();

			if (stack.getDamageValue() <= 0 || !stack.has(DataComponents.DAMAGE)) {
				continue;
			}

			tryRepairingItem(touchstone, player, goldItems, stack, item);
		}
	}

	private void tryRepairingItem(ItemStack touchstone, Player player, List<String> goldItems, ItemStack stack, Item item) {
		if (isRepairableByMidas(stack) || goldItems.contains(RegistryHelper.getItemRegistryName(item))) {
			repairItem(stack, touchstone, player);
		}
	}

	private void repairItem(ItemStack stack, ItemStack touchstone, Player player) {
		if (reduceTouchStoneCharge(touchstone, player)) {
			int damage = stack.getDamageValue();
			stack.setDamageValue(damage - Math.min(damage, 10));
		}
	}

	private boolean reduceTouchStoneCharge(ItemStack stack, Player player) {
		return player.isCreative() || useCharge(stack, getGlowStoneCost());
	}

	private int getGlowStoneCost() {
		return Config.COMMON.items.midasTouchstone.glowstoneCost.get();
	}

	private int getGlowStoneWorth() {
		return Config.COMMON.items.midasTouchstone.glowstoneWorth.get();
	}

	private int getGlowstoneLimit() {
		return Config.COMMON.items.midasTouchstone.glowstoneLimit.get();
	}

	@Override
	public Type getCuriosType() {
		return Type.CHARM;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity player) {
		if (player.level() instanceof ServerLevel serverLevel) {
			inventoryTick(stack, serverLevel, player, null);
		}
	}

	@Override
	public void addStoredCharge(ItemStack containerStack, int slot, int chargeToAdd, @Nullable ItemStack chargeStack) {
		containerStack.set(ModDataComponents.GLOWSTONE, Math.max(getGlowstoneCharge(containerStack) + chargeToAdd, 0));
	}

	@Override
	protected int getSlotWorth(int slot) {
		return slot == 0 ? getGlowStoneWorth() : 0;
	}

	@Override
	public int getStoredCharge(ItemStack containerStack, int slot) {
		return getGlowstoneCharge(containerStack);
	}
}
