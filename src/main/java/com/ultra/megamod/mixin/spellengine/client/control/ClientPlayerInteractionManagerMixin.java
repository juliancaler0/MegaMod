package com.ultra.megamod.mixin.spellengine.client.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import com.ultra.megamod.lib.spellengine.client.input.MinecraftClientExtension;
import com.ultra.megamod.lib.spellengine.client.input.SpellHotbar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    public void interactItem_HEAD_LockHotbar(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof LocalPlayer clientPlayer) {
            // SpellChoice early-return removed.  Source SpellEngine skipped
            // the SpellHotbar path for items with a non-empty SpellChoice pool
            // (to let the server-side picker open instead).  Since we removed
            // the server-side picker from right-click, we now let the
            // SpellHotbar handle the use-key so the selected spell casts
            // directly on right-click.
            if (SpellHotbar.INSTANCE.lastHandled() == null) {
                var handled = SpellHotbar.INSTANCE.handleUseKey(clientPlayer, minecraft.options);
                ((MinecraftClientExtension) minecraft).onSpellHotbarInputHandled(handled);
            }
            if (((MinecraftClientExtension)minecraft).isSpellCastLockActive()) {
                cir.setReturnValue(InteractionResult.FAIL);
                cir.cancel();
            }
        }
        if (EntityActionsAllowed.isImpaired(player, EntityActionsAllowed.Player.ITEM_USE, true)) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }
}
