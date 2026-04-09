/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.ServerScoreboard
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.scores.DisplaySlot
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.ScoreHolder
 *  net.minecraft.world.scores.criteria.ObjectiveCriteria
 *  net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.ultra.megamod.feature.multiplayer;

import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.multiplayer.PlayerStatistics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="megamod")
public class BelownameDisplay {
    private static final String OBJECTIVE_NAME = "megamod_kills";
    private static final String TEAM_PREFIX = "mm_cls_";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        MinecraftServer server = serverPlayer.level().getServer();
        if (server == null) {
            return;
        }
        BelownameDisplay.ensureObjective(server);
        BelownameDisplay.updatePlayerScore(server, serverPlayer);
        BelownameDisplay.updateClassTeam(server, serverPlayer);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        if (overworld.getGameTime() % 100L != 0L) {
            return;
        }
        BelownameDisplay.ensureObjective(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BelownameDisplay.updatePlayerScore(server, player);
            BelownameDisplay.updateClassTeam(server, player);
        }
    }

    private static void ensureObjective(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.addObjective(OBJECTIVE_NAME, ObjectiveCriteria.DUMMY, (Component)Component.literal((String)"Kills"), ObjectiveCriteria.RenderType.INTEGER, true, null);
        }
        if (scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME) != objective) {
            scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
        }
    }

    private static void updatePlayerScore(MinecraftServer server, ServerPlayer player) {
        PlayerStatistics stats = PlayerStatistics.getIfLoaded();
        if (stats == null) {
            return;
        }
        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            return;
        }
        int kills = stats.getStat(player.getUUID(), "kills");
        scoreboard.getOrCreatePlayerScore((ScoreHolder)player, objective).set(kills);
    }

    /**
     * Assigns the player to a per-class team that uses a suffix to show
     * the class name below the player's nameplate in-world.
     * e.g. "PlayerName" with suffix " [Paladin]" in gold.
     */
    private static void updateClassTeam(MinecraftServer server, ServerPlayer player) {
        try {
            ServerLevel overworld = server.overworld();
            PlayerClassManager classManager = PlayerClassManager.get(overworld);
            PlayerClass cls = classManager.getPlayerClass(player.getUUID());

            ServerScoreboard scoreboard = server.getScoreboard();
            String playerName = player.getGameProfile().name();

            if (cls == PlayerClass.NONE) {
                // Remove from any class team if they have none
                PlayerTeam currentTeam = scoreboard.getPlayersTeam(playerName);
                if (currentTeam != null && currentTeam.getName().startsWith(TEAM_PREFIX)) {
                    scoreboard.removePlayerFromTeam(playerName, currentTeam);
                }
                return;
            }

            String teamName = TEAM_PREFIX + cls.name().toLowerCase();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) {
                team = scoreboard.addPlayerTeam(teamName);
                String icon = getClassIcon(cls);
                team.setPlayerSuffix(Component.literal(" " + icon + cls.getDisplayName())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(cls.getColor()))));
            }

            // Only add if not already on this team
            PlayerTeam currentTeam = scoreboard.getPlayersTeam(playerName);
            if (currentTeam != team) {
                if (currentTeam != null && currentTeam.getName().startsWith(TEAM_PREFIX)) {
                    scoreboard.removePlayerFromTeam(playerName, currentTeam);
                }
                scoreboard.addPlayerToTeam(playerName, team);
            }
        } catch (Exception ignored) {
            // PlayerClassManager may not be initialized yet
        }
    }

    private static String getClassIcon(PlayerClass cls) {
        return switch (cls) {
            case PALADIN -> "\u2694 ";
            case WARRIOR -> "\u2694 ";
            case WIZARD -> "\u2726 ";
            case ROGUE -> "\u2020 ";
            case RANGER -> "\u27B3 ";
            default -> "";
        };
    }
}

