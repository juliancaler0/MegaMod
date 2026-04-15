package com.ultra.megamod.feature.skills.client;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.skills.SkillBranch;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.locks.SkillLockDefinitions;
import com.ultra.megamod.feature.skills.network.SkillActionPayload;
import com.ultra.megamod.feature.skills.network.SkillSyncPayload;
import com.ultra.megamod.feature.skills.synergy.SynergyManager;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

/**
 * Skill Tree screen — clean tabbed per-tree column layout.
 *
 * Key design: all tree content is placed in world-space coordinates,
 * then uniformly transformed to screen-space via a single worldToScreen
 * conversion. Text visibility is gated on effective pixel size to prevent
 * overlap at any zoom level.
 */
public class SkillTreeScreen extends Screen {

    // ---- fixed UI layout ----
    private static final int TAB_H = 24, TAB_GAP = 2;
    private static final int XP_BAR_H = 14;
    private static final int FOOTER_H = 46;
    private static final int DETAIL_W = 180;

    // ---- world-space layout ----
    private static final int W_NODE_R = 18;         // node radius in world px
    private static final int W_TIER_GAP = 110;       // vertical gap between tiers
    private static final int W_BRANCH_HDR_Y = 14;    // branch header Y
    private static final int W_FIRST_NODE_Y = 50;    // tier-1 node center Y
    private static final int W_COL_MIN = 100;        // minimum column width

    // ---- colors ----
    private static final int BG = 0xFF0A0A14;
    private static final int C_BRIGHT = 0xFFFFF500;
    private static final int C_TEXT = 0xFFF0E8C0;
    private static final int C_DIM = 0xFFA89868;
    private static final int C_GOLD = 0xFFDAB420;
    private static final int C_LOCK = 0xFF555555;
    private static final int C_LOCK_BR = 0xFFCC3333;
    private static final int C_GREEN = 0xFF4BCC4B;

    // ---- colors (planning mode) ----
    private static final int C_PLAN = 0xFF4488FF;
    private static final int C_PLAN_BG = 0xFF182848;

    // ---- state ----
    private SkillTreeType selectedTree = SkillTreeType.COMBAT;
    private SkillNode hoveredNode = null;
    private SkillNode selectedNode = null;
    private final Set<String> lockedBranches = new HashSet<>();
    private long tick = 0;
    private int lockVersion = -1; // tracks last known unlock set size to avoid rebuilding every frame

    // ---- plan mode ----
    private boolean planMode = false;
    private final Set<String> plannedNodes = new HashSet<>();

    // ---- admin class branch toggle ----
    private static boolean showClassBranches = false;

    // ---- prestige confirmation ----
    private boolean showPrestigeConfirm = false;

    // ---- zoom & pan ----
    private float zoom = 1f;
    private float panX = 0, panY = 0;      // world-space pan offset
    private boolean dragging = false;
    private double dragSX, dragSY;           // screen-space drag start
    private float panSX, panSY;              // pan at drag start

    // ---- world layout cache ----
    private final Map<String, float[]> nodePos = new LinkedHashMap<>(); // id -> {wx, wy}
    private final List<SkillBranch> branches = new ArrayList<>();
    private float wWidth;                     // total world width

    public SkillTreeScreen() { super(Component.literal("Skill Web")); }

    @Override
    protected void init() { super.init(); rebuildLayout(); rebuildLocks(); }

    // ================================================================ layout

    private void rebuildLayout() {
        nodePos.clear(); branches.clear();
        for (SkillBranch b : SkillBranch.values())
            if (b.getTreeType() == selectedTree && !shouldHideClassBranch(b)) branches.add(b);
        int n = branches.size(); if (n == 0) return;
        float col = Math.max(W_COL_MIN, (float) treeW() / n);
        wWidth = col * n;
        for (int i = 0; i < n; i++) {
            SkillBranch br = branches.get(i);
            float cx = col * i + col / 2f;
            List<SkillNode> bNodes = new ArrayList<>();
            for (SkillNode nd : SkillTreeDefinitions.getNodesForTree(selectedTree))
                if (nd.branch() == br) bNodes.add(nd);
            bNodes.sort(Comparator.comparingInt(SkillNode::tier));
            for (SkillNode nd : bNodes)
                nodePos.put(nd.id(), new float[]{cx, W_FIRST_NODE_Y + (nd.tier() - 1) * W_TIER_GAP});
        }
    }

