package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

/**
 * Admin panel GUI tab for controlling Baritone bots.
 * Tab 19 in the AdminTerminalScreen.
 *
 * Features:
 * - Player bot cards with status/stats/controls
 * - Quick command buttons (Mine Diamonds, Farm, Combat, Quarry, etc.)
 * - Inline settings panel with boolean toggles and numeric value editing
 * - Path visualization toggle with node count display
 * - Enhanced log viewer (20 entries, color-coded, auto-refresh)
 * - Full autocomplete with all commands including quarry, combat, deposit, withdraw, chest
 */
public class BotControlPanel {
    private final Font font;

    // Style constants (matching other panels)
    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int SUCCESS = 0xFF3FB950;
    private static final int WARNING = 0xFFD29922;
    private static final int ERROR = 0xFFF85149;
    private static final int BTN_BG = 0xFF21262D;
    private static final int BTN_HOVER = 0xFF30363D;
    private static final int INPUT_BG = 0xFF0D1117;
    private static final int PROGRESS_BG = 0xFF1C1C22;
    private static final int PROGRESS_FILL = 0xFF238636;
    private static final int DIVIDER = 0xFF21262D;

    // Additional colors for log entries
    private static final int LOG_SUCCESS = 0xFF7EE787;
    private static final int LOG_NAV = 0xFFD2A8FF;
    private static final int LOG_FAIL = 0xFFF88070;
    private static final int LOG_DEFAULT = 0xFF6E7681;

    // Layout
    private static final int BTN_H = 14;
    private static final int CARD_H_ACTIVE = 82;
    private static final int CARD_H_INACTIVE = 50;

    // State
    private int scroll = 0;
    private int maxScroll = 0;
    private String selectedPlayer = null;
    private String commandBuffer = "";
    private boolean commandFocused = false;
    private int cursorBlink = 0;
    private boolean showLog = false;
    private boolean showSettings = false;
    private final Set<String> pathToggledPlayers = new HashSet<>();

    // Status
    private String statusMessage = "";
    private int statusColor = TEXT;
    private long statusExpiry = 0;

    // Data from server
    private List<PlayerBotInfo> players = new ArrayList<>();
    private List<String> botLog = new ArrayList<>();
    private int activeBots = 0;
    private int totalBots = 0;

    // Settings panel data (parsed from JSON)
    private Map<String, String> settingsMap = new LinkedHashMap<>();
    private int settingsScroll = 0;

    // Path data per player
    private final Map<String, Integer> pathNodeCounts = new HashMap<>();

    // Auto-refresh
    private int refreshCooldown = 0;
    private int logRefreshCooldown = 0;
    private int pathRefreshCooldown = 0;

    // Command history
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    // Autocomplete — includes new commands: quarry, combat, deposit, withdraw, chest
    private static final String[] COMMANDS = {
        "goto", "mine", "follow", "farm", "explore", "build", "patrol",
        "gotoblock", "tunnel", "waypoint", "scan", "inventory", "come",
        "surface", "home", "eta",
        "quarry", "combat", "deposit", "withdraw", "chest",
        "stop", "cancel", "pause", "resume", "status", "settings"
    };
    private static final String[] COMMON_BLOCKS = {
        "diamond_ore", "deepslate_diamond_ore", "iron_ore", "deepslate_iron_ore",
        "gold_ore", "deepslate_gold_ore", "coal_ore", "copper_ore",
        "lapis_ore", "redstone_ore", "emerald_ore", "ancient_debris",
        "stone", "cobblestone", "oak_log", "dirt", "sand", "gravel"
    };
    private static final String[] SETTINGS_KEYS = com.ultra.megamod.feature.baritone.BotSettings.getAllKeys();

    // Settings metadata: which keys are boolean vs numeric
    private static final Set<String> BOOLEAN_SETTINGS = Set.of(
        "allowBreak", "allowPlace", "allowSprint", "allowParkour", "allowSwim", "allowClimb",
        "allowWaterBucket", "avoidDanger", "avoidMobs", "avoidWater", "avoidLava",
        "preferSilkTouch", "buildInLayers", "buildBottomUp", "skipMatchingBlocks",
        "autoReplant", "useBoneMeal", "followSprint", "loopPatrol", "spiralExpansion"
    );

    // Settings categories for organization
    private static final String[][] SETTINGS_CATEGORIES = {
        {"Movement", "allowBreak", "allowPlace", "allowSprint", "allowParkour", "allowSwim", "allowClimb", "allowWaterBucket", "movementSpeed"},
        {"Pathfinding", "primaryTimeoutMS", "failureTimeoutMS", "maxNodes", "snapshotRadius", "pathSegmentLength"},
        {"Safety", "avoidDanger", "avoidMobs", "mobAvoidRadius", "maxFallHeight", "avoidWater", "avoidLava"},
        {"Mining", "mineRadius", "mineScanInterval", "preferSilkTouch"},
        {"Building", "buildInLayers", "buildBottomUp", "skipMatchingBlocks", "maxBuildRadius"},
        {"Farming", "farmRadius", "autoReplant", "useBoneMeal", "farmScanInterval"},
        {"Following", "followDistance", "followSprint"},
        {"Patrol", "waypointPauseTicks", "loopPatrol"},
        {"Exploration", "exploreChunkRadius", "spiralExpansion"}
    };

    private List<String> suggestions = new ArrayList<>();
    private int selectedSuggestion = -1;

    // Quick command definitions: label, command, color
    private static final String[][] QUICK_CMDS = {
        {"Mine Diamonds", "mine diamond_ore", null},
        {"Mine Iron", "mine iron_ore", null},
        {"Farm", "farm", null},
        {"Come Here", "come", null},
        {"Combat", "combat", null},
        {"Quarry", "quarry", null},
        {"Deposit", "deposit", null},
        {"Explore", "explore", null},
        {"Surface", "surface", null},
    };
    private static final int[] QUICK_COLORS = {
        0xFF79C0FF, // Mine Diamonds - blue
        0xFFD2A8FF, // Mine Iron - purple
        0xFF7EE787, // Farm - green
        ACCENT,     // Come Here - accent blue
        ERROR,      // Combat - red
        WARNING,    // Quarry - yellow
        SUCCESS,    // Deposit - green
        0xFFD2A8FF, // Explore - purple
        LABEL,      // Surface - gray
    };

    // Tunnel direction buttons
    private static final String[][] TUNNEL_DIRS = {
        {"Tunnel N", "tunnel north 50"},
        {"Tunnel S", "tunnel south 50"},
        {"Tunnel E", "tunnel east 50"},
        {"Tunnel W", "tunnel west 50"},
    };

    // Data records
    public record PlayerBotInfo(String name, String uuid, int x, int y, int z,
                                 boolean botActive, String botStatus, boolean botPaused,
                                 int blocksMined, int cropsHarvested, int blocksPlaced,
                                 String settings, int kills, String processName,
                                 int goalX, int goalY, int goalZ, int eta) {}

    public BotControlPanel(Font font) {
        this.font = font;
    }

    public void requestData() {
        sendAction("bot_request_status", "");
    }

