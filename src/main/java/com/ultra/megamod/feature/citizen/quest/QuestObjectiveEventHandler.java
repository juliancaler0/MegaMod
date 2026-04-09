package com.ultra.megamod.feature.citizen.quest;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

/**
 * Listens for game events (block break, block place, entity kill) and forwards
 * them to the quest system to advance event-driven objectives.
 * <p>
 * Mirrors MineColonies' {@code QuestObjectiveEventHandler} but adapted
 * for MegaMod's player-centric (rather than colony-centric) quest tracking.
 * <p>
 * Objective listeners are registered when a player reaches an event-driven
 * objective and unregistered when the objective is fulfilled or cancelled.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class QuestObjectiveEventHandler {

    // ========================= Listener Maps =========================

    /**
     * Block break tracking: Block -> (PlayerUUID -> list of quest IDs)
     */
    private static final Map<Block, Map<UUID, List<String>>> breakBlockListeners = new HashMap<>();

    /**
     * Block place tracking: Block -> (PlayerUUID -> list of quest IDs)
     */
    private static final Map<Block, Map<UUID, List<String>>> placeBlockListeners = new HashMap<>();

    /**
     * Entity kill tracking: EntityType -> (PlayerUUID -> list of quest IDs)
     */
    private static final Map<EntityType<?>, Map<UUID, List<String>>> killEntityListeners = new HashMap<>();

    // ========================= Event Handlers =========================

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        Block block = event.getState().getBlock();
        Map<UUID, List<String>> playerMap = breakBlockListeners.get(block);
        if (playerMap == null) return;

        List<String> questIds = playerMap.get(player.getUUID());
        if (questIds == null || questIds.isEmpty()) return;

        ServerLevel level = (ServerLevel) player.level();
        ColonyQuestManager manager = ColonyQuestManager.get(level);

        for (String questId : new ArrayList<>(questIds)) {
            manager.onBlockBroken(player, questId, block);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Block block = event.getPlacedBlock().getBlock();
        Map<UUID, List<String>> playerMap = placeBlockListeners.get(block);
        if (playerMap == null) return;

        List<String> questIds = playerMap.get(player.getUUID());
        if (questIds == null || questIds.isEmpty()) return;

        ServerLevel level = (ServerLevel) player.level();
        ColonyQuestManager manager = ColonyQuestManager.get(level);

        for (String questId : new ArrayList<>(questIds)) {
            manager.onBlockPlaced(player, questId, block);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        EntityType<?> entityType = event.getEntity().getType();
        Map<UUID, List<String>> playerMap = killEntityListeners.get(entityType);
        if (playerMap == null) return;

        List<String> questIds = playerMap.get(player.getUUID());
        if (questIds == null || questIds.isEmpty()) return;

        ServerLevel level = (ServerLevel) player.level();
        ColonyQuestManager manager = ColonyQuestManager.get(level);

        for (String questId : new ArrayList<>(questIds)) {
            manager.onEntityKilled(player, questId, entityType);
        }
    }

    // ========================= Registration =========================

    /**
     * Registers a listener for block breaking.
     */
    public static void addBreakBlockListener(Block block, UUID playerUUID, String questId) {
        breakBlockListeners
                .computeIfAbsent(block, k -> new HashMap<>())
                .computeIfAbsent(playerUUID, k -> new ArrayList<>())
                .add(questId);
    }

    /**
     * Removes a listener for block breaking.
     */
    public static void removeBreakBlockListener(Block block, UUID playerUUID, String questId) {
        Map<UUID, List<String>> playerMap = breakBlockListeners.get(block);
        if (playerMap != null) {
            List<String> quests = playerMap.get(playerUUID);
            if (quests != null) {
                quests.remove(questId);
            }
        }
    }

    /**
     * Registers a listener for block placement.
     */
    public static void addPlaceBlockListener(Block block, UUID playerUUID, String questId) {
        placeBlockListeners
                .computeIfAbsent(block, k -> new HashMap<>())
                .computeIfAbsent(playerUUID, k -> new ArrayList<>())
                .add(questId);
    }

    /**
     * Removes a listener for block placement.
     */
    public static void removePlaceBlockListener(Block block, UUID playerUUID, String questId) {
        Map<UUID, List<String>> playerMap = placeBlockListeners.get(block);
        if (playerMap != null) {
            List<String> quests = playerMap.get(playerUUID);
            if (quests != null) {
                quests.remove(questId);
            }
        }
    }

    /**
     * Registers a listener for entity killing.
     */
    public static void addKillEntityListener(EntityType<?> entityType, UUID playerUUID, String questId) {
        killEntityListeners
                .computeIfAbsent(entityType, k -> new HashMap<>())
                .computeIfAbsent(playerUUID, k -> new ArrayList<>())
                .add(questId);
    }

    /**
     * Removes a listener for entity killing.
     */
    public static void removeKillEntityListener(EntityType<?> entityType, UUID playerUUID, String questId) {
        Map<UUID, List<String>> playerMap = killEntityListeners.get(entityType);
        if (playerMap != null) {
            List<String> quests = playerMap.get(playerUUID);
            if (quests != null) {
                quests.remove(questId);
            }
        }
    }

    /**
     * Helper to resolve a block from its registry ID string.
     */
    public static Block resolveBlock(String blockId) {
        Identifier id = Identifier.tryParse(blockId);
        if (id == null) return null;
        return BuiltInRegistries.BLOCK.getValue(id);
    }

    /**
     * Helper to resolve an entity type from its registry ID string.
     */
    public static EntityType<?> resolveEntityType(String entityTypeId) {
        Identifier id = Identifier.tryParse(entityTypeId);
        if (id == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getValue(id);
    }

    /**
     * Clears all listeners. Called on server stop.
     */
    public static void clearAll() {
        breakBlockListeners.clear();
        placeBlockListeners.clear();
        killEntityListeners.clear();
    }
}
