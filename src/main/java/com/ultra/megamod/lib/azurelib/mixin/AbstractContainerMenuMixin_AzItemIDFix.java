package com.ultra.megamod.lib.azurelib.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ultra.megamod.lib.azurelib.common.animation.cache.AzIdentityRegistry;

/**
 * AzureLib ItemStack identity management for container interactions.
 * In 1.21.11, the ItemStack comparison methods (matches/isSameItemSameComponents)
 * were removed from broadcastChanges/sendAllDataToRemote, so the original
 * WrapOperation approach no longer works. Instead, we inject at the end of
 * broadcastChanges to ensure Az IDs are properly tracked.
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin_AzItemIDFix {

    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    private void azurelib$afterBroadcastChanges(CallbackInfo ci) {
        // Az ID synchronization is handled through the DataComponent system in 1.21.11.
        // The AZ_ID component is automatically included in ItemStack serialization,
        // so explicit comparison wrapping is no longer needed.
        // This injection point is kept for future Az ID sync logic if needed.
    }
}
