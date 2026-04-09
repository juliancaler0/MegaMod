package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.quests.QuestDefinitions;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestCategory;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestDef;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestTask;
import com.ultra.megamod.feature.quests.QuestEventListener;
import com.ultra.megamod.feature.quests.QuestProgressManager;
import com.ultra.megamod.feature.quests.QuestRewardGranter;
import com.ultra.megamod.feature.quests.QuestTaskEvaluator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles all quest-related computer app actions.
 */
public class QuestsHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        return switch (action) {
            case "quests_request" -> { handleQuestsRequest(player, level, eco); yield true; }
            case "quest_track" -> { handleTrack(player, jsonData, level, eco); yield true; }
            case "quest_untrack" -> { handleUntrack(player, jsonData, level, eco); yield true; }
            case "quest_claim" -> { handleClaim(player, jsonData, level, eco); yield true; }
            case "quest_checkmark" -> { handleCheckmark(player, jsonData, level, eco); yield true; }
            case "quest_mark_seen" -> { handleMarkSeen(player, jsonData, level); yield true; }
            default -> false;
        };
    }

    // ─── Main data request ───

    private static void handleQuestsRequest(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        QuestProgressManager qpm = QuestProgressManager.get(level);

        // Auto-complete gs_01 (Welcome quest) on first request
        QuestDef welcomeQuest = QuestDefinitions.get("gs_01");
        if (welcomeQuest != null && !qpm.isCompleted(uuid, "gs_01")) {
            qpm.completeQuest(uuid, "gs_01");
        }

        // Also check for any auto-completable quests based on current state
        QuestEventListener.checkQuestCompletion(player, level);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"categories\":[");

        boolean firstCat = true;
        int totalCompleted = 0;
        int totalQuests = 0;

        boolean isAdmin = AdminSystem.isAdmin(player);
        PlayerClass playerClass = PlayerClassManager.get(level).getPlayerClass(uuid);

        for (QuestCategory cat : QuestCategory.values()) {
            List<QuestDef> quests = QuestDefinitions.BY_CATEGORY.getOrDefault(cat, List.of());
            if (quests.isEmpty()) continue;

            if (!firstCat) sb.append(",");
            firstCat = false;

            sb.append("{\"id\":\"").append(cat.name()).append("\"");
            sb.append(",\"name\":\"").append(escapeJson(cat.displayName)).append("\"");
            sb.append(",\"desc\":\"").append(escapeJson(cat.description)).append("\"");
            sb.append(",\"color\":").append(cat.color);
            sb.append(",\"quests\":[");

            boolean firstQuest = true;
            for (QuestDef def : quests) {
                // Hide quests for other classes — admins and classless (NONE) players see all
                PlayerClass requiredClass = QuestDefinitions.getClassRequirement(def.id());
                if (requiredClass != null && !isAdmin && playerClass != PlayerClass.NONE
                        && playerClass != requiredClass) {
                    continue;
                }

                if (!firstQuest) sb.append(",");
                firstQuest = false;
                totalQuests++;

                boolean completed = qpm.isCompleted(uuid, def.id());
                boolean claimed = qpm.isRewardClaimed(uuid, def.id());
                boolean prereqsMet = qpm.arePrerequisitesMet(uuid, def);
                boolean seen = qpm.isSeen(uuid, def.id());
                boolean tracked = qpm.getTrackedQuests(uuid).contains(def.id());

                if (completed) totalCompleted++;

                sb.append("{\"id\":\"").append(def.id()).append("\"");
                sb.append(",\"title\":\"").append(escapeJson(def.title())).append("\"");
                sb.append(",\"desc\":[");
                for (int d = 0; d < def.description().length; d++) {
                    if (d > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(def.description()[d])).append("\"");
                }
                sb.append("]");
                sb.append(",\"sort\":").append(def.sortOrder());
                sb.append(",\"completed\":").append(completed);
                sb.append(",\"claimed\":").append(claimed);
                sb.append(",\"prereqsMet\":").append(prereqsMet);
                sb.append(",\"seen\":").append(seen);
                sb.append(",\"tracked\":").append(tracked);
                sb.append(",\"partyShared\":").append(def.partyShared());

                // Class requirement
                if (requiredClass != null) {
                    sb.append(",\"classRequired\":\"").append(escapeJson(requiredClass.getDisplayName())).append("\"");
                    sb.append(",\"classMatch\":").append(isAdmin || playerClass == requiredClass);
                }

                // Tasks with progress
                sb.append(",\"tasks\":[");
                for (int t = 0; t < def.tasks().length; t++) {
                    if (t > 0) sb.append(",");
                    QuestTask task = def.tasks()[t];
                    int progress;
                    if (task.type() == QuestDefinitions.QuestTaskType.CHECKMARK) {
                        progress = completed ? 1 : 0;
                    } else {
                        progress = prereqsMet && !completed
                            ? QuestTaskEvaluator.evaluate(player, task, level)
                            : (completed ? task.targetAmount() : 0);
                    }
                    sb.append("{\"desc\":\"").append(escapeJson(task.description())).append("\"");
                    sb.append(",\"progress\":").append(Math.min(progress, task.targetAmount()));
                    sb.append(",\"target\":").append(task.targetAmount());
                    sb.append(",\"checkmark\":").append(task.type() == QuestDefinitions.QuestTaskType.CHECKMARK);
                    sb.append("}");
                }
                sb.append("]");

                // Rewards
                sb.append(",\"rewards\":[");
                for (int r = 0; r < def.rewards().length; r++) {
                    if (r > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(def.rewards()[r].description())).append("\"");
                }
                sb.append("]");

                sb.append("}");
            }
            sb.append("]}");
        }

        sb.append("],\"totalCompleted\":").append(totalCompleted);
        sb.append(",\"totalQuests\":").append(totalQuests);
        sb.append(",\"trackedCount\":").append(qpm.getTrackedQuests(uuid).size());
        sb.append("}");

        int wallet = eco.getWallet(uuid);
        int bank = eco.getBank(uuid);
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("quests_data", sb.toString(), wallet, bank));
    }

    // ─── Track/Untrack ───

    private static void handleTrack(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        String questId = jsonData.trim().replace("\"", "");
        QuestProgressManager qpm = QuestProgressManager.get(level);

        boolean success = qpm.trackQuest(uuid, questId);

        String response = "{\"success\":" + success + ",\"action\":\"track\",\"questId\":\"" + escapeJson(questId) + "\"}";
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("quests_result", response, eco.getWallet(uuid), eco.getBank(uuid)));
    }

    private static void handleUntrack(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        String questId = jsonData.trim().replace("\"", "");
        QuestProgressManager.get(level).untrackQuest(uuid, questId);

        String response = "{\"success\":true,\"action\":\"untrack\",\"questId\":\"" + escapeJson(questId) + "\"}";
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("quests_result", response, eco.getWallet(uuid), eco.getBank(uuid)));
    }

    // ─── Claim rewards ───

    private static void handleClaim(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        String questId = jsonData.trim().replace("\"", "");
        QuestProgressManager qpm = QuestProgressManager.get(level);

        QuestDef def = QuestDefinitions.get(questId);
        boolean success = false;
        if (def != null && qpm.isCompleted(uuid, questId) && !qpm.isRewardClaimed(uuid, questId)) {
            QuestRewardGranter.grantRewards(player, def, level, eco);
            qpm.claimRewards(uuid, questId);
            success = true;
        }

        String response = "{\"success\":" + success + ",\"action\":\"claim\",\"questId\":\"" + escapeJson(questId) + "\"}";
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("quests_result", response, eco.getWallet(uuid), eco.getBank(uuid)));
    }

    // ─── Checkmark (manual task completion) ───

    private static void handleCheckmark(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        String questId = jsonData.trim().replace("\"", "");
        QuestProgressManager qpm = QuestProgressManager.get(level);

        QuestDef def = QuestDefinitions.get(questId);
        boolean success = false;
        String failReason = "";
        if (def != null && !qpm.isCompleted(uuid, questId) && qpm.arePrerequisitesMet(uuid, def)) {
            // Check class requirement (admins bypass)
            PlayerClass required = QuestDefinitions.getClassRequirement(questId);
            if (required != null && !AdminSystem.isAdmin(player)) {
                PlayerClass playerClass = PlayerClassManager.get(level).getPlayerClass(uuid);
                if (playerClass != required) {
                    failReason = "Requires " + required.getDisplayName() + " class";
                }
            }

            if (failReason.isEmpty()) {
                // Only complete if all tasks are checkmarks (single-action quests)
                boolean allCheckmark = true;
                for (QuestTask task : def.tasks()) {
                    if (task.type() != QuestDefinitions.QuestTaskType.CHECKMARK) {
                        allCheckmark = false;
                        break;
                    }
                }
                if (allCheckmark) {
                    qpm.completeQuest(uuid, questId);
                    success = true;
                }
            }
        }

        StringBuilder response = new StringBuilder();
        response.append("{\"success\":").append(success);
        response.append(",\"action\":\"checkmark\"");
        response.append(",\"questId\":\"").append(escapeJson(questId)).append("\"");
        if (!failReason.isEmpty()) {
            response.append(",\"failReason\":\"").append(escapeJson(failReason)).append("\"");
        }
        response.append("}");
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("quests_result", response.toString(), eco.getWallet(uuid), eco.getBank(uuid)));
    }

    // ─── Mark as seen ───

    private static void handleMarkSeen(ServerPlayer player, String jsonData, ServerLevel level) {
        UUID uuid = player.getUUID();
        String questId = jsonData.trim().replace("\"", "");
        QuestProgressManager.get(level).markSeen(uuid, questId);
    }

    // ─── Helpers ───

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
