/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.player.Player$BedSleepingProblem
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.CommandEvent
 *  net.neoforged.neoforge.event.entity.EntityTeleportEvent$EnderPearl
 *  net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 */
package com.ultra.megamod.feature.dungeons;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class DungeonRules {
    private static final Set<Block> BREAKABLE_BLOCKS = Set.of(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.SPAWNER, Blocks.COBWEB, Blocks.STONE_PRESSURE_PLATE, Blocks.OAK_PRESSURE_PLATE, Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL);
    private static final Set<Block> NO_DROP_BREAKABLE = Set.of(Blocks.IRON_BARS, Blocks.FLOWER_POT, Blocks.DECORATED_POT, Blocks.CANDLE, Blocks.WHITE_CANDLE, Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.LIGHT_BLUE_CANDLE, Blocks.YELLOW_CANDLE, Blocks.LIME_CANDLE, Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE, Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE, Blocks.BLUE_CANDLE, Blocks.BROWN_CANDLE, Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE);
    private static final Set<String> BLOCKED_COMMANDS = Set.of("home", "tp", "teleport", "back", "spawn", "warp", "rtp", "tpa", "tpahere", "tpaccept", "sethome");

    private static boolean isAdminDungeonBypassing(ServerPlayer player) {
        if (!AdminSystem.isAdmin(player)) return false;
        ServerLevel level = player.level().getServer().overworld();
        return FeatureToggleManager.get(level).isEnabled("admin_dungeon_bypass");
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isFallFlying()) return;
        if (!isInDungeon(player)) return;
        if (isAdminDungeonBypassing(player)) return;
        player.stopFallFlying();
        if (player.level().getGameTime() % 20L == 0L) {
            player.sendSystemMessage(Component.literal("Elytra flight is disabled in dungeons!").withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        if (!DungeonRules.isInDungeon(player)) {
            return;
        }
        if (isAdminDungeonBypassing(player)) {
            return;
        }
        event.setCanceled(true);
        player.sendSystemMessage((Component)Component.literal((String)"Ender pearls are disabled in dungeons!").withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent
    public static void onPlayerSleep(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (!DungeonRules.isInDungeon(player)) {
            return;
        }
        if (isAdminDungeonBypassing(player)) {
            return;
        }
        event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
        player.sendSystemMessage((Component)Component.literal((String)"You cannot sleep in a dungeon!").withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (!DungeonRules.isInDungeon(player2)) {
            return;
        }
        if (isAdminDungeonBypassing(player2)) {
            return;
        }
        Block block = event.getState().getBlock();
        if (NO_DROP_BREAKABLE.contains(block)) {
            // Allow breaking but prevent drops: cancel normal break, manually remove block
            event.setCanceled(true);
            ((ServerLevel) player2.level()).setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (!BREAKABLE_BLOCKS.contains(block)) {
            event.setCanceled(true);
            if (player2.level().getGameTime() % 20L == 0L) {
                player2.sendSystemMessage((Component)Component.literal((String)"You cannot break dungeon walls!").withStyle(ChatFormatting.RED));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player2)) return;
        if (!DungeonRules.isInDungeon(player2)) return;
        if (isAdminDungeonBypassing(player2)) return;
        event.setCanceled(true);
        if (player2.level().getGameTime() % 20L == 0L) {
            player2.sendSystemMessage((Component)Component.literal((String)"You cannot place blocks in dungeons!").withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        Entity entity = ((CommandSourceStack)event.getParseResults().getContext().getSource()).getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        if (!DungeonRules.isInDungeon(player)) {
            return;
        }
        if (isAdminDungeonBypassing(player)) {
            return;
        }
        String input = event.getParseResults().getReader().getString();
        String rootCommand = input.split("\\s+")[0].replace("/", "").toLowerCase();
        if (BLOCKED_COMMANDS.contains(rootCommand)) {
            event.setCanceled(true);
            player.sendSystemMessage((Component)Component.literal((String)"That command is disabled in dungeons!").withStyle(ChatFormatting.RED));
        }
    }

    private static boolean isInDungeon(ServerPlayer player) {
        if (!player.level().dimension().equals(MegaModDimensions.DUNGEON)) {
            return false;
        }
        ServerLevel overworld = player.level().getServer().overworld();
        return DungeonManager.get(overworld).isPlayerInDungeon(player.getUUID());
    }
}