    // ================================
    // RENDER
    // ================================

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int w = right - left;

        // === Header bar ===
        g.fill(left, top, right, top + 28, HEADER_BG);
        g.drawString(font, "Bot Control", left + 6, top + 3, ACCENT, false);

        // Active/total indicator with colored dot
        String countStr = activeBots + "/" + totalBots;
        int dotColor = activeBots > 0 ? SUCCESS : LABEL;
        g.fill(left + 6, top + 16, left + 10, top + 20, dotColor);
        g.drawString(font, countStr + " bots", left + 13, top + 15, LABEL, false);

        // Header buttons: Refresh, Stop All, Pause All, Resume All
        int hbX = right - 6;

        // Refresh
        int refreshW = font.width("Refresh") + 8;
        hbX -= refreshW;
        boolean hoverRefresh = mouseX >= hbX && mouseX < hbX + refreshW && mouseY >= top + 3 && mouseY < top + 3 + 14;
        g.fill(hbX, top + 3, hbX + refreshW, top + 17, hoverRefresh ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Refresh", hbX + 4, top + 6, ACCENT, false);
        hbX -= 4;

        // Stop All
        int stopAllW = font.width("Stop All") + 8;
        hbX -= stopAllW;
        boolean hoverStopAll = mouseX >= hbX && mouseX < hbX + stopAllW && mouseY >= top + 3 && mouseY < top + 3 + 14;
        g.fill(hbX, top + 3, hbX + stopAllW, top + 17, hoverStopAll ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Stop All", hbX + 4, top + 6, ERROR, false);
        hbX -= 4;

        // Pause All
        int pauseAllW = font.width("Pause All") + 8;
        hbX -= pauseAllW;
        boolean hoverPauseAll = mouseX >= hbX && mouseX < hbX + pauseAllW && mouseY >= top + 3 && mouseY < top + 3 + 14;
        g.fill(hbX, top + 3, hbX + pauseAllW, top + 17, hoverPauseAll ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Pause All", hbX + 4, top + 6, WARNING, false);
        hbX -= 4;

        // Resume All
        int resumeAllW = font.width("Resume All") + 8;
        hbX -= resumeAllW;
        boolean hoverResumeAll = mouseX >= hbX && mouseX < hbX + resumeAllW && mouseY >= top + 3 && mouseY < top + 3 + 14;
        g.fill(hbX, top + 3, hbX + resumeAllW, top + 17, hoverResumeAll ? BTN_HOVER : BTN_BG);
        g.drawString(font, "Resume All", hbX + 4, top + 6, SUCCESS, false);

        // === Content area (scrollable) ===
        int contentTop = top + 28;
        int contentBottom = bottom - 26;
        // Enable scissor for scrollable content
        g.enableScissor(left, contentTop, right, contentBottom);

        int y = contentTop + 4 - scroll;

        // Status message
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            g.fill(left + 4, y - 1, right - 4, y + 10, 0x40000000);
            g.drawString(font, statusMessage, left + 8, y, statusColor, false);
            y += 14;
        }

        // Player list header
        g.drawString(font, "Players", left + 6, y, TEXT, false);
        g.fill(left + 6 + font.width("Players") + 4, y + 4, right - 6, y + 5, DIVIDER);
        y += 14;

        // Player cards
        int totalContentH = 14; // start with header
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) totalContentH += 14;

        for (int i = 0; i < players.size(); i++) {
            PlayerBotInfo p = players.get(i);
            boolean selected = p.name.equals(selectedPlayer);
            int cardH = calculateCardHeight(p, selected);

            boolean hoverCard = mouseX >= left + 4 && mouseX < right - 4 && mouseY >= y && mouseY < y + cardH
                && mouseY >= contentTop && mouseY < contentBottom;

            // Card background
            int cardBg = selected ? 0xFF1C2333 : (hoverCard ? 0xFF151A23 : HEADER_BG);
            g.fill(left + 4, y, right - 4, y + cardH, cardBg);
            // Left accent bar
            int barColor = p.botActive ? (p.botPaused ? WARNING : SUCCESS) : BORDER;
            if (selected) barColor = ACCENT;
            g.fill(left + 4, y, left + 7, y + cardH, barColor);
            // Bottom border
            g.fill(left + 4, y + cardH - 1, right - 4, y + cardH, DIVIDER);

            // Row 1: Player name + status badge + path nodes + position
            g.drawString(font, p.name, left + 11, y + 3, selected ? ACCENT : TEXT, false);
            int nameW = font.width(p.name);

            if (p.botActive) {
                String badge = p.botPaused ? " PAUSED" : " ACTIVE";
                int badgeColor = p.botPaused ? WARNING : SUCCESS;
                int badgeBg = p.botPaused ? 0x30D29922 : 0x303FB950;
                int badgeX = left + 14 + nameW;
                int badgeW = font.width(badge) + 4;
                g.fill(badgeX, y + 2, badgeX + badgeW, y + 12, badgeBg);
                g.drawString(font, badge, badgeX + 2, y + 3, badgeColor, false);

                // Path node count if path is toggled
                if (pathToggledPlayers.contains(p.name)) {
                    int nodeCount = pathNodeCounts.getOrDefault(p.name, 0);
                    String pathStr = " [" + nodeCount + " nodes]";
                    int pathX = badgeX + badgeW + 4;
                    g.drawString(font, pathStr, pathX, y + 3, 0xFF79C0FF, false);
                }
            }

            // Position (right-aligned)
            String posStr = p.x + ", " + p.y + ", " + p.z;
            int posW = font.width(posStr);
            g.drawString(font, posStr, right - 10 - posW, y + 3, LABEL, false);

            // Row 2: Active process + ETA + goal coords
            if (p.botActive) {
                int row2Y = y + 15;
                // Process name prominently
                String processLabel = !p.processName.isEmpty() && !"none".equals(p.processName) ? p.processName.toUpperCase() : "";
                if (!processLabel.isEmpty()) {
                    int procW = font.width(processLabel) + 6;
                    g.fill(left + 11, row2Y - 1, left + 11 + procW, row2Y + 9, 0x30FFFFFF);
                    g.drawString(font, processLabel, left + 14, row2Y, ACCENT, false);

                    // Status text after process label
                    String status = p.botStatus;
                    if (status.length() > 40) status = status.substring(0, 37) + "...";
                    g.drawString(font, status, left + 18 + procW, row2Y, LABEL, false);
                } else {
                    String status = p.botStatus;
                    if (status.length() > 50) status = status.substring(0, 47) + "...";
                    g.drawString(font, status, left + 11, row2Y, LABEL, false);
                }

                // ETA (right-aligned on row 2)
                if (p.eta > 0) {
                    String etaStr = "ETA: " + p.eta + "s";
                    int etaW = font.width(etaStr);
                    g.drawString(font, etaStr, right - 10 - etaW, row2Y, WARNING, false);
                }

                // Row 3: Enhanced stats bar + goal coordinates
                int statsY = y + 27;
                int statX = left + 11;

                // Compact stats: Mined: X | Farmed: Y | Built: Z | Kills: W
                if (p.blocksMined > 0 || p.cropsHarvested > 0 || p.blocksPlaced > 0 || p.kills > 0) {
                    if (p.blocksMined > 0) {
                        String s = "Mined:" + p.blocksMined;
                        g.drawString(font, s, statX, statsY, 0xFF79C0FF, false);
                        statX += font.width(s) + 4;
                        g.drawString(font, "|", statX, statsY, 0xFF484F58, false);
                        statX += font.width("|") + 4;
                    }
                    if (p.cropsHarvested > 0) {
                        String s = "Farmed:" + p.cropsHarvested;
                        g.drawString(font, s, statX, statsY, 0xFF7EE787, false);
                        statX += font.width(s) + 4;
                        g.drawString(font, "|", statX, statsY, 0xFF484F58, false);
                        statX += font.width("|") + 4;
                    }
                    if (p.blocksPlaced > 0) {
                        String s = "Built:" + p.blocksPlaced;
                        g.drawString(font, s, statX, statsY, 0xFFD2A8FF, false);
                        statX += font.width(s) + 4;
                        g.drawString(font, "|", statX, statsY, 0xFF484F58, false);
                        statX += font.width("|") + 4;
                    }
                    if (p.kills > 0) {
                        String s = "Kills:" + p.kills;
                        g.drawString(font, s, statX, statsY, ERROR, false);
                    }
                } else {
                    g.drawString(font, "No stats yet", left + 11, statsY, 0xFF484F58, false);
                }

                // Goal coordinates (right-aligned on stats row)
                if (p.goalX != 0 || p.goalY != 0 || p.goalZ != 0) {
                    String goalStr = "Goal: " + p.goalX + "," + p.goalY + "," + p.goalZ;
                    int goalW = font.width(goalStr);
                    g.drawString(font, goalStr, right - 10 - goalW, statsY, 0xFF484F58, false);
                }

                // Row 4: Mining progress bar (if status contains progress pattern)
                int progY = y + 39;
                int[] progress = parseProgress(p.botStatus);
                if (progress != null) {
                    int barW = Math.min(w - 24, 120);
                    g.fill(left + 11, progY, left + 11 + barW, progY + 6, PROGRESS_BG);
                    int fillW = (int) ((double) progress[0] / progress[1] * barW);
                    g.fill(left + 11, progY, left + 11 + fillW, progY + 6, PROGRESS_FILL);
                    g.drawString(font, progress[0] + "/" + progress[1], left + 15 + barW, progY - 1, LABEL, false);
                }

                // Row 5: Action buttons
                int btnY = y + 51;
                int btnX = left + 11;

                // Pause/Resume
                String pauseLabel = p.botPaused ? "Resume" : "Pause";
                int pauseW = font.width(pauseLabel) + 8;
                boolean hoverPause = hoverCard && mouseX >= btnX && mouseX < btnX + pauseW && mouseY >= btnY && mouseY < btnY + BTN_H;
                g.fill(btnX, btnY, btnX + pauseW, btnY + BTN_H, hoverPause ? BTN_HOVER : BTN_BG);
                g.drawString(font, pauseLabel, btnX + 4, btnY + 3, p.botPaused ? SUCCESS : WARNING, false);
                btnX += pauseW + 3;

                // Stop
                int stopW = font.width("Stop") + 8;
                boolean hoverStop = hoverCard && mouseX >= btnX && mouseX < btnX + stopW && mouseY >= btnY && mouseY < btnY + BTN_H;
                g.fill(btnX, btnY, btnX + stopW, btnY + BTN_H, hoverStop ? BTN_HOVER : BTN_BG);
                g.drawString(font, "Stop", btnX + 4, btnY + 3, ERROR, false);
                btnX += stopW + 3;

                // Log toggle
                String logLabel = (showLog && selected) ? "Hide Log" : "Log";
                int logW = font.width(logLabel) + 8;
                boolean hoverLog = hoverCard && mouseX >= btnX && mouseX < btnX + logW && mouseY >= btnY && mouseY < btnY + BTN_H;
                g.fill(btnX, btnY, btnX + logW, btnY + BTN_H, hoverLog ? BTN_HOVER : BTN_BG);
                g.drawString(font, logLabel, btnX + 4, btnY + 3, ACCENT, false);
                btnX += logW + 3;

                // Settings toggle
                String settLabel = (showSettings && selected) ? "Hide Settings" : "Settings";
                int settW = font.width(settLabel) + 8;
                boolean hoverSett = hoverCard && mouseX >= btnX && mouseX < btnX + settW && mouseY >= btnY && mouseY < btnY + BTN_H;
                g.fill(btnX, btnY, btnX + settW, btnY + BTN_H, hoverSett ? BTN_HOVER : BTN_BG);
                g.drawString(font, settLabel, btnX + 4, btnY + 3, WARNING, false);
                btnX += settW + 3;

                // Show Path toggle
                boolean pathOn = pathToggledPlayers.contains(p.name);
                String pathLabel = pathOn ? "Hide Path" : "Show Path";
                int pathW = font.width(pathLabel) + 8;
                boolean hoverPath = hoverCard && mouseX >= btnX && mouseX < btnX + pathW && mouseY >= btnY && mouseY < btnY + BTN_H;
                g.fill(btnX, btnY, btnX + pathW, btnY + BTN_H, hoverPath ? BTN_HOVER : BTN_BG);
                g.drawString(font, pathLabel, btnX + 4, btnY + 3, pathOn ? SUCCESS : LABEL, false);
                btnX += pathW + 3;

                // Track extra height from expanded sections
                int extraY = y + 67;

                // ---- Quick Command Buttons (below action buttons, for selected player) ----
                if (selected) {
                    g.fill(left + 8, extraY - 2, right - 8, extraY - 1, DIVIDER);
                    g.drawString(font, "Quick Commands", left + 11, extraY, LABEL, false);
                    extraY += 11;

                    // Row 1: Main quick commands
                    int qX = left + 11;
                    for (int qi = 0; qi < QUICK_CMDS.length; qi++) {
                        String ql = QUICK_CMDS[qi][0];
                        int qw = font.width(ql) + 8;
                        if (qX + qw > right - 10) {
                            // Wrap to next line
                            qX = left + 11;
                            extraY += BTN_H + 2;
                        }
                        boolean hoverQ = mouseX >= qX && mouseX < qX + qw && mouseY >= extraY && mouseY < extraY + BTN_H
                            && mouseY >= contentTop && mouseY < contentBottom;
                        g.fill(qX, extraY, qX + qw, extraY + BTN_H, hoverQ ? BTN_HOVER : BTN_BG);
                        g.drawString(font, ql, qX + 4, extraY + 3, QUICK_COLORS[qi], false);
                        qX += qw + 2;
                    }
                    extraY += BTN_H + 2;

                    // Row 2: Tunnel direction buttons
                    qX = left + 11;
                    for (String[] td : TUNNEL_DIRS) {
                        int tw = font.width(td[0]) + 8;
                        boolean hoverT = mouseX >= qX && mouseX < qX + tw && mouseY >= extraY && mouseY < extraY + BTN_H
                            && mouseY >= contentTop && mouseY < contentBottom;
                        g.fill(qX, extraY, qX + tw, extraY + BTN_H, hoverT ? BTN_HOVER : BTN_BG);
                        g.drawString(font, td[0], qX + 4, extraY + 3, 0xFF79C0FF, false);
                        qX += tw + 2;
                    }
                    extraY += BTN_H + 4;
                }

                // ---- Log display (expanded, color-coded) ----
                if (showLog && selected && !botLog.isEmpty()) {
                    g.fill(left + 8, extraY - 2, right - 8, extraY - 1, DIVIDER);
                    g.drawString(font, "Log (" + botLog.size() + " entries)", left + 11, extraY, LABEL, false);
                    extraY += 11;

                    int logLines = Math.min(botLog.size(), 20);
                    g.fill(left + 8, extraY - 1, right - 8, extraY + logLines * 9 + 2, 0xE00D1117);
                    int logIdx = Math.max(0, botLog.size() - logLines);
                    for (int li = logIdx; li < botLog.size(); li++) {
                        String logLine = botLog.get(li);
                        if (logLine.length() > 70) logLine = logLine.substring(0, 67) + "...";
                        int logColor = getLogColor(logLine);
                        g.drawString(font, logLine, left + 10, extraY + 1, logColor, false);
                        extraY += 9;
                    }
                    extraY += 4;
                }

                // ---- Settings panel (expanded) ----
                if (showSettings && selected && !settingsMap.isEmpty()) {
                    g.fill(left + 8, extraY - 2, right - 8, extraY - 1, DIVIDER);
                    g.drawString(font, "Bot Settings", left + 11, extraY, LABEL, false);
                    extraY += 12;

                    int settingsRendered = 0;
                    for (String[] category : SETTINGS_CATEGORIES) {
                        String catName = category[0];
                        // Category header
                        g.drawString(font, catName, left + 11, extraY, TEXT, false);
                        g.fill(left + 11 + font.width(catName) + 3, extraY + 4, left + 11 + font.width(catName) + 60, extraY + 5, DIVIDER);
                        extraY += 11;

                        for (int si = 1; si < category.length; si++) {
                            String key = category[si];
                            String val = settingsMap.getOrDefault(key, "?");

                            // Setting label
                            g.drawString(font, key, left + 15, extraY + 1, LABEL, false);

                            if (BOOLEAN_SETTINGS.contains(key)) {
                                // Toggle button
                                boolean isTrue = "true".equals(val);
                                String toggleLabel = isTrue ? "ON" : "OFF";
                                int toggleColor = isTrue ? SUCCESS : ERROR;
                                int toggleW = font.width(toggleLabel) + 8;
                                int toggleX = right - 14 - toggleW;
                                boolean hoverToggle = mouseX >= toggleX && mouseX < toggleX + toggleW
                                    && mouseY >= extraY && mouseY < extraY + 12
                                    && mouseY >= contentTop && mouseY < contentBottom;
                                g.fill(toggleX, extraY, toggleX + toggleW, extraY + 12, hoverToggle ? BTN_HOVER : BTN_BG);
                                g.drawString(font, toggleLabel, toggleX + 4, extraY + 2, toggleColor, false);
                            } else {
                                // Numeric value with +/- buttons
                                int valW = font.width(val);
                                int valX = right - 14 - valW - 28; // 28 for +/- buttons
                                g.drawString(font, val, valX, extraY + 1, TEXT, false);

                                // [-] button
                                int minusBtnX = right - 14 - 24;
                                boolean hoverMinus = mouseX >= minusBtnX && mouseX < minusBtnX + 10
                                    && mouseY >= extraY && mouseY < extraY + 12
                                    && mouseY >= contentTop && mouseY < contentBottom;
                                g.fill(minusBtnX, extraY, minusBtnX + 10, extraY + 12, hoverMinus ? BTN_HOVER : BTN_BG);
                                g.drawString(font, "-", minusBtnX + 3, extraY + 2, ERROR, false);

                                // [+] button
                                int plusBtnX = right - 14 - 12;
                                boolean hoverPlus = mouseX >= plusBtnX && mouseX < plusBtnX + 10
                                    && mouseY >= extraY && mouseY < extraY + 12
                                    && mouseY >= contentTop && mouseY < contentBottom;
                                g.fill(plusBtnX, extraY, plusBtnX + 10, extraY + 12, hoverPlus ? BTN_HOVER : BTN_BG);
                                g.drawString(font, "+", plusBtnX + 2, extraY + 2, SUCCESS, false);
                            }
                            extraY += 13;
                            settingsRendered++;
                        }
                        extraY += 3; // gap between categories
                    }
                    extraY += 4;
                }
            } else {
                // Inactive bot - show quick start buttons
                g.drawString(font, "No bot running", left + 11, y + 15, 0xFF484F58, false);

                int btnY = y + 28;
                int btnX = left + 11;
                String[] quickBtns = {"GoTo...", "Mine...", "Follow...", "Farm", "Explore", "Come"};
                int[] quickColors = {ACCENT, SUCCESS, WARNING, SUCCESS, 0xFFD2A8FF, ACCENT};
                for (int b = 0; b < quickBtns.length; b++) {
                    int bw = font.width(quickBtns[b]) + 8;
                    if (btnX + bw > right - 10) break; // Don't overflow
                    boolean hoverBtn = hoverCard && mouseX >= btnX && mouseX < btnX + bw && mouseY >= btnY && mouseY < btnY + BTN_H;
                    g.fill(btnX, btnY, btnX + bw, btnY + BTN_H, hoverBtn ? BTN_HOVER : BTN_BG);
                    g.drawString(font, quickBtns[b], btnX + 4, btnY + 3, quickColors[b], false);
                    btnX += bw + 3;
                }
            }

            y += cardH + 3;
            totalContentH += cardH + 3;
        }

        if (players.isEmpty()) {
            g.drawString(font, "No players online", left + 6, y, LABEL, false);
            y += 14;
        }

        maxScroll = Math.max(0, totalContentH - (contentBottom - contentTop) + 30);
        g.disableScissor();

        // === Command input area (fixed at bottom) ===
        int inputY = bottom - 24;
        g.fill(left, inputY - 2, right, bottom, HEADER_BG);
        g.fill(left + 4, inputY, right - 4, inputY + 20, INPUT_BG);
        g.fill(left + 4, inputY, right - 4, inputY + 1, BORDER);
        g.fill(left + 4, inputY + 19, right - 4, inputY + 20, BORDER);

        String prefix = selectedPlayer != null ? selectedPlayer + " > " : "Select player > ";
        int prefixW = font.width(prefix);
        g.drawString(font, prefix, left + 8, inputY + 6, selectedPlayer != null ? ACCENT : LABEL, false);

        // Command text with cursor
        cursorBlink++;
        String displayCmd = commandBuffer;
        if (commandFocused && (cursorBlink / 10) % 2 == 0) {
            displayCmd += "_";
        }
        g.drawString(font, displayCmd, left + 8 + prefixW, inputY + 6, TEXT, false);

        // History indicator
        if (!commandHistory.isEmpty() && commandFocused) {
            String histHint = "[" + commandHistory.size() + "]";
            int histW = font.width(histHint);
            g.drawString(font, histHint, right - 8 - histW, inputY + 6, 0xFF484F58, false);
        }

        // Autocomplete suggestions popup (above input)
        if (!suggestions.isEmpty() && commandFocused) {
            int sugY = inputY - 2 - suggestions.size() * 12 - 4;
            int sugX = left + 8 + prefixW;
            int maxSugW = 0;
            for (String s : suggestions) maxSugW = Math.max(maxSugW, font.width(s));
            maxSugW = Math.min(maxSugW, w - prefixW - 24);
            g.fill(sugX - 2, sugY - 2, sugX + maxSugW + 6, inputY - 2, 0xEE161B22);
            g.fill(sugX - 2, sugY - 2, sugX + maxSugW + 6, sugY - 1, BORDER);
            for (int si = 0; si < suggestions.size(); si++) {
                boolean sel = si == selectedSuggestion;
                if (sel) {
                    g.fill(sugX - 2, sugY, sugX + maxSugW + 6, sugY + 11, 0xFF21262D);
                }
                g.drawString(font, suggestions.get(si), sugX, sugY + 1, sel ? ACCENT : LABEL, false);
                sugY += 12;
            }
        }
    }

