package com.ultra.megamod.feature.computer.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Computer app screen for the quest/progression system.
 * Tabbed interface with category tabs, scrollable quest list, and detail panel.
 */
public class QuestsScreen extends Screen {
    private final Screen parent;
    private boolean dataLoaded = false;
    private int refreshTimer = 0;

    // Parsed quest data
    private final List<CategoryData> categories = new ArrayList<>();
    private int totalCompleted = 0;
    private int totalQuests = 0;
    private int trackedCount = 0;

    // UI state
    private int selectedCategoryIndex = 0;
    private int selectedQuestIndex = 0;
    private float scrollOffset = 0;
    private int maxScroll = 0;

    // Layout constants
    private int panelX, panelY, panelW, panelH;
    private int tabBarY, tabBarH;
    private int listX, listY, listW, listH;
    private int detailX, detailY, detailW, detailH;

    // ─── Data records ───

    private record TaskData(String desc, int progress, int target, boolean checkmark) {}
    private record QuestData(String id, String title, List<String> desc, int sort,
                             boolean completed, boolean claimed, boolean prereqsMet, boolean seen,
                             boolean tracked, boolean partyShared,
                             String classRequired, boolean classMatch,
                             List<TaskData> tasks, List<String> rewards) {}
    private record CategoryData(String id, String name, String desc, int color, List<QuestData> quests) {}

