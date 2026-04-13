package com.ultra.megamod.mixin.spellengine.criteria;

import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.spellengine.internals.criteria.EnchantmentSpecificCriteria;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedItemTrigger.class)
public class EnchantedItemCriterionMixin {
    @Inject(method = "trigger", at = @At("HEAD"))
    private void trigger_HEAD_SpellEngine(ServerPlayer player, ItemStack stack, int levels, CallbackInfo ci) {
        var enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for(var entry: enchants.entrySet()) {
            var id = entry.getKey().unwrapKey().get().identifier();
            if (id != null) {
                EnchantmentSpecificCriteria.INSTANCE.trigger(player, id);
            }
        }
    }
}
