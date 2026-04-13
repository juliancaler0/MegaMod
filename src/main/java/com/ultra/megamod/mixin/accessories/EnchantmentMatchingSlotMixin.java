package com.ultra.megamod.mixin.accessories;

import com.ultra.megamod.lib.accessories.AccessoriesInternals;
import com.ultra.megamod.lib.accessories.api.data.AccessoriesTags;
import com.ultra.megamod.lib.accessories.utils.ServerInstanceHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes Enchantment.matchingSlot() accept the accessories internal equipment slot.
 * This covers all call sites (runIterationOnItem, forEachModifier lambdas, etc.)
 * without needing to target specific lambdas that may be renamed at runtime.
 */
@Mixin(Enchantment.class)
public class EnchantmentMatchingSlotMixin {

    @Inject(method = "matchingSlot", at = @At("HEAD"), cancellable = true)
    private void accessories$allowAccessoriesSlot(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (slot.equals(AccessoriesInternals.INSTANCE.getInternalEquipmentSlot())) {
            var server = ServerInstanceHolder.getInstance();
            if (server != null) {
                Registry<Enchantment> enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                boolean valid = !enchantments.get(enchantments.getResourceKey((Enchantment)(Object)this).orElseThrow())
                        .orElseThrow()
                        .is(AccessoriesTags.INVALID_FOR_REDIRECTION);
                cir.setReturnValue(valid);
            }
        }
    }
}