    private void rebuildLocks() {
        lockedBranches.clear();
        // Admins have no branch limit
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            String name = mc.player.getGameProfile().name();
            if (AdminSystem.isAdmin(name)) return;
        }
        Set<String> u = SkillSyncPayload.clientUnlockedNodes;
        for (SkillTreeType t : SkillTreeType.values()) {
            Set<String> sp = new HashSet<>();
            for (String id : u) { SkillNode nd = SkillTreeDefinitions.getNodeById(id);
                if (nd != null && nd.branch().getTreeType() == t && !shouldHideClassBranch(nd.branch())) sp.add(nd.branch().name()); }
            if (sp.size() >= 2)
                for (SkillBranch b : SkillBranch.values())
                    if (b.getTreeType() == t && !shouldHideClassBranch(b) && !sp.contains(b.name())) lockedBranches.add(b.name());
        }
    }

    // ================================================================ metrics

    private int tabY()    { return 4; }
    private int xpY()     { return tabY() + TAB_H + 4; }
    private int treeT()   { return xpY() + XP_BAR_H + 4; }
    private int treeB()   { return height - FOOTER_H; }
    private int treeL()   { return 6; }
    private int treeR()   { return width - DETAIL_W - 8; }
    private int treeW()   { return treeR() - treeL(); }
    private int detailL() { return width - DETAIL_W - 4; }

    // World → Screen (consistent for both axes)
    private int ws_x(float wx) { return (int)(treeL() + treeW() / 2f + (wx - wWidth / 2f + panX) * zoom); }
    private int ws_y(float wy) { return (int)(treeT() + 8 + (wy + panY) * zoom); }

    // Effective pixel size of a world-space dimension
    private float eff(float worldPx) { return worldPx * zoom; }

    // ================================================================ render

    @Override
    public void tick() {
        super.tick();
        tick++;
        // Only rebuild locks when the unlocked node set actually changes
        int currentVersion = SkillSyncPayload.clientUnlockedNodes.size();
        if (currentVersion != lockVersion) {
            lockVersion = currentVersion;
            rebuildLocks();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        float anim = tick + pt;

        g.fill(0, 0, width, height, BG);
        UIHelper.drawScreenBg(g, 0, 0, width, height);

        drawTabs(g, mouseX, mouseY);
        drawXpBar(g);

        // Tree area (clipped)
        g.enableScissor(treeL() - 2, treeT(), treeR() + 2, treeB());
        UIHelper.drawInsetPanel(g, treeL() - 2, treeT(), treeW() + 4, treeB() - treeT());
        drawGrid(g);
        drawBranchHeaders(g);
        drawConnections(g);
        hoveredNode = null;
        drawNodes(g, mouseX, mouseY, anim);
        g.disableScissor();

        drawDetailPanel(g, mouseX, mouseY);
        drawFooter(g, mouseX, mouseY);
        drawZoomCtrl(g, mouseX, mouseY);

        super.render(g, mouseX, mouseY, pt);

        if (hoveredNode != null) {
            Set<String> u = SkillSyncPayload.clientUnlockedNodes;
            int pts = SkillSyncPayload.clientTreePoints.getOrDefault(selectedTree, 0);
            boolean unl = u.contains(hoveredNode.id()), bl = lockedBranches.contains(hoveredNode.branch().name());
            SkillNodeWidget.drawTooltip(g, mouseX, mouseY, hoveredNode, unl,
                    !unl && !bl && canUnlock(hoveredNode, u, pts), pts, bl);
        }

        // Prestige confirmation dialog overlay
        if (showPrestigeConfirm) {
            g.fill(0, 0, width, height, 0xAA000000);
            int dw = 240, dh = 130;
            int dx = (width - dw) / 2, dy = (height - dh) / 2;
            UIHelper.drawPanel(g, dx, dy, dw, dh);
            int pPres = SkillSyncPayload.clientPrestige.getOrDefault(selectedTree, 0);
            int cost = 100 * (pPres + 1);
            int newPres = pPres + 1;
            int bonusPct = newPres * 5;
            g.drawString(font, "\u2605 Prestige " + selectedTree.getDisplayName() + "?", dx + dw / 2 - font.width("\u2605 Prestige " + selectedTree.getDisplayName() + "?") / 2, dy + 10, C_GOLD, false);
            g.drawString(font, "Level resets to 0, all nodes reset", dx + 12, dy + 28, C_TEXT, false);
            g.drawString(font, "Cost: " + cost + " MegaCoins", dx + 12, dy + 42, 0xFFCC4444, false);
            g.drawString(font, "Permanent +" + bonusPct + "% XP bonus (P" + newPres + ")", dx + 12, dy + 56, C_GREEN, false);
            g.drawString(font, "+25 Marks of Mastery", dx + 12, dy + 70, 0xFFBB99FF, false);
            // Confirm button
            int cbW = 80, cbH = 18, cbX = dx + dw / 2 - cbW - 6, cbY = dy + dh - 28;
            boolean cbH2 = mouseX >= cbX && mouseX < cbX + cbW && mouseY >= cbY && mouseY < cbY + cbH;
            UIHelper.drawButton(g, cbX, cbY, cbW, cbH, cbH2);
            g.drawString(font, "Confirm", cbX + (cbW - font.width("Confirm")) / 2, cbY + (cbH - 9) / 2, cbH2 ? C_BRIGHT : C_GREEN, false);
            // Cancel button
            int ccX = dx + dw / 2 + 6;
            boolean ccHov = mouseX >= ccX && mouseX < ccX + cbW && mouseY >= cbY && mouseY < cbY + cbH;
            UIHelper.drawButton(g, ccX, cbY, cbW, cbH, ccHov);
            g.drawString(font, "Cancel", ccX + (cbW - font.width("Cancel")) / 2, cbY + (cbH - 9) / 2, ccHov ? C_BRIGHT : 0xFFCC4444, false);
        }
    }

    // ---- grid ----
    private void drawGrid(GuiGraphics g) {
        int sp = Math.max(16, (int)(40 * zoom));
        int c = 0x12FFFFFF;
        int x0 = ws_x(0), y0 = ws_y(0);
        for (int x = treeL() + (x0 - treeL()) % sp; x < treeR(); x += sp)
            for (int y = treeT() + (y0 - treeT()) % sp; y < treeB(); y += sp)
                g.fill(x, y, x + 1, y + 1, c);
    }

    // ---- tabs ----
    private void drawTabs(GuiGraphics g, int mx, int my) {
        // Class identity header retired with the class-selection system.

        SkillTreeType[] ts = SkillTreeType.values();
        int tw = (width - 12 - (ts.length - 1) * TAB_GAP) / ts.length;
        for (int i = 0; i < ts.length; i++) {
            SkillTreeType t = ts[i]; int tx = 6 + i * (tw + TAB_GAP);
            boolean sel = t == selectedTree, hov = mx >= tx && mx < tx + tw && my >= tabY() && my < tabY() + TAB_H;
            // Class-tree highlight retired with the class-selection system.
            UIHelper.drawTab(g, tx, tabY(), tw, TAB_H, sel, t.getColor());
            int lv = SkillSyncPayload.clientLevels.getOrDefault(t, 0);
            String lab = t.getDisplayName() + " [" + lv + "]";
            int lw = font.width(lab);
            if (lw > tw - 8) { lab = t.getAbbreviation() + " " + lv; lw = font.width(lab); }
            int tc = sel ? C_BRIGHT : hov ? C_TEXT : C_DIM;
            g.drawString(font, lab, tx + (tw - lw) / 2, tabY() + (TAB_H - 9) / 2, tc, false);
            // Mastery icon: gold star if level >= 50
            if (lv >= 50) {
                String star = "\u2605";
                g.drawString(font, star, tx + (tw - lw) / 2 + lw + 2, tabY() + (TAB_H - 9) / 2, C_GOLD, false);
            }
            g.drawString(font, String.valueOf(i + 1), tx + tw - font.width(String.valueOf(i + 1)) - 3, tabY() + 2, 0x44FFFFFF, false);
        }
        // Prestige display — show per-tree prestige from synced data
        int treePrestige = SkillSyncPayload.clientPrestige.getOrDefault(selectedTree, 0);
        int treeLv = SkillSyncPayload.clientLevels.getOrDefault(selectedTree, 0);
        if (treePrestige > 0) {
            int bonusPct = treePrestige * 5;
            String pStr = "\u2605 Prestige " + treePrestige + " (+" + bonusPct + "% bonus)";
            int pW = font.width(pStr);
            g.drawString(font, pStr, width - pW - DETAIL_W - 14, tabY() + TAB_H + 1, C_GOLD, false);
        } else if (treeLv >= 50) {
            String pStr = "\u2606 Prestige available!";
            int pW = font.width(pStr);
            g.drawString(font, pStr, width - pW - DETAIL_W - 14, tabY() + TAB_H + 1, C_GOLD, false);
        } else {
            String pStr = "Prestige at Lv50";
            int pW = font.width(pStr);
            g.drawString(font, pStr, width - pW - DETAIL_W - 14, tabY() + TAB_H + 1, 0xFF555555, false);
        }
    }

    // ---- xp bar ----
    private void drawXpBar(GuiGraphics g) {
        int bx = 6, bw = width - 12;
        int lv = SkillSyncPayload.clientLevels.getOrDefault(selectedTree, 0);
        int xp = SkillSyncPayload.clientXp.getOrDefault(selectedTree, 0);
        int need = SkillTreeType.xpForLevel(lv);
        UIHelper.drawXpBar(g, font, bx, xpY(), bw, XP_BAR_H, need > 0 ? (float) xp / need : 0, lv);
        g.drawString(font, xp + "/" + need + " XP", bx + bw - font.width(xp + "/" + need + " XP") - 4, xpY() + (XP_BAR_H - 9) / 2, C_TEXT, false);
        g.drawString(font, selectedTree.getDisplayName() + " Lv" + lv, bx + 4, xpY() + (XP_BAR_H - 9) / 2, selectedTree.getColor(), false);
    }

    // ---- branch headers ----
    private void drawBranchHeaders(GuiGraphics g) {
        int n = branches.size(); if (n == 0) return;
        float col = wWidth / n;
        for (int i = 0; i < n; i++) {
            SkillBranch b = branches.get(i);
            boolean lk = lockedBranches.contains(b.name());
            float wcx = col * i + col / 2f;
            int sx = ws_x(wcx), sy = ws_y(W_BRANCH_HDR_Y);
            int color = lk ? C_LOCK_BR : selectedTree.getColor();
            String name = b.getDisplayName();
            int nw = font.width(name);

            // Only draw if text fits visually (won't overlap neighbors)
            float effCol = eff(col);
            if (effCol >= nw + 12) {
                g.fill(sx - nw / 2 - 6, sy - 6, sx + nw / 2 + 6, sy + 7, 0xDD000000);
                g.fill(sx - nw / 2 - 6, sy - 6, sx + nw / 2 + 6, sy - 5, color);
                g.drawString(font, name, sx - nw / 2, sy - 4, color, false);
                if (lk) {
                    String ls = "(Locked)"; int lsw = font.width(ls);
                    if (effCol >= lsw + 8) g.drawString(font, ls, sx - lsw / 2, sy + 8, 0xFFAA4444, false);
                }
            } else if (effCol >= 20) {
                // Abbreviated: first 3 chars
                String abbr = name.length() > 3 ? name.substring(0, 3) : name;
                int aw = font.width(abbr);
                g.fill(sx - aw / 2 - 3, sy - 5, sx + aw / 2 + 3, sy + 6, 0xCC000000);
                g.drawString(font, abbr, sx - aw / 2, sy - 4, color, false);
            }

            // Vertical guide line
            int ly1 = ws_y(W_FIRST_NODE_Y - 12), ly2 = ws_y(W_FIRST_NODE_Y + 4 * W_TIER_GAP + 12);
            if (ly2 > ly1) g.fill(sx, ly1, sx + 1, Math.min(ly2, treeB()), lk ? 0x10FFFFFF : 0x18FFFFFF);
        }
    }

    // ---- connections ----
    private void drawConnections(GuiGraphics g) {
        Set<String> u = SkillSyncPayload.clientUnlockedNodes;
        for (SkillNode nd : SkillTreeDefinitions.getNodesForTree(selectedTree)) {
            if (shouldHideClassBranch(nd.branch())) continue;
            float[] to = nodePos.get(nd.id()); if (to == null) continue;
            boolean lk = lockedBranches.contains(nd.branch().name());
            int tx = ws_x(to[0]), ty = ws_y(to[1]);
            for (String pid : nd.prerequisites()) {
                float[] fr = nodePos.get(pid); if (fr == null) continue;
                int fx = ws_x(fr[0]), fy = ws_y(fr[1]);
                boolean both = u.contains(pid) && u.contains(nd.id());
                int c = lk ? 0xFF333344 : both ? selectedTree.getColor() : u.contains(pid) ? C_GOLD : 0xFF444455;
                drawThickLine(g, fx, fy, tx, ty, c, both ? 2 : 1);
            }
        }
    }

    // ---- nodes ----
    private void drawNodes(GuiGraphics g, int mx, int my, float anim) {
        Set<String> u = SkillSyncPayload.clientUnlockedNodes;
        int pts = SkillSyncPayload.clientTreePoints.getOrDefault(selectedTree, 0);
        float effR = eff(W_NODE_R);                // effective node radius in screen px
        float effCol = branches.size() > 0 ? eff(wWidth / branches.size()) : 200;

        for (SkillNode nd : SkillTreeDefinitions.getNodesForTree(selectedTree)) {
            if (shouldHideClassBranch(nd.branch())) continue;
            float[] wp = nodePos.get(nd.id()); if (wp == null) continue;
            int sx = ws_x(wp[0]), sy = ws_y(wp[1]);
            int r = Math.max(4, (int) effR);

            boolean unl = u.contains(nd.id()), bl = lockedBranches.contains(nd.branch().name());
            boolean av = !unl && !bl && canUnlock(nd, u, pts);
            double dx = mx - sx, dy = my - sy;
            boolean hov = dx * dx + dy * dy <= (double) r * r;
            if (hov) hoveredNode = nd;

            // ---- draw node circle ----
            int bgC, bdC, txC;
            if (bl)       { bgC = 0xFF191930; bdC = 0xFF664466; txC = 0xFF555555; }
            else if (unl) { bgC = 0xFF3C2808; bdC = nd.branch().getTreeType().getColor(); txC = C_BRIGHT; }
            else if (av)  { float p = (float)(Math.sin(anim * 0.1) * 0.5 + 0.5); bgC = 0xFF222238; bdC = lerpC(0xFFCCCCCC, 0xFFFFFFFF, p); txC = C_TEXT; }
            else          { bgC = 0xFF161622; bdC = 0xFF555555; txC = 0xFF666666; }

            // Plan mode: blue highlight for planned nodes
            boolean planned = planMode && plannedNodes.contains(nd.id());
            if (planned && !unl) {
                bgC = C_PLAN_BG;
                bdC = C_PLAN;
                txC = C_PLAN;
            }

            if (unl && !bl) { int gc = (0x30 << 24) | (bdC & 0xFFFFFF); SkillNodeWidget.drawFilledCircle(g, sx, sy, r + 4, gc); }
            if (planned && !unl) { SkillNodeWidget.drawFilledCircle(g, sx, sy, r + 4, 0x304488FF); }
            if (hov) SkillNodeWidget.drawFilledCircle(g, sx, sy, r + 3, 0x30FFFFFF);
            SkillNodeWidget.drawFilledCircle(g, sx, sy, r, bgC);
            drawRing(g, sx, sy, r, bdC);
            if (unl && !bl) drawRing(g, sx, sy, Math.max(2, r - 2), C_GOLD);
            if (planned && !unl) drawRing(g, sx, sy, Math.max(2, r - 2), C_PLAN);

            // ---- text inside node (initials) — only if node is big enough ----
            if (r >= 10) {
                String ini = nd.displayName().length() >= 2 ? nd.displayName().substring(0, 2).toUpperCase() : nd.displayName().toUpperCase();
                int iw = font.width(ini);
                g.drawString(font, ini, sx - iw / 2, sy - 4, bl ? C_LOCK_BR : txC, false);
            }

            // ---- name below node — only if there's room ----
            if (effCol >= font.width(nd.displayName()) + 8 && effR >= 8) {
                String nm = nd.displayName();
                int nw = font.width(nm);
                g.drawString(font, nm, sx - nw / 2, sy + r + 4, unl ? C_BRIGHT : bl ? C_LOCK : C_DIM, false);
                // Tier
                String tier = roman(nd.tier()); int tw = font.width(tier);
                g.drawString(font, tier, sx - tw / 2, sy + r + 15, 0xFF777060, false);
            } else if (effR >= 6) {
                // Just show tier when name won't fit
                String tier = roman(nd.tier()); int tw = font.width(tier);
                g.drawString(font, tier, sx - tw / 2, sy + r + 3, 0xFF777060, false);
            }

            // Cost badge for all non-unlocked nodes (helps path planning)
            if (!unl && r >= 8) {
                String cs = String.valueOf(nd.cost()) + "pt";
                int cw = font.width(cs);
                int badgeBg = av ? 0xDD1A3A1A : bl ? 0xDD3A1A1A : 0xDD1A1A2A;
                int badgeText = av ? C_BRIGHT : bl ? C_LOCK_BR : C_DIM;
                g.fill(sx + r - 2, sy - r - 2, sx + r + cw + 3, sy - r + 9, badgeBg);
                g.fill(sx + r - 2, sy - r - 2, sx + r + cw + 3, sy - r - 1, av ? C_GREEN : 0xFF444444);
                g.drawString(font, cs, sx + r, sy - r - 1, badgeText, false);
            }
            // Checkmark for unlocked nodes
            if (unl && r >= 8) {
                g.drawString(font, "\u2714", sx + r - 2, sy - r - 2, C_GREEN, false);
            }
        }
    }

    // ---- detail panel ----
    private void drawDetailPanel(GuiGraphics g, int mx, int my) {
        int px = detailL(), py = treeT(), pw = DETAIL_W, ph = treeB() - py;
        UIHelper.drawPanel(g, px, py, pw, ph);
        SkillNode show = hoveredNode != null ? hoveredNode : selectedNode;
        if (show == null) { drawSummary(g, px, py, pw); return; }

        Set<String> u = SkillSyncPayload.clientUnlockedNodes;
        int pts = SkillSyncPayload.clientTreePoints.getOrDefault(selectedTree, 0);
        boolean unl = u.contains(show.id()), bl = lockedBranches.contains(show.branch().name());
        boolean av = !unl && !bl && canUnlock(show, u, pts);
        int ix = px + 8, iw = pw - 16, dy = py + 10;

        UIHelper.drawCenteredTitle(g, font, show.displayName(), px + pw / 2, dy); dy += 14;
        String sub = show.branch().getDisplayName() + " - Tier " + roman(show.tier());
        g.drawString(font, sub, px + (pw - font.width(sub)) / 2, dy, selectedTree.getColor(), false); dy += 14;
        UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 8;
        dy = wrap(g, show.description(), ix, dy, iw, C_TEXT); dy += 6;

        if (!show.bonuses().isEmpty()) {
            UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 6;
            g.drawString(font, "Bonuses:", ix, dy, C_GOLD, false); dy += 12;
            for (var e : show.bonuses().entrySet()) {
                g.drawString(font, "+" + fmtV(e.getValue()) + " " + fmtN(e.getKey()), ix + 4, dy, C_GREEN, false); dy += 11;
            }
        }
        dy += 4;
        if (!show.prerequisites().isEmpty()) {
            g.drawString(font, "Requires:", ix, dy, 0xFFCC8844, false); dy += 11;
            for (String pid : show.prerequisites()) {
                SkillNode pn = SkillTreeDefinitions.getNodeById(pid); boolean met = u.contains(pid);
                g.drawString(font, (met ? "  > " : "  x ") + (pn != null ? pn.displayName() : pid), ix, dy, met ? C_GREEN : 0xFFCC4444, false); dy += 11;
            }
        }
        dy += 6; UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 8;

        // Status + cost display
        g.drawString(font, "Cost: " + show.cost() + " pts", ix, dy, C_GOLD, false);
        dy += 12;
        // Total path cost (sum of all locked prerequisites + this node)
        if (!unl) {
            int pathCost = calcPathCost(show, u);
            if (pathCost > show.cost()) {
                g.drawString(font, "Total path: " + pathCost + " pts", ix, dy, 0xFFCC8844, false);
                dy += 12;
            }
        }
        if (bl) {
            g.drawString(font, "\u26D4 BRANCH LOCKED", ix, dy, C_LOCK_BR, false); dy += 11;
            wrap(g, "Specialized in 2 branches. Respec to change.", ix, dy, iw, 0xFF999999);
        } else if (unl) {
            g.drawString(font, "\u2714 UNLOCKED", ix, dy, C_GREEN, false);
        } else if (av) {
            g.drawString(font, "\u25B6 AVAILABLE - Click to unlock", ix, dy, C_GREEN, false); dy += 12;
            g.drawString(font, "Points: " + pts + " / " + show.cost() + " needed", ix, dy, pts >= show.cost() ? C_GREEN : 0xFFCC4444, false);
            dy += 12;
        } else {
            g.drawString(font, "\u274C LOCKED", ix, dy, C_LOCK, false); dy += 11;
            if (pts < show.cost())
                g.drawString(font, "Need " + show.cost() + " pts (have " + pts + ")", ix, dy, 0xFFCC4444, false);
            else
                g.drawString(font, "Unlock prerequisites first", ix, dy, 0xFFCC8844, false);
            dy += 12;
        }

        // --- Item Locks Info (for tier 3+ nodes) ---
        if (show.tier() >= 3) {
            SkillBranch showBranch = show.branch();
            List<String> unlockedItems = new ArrayList<>();
            List<String> unlockedEnchants = new ArrayList<>();

            for (SkillLockDefinitions.UseLock lock : SkillLockDefinitions.USE_LOCKS) {
                if (lock.branchA() == showBranch || lock.branchB() == showBranch) {
                    unlockedItems.add(lock.category());
                }
            }
            for (SkillLockDefinitions.EnchantLock lock : SkillLockDefinitions.ENCHANT_LOCKS) {
                if (lock.branchA() == showBranch || lock.branchB() == showBranch) {
                    String enchName = fmtN(lock.enchantId()) + " " + roman(lock.minLockedLevel());
                    if (lock.exclusive()) enchName += " (exclusive)";
                    unlockedEnchants.add(enchName);
                }
            }

            if (!unlockedItems.isEmpty() || !unlockedEnchants.isEmpty()) {
                dy += 4; UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 6;
                g.drawString(font, "--- Unlocks ---", ix, dy, C_GOLD, false); dy += 12;
                if (!unlockedItems.isEmpty()) {
                    StringBuilder itemLine = new StringBuilder();
                    for (int i = 0; i < unlockedItems.size(); i++) {
                        if (i > 0) itemLine.append(", ");
                        itemLine.append(unlockedItems.get(i));
                    }
                    dy = wrap(g, itemLine.toString(), ix, dy, iw, C_TEXT); dy += 2;
                }
                for (String ench : unlockedEnchants) {
                    g.drawString(font, "\u2728 " + ench, ix + 2, dy, 0xFFBB99FF, false); dy += 11;
                }
            }
        }
    }

    private void drawSummary(GuiGraphics g, int px, int py, int pw) {
        int ix = px + 8, iw = pw - 16, dy = py + 10;
        UIHelper.drawCenteredTitle(g, font, selectedTree.getDisplayName(), px + pw / 2, dy); dy += 16;
        dy = wrap(g, selectedTree.getDescription(), ix, dy, iw, C_TEXT); dy += 6;
        UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 8;
        Set<String> u = SkillSyncPayload.clientUnlockedNodes;
        for (SkillBranch b : branches) {
            boolean lk = lockedBranches.contains(b.name());
            g.drawString(font, b.getDisplayName(), ix, dy, lk ? C_LOCK_BR : selectedTree.getColor(), false); dy += 10;
            int tot = 0, uc = 0;
            for (SkillNode nd : SkillTreeDefinitions.getNodesForTree(selectedTree))
                if (nd.branch() == b) { tot++; if (u.contains(nd.id())) uc++; }
            g.drawString(font, "  " + uc + "/" + tot, ix, dy, C_DIM, false); dy += 12;
            UIHelper.drawProgressBar(g, ix + 4, dy, iw - 8, 6, tot > 0 ? (float) uc / tot : 0, lk ? 0xFF883333 : selectedTree.getColor()); dy += 14;
        }

        // --- Active Synergies ---
        Set<String> activeSyn = SkillSyncPayload.clientActiveSynergies;
        List<SynergyManager.Synergy> allSynergies = SynergyManager.getAllSynergies();
        dy += 2; UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 6;
        g.drawString(font, "Active Synergies", ix, dy, C_GOLD, false); dy += 12;

        boolean hasActive = false;
        for (SynergyManager.Synergy syn : allSynergies) {
            if (activeSyn.contains(syn.id())) {
                hasActive = true;
                g.drawString(font, "\u2726 " + syn.displayName(), ix + 4, dy, 0xFFFFFF44, false); dy += 10;
                dy = wrap(g, syn.description(), ix + 8, dy, iw - 12, C_DIM); dy += 2;
                String branchPair = syn.branch1().getDisplayName() + " + " + syn.branch2().getDisplayName();
                g.drawString(font, "  " + branchPair, ix + 8, dy, 0xFF666655, false); dy += 12;
            }
        }
        if (!hasActive) {
            g.drawString(font, "(none yet)", ix + 4, dy, C_DIM, false); dy += 12;
        }

        // --- Inactive Synergies (grayed out) ---
        boolean hasInactive = false;
        for (SynergyManager.Synergy syn : allSynergies) {
            if (!activeSyn.contains(syn.id())) {
                if (!hasInactive) {
                    dy += 2;
                    g.drawString(font, "Available:", ix, dy, 0xFF666666, false); dy += 11;
                    hasInactive = true;
                }
                g.drawString(font, "\u2726 " + syn.displayName(), ix + 4, dy, 0xFF555555, false); dy += 10;
                String req = "Requires: " + syn.branch1().getDisplayName() + " + " + syn.branch2().getDisplayName();
                dy = wrap(g, req, ix + 8, dy, iw - 12, 0xFF444444); dy += 4;
            }
        }

        // Show projected bonuses in plan mode
        if (planMode && !plannedNodes.isEmpty()) {
            dy += 4; UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 6;
            g.drawString(font, "Plan Preview:", ix, dy, C_PLAN, false); dy += 12;
            // Calculate projected stat totals
            Map<String, Double> projected = new LinkedHashMap<>();
            for (String nid : plannedNodes) {
                SkillNode nd = SkillTreeDefinitions.getNodeById(nid);
                if (nd != null) {
                    for (Map.Entry<String, Double> e : nd.bonuses().entrySet()) {
                        projected.merge(e.getKey(), e.getValue(), Double::sum);
                    }
                }
            }
            for (Map.Entry<String, Double> e : projected.entrySet()) {
                g.drawString(font, "+" + fmtV(e.getValue()) + " " + fmtN(e.getKey()), ix + 4, dy, C_PLAN, false);
                dy += 11;
            }
            int planCost = 0;
            for (String nid : plannedNodes) {
                SkillNode nd = SkillTreeDefinitions.getNodeById(nid);
                if (nd != null) planCost += nd.cost();
            }
            dy += 4;
            g.drawString(font, "Total cost: " + planCost + " pts", ix, dy, C_GOLD, false);
        } else {
            dy += 4; UIHelper.drawHorizontalDivider(g, ix, dy, iw); dy += 8;
            g.drawString(font, "Hover a node for info", ix, dy, C_DIM, false); dy += 12;
            g.drawString(font, "[1]-[5] switch trees", ix, dy, C_DIM, false); dy += 12;
            g.drawString(font, "Scroll=zoom Drag=pan", ix, dy, C_DIM, false);
        }
    }

    private void drawFooter(GuiGraphics g, int mx, int my) {
        int fy = height - FOOTER_H;
        UIHelper.drawPanel(g, 0, fy, width, FOOTER_H);
        int pts = SkillSyncPayload.clientTreePoints.getOrDefault(selectedTree, 0);

        // === ROW 1 (top): Info text ===
        int row1Y = fy + 4;

        // Points
        String ptStr = "\u2726 " + pts + " " + selectedTree.getDisplayName() + " Points";
        g.drawString(font, ptStr, 12, row1Y, pts > 0 ? C_BRIGHT : C_DIM, false);

        // Node count (centered) — exclude class branches (unless admin toggle)
        int treeTotal = 0, treeUnlocked = 0;
        for (SkillNode nd : SkillTreeDefinitions.getNodesForTree(selectedTree)) {
            if (shouldHideClassBranch(nd.branch())) continue;
            treeTotal++;
            if (SkillSyncPayload.clientUnlockedNodes.contains(nd.id())) treeUnlocked++;
        }
        String ns = treeUnlocked + "/" + treeTotal + " nodes";
        g.drawString(font, ns, width / 2 - font.width(ns) / 2, row1Y, selectedTree.getColor(), false);

        // Mastery / plan info (right-aligned on row 1)
        if (planMode && !plannedNodes.isEmpty()) {
            int planCost = 0;
            for (String nid : plannedNodes) {
                SkillNode nd = SkillTreeDefinitions.getNodeById(nid);
                if (nd != null) planCost += nd.cost();
            }
            String planInfo = "Plan: " + plannedNodes.size() + " nodes, " + planCost + " pts";
            g.drawString(font, planInfo, width - font.width(planInfo) - 12, row1Y, C_PLAN, false);
        } else if (pts > 0) {
            String masteryStat = switch (selectedTree) {
                case COMBAT -> "Atk Dmg";
                case MINING -> "Mine Spd";
                case FARMING -> "Farm XP";
                case ARCANE -> "Ability Pwr";
                case SURVIVAL -> "Move Spd";
            };
            int effectivePts = Math.min(pts, 10);
            String mStr = "Mastery: " + String.format("+%.1f", effectivePts * 0.5) + " " + masteryStat;
            g.drawString(font, mStr, width - font.width(mStr) - 12, row1Y, 0xFFBB99FF, false);
        }

        // === ROW 2 (bottom): Buttons, left to right with proper spacing ===
        int row2Y = fy + 22;
        int btnH = 18;
        int btnGap = 6;
        int btnX = 12;

        // Plan Mode toggle
        int pmW = 74;
        boolean pmHov = mx >= btnX && mx < btnX + pmW && my >= row2Y && my < row2Y + btnH;
        UIHelper.drawButton(g, btnX, row2Y, pmW, btnH, pmHov);
        String pmLabel = planMode ? "Exit Plan" : "Plan Mode";
        int pmColor = planMode ? C_PLAN : (pmHov ? C_TEXT : C_DIM);
        g.drawString(font, pmLabel, btnX + (pmW - font.width(pmLabel)) / 2, row2Y + (btnH - 9) / 2, pmColor, false);
        btnX += pmW + btnGap;

        // Apply + Clear (only in plan mode)
        if (planMode && !plannedNodes.isEmpty()) {
            int apW = 54;
            boolean apHov = mx >= btnX && mx < btnX + apW && my >= row2Y && my < row2Y + btnH;
            UIHelper.drawButton(g, btnX, row2Y, apW, btnH, apHov);
            g.drawString(font, "Apply", btnX + (apW - font.width("Apply")) / 2, row2Y + (btnH - 9) / 2, apHov ? C_GREEN : C_DIM, false);
            btnX += apW + btnGap;

            int clW = 48;
            boolean clHov = mx >= btnX && mx < btnX + clW && my >= row2Y && my < row2Y + btnH;
            UIHelper.drawButton(g, btnX, row2Y, clW, btnH, clHov);
            g.drawString(font, "Clear", btnX + (clW - font.width("Clear")) / 2, row2Y + (btnH - 9) / 2, clHov ? 0xFFCC4444 : C_DIM, false);
            btnX += clW + btnGap;
        }

        // Admin toggle: Show Class Branches (only visible to admins)
        if (isLocalPlayerAdmin()) {
            int cbW = 108;
            boolean cbHov = mx >= btnX && mx < btnX + cbW && my >= row2Y && my < row2Y + btnH;
            UIHelper.drawButton(g, btnX, row2Y, cbW, btnH, cbHov);
            String cbLabel = showClassBranches ? "Hide Classes" : "Show Classes";
            int cbColor = showClassBranches ? 0xFFFF8844 : (cbHov ? C_TEXT : C_DIM);
            g.drawString(font, cbLabel, btnX + (cbW - font.width(cbLabel)) / 2, row2Y + (btnH - 9) / 2, cbColor, false);
            btnX += cbW + btnGap;
        }

        // Right-aligned buttons: Respec and Prestige
        int rw = 84;
        int rx = width - rw - 12;
        boolean rH = mx >= rx && mx < rx + rw && my >= row2Y && my < row2Y + btnH;
        UIHelper.drawButton(g, rx, row2Y, rw, btnH, rH);
        String rLabel = "\u21BB Respec (50MC)";
        g.drawString(font, rLabel, rx + (rw - font.width(rLabel)) / 2, row2Y + (btnH - 9) / 2, rH ? C_BRIGHT : 0xFFCC4444, false);

        // Prestige button (only when tree level >= 50 and prestige < 5)
        int pLv = SkillSyncPayload.clientLevels.getOrDefault(selectedTree, 0);
        int pPres = SkillSyncPayload.clientPrestige.getOrDefault(selectedTree, 0);
        if (pLv >= 50 && pPres < 5) {
            int prW = 94;
            int prX = rx - prW - btnGap;
            boolean prHov = mx >= prX && mx < prX + prW && my >= row2Y && my < row2Y + btnH;
            UIHelper.drawButton(g, prX, row2Y, prW, btnH, prHov);
            int cost = 100 * (pPres + 1);
            String prLabel = "\u2605 Prestige (" + cost + "MC)";
            g.drawString(font, prLabel, prX + (prW - font.width(prLabel)) / 2, row2Y + (btnH - 9) / 2, prHov ? C_BRIGHT : C_GOLD, false);
        }
    }

    private void drawZoomCtrl(GuiGraphics g, int mx, int my) {
        int sz = 18, bx = treeR() - sz - 4, by = treeB() - sz * 3 - 16;
        // Reset view button
        boolean hr = mx >= bx && mx < bx + sz && my >= by && my < by + sz;
        UIHelper.drawIconButton(g, font, bx, by, sz, "\u21BA", hr ? C_BRIGHT : C_TEXT, hr);
        // Zoom +
        int by2 = by + sz + 4;
        boolean hp = mx >= bx && mx < bx + sz && my >= by2 && my < by2 + sz;
        UIHelper.drawIconButton(g, font, bx, by2, sz, "+", hp ? C_BRIGHT : C_TEXT, hp);
        // Zoom -
        int by3 = by2 + sz + 4;
        boolean hm = mx >= bx && mx < bx + sz && my >= by3 && my < by3 + sz;
        UIHelper.drawIconButton(g, font, bx, by3, sz, "-", hm ? C_BRIGHT : C_TEXT, hm);
        String zs = (int)(zoom * 100) + "%";
        g.drawString(font, zs, bx + (sz - font.width(zs)) / 2, by3 + sz + 3, C_DIM, false);
    }

    // ================================================================ input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        double mx = event.x(), my = event.y();
        if (event.button() != 0) return super.mouseClicked(event, consumed);

        // Prestige confirmation dialog click handling
        if (showPrestigeConfirm) {
            int dw = 240, dh = 130;
            int dx = (width - dw) / 2, dy = (height - dh) / 2;
            int cbW = 80, cbH = 18, cbX = dx + dw / 2 - cbW - 6, cbY = dy + dh - 28;
            int ccX = dx + dw / 2 + 6;
            if (mx >= cbX && mx < cbX + cbW && my >= cbY && my < cbY + cbH) {
                // Confirm prestige
                ClientPacketDistributor.sendToServer((CustomPacketPayload) new SkillActionPayload("prestige", selectedTree.name()), new CustomPacketPayload[0]);
                showPrestigeConfirm = false;
                return true;
            }
            if (mx >= ccX && mx < ccX + cbW && my >= cbY && my < cbY + cbH) {
                // Cancel prestige
                showPrestigeConfirm = false;
                return true;
            }
            // Click anywhere else on the overlay = cancel
            showPrestigeConfirm = false;
            return true;
        }

        if (my >= tabY() && my < tabY() + TAB_H) {
            SkillTreeType[] ts = SkillTreeType.values();
            int tw = (width - 12 - (ts.length - 1) * TAB_GAP) / ts.length;
            for (int i = 0; i < ts.length; i++) { int tx = 6 + i * (tw + TAB_GAP);
                if (mx >= tx && mx < tx + tw) { selectTree(ts[i]); return true; } }
        }
        // Footer buttons — row 2, matching drawFooter layout
        int fy = height - FOOTER_H;
        int row2Y = fy + 22, btnH = 18, btnGap = 6;
        int btnX = 12;

        // Plan Mode toggle
        int pmW = 74;
        if (mx >= btnX && mx < btnX + pmW && my >= row2Y && my < row2Y + btnH) {
            planMode = !planMode;
            if (!planMode) plannedNodes.clear();
            return true;
        }
        btnX += pmW + btnGap;

        // Apply + Clear (only in plan mode)
        if (planMode && !plannedNodes.isEmpty()) {
            int apW = 54;
            if (mx >= btnX && mx < btnX + apW && my >= row2Y && my < row2Y + btnH) {
                for (String nid : new ArrayList<>(plannedNodes)) {
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new SkillActionPayload("unlock", nid), new CustomPacketPayload[0]);
                }
                plannedNodes.clear();
                planMode = false;
                return true;
            }
            btnX += apW + btnGap;

            int clW = 48;
            if (mx >= btnX && mx < btnX + clW && my >= row2Y && my < row2Y + btnH) {
                plannedNodes.clear();
                return true;
            }
            btnX += clW + btnGap;
        }

        // Admin toggle: Show Class Branches
        if (isLocalPlayerAdmin()) {
            int cbW = 108;
            if (mx >= btnX && mx < btnX + cbW && my >= row2Y && my < row2Y + btnH) {
                showClassBranches = !showClassBranches;
                rebuildLayout();
                rebuildLocks();
                return true;
            }
            btnX += cbW + btnGap;
        }

        // Respec button (right-aligned)
        int rw = 84, rx = width - rw - 12;
        if (mx >= rx && mx < rx + rw && my >= row2Y && my < row2Y + btnH) {
            ClientPacketDistributor.sendToServer((CustomPacketPayload) new SkillActionPayload("respec", ""), new CustomPacketPayload[0]); return true;
        }

        // Prestige button
        int pLvC = SkillSyncPayload.clientLevels.getOrDefault(selectedTree, 0);
        int pPresC = SkillSyncPayload.clientPrestige.getOrDefault(selectedTree, 0);
        if (pLvC >= 50 && pPresC < 5) {
            int prW = 94, prX = rx - prW - btnGap;
            if (mx >= prX && mx < prX + prW && my >= row2Y && my < row2Y + btnH) {
                showPrestigeConfirm = true;
                return true;
            }
        }
        int sz = 18, bx = treeR() - sz - 4, by = treeB() - sz * 3 - 16;
        if (mx >= bx && mx < bx + sz) {
            if (my >= by && my < by + sz) { panX = 0; panY = 0; zoom = 1f; return true; }
            if (my >= by + sz + 4 && my < by + sz * 2 + 4) { zoom = Math.min(2f, zoom + .1f); return true; }
            if (my >= by + sz * 2 + 8 && my < by + sz * 3 + 8) { zoom = Math.max(.5f, zoom - .1f); return true; } }
        if (hoveredNode != null) {
            Set<String> u = SkillSyncPayload.clientUnlockedNodes; int pts = SkillSyncPayload.clientTreePoints.getOrDefault(selectedTree, 0);
            boolean unl = u.contains(hoveredNode.id()), bl = lockedBranches.contains(hoveredNode.branch().name());
            if (planMode) {
                // In plan mode, toggle node in planned set
                if (!unl) {
                    if (plannedNodes.contains(hoveredNode.id())) {
                        plannedNodes.remove(hoveredNode.id());
                    } else {
                        plannedNodes.add(hoveredNode.id());
                    }
                }
            } else {
                if (!unl && !bl && canUnlock(hoveredNode, u, pts))
                    ClientPacketDistributor.sendToServer((CustomPacketPayload) new SkillActionPayload("unlock", hoveredNode.id()), new CustomPacketPayload[0]);
            }
            selectedNode = hoveredNode; return true; }
        if (my >= treeT() && my < treeB() && mx >= treeL() && mx < treeR()) {
            dragging = true; dragSX = mx; dragSY = my; panSX = panX; panSY = panY; return true; }
        return super.mouseClicked(event, consumed);
    }

    @Override public boolean mouseReleased(MouseButtonEvent e) { if (e.button() == 0) dragging = false; return super.mouseReleased(e); }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging && e.button() == 0) { panX = panSX + (float)(e.x() - dragSX) / zoom; panY = panSY + (float)(e.y() - dragSY) / zoom; return true; }
        return super.mouseDragged(e, dx, dy);
    }

    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (my >= treeT() && my < treeB() && mx >= treeL() && mx < treeR()) {
            float old = zoom;
            zoom = Math.max(.5f, Math.min(2f, zoom + (sy > 0 ? .1f : sy < 0 ? -.1f : 0)));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    public boolean keyPressed(KeyEvent event) {
        int k = event.key();
        if (k >= 49 && k <= 53) { SkillTreeType[] t = SkillTreeType.values(); if (k - 49 < t.length) { selectTree(t[k - 49]); return true; } }
        if (k == 258) { SkillTreeType[] t = SkillTreeType.values(); selectTree(t[(selectedTree.ordinal() + 1) % t.length]); return true; }
        return super.keyPressed(event);
    }

    // ================================================================ helpers

    private void selectTree(SkillTreeType t) { selectedTree = t; selectedNode = null; panX = panY = 0; zoom = 1f; plannedNodes.clear(); rebuildLayout(); }

    private int calcPathCost(SkillNode nd, Set<String> unlocked) {
        if (unlocked.contains(nd.id())) return 0;
        int cost = nd.cost();
        for (String pid : nd.prerequisites()) {
            if (unlocked.contains(pid)) continue;
            SkillNode parent = SkillTreeDefinitions.getNodeById(pid);
            if (parent != null) cost += calcPathCost(parent, unlocked);
        }
        return cost;
    }

    private boolean canUnlock(SkillNode nd, Set<String> u, int pts) {
        if (pts < nd.cost()) return false;
        for (String p : nd.prerequisites()) if (!u.contains(p)) return false;
        return !lockedBranches.contains(nd.branch().name());
    }

    private int wrap(GuiGraphics g, String text, int x, int y, int maxW, int c) {
        StringBuilder ln = new StringBuilder(); int dy = y;
        for (String w : text.split(" ")) {
            String t = ln.length() == 0 ? w : ln + " " + w;
            if (font.width(t) > maxW && ln.length() > 0) { g.drawString(font, ln.toString(), x, dy, c, false); dy += 11; ln = new StringBuilder(w); }
            else ln = new StringBuilder(t);
        }
        if (ln.length() > 0) { g.drawString(font, ln.toString(), x, dy, c, false); dy += 11; }
        return dy;
    }

    private void drawRing(GuiGraphics g, int cx, int cy, int r, int c) {
        int s = Math.max(16, r * 4);
        for (int i = 0; i < s; i++) { double a = Math.PI * 2 * i / s;
            int px = cx + (int)(Math.cos(a) * r), py = cy + (int)(Math.sin(a) * r);
            g.fill(px, py, px + 1, py + 1, c); }
    }

    private void drawThickLine(GuiGraphics g, int x0, int y0, int x1, int y1, int c, int t) {
        if (t <= 1) { drawLine(g, x0, y0, x1, y1, c); return; }
        int w = t / 2; boolean h = Math.abs(x1 - x0) >= Math.abs(y1 - y0);
        for (int o = -w; o <= w; o++) if (h) drawLine(g, x0, y0 + o, x1, y1 + o, c); else drawLine(g, x0 + o, y0, x1 + o, y1, c);
    }

    private void drawLine(GuiGraphics g, int x0, int y0, int x1, int y1, int c) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0), sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, e = dx - dy;
        for (int i = 0, m = dx + dy + 1; i < m; i++) { g.fill(x0, y0, x0 + 1, y0 + 1, c); if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * e; if (e2 > -dy) { e -= dy; x0 += sx; } if (e2 < dx) { e += dx; y0 += sy; } }
    }

    private static int lerpC(int a, int b, float t) {
        return ((int)((a>>24&0xFF)+(((b>>24&0xFF)-(a>>24&0xFF))*t))<<24)|((int)((a>>16&0xFF)+(((b>>16&0xFF)-(a>>16&0xFF))*t))<<16)|((int)((a>>8&0xFF)+(((b>>8&0xFF)-(a>>8&0xFF))*t))<<8)|((int)((a&0xFF)+(((b&0xFF)-(a&0xFF))*t)));
    }
    private static String roman(int t) { return switch(t) { case 1->"I"; case 2->"II"; case 3->"III"; case 4->"IV"; case 5->"V"; default->String.valueOf(t); }; }
    private static String fmtN(String k) { StringBuilder s = new StringBuilder(); for (String p : k.split("_")) { if(s.length()>0) s.append(' '); if(!p.isEmpty()) { s.append(Character.toUpperCase(p.charAt(0))); if(p.length()>1) s.append(p.substring(1)); } } return s.toString(); }
    private static String fmtV(double v) { return v==(int)v ? String.valueOf((int)v) : String.format("%.1f", v); }

    /** Client-side admin check using the admin username list. */
    private static boolean isLocalPlayerAdmin() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) return AdminSystem.ADMIN_USERNAMES.contains(mc.player.getGameProfile().name());
        return AdminSystem.ADMIN_USERNAMES.contains(mc.getUser().getName());
    }

    /**
     * Class-branch hiding is retired with the class-selection system. All
     * branches (including former archetype branches like PALADIN, WARRIOR,
     * WIZARD, ROGUE, RANGER) are visible to every player now.
     */
    private static boolean shouldHideClassBranch(SkillBranch b) {
        return false;
    }

    @Override public boolean isPauseScreen() { return false; }
}
