/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.LevelResource
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$EntityPlaceEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.ultra.megamod.feature.multiplayer;

import com.ultra.megamod.MegaMod;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="megamod")
public class PlayerStatistics {
    private static PlayerStatistics INSTANCE;
    private static final String FILE_NAME = "megamod_stats.dat";
    public static final String KILLS = "kills";
    public static final String DEATHS = "deaths";
    public static final String MOB_KILLS = "mobKills";
    public static final String BLOCKS_BROKEN = "blocksBroken";
    public static final String BLOCKS_PLACED = "blocksPlaced";
    public static final String PLAY_TIME_TICKS = "playTimeTicks";
    public static final String DAMAGE_DEALT = "damageDealt";
    public static final String DAMAGE_TAKEN = "damageTaken";
    private static final String[] ALL_STATS;
    private final Map<UUID, Map<String, Integer>> playerStats = new HashMap<UUID, Map<String, Integer>>();
    private boolean dirty = false;

    public static PlayerStatistics get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new PlayerStatistics();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static PlayerStatistics getIfLoaded() {
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pData = players.getCompoundOrEmpty(key);
                    HashMap<String, Integer> stats = new HashMap<String, Integer>();
                    for (String stat : ALL_STATS) {
                        stats.put(stat, pData.getIntOr(stat, 0));
                    }
                    this.playerStats.put(uuid, stats);
                }
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load player statistics data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, Map<String, Integer>> entry : this.playerStats.entrySet()) {
                CompoundTag pData = new CompoundTag();
                for (Map.Entry<String, Integer> stat : entry.getValue().entrySet()) {
                    pData.putInt(stat.getKey(), stat.getValue().intValue());
                }
                players.put(entry.getKey().toString(), (Tag)pData);
            }
            root.put("players", (Tag)players);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save player statistics data", e);
        }
    }

    private void markDirty() {
        this.dirty = true;
    }

    private Map<String, Integer> getOrCreate(UUID playerId) {
        return this.playerStats.computeIfAbsent(playerId, k -> {
            HashMap<String, Integer> stats = new HashMap<String, Integer>();
            for (String stat : ALL_STATS) {
                stats.put(stat, 0);
            }
            return stats;
        });
    }

    public int getStat(UUID playerId, String stat) {
        return this.getOrCreate(playerId).getOrDefault(stat, 0);
    }

    public void incrementStat(UUID playerId, String stat) {
        Map<String, Integer> stats = this.getOrCreate(playerId);
        stats.put(stat, stats.getOrDefault(stat, 0) + 1);
        this.markDirty();
    }

    public void incrementStat(UUID playerId, String stat, int amount) {
        Map<String, Integer> stats = this.getOrCreate(playerId);
        stats.put(stat, stats.getOrDefault(stat, 0) + amount);
        this.markDirty();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        PlayerStatistics stats = PlayerStatistics.get(player2.level());
        stats.getOrCreate(player2.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        PlayerStatistics stats = PlayerStatistics.getIfLoaded();
        if (stats != null) {
            stats.saveToDisk(player2.level());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)event.getEntity().level();
        PlayerStatistics stats = PlayerStatistics.get(serverLevel);
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer deadPlayer = (ServerPlayer)livingEntity;
            stats.incrementStat(deadPlayer.getUUID(), DEATHS);
        }
        Entity killerEntity = event.getSource().getEntity();
        if (killerEntity instanceof ServerPlayer) {
            ServerPlayer killer = (ServerPlayer)killerEntity;
            if (event.getEntity() instanceof Player) {
                stats.incrementStat(killer.getUUID(), KILLS);
            } else {
                stats.incrementStat(killer.getUUID(), MOB_KILLS);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Level level = (Level)event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        PlayerStatistics.get(player2.level()).incrementStat(player2.getUUID(), BLOCKS_BROKEN);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        Level level = (Level)event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        PlayerStatistics.get(player.level()).incrementStat(player.getUUID(), BLOCKS_PLACED);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) event.getEntity().level();
        PlayerStatistics stats = PlayerStatistics.get(serverLevel);
        int damage = (int) event.getNewDamage();
        if (damage <= 0) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer damagedPlayer) {
            stats.incrementStat(damagedPlayer.getUUID(), DAMAGE_TAKEN, damage);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            stats.incrementStat(attacker.getUUID(), DAMAGE_DEALT, damage);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long gameTime = overworld.getGameTime();
        PlayerStatistics stats = PlayerStatistics.get(overworld);
        if (gameTime % 20L == 0L) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                stats.incrementStat(player.getUUID(), PLAY_TIME_TICKS, 20);
            }
        }
        if (gameTime % 6000L == 0L) {
            stats.saveToDisk(overworld);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PlayerStatistics stats = PlayerStatistics.getIfLoaded();
        if (stats != null) {
            stats.saveToDisk(event.getServer().overworld());
        }
        PlayerStatistics.reset();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)Commands.literal((String)"megamod").then(((LiteralArgumentBuilder)Commands.literal((String)"stats").executes(ctx -> {
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            PlayerStatistics.showStats(player, player);
            return 1;
        })).then(Commands.argument((String)"player", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            ServerPlayer sender = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            String targetName = StringArgumentType.getString((CommandContext)ctx, (String)"player");
            ServerPlayer target = ((CommandSourceStack)ctx.getSource()).getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                sender.sendSystemMessage((Component)Component.literal((String)("Player not found: " + targetName)).withStyle(ChatFormatting.RED));
                return 0;
            }
            PlayerStatistics.showStats(sender, target);
            return 1;
        }))));
    }

    private static void showStats(ServerPlayer viewer, ServerPlayer target) {
        PlayerStatistics stats = PlayerStatistics.getIfLoaded();
        if (stats == null) {
            return;
        }
        UUID targetId = target.getUUID();
        String targetName = target.getGameProfile().name();
        int playTimeTicks = stats.getStat(targetId, PLAY_TIME_TICKS);
        int playTimeMinutes = playTimeTicks / 1200;
        int playTimeHours = playTimeMinutes / 60;
        int remainingMinutes = playTimeMinutes % 60;
        viewer.sendSystemMessage((Component)Component.literal((String)""));
        viewer.sendSystemMessage((Component)Component.literal((String)("--- Stats for " + targetName + " ---")).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}));
        viewer.sendSystemMessage(PlayerStatistics.statLine("PvP Kills", stats.getStat(targetId, KILLS)));
        viewer.sendSystemMessage(PlayerStatistics.statLine("Deaths", stats.getStat(targetId, DEATHS)));
        viewer.sendSystemMessage(PlayerStatistics.statLine("Mob Kills", stats.getStat(targetId, MOB_KILLS)));
        viewer.sendSystemMessage(PlayerStatistics.statLine("Blocks Broken", stats.getStat(targetId, BLOCKS_BROKEN)));
        viewer.sendSystemMessage(PlayerStatistics.statLine("Blocks Placed", stats.getStat(targetId, BLOCKS_PLACED)));
        viewer.sendSystemMessage(PlayerStatistics.statLine("Damage Dealt", stats.getStat(targetId, DAMAGE_DEALT)));
        viewer.sendSystemMessage(PlayerStatistics.statLine("Damage Taken", stats.getStat(targetId, DAMAGE_TAKEN)));
        viewer.sendSystemMessage((Component)Component.literal((String)"  Play Time: ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)(playTimeHours + "h " + remainingMinutes + "m")).withStyle(ChatFormatting.WHITE)));
        viewer.sendSystemMessage((Component)Component.literal((String)""));
    }

    private static Component statLine(String label, int value) {
        return Component.literal((String)("  " + label + ": ")).withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)String.valueOf(value)).withStyle(ChatFormatting.WHITE));
    }

    static {
        ALL_STATS = new String[]{KILLS, DEATHS, MOB_KILLS, BLOCKS_BROKEN, BLOCKS_PLACED, PLAY_TIME_TICKS, DAMAGE_DEALT, DAMAGE_TAKEN};
    }
}

