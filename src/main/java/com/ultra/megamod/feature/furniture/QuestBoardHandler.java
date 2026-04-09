package com.ultra.megamod.feature.furniture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side handler for Quest Board actions.
 * Manages quest listing and acceptance, gives dungeon keys, tracks quests for payout.
 */
public class QuestBoardHandler {

    public static void handleAction(ServerPlayer player, String action, String jsonData) {
        ServerLevel level = (ServerLevel) player.level();
        switch (action) {
            case "quest_board_get" -> handleGetQuests(player, level);
            case "quest_board_accept" -> handleAcceptQuest(player, jsonData, level);
        }
    }

    private static void handleGetQuests(ServerPlayer player, ServerLevel level) {
        long tick = level.getServer().getTickCount();
        List<QuestData> quests = QuestBoardManager.getQuests(tick);

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < quests.size(); i++) {
            var q = quests.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"").append(q.id()).append("\"");
            sb.append(",\"title\":\"").append(q.title()).append("\"");
            sb.append(",\"difficulty\":\"").append(q.difficulty()).append("\"");
            sb.append(",\"dungeonName\":\"").append(q.dungeonName()).append("\"");
            sb.append(",\"coinReward\":").append(q.coinReward());
            sb.append(",\"keyItemId\":\"").append(q.keyItemId()).append("\"");
            sb.append(",\"diffLevel\":").append(q.difficultyLevel());
            sb.append("}");
        }
        sb.append("]");
        PacketDistributor.sendToPlayer(player,
            (CustomPacketPayload) new QuestBoardDataPayload("quest_board_quests", sb.toString()));
    }

    private static void handleAcceptQuest(ServerPlayer player, String jsonData, ServerLevel level) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String questId = json.get("questId").getAsString();

        long tick = level.getServer().getTickCount();
        List<QuestData> quests = new ArrayList<>(QuestBoardManager.getQuests(tick));

        QuestData quest = null;
        for (var q : quests) {
            if (q.id().equals(questId)) {
                quest = q;
                break;
            }
        }

        if (quest == null) {
            player.sendSystemMessage(Component.literal("\u00A7cQuest no longer available."));
            return;
        }

        // Give dungeon key item
        net.minecraft.world.item.Item keyItem = switch (quest.keyItemId()) {
            case "dungeon_key_normal" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NORMAL.get();
            case "dungeon_key_hard" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_HARD.get();
            case "dungeon_key_nightmare" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NIGHTMARE.get();
            case "dungeon_key_infernal" -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_INFERNAL.get();
            default -> com.ultra.megamod.feature.dungeons.DungeonRegistry.DUNGEON_KEY_NORMAL.get();
        };

        ItemStack keyStack = new ItemStack(keyItem);
        if (!player.getInventory().add(keyStack)) {
            player.spawnAtLocation(level, keyStack);
        }

        // Track quest — coin reward paid on dungeon completion
        QuestTracker tracker = QuestTracker.get(level);
        tracker.acceptQuest(player.getUUID(), quest);
        tracker.saveToDisk(level);

        // Remove from board
        QuestBoardManager.removeQuest(questId);

        player.sendSystemMessage(Component.literal(
            "\u00A7a\u00A7l\u2605 \u00A7eQuest accepted! \u00A77Received \u00A7b" + quest.difficulty()
            + " Dungeon Key\u00A77. Complete the dungeon to earn \u00A7a" + quest.coinReward() + " MC\u00A77!"));

        // Send updated quest list
        handleGetQuests(player, level);
    }
}
