package com.ultra.megamod.feature.arena;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.UUID;

@EventBusSubscriber(modid = MegaMod.MODID)
public class ArenaEvents {

    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // Check every second
    private static final int MATCHMAKING_INTERVAL = 60; // Try matchmaking every 3 seconds
    private static final int SAVE_INTERVAL = 1200; // Auto-save every minute

    /**
     * Server tick: check active arenas, advance waves when mobs cleared,
     * handle PvP round timers, and run matchmaking.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter % CHECK_INTERVAL != 0) return;

        ServerLevel overworld = event.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        ServerLevel pocketLevel = event.getServer().getLevel(MegaModDimensions.DUNGEON);
        if (pocketLevel == null) return;

        // Check PvE arenas for wave completion
        for (ArenaManager.ArenaInstance instance : new ArrayList<>(arenaManager.getActiveInstances().values())) {
            if (instance.state != ArenaManager.ArenaState.ACTIVE) continue;
            if (instance.atCheckpoint) continue; // Don't check during checkpoint pause

            if (instance.mode == ArenaManager.ArenaMode.PVE) {
                if (instance.mobsAlive <= 0) {
                    arenaManager.completeWave(instance.instanceId, pocketLevel);
                } else {
                    // Fallback: scan for actual alive mobs in the arena area
                    // This catches mobs that despawned, fell into void, or died without firing events
                    net.minecraft.core.BlockPos o = instance.origin;
                    net.minecraft.world.phys.AABB arenaBox = new net.minecraft.world.phys.AABB(
                            o.getX(), o.getY() - 5, o.getZ(),
                            o.getX() + 53, o.getY() + 20, o.getZ() + 53);
                    long actualMobs = pocketLevel.getEntitiesOfClass(
                            net.minecraft.world.entity.Mob.class, arenaBox,
                            net.minecraft.world.entity.Mob::isAlive).size();
                    if (actualMobs == 0 && instance.mobsAlive > 0) {
                        // Counter is stale — force it to 0
                        instance.mobsAlive = 0;
                        arenaManager.completeWave(instance.instanceId, pocketLevel);
                    }
                }
            } else if (instance.mode == ArenaManager.ArenaMode.BOSS_RUSH) {
                BossRushManager.checkBossRushProgress(pocketLevel, instance, arenaManager);
            }
        }

        // PvP round timers
        if (tickCounter % CHECK_INTERVAL == 0) {
            ArenaPvpManager.checkRoundTimers(overworld);
        }

        // Matchmaking
        if (tickCounter % MATCHMAKING_INTERVAL == 0) {
            ArenaPvpManager.tryMatchmaking(overworld);
        }

        // Auto-save
        if (tickCounter % SAVE_INTERVAL == 0) {
            arenaManager.saveToDisk(overworld);
            BossRushLeaderboard.get(overworld).saveToDisk(overworld);
        }
    }

    /**
     * When a mob dies in the arena dimension, decrement the mob count for the arena instance.
     * When a player dies in a PvP arena, handle the round result.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        // Check if this is in the dungeon/pocket dimension
        if (!entity.level().dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = entity.level().getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);

        if (entity instanceof Mob) {
            // A mob died — find the arena instance and decrement mob count
            ArenaManager.ArenaInstance instance = arenaManager.findInstanceAtPos(entity.blockPosition());
            if (instance != null) {
                arenaManager.decrementMobCount(instance.instanceId);
            }
        }

        if (entity instanceof ServerPlayer deadPlayer) {
            // Player died in arena — check if it's a PvP match or PvE
            ArenaManager.ArenaInstance instance = arenaManager.getInstanceForPlayer(deadPlayer.getUUID());
            if (instance == null) return;

            if (instance.mode == ArenaManager.ArenaMode.PVP) {
                // Determine killer
                if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                    ArenaPvpManager.handlePvpKill(killer.getUUID(), deadPlayer.getUUID(), overworld);
                    // Cancel the death event to prevent actual death
                    event.setCanceled(true);
                    deadPlayer.setHealth(deadPlayer.getMaxHealth());
                }
            } else if (instance.mode == ArenaManager.ArenaMode.PVE || instance.mode == ArenaManager.ArenaMode.BOSS_RUSH) {
                // Player died in PvE/Boss Rush — end the run
                ServerLevel pocketLevel = (ServerLevel) entity.level();
                arenaManager.endArena(instance.instanceId, false, pocketLevel);
                // Cancel death to prevent item loss
                event.setCanceled(true);
                deadPlayer.setHealth(deadPlayer.getMaxHealth());
            }
        }
    }

    /**
     * Prevent explosion block damage in arenas.
     * Creepers, TNT, etc. still deal entity damage but don't break blocks.
     */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = level.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);

        // Clear affected blocks if explosion is within any arena
        if (!event.getAffectedBlocks().isEmpty()) {
            BlockPos first = event.getAffectedBlocks().get(0);
            ArenaManager.ArenaInstance instance = arenaManager.findInstanceAtPos(first);
            if (instance != null) {
                event.getAffectedBlocks().clear();
            }
        }
    }

    /**
     * Prevent mob griefing (enderman picking blocks, etc.) in arenas.
     */
    @SubscribeEvent
    public static void onMobGriefing(net.neoforged.neoforge.event.entity.EntityMobGriefingEvent event) {
        net.minecraft.world.entity.Entity entity = event.getEntity();
        if (entity == null) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = level.getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        ArenaManager.ArenaInstance instance = arenaManager.findInstanceAtPos(entity.blockPosition());
        if (instance != null) {
            event.setCanGrief(false);
        }
    }

    /**
     * Prevent players from breaking blocks in arenas (PvE and PvP).
     * HIGHEST priority so this runs before tree felling, skill synergies, etc.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = player.level().getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        if (arenaManager.isInArena(player.getUUID())) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.literal("You can't break blocks in the arena!").withStyle(ChatFormatting.RED), true);
        }
    }

    /**
     * Cancel left-click block interactions in arena to prevent instant-break blocks
     * (torches, flowers, etc.) from being destroyed before BreakEvent fires.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = player.level().getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        if (arenaManager.isInArena(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    /**
     * Prevent players from placing blocks in arenas (PvE and PvP).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = player.level().getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        if (arenaManager.isInArena(player.getUUID())) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.literal("You can't place blocks in the arena!").withStyle(ChatFormatting.RED), true);
        }
    }

    /**
     * No Damage challenge: any damage taken = run over.
     */
    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(MegaModDimensions.DUNGEON)) return;

        ServerLevel overworld = player.level().getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        ArenaManager.ArenaInstance instance = arenaManager.getInstanceForPlayer(player.getUUID());
        if (instance == null || instance.mode != ArenaManager.ArenaMode.PVE) return;

        if (instance.challengeMode == ArenaManager.ChallengeMode.NO_DAMAGE && !instance.tookDamage) {
            instance.tookDamage = true;
            ServerLevel pocketLevel = (ServerLevel) player.level();
            player.sendSystemMessage(Component.literal("YOU TOOK DAMAGE! Challenge failed!")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            arenaManager.endArena(instance.instanceId, false, pocketLevel);
        }
    }

    /**
     * No Armor challenge: prevent equipping armor during the run.
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(MegaModDimensions.DUNGEON)) return;

        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;

        ServerLevel overworld = player.level().getServer().overworld();
        ArenaManager arenaManager = ArenaManager.get(overworld);
        ArenaManager.ArenaInstance instance = arenaManager.getInstanceForPlayer(player.getUUID());
        if (instance == null || instance.challengeMode != ArenaManager.ChallengeMode.NO_ARMOR) return;

        ItemStack newItem = event.getTo();
        if (!newItem.isEmpty()) {
            // Force remove the armor piece next tick
            player.level().getServer().execute(() -> {
                player.setItemSlot(slot, ItemStack.EMPTY);
                if (!player.getInventory().add(newItem.copy())) {
                    player.spawnAtLocation((ServerLevel) player.level(), newItem.copy());
                }
                player.sendSystemMessage(Component.literal("No armor allowed in this challenge!")
                        .withStyle(ChatFormatting.RED));
            });
        }
    }

    /**
     * Clean up arena on player disconnect.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel overworld = player.level().getServer().overworld();

        ArenaManager arenaManager = ArenaManager.get(overworld);
        arenaManager.removePlayerOnDisconnect(player.getUUID(), overworld);

        // Remove from PvP queue
        ArenaPvpManager.dequeueFromMatch(player.getUUID());
    }
}
