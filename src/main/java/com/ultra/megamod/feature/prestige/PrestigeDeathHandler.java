package com.ultra.megamod.feature.prestige;

import com.ultra.megamod.MegaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Broadcasts custom prestige death messages when a player with an active
 * death message reward dies.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class PrestigeDeathHandler {

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ServerLevel overworld = player.level().getServer().overworld();
        PrestigeRewardManager prm = PrestigeRewardManager.get(overworld);
        String template = prm.getActiveDeathMessage(player.getUUID());
        if (template.isEmpty()) return;

        String message = template.replace("X", player.getGameProfile().name());
        for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