    /** Calculate the dynamic height of a player card based on expanded sections */
    private int calculateCardHeight(PlayerBotInfo p, boolean selected) {
        if (!p.botActive) return CARD_H_INACTIVE;

        int h = CARD_H_ACTIVE;

        // Quick command buttons (when selected, active)
        if (selected) {
            // "Quick Commands" header
            h += 13;
            // Calculate rows needed for quick commands
            int qX = 0;
            int rowW = 0;
            // We'll estimate the available width at ~w-22 but we don't have w here directly.
            // Use a conservative estimate. The buttons wrap at right-10 from left+11, so ~w-21.
            // We'll use the font to measure. Since we can't get w easily, just add 2 rows.
            h += (BTN_H + 2) * 2; // main commands row + tunnel row
            h += 4; // gap
        }

        // Log section
        if (showLog && selected && !botLog.isEmpty()) {
            int logLines = Math.min(botLog.size(), 20);
            h += 11 + logLines * 9 + 6; // header + lines + padding
        }

        // Settings section
        if (showSettings && selected && !settingsMap.isEmpty()) {
            h += 12; // "Bot Settings" header
            for (String[] category : SETTINGS_CATEGORIES) {
                h += 11; // category header
                h += (category.length - 1) * 13; // settings rows
                h += 3; // gap
            }
            h += 4;
        }

        return h;
    }

