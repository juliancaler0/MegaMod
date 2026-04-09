package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler;
import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.furniture.QuestTracker;
import com.ultra.megamod.feature.hud.network.TrackerSyncPayload;
import com.ultra.megamod.feature.quests.QuestDefinitions;
import com.ultra.megamod.feature.quests.QuestProgressManager;
import com.ultra.megamod.feature.quests.QuestTaskEvaluator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side: syncs active bounties and quests to clients every 60 ticks.
 */
@EventBusSubscriber(modid = "megamod")
public class QuestTrackerSyncHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 60 != 0) return;

        ServerLevel overworld = event.getServer().overworld();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            if (!SettingsHandler.isEnabled(uuid, "hud_quest_tracker")) continue;

            // Bounties
            List<TrackerSyncPayload.BountyInfo> bountyInfos = new ArrayList<>();
            try {
                var allBounties = BountyBoardHandler.getAllBounties();
                for (var bounty : allBounties) {
                    if (bounty.posterUuid.equals(uuid) && !bounty.fulfilled) {
                        bountyInfos.add(new TrackerSyncPayload.BountyInfo(bounty.itemName, bounty.quantity));
                    }
                }
            } catch (Exception ignored) {
                // BountyBoardHandler may not be loaded yet
            }

            // Quests
            List<TrackerSyncPayload.QuestInfo> questInfos = new ArrayList<>();
            try {
                var tracker = QuestTracker.get(overworld);
                var activeQuests = tracker.getActiveQuests(uuid);
                for (var quest : activeQuests) {
                    questInfos.add(new TrackerSyncPayload.QuestInfo(quest.title(), quest.difficultyLevel()));
                }
            } catch (Exception ignored) {
                // QuestTracker may not be loaded yet
            }

            // Tracked quests (from quest app)
            List<TrackerSyncPayload.ProgressQuestInfo> trackedQuests = new ArrayList<>();
            try {
                QuestProgressManager qpm = QuestProgressManager.get(overworld);
                Set<String> tracked = qpm.getTrackedQuests(uuid);
                for (String questId : tracked) {
                    QuestDefinitions.QuestDef def = QuestDefinitions.get(questId);
                    if (def == null || qpm.isCompleted(uuid, questId)) continue;
                    // Find first incomplete task for HUD display
                    for (QuestDefinitions.QuestTask task : def.tasks()) {
                        int current = QuestTaskEvaluator.evaluate(player, task, overworld);
                        if (current < task.targetAmount()) {
                            trackedQuests.add(new TrackerSyncPayload.ProgressQuestInfo(
                                def.title(), task.description(),
                                Math.min(current, task.targetAmount()), task.targetAmount()));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}

            if (!bountyInfos.isEmpty() || !questInfos.isEmpty() || !trackedQuests.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new TrackerSyncPayload(bountyInfos, questInfos, trackedQuests));
            }
        }
    }
}
