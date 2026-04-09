package com.ultra.megamod.feature.furniture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Quest Board screen — shows available dungeon quests with difficulties and rewards.
 * Players accept quests to receive dungeon keys.
 */
public class QuestBoardScreen extends Screen {

    private int panelX, panelY, panelW, panelH;
    private int scrollOffset = 0;
    private int maxVisible = 6;

    private List<QuestEntry> quests = List.of();
    private boolean dataLoaded = false;

    public record QuestEntry(String id, String title, String difficulty, String dungeonName,
                             int coinReward, String keyItemId, int diffLevel) {}

    public QuestBoardScreen() {
        super(Component.literal("Quest Board"));
    }

    @Override
    public void tick() {
        super.tick();
        QuestBoardDataPayload resp = QuestBoardDataPayload.lastResponse;
        if (resp != null && "quest_board_quests".equals(resp.dataType())) {
            QuestBoardDataPayload.lastResponse = null;
            parseQuestData(resp.jsonData());
            this.dataLoaded = true;
        }
    }

    private void parseQuestData(String json) {
        try {
            ArrayList<QuestEntry> parsed = new ArrayList<>();
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (var el : arr) {
                JsonObject obj = el.getAsJsonObject();
                parsed.add(new QuestEntry(
                    obj.get("id").getAsString(),
                    obj.get("title").getAsString(),
                    obj.get("difficulty").getAsString(),
                    obj.get("dungeonName").getAsString(),
                    obj.get("coinReward").getAsInt(),
                    obj.get("keyItemId").getAsString(),
                    obj.get("diffLevel").getAsInt()
                ));
            }
            this.quests = parsed;
        } catch (Exception e) {
            com.ultra.megamod.MegaMod.LOGGER.error("Failed to parse quest board data", e);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.panelW = 280;
        this.panelH = 240;
        this.panelX = (this.width - panelW) / 2;
        this.panelY = (this.height - panelH) / 2;

        // Request quest data from server
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new QuestBoardActionPayload("quest_board_get", "{}"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);

        // Title
        g.drawCenteredString(this.font, "\u00A76\u00A7l\u2605 Village Quest Board \u2605",
            panelX + panelW / 2, panelY + 6, 0xFFFFD700);
        g.drawCenteredString(this.font, "\u00A77Accept a quest to receive a dungeon key",
            panelX + panelW / 2, panelY + 18, 0xFF888899);

        // Quest list
        int listY = panelY + 34;
        int entryH = 32;

        if (quests.isEmpty()) {
            String msg = dataLoaded ? "\u00A77No quests available. Check back later!" : "\u00A78Loading quests...";
            g.drawCenteredString(this.font, msg, panelX + panelW / 2, listY + 40, 0xFF666666);
            return;
        }

        for (int i = scrollOffset; i < Math.min(quests.size(), scrollOffset + maxVisible); i++) {
            QuestEntry q = quests.get(i);
            int ey = listY + (i - scrollOffset) * entryH;
            int ex = panelX + 8;
            int ew = panelW - 16;

            boolean hovered = mouseX >= ex && mouseX < ex + ew && mouseY >= ey && mouseY < ey + entryH - 2;
            g.fill(ex, ey, ex + ew, ey + entryH - 2, hovered ? 0xFF2A2A3A : 0xFF1E1E2E);

            // Difficulty color bar
            int diffColor = switch (q.diffLevel) {
                case 0 -> 0xFF55FF55;
                case 1 -> 0xFFFFAA00;
                case 2 -> 0xFFFF5555;
                case 3 -> 0xFFAA00FF;
                default -> 0xFFFFFFFF;
            };
            g.fill(ex, ey, ex + 3, ey + entryH - 2, diffColor);

            g.drawString(this.font, q.title, ex + 8, ey + 3, 0xFFFFFFFF);
            g.drawString(this.font, "\u00A77" + q.difficulty + " \u00A78| " + q.dungeonName,
                ex + 8, ey + 14, 0xFF888888);

            String reward = "\u00A7a+" + q.coinReward + " MC \u00A78+ \u00A7bDungeon Key";
            int rewardW = this.font.width(reward);
            g.drawString(this.font, reward, ex + ew - rewardW - 4, ey + 14, 0xFFAAAAAA);

            // Accept button
            int btnX = ex + ew - 44;
            int btnY = ey + 2;
            int btnW = 40;
            int btnH = 12;
            boolean btnHov = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHov, false);
            g.drawCenteredString(this.font, "Accept", btnX + btnW / 2, btnY + 2,
                btnHov ? 0xFF55FF55 : 0xFFAAFFAA);
        }

        // Scroll indicators
        if (scrollOffset > 0) {
            g.drawCenteredString(this.font, "\u25B2", panelX + panelW / 2, panelY + 28, 0xFF888888);
        }
        if (scrollOffset + maxVisible < quests.size()) {
            g.drawCenteredString(this.font, "\u25BC", panelX + panelW / 2, panelY + panelH - 10, 0xFF888888);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        double mouseX = event.x();
        double mouseY = event.y();

        int listY = panelY + 34;
        int entryH = 32;

        for (int i = scrollOffset; i < Math.min(quests.size(), scrollOffset + maxVisible); i++) {
            QuestEntry q = quests.get(i);
            int ey = listY + (i - scrollOffset) * entryH;
            int ex = panelX + 8;
            int ew = panelW - 16;

            int btnX = ex + ew - 44;
            int btnY = ey + 2;
            int btnW = 40;
            int btnH = 12;
            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
                JsonObject json = new JsonObject();
                json.addProperty("questId", q.id);
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new QuestBoardActionPayload("quest_board_accept", json.toString()));
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (scrollY < 0 && scrollOffset + maxVisible < quests.size()) {
            scrollOffset++;
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
