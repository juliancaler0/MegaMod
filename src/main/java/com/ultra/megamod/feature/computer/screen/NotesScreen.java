package com.ultra.megamod.feature.computer.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NotesScreen extends Screen {
    private final Screen parent;
    private final List<NoteEntry> notes = new ArrayList<>();
    private int selectedNote = -1;
    private int listScroll = 0;
    private int editorScroll = 0;
    private int cursorPos = 0;
    private int cursorBlink = 0;
    private boolean titleFocused = true;
    private int autoSaveTimer = -1;

    // Layout constants
    private static final int SIDEBAR_W = 110;
    private static final int TITLE_BAR_H = 25;
    private static final int TOOLBAR_H = 18;
    private static final int TITLE_INPUT_H = 16;
    private static final int NOTE_ROW_H = 28;
    private static final int NEW_BTN_H = 16;
    private static final int LINE_HEIGHT = 11;
    private static final int MAX_NOTES = 50;
    private static final int MAX_CONTENT_LEN = 10000;

    // Colors
    private static final int BG_SIDEBAR = 0xFF161B22;
    private static final int BG_EDITOR = 0xFF0D1117;
    private static final int TEXT_COLOR = 0xFFE6EDF3;
    private static final int CURSOR_COLOR = 0xFF58A6FF;
    private static final int SELECTED_BG = 0xFF21262D;
    private static final int TIMESTAMP_COLOR = 0xFF8B949E;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int DELETE_COLOR = 0xFFFF6B6B;
    private static final int PREVIEW_COLOR = 0xFF8B949E;
    private static final int TOOLBAR_BG = 0xFF161B22;
    private static final int WORD_COUNT_COLOR = 0xFF8B949E;

    private boolean dataLoaded = false;
    private int pollTimer = 0;

    public static class NoteEntry {
        public String id;
        public String title;
        public String content;
        public long lastModified;

        public NoteEntry(String id, String title, String content, long lastModified) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.lastModified = lastModified;
        }
    }

    public NotesScreen(Screen parent) {
        super(Component.literal("Notes"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.dataLoaded) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("notes_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.cursorBlink++;

        // Poll for data response
        if (!this.dataLoaded || this.pollTimer > 0) {
            ComputerDataPayload resp = ComputerDataPayload.lastResponse;
            if (resp != null && resp.dataType().equals("notes_data")) {
                ComputerDataPayload.lastResponse = null;
                this.parseNotesData(resp.jsonData());
                this.dataLoaded = true;
                this.pollTimer = 0;
            } else if (resp != null && "error".equals(resp.dataType())) {
                // Consume error responses so the screen doesn't stay stuck
                ComputerDataPayload.lastResponse = null;
                this.dataLoaded = true;
                this.pollTimer = 0;
            } else {
                this.pollTimer++;
            }
        }

        // Auto-save countdown
        if (this.autoSaveTimer > 0) {
            this.autoSaveTimer--;
            if (this.autoSaveTimer == 0) {
                this.saveCurrentNote();
                this.autoSaveTimer = -1;
            }
        }
    }

    private void parseNotesData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("notes");
            int prevSelected = this.selectedNote;
            String prevId = (prevSelected >= 0 && prevSelected < this.notes.size())
                    ? this.notes.get(prevSelected).id : null;
            this.notes.clear();
            for (JsonElement el : arr) {
                JsonObject n = el.getAsJsonObject();
                this.notes.add(new NoteEntry(
                    n.get("id").getAsString(),
                    n.get("title").getAsString(),
                    n.get("content").getAsString(),
                    n.get("lastModified").getAsLong()
                ));
            }
            // Restore selection by id
            if (prevId != null) {
                for (int i = 0; i < this.notes.size(); i++) {
                    if (this.notes.get(i).id.equals(prevId)) {
                        this.selectedNote = i;
                        return;
                    }
                }
            }
            // If we had no previous selection and notes exist, select first
            if (this.selectedNote < 0 && !this.notes.isEmpty()) {
                this.selectedNote = 0;
                this.cursorPos = 0;
                this.titleFocused = true;
            }
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Full background
        g.fill(0, 0, this.width, this.height, BG_EDITOR);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, TITLE_BAR_H);
        Objects.requireNonNull(this.font);
        int titleY = (TITLE_BAR_H - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Notes", this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (TITLE_BAR_H - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        int contentTop = TITLE_BAR_H;
        int contentH = this.height - contentTop;

        // Sidebar
        this.renderSidebar(g, 0, contentTop, SIDEBAR_W, contentH, mouseX, mouseY);

        // Vertical divider
        g.fill(SIDEBAR_W, contentTop, SIDEBAR_W + 1, this.height, 0xFF30363D);

        // Editor area
        int editorX = SIDEBAR_W + 1;
        int editorW = this.width - editorX;
        this.renderEditor(g, editorX, contentTop, editorW, contentH, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderSidebar(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.fill(x, y, x + w, y + h, BG_SIDEBAR);

        // "New Note" button
        int btnPad = 6;
        int btnX = x + btnPad;
        int btnY = y + btnPad;
        int btnW = w - btnPad * 2;
        boolean newHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + NEW_BTN_H;
        UIHelper.drawButton(g, btnX, btnY, btnW, NEW_BTN_H, newHover);
        String newLabel = "+ New Note";
        int newLabelX = btnX + (btnW - this.font.width(newLabel)) / 2;
        g.drawString(this.font, newLabel, newLabelX, btnY + (NEW_BTN_H - 9) / 2, UIHelper.CREAM_TEXT, false);

        // Note list
        int listTop = btnY + NEW_BTN_H + 6;
        int listBottom = y + h;
        int visibleH = listBottom - listTop;
        int totalH = this.notes.size() * NOTE_ROW_H;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.listScroll = Math.max(0, Math.min(this.listScroll, maxScroll));

        g.enableScissor(x, listTop, x + w, listBottom);
        for (int i = 0; i < this.notes.size(); i++) {
            int rowY = listTop + i * NOTE_ROW_H - this.listScroll;
            if (rowY + NOTE_ROW_H < listTop || rowY > listBottom) continue;

            NoteEntry note = this.notes.get(i);
            boolean selected = (i == this.selectedNote);
            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= rowY && mouseY < rowY + NOTE_ROW_H;

            // Row background
            if (selected) {
                g.fill(x, rowY, x + w, rowY + NOTE_ROW_H, SELECTED_BG);
            } else if (hovered) {
                g.fill(x, rowY, x + w, rowY + NOTE_ROW_H, 0xFF1C2128);
            }

            // Title text (truncated)
            String noteTitle = note.title.isEmpty() ? "Untitled" : note.title;
            if (this.font.width(noteTitle) > w - 28) {
                while (this.font.width(noteTitle + "..") > w - 28 && noteTitle.length() > 1) {
                    noteTitle = noteTitle.substring(0, noteTitle.length() - 1);
                }
                noteTitle += "..";
            }
            g.drawString(this.font, noteTitle, x + 6, rowY + 3, selected ? TITLE_COLOR : TEXT_COLOR, false);

            // Preview text
            String preview = note.content.replace("\n", " ");
            if (preview.length() > 20) preview = preview.substring(0, 20) + "..";
            if (preview.isEmpty()) preview = "Empty note";
            g.drawString(this.font, preview, x + 6, rowY + 14, PREVIEW_COLOR, false);

            // Delete X button
            int delX = x + w - 14;
            int delY = rowY + 3;
            int delSize = 10;
            boolean delHover = mouseX >= delX && mouseX < delX + delSize && mouseY >= delY && mouseY < delY + delSize;
            if (hovered || selected) {
                g.drawString(this.font, "x", delX + 1, delY, delHover ? 0xFFFF4444 : DELETE_COLOR, false);
            }

            // Bottom divider
            g.fill(x + 6, rowY + NOTE_ROW_H - 1, x + w - 6, rowY + NOTE_ROW_H, 0xFF30363D);
        }
        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH && maxScroll > 0) {
            float progress = (float) this.listScroll / (float) maxScroll;
            UIHelper.drawScrollbar(g, x + w - 8, listTop, visibleH, progress);
        }
    }

    private void renderEditor(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        if (this.selectedNote < 0 || this.selectedNote >= this.notes.size()) {
            // No note selected — show placeholder
            String msg = this.notes.isEmpty() ? "Click '+ New Note' to get started" : "Select a note from the list";
            int msgW = this.font.width(msg);
            g.drawString(this.font, msg, x + (w - msgW) / 2, y + h / 2 - 4, TIMESTAMP_COLOR, false);
            return;
        }

        NoteEntry note = this.notes.get(this.selectedNote);
        int pad = 10;
        int innerX = x + pad;
        int innerW = w - pad * 2;

        // Toolbar
        int toolY = y;
        g.fill(x, toolY, x + w, toolY + TOOLBAR_H, TOOLBAR_BG);
        g.fill(x, toolY + TOOLBAR_H - 1, x + w, toolY + TOOLBAR_H, 0xFF30363D);
        this.renderToolbar(g, innerX, toolY, innerW, mouseX, mouseY, note);

        // Title input area
        int titleAreaY = toolY + TOOLBAR_H + 4;
        boolean titleHover = mouseX >= innerX && mouseX < innerX + innerW && mouseY >= titleAreaY && mouseY < titleAreaY + TITLE_INPUT_H;
        // Title background
        g.fill(innerX - 2, titleAreaY - 1, innerX + innerW + 2, titleAreaY + TITLE_INPUT_H + 1, this.titleFocused ? 0xFF21262D : 0xFF161B22);
        // Draw title text
        String titleText = note.title;
        if (this.titleFocused) {
            // Draw with cursor
            String beforeCursor = titleText.substring(0, Math.min(this.cursorPos, titleText.length()));
            String afterCursor = titleText.substring(Math.min(this.cursorPos, titleText.length()));
            int textX = innerX;
            int textY = titleAreaY + (TITLE_INPUT_H - 9) / 2;
            g.drawString(this.font, beforeCursor, textX, textY, TITLE_COLOR, false);
            int cursorX = textX + this.font.width(beforeCursor);
            g.drawString(this.font, afterCursor, cursorX, textY, TITLE_COLOR, false);
            // Blinking cursor
            if ((this.cursorBlink / 10) % 2 == 0) {
                g.fill(cursorX, textY - 1, cursorX + 1, textY + 10, CURSOR_COLOR);
            }
        } else {
            g.drawString(this.font, titleText.isEmpty() ? "Untitled" : titleText, innerX, titleAreaY + (TITLE_INPUT_H - 9) / 2,
                    titleText.isEmpty() ? TIMESTAMP_COLOR : TITLE_COLOR, false);
        }

        // Divider below title
        int divY = titleAreaY + TITLE_INPUT_H + 4;
        g.fill(innerX, divY, innerX + innerW, divY + 1, 0xFF30363D);

        // Content area
        int contentTop = divY + 4;
        int contentBottom = y + h - 4;
        int contentH = contentBottom - contentTop;
        if (contentH <= 0) return;

        // Word wrap and render content
        List<VisualLine> lines = this.wordWrap(note.content, innerW);
        int totalVisualH = lines.size() * LINE_HEIGHT;
        int maxEditorScroll = Math.max(0, totalVisualH - contentH);
        this.editorScroll = Math.max(0, Math.min(this.editorScroll, maxEditorScroll));

        g.enableScissor(innerX, contentTop, innerX + innerW, contentBottom);

        if (!this.titleFocused) {
            // Find cursor visual position
            int cursorLine = -1;
            int cursorXOffset = 0;
            int charsSoFar = 0;
            for (int i = 0; i < lines.size(); i++) {
                VisualLine vl = lines.get(i);
                int lineLen = vl.text.length();
                if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                    cursorLine = i;
                    String sub = vl.text.substring(0, this.cursorPos - charsSoFar);
                    cursorXOffset = this.font.width(sub);
                    break;
                }
                charsSoFar += lineLen;
            }
            // If cursor wasn't found (e.g., at end), put on last line
            if (cursorLine == -1 && !lines.isEmpty()) {
                cursorLine = lines.size() - 1;
                cursorXOffset = this.font.width(lines.get(cursorLine).text);
            }

            // Auto-scroll to keep cursor visible
            if (cursorLine >= 0) {
                int cursorVisualY = cursorLine * LINE_HEIGHT;
                if (cursorVisualY - this.editorScroll < 0) {
                    this.editorScroll = cursorVisualY;
                }
                if (cursorVisualY + LINE_HEIGHT - this.editorScroll > contentH) {
                    this.editorScroll = cursorVisualY + LINE_HEIGHT - contentH;
                }
                this.editorScroll = Math.max(0, Math.min(this.editorScroll, maxEditorScroll));
            }
        }

        // Render lines
        for (int i = 0; i < lines.size(); i++) {
            int lineY = contentTop + i * LINE_HEIGHT - this.editorScroll;
            if (lineY + LINE_HEIGHT < contentTop || lineY > contentBottom) continue;
            g.drawString(this.font, lines.get(i).text, innerX, lineY, TEXT_COLOR, false);
        }

        // Render cursor in content
        if (!this.titleFocused && (this.cursorBlink / 10) % 2 == 0) {
            int charsSoFar2 = 0;
            for (int i = 0; i < lines.size(); i++) {
                VisualLine vl = lines.get(i);
                int lineLen = vl.text.length();
                if (this.cursorPos >= charsSoFar2 && this.cursorPos <= charsSoFar2 + lineLen) {
                    String sub = vl.text.substring(0, this.cursorPos - charsSoFar2);
                    int cxPos = innerX + this.font.width(sub);
                    int cyPos = contentTop + i * LINE_HEIGHT - this.editorScroll;
                    g.fill(cxPos, cyPos - 1, cxPos + 1, cyPos + 10, CURSOR_COLOR);
                    break;
                }
                charsSoFar2 += lineLen;
            }
        }

        g.disableScissor();

        // Editor scrollbar
        if (totalVisualH > contentH && maxEditorScroll > 0) {
            float progress = (float) this.editorScroll / (float) maxEditorScroll;
            UIHelper.drawScrollbar(g, x + w - 10, contentTop, contentH, progress);
        }

        // Placeholder text if content empty
        if (note.content.isEmpty() && !this.titleFocused) {
            g.drawString(this.font, "Start typing...", innerX, contentTop, TIMESTAMP_COLOR, false);
        }
    }

    private void renderToolbar(GuiGraphics g, int x, int y, int w, int mouseX, int mouseY, NoteEntry note) {
        int btnY = y + 2;
        int btnH = 14;
        int bx = x;

        // Bold marker button
        int boldW = 30;
        boolean boldHover = mouseX >= bx && mouseX < bx + boldW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, bx, btnY, boldW, btnH, boldHover);
        g.drawString(this.font, "*B*", bx + (boldW - this.font.width("*B*")) / 2, btnY + (btnH - 9) / 2, TEXT_COLOR, false);
        bx += boldW + 4;

        // Timestamp button
        int tsW = 50;
        boolean tsHover = mouseX >= bx && mouseX < bx + tsW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, bx, btnY, tsW, btnH, tsHover);
        g.drawString(this.font, "Time", bx + (tsW - this.font.width("Time")) / 2, btnY + (btnH - 9) / 2, TEXT_COLOR, false);
        bx += tsW + 4;

        // Word count display (right side)
        String content = note.content;
        int wordCount = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
        String wcText = wordCount + " word" + (wordCount != 1 ? "s" : "");
        int wcW = this.font.width(wcText);
        g.drawString(this.font, wcText, x + w - wcW, btnY + (btnH - 9) / 2, WORD_COUNT_COLOR, false);

        // Auto-save indicator
        if (this.autoSaveTimer > 0) {
            String saveText = "Saving...";
            int saveW = this.font.width(saveText);
            g.drawString(this.font, saveText, x + w - wcW - saveW - 10, btnY + (btnH - 9) / 2, CURSOR_COLOR, false);
        }

        // Last modified timestamp
        if (note.lastModified > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm");
            String timeStr = sdf.format(new Date(note.lastModified));
            int timeW = this.font.width(timeStr);
            g.drawString(this.font, timeStr, x + w - wcW - timeW - 10, btnY + (btnH - 9) / 2, TIMESTAMP_COLOR, false);
        }
    }

    // --- Word wrapping ---

    private static class VisualLine {
        final String text;
        final int startIndex; // index into the original content string where this visual line starts

        VisualLine(String text, int startIndex) {
            this.text = text;
            this.startIndex = startIndex;
        }
    }

    private List<VisualLine> wordWrap(String content, int maxWidth) {
        List<VisualLine> result = new ArrayList<>();
        if (content.isEmpty()) {
            result.add(new VisualLine("", 0));
            return result;
        }

        String[] rawLines = content.split("\n", -1);
        int globalIndex = 0;
        for (String rawLine : rawLines) {
            if (rawLine.isEmpty()) {
                result.add(new VisualLine("", globalIndex));
                globalIndex += 1; // the \n
                continue;
            }
            int lineStart = globalIndex;
            int pos = 0;
            while (pos < rawLine.length()) {
                int endPos = rawLine.length();
                // Find how much text fits in maxWidth
                String sub = rawLine.substring(pos);
                if (this.font.width(sub) <= maxWidth) {
                    result.add(new VisualLine(sub, lineStart + pos));
                    pos = rawLine.length();
                } else {
                    // Binary search for the break point
                    int lo = pos + 1;
                    int hi = rawLine.length();
                    while (lo < hi) {
                        int mid = (lo + hi + 1) / 2;
                        if (this.font.width(rawLine.substring(pos, mid)) <= maxWidth) {
                            lo = mid;
                        } else {
                            hi = mid - 1;
                        }
                    }
                    int breakAt = lo;
                    // Try to break at a word boundary
                    int wordBreak = rawLine.lastIndexOf(' ', breakAt - 1);
                    if (wordBreak > pos) {
                        breakAt = wordBreak + 1; // include the space on this line
                    }
                    result.add(new VisualLine(rawLine.substring(pos, breakAt), lineStart + pos));
                    pos = breakAt;
                }
            }
            globalIndex += rawLine.length() + 1; // +1 for \n
        }
        return result;
    }

    // --- Input handling ---

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (TITLE_BAR_H - backH) / 2;
        if (mx >= backX && mx < backX + backW && my >= backY && my < backY + backH) {
            // Flush pending save
            if (this.autoSaveTimer > 0) {
                this.saveCurrentNote();
                this.autoSaveTimer = -1;
            }
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        // New Note button
        int btnPad = 6;
        int btnX = btnPad;
        int btnY = TITLE_BAR_H + btnPad;
        int btnW = SIDEBAR_W - btnPad * 2;
        if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + NEW_BTN_H) {
            if (this.notes.size() < MAX_NOTES) {
                // Flush pending save first
                if (this.autoSaveTimer > 0) {
                    this.saveCurrentNote();
                    this.autoSaveTimer = -1;
                }
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("notes_create", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                this.pollTimer = 1;
            }
            return true;
        }

        // Note list click
        int listTop = btnY + NEW_BTN_H + 6;
        int listBottom = this.height;
        if (mx >= 0 && mx < SIDEBAR_W && my >= listTop && my < listBottom) {
            int relY = my - listTop + this.listScroll;
            int index = relY / NOTE_ROW_H;
            if (index >= 0 && index < this.notes.size()) {
                // Check if delete button clicked
                int delX = SIDEBAR_W - 14;
                int rowTop = listTop + index * NOTE_ROW_H - this.listScroll;
                int delY2 = rowTop + 3;
                if (mx >= delX && mx < delX + 10 && my >= delY2 && my < delY2 + 10) {
                    this.deleteNote(index);
                    return true;
                }
                // Select note
                if (this.autoSaveTimer > 0) {
                    this.saveCurrentNote();
                    this.autoSaveTimer = -1;
                }
                this.selectedNote = index;
                this.cursorPos = 0;
                this.titleFocused = true;
                this.editorScroll = 0;
            }
            return true;
        }

        // Toolbar clicks
        if (this.selectedNote >= 0 && this.selectedNote < this.notes.size()) {
            int editorX = SIDEBAR_W + 1;
            int pad = 10;
            int innerX = editorX + pad;
            int toolY = TITLE_BAR_H;
            int tbtnY = toolY + 2;
            int tbtnH = 14;

            // Bold button
            int boldW = 30;
            if (mx >= innerX && mx < innerX + boldW && my >= tbtnY && my < tbtnY + tbtnH) {
                this.insertBoldMarker();
                return true;
            }

            // Timestamp button
            int tsX = innerX + boldW + 4;
            int tsW = 50;
            if (mx >= tsX && mx < tsX + tsW && my >= tbtnY && my < tbtnY + tbtnH) {
                this.insertTimestamp();
                return true;
            }

            // Title area click
            int titleAreaY = toolY + TOOLBAR_H + 4;
            int innerW = this.width - editorX - pad * 2;
            if (mx >= innerX && mx < innerX + innerW && my >= titleAreaY && my < titleAreaY + TITLE_INPUT_H) {
                this.titleFocused = true;
                NoteEntry note = this.notes.get(this.selectedNote);
                // Position cursor based on click position
                int clickX = mx - innerX;
                this.cursorPos = this.getCharIndexAtX(note.title, clickX);
                return true;
            }

            // Content area click
            int divY2 = titleAreaY + TITLE_INPUT_H + 4;
            int contentTop2 = divY2 + 4;
            int contentBottom2 = this.height - 4;
            if (mx >= innerX && mx < innerX + innerW && my >= contentTop2 && my < contentBottom2) {
                this.titleFocused = false;
                NoteEntry note = this.notes.get(this.selectedNote);
                // Find which visual line was clicked
                List<VisualLine> lines = this.wordWrap(note.content, innerW);
                int clickRelY = my - contentTop2 + this.editorScroll;
                int lineIndex = clickRelY / LINE_HEIGHT;
                lineIndex = Math.max(0, Math.min(lineIndex, lines.size() - 1));
                if (lineIndex < lines.size()) {
                    VisualLine vl = lines.get(lineIndex);
                    int clickX = mx - innerX;
                    int charInLine = this.getCharIndexAtX(vl.text, clickX);
                    this.cursorPos = vl.startIndex + charInLine;
                }
                return true;
            }
        }

        return super.mouseClicked(event, consumed);
    }

    private int getCharIndexAtX(String text, int targetX) {
        if (text.isEmpty()) return 0;
        for (int i = 0; i <= text.length(); i++) {
            int w = this.font.width(text.substring(0, i));
            if (w > targetX) {
                // Check if previous or current char is closer
                if (i > 0) {
                    int prevW = this.font.width(text.substring(0, i - 1));
                    return (targetX - prevW < w - targetX) ? i - 1 : i;
                }
                return 0;
            }
        }
        return text.length();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int mx = (int) mouseX;
        // Sidebar scroll
        if (mx < SIDEBAR_W) {
            this.listScroll -= (int) (scrollY * NOTE_ROW_H);
            this.listScroll = Math.max(0, this.listScroll);
            return true;
        }
        // Editor scroll
        this.editorScroll -= (int) (scrollY * LINE_HEIGHT * 3);
        this.editorScroll = Math.max(0, this.editorScroll);
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.autoSaveTimer > 0) {
                this.saveCurrentNote();
                this.autoSaveTimer = -1;
            }
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        if (this.selectedNote < 0 || this.selectedNote >= this.notes.size()) {
            return super.keyPressed(event);
        }

        NoteEntry note = this.notes.get(this.selectedNote);

        if (this.titleFocused) {
            return this.handleTitleKeyPress(keyCode, note, event);
        } else {
            return this.handleContentKeyPress(keyCode, note, event);
        }
    }

    private boolean handleTitleKeyPress(int keyCode, NoteEntry note, KeyEvent event) {
        boolean handled = false;
        switch (keyCode) {
            case 257: // Enter — switch to content
                this.titleFocused = false;
                this.cursorPos = 0;
                handled = true;
                break;
            case 258: // Tab — switch to content
                this.titleFocused = false;
                this.cursorPos = 0;
                handled = true;
                break;
            case 259: // Backspace
                if (this.cursorPos > 0 && !note.title.isEmpty()) {
                    int cp = Math.min(this.cursorPos, note.title.length());
                    note.title = note.title.substring(0, cp - 1) + note.title.substring(cp);
                    this.cursorPos = cp - 1;
                    this.markDirty();
                }
                handled = true;
                break;
            case 261: // Delete
                if (this.cursorPos < note.title.length()) {
                    note.title = note.title.substring(0, this.cursorPos) + note.title.substring(this.cursorPos + 1);
                    this.markDirty();
                }
                handled = true;
                break;
            case 263: // Left
                if (this.cursorPos > 0) this.cursorPos--;
                handled = true;
                break;
            case 262: // Right
                if (this.cursorPos < note.title.length()) this.cursorPos++;
                handled = true;
                break;
            case 268: // Home
                this.cursorPos = 0;
                handled = true;
                break;
            case 269: // End
                this.cursorPos = note.title.length();
                handled = true;
                break;
            default:
                break;
        }
        if (handled) {
            this.cursorBlink = 0;
            return true;
        }
        return super.keyPressed(event);
    }

    private boolean handleContentKeyPress(int keyCode, NoteEntry note, KeyEvent event) {
        boolean handled = false;
        int editorX = SIDEBAR_W + 1;
        int pad = 10;
        int innerW = this.width - editorX - pad * 2;

        switch (keyCode) {
            case 257: // Enter
                if (note.content.length() < MAX_CONTENT_LEN) {
                    int cp = Math.min(this.cursorPos, note.content.length());
                    note.content = note.content.substring(0, cp) + "\n" + note.content.substring(cp);
                    this.cursorPos = cp + 1;
                    this.markDirty();
                }
                handled = true;
                break;
            case 259: // Backspace
                if (this.cursorPos > 0) {
                    int cp = Math.min(this.cursorPos, note.content.length());
                    note.content = note.content.substring(0, cp - 1) + note.content.substring(cp);
                    this.cursorPos = cp - 1;
                    this.markDirty();
                }
                handled = true;
                break;
            case 261: // Delete
                if (this.cursorPos < note.content.length()) {
                    note.content = note.content.substring(0, this.cursorPos) + note.content.substring(this.cursorPos + 1);
                    this.markDirty();
                }
                handled = true;
                break;
            case 263: // Left
                if (this.cursorPos > 0) this.cursorPos--;
                handled = true;
                break;
            case 262: // Right
                if (this.cursorPos < note.content.length()) this.cursorPos++;
                handled = true;
                break;
            case 265: // Up
                this.moveCursorVertically(note.content, innerW, -1);
                handled = true;
                break;
            case 264: // Down
                this.moveCursorVertically(note.content, innerW, 1);
                handled = true;
                break;
            case 268: // Home
                this.moveCursorToLineStart(note.content, innerW);
                handled = true;
                break;
            case 269: // End
                this.moveCursorToLineEnd(note.content, innerW);
                handled = true;
                break;
            default:
                break;
        }
        if (handled) {
            this.cursorBlink = 0;
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char codePoint = (char) event.codepoint();
        int modifiers = event.modifiers();
        if (this.selectedNote < 0 || this.selectedNote >= this.notes.size()) {
            return false;
        }

        // Filter out control characters
        if (codePoint < 32 && codePoint != '\t') return false;

        NoteEntry note = this.notes.get(this.selectedNote);

        if (this.titleFocused) {
            // Limit title length
            if (note.title.length() >= 100) return true;
            int cp = Math.min(this.cursorPos, note.title.length());
            note.title = note.title.substring(0, cp) + codePoint + note.title.substring(cp);
            this.cursorPos = cp + 1;
            this.markDirty();
        } else {
            if (note.content.length() >= MAX_CONTENT_LEN) return true;
            int cp = Math.min(this.cursorPos, note.content.length());
            note.content = note.content.substring(0, cp) + codePoint + note.content.substring(cp);
            this.cursorPos = cp + 1;
            this.markDirty();
        }
        this.cursorBlink = 0;
        return true;
    }

    // --- Cursor movement helpers ---

    private void moveCursorVertically(String content, int maxWidth, int direction) {
        List<VisualLine> lines = this.wordWrap(content, maxWidth);
        // Find current visual line and x offset
        int currentLine = -1;
        int offsetInLine = 0;
        int charsSoFar = 0;
        for (int i = 0; i < lines.size(); i++) {
            int lineLen = lines.get(i).text.length();
            if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                currentLine = i;
                offsetInLine = this.cursorPos - charsSoFar;
                break;
            }
            charsSoFar += lineLen;
        }
        if (currentLine < 0) return;

        // Get x pixel position of cursor on current line
        int xPixel = this.font.width(lines.get(currentLine).text.substring(0, offsetInLine));

        int targetLine = currentLine + direction;
        if (targetLine < 0) {
            // Move to title
            this.titleFocused = true;
            if (this.selectedNote >= 0 && this.selectedNote < this.notes.size()) {
                this.cursorPos = Math.min(this.notes.get(this.selectedNote).title.length(),
                        this.getCharIndexAtX(this.notes.get(this.selectedNote).title, xPixel));
            }
            return;
        }
        if (targetLine >= lines.size()) {
            // Already at bottom, move to end
            this.cursorPos = content.length();
            return;
        }

        // Find equivalent x position on target line
        VisualLine targetVl = lines.get(targetLine);
        int charInTarget = this.getCharIndexAtX(targetVl.text, xPixel);
        this.cursorPos = targetVl.startIndex + charInTarget;
    }

    private void moveCursorToLineStart(String content, int maxWidth) {
        List<VisualLine> lines = this.wordWrap(content, maxWidth);
        int charsSoFar = 0;
        for (VisualLine vl : lines) {
            int lineLen = vl.text.length();
            if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                this.cursorPos = charsSoFar;
                return;
            }
            charsSoFar += lineLen;
        }
    }

    private void moveCursorToLineEnd(String content, int maxWidth) {
        List<VisualLine> lines = this.wordWrap(content, maxWidth);
        int charsSoFar = 0;
        for (VisualLine vl : lines) {
            int lineLen = vl.text.length();
            if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                this.cursorPos = charsSoFar + lineLen;
                return;
            }
            charsSoFar += lineLen;
        }
    }

    // --- Toolbar actions ---

    private void insertBoldMarker() {
        if (this.selectedNote < 0 || this.selectedNote >= this.notes.size()) return;
        NoteEntry note = this.notes.get(this.selectedNote);
        if (this.titleFocused) return;
        if (note.content.length() + 2 > MAX_CONTENT_LEN) return;
        int cp = Math.min(this.cursorPos, note.content.length());
        note.content = note.content.substring(0, cp) + "**" + note.content.substring(cp);
        this.cursorPos = cp + 1; // place cursor between the two asterisks
        this.markDirty();
    }

    private void insertTimestamp() {
        if (this.selectedNote < 0 || this.selectedNote >= this.notes.size()) return;
        NoteEntry note = this.notes.get(this.selectedNote);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String ts = "[" + sdf.format(new Date()) + "] ";
        if (this.titleFocused) {
            if (note.title.length() + ts.length() > 100) return;
            int cp = Math.min(this.cursorPos, note.title.length());
            note.title = note.title.substring(0, cp) + ts + note.title.substring(cp);
            this.cursorPos = cp + ts.length();
        } else {
            if (note.content.length() + ts.length() > MAX_CONTENT_LEN) return;
            int cp = Math.min(this.cursorPos, note.content.length());
            note.content = note.content.substring(0, cp) + ts + note.content.substring(cp);
            this.cursorPos = cp + ts.length();
        }
        this.markDirty();
    }

    // --- Saving & communication ---

    private void markDirty() {
        this.autoSaveTimer = 60; // 3 seconds at 20 ticks/sec
    }

    private void saveCurrentNote() {
        if (this.selectedNote < 0 || this.selectedNote >= this.notes.size()) return;
        NoteEntry note = this.notes.get(this.selectedNote);
        // Build JSON manually to avoid issues with special characters
        String escapedTitle = escapeJson(note.title);
        String escapedContent = escapeJson(note.content);
        String json = "{\"id\":\"" + escapeJson(note.id) + "\",\"title\":\"" + escapedTitle + "\",\"content\":\"" + escapedContent + "\"}";
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("notes_save", json),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    private void deleteNote(int index) {
        if (index < 0 || index >= this.notes.size()) return;
        NoteEntry note = this.notes.get(index);
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("notes_delete", note.id),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.notes.remove(index);
        if (this.selectedNote >= this.notes.size()) {
            this.selectedNote = this.notes.size() - 1;
        }
        if (this.selectedNote >= 0 && this.selectedNote < this.notes.size()) {
            this.cursorPos = 0;
            this.titleFocused = true;
        } else {
            this.selectedNote = -1;
        }
        this.autoSaveTimer = -1;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        // Flush pending save on close
        if (this.autoSaveTimer > 0) {
            this.saveCurrentNote();
        }
        super.onClose();
    }
}