    /** Get a color for a log line based on keywords */
    private int getLogColor(String line) {
        String lower = line.toLowerCase();
        if (lower.contains("error") || lower.contains("fail") || lower.contains("no path") || lower.contains("not found")) {
            return LOG_FAIL;
        }
        if (lower.contains("mined") || lower.contains("harvested") || lower.contains("arrived") || lower.contains("complete")
            || lower.contains("saved") || lower.contains("placed")) {
            return LOG_SUCCESS;
        }
        if (lower.contains("goto") || lower.contains("path") || lower.contains("going") || lower.contains("following")
            || lower.contains("exploring") || lower.contains("tunnel")) {
            return LOG_NAV;
        }
        return LOG_DEFAULT;
    }

    // ================================
    // MOUSE CLICK
    // ================================

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        int w = right - left;

        // === Header buttons ===
        int hbX = right - 6;

        // Refresh
        int refreshW = font.width("Refresh") + 8;
        hbX -= refreshW;
        if (mouseX >= hbX && mouseX < hbX + refreshW && mouseY >= top + 3 && mouseY < top + 17) {
            requestData();
            setStatus("Refreshing...", ACCENT);
            return true;
        }
        hbX -= 4;

        // Stop All
        int stopAllW = font.width("Stop All") + 8;
        hbX -= stopAllW;
        if (mouseX >= hbX && mouseX < hbX + stopAllW && mouseY >= top + 3 && mouseY < top + 17) {
            sendAction("bot_group_command", "{\"cmd\":\"stop\"}");
            setStatus("Stopping all bots", ERROR);
            return true;
        }
        hbX -= 4;