    public QuestsScreen(Screen parent) {
        super(Component.literal("Quests"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        requestData();
    }

    private void requestData() {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("quests_request", ""),
            new CustomPacketPayload[0]);
    }

    private void sendAction(String action, String questId) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload(action, questId),
            new CustomPacketPayload[0]);
    }

    // ─── Tick / data polling ───

    @Override
    public void tick() {
        super.tick();
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null && "quests_data".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseData(resp.jsonData());
            dataLoaded = true;
        }
        if (resp != null && "quests_result".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            // Refresh after any action
            requestData();
        }
        if (resp != null && "error".equals(resp.dataType())) {
            ComputerDataPayload.lastResponse = null;
            dataLoaded = true;
        }
        if (++refreshTimer >= 200) { // Refresh every 10s
            refreshTimer = 0;
            requestData();
        }
    }

    private void parseData(String json) {
        try {
            categories.clear();
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            totalCompleted = root.get("totalCompleted").getAsInt();
            totalQuests = root.get("totalQuests").getAsInt();
            trackedCount = root.get("trackedCount").getAsInt();

            JsonArray cats = root.getAsJsonArray("categories");
            for (JsonElement catEl : cats) {
                JsonObject catObj = catEl.getAsJsonObject();
                List<QuestData> quests = new ArrayList<>();
                JsonArray questArr = catObj.getAsJsonArray("quests");
                for (JsonElement qEl : questArr) {
                    JsonObject qObj = qEl.getAsJsonObject();
                    List<TaskData> tasks = new ArrayList<>();
                    JsonArray taskArr = qObj.getAsJsonArray("tasks");
                    for (JsonElement tEl : taskArr) {
                        JsonObject tObj = tEl.getAsJsonObject();
                        tasks.add(new TaskData(
                            tObj.get("desc").getAsString(),
                            tObj.get("progress").getAsInt(),
                            tObj.get("target").getAsInt(),
                            tObj.get("checkmark").getAsBoolean()
                        ));
                    }
                    List<String> rewards = new ArrayList<>();
                    JsonArray rewArr = qObj.getAsJsonArray("rewards");
                    for (JsonElement rEl : rewArr) rewards.add(rEl.getAsString());

                    List<String> descLines = new ArrayList<>();
                    JsonArray descArr = qObj.getAsJsonArray("desc");
                    for (JsonElement dEl : descArr) descLines.add(dEl.getAsString());

                    String classRequired = qObj.has("classRequired") ? qObj.get("classRequired").getAsString() : null;
                    boolean classMatch = !qObj.has("classMatch") || qObj.get("classMatch").getAsBoolean();

                    quests.add(new QuestData(
                        qObj.get("id").getAsString(),
                        qObj.get("title").getAsString(),
                        descLines,
                        qObj.get("sort").getAsInt(),
                        qObj.get("completed").getAsBoolean(),
                        qObj.get("claimed").getAsBoolean(),
                        qObj.get("prereqsMet").getAsBoolean(),
                        qObj.get("seen").getAsBoolean(),
                        qObj.get("tracked").getAsBoolean(),
                        qObj.get("partyShared").getAsBoolean(),
                        classRequired, classMatch,
                        tasks, rewards
                    ));
                }
                categories.add(new CategoryData(
                    catObj.get("id").getAsString(),
                    catObj.get("name").getAsString(),
                    catObj.get("desc").getAsString(),
                    catObj.get("color").getAsInt(),
                    quests
                ));
            }

            // Clamp selection
            if (selectedCategoryIndex >= categories.size()) selectedCategoryIndex = 0;
            List<QuestData> currentQuests = getCurrentQuests();
            if (selectedQuestIndex >= currentQuests.size()) selectedQuestIndex = Math.max(0, currentQuests.size() - 1);
            scrollOffset = 0;
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    private List<QuestData> getCurrentQuests() {
        if (selectedCategoryIndex < categories.size()) {
            return categories.get(selectedCategoryIndex).quests();
        }
        return List.of();
    }

    // ─── Rendering ───

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        UIHelper.drawScreenBg(g, 0, 0, width, height);

        // Calculate layout
        panelW = Math.min(440, width - 20);
        panelH = height - 20;
        panelX = (width - panelW) / 2;
        panelY = 10;

        UIHelper.drawPanel(g, panelX, panelY, panelW, panelH);

        // Title bar
        int titleH = 20;
        String titleText = "Quests";
        g.drawString(font, titleText, panelX + 10, panelY + 6, 0xFFFFFFFF, false);
        String progress = totalCompleted + "/" + totalQuests + " Complete";
        g.drawString(font, progress, panelX + panelW - 10 - font.width(progress), panelY + 6, 0xFF888888, false);

        if (!dataLoaded) {
            g.drawCenteredString(font, "Loading...", width / 2, height / 2, 0xFFAAAAAA);
            renderBackButton(g, mx, my);
            super.render(g, mx, my, pt);
            return;
        }

        if (categories.isEmpty()) {
            g.drawCenteredString(font, "No quests available", width / 2, height / 2, 0xFFAAAAAA);
            renderBackButton(g, mx, my);
            super.render(g, mx, my, pt);
            return;
        }

        // Tab bar
        tabBarY = panelY + titleH + 2;
        tabBarH = 18;
        renderTabs(g, mx, my);

        // Content area: quest list (left 42%) | detail (right 58%)
        int contentY = tabBarY + tabBarH + 4;
        int contentH = panelH - (contentY - panelY) - 30; // leave room for back button
        listX = panelX + 6;
        listW = (int)(panelW * 0.42) - 8;
        listY = contentY;
        listH = contentH;

        detailX = listX + listW + 4;
        detailW = panelW - listW - 16;
        detailY = contentY;
        detailH = contentH;

        renderQuestList(g, mx, my);
        renderQuestDetail(g, mx, my);
        renderBackButton(g, mx, my);

        super.render(g, mx, my, pt);
    }

    // ─── Tab bar ───

    private void renderTabs(GuiGraphics g, int mx, int my) {
        int tabX = panelX + 4;
        int tabW = Math.max(30, (panelW - 8) / categories.size());

        for (int i = 0; i < categories.size(); i++) {
            CategoryData cat = categories.get(i);
            boolean selected = i == selectedCategoryIndex;
            int tx = tabX + i * tabW;

            UIHelper.drawTab(g, tx, tabBarY, tabW - 1, tabBarH, selected, cat.color());

            // Abbreviate name if needed
            String label = cat.name();
            int maxLabelW = tabW - 6;
            if (font.width(label) > maxLabelW) {
                // Use first word only
                int spaceIdx = label.indexOf(' ');
                label = spaceIdx > 0 ? label.substring(0, spaceIdx) : label.substring(0, Math.min(6, label.length()));
            }
            int labelColor = selected ? 0xFFFFFFFF : 0xFFAAAAAA;
            g.drawCenteredString(font, label, tx + (tabW - 1) / 2, tabBarY + (tabBarH - 9) / 2, labelColor);

            // Count completed in this category
            long catCompleted = cat.quests().stream().filter(QuestData::claimed).count();
            if (catCompleted == cat.quests().size() && !cat.quests().isEmpty()) {
                // Small gold check mark
                g.drawString(font, "\u2714", tx + tabW - 10, tabBarY + 2, 0xFFFFD700, false);
            }
        }
    }

    // ─── Quest list (left panel) ───

    private void renderQuestList(GuiGraphics g, int mx, int my) {
        UIHelper.drawInsetPanel(g, listX, listY, listW, listH);

        List<QuestData> quests = getCurrentQuests();
        if (quests.isEmpty()) {
            g.drawCenteredString(font, "No quests", listX + listW / 2, listY + listH / 2, 0xFF666666);
            return;
        }

        // Calculate total content height and max scroll
        int rowH = 22;
        int totalContentH = quests.size() * rowH;
        maxScroll = Math.max(0, totalContentH - listH + 4);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // Enable scissoring for scrollable area
        g.enableScissor(listX + 1, listY + 1, listX + listW - 1, listY + listH - 1);

        int baseY = listY + 2 - (int) scrollOffset;
        for (int i = 0; i < quests.size(); i++) {
            QuestData q = quests.get(i);
            int rowY = baseY + i * rowH;

            // Skip if off-screen
            if (rowY + rowH < listY || rowY > listY + listH) continue;

            boolean hovered = mx >= listX && mx < listX + listW && my >= rowY && my < rowY + rowH
                && my >= listY && my < listY + listH;
            boolean selected = i == selectedQuestIndex;

            // Background
            if (selected) {
                g.fill(listX + 2, rowY, listX + listW - 2, rowY + rowH - 1, 0xFF2A2A44);
            } else if (hovered) {
                g.fill(listX + 2, rowY, listX + listW - 2, rowY + rowH - 1, 0xFF1E1E36);
            }

            // Class-locked quests: dim if classRequired is set but classMatch is false
            boolean classLocked = q.classRequired() != null && !q.classMatch();

            // Status icon
            String icon;
            int iconColor;
            if (classLocked && !q.completed()) {
                icon = "\u2022"; iconColor = 0xFF555555; // gray dot (class locked)
            } else if (!q.prereqsMet()) {
                icon = "\u2022"; iconColor = 0xFF555555; // gray dot (locked)
            } else if (q.claimed()) {
                icon = "\u2605"; iconColor = 0xFFFFD700; // gold star
            } else if (q.completed()) {
                icon = "\u2714"; iconColor = 0xFF44FF44; // green check
            } else {
                // Check if in progress (at least one task has progress)
                boolean hasProgress = q.tasks().stream().anyMatch(t -> t.progress() > 0);
                if (hasProgress) {
                    icon = "\u25B6"; iconColor = 0xFF4488CC; // blue arrow
                } else {
                    icon = "\u25CB"; iconColor = 0xFFCCCCCC; // white circle
                }
            }
            g.drawString(font, icon, listX + 5, rowY + 3, iconColor, false);

            // Quest title
            String title = q.title();
            int maxTitleW = listW - 28;
            // Reduce title width if class badge will be drawn
            if (q.classRequired() != null) {
                maxTitleW -= font.width("[" + q.classRequired().substring(0, 1).toUpperCase() + "]") + 4;
            }
            int titleColor;
            if (classLocked && !q.completed()) {
                titleColor = 0xFF555555; // dimmed for wrong class
            } else if (!q.prereqsMet()) {
                titleColor = 0xFF555555;
            } else if (q.claimed()) {
                titleColor = 0xFF99AA88;
            } else if (q.completed()) {
                titleColor = 0xFF88CC88;
            } else {
                titleColor = 0xFFDDDDDD;
            }
            if (font.width(title) > maxTitleW) {
                while (font.width(title + "..") > maxTitleW && title.length() > 3) {
                    title = title.substring(0, title.length() - 1);
                }
                title += "..";
            }
            g.drawString(font, title, listX + 16, rowY + 3, titleColor, false);

            // Class badge next to title
            if (q.classRequired() != null) {
                String badge = "[" + q.classRequired().toUpperCase() + "]";
                int badgeX = listX + 16 + font.width(title) + 3;
                int badgeColor = q.classMatch() ? 0xFFFFD700 : 0xFF666644; // gold if matches, dim otherwise
                // Only draw if it fits
                if (badgeX + font.width(badge) < listX + listW - 14) {
                    g.drawString(font, badge, badgeX, rowY + 3, badgeColor, false);
                }
            }

            // Status label below title
            String status;
            int statusColor;
            if (classLocked && !q.completed()) {
                status = "WRONG CLASS"; statusColor = 0xFF553333;
            } else if (!q.prereqsMet()) {
                status = "LOCKED"; statusColor = 0xFF444444;
            } else if (q.claimed()) {
                status = "CLAIMED"; statusColor = 0xFF888844;
            } else if (q.completed()) {
                status = "COMPLETE"; statusColor = 0xFF44AA44;
            } else {
                boolean hasProgress = q.tasks().stream().anyMatch(t -> t.progress() > 0);
                status = hasProgress ? "IN PROGRESS" : "AVAILABLE";
                statusColor = hasProgress ? 0xFF4466AA : 0xFF666688;
            }
            g.drawString(font, status, listX + 16, rowY + 13, statusColor, false);

            // NEW indicator
            if (q.prereqsMet() && !q.completed() && !q.seen()) {
                g.drawString(font, "NEW", listX + listW - 26, rowY + 3, 0xFFFFAA00, false);
            }

            // Tracked indicator
            if (q.tracked()) {
                g.drawString(font, "\u25C9", listX + listW - 12, rowY + 3, 0xFF44CCFF, false);
            }
        }

        g.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            float scrollProgress = scrollOffset / maxScroll;
            UIHelper.drawScrollbar(g, listX + listW - 5, listY + 2, listH - 4, scrollProgress);
        }
    }

    // ─── Quest detail (right panel) ───

    private void renderQuestDetail(GuiGraphics g, int mx, int my) {
        UIHelper.drawInsetPanel(g, detailX, detailY, detailW, detailH);

        List<QuestData> quests = getCurrentQuests();
        if (selectedQuestIndex >= quests.size()) {
            g.drawCenteredString(font, "Select a quest", detailX + detailW / 2, detailY + detailH / 2, 0xFF666666);
            return;
        }

        QuestData q = quests.get(selectedQuestIndex);
        int cx = detailX + 8;
        int cy = detailY + 8;
        int cw = detailW - 16;

        boolean classLocked = q.classRequired() != null && !q.classMatch();

        // Title
        g.drawString(font, q.title(), cx, cy, classLocked ? 0xFF888888 : 0xFFFFFFFF, false);
        cy += 14;

        // Class requirement badge
        if (q.classRequired() != null) {
            String classBadge = q.classMatch()
                ? "\u2605 " + q.classRequired() + " Quest"
                : "\u26D4 Requires " + q.classRequired() + " Class";
            int badgeColor = q.classMatch() ? 0xFFFFD700 : 0xFFCC4444;
            g.drawString(font, classBadge, cx, cy, badgeColor, false);
            cy += 12;
        }

        // Party shared indicator
        if (q.partyShared()) {
            g.drawString(font, "\u2764 Party Shared", cx, cy, 0xFF9B59B6, false);
            cy += 12;
        }

        // Description (always shown so players can read what they're missing)
        for (String line : q.desc()) {
            g.drawString(font, line, cx, cy, classLocked ? 0xFF666666 : 0xFF999999, false);
            cy += 10;
        }
        cy += 6;

        // Divider
        UIHelper.drawHorizontalDivider(g, cx, cy, cw);
        cy += 6;

        // Tasks
        g.drawString(font, "Tasks", cx, cy, 0xFFBBBBBB, false);
        cy += 12;

        for (TaskData task : q.tasks()) {
            // Task description
            g.drawString(font, task.desc(), cx + 4, cy, 0xFFDDDDDD, false);
            cy += 11;

            // Progress bar
            if (!task.checkmark()) {
                int barW = Math.min(cw - 8, 160);
                float pct = task.target() > 0 ? (float) task.progress() / task.target() : 0;
                pct = Math.min(1.0f, pct);
                int fillColor = pct >= 1.0f ? 0xFF44CC44 : 0xFF4488CC;
                UIHelper.drawProgressBar(g, cx + 4, cy, barW, 8, pct, fillColor);

                // Progress text
                String progText = task.progress() + "/" + task.target();
                g.drawString(font, progText, cx + barW + 10, cy, 0xFFAAAAAA, false);
                cy += 12;
            } else {
                // Checkmark task: show done/not done
                String checkStatus = q.completed() ? "\u2714 Done" : "\u25CB Incomplete";
                int checkColor = q.completed() ? 0xFF44CC44 : 0xFF888888;
                g.drawString(font, checkStatus, cx + 4, cy, checkColor, false);
                cy += 12;
            }
        }
        cy += 4;

        // Divider
        UIHelper.drawHorizontalDivider(g, cx, cy, cw);
        cy += 6;

        // Rewards
        g.drawString(font, "Rewards", cx, cy, 0xFFBBBBBB, false);
        cy += 12;
        for (String reward : q.rewards()) {
            g.drawString(font, "+ " + reward, cx + 4, cy, 0xFFFFD700, false);
            cy += 10;
        }
        cy += 8;

        // Action buttons
        if (classLocked && !q.completed()) {
            // Class-locked: show requirement text instead of action buttons
            g.drawString(font, "Requires " + q.classRequired(), cx, cy, 0xFFCC4444, false);
            cy += 12;
            g.drawString(font, "Pick this class to unlock", cx, cy, 0xFF666666, false);
        } else if (!q.prereqsMet()) {
            // Show what's needed
            g.drawString(font, "Complete prerequisites to unlock", cx, cy, 0xFF666666, false);
        } else if (q.completed() && !q.claimed()) {
            // Claim button
            renderActionButton(g, mx, my, cx, cy, 80, "Claim", 0xFF44CC44, "btn_claim");
        } else if (!q.completed()) {
            // Track/Untrack button
            if (q.tracked()) {
                renderActionButton(g, mx, my, cx, cy, 80, "Untrack", 0xFF888888, "btn_untrack");
            } else {
                boolean canTrack = trackedCount < 3;
                int trackColor = canTrack ? 0xFF4488CC : 0xFF444444;
                renderActionButton(g, mx, my, cx, cy, 80, "Track", trackColor, "btn_track");
            }

            // Checkmark button for CHECKMARK-only quests
            boolean allCheckmark = q.tasks().stream().allMatch(TaskData::checkmark);
            if (allCheckmark && !q.completed()) {
                renderActionButton(g, mx, my, cx + 90, cy, 80, "Complete", 0xFF44AA44, "btn_checkmark");
            }
        } else if (q.claimed()) {
            g.drawString(font, "\u2605 Rewards claimed!", cx, cy, 0xFF888844, false);
        }
    }

    private void renderActionButton(GuiGraphics g, int mx, int my, int x, int y, int w, String label, int color, String id) {
        int h = 16;
        boolean hovered = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hovered ? UIHelper.brightenColor(color, 30) : color;
        g.fill(x, y, x + w, y + h, (bg & 0x00FFFFFF) | 0xDD000000);
        g.drawCenteredString(font, label, x + w / 2, y + (h - 9) / 2, hovered ? 0xFFFFFFFF : 0xFFDDDDDD);
    }

    // ─── Back button ───

    private void renderBackButton(GuiGraphics g, int mx, int my) {
        int bbW = 60, bbH = 18;
        int bbX = panelX + 6;
        int bbY = panelY + panelH - 24;
        boolean bbHov = mx >= bbX && mx < bbX + bbW && my >= bbY && my < bbY + bbH;
        UIHelper.drawButton(g, bbX, bbY, bbW, bbH, bbHov);
        g.drawCenteredString(font, "Back", bbX + bbW / 2, bbY + (bbH - 9) / 2, bbHov ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    // ─── Input ───

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        if (event.button() != 0) return super.mouseClicked(event, consumed);

        int mx = (int) event.x();
        int my = (int) event.y();

        // Back button
        int bbW = 60, bbH = 18, bbX = panelX + 6, bbY = panelY + panelH - 24;
        if (mx >= bbX && mx < bbX + bbW && my >= bbY && my < bbY + bbH) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }

        // Tab clicks
        if (my >= tabBarY && my < tabBarY + tabBarH && !categories.isEmpty()) {
            int tabW = Math.max(30, (panelW - 8) / categories.size());
            int tabX = panelX + 4;
            for (int i = 0; i < categories.size(); i++) {
                int tx = tabX + i * tabW;
                if (mx >= tx && mx < tx + tabW) {
                    if (selectedCategoryIndex != i) {
                        selectedCategoryIndex = i;
                        selectedQuestIndex = 0;
                        scrollOffset = 0;
                    }
                    return true;
                }
            }
        }

        // Quest list clicks
        if (mx >= listX && mx < listX + listW && my >= listY && my < listY + listH) {
            List<QuestData> quests = getCurrentQuests();
            int rowH = 22;
            int baseY = listY + 2 - (int) scrollOffset;
            for (int i = 0; i < quests.size(); i++) {
                int rowY = baseY + i * rowH;
                if (my >= rowY && my < rowY + rowH && my >= listY && my < listY + listH) {
                    selectedQuestIndex = i;
                    // Mark as seen
                    QuestData q = quests.get(i);
                    if (!q.seen() && q.prereqsMet()) {
                        sendAction("quest_mark_seen", q.id());
                    }
                    return true;
                }
            }
        }

        // Detail panel action buttons
        List<QuestData> quests = getCurrentQuests();
        if (selectedQuestIndex < quests.size()) {
            QuestData q = quests.get(selectedQuestIndex);
            // Calculate button Y position (approximate — matches render logic)
            int buttonY = estimateButtonY(q);
            int cx = detailX + 8;

            boolean classLocked = q.classRequired() != null && !q.classMatch();

            if (classLocked && !q.completed()) {
                // Class-locked quests have no actionable buttons — do nothing
            } else if (q.completed() && !q.claimed()) {
                // Claim button
                if (mx >= cx && mx < cx + 80 && my >= buttonY && my < buttonY + 16) {
                    sendAction("quest_claim", q.id());
                    return true;
                }
            } else if (!q.completed() && q.prereqsMet()) {
                // Track/Untrack
                if (mx >= cx && mx < cx + 80 && my >= buttonY && my < buttonY + 16) {
                    if (q.tracked()) {
                        sendAction("quest_untrack", q.id());
                    } else if (trackedCount < 3) {
                        sendAction("quest_track", q.id());
                    }
                    return true;
                }
                // Checkmark button
                boolean allCheckmark = q.tasks().stream().allMatch(TaskData::checkmark);
                if (allCheckmark && mx >= cx + 90 && mx < cx + 170 && my >= buttonY && my < buttonY + 16) {
                    sendAction("quest_checkmark", q.id());
                    return true;
                }
            }
        }

        return super.mouseClicked(event, consumed);
    }

    private int estimateButtonY(QuestData q) {
        int cy = detailY + 8;
        cy += 14; // title
        if (q.classRequired() != null) cy += 12; // class badge
        if (q.partyShared()) cy += 12;
        cy += q.desc().size() * 10 + 6; // description
        cy += 6; // divider
        cy += 12; // "Tasks" header
        for (TaskData task : q.tasks()) {
            cy += 11; // task desc
            cy += 12; // progress bar or checkmark
        }
        cy += 4 + 6; // gap + divider
        cy += 12; // "Rewards" header
        cy += q.rewards().size() * 10;
        cy += 8;
        return cy;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (mx >= listX && mx < listX + listW && my >= listY && my < listY + listH) {
            scrollOffset -= (float) scrollY * 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { // ESC
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
