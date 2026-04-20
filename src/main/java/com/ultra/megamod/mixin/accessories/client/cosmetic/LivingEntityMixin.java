package com.ultra.megamod.mixin.accessories.client.cosmetic;

import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.pond.CosmeticArmorLookupTogglable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CosmeticArmorLookupTogglable {

    private boolean accessories$cosmeticArmorAlternative = false;

    @Override
    public void setLookupToggle(boolean value) {
        var capability = AccessoriesCapability.get((LivingEntity) (Object) this);

        if (capability == null) {
            this.accessories$cosmeticArmorAlternative = false;
            return;
        }

        this.accessories$cosmeticArmorAlternative = value;
    }

    @Override
    public boolean getLookupToggle() {
        if (!((LivingEntity) (Object) this).level().isClientSide()) return false;
        return accessories$cosmeticArmorAlternative;
    }

    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void accessories$getCosmeticAlternative(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        CosmeticArmorLookupTogglable.getAlternativeStack(((LivingEntity) (Object) this), slot, cir::setReturnValue);
    }
}
