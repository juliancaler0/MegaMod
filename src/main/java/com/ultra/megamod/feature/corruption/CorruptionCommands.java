package com.ultra.megamod.feature.corruption;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.corruption.CorruptionManager.CorruptionZone;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@EventBusSubscriber(modid = MegaMod.MODID)
public class CorruptionCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("megamod").then(
                Commands.literal("corruption")
                    .then(Commands.literal("list").executes(ctx -> listZones(ctx.getSource())))
                    .then(Commands.literal("create")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                            .then(Commands.argument("z", IntegerArgumentType.integer())
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
                                    .executes(ctx -> createZone(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "x"),
                                        IntegerArgumentType.getInteger(ctx, "z"),
                                        IntegerArgumentType.getInteger(ctx, "tier")
                                    ))
                                )
                            )
                        )
                    )
                    .then(Commands.literal("remove")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("zoneId", IntegerArgumentType.integer(1))
                            .executes(ctx -> removeZone(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "zoneId")))
                        )
                    )
                    .then(Commands.literal("purge")
                        .then(Commands.argument("zoneId", IntegerArgumentType.integer(1))
                            .executes(ctx -> startPurge(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "zoneId")))
                        )
                    )
                    .then(Commands.literal("status").executes(ctx -> showStatus(ctx.getSource())))
                    .then(Commands.literal("clear_all")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .executes(ctx -> clearAll(ctx.getSource()))
                    )
            )
        );
    }

    private static int listZones(CommandSourceStack source) {
        ServerLevel level = source.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) {
            source.sendFailure(Component.literal("Corruption system is disabled."));
            return 0;
        }

        CorruptionManager cm = CorruptionManager.get(level);
        List<CorruptionZone> zones = cm.getAllZones();

        if (zones.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No corruption zones exist.").withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("=== Corruption Zones (" + cm.getActiveZoneCount() + " active) ===")
                .withStyle(ChatFormatting.DARK_PURPLE), false);

        for (CorruptionZone zone : zones) {
            String status = zone.active ? "ACTIVE" : "INACTIVE";
            ChatFormatting statusColor = zone.active ? ChatFormatting.RED : ChatFormatting.GRAY;
            ChatFormatting tierColor = switch (zone.tier) {
                case 1 -> ChatFormatting.GREEN;
                case 2 -> ChatFormatting.YELLOW;
                case 3 -> ChatFormatting.GOLD;
                case 4 -> ChatFormatting.DARK_RED;
                default -> ChatFormatting.WHITE;
            };

            source.sendSuccess(() -> Component.literal("  #" + zone.zoneId + " ")
                    .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("[" + status + "] ").withStyle(statusColor))
                    .append(Component.literal("Tier " + zone.tier + " ").withStyle(tierColor))
                    .append(Component.literal("@ (" + zone.centerX + ", " + zone.centerZ + ") ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("R:" + zone.radius + "/" + zone.maxRadius + " ").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("[" + zone.sourceType + "]").withStyle(ChatFormatting.DARK_GRAY)),
                    false);
        }

        return zones.size();
    }

    private static int createZone(CommandSourceStack source, int x, int z, int tier) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) {
            source.sendFailure(Component.literal("Corruption system is disabled."));
            return 0;
        }

        if (!AdminSystem.isAdmin(player)) {
            source.sendFailure(Component.literal("Only admins can create corruption zones."));
            return 0;
        }

        CorruptionManager cm = CorruptionManager.get(level);
        CorruptionZone zone = cm.createZone(x, z, tier, "admin", level.getServer().getTickCount());

        source.sendSuccess(() -> Component.literal("Created corruption zone #" + zone.zoneId + " at (" + x + ", " + z + ") tier " + tier)
                .withStyle(ChatFormatting.DARK_PURPLE), true);

        return 1;
    }

    private static int removeZone(CommandSourceStack source, int zoneId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) {
            source.sendFailure(Component.literal("Corruption system is disabled."));
            return 0;
        }

        if (!AdminSystem.isAdmin(player)) {
            source.sendFailure(Component.literal("Only admins can remove corruption zones."));
            return 0;
        }

        CorruptionManager cm = CorruptionManager.get(level);
        boolean removed = cm.removeZone(zoneId);

        if (removed) {
            source.sendSuccess(() -> Component.literal("Removed corruption zone #" + zoneId)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendFailure(Component.literal("Zone #" + zoneId + " not found."));
        }

        return removed ? 1 : 0;
    }

    private static int startPurge(CommandSourceStack source, int zoneId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) {
            source.sendFailure(Component.literal("Corruption system is disabled."));
            return 0;
        }

        CorruptionManager cm = CorruptionManager.get(level);
        PurgeManager pm = PurgeManager.get(level);

        if (pm.hasPurgeActive()) {
            source.sendFailure(Component.literal("A purge is already in progress!"));
            return 0;
        }

        CorruptionZone zone = cm.getZone(zoneId);
        if (zone == null || !zone.active) {
            source.sendFailure(Component.literal("Zone #" + zoneId + " not found or inactive."));
            return 0;
        }

        // Must be within 64 blocks (unless admin)
        if (!AdminSystem.isAdmin(player)) {
            double dx = player.getX() - zone.centerX;
            double dz = player.getZ() - zone.centerZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 64 + zone.radius) {
                source.sendFailure(Component.literal("You must be within 64 blocks of the corruption zone!"));
                return 0;
            }
        }

        PurgeManager.PurgeEvent purge = pm.startPurge(zoneId, player);
        if (purge != null) {
            source.sendSuccess(() -> Component.literal("Purge started! Kill " + purge.killsRequired + " corrupted mobs within 5 minutes!")
                    .withStyle(ChatFormatting.GOLD), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to start purge."));
            return 0;
        }
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("corruption")) {
            source.sendFailure(Component.literal("Corruption system is disabled."));
            return 0;
        }

        CorruptionManager cm = CorruptionManager.get(level);
        PurgeManager pm = PurgeManager.get(level);

        source.sendSuccess(() -> Component.literal("=== Corruption Status ===").withStyle(ChatFormatting.DARK_PURPLE), false);
        source.sendSuccess(() -> Component.literal("  Active Zones: " + cm.getActiveZoneCount())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        source.sendSuccess(() -> Component.literal("  Corrupted Chunks: ~" + cm.getTotalCorruptedChunks())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        source.sendSuccess(() -> Component.literal("  Spread: " + (cm.isSpreadEnabled() ? "ENABLED" : "DISABLED"))
                .withStyle(cm.isSpreadEnabled() ? ChatFormatting.RED : ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.literal("  Stats: " + cm.getTotalZonesCreated() + " created, " +
                cm.getTotalZonesDestroyed() + " destroyed, " + cm.getTotalPurgesCompleted() + " purges OK, " +
                cm.getTotalPurgesFailed() + " failed").withStyle(ChatFormatting.GRAY), false);

        // Show nearby zones if player
        if (source.getEntity() instanceof ServerPlayer player) {
            BlockPos pos = player.blockPosition();
            List<CorruptionZone> nearby = cm.getZonesInRange(pos, 256);
            if (!nearby.isEmpty()) {
                source.sendSuccess(() -> Component.literal("  Nearby Zones (" + nearby.size() + "):")
                        .withStyle(ChatFormatting.YELLOW), false);
                for (CorruptionZone zone : nearby) {
                    double dx = player.getX() - zone.centerX;
                    double dz = player.getZ() - zone.centerZ;
                    int dist = (int) Math.sqrt(dx * dx + dz * dz);
                    source.sendSuccess(() -> Component.literal("    #" + zone.zoneId + " Tier " + zone.tier +
                            " (" + dist + " blocks away, R:" + zone.radius + ")")
                            .withStyle(ChatFormatting.RED), false);
                }
            } else {
                source.sendSuccess(() -> Component.literal("  No corruption zones within 256 blocks.")
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }

        // Purge status
        if (pm.hasPurgeActive()) {
            PurgeManager.PurgeEvent purge = pm.getActivePurge();
            long ticksLeft = purge.getTimeRemainingTicks(level.getServer().getTickCount());
            long secsLeft = ticksLeft / 20;
            source.sendSuccess(() -> Component.literal("  PURGE ACTIVE on Zone #" + purge.targetZoneId +
                    " - " + purge.currentKills + "/" + purge.killsRequired + " kills, " + secsLeft + "s left")
                    .withStyle(ChatFormatting.GOLD), false);
        }

        return 1;
    }

    private static int clearAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getServer().overworld();
        if (!AdminSystem.isAdmin(player)) {
            source.sendFailure(Component.literal("Only admins can clear all corruption."));
            return 0;
        }

        CorruptionManager cm = CorruptionManager.get(level);
        int count = cm.getActiveZoneCount();
        cm.clearAll();

        PurgeManager pm = PurgeManager.get(level);
        if (pm.hasPurgeActive()) {
            pm.stopPurge(level);
        }

        source.sendSuccess(() -> Component.literal("Cleared " + count + " corruption zones.")
                .withStyle(ChatFormatting.GREEN), true);

        // Broadcast
        Component broadcast = Component.literal("[Corruption] ")
                .withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal("All corruption has been purged by an admin!")
                        .withStyle(ChatFormatting.GREEN));
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(broadcast);
        }

        return count;
    }
}
