package com.ultra.megamod.feature.computer.screen.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Admin panel for SpellEngine management. Replaces the deprecated
 * Skill Editor / Cooldowns tabs with four sub-views:
 *   - Container Editor: view/edit spell_ids on mainhand/offhand's spell container data component.
 *   - Cooldown Override: inspect and reset per-spell cooldowns on a target player.
 *   - Registry Viewer: read-only list of every loaded spell JSON, with per-spell detail.
 *   - Trigger Tester: enumerate passive spells matching a trigger type, with safe live-fire for a subset.
 */
public class SpellsAdminPanel {

    // Style
    private static final int BG = 0xFF0D1117;
    private static final int HEADER_BG = 0xFF161B22;
    private static final int BORDER = 0xFF30363D;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int LABEL = 0xFF8B949E;
    private static final int ACCENT_BLUE = 0xFF58A6FF;
    private static final int ACCENT_ORANGE = 0xFFD29922;
    private static final int ACCENT_GREEN = 0xFF3FB950;
    private static final int ACCENT_RED = 0xFFF85149;
    private static final int ACCENT_PURPLE = 0xFFA371F7;
    private static final int ROW_EVEN = 0xFF12151B;
    private static final int ROW_ODD = 0xFF161B22;
    private static final int BTN_BG = 0xFF21262D;
    private static final int BTN_HOVER = 0xFF30363D;

    private static final int ROW_H = 16;

    private static final String[] SUB_VIEWS = {"Container", "Cooldowns", "Registry", "Triggers"};
    private String activeView = "Container";

    private final Font font;

    // Tab-local state
    private int scroll = 0;
    private final List<ActionRect> clickRects = new ArrayList<>();

    // Container state
    private String containerSlot = "mainhand"; // mainhand or offhand
    private ContainerInfo containerInfo = null;
    private int registrySearchScroll = 0; // for picker
    private String containerPickerFilter = "";

    // Cooldowns state
    private String cooldownTargetUUID = ""; // empty = self
    private String cooldownTargetName = "(self)";
    private CooldownInfo cooldownInfo = null;

    // Registry state
    private final List<SpellEntry> registryEntries = new ArrayList<>();
    private String registryFilter = "";
    private String selectedSpellId = null;
    private String selectedSpellJson = null;

    // Trigger state
    private Spell.Trigger.Type selectedTrigger = Spell.Trigger.Type.MELEE_IMPACT;
    private String lastTriggerResult = "(no fires yet — select a trigger and press Fire Now)";

    public SpellsAdminPanel(Font font) {
        this.font = font;
    }

    public void requestInitialData() {
        sendAction("spells_request_registry", "");
        sendAction("spells_request_container", containerSlot);
        sendAction("spells_request_cooldowns", "");
    }

