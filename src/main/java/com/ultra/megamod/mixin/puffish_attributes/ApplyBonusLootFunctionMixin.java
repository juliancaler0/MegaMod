package com.ultra.megamod.mixin.puffish_attributes;

import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies {@code FORTUNE} to Fortune-enchanted loot drops (ores, grass, leaves, etc.)
 * when the block was broken by a player.
 *
 * <p>1.21.11 Parchment renames Yarn's {@code ApplyBonusLootFunction} to
 * {@link ApplyBonusCount}, the {@code process} method to {@code run}, the
 * {@code LootContextParameters} class to {@link LootContextParams}, and the
 * {@code THIS_ENTITY} key to {@code THIS_ENTITY}. Enchantment comparison uses
 * {@code Holder#is(ResourceKey)} instead of Yarn's {@code matchesKey}.</p>
 */
@Mixin(ApplyBonusCount.class)
public abstract class ApplyBonusLootFunctionMixin {

	@Shadow
	@Final
	private Holder<Enchantment> enchantment;

	@ModifyVariable(
			method = "run",
			at = @At("STORE"),
			ordinal = 0
	)
	private int modifyVariableAtProcess(int value, ItemStack itemStack, LootContext context) {
		if (enchantment.is(Enchantments.FORTUNE) && context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player) {
			var fortune = DynamicModification.create()
					.withPositive(PuffishAttributes.FORTUNE, player)
					.applyTo((double) value);

			value = (int) fortune;
			if (context.getRandom().nextFloat() < fortune - value) {
				value++;
			}
		}
		return value;
	}
}
