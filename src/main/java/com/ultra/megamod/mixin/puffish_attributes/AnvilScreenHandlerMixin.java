package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Applies {@code REPAIR_COST} to the anvil's level-cost clamp so ranks of the repair_cost
 * attribute scale enchanting costs linearly.
 *
 * <p>1.21.11 Parchment renames {@code AnvilScreenHandler} to {@link AnvilMenu} and
 * {@code ForgingScreenHandler} to {@link ItemCombinerMenu}. The vanilla {@code updateResult}
 * method was replaced by {@code createResultInternal} in both Fabric and NeoForge (NeoForge
 * previously used this name directly; Mojang adopted it upstream). The clamp target is now
 * {@code Lnet/minecraft/util/Mth;clamp(JJJ)J} (Mth, not MathHelper).</p>
 */
@Mixin(value = AnvilMenu.class, priority = 1100)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {
	private AnvilScreenHandlerMixin(MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context, ItemCombinerMenuSlotDefinition forgingSlotsManager) {
		super(type, syncId, playerInventory, context, forgingSlotsManager);
	}

	@ModifyExpressionValue(
			method = "createResultInternal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Mth;clamp(JJJ)J"
			)
	)
	private long modifyExpressionValueAtClamp(long value) {
		return Math.max(1, Math.round(DynamicModification.create()
				.withPositive(PuffishAttributes.REPAIR_COST, player)
				.applyTo(value)));
	}
}
