package com.ultra.megamod.mixin.bettercombat.client;

import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.client.misc.ItemStackViewerPlayer;
import com.ultra.megamod.feature.combat.animation.logic.EntityAttributeHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Tracks the viewed ItemStack during tooltip rendering for BetterCombat attack range display.
 * In 1.21.11, addAttributeTooltipLines was removed; attribute tooltips are now built inside
 * addDetailsToTooltip. We hook HEAD/TAIL of that method instead.
 */
@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {

    @Inject(method = "addDetailsToTooltip", at = @At("HEAD"))
    private void bettercombat$addDetailsToTooltip_HEAD(Item.TooltipContext tooltipContext,
                                                       TooltipDisplay displayComponent,
                                                       @Nullable Player player,
                                                       TooltipFlag tooltipFlag,
                                                       Consumer<Component> textConsumer,
                                                       CallbackInfo ci) {
        if (player instanceof ItemStackViewerPlayer viewer) {
            var itemStack = (ItemStack) (Object) this;
            viewer.betterCombat_setViewedItemStack(itemStack);
        }
    }

    @Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
    private void bettercombat$addDetailsToTooltip_TAIL(Item.TooltipContext tooltipContext,
                                                       TooltipDisplay displayComponent,
                                                       @Nullable Player player,
                                                       TooltipFlag tooltipFlag,
                                                       Consumer<Component> textConsumer,
                                                       CallbackInfo ci) {
        if (player instanceof ItemStackViewerPlayer viewer) {
            viewer.betterCombat_setViewedItemStack(null);
        }
    }
}
