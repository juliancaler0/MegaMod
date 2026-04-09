package com.ultra.megamod.feature.baritone.screen;

import com.ultra.megamod.MegaMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Expanded HUD overlay that shows bot status and path visualization info.
 * Shows when bot is active (even without detailed path data), and indicates
 * that ESC or ` (grave accent) can cancel the bot.
 *
 * Displays: process status, destination, distance, direction, ETA,
 * stats (blocks mined, crops harvested), progress bar, cancel hint.
 */
public class BotPathRenderHandler {
    private static final int BG = 0xAA0D1117;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT = 0xFF58A6FF;
    private static final int PATH_COLOR = 0xFF3FB950;
    private static final int WARNING = 0xFFD29922;
    private static final int PROGRESS_BG = 0xFF21262D;
    private static final int PROGRESS_FG = 0xFF3FB950;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(MegaMod.MODID, "bot_path_hud"), BotPathRenderHandler::renderOverlay);
    }

    private static void renderOverlay(GuiGraphics g, DeltaTracker deltaTracker) {
        // Show overlay when bot is active OR when path data is available
        boolean hasPath = BotPathRenderer.isEnabled();
        boolean botActive = BotPathRenderer.isBotActive();
        if (!hasPath && !botActive) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int screenW = g.guiWidth();
        int panelW = 158;
        int x = screenW - panelW - 4;
        int y = 4;

        // Calculate panel height dynamically based on content
        int contentY = y + 4;
        int lineH = 11;
        int sections = 0;

        // Count sections for height calculation
        sections++; // Title line always present
        String status = BotPathRenderer.getTargetDescription();
        if (!status.isEmpty()) sections++; // Status text
        BotPathRenderer.PathPoint dest = BotPathRenderer.getDestination();
        if (dest != null) sections += 2; // Dest + distance
        int eta = BotPathRenderer.getEta();
        if (eta >= 0) sections++; // ETA
        int mined = BotPathRenderer.getBlocksMined();
        int harvested = BotPathRenderer.getCropsHarvested();
        int placed = BotPathRenderer.getBlocksPlaced();
        if (mined > 0 || harvested > 0 || placed > 0) sections++; // Stats
        int pathSize = BotPathRenderer.getPathSize();
        if (pathSize > 0) sections++; // Progress bar (counts as a line)
        sections++; // Cancel hint always shown

        int panelH = sections * lineH + 8;

        // Background
        g.fill(x, y, x + panelW, y + panelH, BG);
        // Border
        g.fill(x, y, x + panelW, y + 1, BORDER);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, BORDER);
        g.fill(x, y, x + 1, y + panelH, BORDER);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, BORDER);

        // Accent top stripe (1px colored line under top border)
        g.fill(x + 1, y + 1, x + panelW - 1, y + 2, ACCENT);

        // Title bar
        String title;
        if (hasPath) {
            title = BotWorldRenderer.isRenderEnabled() ? "[Bot] Active" : "[Bot] Active";
        } else {
            title = "[Bot] Running";
        }
        g.drawString(mc.font, title, x + 4, contentY, ACCENT, true);

        // Path node count on the right
        if (pathSize > 0) {
            String nodeStr = pathSize + " nodes";
            int nodeW = mc.font.width(nodeStr);
            g.drawString(mc.font, nodeStr, x + panelW - nodeW - 4, contentY, LABEL, false);
        }
        contentY += lineH;

        // Process status text (most important info)
        if (!status.isEmpty()) {
            String displayStatus = status;
            if (mc.font.width(displayStatus) > panelW - 8) {
                displayStatus = displayStatus.substring(0, Math.min(displayStatus.length(), 22)) + "...";
            }
            g.drawString(mc.font, displayStatus, x + 4, contentY, TEXT, false);
            contentY += lineH;
        }

        // Destination
        if (dest != null) {
            String destStr = String.format("Dest: %d, %d, %d", (int) dest.x(), (int) dest.y(), (int) dest.z());
            g.drawString(mc.font, destStr, x + 4, contentY, TEXT, false);
            contentY += lineH;

            // Distance and direction
            double dx = dest.x() - mc.player.getX();
            double dz = dest.z() - mc.player.getZ();
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            g.drawString(mc.font, dist + "m away", x + 4, contentY, PATH_COLOR, false);

            // Direction arrow
            float playerYaw = mc.player.getYRot();
            double angle = Math.atan2(-dx, dz) * (180.0 / Math.PI);
            double relAngle = ((angle - playerYaw) % 360 + 360) % 360;
            String arrow = getDirectionArrow(relAngle);
            g.drawString(mc.font, arrow, x + 60, contentY, ACCENT, false);
            contentY += lineH;
        }

        // ETA
        if (eta >= 0) {
            String etaStr = eta > 60 ? (eta / 60) + "m " + (eta % 60) + "s" : eta + "s";
            g.drawString(mc.font, "ETA: ~" + etaStr, x + 4, contentY, WARNING, false);
            contentY += lineH;
        }

        // Stats
        if (mined > 0 || harvested > 0 || placed > 0) {
            StringBuilder stats = new StringBuilder();
            if (mined > 0) stats.append("Mined:").append(mined).append(" ");
            if (harvested > 0) stats.append("Farm:").append(harvested).append(" ");
            if (placed > 0) stats.append("Built:").append(placed);
            g.drawString(mc.font, stats.toString(), x + 4, contentY, LABEL, false);
            contentY += lineH;
        }

        // Progress bar
        if (pathSize > 0) {
            int barX = x + 4;
            int barY = contentY + 1;
            int barW = panelW - 8;
            int barH = 4;
            // Estimate progress from current path position
            int totalNodes = Math.max(pathSize, 1);
            int current = Math.max(0, totalNodes - BotPathRenderer.getPathSize());
            float progress = Math.min(1.0f, (float) current / totalNodes);
            g.fill(barX, barY, barX + barW, barY + barH, PROGRESS_BG);
            g.fill(barX, barY, barX + (int)(barW * progress), barY + barH, PROGRESS_FG);
            contentY += lineH;
        }

        // Cancel hint (always shown at the bottom)
        g.drawString(mc.font, "ESC / ` to cancel", x + 4, contentY, LABEL, false);
    }

    private static String getDirectionArrow(double relAngle) {
        if (relAngle < 22.5 || relAngle >= 337.5) return "^ Ahead";
        if (relAngle < 67.5) return "< F-Left";
        if (relAngle < 112.5) return "<< Left";
        if (relAngle < 157.5) return "v< B-Left";
        if (relAngle < 202.5) return "v Behind";
        if (relAngle < 247.5) return ">v B-Right";
        if (relAngle < 292.5) return ">> Right";
        return "^> F-Right";
    }
}
