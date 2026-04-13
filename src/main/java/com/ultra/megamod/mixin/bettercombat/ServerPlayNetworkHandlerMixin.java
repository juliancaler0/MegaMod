package com.ultra.megamod.mixin.bettercombat;

import com.ultra.megamod.feature.combat.animation.logic.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Server-side mixin to fix getItemInHand during BetterCombat dual-wield attacks.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.ServerPlayNetworkHandlerMixin).
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {

    @Redirect(method = "handlePlayerAction",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    public ItemStack bettercombat$getItemInHand(ServerPlayer instance, InteractionHand hand) {
        ItemStack result = null;
        switch (hand) {
            case MAIN_HAND -> {
                result = instance.getMainHandItem();
            }
            case OFF_HAND -> {
                result = InventoryUtil.getOffHandSlotStack(instance);
            }
        }
        return result;
    }
}
