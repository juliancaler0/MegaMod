package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.hud.network.TrackerSyncPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.List;

/**
 * Right-edge HUD showing active bounties and quests.
 */
public class QuestTrackerDisplay {

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "quest_tracker"),
            QuestTrackerDisplay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        List<TrackerSyncPayload.BountyInfo> bounties = TrackerSyncPayload.clientBounties;
        List<TrackerSyncPayload.QuestInfo> quests = TrackerSyncPayload.clientQuests;
        List<TrackerSyncPayload.ProgressQuestInfo> tracked = TrackerSyncPayload.clientTrackedQuests;

        if (bounties.isEmpty() && quests.isEmpty() && tracked.isEmpty()) return;

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        // Measure actual text widths to size panel dynamically
        int maxTextW = 0;
        if (!bounties.isEmpty()) {
            maxTextW = Math.max(maxTextW, mc.font.width("BOUNTIES"));
            for (TrackerSyncPayload.BountyInfo b : bounties) {
                maxTextW = Math.max(maxTextW, mc.font.width("- " + b.itemName() + " x" + b.quantity()));
            }
        }
        if (!quests.isEmpty()) {
            maxTextW = Math.max(maxTextW, mc.font.width("QUESTS"));
            for (TrackerSyncPayload.QuestInfo q : quests) {
                String diff = switch (q.difficulty()) {
                    case 0 -> "[N]"; case 1 -> "[H]"; case 2 -> "[NM]"; case 3 -> "[I]";
                    default -> "";
                };
                maxTextW = Math.max(maxTextW, mc.font.width("- " + q.title()) + 4 + mc.font.width(diff));
            }
        }
        if (!tracked.isEmpty()) {
            maxTextW = Math.max(maxTextW, mc.font.width("TRACKED"));
            for (TrackerSyncPayload.ProgressQuestInfo pq : tracked) {
                maxTextW = Math.max(maxTextW, mc.font.width("- " + pq.title()));
                maxTextW = Math.max(maxTextW, mc.font.width("  " + pq.progress() + "/" + pq.target()));
            }
        }

        int panelW = Math.min(Math.max(maxTextW + 12, 80), screenW / 3);
        int contentW = panelW - 8;

        // Calculate height
        int lines = 0;
        if (!bounties.isEmpty()) lines += 1 + bounties.size();
        if (!quests.isEmpty()) lines += 1 + quests.size();
        if (!tracked.isEmpty()) lines += 1 + tracked.size() * 2;
        int panelH = lines * 10 + 8;

        int y = 4;

        // Scale down if panel exceeds screen height
        float scale = 1.0f;
        if (panelH + y + 4 > screenH) {
            scale = Math.max(0.5f, (float)(screenH - y - 4) / (float) panelH);
        }

        int x = screenW - panelW - 4;

        if (scale < 1.0f) {
            // PoseStack scaling disabled — API changed in 1.21.11
            // Recalculate x in scaled space
            x = (int)(screenW / scale) - panelW - 4;
        }

        UIHelper.drawHudPanel(g, x, y, panelW, panelH);

        int ty = y + 4;
        int tx = x + 4;

        // Bounties
        if (!bounties.isEmpty()) {
            g.drawString(mc.font, "BOUNTIES", tx, ty, 0xFFFFAA00, false);
            ty += 10;
            for (TrackerSyncPayload.BountyInfo b : bounties) {
                String text = "- " + b.itemName() + " x" + b.quantity();
                if (mc.font.width(text) > contentW) {
                    text = mc.font.plainSubstrByWidth(text, contentW - mc.font.width("..")) + "..";
                }
                g.drawString(mc.font, text, tx + 2, ty, 0xFFCCCCCC, false);
                ty += 10;
            }
        }

        // Quests
        if (!quests.isEmpty()) {
            g.drawString(mc.font, "QUESTS", tx, ty, 0xFF44CCCC, false);
            ty += 10;
            for (TrackerSyncPayload.QuestInfo q : quests) {
                String diff = switch (q.difficulty()) {
                    case 0 -> "[N]"; case 1 -> "[H]"; case 2 -> "[NM]"; case 3 -> "[I]";
                    default -> "";
                };
                int diffW = mc.font.width(diff);
                int diffColor = switch (q.difficulty()) {
                    case 0 -> 0xFF55FF55;
                    case 1 -> 0xFF5555FF;
                    case 2 -> 0xFFAA44FF;
                    case 3 -> 0xFFFF4444;
                    default -> 0xFFCCCCCC;
                };
                String title = "- " + q.title();
                int titleSpace = contentW - diffW - 4;
                if (mc.font.width(title) > titleSpace) {
                    title = mc.font.plainSubstrByWidth(title, titleSpace - mc.font.width("..")) + "..";
                }
                g.drawString(mc.font, title, tx + 2, ty, 0xFFCCCCCC, false);
                g.drawString(mc.font, diff, tx + contentW - diffW, ty, diffColor, false);
                ty += 10;
            }
        }

        // Tracked quests (from quest app)
        if (!tracked.isEmpty()) {
            g.drawString(mc.font, "TRACKED", tx, ty, 0xFF58A6FF, false);
            ty += 10;
            for (TrackerSyncPayload.ProgressQuestInfo pq : tracked) {
                String title = "- " + pq.title();
                if (mc.font.width(title) > contentW) {
                    title = mc.font.plainSubstrByWidth(title, contentW - mc.font.width("..")) + "..";
                }
                g.drawString(mc.font, title, tx + 2, ty, 0xFFCCCCCC, false);
                ty += 10;
                String prog = "  " + pq.progress() + "/" + pq.target();
                g.drawString(mc.font, prog, tx + 2, ty, 0xFF888899, false);
                ty += 10;
            }
        }


    }
}
