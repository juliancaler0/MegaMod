/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.ultra.megamod.feature.economy;

import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.economy.network.PlayerInfoSyncPayload;
import com.ultra.megamod.feature.economy.shop.MegaShop;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid="megamod")
public class EconomyEvents {
    // Ore reward batching: accumulate rewards per player, display every 60 ticks (3 seconds)
    private static final Map<UUID, Integer> pendingOreRewards = new HashMap<>();
    private static long lastBatchTick = 0;

    // Anti-farm: track recent kills per player for diminishing returns
    // Stores the tick of each kill in a rolling window
    private static final Map<UUID, java.util.Deque<Long>> recentKills = new HashMap<>();
    private static final int KILL_WINDOW_TICKS = 6000; // 5 minutes
    private static final int FULL_REWARD_KILLS = 30;
    private static final int HALF_REWARD_KILLS = 50;
    private static final int QUARTER_REWARD_KILLS = 75;
    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        EconomyManager eco = EconomyManager.get(player.level());
        int lost = eco.getWallet(player.getUUID());
        if (lost > 0) {
            eco.setWallet(player.getUUID(), 0);
            player.sendSystemMessage((Component)Component.literal((String)("You lost " + lost + " MegaCoins from your wallet!")).withStyle(ChatFormatting.RED));
        }
    }

    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;

        LivingEntity mob = event.getEntity();

        // In dungeon dimension: no coin rewards for regular mobs, only boss kills reward via quest system
        if (player.level().dimension().equals(MegaModDimensions.DUNGEON)) {
            return;
        }

        int reward = EconomyEvents.getMobReward(mob);
        if (reward <= 0) return;

        // Anti-farm diminishing returns
        reward = applyDiminishingReturns(player, reward);
        if (reward <= 0) return;

        // TODO: Reconnect with Pufferfish Skills API (was SkillsEconomyIntegration.applyMegacoinBonus)
        EconomyManager eco = EconomyManager.get(player.level());
        eco.addWallet(player.getUUID(), reward);
        player.sendSystemMessage((Component)Component.literal((String)("+" + reward + " MegaCoins")).withStyle(ChatFormatting.GOLD));
    }

    /**
     * Diminishing returns for mob kill rewards. Tracks kills in a rolling 5-minute window.
     * First 20 kills: full reward. 21-40: 50%. 41-60: 25%. 61+: 0.
     */
    private static int applyDiminishingReturns(ServerPlayer player, int reward) {
        long currentTick = player.level().getGameTime();
        UUID uuid = player.getUUID();
        java.util.Deque<Long> kills = recentKills.computeIfAbsent(uuid, k -> new java.util.ArrayDeque<>());

        // Prune old kills outside the window
        while (!kills.isEmpty() && currentTick - kills.peekFirst() > KILL_WINDOW_TICKS) {
            kills.pollFirst();
        }

        int killCount = kills.size();
        kills.addLast(currentTick);

        if (killCount < FULL_REWARD_KILLS) {
            return reward; // full
        } else if (killCount < HALF_REWARD_KILLS) {
            return Math.max(1, reward / 2); // 50%
        } else if (killCount < QUARTER_REWARD_KILLS) {
            return Math.max(1, reward / 4); // 25%
        } else {
            // Over limit — no reward, warn once
            if (killCount == QUARTER_REWARD_KILLS) {
                player.sendSystemMessage(Component.literal("Mob rewards paused — too many kills recently. Wait a few minutes.").withStyle(ChatFormatting.GRAY));
            }
            return 0;
        }
    }

    private static int getMobReward(LivingEntity mob) {
        String typeName;
        return switch (typeName = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath()) {
            // Common mobs: 3-5 MC (bumped from 2)
            case "zombie", "husk", "drowned" -> 4;
            case "skeleton", "stray" -> 4;
            case "spider", "cave_spider" -> 3;
            case "slime", "magma_cube" -> 3;
            case "silverfish" -> 3;
            case "phantom" -> 6;
            case "zombified_piglin" -> 5;
            // Mid-tier mobs: 8-15 MC (bumped from 5-8)
            case "creeper" -> 8;
            case "witch" -> 12;
            case "guardian" -> 10;
            case "piglin_brute" -> 12;
            case "vindicator" -> 10;
            case "pillager" -> 8;
            case "hoglin" -> 10;
            // Strong mobs: 25-40 MC (bumped from 18-30)
            case "enderman" -> 25;
            case "blaze" -> 25;
            case "wither_skeleton" -> 40;
            case "ghast" -> 30;
            case "shulker" -> 35;
            // Mini-bosses: 100-150 MC (bumped from 45-75)
            case "elder_guardian" -> 120;
            case "ravager" -> 100;
            case "evoker" -> 80;
            // Bosses: kept high
            case "warden" -> 500;
            case "wither" -> 1000;
            case "ender_dragon" -> 2000;
            case "cow", "pig", "sheep", "chicken", "rabbit", "horse", "donkey", "mule", "llama", "cat", "wolf", "fox", "bee", "turtle", "axolotl", "frog", "goat", "mooshroom", "parrot", "panda", "polar_bear", "squid", "glow_squid", "bat", "villager", "wandering_trader", "iron_golem", "snow_golem", "dolphin", "cod", "salmon", "tropical_fish", "pufferfish", "strider", "camel", "sniffer", "armadillo" -> 0;
            default -> 0;
        };
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
        Block block = event.getState().getBlock();
        int reward = EconomyEvents.getOreReward(block);
        if (reward > 0) {
            // TODO: Reconnect with Pufferfish Skills API (was SkillsEconomyIntegration.applyMegacoinBonus)
            EconomyManager eco = EconomyManager.get(player2.level());
            eco.addWallet(player2.getUUID(), reward);
            // Batch ore rewards — accumulate and display every 3 seconds
            pendingOreRewards.merge(player2.getUUID(), reward, Integer::sum);
        }
    }

    private static int getOreReward(Block block) {
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return 2;
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return 2;
        }
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return 4;
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return 6;
        }
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return 4;
        }
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return 3;
        }
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return 15;
        }
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return 12;
        }
        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return 3;
        }
        if (block == Blocks.NETHER_GOLD_ORE) {
            return 4;
        }
        if (block == Blocks.ANCIENT_DEBRIS) {
            return 40;
        }
        return 0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long time = overworld.getGameTime();

        // Flush batched ore rewards every 3 seconds
        if (time % 60L == 0L && !pendingOreRewards.isEmpty()) {
            var iterator = pendingOreRewards.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                ServerPlayer p = event.getServer().getPlayerList().getPlayer(entry.getKey());
                if (p != null) {
                    p.sendSystemMessage(Component.literal("+" + entry.getValue() + " MegaCoins (mining)").withStyle(ChatFormatting.GOLD));
                }
                iterator.remove();
            }
        }

        // Sync player info HUD every 10 seconds
        if (time % 200L == 0L) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                syncPlayerInfo(player);
            }
        }

        if (time % 1200L == 0L && event.getServer().isRunning()) {
            // Use async saving for periodic saves to avoid blocking the server tick
            final ServerLevel saveLevel = overworld;
            com.ultra.megamod.util.AsyncSaveHelper.saveAsync(() -> {
                EconomyManager.get(saveLevel).saveToDisk(saveLevel);
                MegaShop.get(saveLevel).saveToDisk(saveLevel);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ServerLevel overworld = sp.level().getServer().overworld();
            EconomyManager eco = EconomyManager.get(overworld);
            // First-join welcome: grant starting coins and send intro message.
            // Use isKnownPlayer to avoid re-granting 250 after a crash/data recovery
            // where wallet and bank are legitimately 0.
            if (!eco.isKnownPlayer(sp.getUUID())) {
                eco.addWallet(sp.getUUID(), 250);
                eco.saveToDisk(overworld);
                sp.sendSystemMessage(Component.literal("=== Welcome to MegaMod! ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                sp.sendSystemMessage(Component.literal("You received 250 MegaCoins to start!").withStyle(ChatFormatting.GREEN));
                sp.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.GRAY));
                sp.sendSystemMessage(Component.literal("Key Features:").withStyle(ChatFormatting.YELLOW));
                sp.sendSystemMessage(Component.literal("  V - Accessories  |  K - Skill Tree").withStyle(ChatFormatting.AQUA));
                sp.sendSystemMessage(Component.literal("  R - Primary Ability  |  G - Secondary Ability").withStyle(ChatFormatting.AQUA));
                sp.sendSystemMessage(Component.literal("  Craft a Computer for apps, shop, bank & more").withStyle(ChatFormatting.AQUA));
                sp.sendSystemMessage(Component.literal("  Find a Royal Herald for dungeon quests").withStyle(ChatFormatting.AQUA));
                sp.sendSystemMessage(Component.literal("  Earn coins by mining ores & killing mobs").withStyle(ChatFormatting.GRAY));
                sp.sendSystemMessage(Component.literal("  Check the MegaMod Guide book in your inventory!").withStyle(ChatFormatting.AQUA));
                // Give starter guide book
                net.minecraft.world.item.ItemStack guideBook = GuideBookHelper.createGuideBook();
                if (!sp.getInventory().add(guideBook)) {
                    sp.spawnAtLocation((ServerLevel) sp.level(), guideBook);
                }
            }
            syncPlayerInfo(sp);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Ensure stale singletons are cleared on server start.
        // On a crash, ServerStoppingEvent never fires, so reset() was never called.
        // Without this, integrated servers would reuse stale in-memory data.
        EconomyManager.reset();
        MegaShop.reset();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // Drain any pending async saves before synchronous shutdown saves
        com.ultra.megamod.util.AsyncSaveHelper.shutdown();

        ServerLevel overworld = event.getServer().overworld();
        // Synchronous saves on shutdown to ensure all data is written
        EconomyManager.get(overworld).saveToDisk(overworld);
        MegaShop.get(overworld).saveToDisk(overworld);
        EconomyManager.reset();
        MegaShop.reset();
        recentKills.clear();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Save dirty economy data when a level unloads to prevent data loss
            ServerLevel overworld = serverLevel.getServer().overworld();
            EconomyManager.get(overworld).saveToDisk(overworld);
            MegaShop.get(overworld).saveToDisk(overworld);
        }
    }

    public static void syncPlayerInfo(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        UUID uuid = player.getUUID();

        EconomyManager eco = EconomyManager.get(overworld);
        int wallet = eco.getWallet(uuid);
        int bank = eco.getBank(uuid);

        // TODO: Reconnect with Pufferfish Skills API (was SkillManager levels + SkillBadges)
        int totalLevel = 0;
        String badgeTitle = "";
        String badgeColorCode = "";
        int totalPrestige = 0;

        PacketDistributor.sendToPlayer(player, new PlayerInfoSyncPayload(
                wallet, bank, totalLevel, totalPrestige, badgeTitle, badgeColorCode));
    }
}

