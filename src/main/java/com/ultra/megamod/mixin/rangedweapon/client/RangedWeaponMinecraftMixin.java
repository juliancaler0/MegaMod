package com.ultra.megamod.mixin.rangedweapon.client;

import com.ultra.megamod.lib.rangedweapon.api.CustomBow;
import com.ultra.megamod.lib.rangedweapon.api.CustomCrossbow;
import com.ultra.megamod.lib.rangedweapon.client.ModelPredicateHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In MC 1.21.11, the ModelPredicateProviderRegistry was removed.
 * Item model predicates are now handled via JSON item model definitions.
 * This mixin is retained for API structure but the model predicate
 * registration calls are no-ops (ModelPredicateHelper methods are stubs).
 */
@Mixin(Minecraft.class)
public class RangedWeaponMinecraftMixin {
    @Inject(method = "run", at = @At("HEAD"))
    private void run_HEAD(CallbackInfo ci) {
        for (var bow: CustomBow.instances) {
            ModelPredicateHelper.registerBowModelPredicates(bow);
        }
        ModelPredicateHelper.registerBowModelPredicates(Items.BOW);
        for (var crossbow: CustomCrossbow.instances) {
            ModelPredicateHelper.registerCrossbowModelPredicates(crossbow);
        }
    }
}