    public void tick() {
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response == null) return;
        String type = response.dataType();
        if (!type.startsWith("spells_")) return;
        handleResponse(type, response.jsonData());
        ComputerDataPayload.lastResponse = null;
    }

    public void handleResponse(String type, String jsonData) {
        try {
            switch (type) {
                case "spells_registry_data": parseRegistry(jsonData); break;
                case "spells_container_data": parseContainer(jsonData); break;
                case "spells_cooldowns_data": parseCooldowns(jsonData); break;
                case "spells_spell_detail": parseSpellDetail(jsonData); break;
                case "spells_trigger_result": parseTriggerResult(jsonData); break;
                case "spells_result": /* toast messages - ignore */ break;
                default: break;
            }
        } catch (Exception ignored) {}
    }

    // ------------------------------------------------------------------
    // Render

    public void render(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        clickRects.clear();
        g.fill(left, top, right, bottom, BG);

        // Header
        int headerH = 20;
        g.fill(left, top, right, top + headerH, HEADER_BG);
        g.fill(left, top + headerH - 1, right, top + headerH, BORDER);
        g.drawString(font, "Spells", left + 6, top + 6, TEXT, false);
        String hint = "SpellEngine authority — container / cooldowns / registry / triggers";
        int hintW = font.width(hint);
        g.drawString(font, hint, right - hintW - 6, top + 6, LABEL, false);

        // Sub-view tabs
        int subY = top + headerH + 3;
        int subX = left + 4;
        for (String sv : SUB_VIEWS) {
            int tw = font.width(sv) + 14;
            boolean active = sv.equals(activeView);
            boolean hover = inRect(mouseX, mouseY, subX, subY, tw, 16);
            int bg = active ? ACCENT_BLUE : (hover ? BTN_HOVER : BTN_BG);
            int txt = active ? 0xFF000000 : TEXT;
            g.fill(subX, subY, subX + tw, subY + 16, bg);
            drawOutline(g, subX, subY, subX + tw, subY + 16, active ? ACCENT_BLUE : BORDER);
            g.drawString(font, sv, subX + 7, subY + 4, txt, false);
            clickRects.add(new ActionRect(subX, subY, tw, 16, "__view__:" + sv));
            subX += tw + 4;
        }

        int contentTop = subY + 22;
        switch (activeView) {
            case "Container": renderContainer(g, mouseX, mouseY, left, contentTop, right, bottom); break;
            case "Cooldowns": renderCooldowns(g, mouseX, mouseY, left, contentTop, right, bottom); break;
            case "Registry":  renderRegistry(g,  mouseX, mouseY, left, contentTop, right, bottom); break;
            case "Triggers":  renderTriggers(g,  mouseX, mouseY, left, contentTop, right, bottom); break;
        }
    }

    // ----- Container view -----

    private void renderContainer(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int x = left + 6;
        int y = top;
        int contentW = right - left - 12;

        // Hand selector
        g.drawString(font, "Slot:", x, y + 4, LABEL, false);
        int bx = x + 36;
        drawToggleButton(g, bx, y, 70, 14, "Mainhand", "mainhand".equals(containerSlot), mouseX, mouseY, "__cont_slot__:mainhand");
        drawToggleButton(g, bx + 74, y, 60, 14, "Offhand", "offhand".equals(containerSlot), mouseX, mouseY, "__cont_slot__:offhand");
        drawButton(g, bx + 138, y, 60, 14, "Refresh", mouseX, mouseY, "__cont_refresh__");
        y += 18;

        // Current item/container info
        g.fill(x, y, right - 6, y + 40, ROW_ODD);
        drawOutline(g, x, y, right - 6, y + 40, BORDER);
        if (containerInfo == null) {
            g.drawString(font, "(no data — click Refresh)", x + 4, y + 14, LABEL, false);
        } else {
            g.drawString(font, "Item: " + containerInfo.itemId + " (" + containerInfo.itemName + ")", x + 4, y + 4, TEXT, false);
            if (!containerInfo.hasContainer) {
                g.drawString(font, "No spell container on this item.", x + 4, y + 18, ACCENT_ORANGE, false);
            } else {
                String line1 = "Access: " + containerInfo.access + "    Pool: " + (containerInfo.pool.isEmpty() ? "-" : containerInfo.pool)
                        + "    Slot restr: " + (containerInfo.containerSlot.isEmpty() ? "-" : containerInfo.containerSlot)
                        + "    Max: " + containerInfo.maxSpells;
                g.drawString(font, line1, x + 4, y + 18, LABEL, false);
                g.drawString(font, "Contains " + containerInfo.spells.size() + " spell(s)", x + 4, y + 30, ACCENT_BLUE, false);
                drawButton(g, right - 6 - 72, y + 22, 66, 14, "Clear All", mouseX, mouseY, "__cont_clear__");
            }
        }
        y += 44;

        // Current spell list
        g.drawString(font, "Bound Spells", x, y, ACCENT_BLUE, false);
        y += 10;
        if (containerInfo != null && containerInfo.hasContainer) {
            if (containerInfo.spells.isEmpty()) {
                g.drawString(font, "(none)", x + 4, y + 2, LABEL, false);
                y += 14;
            } else {
                for (int i = 0; i < containerInfo.spells.size(); i++) {
                    String sid = containerInfo.spells.get(i);
                    int rowBg = (i % 2 == 0) ? ROW_EVEN : ROW_ODD;
                    g.fill(x, y, right - 6, y + ROW_H, rowBg);
                    g.drawString(font, sid, x + 6, y + 4, TEXT, false);
                    drawButton(g, right - 6 - 54, y + 1, 50, ROW_H - 2, "Remove", mouseX, mouseY, "__cont_rm__:" + sid);
                    y += ROW_H;
                }
            }
        }

        // Picker (all spells, filtered)
        int pickerY = y + 6;
        if (pickerY > bottom - 60) pickerY = bottom - 60;
        g.drawString(font, "Add Spell — click to bind", x, pickerY, ACCENT_GREEN, false);
        drawButton(g, right - 6 - 60, pickerY - 2, 56, 12, "Scroll Up",   mouseX, mouseY, "__cont_pick_up__");
        drawButton(g, right - 6 - 122, pickerY - 2, 56, 12, "Scroll Dn", mouseX, mouseY, "__cont_pick_dn__");
        int listY = pickerY + 12;

        List<SpellEntry> filtered = filterRegistry(containerPickerFilter);
        int rowCap = Math.max(1, (bottom - listY - 4) / ROW_H);
        int start = Math.max(0, Math.min(registrySearchScroll, Math.max(0, filtered.size() - rowCap)));
        registrySearchScroll = start;
        int end = Math.min(filtered.size(), start + rowCap);
        for (int i = start; i < end; i++) {
            SpellEntry se = filtered.get(i);
            int idx = i - start;
            int rowBg = (idx % 2 == 0) ? ROW_EVEN : ROW_ODD;
            int ry = listY + idx * ROW_H;
            g.fill(x, ry, right - 6, ry + ROW_H, rowBg);
            g.drawString(font, se.id, x + 6, ry + 4, TEXT, false);
            String tag = se.type + "  " + se.deliver;
            int tagW = font.width(tag);
            g.drawString(font, tag, right - 6 - 70 - tagW - 6, ry + 4, LABEL, false);
            drawButton(g, right - 6 - 66, ry + 1, 62, ROW_H - 2, "Bind", mouseX, mouseY, "__cont_add__:" + se.id);
        }
        String countTxt = filtered.size() + " spells";
        int cW = font.width(countTxt);
        g.drawString(font, countTxt, right - 6 - 190 - cW, pickerY, LABEL, false);
    }

    // ----- Cooldowns view -----

    private void renderCooldowns(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int x = left + 6;
        int y = top;

        g.drawString(font, "Target: " + cooldownTargetName, x, y + 4, TEXT, false);
        drawButton(g, x + 200, y, 60, 14, "Self",     mouseX, mouseY, "__cd_self__");
        drawButton(g, x + 266, y, 70, 14, "Refresh",  mouseX, mouseY, "__cd_refresh__");
        drawButton(g, x + 340, y, 70, 14, "Clear All", mouseX, mouseY, "__cd_clear_all__");
        y += 20;

        if (cooldownInfo == null) {
            g.drawString(font, "(no data — click Refresh)", x, y + 4, LABEL, false);
            return;
        }
        if (cooldownInfo.entries.isEmpty()) {
            g.drawString(font, "No spells are on cooldown for " + cooldownTargetName + ".", x, y + 4, ACCENT_GREEN, false);
            return;
        }

        // Column headers
        g.fill(x, y, right - 6, y + 12, HEADER_BG);
        g.drawString(font, "Spell ID",      x + 6,                 y + 2, LABEL, false);
        g.drawString(font, "Ticks Left",    right - 6 - 220,       y + 2, LABEL, false);
        g.drawString(font, "Seconds",       right - 6 - 140,       y + 2, LABEL, false);
        y += 14;

        for (int i = 0; i < cooldownInfo.entries.size(); i++) {
            CooldownRow row = cooldownInfo.entries.get(i);
            int rowBg = (i % 2 == 0) ? ROW_EVEN : ROW_ODD;
            g.fill(x, y, right - 6, y + ROW_H, rowBg);
            g.drawString(font, row.id, x + 6, y + 4, TEXT, false);
            g.drawString(font, String.valueOf(row.remainingTicks), right - 6 - 220, y + 4, ACCENT_ORANGE, false);
            g.drawString(font, String.format(Locale.ROOT, "%.1fs", row.remainingTicks / 20.0), right - 6 - 140, y + 4, LABEL, false);
            drawButton(g, right - 6 - 58, y + 1, 54, ROW_H - 2, "Reset", mouseX, mouseY, "__cd_reset__:" + row.id);
            y += ROW_H;
        }
    }

    // ----- Registry view -----

    private void renderRegistry(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int x = left + 6;
        int y = top;

        g.drawString(font, registryEntries.size() + " spells registered", x, y + 4, LABEL, false);
        drawButton(g, right - 6 - 70, y, 66, 14, "Refresh", mouseX, mouseY, "__reg_refresh__");
        y += 18;

        // Split into list + detail
        int listW = (right - left) / 2 - 8;
        int listRight = left + 6 + listW;
        int detailLeft = listRight + 6;

        g.fill(x, y, listRight, bottom - 4, ROW_ODD);
        drawOutline(g, x, y, listRight, bottom - 4, BORDER);

        int listY = y + 2;
        int rowCap = Math.max(1, (bottom - 4 - listY - 4) / ROW_H);
        List<SpellEntry> all = filterRegistry(registryFilter);
        int start = Math.max(0, Math.min(scroll, Math.max(0, all.size() - rowCap)));
        scroll = start;
        int end = Math.min(all.size(), start + rowCap);
        for (int i = start; i < end; i++) {
            SpellEntry se = all.get(i);
            int rowY = listY + (i - start) * ROW_H;
            boolean selected = se.id.equals(selectedSpellId);
            boolean hover = inRect(mouseX, mouseY, x + 2, rowY, listW - 4, ROW_H - 1);
            int rbg = selected ? 0xFF1F3A5C : (hover ? 0xFF1B2330 : ((i & 1) == 0 ? ROW_EVEN : ROW_ODD));
            g.fill(x + 2, rowY, listRight - 2, rowY + ROW_H - 1, rbg);
            g.drawString(font, se.id, x + 6, rowY + 4, selected ? ACCENT_BLUE : TEXT, false);
            clickRects.add(new ActionRect(x + 2, rowY, listW - 4, ROW_H - 1, "__reg_select__:" + se.id));
        }

        // Detail
        g.fill(detailLeft, y, right - 4, bottom - 4, ROW_ODD);
        drawOutline(g, detailLeft, y, right - 4, bottom - 4, BORDER);
        int dY = y + 4;
        if (selectedSpellId == null) {
            g.drawString(font, "Select a spell from the list to view details.", detailLeft + 6, dY, LABEL, false);
        } else {
            g.drawString(font, selectedSpellId, detailLeft + 6, dY, ACCENT_BLUE, false);
            dY += 12;
            SpellEntry meta = findEntry(selectedSpellId);
            if (meta != null) {
                g.drawString(font, "Type: " + meta.type + "    School: " + (meta.school.isEmpty() ? "-" : meta.school), detailLeft + 6, dY, LABEL, false);
                dY += 10;
                g.drawString(font, "Deliver: " + meta.deliver + "    Target: " + meta.target + "    Tier: " + meta.tier, detailLeft + 6, dY, LABEL, false);
                dY += 12;
            }
            if (selectedSpellJson == null) {
                g.drawString(font, "(loading...)", detailLeft + 6, dY, LABEL, false);
            } else {
                // Render JSON lines, wrapping on \n
                String[] lines = selectedSpellJson.split("\n");
                int maxLines = (bottom - 4 - dY) / 10;
                int shown = Math.min(lines.length, maxLines);
                for (int i = 0; i < shown; i++) {
                    String ln = lines[i];
                    if (ln.length() > 120) ln = ln.substring(0, 117) + "...";
                    g.drawString(font, ln, detailLeft + 6, dY, TEXT, false);
                    dY += 10;
                }
                if (shown < lines.length) {
                    g.drawString(font, "... (" + (lines.length - shown) + " more lines truncated)", detailLeft + 6, dY, LABEL, false);
                }
            }
        }
    }

    // ----- Triggers view -----

    private void renderTriggers(GuiGraphics g, int mouseX, int mouseY, int left, int top, int right, int bottom) {
        int x = left + 6;
        int y = top;

        g.drawString(font, "Trigger Type:", x, y + 4, LABEL, false);
        y += 14;

        // Trigger type grid
        Spell.Trigger.Type[] types = Spell.Trigger.Type.values();
        int btnW = 112, btnH = 14, gap = 4;
        int cols = Math.max(1, (right - left - 12) / (btnW + gap));
        for (int i = 0; i < types.length; i++) {
            int cx = x + (i % cols) * (btnW + gap);
            int cy = y + (i / cols) * (btnH + gap);
            Spell.Trigger.Type t = types[i];
            boolean active = t == selectedTrigger;
            drawToggleButton(g, cx, cy, btnW, btnH, t.name(), active, mouseX, mouseY, "__trig_select__:" + t.name());
        }
        int rows = (types.length + cols - 1) / cols;
        y += rows * (btnH + gap) + 4;

        // Fire button
        drawButton(g, x, y, 100, 16, "Fire Now", mouseX, mouseY, "__trig_fire__");
        g.drawString(font, "(fires against admin's equipment; some triggers only scan — see notes)", x + 108, y + 4, LABEL, false);
        y += 22;

        // Result box
        g.fill(x, y, right - 6, bottom - 4, ROW_ODD);
        drawOutline(g, x, y, right - 6, bottom - 4, BORDER);
        g.drawString(font, "Result:", x + 4, y + 2, ACCENT_PURPLE, false);
        String[] lines = lastTriggerResult.split("\n");
        int ly = y + 14;
        int maxLines = (bottom - 4 - ly) / 10;
        for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
            g.drawString(font, lines[i], x + 6, ly, TEXT, false);
            ly += 10;
        }
    }

    // ------------------------------------------------------------------
    // Mouse

    public boolean mouseClicked(double mouseX, double mouseY, int button, int left, int top, int right, int bottom) {
        if (button != 0) return false;
        for (ActionRect r : clickRects) {
            if (mouseX >= r.x && mouseX < r.x + r.w && mouseY >= r.y && mouseY < r.y + r.h) {
                handleAction(r.action);
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int step = scrollY > 0 ? -3 : 3;
        if ("Registry".equals(activeView)) {
            scroll = Math.max(0, scroll + step);
        } else if ("Container".equals(activeView)) {
            registrySearchScroll = Math.max(0, registrySearchScroll + step);
        }
        return true;
    }

    private void handleAction(String action) {
        if (action.startsWith("__view__:")) {
            activeView = action.substring(9);
            scroll = 0;
            if ("Container".equals(activeView))  sendAction("spells_request_container", containerSlot);
            if ("Cooldowns".equals(activeView))  sendAction("spells_request_cooldowns", cooldownTargetUUID);
            if ("Registry".equals(activeView) && registryEntries.isEmpty()) sendAction("spells_request_registry", "");
            return;
        }
        if (action.startsWith("__cont_slot__:")) {
            containerSlot = action.substring(14);
            sendAction("spells_request_container", containerSlot);
            return;
        }
        if ("__cont_refresh__".equals(action)) { sendAction("spells_request_container", containerSlot); return; }
        if ("__cont_clear__".equals(action))   { sendAction("spells_container_clear", containerSlot); return; }
        if (action.startsWith("__cont_rm__:")) {
            String spellId = action.substring(12);
            sendAction("spells_container_remove_spell", containerSlot + ":" + spellId);
            return;
        }
        if (action.startsWith("__cont_add__:")) {
            String spellId = action.substring(13);
            sendAction("spells_container_add_spell", containerSlot + ":" + spellId);
            return;
        }
        if ("__cont_pick_up__".equals(action)) { registrySearchScroll = Math.max(0, registrySearchScroll - 6); return; }
        if ("__cont_pick_dn__".equals(action)) { registrySearchScroll = registrySearchScroll + 6; return; }

        if ("__cd_self__".equals(action))     { cooldownTargetUUID = ""; cooldownTargetName = "(self)"; sendAction("spells_request_cooldowns", ""); return; }
        if ("__cd_refresh__".equals(action))  { sendAction("spells_request_cooldowns", cooldownTargetUUID); return; }
        if ("__cd_clear_all__".equals(action)){ sendAction("spells_cooldown_reset", cooldownTargetUUID + ":"); return; }
        if (action.startsWith("__cd_reset__:")) {
            String sid = action.substring(13);
            sendAction("spells_cooldown_reset", cooldownTargetUUID + ":" + sid);
            return;
        }

        if ("__reg_refresh__".equals(action)) { sendAction("spells_request_registry", ""); return; }
        if (action.startsWith("__reg_select__:")) {
            selectedSpellId = action.substring(15);
            selectedSpellJson = null;
            sendAction("spells_request_spell_detail", selectedSpellId);
            return;
        }

        if (action.startsWith("__trig_select__:")) {
            String name = action.substring(16);
            try { selectedTrigger = Spell.Trigger.Type.valueOf(name); } catch (Exception ignored) {}
            return;
        }
        if ("__trig_fire__".equals(action)) {
            sendAction("spells_trigger_fire", selectedTrigger.name());
            return;
        }
    }

    // ------------------------------------------------------------------
    // Parsing

    private void parseRegistry(String json) {
        registryEntries.clear();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = root.getAsJsonArray("spells");
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                registryEntries.add(new SpellEntry(
                        str(o, "id"), str(o, "type"), str(o, "school"),
                        str(o, "deliver"), str(o, "target"), intVal(o, "tier")));
            }
            registryEntries.sort((a, b) -> a.id.compareTo(b.id));
        } catch (Exception ignored) {}
    }

    private void parseContainer(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            ContainerInfo ci = new ContainerInfo();
            ci.slot = str(o, "slot");
            ci.itemId = str(o, "itemId");
            ci.itemName = str(o, "itemName");
            ci.hasContainer = o.has("hasContainer") && o.get("hasContainer").getAsBoolean();
            ci.access = str(o, "access");
            ci.pool = str(o, "pool");
            ci.containerSlot = str(o, "containerSlot");
            ci.maxSpells = intVal(o, "maxSpells");
            ci.spells = new ArrayList<>();
            if (o.has("spells")) {
                for (JsonElement el : o.getAsJsonArray("spells")) ci.spells.add(el.getAsString());
            }
            this.containerInfo = ci;
        } catch (Exception ignored) {}
    }

    private void parseCooldowns(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            CooldownInfo ci = new CooldownInfo();
            ci.playerName = str(o, "player");
            ci.uuid = str(o, "uuid");
            ci.entries = new ArrayList<>();
            if (o.has("cooldowns")) {
                for (JsonElement el : o.getAsJsonArray("cooldowns")) {
                    JsonObject e = el.getAsJsonObject();
                    ci.entries.add(new CooldownRow(str(e, "id"), intVal(e, "remainingTicks")));
                }
            }
            this.cooldownInfo = ci;
            if (cooldownTargetUUID == null || cooldownTargetUUID.isEmpty()) {
                cooldownTargetName = ci.playerName + " (self)";
            } else {
                cooldownTargetName = ci.playerName;
            }
        } catch (Exception ignored) {}
    }

    private void parseSpellDetail(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            String id = str(o, "id");
            if (!id.equals(selectedSpellId)) return;
            if (o.has("error")) {
                selectedSpellJson = "ERROR: " + str(o, "error");
                return;
            }
            if (o.has("json")) {
                // Pretty-print with line breaks after commas at outer depth
                String raw = o.get("json").toString();
                selectedSpellJson = prettyJson(raw);
            }
        } catch (Exception ignored) {}
    }

    private void parseTriggerResult(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            StringBuilder sb = new StringBuilder();
            sb.append("Trigger: ").append(str(o, "type")).append('\n');
            int count = intVal(o, "matchCount");
            sb.append("Matches on admin's equipment: ").append(count).append('\n');
            if (o.has("matches")) {
                for (JsonElement el : o.getAsJsonArray("matches")) {
                    JsonObject m = el.getAsJsonObject();
                    sb.append("  - ").append(str(m, "spell"))
                            .append("  chance=").append(m.has("chance") ? m.get("chance").getAsString() : "?")
                            .append("  stage=").append(str(m, "stage")).append('\n');
                }
            }
            if (o.has("msg")) sb.append('\n').append(str(o, "msg"));
            lastTriggerResult = sb.toString();
        } catch (Exception ignored) {
            lastTriggerResult = "Failed to parse result:\n" + json;
        }
    }

    // ------------------------------------------------------------------
    // Utilities

    private List<SpellEntry> filterRegistry(String filter) {
        if (filter == null || filter.isEmpty()) return registryEntries;
        String f = filter.toLowerCase(Locale.ROOT);
        List<SpellEntry> out = new ArrayList<>();
        for (SpellEntry se : registryEntries) {
            if (se.id.toLowerCase(Locale.ROOT).contains(f)) out.add(se);
        }
        return out;
    }

    private SpellEntry findEntry(String id) {
        for (SpellEntry se : registryEntries) if (se.id.equals(id)) return se;
        return null;
    }

    private static String str(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : ""; }
    private static int intVal(JsonObject o, String k) { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0; }

    private static String prettyJson(String raw) {
        // Very lightweight pretty-printer: indent on { and [, newline after , at depth <= 2
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '"' && (i == 0 || raw.charAt(i - 1) != '\\')) inString = !inString;
            if (inString) { sb.append(c); continue; }
            switch (c) {
                case '{': case '[':
                    depth++;
                    sb.append(c);
                    if (depth <= 3) { sb.append('\n'); appendIndent(sb, depth); }
                    break;
                case '}': case ']':
                    depth--;
                    if (depth <= 2) { sb.append('\n'); appendIndent(sb, depth); }
                    sb.append(c);
                    break;
                case ',':
                    sb.append(c);
                    if (depth <= 3) { sb.append('\n'); appendIndent(sb, depth); }
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void appendIndent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) sb.append("  ");
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, int h, String label, int mx, int my, String action) {
        boolean hover = inRect(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, hover ? BTN_HOVER : BTN_BG);
        drawOutline(g, x, y, x + w, y + h, BORDER);
        int tw = font.width(label);
        g.drawString(font, label, x + (w - tw) / 2, y + (h - 8) / 2, TEXT, false);
        clickRects.add(new ActionRect(x, y, w, h, action));
    }

    private void drawToggleButton(GuiGraphics g, int x, int y, int w, int h, String label, boolean active, int mx, int my, String action) {
        boolean hover = inRect(mx, my, x, y, w, h);
        int bg = active ? ACCENT_BLUE : (hover ? BTN_HOVER : BTN_BG);
        int txt = active ? 0xFF000000 : TEXT;
        g.fill(x, y, x + w, y + h, bg);
        drawOutline(g, x, y, x + w, y + h, active ? ACCENT_BLUE : BORDER);
        int tw = font.width(label);
        g.drawString(font, label, x + (w - tw) / 2, y + (h - 8) / 2, txt, false);
        clickRects.add(new ActionRect(x, y, w, h, action));
    }

    private static void drawOutline(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        g.fill(x0, y0, x1, y0 + 1, color);
        g.fill(x0, y1 - 1, x1, y1, color);
        g.fill(x0, y0, x0 + 1, y1, color);
        g.fill(x1 - 1, y0, x1, y1, color);
    }

    private static void sendAction(String action, String data) {
        ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload(action, data),
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    // ------------------------------------------------------------------
    // POD

    private record ActionRect(int x, int y, int w, int h, String action) {}
    private record SpellEntry(String id, String type, String school, String deliver, String target, int tier) {}
    private record CooldownRow(String id, int remainingTicks) {}

    private static class ContainerInfo {
        String slot = "";
        String itemId = "";
        String itemName = "";
        boolean hasContainer;
        String access = "";
        String pool = "";
        String containerSlot = "";
        int maxSpells;
        List<String> spells = new ArrayList<>();
    }

    private static class CooldownInfo {
        String playerName = "";
        String uuid = "";
        List<CooldownRow> entries = new ArrayList<>();
    }
}