        // Pause All
        int pauseAllW = font.width("Pause All") + 8;
        hbX -= pauseAllW;
        if (mouseX >= hbX && mouseX < hbX + pauseAllW && mouseY >= top + 3 && mouseY < top + 17) {
            sendAction("bot_group_command", "{\"cmd\":\"pause\"}");
            setStatus("Pausing all bots", WARNING);
            return true;
        }
        hbX -= 4;

        // Resume All
        int resumeAllW = font.width("Resume All") + 8;
        hbX -= resumeAllW;
        if (mouseX >= hbX && mouseX < hbX + resumeAllW && mouseY >= top + 3 && mouseY < top + 17) {
            sendAction("bot_group_command", "{\"cmd\":\"resume\"}");
            setStatus("Resuming all bots", SUCCESS);
            return true;
        }

        // === Player card clicks ===
        int contentTop = top + 28;
        int contentBottom = bottom - 26;
        if (mouseY < contentTop || mouseY >= contentBottom) {
            // Check command input click
            int inputY = bottom - 24;
            if (mouseX >= left + 4 && mouseX < right - 4 && mouseY >= inputY && mouseY < inputY + 20) {
                commandFocused = true;
                return true;
            }
            commandFocused = false;
            return false;
        }

        int y = contentTop + 4 - scroll;

