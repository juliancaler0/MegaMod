package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Broadcasts class-specific death messages when a player with a class dies.
 * Runs at LOW priority so other death handlers (PrestigeDeathHandler, dungeon insurance, etc.)
 * can cancel the event first if needed.
 *
 * Death messages by class:
 * - Paladin: "PlayerName the Paladin has fallen in battle"
 * - Warrior: "PlayerName the Warrior met a glorious end"
 * - Wizard:  "PlayerName the Wizard's magic faded"
 * - Rogue:   "PlayerName the Rogue was caught in the shadows"
 * - Ranger:  "PlayerName the Ranger fell to the wilds"
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ClassDeathMessageHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        try {
            ServerLevel overworld = player.level().getServer().overworld();
            PlayerClassManager classManager = PlayerClassManager.get(overworld);
            PlayerClass cls = classManager.getPlayerClass(player.getUUID());

            if (cls == PlayerClass.NONE) return;

            String playerName = player.getGameProfile().name();
            String message = getClassDeathMessage(cls, playerName);
            int color = cls.getColor();

            // Broadcast the class death message to all online players
            Component deathComponent = Component.literal(message)
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));

            for (ServerPlayer onlinePlayer : player.level().getServer().getPlayerList().getPlayers()) {
                onlinePlayer.sendSystemMessage(deathComponent);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.debug("Failed to send class death message", e);
        }
    }

    private static String getClassDeathMessage(PlayerClass cls, String playerName) {
        return switch (cls) {
            case PALADIN -> "\u2694 " + playerName + " the Paladin has fallen in battle";
            case WARRIOR -> "\u2694 " + playerName + " the Warrior met a glorious end";
            case WIZARD  -> "\u2726 " + playerName + " the Wizard's magic faded";
            case ROGUE   -> "\u2020 " + playerName + " the Rogue was caught in the shadows";
            case RANGER  -> "\u27B3 " + playerName + " the Ranger fell to the wilds";
            default -> "";
        };
    }
}
