package com.ultra.megamod.mixin.archers.client.autofire;

import com.ultra.megamod.feature.combat.archers.client.util.ItemUseDelay;
import com.ultra.megamod.feature.combat.archers.item.misc.AutoFireHook;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class ArchersLivingEntityMixin {
    private int timeHoldingCharged = 0;
    private boolean autoFiring = false;

    @Inject(method = "updateUsingItem", at = @At("HEAD"))
    private void autoFireHookRelease(ItemStack stack, CallbackInfo ci) {
        if (autoFiring) {
            return;
        }
        var entity = (LivingEntity) (Object) this;
        var charged = false;
        if (entity.level().isClientSide()) {
            if (entity == Minecraft.getInstance().player) {
                var player = Minecraft.getInstance().player;
                var mainHandStack = player.getMainHandItem();
                if (AutoFireHook.isApplied(mainHandStack)) {
                    // Check if the weapon is fully charged by checking the pull progress
                    var useTime = player.getTicksUsingItem();
                    var maxUseTime = mainHandStack.getUseDuration(player);
                    if (maxUseTime > 0) {
                        float pullProgress = (float) useTime / (float) maxUseTime;
                        if (pullProgress >= 1.0F) {
                            charged = true;
                            // 1 Extra tick to avoid releasing earlier than server agrees on being charged
                            if (timeHoldingCharged > 1) {
                                autoFiring = true;
                                Minecraft.getInstance().gameMode.releaseUsingItem(player);
                                // Wait a little before firing
                                ((ItemUseDelay) Minecraft.getInstance()).imposeItemUseCD_Archers(2);
                                autoFiring = false;
                            }
                        }
                    }
                }
            }
        }
        timeHoldingCharged = charged ? timeHoldingCharged + 1 : 0;
    }
}
