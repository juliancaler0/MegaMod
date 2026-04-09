package com.ultra.megamod.feature.citizen.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.ColonyStatisticsManager;
import com.ultra.megamod.feature.citizen.data.FactionData;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.raid.ColonyRaidManager;
import com.ultra.megamod.feature.citizen.raid.RaiderCulture;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Full /mc command tree matching MegaColonies spec.
 * Base commands, colony subcommands, citizens subcommands, kill subcommands.
 */
public class ColonyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mc")
                // ==================== Base Commands ====================
                .then(Commands.literal("backup")
                    .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                    .executes(ctx -> backup(ctx.getSource()))
                )
                .then(Commands.literal("home")
                    .executes(ctx -> home(ctx.getSource()))
                )
                .then(Commands.literal("rtp")
                    .executes(ctx -> randomTeleport(ctx.getSource()))
                )
                .then(Commands.literal("whereami")
                    .executes(ctx -> whereAmI(ctx.getSource()))
                )
                .then(Commands.literal("whoami")
                    .executes(ctx -> whoAmI(ctx.getSource()))
                )
                .then(Commands.literal("help")
                    .executes(ctx -> help(ctx.getSource()))
                )

                // ==================== Colony Subcommands ====================
                .then(Commands.literal("colony")
                    .then(Commands.literal("info")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyInfo(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(ctx -> colonyList(ctx.getSource()))
                    )
                    .then(Commands.literal("teleport")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyTeleport(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("claim")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyClaim(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"), 1))
                            .then(Commands.argument("chunks", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> colonyClaim(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"),
                                    IntegerArgumentType.getInteger(ctx, "chunks")))
                            )
                        )
                    )
                    .then(Commands.literal("delete")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyDelete(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("setowner")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> colonySetOwner(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"),
                                    EntityArgument.getPlayer(ctx, "player")))
                            )
                        )
                    )
                    .then(Commands.literal("addOfficer")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> colonyAddOfficer(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"),
                                    EntityArgument.getPlayer(ctx, "player")))
                            )
                        )
                    )
                    .then(Commands.literal("raid")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.literal("now")
                            .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> colonyRaid(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"), true))
                            )
                        )
                        .then(Commands.literal("tonight")
                            .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> colonyRaid(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"), false))
                            )
                        )
                    )
                    .then(Commands.literal("requestsystem-reset")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyRequestReset(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("setAbandoned")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonySetAbandoned(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("canSpawnRaiders")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> colonyCanSpawnRaiders(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"),
                                    BoolArgumentType.getBool(ctx, "enabled")))
                            )
                        )
                    )
                    .then(Commands.literal("showClaim")
                        .executes(ctx -> colonyShowClaim(ctx.getSource()))
                    )
                    .then(Commands.literal("printStats")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyPrintStats(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("unclaim")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyUnclaim(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                    .then(Commands.literal("requestsystem-reset-all")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .executes(ctx -> colonyRequestResetAll(ctx.getSource()))
                    )
                    .then(Commands.literal("export")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> colonyExport(ctx.getSource(),
                                StringArgumentType.getString(ctx, "id")))
                        )
                    )
                )

                // ==================== Citizens Subcommands ====================
                .then(Commands.literal("citizens")
                    .then(Commands.literal("info")
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .then(Commands.argument("citizenId", StringArgumentType.word())
                                .executes(ctx -> citizensInfo(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "colonyId"),
                                    StringArgumentType.getString(ctx, "citizenId")))
                            )
                        )
                    )
                    .then(Commands.literal("list")
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .executes(ctx -> citizensList(ctx.getSource(),
                                StringArgumentType.getString(ctx, "colonyId")))
                        )
                    )
                    .then(Commands.literal("kill")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .then(Commands.argument("citizenId", StringArgumentType.word())
                                .executes(ctx -> citizensKill(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "colonyId"),
                                    StringArgumentType.getString(ctx, "citizenId")))
                            )
                        )
                    )
                    .then(Commands.literal("spawnNew")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .executes(ctx -> citizensSpawnNew(ctx.getSource(),
                                StringArgumentType.getString(ctx, "colonyId")))
                        )
                    )
                    .then(Commands.literal("teleport")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .then(Commands.argument("citizenId", StringArgumentType.word())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                    .executes(ctx -> citizensTeleport(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "colonyId"),
                                        StringArgumentType.getString(ctx, "citizenId"),
                                        BlockPosArgument.getBlockPos(ctx, "pos")))
                                )
                            )
                        )
                    )
                    .then(Commands.literal("walk")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .then(Commands.argument("citizenId", StringArgumentType.word())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                    .executes(ctx -> citizensWalk(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "colonyId"),
                                        StringArgumentType.getString(ctx, "citizenId"),
                                        BlockPosArgument.getBlockPos(ctx, "pos")))
                                )
                            )
                        )
                    )
                    .then(Commands.literal("reload")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .then(Commands.argument("citizenId", StringArgumentType.word())
                                .executes(ctx -> citizensReload(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "colonyId"),
                                    StringArgumentType.getString(ctx, "citizenId")))
                            )
                        )
                    )
                    .then(Commands.literal("modify")
                        .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                        .then(Commands.argument("colonyId", StringArgumentType.word())
                            .then(Commands.argument("citizenId", StringArgumentType.word())
                                .then(Commands.literal("hunger")
                                    .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                                        .executes(ctx -> citizensModifyHunger(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "colonyId"),
                                            StringArgumentType.getString(ctx, "citizenId"),
                                            IntegerArgumentType.getInteger(ctx, "value")))
                                    )
                                )
                                .then(Commands.literal("health")
                                    .then(Commands.argument("value", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> citizensModifyHealth(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "colonyId"),
                                            StringArgumentType.getString(ctx, "citizenId"),
                                            IntegerArgumentType.getInteger(ctx, "value")))
                                    )
                                )
                            )
                        )
                    )
                )

                // ==================== Kill Subcommands ====================
                .then(Commands.literal("kill")
                    .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                    .then(Commands.literal("raider")
                        .executes(ctx -> killRaiders(ctx.getSource()))
                    )
                    .then(Commands.literal("animals")
                        .executes(ctx -> killAnimals(ctx.getSource()))
                    )
                    .then(Commands.literal("monster")
                        .executes(ctx -> killMonsters(ctx.getSource()))
                    )
                    .then(Commands.literal("chicken")
                        .executes(ctx -> killAnimalType(ctx.getSource(), Chicken.class, "chickens"))
                    )
                    .then(Commands.literal("cow")
                        .executes(ctx -> killAnimalType(ctx.getSource(), Cow.class, "cows"))
                    )
                    .then(Commands.literal("pig")
                        .executes(ctx -> killAnimalType(ctx.getSource(), Pig.class, "pigs"))
                    )
                    .then(Commands.literal("sheep")
                        .executes(ctx -> killAnimalType(ctx.getSource(), Sheep.class, "sheep"))
                    )
                )
        );
    }

    // ==================== Base Commands ====================

    private static int backup(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        try {
            FactionManager.get(level).saveToDisk(level);
            CitizenManager.get(level).saveToDisk(level);
            ColonyStatisticsManager.saveAll(level);
            ClaimManager.get(level).saveToDisk(level);
            source.sendSuccess(() -> Component.literal("All colony data backed up successfully.")
                .withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to backup colony data: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    private static int home(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getPlayerFactionData(player.getUUID());
        if (faction == null) {
            source.sendFailure(Component.literal("You are not in a colony. Create one first."));
            return 0;
        }

        BlockPos center = faction.getTownChestPos();
        if (center == null || center.equals(BlockPos.ZERO)) {
            source.sendFailure(Component.literal("Your colony has no Town Hall placed."));
            return 0;
        }

        player.teleportTo(level, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
            java.util.Set.of(), player.getYRot(), player.getXRot(), true);
        source.sendSuccess(() -> Component.literal("Teleported to your Town Hall.")
            .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int randomTeleport(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        // Random teleport 1000-5000 blocks away from current position
        Random rand = new Random();
        int distance = 1000 + rand.nextInt(4001);
        double angle = rand.nextDouble() * Math.PI * 2;
        int newX = (int) (player.getX() + Math.cos(angle) * distance);
        int newZ = (int) (player.getZ() + Math.sin(angle) * distance);
        int newY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, newX, newZ);

        player.teleportTo(level, newX + 0.5, newY + 1, newZ + 0.5,
            java.util.Set.of(), player.getYRot(), player.getXRot(), true);
        source.sendSuccess(() -> Component.literal("Randomly teleported to (" + newX + ", " + newY + ", " + newZ + ").")
            .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int whereAmI(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        Collection<FactionData> factions = FactionManager.get(level).getAllFactions();
        double nearestDist = Double.MAX_VALUE;
        String nearestName = "None";

        for (FactionData faction : factions) {
            BlockPos center = faction.getTownChestPos();
            if (center != null && !center.equals(BlockPos.ZERO)) {
                double dist = Math.sqrt(player.blockPosition().distSqr(center));
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestName = faction.getDisplayName();
                }
            }
        }

        if (nearestDist == Double.MAX_VALUE) {
            source.sendSuccess(() -> Component.literal("No colonies exist on this server.")
                .withStyle(ChatFormatting.GRAY), false);
        } else {
            String colonyName = nearestName;
            int dist = (int) nearestDist;
            source.sendSuccess(() -> Component.literal("Nearest colony: " + colonyName + " (" + dist + " blocks away)")
                .withStyle(ChatFormatting.YELLOW), false);
        }
        return 1;
    }

    private static int whoAmI(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getPlayerFactionData(player.getUUID());
        if (faction == null) {
            source.sendSuccess(() -> Component.literal("You are not part of any colony.")
                .withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        int citizenCount = CitizenManager.get(level).getCitizenCount(player.getUUID());
        boolean isLeader = player.getUUID().equals(faction.getLeaderUuid());

        source.sendSuccess(() -> Component.literal("=== Your Colony Info ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Colony: " + faction.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("Faction ID: " + faction.getFactionId()).withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("Role: " + (isLeader ? "Owner" : "Member")).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Your Citizens: " + citizenCount).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Members: " + faction.getMemberCount()).withStyle(ChatFormatting.WHITE), false);
        return 1;
    }

    // ==================== Colony Subcommands ====================

    private static int colonyInfo(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        ColonyStatisticsManager stats = ColonyStatisticsManager.get(level, factionId);

        source.sendSuccess(() -> Component.literal("=== Colony Info ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Name: " + faction.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("Faction ID: " + factionId).withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("Members: " + faction.getMemberCount()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Buildings Built: " + stats.getTotal(ColonyStatisticsManager.BUILD_BUILT)).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Raids Survived: " + stats.getTotal(ColonyStatisticsManager.RAIDS_SURVIVED)).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Mobs Killed: " + stats.getTotal(ColonyStatisticsManager.MOBS_KILLED)).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Day: " + stats.getCurrentDay()).withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int colonyList(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        Collection<FactionData> factions = FactionManager.get(level).getAllFactions();
        if (factions.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No colonies exist on this server.")
                .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("=== Colonies ===").withStyle(ChatFormatting.GOLD), false);
        for (FactionData faction : factions) {
            source.sendSuccess(() -> Component.literal(
                "  " + faction.getDisplayName() + " [" + faction.getFactionId() + "] - "
                    + faction.getMemberCount() + " members"
            ).withStyle(ChatFormatting.YELLOW), false);
        }

        return 1;
    }

    private static int colonyTeleport(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        // Check permission: admin/op or own faction
        String playerFactionId = FactionManager.get(level).getPlayerFaction(player.getUUID());
        boolean isAdmin = AdminSystem.isAdmin(player) || Commands.LEVEL_GAMEMASTERS.check(source.permissions());
        if (!isAdmin && !factionId.equals(playerFactionId)) {
            source.sendFailure(Component.literal("You can only teleport to your own colony."));
            return 0;
        }

        BlockPos center = faction.getTownChestPos();
        if (center == null || center.equals(BlockPos.ZERO)) {
            source.sendFailure(Component.literal("Colony center (Town Hall) is not set."));
            return 0;
        }

        player.teleportTo(level, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
            java.util.Set.of(), player.getYRot(), player.getXRot(), true);
        source.sendSuccess(() -> Component.literal("Teleported to colony " + faction.getDisplayName() + ".")
            .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int colonyClaim(CommandSourceStack source, String factionId, int chunks) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        ClaimManager claimManager = ClaimManager.get(level);
        ChunkPos playerChunk = new ChunkPos(player.blockPosition());
        int claimed = 0;

        // Claim chunks in a spiral around the player
        for (int dx = 0; dx < chunks && dx < 8; dx++) {
            for (int dz = 0; dz < chunks && dz < 8; dz++) {
                int cx = playerChunk.x + dx;
                int cz = playerChunk.z + dz;
                String existing = claimManager.getFactionAtChunk(cx, cz);
                if (existing == null) {
                    if (claimManager.claimChunk(factionId, cx, cz)) {
                        claimed++;
                    }
                }
                if (claimed >= chunks) break;
            }
            if (claimed >= chunks) break;
        }

        if (claimed > 0) {
            claimManager.saveToDisk(level);
            int finalClaimed = claimed;
            source.sendSuccess(() -> Component.literal("Claimed " + finalClaimed + " chunk(s) for colony " + factionId + ".")
                .withStyle(ChatFormatting.GREEN), false);
        } else {
            source.sendFailure(Component.literal("No chunks could be claimed. They may already be owned."));
        }

        return claimed > 0 ? 1 : 0;
    }

    private static int colonyDelete(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        ClaimManager.get(level).removeFactionClaims(factionId);
        ClaimManager.get(level).saveToDisk(level);

        boolean deleted = FactionManager.get(level).deleteFaction(factionId);
        if (!deleted) {
            source.sendFailure(Component.literal("Failed to delete colony '" + factionId + "'."));
            return 0;
        }

        FactionManager.get(level).saveToDisk(level);
        MegaMod.LOGGER.info("Admin {} deleted colony {}", player.getGameProfile().name(), factionId);
        source.sendSuccess(() -> Component.literal("Deleted colony '" + factionId + "' and removed all claims.")
            .withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static int colonySetOwner(CommandSourceStack source, String factionId, ServerPlayer newOwner) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        if (!faction.isMember(newOwner.getUUID())) {
            FactionManager.get(level).joinFaction(newOwner.getUUID(), factionId);
        }

        UUID oldLeader = faction.getLeaderUuid();
        faction.setLeaderUuid(newOwner.getUUID());
        faction.setMemberRank(newOwner.getUUID(), FactionData.PlayerRank.LEADER);
        if (oldLeader != null && !oldLeader.equals(newOwner.getUUID())) {
            faction.setMemberRank(oldLeader, FactionData.PlayerRank.MEMBER);
        }

        FactionManager.get(level).saveToDisk(level);
        MegaMod.LOGGER.info("Admin {} transferred colony {} ownership to {}",
            player.getGameProfile().name(), factionId, newOwner.getGameProfile().name());
        source.sendSuccess(() -> Component.literal("Transferred colony '" + factionId + "' ownership to "
            + newOwner.getGameProfile().name() + ".").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int colonyAddOfficer(CommandSourceStack source, String factionId, ServerPlayer officer) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        if (!faction.isMember(officer.getUUID())) {
            FactionManager.get(level).joinFaction(officer.getUUID(), factionId);
        }

        faction.setMemberRank(officer.getUUID(), FactionData.PlayerRank.OFFICER);
        FactionManager.get(level).saveToDisk(level);
        source.sendSuccess(() -> Component.literal("Added " + officer.getGameProfile().name()
            + " as officer of colony '" + factionId + "'.").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int colonyRaid(CommandSourceStack source, String factionId, boolean now) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        BlockPos raidCenter = faction.getTownChestPos();
        if (raidCenter == null || raidCenter.equals(BlockPos.ZERO)) {
            raidCenter = player.blockPosition();
        }

        if (now) {
            ColonyRaidManager.startCultureRaid(level, raidCenter, RaiderCulture.BARBARIAN);
            source.sendSuccess(() -> Component.literal("Raid started NOW on colony " + faction.getDisplayName() + "!")
                .withStyle(ChatFormatting.RED), true);
        } else {
            // Schedule raid for tonight (next tick 13000 = dusk)
            long dayTime = level.getDayTime() % 24000L;
            long ticksUntilNight = dayTime < 13000L ? (13000L - dayTime) : (24000L - dayTime + 13000L);
            // Store in faction data for the raid manager to pick up
            source.sendSuccess(() -> Component.literal("Raid scheduled for tonight on colony " + faction.getDisplayName()
                + " (in ~" + (ticksUntilNight / 20) + " seconds).").withStyle(ChatFormatting.RED), true);
            BlockPos finalCenter = raidCenter;
            level.getServer().execute(() -> {
                // Simple delayed execution using server scheduler
                ColonyRaidManager.scheduleRaid(level, finalCenter, ticksUntilNight);
            });
        }

        MegaMod.LOGGER.info("Admin {} {} raid on colony {}",
            player.getGameProfile().name(), now ? "started" : "scheduled", factionId);
        return 1;
    }

    private static int colonyRequestReset(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        // Reset citizen needs/requests for this colony
        CitizenManager cm = CitizenManager.get(level);
        cm.resetRequests(factionId);
        cm.saveToDisk(level);

        source.sendSuccess(() -> Component.literal("Request system reset for colony '" + factionId + "'.")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int colonySetAbandoned(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        faction.setAbandoned(true);
        FactionManager.get(level).saveToDisk(level);
        source.sendSuccess(() -> Component.literal("Colony '" + factionId + "' marked as abandoned.")
            .withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    private static int colonyCanSpawnRaiders(CommandSourceStack source, String factionId, boolean enabled) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        faction.setRaidersEnabled(enabled);
        FactionManager.get(level).saveToDisk(level);
        source.sendSuccess(() -> Component.literal("Raider spawning for colony '" + factionId + "' set to: " + enabled + ".")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int colonyShowClaim(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        ChunkPos chunk = new ChunkPos(player.blockPosition());
        ClaimManager cm = ClaimManager.get(level);
        String owner = cm.getFactionAtChunk(chunk.x, chunk.z);

        if (owner == null) {
            source.sendSuccess(() -> Component.literal("Chunk (" + chunk.x + ", " + chunk.z + ") is unclaimed.")
                .withStyle(ChatFormatting.GRAY), false);
        } else {
            FactionData faction = FactionManager.get(level).getFaction(owner);
            String displayName = faction != null ? faction.getDisplayName() : owner;
            source.sendSuccess(() -> Component.literal("=== Claim Info ===").withStyle(ChatFormatting.GOLD), false);
            source.sendSuccess(() -> Component.literal("Chunk: (" + chunk.x + ", " + chunk.z + ")")
                .withStyle(ChatFormatting.WHITE), false);
            source.sendSuccess(() -> Component.literal("Block Position: " + player.blockPosition().toShortString())
                .withStyle(ChatFormatting.GRAY), false);
            source.sendSuccess(() -> Component.literal("Colony: " + displayName + " [" + owner + "]")
                .withStyle(ChatFormatting.YELLOW), false);
        }
        return 1;
    }

    private static int colonyPrintStats(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        ColonyStatisticsManager stats = ColonyStatisticsManager.get(level, factionId);
        int citizenCount = CitizenManager.get(level).getCitizenCount(
            faction.getLeaderUuid() != null ? faction.getLeaderUuid() : new UUID(0, 0));

        source.sendSuccess(() -> Component.literal("=== Colony Stats: " + faction.getDisplayName() + " ===")
            .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("ID: " + factionId).withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("Members: " + faction.getMemberCount()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Citizens: " + citizenCount + "/" + faction.getMaxNPCs())
            .withStyle(ChatFormatting.WHITE), false);

        BlockPos center = faction.getTownChestPos();
        if (center != null && !center.equals(BlockPos.ZERO)) {
            source.sendSuccess(() -> Component.literal("Center: " + center.toShortString())
                .withStyle(ChatFormatting.GREEN), false);
        }

        source.sendSuccess(() -> Component.literal("Day: " + stats.getCurrentDay()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Happiness: " + String.format("%.1f", faction.getColony().getOverallHappiness()))
            .withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("Abandoned: " + faction.isAbandoned()).withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(() -> Component.literal("Raiders: " + (faction.isRaidersEnabled() ? "Enabled" : "Disabled"))
            .withStyle(ChatFormatting.GRAY), false);

        source.sendSuccess(() -> Component.literal("--- Statistics ---").withStyle(ChatFormatting.DARK_AQUA), false);
        source.sendSuccess(() -> Component.literal("Buildings Built: " + stats.getTotal(ColonyStatisticsManager.BUILD_BUILT))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Raids Survived: " + stats.getTotal(ColonyStatisticsManager.RAIDS_SURVIVED))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Raids Failed: " + stats.getTotal(ColonyStatisticsManager.RAIDS_FAILED))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Mobs Killed: " + stats.getTotal(ColonyStatisticsManager.MOBS_KILLED))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Trees Cut: " + stats.getTotal(ColonyStatisticsManager.TREE_CUT))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Ores Mined: " + stats.getTotal(ColonyStatisticsManager.ORES_MINED))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Crops Harvested: " + stats.getTotal(ColonyStatisticsManager.CROPS_HARVESTED))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Fish Caught: " + stats.getTotal(ColonyStatisticsManager.FISH_CAUGHT))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Coins Earned: " + stats.getTotal(ColonyStatisticsManager.COINS_EARNED))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Coins Spent: " + stats.getTotal(ColonyStatisticsManager.COINS_SPENT))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Deaths: " + stats.getTotal(ColonyStatisticsManager.DEATH))
            .withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(() -> Component.literal("Births: " + stats.getTotal(ColonyStatisticsManager.BIRTH))
            .withStyle(ChatFormatting.WHITE), false);

        MegaMod.LOGGER.info("Colony {} stats printed by {}", factionId, player.getGameProfile().name());
        return 1;
    }

    private static int colonyUnclaim(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        ClaimManager cm = ClaimManager.get(level);
        cm.removeFactionClaims(factionId);
        cm.saveToDisk(level);

        MegaMod.LOGGER.info("Admin {} removed all claims for colony {}", player.getGameProfile().name(), factionId);
        source.sendSuccess(() -> Component.literal("Removed all chunk claims for colony '" + factionId + "'.")
            .withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static int colonyRequestResetAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        CitizenManager cm = CitizenManager.get(level);
        Collection<FactionData> factions = FactionManager.get(level).getAllFactions();
        int count = 0;
        for (FactionData faction : factions) {
            cm.resetRequests(faction.getFactionId());
            count++;
        }
        cm.saveToDisk(level);

        int finalCount = count;
        source.sendSuccess(() -> Component.literal("Request system reset for all " + finalCount + " colonies.")
            .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int colonyExport(CommandSourceStack source, String factionId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(factionId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + factionId + "' not found."));
            return 0;
        }

        try {
            // Save all data first to ensure it's current
            FactionManager.get(level).saveToDisk(level);
            CitizenManager.get(level).saveToDisk(level);
            ColonyStatisticsManager.saveAll(level);
            ClaimManager.get(level).saveToDisk(level);

            MegaMod.LOGGER.info("Admin {} exported colony {} data", player.getGameProfile().name(), factionId);
            source.sendSuccess(() -> Component.literal("Colony '" + factionId + "' data exported successfully. "
                + "Data saved to world/data/ directory.").withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to export colony data: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== MegaMod Colony Commands (/mc) ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("--- General ---").withStyle(ChatFormatting.DARK_AQUA), false);
        source.sendSuccess(() -> Component.literal("  /mc home - Teleport to your Town Hall").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc rtp - Random teleport").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc whereami - Find nearest colony").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc whoami - Show your colony info").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc backup - Save all colony data [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("--- Colony ---").withStyle(ChatFormatting.DARK_AQUA), false);
        source.sendSuccess(() -> Component.literal("  /mc colony list - List all colonies").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony info <id> - Colony details").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony teleport <id> - TP to colony").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony showClaim - Check chunk claim").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony printStats <id> - Full stats [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony claim <id> [chunks] - Claim chunks [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony unclaim <id> - Remove all claims [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony delete <id> - Delete colony [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony setowner <id> <player> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony addOfficer <id> <player> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony raid now|tonight <id> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony setAbandoned <id> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony canSpawnRaiders <id> <bool> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony requestsystem-reset <id> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony requestsystem-reset-all [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc colony export <id> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("--- Citizens ---").withStyle(ChatFormatting.DARK_AQUA), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens list <colonyId>").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens info <colonyId> <name>").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens kill <colonyId> <name> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens spawnNew <colonyId> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens teleport <colonyId> <name> <pos> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens walk <colonyId> <name> <pos> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens reload <colonyId> <name> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("  /mc citizens modify <colonyId> <name> hunger|health <val> [OP]").withStyle(ChatFormatting.YELLOW), false);
        source.sendSuccess(() -> Component.literal("--- Kill ---").withStyle(ChatFormatting.DARK_AQUA), false);
        source.sendSuccess(() -> Component.literal("  /mc kill raider|animals|monster|chicken|cow|pig|sheep [OP]").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    // ==================== Citizens Subcommands ====================

    private static int citizensInfo(CommandSourceStack source, String colonyId, String citizenName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(colonyId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + colonyId + "' not found."));
            return 0;
        }

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    UUID ownerUUID = citizen.getOwnerUUID();
                    if (ownerUUID != null && faction.isMember(ownerUUID)) {
                        source.sendSuccess(() -> Component.literal("=== Citizen Info ===").withStyle(ChatFormatting.GOLD), false);
                        source.sendSuccess(() -> Component.literal("Name: " + citizen.getCitizenName()).withStyle(ChatFormatting.YELLOW), false);
                        source.sendSuccess(() -> Component.literal("Job: " + citizen.getCitizenJob().getDisplayName()).withStyle(ChatFormatting.WHITE), false);
                        source.sendSuccess(() -> Component.literal("Health: " + String.format("%.1f", citizen.getHealth())
                            + "/" + String.format("%.1f", citizen.getMaxHealth())).withStyle(ChatFormatting.RED), false);
                        source.sendSuccess(() -> Component.literal("Hunger: " + citizen.getHunger()).withStyle(ChatFormatting.YELLOW), false);
                        source.sendSuccess(() -> Component.literal("Position: " + citizen.blockPosition().toShortString()).withStyle(ChatFormatting.GRAY), false);
                        source.sendSuccess(() -> Component.literal("Happiness: " + String.format("%.1f",
                            citizen.getHappinessData().getHappiness())).withStyle(ChatFormatting.AQUA), false);
                        return 1;
                    }
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found in colony '" + colonyId + "'."));
        return 0;
    }

    private static int citizensList(CommandSourceStack source, String colonyId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(colonyId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + colonyId + "' not found."));
            return 0;
        }

        List<MCEntityCitizen> colonyCitizens = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                UUID ownerUUID = citizen.getOwnerUUID();
                if (ownerUUID != null && faction.isMember(ownerUUID)) {
                    colonyCitizens.add(citizen);
                }
            }
        }

        if (colonyCitizens.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No citizens found in colony '" + colonyId + "'.")
                .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("=== Citizens of " + faction.getDisplayName() + " (" + colonyCitizens.size() + ") ===")
            .withStyle(ChatFormatting.GOLD), false);
        for (MCEntityCitizen citizen : colonyCitizens) {
            source.sendSuccess(() -> Component.literal(
                "  " + citizen.getCitizenName() + " - " + citizen.getCitizenJob().getDisplayName()
                + " [HP: " + String.format("%.0f", citizen.getHealth()) + "]"
            ).withStyle(ChatFormatting.YELLOW), false);
        }

        return 1;
    }

    private static int citizensKill(CommandSourceStack source, String colonyId, String citizenName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(colonyId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + colonyId + "' not found."));
            return 0;
        }

        for (Entity entity : java.util.stream.StreamSupport.stream(
            level.getAllEntities().spliterator(), false).toList()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    UUID ownerUUID = citizen.getOwnerUUID();
                    if (ownerUUID != null && faction.isMember(ownerUUID)) {
                        citizen.kill(level);
                        CitizenManager.get(level).unregisterCitizen(citizen.getUUID());
                        source.sendSuccess(() -> Component.literal("Killed citizen: " + citizenName)
                            .withStyle(ChatFormatting.RED), true);
                        return 1;
                    }
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found in colony '" + colonyId + "'."));
        return 0;
    }

    private static int citizensSpawnNew(CommandSourceStack source, String colonyId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(colonyId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + colonyId + "' not found."));
            return 0;
        }

        UUID leader = faction.getLeaderUuid();
        if (leader == null) {
            source.sendFailure(Component.literal("Colony has no leader."));
            return 0;
        }

        try {
            var entityType = com.ultra.megamod.feature.citizen.CitizenRegistry.MC_CITIZEN.get();
            var entity = entityType.create(level, EntitySpawnReason.COMMAND);
            if (entity != null) {
                BlockPos center = faction.getTownChestPos();
                if (center != null && !center.equals(BlockPos.ZERO)) {
                    entity.setPos(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5);
                } else {
                    entity.setPos(player.getX(), player.getY(), player.getZ());
                }
                if (entity instanceof MCEntityCitizen citizenEntity) {
                    citizenEntity.setOwnerUUID(leader);
                }
                level.addFreshEntity(entity);
                source.sendSuccess(() -> Component.literal("Spawned a new citizen in colony " + colonyId + ".")
                    .withStyle(ChatFormatting.GREEN), true);
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to spawn citizen: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    private static int citizensTeleport(CommandSourceStack source, String colonyId, String citizenName, BlockPos pos) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    citizen.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    source.sendSuccess(() -> Component.literal("Teleported " + citizenName + " to "
                        + pos.toShortString() + ".").withStyle(ChatFormatting.GREEN), false);
                    return 1;
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found."));
        return 0;
    }

    private static int citizensWalk(CommandSourceStack source, String colonyId, String citizenName, BlockPos pos) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    citizen.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0);
                    source.sendSuccess(() -> Component.literal("Citizen " + citizenName + " is walking to "
                        + pos.toShortString() + ".").withStyle(ChatFormatting.GREEN), false);
                    return 1;
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found."));
        return 0;
    }

    private static int citizensReload(CommandSourceStack source, String colonyId, String citizenName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    // Re-initialize goals and refresh AI
                    citizen.getNavigation().stop();
                    source.sendSuccess(() -> Component.literal("Reloaded citizen: " + citizenName)
                        .withStyle(ChatFormatting.GREEN), false);
                    return 1;
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found."));
        return 0;
    }

    private static int citizensModifyHunger(CommandSourceStack source, String colonyId, String citizenName, int value) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(colonyId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + colonyId + "' not found."));
            return 0;
        }

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    UUID ownerUUID = citizen.getOwnerUUID();
                    if (ownerUUID != null && faction.isMember(ownerUUID)) {
                        citizen.setHunger(value);
                        source.sendSuccess(() -> Component.literal("Set " + citizenName + "'s hunger to " + value + ".")
                            .withStyle(ChatFormatting.GREEN), false);
                        return 1;
                    }
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found in colony '" + colonyId + "'."));
        return 0;
    }

    private static int citizensModifyHealth(CommandSourceStack source, String colonyId, String citizenName, int value) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        FactionData faction = FactionManager.get(level).getFaction(colonyId);
        if (faction == null) {
            source.sendFailure(Component.literal("Colony '" + colonyId + "' not found."));
            return 0;
        }

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                if (citizen.getCitizenName().equalsIgnoreCase(citizenName)) {
                    UUID ownerUUID = citizen.getOwnerUUID();
                    if (ownerUUID != null && faction.isMember(ownerUUID)) {
                        citizen.setHealth(Math.min(value, citizen.getMaxHealth()));
                        source.sendSuccess(() -> Component.literal("Set " + citizenName + "'s health to "
                            + String.format("%.0f", citizen.getHealth()) + ".")
                            .withStyle(ChatFormatting.GREEN), false);
                        return 1;
                    }
                }
            }
        }

        source.sendFailure(Component.literal("Citizen '" + citizenName + "' not found in colony '" + colonyId + "'."));
        return 0;
    }

    // ==================== Kill Subcommands ====================

    private static int killRaiders(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        int killed = 0;
        for (Entity entity : java.util.stream.StreamSupport.stream(
            level.getAllEntities().spliterator(), false).toList()) {
            if (entity instanceof net.minecraft.world.entity.raid.Raider raider) {
                raider.kill(level);
                killed++;
            }
        }

        int finalKilled = killed;
        source.sendSuccess(() -> Component.literal("Killed " + finalKilled + " raider(s).")
            .withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static int killAnimals(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        // Kill all animals in claimed colony chunks
        ClaimManager cm = ClaimManager.get(level);
        int killed = 0;

        for (Entity entity : java.util.stream.StreamSupport.stream(
            level.getAllEntities().spliterator(), false).toList()) {
            if (entity instanceof Animal animal) {
                ChunkPos chunk = new ChunkPos(animal.blockPosition());
                if (cm.getFactionAtChunk(chunk.x, chunk.z) != null) {
                    animal.kill(level);
                    killed++;
                }
            }
        }

        int finalKilled = killed;
        source.sendSuccess(() -> Component.literal("Killed " + finalKilled + " animal(s) in colony territories.")
            .withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static int killMonsters(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        ClaimManager cm = ClaimManager.get(level);
        int killed = 0;

        for (Entity entity : java.util.stream.StreamSupport.stream(
            level.getAllEntities().spliterator(), false).toList()) {
            if (entity instanceof Monster monster) {
                ChunkPos chunk = new ChunkPos(monster.blockPosition());
                if (cm.getFactionAtChunk(chunk.x, chunk.z) != null) {
                    monster.kill(level);
                    killed++;
                }
            }
        }

        int finalKilled = killed;
        source.sendSuccess(() -> Component.literal("Killed " + finalKilled + " monster(s) in colony territories.")
            .withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static <T extends Animal> int killAnimalType(CommandSourceStack source, Class<T> animalClass, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel level = (ServerLevel) player.level();

        ClaimManager cm = ClaimManager.get(level);
        int killed = 0;

        for (Entity entity : java.util.stream.StreamSupport.stream(
            level.getAllEntities().spliterator(), false).toList()) {
            if (animalClass.isInstance(entity)) {
                ChunkPos chunk = new ChunkPos(entity.blockPosition());
                if (cm.getFactionAtChunk(chunk.x, chunk.z) != null) {
                    ((Animal) entity).kill(level);
                    killed++;
                }
            }
        }

        int finalKilled = killed;
        source.sendSuccess(() -> Component.literal("Killed " + finalKilled + " " + name + " in colony territories.")
            .withStyle(ChatFormatting.RED), true);
        return 1;
    }
}
