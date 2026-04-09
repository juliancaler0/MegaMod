package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.items.ArcherItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side event handler for archer utility items.
 * Handles the auto-fire hook: when a player holds a loaded crossbow in their
 * main hand and an auto-fire hook in their offhand, the crossbow automatically fires.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ArcherEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // Check if offhand has auto-fire hook
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !offhand.is(ArcherItemRegistry.AUTO_FIRE_HOOK.get())) return;

        // Check if mainhand has a loaded crossbow
        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.isEmpty() || !(mainhand.getItem() instanceof CrossbowItem crossbow)) return;

        // Check if the crossbow is loaded (has charged projectiles)
        ChargedProjectiles charged = mainhand.get(DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) return;

        // The crossbow is loaded and we have auto-fire hook — trigger its use() method
        // which handles the firing logic internally. This is the same code path as
        // when the player right-clicks with a loaded crossbow.
        crossbow.use(player.level(), player, InteractionHand.MAIN_HAND);
    }
}