        // Skip status message offset
        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            y += 14;
        }
        y += 14; // "Players" header

        for (int i = 0; i < players.size(); i++) {
            PlayerBotInfo p = players.get(i);
            boolean selected = p.name.equals(selectedPlayer);
            int cardH = calculateCardHeight(p, selected);

            if (mouseX >= left + 4 && mouseX < right - 4 && mouseY >= y && mouseY < y + cardH) {
                if (p.botActive) {
                    // === Action buttons row (y + 51) ===
                    int btnY = y + 51;
                    int btnX = left + 11;

                    // Pause/Resume
                    String pauseLabel = p.botPaused ? "Resume" : "Pause";
                    int pauseW = font.width(pauseLabel) + 8;
                    if (mouseX >= btnX && mouseX < btnX + pauseW && mouseY >= btnY && mouseY < btnY + BTN_H) {
                        sendCommand(p.name, p.botPaused ? "resume" : "pause");
                        return true;
                    }
                    btnX += pauseW + 3;

                    // Stop
                    int stopW = font.width("Stop") + 8;
                    if (mouseX >= btnX && mouseX < btnX + stopW && mouseY >= btnY && mouseY < btnY + BTN_H) {
                        sendAction("bot_cancel", "{\"target\":\"" + p.name + "\"}");
                        setStatus("Stopping bot for " + p.name, WARNING);
                        return true;
                    }
                    btnX += stopW + 3;

                    // Log toggle
                    String logLabel = (showLog && p.name.equals(selectedPlayer)) ? "Hide Log" : "Log";
                    int logW = font.width(logLabel) + 8;
                    if (mouseX >= btnX && mouseX < btnX + logW && mouseY >= btnY && mouseY < btnY + BTN_H) {
                        if (showLog && p.name.equals(selectedPlayer)) {
                            showLog = false;
                        } else {
                            selectedPlayer = p.name;
                            showLog = true;
                            sendAction("bot_request_log", "{\"target\":\"" + p.name + "\"}");
                        }
                        return true;
                    }
                    btnX += logW + 3;

                    // Settings toggle
                    String settLabel = (showSettings && p.name.equals(selectedPlayer)) ? "Hide Settings" : "Settings";
                    int settW = font.width(settLabel) + 8;
                    if (mouseX >= btnX && mouseX < btnX + settW && mouseY >= btnY && mouseY < btnY + BTN_H) {
                        if (showSettings && p.name.equals(selectedPlayer)) {
                            showSettings = false;
                        } else {
                            selectedPlayer = p.name;
                            showSettings = true;
                            // Request full settings from server
                            sendAction("bot_settings_all", "{\"target\":\"" + p.name + "\"}");
                        }
                        return true;
                    }
                    btnX += settW + 3;

                    // Show Path toggle
                    boolean pathOn = pathToggledPlayers.contains(p.name);
                    String pathLabel = pathOn ? "Hide Path" : "Show Path";
                    int pathW = font.width(pathLabel) + 8;
                    if (mouseX >= btnX && mouseX < btnX + pathW && mouseY >= btnY && mouseY < btnY + BTN_H) {
                        if (pathOn) {
                            pathToggledPlayers.remove(p.name);
                            com.ultra.megamod.feature.baritone.screen.BotPathRenderer.clear();
                        } else {
                            pathToggledPlayers.add(p.name);
                            sendAction("bot_request_path", "{\"target\":\"" + p.name + "\"}");
                        }
                        return true;
                    }
                    btnX += pathW + 3;

                    // === Quick Command Buttons (for selected player) ===
                    if (p.name.equals(selectedPlayer)) {
                        int extraY = y + 67;
                        extraY += 11; // "Quick Commands" label

                        // Row 1: Main quick commands
                        int qX = left + 11;
                        for (int qi = 0; qi < QUICK_CMDS.length; qi++) {
                            String ql = QUICK_CMDS[qi][0];
                            String qc = QUICK_CMDS[qi][1];
                            int qw = font.width(ql) + 8;
                            if (qX + qw > right - 10) {
                                qX = left + 11;
                                extraY += BTN_H + 2;
                            }
                            if (mouseX >= qX && mouseX < qX + qw && mouseY >= extraY && mouseY < extraY + BTN_H) {
                                if (qc.equals("quarry")) {
                                    // For quarry, put command in buffer so user can add coords
                                    selectedPlayer = p.name;
                                    commandBuffer = "quarry ";
                                    commandFocused = true;
                                } else {
                                    sendCommand(p.name, qc);
                                }
                                return true;
                            }
                            qX += qw + 2;
                        }
                        extraY += BTN_H + 2;

                        // Row 2: Tunnel direction buttons
                        qX = left + 11;
                        for (String[] td : TUNNEL_DIRS) {
                            int tw = font.width(td[0]) + 8;
                            if (mouseX >= qX && mouseX < qX + tw && mouseY >= extraY && mouseY < extraY + BTN_H) {
                                sendCommand(p.name, td[1]);
                                return true;
                            }
                            qX += tw + 2;
                        }
                        extraY += BTN_H + 4;

                        // === Settings panel clicks ===
                        if (showSettings && !settingsMap.isEmpty()) {
                            // Skip log section height if log is also showing
                            if (showLog && !botLog.isEmpty()) {
                                int logLines = Math.min(botLog.size(), 20);
                                extraY += 11 + logLines * 9 + 6;
                            }

                            extraY += 12; // "Bot Settings" header

                            for (String[] category : SETTINGS_CATEGORIES) {
                                extraY += 11; // category header
                                for (int si = 1; si < category.length; si++) {
                                    String key = category[si];
                                    String val = settingsMap.getOrDefault(key, "?");

                                    if (BOOLEAN_SETTINGS.contains(key)) {
                                        // Toggle button
                                        boolean isTrue = "true".equals(val);
                                        String toggleLabel = isTrue ? "ON" : "OFF";
                                        int toggleW = font.width(toggleLabel) + 8;
                                        int toggleX = right - 14 - toggleW;
                                        if (mouseX >= toggleX && mouseX < toggleX + toggleW
                                            && mouseY >= extraY && mouseY < extraY + 12) {
                                            String newVal = isTrue ? "false" : "true";
                                            settingsMap.put(key, newVal);
                                            sendToggleSetting(p.name, key, newVal);
                                            return true;
                                        }
                                    } else {
                                        // +/- buttons for numeric
                                        int minusBtnX = right - 14 - 24;
                                        if (mouseX >= minusBtnX && mouseX < minusBtnX + 10
                                            && mouseY >= extraY && mouseY < extraY + 12) {
                                            adjustNumericSetting(p.name, key, val, -1);
                                            return true;
                                        }
                                        int plusBtnX = right - 14 - 12;
                                        if (mouseX >= plusBtnX && mouseX < plusBtnX + 10
                                            && mouseY >= extraY && mouseY < extraY + 12) {
                                            adjustNumericSetting(p.name, key, val, 1);
                                            return true;
                                        }
                                    }
                                    extraY += 13;
                                }
                                extraY += 3;
                            }
                        }
                    }
                } else {
                    // Quick start buttons for inactive bots
                    int btnY = y + 28;
                    int btnX = left + 11;
                    String[] quickBtns = {"GoTo...", "Mine...", "Follow...", "Farm", "Explore", "Come"};
                    for (int b = 0; b < quickBtns.length; b++) {
                        int bw = font.width(quickBtns[b]) + 8;
                        if (btnX + bw > right - 10) break;
                        if (mouseX >= btnX && mouseX < btnX + bw && mouseY >= btnY && mouseY < btnY + BTN_H) {
                            selectedPlayer = p.name;
                            commandFocused = true;
                            switch (b) {
                                case 0 -> commandBuffer = "goto ";
                                case 1 -> commandBuffer = "mine ";
                                case 2 -> commandBuffer = "follow ";
                                case 3 -> { sendCommand(p.name, "farm"); return true; }
                                case 4 -> { sendCommand(p.name, "explore"); return true; }
                                case 5 -> { sendCommand(p.name, "come"); return true; }
                            }
                            return true;
                        }
                        btnX += bw + 3;
                    }
                }

                // Select player (if no button was hit)
                selectedPlayer = p.name;
                commandFocused = true;
                return true;
            }

            y += cardH + 3;
        }

        commandFocused = false;
        return false;
    }

    // ================================
    // KEYBOARD
    // ================================

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!commandFocused) return false;

        if (keyCode == 259) { // Backspace
            if (!commandBuffer.isEmpty()) {
                commandBuffer = commandBuffer.substring(0, commandBuffer.length() - 1);
                updateSuggestions();
            }
            return true;
        }
        if (keyCode == 257) { // Enter
            if (!commandBuffer.isEmpty() && selectedPlayer != null) {
                sendCommand(selectedPlayer, commandBuffer);
                commandHistory.add(0, commandBuffer);
                if (commandHistory.size() > 20) commandHistory.remove(commandHistory.size() - 1);
                historyIndex = -1;
                commandBuffer = "";
                suggestions.clear();
                selectedSuggestion = -1;
            }
            return true;
        }
        if (keyCode == 258) { // Tab — accept suggestion
            if (!suggestions.isEmpty()) {
                int idx = selectedSuggestion >= 0 ? selectedSuggestion : 0;
                if (idx < suggestions.size()) {
                    String suggestion = suggestions.get(idx);
                    int lastSpace = commandBuffer.lastIndexOf(' ');
                    if (lastSpace >= 0) {
                        commandBuffer = commandBuffer.substring(0, lastSpace + 1) + suggestion;
                    } else {
                        commandBuffer = suggestion;
                    }
                    commandBuffer += " ";
                    suggestions.clear();
                    selectedSuggestion = -1;
                    updateSuggestions();
                }
            }
            return true;
        }
        if (keyCode == 264) { // Down arrow
            if (!suggestions.isEmpty()) {
                selectedSuggestion = (selectedSuggestion + 1) % suggestions.size();
            }
            return true;
        }
        if (keyCode == 265) { // Up arrow
            if (!suggestions.isEmpty()) {
                selectedSuggestion = selectedSuggestion <= 0 ? suggestions.size() - 1 : selectedSuggestion - 1;
            } else if (!commandHistory.isEmpty()) {
                // Command history recall
                historyIndex = Math.min(historyIndex + 1, commandHistory.size() - 1);
                commandBuffer = commandHistory.get(historyIndex);
                updateSuggestions();
            }
            return true;
        }
        if (keyCode == 256) { // Escape
            commandFocused = false;
            suggestions.clear();
            return true;
        }
        return false;
    }

    public boolean charTyped(char ch, int modifiers) {
        if (!commandFocused) return false;
        if (ch >= 32 && ch < 127) {
            commandBuffer += ch;
            historyIndex = -1;
            updateSuggestions();
            return true;
        }
        return false;
    }

    // ================================
    // AUTOCOMPLETE (enhanced with new commands)
    // ================================

    private void updateSuggestions() {
        suggestions.clear();
        selectedSuggestion = -1;
        if (commandBuffer.isEmpty()) return;

        String[] parts = commandBuffer.split("\\s+", -1);
        String lastToken = parts[parts.length - 1].toLowerCase();

        if (parts.length == 1) {
            for (String cmd : COMMANDS) {
                if (cmd.startsWith(lastToken)) suggestions.add(cmd);
            }
        } else {
            String cmd = parts[0].toLowerCase();
            if (parts.length == 2) {
                switch (cmd) {
                    case "mine", "gotoblock", "scan" -> {
                        for (String block : COMMON_BLOCKS) {
                            if (block.startsWith(lastToken)) suggestions.add(block);
                        }
                    }
                    case "follow" -> {
                        for (PlayerBotInfo p : players) {
                            if (p.name.toLowerCase().startsWith(lastToken)) suggestions.add(p.name);
                        }
                    }
                    case "settings" -> {
                        for (String key : SETTINGS_KEYS) {
                            if (key.toLowerCase().startsWith(lastToken)) suggestions.add(key);
                        }
                    }
                    case "build" -> {
                        if (lastToken.isEmpty()) suggestions.add("5");
                    }
                    case "tunnel" -> {
                        String[] dirs = {"north", "south", "east", "west"};
                        for (String d : dirs) {
                            if (d.startsWith(lastToken)) suggestions.add(d);
                        }
                    }
                    case "waypoint" -> {
                        String[] subs = {"save", "goto", "list", "delete"};
                        for (String s : subs) {
                            if (s.startsWith(lastToken)) suggestions.add(s);
                        }
                    }
                    case "quarry" -> {
                        // Suggest common sizes
                        String[] sizes = {"16", "32", "64"};
                        for (String s : sizes) {
                            if (s.startsWith(lastToken)) suggestions.add(s);
                        }
                    }
                    case "combat" -> {
                        String[] modes = {"aggressive", "defensive", "passive"};
                        for (String m : modes) {
                            if (m.startsWith(lastToken)) suggestions.add(m);
                        }
                    }
                    case "deposit", "withdraw" -> {
                        for (String block : COMMON_BLOCKS) {
                            if (block.startsWith(lastToken)) suggestions.add(block);
                        }
                        // Also suggest "all"
                        if ("all".startsWith(lastToken)) suggestions.add("all");
                    }
                    case "chest" -> {
                        String[] subs = {"find", "sort", "dump", "load"};
                        for (String s : subs) {
                            if (s.startsWith(lastToken)) suggestions.add(s);
                        }
                    }
                }
            } else if (parts.length == 5 && "build".equals(cmd)) {
                for (String block : COMMON_BLOCKS) {
                    if (block.startsWith(lastToken)) suggestions.add(block);
                }
            } else if (parts.length == 3 && "settings".equals(cmd)) {
                String key = parts[1];
                if (BOOLEAN_SETTINGS.contains(key)) {
                    if ("true".startsWith(lastToken)) suggestions.add("true");
                    if ("false".startsWith(lastToken)) suggestions.add("false");
                }
            } else if (parts.length == 3 && "tunnel".equals(cmd)) {
                // Suggest tunnel length
                String[] lengths = {"25", "50", "100", "200"};
                for (String l : lengths) {
                    if (l.startsWith(lastToken)) suggestions.add(l);
                }
            }
        }
        if (suggestions.size() > 8) suggestions = new ArrayList<>(suggestions.subList(0, 8));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int)(scrollY * 20)));
        return true;
    }

    // ================================
    // TICK (auto-refresh logic)
    // ================================

    public void tick() {
        refreshCooldown--;
        if (refreshCooldown <= 0) {
            requestData();
            refreshCooldown = 40; // ~2 seconds
        }

        // Auto-refresh path data for toggled players every 2 seconds
        pathRefreshCooldown--;
        if (pathRefreshCooldown <= 0) {
            for (String playerName : pathToggledPlayers) {
                sendAction("bot_request_path", "{\"target\":\"" + playerName + "\"}");
            }
            pathRefreshCooldown = 40;
        }

        // Auto-refresh log every 3 seconds if log is open
        logRefreshCooldown--;
        if (logRefreshCooldown <= 0 && showLog && selectedPlayer != null) {
            sendAction("bot_request_log", "{\"target\":\"" + selectedPlayer + "\"}");
            logRefreshCooldown = 60; // ~3 seconds
        }
    }

    // ================================
    // RESPONSE HANDLING
    // ================================

    public void handleResponse(String type, String jsonData) {
        switch (type) {
            case "bot_status" -> parseBotStatus(jsonData);
            case "bot_command_result" -> parseCommandResult(jsonData);
            case "bot_log" -> parseBotLog(jsonData);
            case "bot_path" -> {
                parseBotPath(jsonData);
                com.ultra.megamod.feature.baritone.screen.BotPathRenderer.parsePathData(jsonData);
            }
            case "bot_settings_all" -> parseSettingsAll(jsonData);
        }
    }

    private void parseBotStatus(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            activeBots = obj.get("activeBots").getAsInt();
            totalBots = obj.get("totalBots").getAsInt();

            players.clear();
            JsonArray arr = obj.getAsJsonArray("players");
            for (JsonElement el : arr) {
                JsonObject p = el.getAsJsonObject();

                // Parse extended fields
                int kills = p.has("kills") ? p.get("kills").getAsInt() : 0;
                String processName = p.has("processName") ? p.get("processName").getAsString() : "";
                int goalX = p.has("goalX") ? p.get("goalX").getAsInt() : 0;
                int goalY = p.has("goalY") ? p.get("goalY").getAsInt() : 0;
                int goalZ = p.has("goalZ") ? p.get("goalZ").getAsInt() : 0;
                int eta = p.has("eta") ? p.get("eta").getAsInt() : -1;

                players.add(new PlayerBotInfo(
                    p.get("name").getAsString(),
                    p.get("uuid").getAsString(),
                    p.get("x").getAsInt(),
                    p.get("y").getAsInt(),
                    p.get("z").getAsInt(),
                    p.get("botActive").getAsBoolean(),
                    p.has("botStatus") ? p.get("botStatus").getAsString() : "",
                    p.has("botPaused") && p.get("botPaused").getAsBoolean(),
                    p.has("blocksMined") ? p.get("blocksMined").getAsInt() : 0,
                    p.has("cropsHarvested") ? p.get("cropsHarvested").getAsInt() : 0,
                    p.has("blocksPlaced") ? p.get("blocksPlaced").getAsInt() : 0,
                    p.has("settings") ? p.get("settings").getAsString() : "",
                    kills,
                    processName,
                    goalX, goalY, goalZ,
                    eta
                ));

                // If this player has settings data and is selected, update the settings map
                if (p.has("settings") && p.get("name").getAsString().equals(selectedPlayer)) {
                    parseSettingsJson(p.get("settings").getAsString());
                }

                // Update global bot active state for HUD overlay (track the local player's bot)
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && p.get("uuid").getAsString().equals(mc.player.getStringUUID())) {
                    boolean isActive = p.get("botActive").getAsBoolean();
                    String status = p.has("botStatus") ? p.get("botStatus").getAsString() : "";
                    com.ultra.megamod.feature.baritone.screen.BotPathRenderer.setBotActive(isActive, status);
                    if (p.has("eta")) {
                        com.ultra.megamod.feature.baritone.screen.BotPathRenderer.setEta(p.get("eta").getAsInt());
                    }
                    com.ultra.megamod.feature.baritone.screen.BotPathRenderer.setStats(
                        p.has("blocksMined") ? p.get("blocksMined").getAsInt() : 0,
                        p.has("cropsHarvested") ? p.get("cropsHarvested").getAsInt() : 0,
                        p.has("blocksPlaced") ? p.get("blocksPlaced").getAsInt() : 0
                    );
                }
            }
        } catch (Exception e) {
            setStatus("Parse error: " + e.getMessage(), ERROR);
        }
    }

    private void parseCommandResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String msg = obj.get("msg").getAsString();
            setStatus(msg, SUCCESS);
        } catch (Exception ignored) {}
    }

    private void parseBotLog(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            botLog.clear();
            JsonArray arr = obj.getAsJsonArray("log");
            for (JsonElement el : arr) {
                botLog.add(el.getAsString());
            }
        } catch (Exception ignored) {}
    }

    private void parseBotPath(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("path")) {
                JsonArray pathArr = obj.getAsJsonArray("path");
                // Store node count
                if (obj.has("status")) {
                    // Try to figure out which player this is for — use the target description
                }
                // We store node count for the currently-path-requested player
                if (selectedPlayer != null) {
                    pathNodeCounts.put(selectedPlayer, pathArr.size());
                }
                // Also update for any toggled players
                for (String pn : pathToggledPlayers) {
                    pathNodeCounts.put(pn, pathArr.size());
                }
            }
        } catch (Exception ignored) {}
    }

    private void parseSettingsAll(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("settings")) {
                parseSettingsJson(obj.get("settings").getAsString());
            }
        } catch (Exception ignored) {}
    }

    private void parseSettingsJson(String settingsJson) {
        try {
            JsonObject sObj = JsonParser.parseString(settingsJson).getAsJsonObject();
            settingsMap.clear();
            for (String key : SETTINGS_KEYS) {
                if (sObj.has(key)) {
                    settingsMap.put(key, sObj.get(key).getAsString());
                }
            }
        } catch (Exception ignored) {}
    }

    /** Parse progress from status strings like "Mining diamond_ore (3/64)" */
    private int[] parseProgress(String status) {
        if (status == null) return null;
        int openParen = status.lastIndexOf('(');
        int slash = status.indexOf('/', openParen > 0 ? openParen : 0);
        int closeParen = status.indexOf(')', slash > 0 ? slash : 0);
        if (openParen >= 0 && slash > openParen && closeParen > slash) {
            try {
                int current = Integer.parseInt(status.substring(openParen + 1, slash).trim());
                int total = Integer.parseInt(status.substring(slash + 1, closeParen).trim());
                if (total > 0) return new int[]{current, total};
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // ================================
    // ACTIONS
    // ================================

    private void sendCommand(String target, String cmd) {
        String json = "{\"target\":\"" + target + "\",\"cmd\":\"" + cmd.replace("\"", "\\\"") + "\"}";
        sendAction("bot_command", json);
        setStatus("Sent: " + cmd + " -> " + target, ACCENT);
    }

    private void sendToggleSetting(String target, String key, String value) {
        String json = "{\"target\":\"" + target + "\",\"key\":\"" + key + "\",\"value\":\"" + value + "\"}";
        sendAction("bot_toggle_setting", json);
        setStatus("Set " + key + " = " + value, SUCCESS);
    }

    private void adjustNumericSetting(String target, String key, String currentValue, int direction) {
        try {
            // Determine step size based on the magnitude of the current value
            if (currentValue.contains(".")) {
                double val = Double.parseDouble(currentValue);
                double step = val >= 1.0 ? 0.1 : 0.05;
                double newVal = val + direction * step;
                newVal = Math.round(newVal * 100.0) / 100.0;
                String newValStr = String.valueOf(newVal);
                settingsMap.put(key, newValStr);
                sendToggleSetting(target, key, newValStr);
            } else {
                int val = Integer.parseInt(currentValue);
                int step;
                if (val > 10000) step = 10000;
                else if (val > 1000) step = 1000;
                else if (val > 100) step = 100;
                else if (val > 10) step = 10;
                else step = 1;
                int newVal = val + direction * step;
                String newValStr = String.valueOf(Math.max(0, newVal));
                settingsMap.put(key, newValStr);
                sendToggleSetting(target, key, newValStr);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload(action, data),
            (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private void setStatus(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
        this.statusExpiry = System.currentTimeMillis() + 5000;
    }
}
