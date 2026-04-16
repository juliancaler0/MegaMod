package com.ultra.megamod.mixin.spellengine.action_impair;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerInteractionManagerMixin {
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void interactItem_HEAD_SpellEngine(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (EntityActionsAllowed.isImpaired(player, EntityActionsAllowed.Player.ITEM_USE)) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
        // SpellChoice picker removed from right-click path.
        // Source SpellEngine opens the picker here for weapons with a non-empty
        // SpellChoice pool, but that hijacks every right-click and prevents
        // direct spell casting.  The picker is still reachable through the
        // Spell Binding Table (SpellBindingScreenHandler).  Right-click now
        // falls through to the SpellHotbar cast pipeline on the client,
        // which handles casting the selected/first spell in the container.
    }
}
