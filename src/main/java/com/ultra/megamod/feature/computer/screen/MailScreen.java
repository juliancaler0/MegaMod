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

public class MailScreen extends Screen {
    private final Screen parent;
    private int listScroll = 0;
    private int detailScroll = 0;
    private int selectedMail = -1;
    private boolean composing = false;
    private String composeTo = "";
    private String composeSubject = "";
    private String composeBody = "";
    private String composeAttachment = "";
    private String composeAttachmentName = "";
    private int focusedField = 0; // 0=to, 1=subject, 2=body
    private int cursorPos = 0;
    private int cursorBlink = 0;
    private List<MailEntry> inbox = new ArrayList<>();
    private String statusMsg = "";
    private int statusTimer = 0;

    private boolean dataLoaded = false;
    private int pollTimer = 0;

    // Layout constants
    private static final int TITLE_BAR_H = 25;
    private static final int TOOLBAR_H = 20;
    private static final int LIST_W = 120;
    private static final int MAIL_ROW_H = 32;
    private static final int LINE_HEIGHT = 11;
    private static final int FIELD_H = 14;
    private static final int FIELD_GAP = 4;

    // Colors
    private static final int BG_DARK = 0xFF0D1117;
    private static final int BG_SIDEBAR = 0xFF161B22;
    private static final int BG_COMPOSE = 0xFF161B22;
    private static final int TEXT_COLOR = 0xFFE6EDF3;
    private static final int DIM_TEXT = 0xFF8B949E;
    private static final int UNREAD_DOT = 0xFF58A6FF;
    private static final int CURSOR_COLOR = 0xFF58A6FF;
    private static final int SELECTED_BG = 0xFF21262D;
    private static final int HOVER_BG = 0xFF1C2128;
    private static final int ATTACHMENT_COLOR = 0xFFD29922;
    private static final int DIVIDER_COLOR = 0xFF30363D;
    private static final int DELETE_COLOR = 0xFFFF6B6B;
    private static final int SEND_COLOR = 0xFF3FB950;
    private static final int FIELD_BG = 0xFF21262D;
    private static final int FIELD_FOCUS_BORDER = 0xFF58A6FF;
    private static final int FIELD_BORDER = 0xFF30363D;

    public record MailEntry(String id, String from, String fromUuid, String subject, String body,
                            long timestamp, boolean read, String attachmentId, String attachmentName,
                            int attachmentCount, boolean attachmentClaimed) {}

    public MailScreen(Screen parent) {
        super(Component.literal("Mail"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.dataLoaded) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("mail_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.cursorBlink++;

        // Status timer
        if (this.statusTimer > 0) {
            this.statusTimer--;
            if (this.statusTimer <= 0) {
                this.statusMsg = "";
            }
        }

        // Poll for responses
        ComputerDataPayload resp = ComputerDataPayload.lastResponse;
        if (resp != null) {
            if (resp.dataType().equals("mail_data")) {
                ComputerDataPayload.lastResponse = null;
                this.parseMailData(resp.jsonData());
                this.dataLoaded = true;
                this.pollTimer = 0;
            } else if (resp.dataType().equals("mail_attach_data")) {
                ComputerDataPayload.lastResponse = null;
                this.parseAttachData(resp.jsonData());
            } else if (resp.dataType().equals("mail_send_result")) {
                ComputerDataPayload.lastResponse = null;
                this.parseSendResult(resp.jsonData());
            } else if (resp.dataType().equals("mail_claim_result")) {
                ComputerDataPayload.lastResponse = null;
                this.parseClaimResult(resp.jsonData());
            } else if ("error".equals(resp.dataType())) {
                // Consume error responses so the screen doesn't stay stuck
                ComputerDataPayload.lastResponse = null;
                this.dataLoaded = true;
            }
        }

        if (!this.dataLoaded && this.pollTimer >= 0) {
            this.pollTimer++;
        }
    }

    private void parseMailData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray arr = obj.getAsJsonArray("inbox");
            String prevId = (this.selectedMail >= 0 && this.selectedMail < this.inbox.size())
                    ? this.inbox.get(this.selectedMail).id : null;
            this.inbox.clear();
            for (JsonElement el : arr) {
                JsonObject m = el.getAsJsonObject();
                this.inbox.add(new MailEntry(
                    m.get("id").getAsString(),
                    m.get("from").getAsString(),
                    m.has("fromUuid") ? m.get("fromUuid").getAsString() : "",
                    m.get("subject").getAsString(),
                    m.get("body").getAsString(),
                    m.get("timestamp").getAsLong(),
                    m.get("read").getAsBoolean(),
                    m.has("attachmentId") ? m.get("attachmentId").getAsString() : "",
                    m.has("attachmentName") ? m.get("attachmentName").getAsString() : "",
                    m.has("attachmentCount") ? m.get("attachmentCount").getAsInt() : 0,
                    m.has("attachmentClaimed") ? m.get("attachmentClaimed").getAsBoolean() : false
                ));
            }
            // Restore selection
            if (prevId != null) {
                for (int i = 0; i < this.inbox.size(); i++) {
                    if (this.inbox.get(i).id.equals(prevId)) {
                        this.selectedMail = i;
                        return;
                    }
                }
            }
            if (this.selectedMail >= this.inbox.size()) {
                this.selectedMail = this.inbox.isEmpty() ? -1 : 0;
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void parseAttachData(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            this.composeAttachment = obj.has("itemId") ? obj.get("itemId").getAsString() : "";
            this.composeAttachmentName = obj.has("itemName") ? obj.get("itemName").getAsString() : "";
            if (this.composeAttachment.isEmpty() || this.composeAttachment.equals("minecraft:air")) {
                this.composeAttachment = "";
                this.composeAttachmentName = "";
                this.setStatus("No item in hand to attach");
            } else {
                this.setStatus("Attached: " + this.composeAttachmentName);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void parseSendResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.get("success").getAsBoolean();
            if (success) {
                this.setStatus("Mail sent successfully!");
                this.composing = false;
                this.composeTo = "";
                this.composeSubject = "";
                this.composeBody = "";
                this.composeAttachment = "";
                this.composeAttachmentName = "";
                this.focusedField = 0;
                this.cursorPos = 0;
                // Refresh inbox
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("mail_request", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            } else {
                String msg = obj.has("error") ? obj.get("error").getAsString() : "Failed to send mail";
                this.setStatus(msg);
            }
        } catch (Exception e) {
            this.setStatus("Failed to send mail");
        }
    }

    private void parseClaimResult(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.get("success").getAsBoolean();
            if (success) {
                this.setStatus("Item claimed!");
                // Refresh inbox
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("mail_request", ""),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
            } else {
                this.setStatus("Failed to claim item");
            }
        } catch (Exception e) {
            this.setStatus("Failed to claim item");
        }
    }

    private void setStatus(String msg) {
        this.statusMsg = msg;
        this.statusTimer = 80; // 4 seconds
    }

    // --- Rendering ---

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Full background
        g.fill(0, 0, this.width, this.height, BG_DARK);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, TITLE_BAR_H);
        Objects.requireNonNull(this.font);
        int titleY = (TITLE_BAR_H - 9) / 2;

        int unreadCount = 0;
        for (MailEntry m : this.inbox) {
            if (!m.read) unreadCount++;
        }
        String titleText = unreadCount > 0 ? "Mail (" + unreadCount + ")" : "Mail";
        UIHelper.drawCenteredTitle(g, this.font, titleText, this.width / 2, titleY);

        // Back button
        int backW = 50;
        int backH = 16;
        int backX = 8;
        int backY = (TITLE_BAR_H - backH) / 2;
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        // Toolbar
        int toolbarY = TITLE_BAR_H;
        this.renderToolbar(g, toolbarY, mouseX, mouseY);

        int contentTop = TITLE_BAR_H + TOOLBAR_H;
        int contentH = this.height - contentTop;

        // Left panel: mail list
        this.renderMailList(g, 0, contentTop, LIST_W, contentH, mouseX, mouseY);

        // Vertical divider
        g.fill(LIST_W, contentTop, LIST_W + 1, this.height, DIVIDER_COLOR);

        // Right panel
        int rightX = LIST_W + 1;
        int rightW = this.width - rightX;
        if (this.composing) {
            this.renderComposeView(g, rightX, contentTop, rightW, contentH, mouseX, mouseY);
        } else {
            this.renderDetailView(g, rightX, contentTop, rightW, contentH, mouseX, mouseY);
        }

        // Status message
        if (!this.statusMsg.isEmpty() && this.statusTimer > 0) {
            int msgW = this.font.width(this.statusMsg) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height - 20;
            UIHelper.drawCard(g, msgX, msgY, msgW, 16);
            g.drawCenteredString(this.font, this.statusMsg, this.width / 2, msgY + 4, UNREAD_DOT);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderToolbar(GuiGraphics g, int y, int mouseX, int mouseY) {
        g.fill(0, y, this.width, y + TOOLBAR_H, BG_SIDEBAR);
        g.fill(0, y + TOOLBAR_H - 1, this.width, y + TOOLBAR_H, DIVIDER_COLOR);

        int btnY = y + 2;
        int btnH = 16;
        int bx = 8;

        // Compose button
        int composeW = 60;
        boolean composeHover = mouseX >= bx && mouseX < bx + composeW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, bx, btnY, composeW, btnH, composeHover);
        String compLabel = "+ Compose";
        g.drawString(this.font, compLabel, bx + (composeW - this.font.width(compLabel)) / 2, btnY + (btnH - 9) / 2, UIHelper.CREAM_TEXT, false);
        bx += composeW + 4;

        // Refresh button
        int refreshW = 52;
        boolean refreshHover = mouseX >= bx && mouseX < bx + refreshW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, bx, btnY, refreshW, btnH, refreshHover);
        String refLabel = "Refresh";
        g.drawString(this.font, refLabel, bx + (refreshW - this.font.width(refLabel)) / 2, btnY + (btnH - 9) / 2, UIHelper.CREAM_TEXT, false);
        bx += refreshW + 4;

        // Delete All Read button
        int delW = 84;
        boolean delHover = mouseX >= bx && mouseX < bx + delW && mouseY >= btnY && mouseY < btnY + btnH;
        UIHelper.drawButton(g, bx, btnY, delW, btnH, delHover);
        String delLabel = "Delete Read";
        g.drawString(this.font, delLabel, bx + (delW - this.font.width(delLabel)) / 2, btnY + (btnH - 9) / 2, DELETE_COLOR, false);
    }

    private void renderMailList(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        g.fill(x, y, x + w, y + h, BG_SIDEBAR);

        if (this.inbox.isEmpty()) {
            String msg = this.dataLoaded ? "No mail" : "Loading...";
            int msgW = this.font.width(msg);
            g.drawString(this.font, msg, x + (w - msgW) / 2, y + h / 2 - 4, DIM_TEXT, false);
            return;
        }

        int listBottom = y + h;
        int visibleH = h;
        int totalH = this.inbox.size() * MAIL_ROW_H;
        int maxScroll = Math.max(0, totalH - visibleH);
        this.listScroll = Math.max(0, Math.min(this.listScroll, maxScroll));

        g.enableScissor(x, y, x + w, listBottom);
        for (int i = 0; i < this.inbox.size(); i++) {
            int rowY = y + i * MAIL_ROW_H - this.listScroll;
            if (rowY + MAIL_ROW_H < y || rowY > listBottom) continue;

            MailEntry mail = this.inbox.get(i);
            boolean selected = (i == this.selectedMail && !this.composing);
            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= rowY && mouseY < rowY + MAIL_ROW_H;

            // Background
            if (selected) {
                g.fill(x, rowY, x + w, rowY + MAIL_ROW_H, SELECTED_BG);
            } else if (hovered) {
                g.fill(x, rowY, x + w, rowY + MAIL_ROW_H, HOVER_BG);
            }

            int textX = x + 6;

            // Unread dot
            if (!mail.read) {
                g.fill(x + 2, rowY + 4, x + 5, rowY + 7, UNREAD_DOT);
            }

            // Sender name
            String sender = mail.from;
            if (this.font.width(sender) > w - 14) {
                while (this.font.width(sender + "..") > w - 14 && sender.length() > 1) {
                    sender = sender.substring(0, sender.length() - 1);
                }
                sender += "..";
            }
            int senderColor = mail.read ? DIM_TEXT : TEXT_COLOR;
            g.drawString(this.font, sender, textX, rowY + 3, senderColor, false);

            // Subject preview
            String subj = mail.subject.isEmpty() ? "(No subject)" : mail.subject;
            if (this.font.width(subj) > w - 12) {
                while (this.font.width(subj + "..") > w - 12 && subj.length() > 1) {
                    subj = subj.substring(0, subj.length() - 1);
                }
                subj += "..";
            }
            g.drawString(this.font, subj, textX, rowY + 13, DIM_TEXT, false);

            // Time ago
            String timeAgo = formatTimeAgo(mail.timestamp);
            int timeW = this.font.width(timeAgo);
            g.drawString(this.font, timeAgo, x + w - timeW - 4, rowY + 23, DIM_TEXT, false);

            // Attachment indicator
            if (!mail.attachmentId.isEmpty()) {
                g.drawString(this.font, "@", textX, rowY + 23, ATTACHMENT_COLOR, false);
            }

            // Bottom divider
            g.fill(x + 4, rowY + MAIL_ROW_H - 1, x + w - 4, rowY + MAIL_ROW_H, DIVIDER_COLOR);
        }
        g.disableScissor();

        // Scrollbar
        if (totalH > visibleH && maxScroll > 0) {
            float progress = (float) this.listScroll / (float) maxScroll;
            UIHelper.drawScrollbar(g, x + w - 8, y, visibleH, progress);
        }
    }

    private void renderDetailView(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        if (this.selectedMail < 0 || this.selectedMail >= this.inbox.size()) {
            String msg = "Select a mail to read";
            int msgW = this.font.width(msg);
            g.drawString(this.font, msg, x + (w - msgW) / 2, y + h / 2 - 4, DIM_TEXT, false);
            return;
        }

        MailEntry mail = this.inbox.get(this.selectedMail);
        int pad = 10;
        int innerX = x + pad;
        int innerW = w - pad * 2;
        int cy = y + pad;

        // From
        g.drawString(this.font, "From:", innerX, cy, DIM_TEXT, false);
        g.drawString(this.font, mail.from, innerX + this.font.width("From: "), cy, TEXT_COLOR, false);
        cy += LINE_HEIGHT;

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm");
        String dateStr = sdf.format(new Date(mail.timestamp));
        g.drawString(this.font, "Date:", innerX, cy, DIM_TEXT, false);
        g.drawString(this.font, dateStr, innerX + this.font.width("Date: "), cy, DIM_TEXT, false);
        cy += LINE_HEIGHT;

        // Subject (bold-like: draw twice offset)
        String subjectText = mail.subject.isEmpty() ? "(No subject)" : mail.subject;
        g.drawString(this.font, subjectText, innerX + 1, cy, TEXT_COLOR, false);
        g.drawString(this.font, subjectText, innerX, cy, TEXT_COLOR, false);
        cy += LINE_HEIGHT + 4;

        // Divider
        g.fill(innerX, cy, innerX + innerW, cy + 1, DIVIDER_COLOR);
        cy += 6;

        // Body area - scrollable
        int bodyTop = cy;
        int buttonsH = 50; // space for buttons at bottom
        int attachH = (!mail.attachmentId.isEmpty()) ? 30 : 0;
        int bodyBottom = y + h - buttonsH - attachH;
        if (bodyBottom <= bodyTop) bodyBottom = bodyTop + LINE_HEIGHT;
        int bodyH = bodyBottom - bodyTop;

        List<String> wrappedBody = wordWrapSimple(mail.body, innerW);
        int totalBodyH = wrappedBody.size() * LINE_HEIGHT;
        int maxDetailScroll = Math.max(0, totalBodyH - bodyH);
        this.detailScroll = Math.max(0, Math.min(this.detailScroll, maxDetailScroll));

        g.enableScissor(innerX, bodyTop, innerX + innerW, bodyBottom);
        for (int i = 0; i < wrappedBody.size(); i++) {
            int lineY = bodyTop + i * LINE_HEIGHT - this.detailScroll;
            if (lineY + LINE_HEIGHT < bodyTop || lineY > bodyBottom) continue;
            g.drawString(this.font, wrappedBody.get(i), innerX, lineY, TEXT_COLOR, false);
        }
        g.disableScissor();

        if (totalBodyH > bodyH && maxDetailScroll > 0) {
            float progress = (float) this.detailScroll / (float) maxDetailScroll;
            UIHelper.drawScrollbar(g, x + w - 10, bodyTop, bodyH, progress);
        }

        // Attachment display
        int attachY = bodyBottom + 4;
        if (!mail.attachmentId.isEmpty()) {
            g.fill(innerX, attachY, innerX + innerW, attachY + 1, DIVIDER_COLOR);
            attachY += 4;
            g.drawString(this.font, "Attachment:", innerX, attachY, ATTACHMENT_COLOR, false);
            String attachText = mail.attachmentName + " x" + mail.attachmentCount;
            g.drawString(this.font, attachText, innerX + this.font.width("Attachment: "), attachY, TEXT_COLOR, false);

            if (!mail.attachmentClaimed) {
                // Claim button
                int claimW = 44;
                int claimH = 14;
                int claimX = innerX + innerW - claimW;
                int claimY2 = attachY - 2;
                boolean claimHover = mouseX >= claimX && mouseX < claimX + claimW && mouseY >= claimY2 && mouseY < claimY2 + claimH;
                UIHelper.drawButton(g, claimX, claimY2, claimW, claimH, claimHover);
                String claimLabel = "Claim";
                g.drawString(this.font, claimLabel, claimX + (claimW - this.font.width(claimLabel)) / 2, claimY2 + (claimH - 9) / 2, SEND_COLOR, false);
            } else {
                g.drawString(this.font, "(Claimed)", innerX + innerW - this.font.width("(Claimed)"), attachY, DIM_TEXT, false);
            }
            attachY += 20;
        }

        // Reply and Delete buttons
        int btnBaseY = y + h - 30;
        int replyW = 50;
        int replyH = 16;
        int replyX = innerX;
        boolean replyHover = mouseX >= replyX && mouseX < replyX + replyW && mouseY >= btnBaseY && mouseY < btnBaseY + replyH;
        UIHelper.drawButton(g, replyX, btnBaseY, replyW, replyH, replyHover);
        g.drawString(this.font, "Reply", replyX + (replyW - this.font.width("Reply")) / 2, btnBaseY + (replyH - 9) / 2, UIHelper.CREAM_TEXT, false);

        int deleteW = 50;
        int deleteX = replyX + replyW + 8;
        boolean deleteHover = mouseX >= deleteX && mouseX < deleteX + deleteW && mouseY >= btnBaseY && mouseY < btnBaseY + replyH;
        UIHelper.drawButton(g, deleteX, btnBaseY, deleteW, replyH, deleteHover);
        g.drawString(this.font, "Delete", deleteX + (deleteW - this.font.width("Delete")) / 2, btnBaseY + (replyH - 9) / 2, DELETE_COLOR, false);
    }

    private void renderComposeView(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        int pad = 10;
        int innerX = x + pad;
        int innerW = w - pad * 2;
        int cy = y + pad;

        // "To:" field
        this.renderTextField(g, innerX, cy, innerW, "To:", this.composeTo, this.focusedField == 0, mouseX, mouseY);
        cy += FIELD_H + FIELD_GAP;

        // "Subject:" field
        this.renderTextField(g, innerX, cy, innerW, "Subject:", this.composeSubject, this.focusedField == 1, mouseX, mouseY);
        cy += FIELD_H + FIELD_GAP;

        // Body label
        g.drawString(this.font, "Message:", innerX, cy, DIM_TEXT, false);
        cy += LINE_HEIGHT;

        // Body text area
        int bodyBottom = y + h - 50;
        int bodyH = bodyBottom - cy;
        if (bodyH < LINE_HEIGHT * 2) bodyH = LINE_HEIGHT * 2;
        int bodyAreaBottom = cy + bodyH;

        // Body background
        int borderColor = (this.focusedField == 2) ? FIELD_FOCUS_BORDER : FIELD_BORDER;
        g.fill(innerX - 1, cy - 1, innerX + innerW + 1, bodyAreaBottom + 1, borderColor);
        g.fill(innerX, cy, innerX + innerW, bodyAreaBottom, BG_COMPOSE);

        // Word wrap body and render
        List<String> wrappedBody = wordWrapSimple(this.composeBody, innerW - 6);
        if (wrappedBody.isEmpty()) {
            wrappedBody.add("");
        }
        int totalBodyH = wrappedBody.size() * LINE_HEIGHT;
        // We don't scroll compose body for simplicity; auto-scroll to cursor
        int composeBodyScroll = 0;
        if (this.focusedField == 2) {
            // Find cursor line
            int charsSoFar = 0;
            int cursorLine = 0;
            for (int i = 0; i < wrappedBody.size(); i++) {
                int lineLen = wrappedBody.get(i).length();
                if (this.cursorPos <= charsSoFar + lineLen) {
                    cursorLine = i;
                    break;
                }
                charsSoFar += lineLen;
                if (i < wrappedBody.size() - 1) cursorLine = i + 1;
            }
            int cursorVisualY = cursorLine * LINE_HEIGHT;
            if (cursorVisualY + LINE_HEIGHT > bodyH) {
                composeBodyScroll = cursorVisualY + LINE_HEIGHT - bodyH;
            }
        }

        g.enableScissor(innerX + 3, cy, innerX + innerW - 3, bodyAreaBottom);
        for (int i = 0; i < wrappedBody.size(); i++) {
            int lineY = cy + 2 + i * LINE_HEIGHT - composeBodyScroll;
            if (lineY + LINE_HEIGHT < cy || lineY > bodyAreaBottom) continue;
            g.drawString(this.font, wrappedBody.get(i), innerX + 3, lineY, TEXT_COLOR, false);
        }

        // Cursor in body
        if (this.focusedField == 2 && (this.cursorBlink / 10) % 2 == 0) {
            int charsSoFar = 0;
            for (int i = 0; i < wrappedBody.size(); i++) {
                int lineLen = wrappedBody.get(i).length();
                if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                    String sub = wrappedBody.get(i).substring(0, this.cursorPos - charsSoFar);
                    int cxPos = innerX + 3 + this.font.width(sub);
                    int cyPos = cy + 2 + i * LINE_HEIGHT - composeBodyScroll;
                    g.fill(cxPos, cyPos - 1, cxPos + 1, cyPos + 10, CURSOR_COLOR);
                    break;
                }
                charsSoFar += lineLen;
            }
        }
        g.disableScissor();

        // Placeholder
        if (this.composeBody.isEmpty() && this.focusedField != 2) {
            g.drawString(this.font, "Write your message...", innerX + 3, cy + 2, DIM_TEXT, false);
        }

        // Bottom area: attachment + buttons
        int bottomY = bodyAreaBottom + 6;

        // Attachment display / button
        int attachBtnW = 80;
        int attachBtnH = 16;
        int attachBtnX = innerX;
        boolean attachHover = mouseX >= attachBtnX && mouseX < attachBtnX + attachBtnW && mouseY >= bottomY && mouseY < bottomY + attachBtnH;
        UIHelper.drawButton(g, attachBtnX, bottomY, attachBtnW, attachBtnH, attachHover);
        String attachLabel = this.composeAttachment.isEmpty() ? "Attach Item" : "Change Item";
        g.drawString(this.font, attachLabel, attachBtnX + (attachBtnW - this.font.width(attachLabel)) / 2, bottomY + (attachBtnH - 9) / 2, ATTACHMENT_COLOR, false);

        if (!this.composeAttachment.isEmpty()) {
            g.drawString(this.font, this.composeAttachmentName, attachBtnX + attachBtnW + 6, bottomY + (attachBtnH - 9) / 2, TEXT_COLOR, false);
            // Remove attachment button
            int rmX = attachBtnX + attachBtnW + 6 + this.font.width(this.composeAttachmentName) + 4;
            boolean rmHover = mouseX >= rmX && mouseX < rmX + 12 && mouseY >= bottomY && mouseY < bottomY + attachBtnH;
            g.drawString(this.font, "x", rmX + 2, bottomY + (attachBtnH - 9) / 2, rmHover ? 0xFFFF4444 : DELETE_COLOR, false);
        }

        // Send and Cancel buttons
        int sendW = 50;
        int cancelW = 50;
        int btnH = 16;
        int sendX = innerX + innerW - sendW;
        int cancelX = sendX - cancelW - 6;
        boolean sendHover = mouseX >= sendX && mouseX < sendX + sendW && mouseY >= bottomY && mouseY < bottomY + btnH;
        boolean cancelHover = mouseX >= cancelX && mouseX < cancelX + cancelW && mouseY >= bottomY && mouseY < bottomY + btnH;

        UIHelper.drawButton(g, sendX, bottomY, sendW, btnH, sendHover);
        g.drawString(this.font, "Send", sendX + (sendW - this.font.width("Send")) / 2, bottomY + (btnH - 9) / 2, SEND_COLOR, false);

        UIHelper.drawButton(g, cancelX, bottomY, cancelW, btnH, cancelHover);
        g.drawString(this.font, "Cancel", cancelX + (cancelW - this.font.width("Cancel")) / 2, bottomY + (btnH - 9) / 2, DELETE_COLOR, false);
    }

    private void renderTextField(GuiGraphics g, int x, int y, int w, String label, String value, boolean focused, int mouseX, int mouseY) {
        int labelW = this.font.width(label) + 4;
        g.drawString(this.font, label, x, y + (FIELD_H - 9) / 2, DIM_TEXT, false);

        int fieldX = x + labelW;
        int fieldW = w - labelW;
        int borderColor = focused ? FIELD_FOCUS_BORDER : FIELD_BORDER;
        g.fill(fieldX - 1, y - 1, fieldX + fieldW + 1, y + FIELD_H + 1, borderColor);
        g.fill(fieldX, y, fieldX + fieldW, y + FIELD_H, FIELD_BG);

        // Clip text to field
        g.enableScissor(fieldX + 2, y, fieldX + fieldW - 2, y + FIELD_H);
        int textY = y + (FIELD_H - 9) / 2;

        if (focused) {
            int cp = Math.min(this.cursorPos, value.length());
            String before = value.substring(0, cp);
            String after = value.substring(cp);

            // Ensure cursor is visible - scroll text if needed
            int beforeW = this.font.width(before);
            int maxVisibleW = fieldW - 6;
            int textOffset = 0;
            if (beforeW > maxVisibleW) {
                textOffset = beforeW - maxVisibleW;
            }

            g.drawString(this.font, before, fieldX + 3 - textOffset, textY, TEXT_COLOR, false);
            int cursorX = fieldX + 3 + beforeW - textOffset;
            g.drawString(this.font, after, cursorX, textY, TEXT_COLOR, false);

            if ((this.cursorBlink / 10) % 2 == 0) {
                g.fill(cursorX, textY - 1, cursorX + 1, textY + 10, CURSOR_COLOR);
            }
        } else {
            if (value.isEmpty()) {
                String placeholder = label.equals("To:") ? "Player name" : "Subject";
                g.drawString(this.font, placeholder, fieldX + 3, textY, DIM_TEXT, false);
            } else {
                g.drawString(this.font, value, fieldX + 3, textY, TEXT_COLOR, false);
            }
        }
        g.disableScissor();
    }

    // --- Word wrap ---

    private List<String> wordWrapSimple(String content, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (content.isEmpty()) {
            result.add("");
            return result;
        }

        String[] rawLines = content.split("\n", -1);
        for (String rawLine : rawLines) {
            if (rawLine.isEmpty()) {
                result.add("");
                continue;
            }
            int pos = 0;
            while (pos < rawLine.length()) {
                String sub = rawLine.substring(pos);
                if (this.font.width(sub) <= maxWidth) {
                    result.add(sub);
                    break;
                }
                // Binary search for break point
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
                int wordBreak = rawLine.lastIndexOf(' ', breakAt - 1);
                if (wordBreak > pos) {
                    breakAt = wordBreak + 1;
                }
                result.add(rawLine.substring(pos, breakAt));
                pos = breakAt;
            }
        }
        return result;
    }

    // --- Time formatting ---

    private static String formatTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 0) return "now";
        long seconds = diff / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        if (days < 30) return days + "d";
        long months = days / 30;
        return months + "mo";
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
            if (this.composing) {
                this.composing = false;
                this.focusedField = 0;
                this.cursorPos = 0;
            } else {
                Minecraft.getInstance().setScreen(this.parent);
            }
            return true;
        }

        // Toolbar buttons
        int toolbarY = TITLE_BAR_H;
        int btnY = toolbarY + 2;
        int btnH = 16;
        int bx = 8;

        // Compose
        int composeW = 60;
        if (mx >= bx && mx < bx + composeW && my >= btnY && my < btnY + btnH) {
            this.composing = true;
            this.composeTo = "";
            this.composeSubject = "";
            this.composeBody = "";
            this.composeAttachment = "";
            this.composeAttachmentName = "";
            this.focusedField = 0;
            this.cursorPos = 0;
            return true;
        }
        bx += composeW + 4;

        // Refresh
        int refreshW = 52;
        if (mx >= bx && mx < bx + refreshW && my >= btnY && my < btnY + btnH) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("mail_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            this.setStatus("Refreshing...");
            return true;
        }
        bx += refreshW + 4;

        // Delete All Read
        int delW = 84;
        if (mx >= bx && mx < bx + delW && my >= btnY && my < btnY + btnH) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("mail_delete_read", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            this.setStatus("Deleting read mail...");
            // Refresh after short delay by requesting data
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("mail_request", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return true;
        }

        int contentTop = TITLE_BAR_H + TOOLBAR_H;
        int contentH = this.height - contentTop;

        // Mail list click
        if (mx >= 0 && mx < LIST_W && my >= contentTop && my < this.height) {
            int relY = my - contentTop + this.listScroll;
            int index = relY / MAIL_ROW_H;
            if (index >= 0 && index < this.inbox.size()) {
                this.selectedMail = index;
                this.detailScroll = 0;
                this.composing = false;
                // Mark as read
                MailEntry mail = this.inbox.get(index);
                if (!mail.read) {
                    ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new ComputerActionPayload("mail_read", mail.id),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]
                    );
                    // Update local state
                    this.inbox.set(index, new MailEntry(
                        mail.id, mail.from, mail.fromUuid, mail.subject, mail.body,
                        mail.timestamp, true, mail.attachmentId, mail.attachmentName,
                        mail.attachmentCount, mail.attachmentClaimed
                    ));
                }
            }
            return true;
        }

        // Right panel clicks
        int rightX = LIST_W + 1;
        int rightW = this.width - rightX;

        if (this.composing) {
            return this.handleComposeClick(mx, my, rightX, contentTop, rightW, contentH);
        } else {
            return this.handleDetailClick(mx, my, rightX, contentTop, rightW, contentH);
        }
    }

    private boolean handleComposeClick(int mx, int my, int x, int y, int w, int h) {
        int pad = 10;
        int innerX = x + pad;
        int innerW = w - pad * 2;
        int cy = y + pad;

        int labelWTo = this.font.width("To:") + 4;
        int labelWSub = this.font.width("Subject:") + 4;

        // To field click
        int toFieldX = innerX + labelWTo;
        int toFieldW = innerW - labelWTo;
        if (mx >= toFieldX && mx < toFieldX + toFieldW && my >= cy && my < cy + FIELD_H) {
            this.focusedField = 0;
            this.cursorPos = Math.min(this.cursorPos, this.composeTo.length());
            int clickX = mx - toFieldX - 3;
            this.cursorPos = getCharIndexAtX(this.composeTo, clickX);
            this.cursorBlink = 0;
            return true;
        }
        cy += FIELD_H + FIELD_GAP;

        // Subject field click
        int subFieldX = innerX + labelWSub;
        int subFieldW = innerW - labelWSub;
        if (mx >= subFieldX && mx < subFieldX + subFieldW && my >= cy && my < cy + FIELD_H) {
            this.focusedField = 1;
            this.cursorPos = Math.min(this.cursorPos, this.composeSubject.length());
            int clickX = mx - subFieldX - 3;
            this.cursorPos = getCharIndexAtX(this.composeSubject, clickX);
            this.cursorBlink = 0;
            return true;
        }
        cy += FIELD_H + FIELD_GAP;

        // Body label
        cy += LINE_HEIGHT;

        // Body area click
        int bodyBottom = y + h - 50;
        int bodyH = bodyBottom - cy;
        if (bodyH < LINE_HEIGHT * 2) bodyH = LINE_HEIGHT * 2;
        int bodyAreaBottom = cy + bodyH;

        if (mx >= innerX && mx < innerX + innerW && my >= cy && my < bodyAreaBottom) {
            this.focusedField = 2;
            // Find cursor position from click
            List<String> wrapped = wordWrapSimple(this.composeBody, innerW - 6);
            if (wrapped.isEmpty()) wrapped.add("");
            int clickRelY = my - cy - 2;
            int lineIdx = clickRelY / LINE_HEIGHT;
            lineIdx = Math.max(0, Math.min(lineIdx, wrapped.size() - 1));
            int charsSoFar = 0;
            for (int i = 0; i < lineIdx; i++) {
                charsSoFar += wrapped.get(i).length();
            }
            int clickX = mx - innerX - 3;
            int charInLine = getCharIndexAtX(wrapped.get(lineIdx), clickX);
            this.cursorPos = charsSoFar + charInLine;
            this.cursorBlink = 0;
            return true;
        }

        // Bottom area buttons
        int bottomY = bodyAreaBottom + 6;
        int btnH2 = 16;

        // Attach Item button
        int attachBtnW = 80;
        if (mx >= innerX && mx < innerX + attachBtnW && my >= bottomY && my < bottomY + btnH2) {
            // Request current held item info from server
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("mail_attach_check", ""),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            return true;
        }

        // Remove attachment "x" button
        if (!this.composeAttachment.isEmpty()) {
            int rmX = innerX + attachBtnW + 6 + this.font.width(this.composeAttachmentName) + 4;
            if (mx >= rmX && mx < rmX + 12 && my >= bottomY && my < bottomY + btnH2) {
                this.composeAttachment = "";
                this.composeAttachmentName = "";
                this.setStatus("Attachment removed");
                return true;
            }
        }

        // Send button
        int sendW = 50;
        int sendX = innerX + innerW - sendW;
        if (mx >= sendX && mx < sendX + sendW && my >= bottomY && my < bottomY + btnH2) {
            this.sendMail();
            return true;
        }

        // Cancel button
        int cancelW = 50;
        int cancelX = sendX - cancelW - 6;
        if (mx >= cancelX && mx < cancelX + cancelW && my >= bottomY && my < bottomY + btnH2) {
            this.composing = false;
            this.composeTo = "";
            this.composeSubject = "";
            this.composeBody = "";
            this.composeAttachment = "";
            this.composeAttachmentName = "";
            this.focusedField = 0;
            this.cursorPos = 0;
            return true;
        }

        return false;
    }

    private boolean handleDetailClick(int mx, int my, int x, int y, int w, int h) {
        if (this.selectedMail < 0 || this.selectedMail >= this.inbox.size()) return false;
        MailEntry mail = this.inbox.get(this.selectedMail);
        int pad = 10;
        int innerX = x + pad;
        int innerW = w - pad * 2;

        // Claim button
        if (!mail.attachmentId.isEmpty() && !mail.attachmentClaimed) {
            int claimW = 44;
            int claimH = 14;
            int claimX = innerX + innerW - claimW;
            // Compute attachY: same as in renderDetailView
            int cy2 = y + pad;
            cy2 += LINE_HEIGHT; // from
            cy2 += LINE_HEIGHT; // date
            cy2 += LINE_HEIGHT + 4; // subject
            cy2 += 6; // divider
            int bodyTop = cy2;
            int buttonsHSpace = 50;
            int attachHSpace = 30;
            int bodyBottom = y + h - buttonsHSpace - attachHSpace;
            if (bodyBottom <= bodyTop) bodyBottom = bodyTop + LINE_HEIGHT;
            int attachY = bodyBottom + 4 + 4; // divider gap
            int claimY2 = attachY - 2;
            if (mx >= claimX && mx < claimX + claimW && my >= claimY2 && my < claimY2 + claimH) {
                ClientPacketDistributor.sendToServer(
                    (CustomPacketPayload) new ComputerActionPayload("mail_claim", mail.id),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]
                );
                return true;
            }
        }

        // Reply button
        int btnBaseY = y + h - 30;
        int replyW = 50;
        int replyH = 16;
        int replyX = innerX;
        if (mx >= replyX && mx < replyX + replyW && my >= btnBaseY && my < btnBaseY + replyH) {
            this.composing = true;
            this.composeTo = mail.from;
            this.composeSubject = mail.subject.startsWith("Re: ") ? mail.subject : "Re: " + mail.subject;
            this.composeBody = "";
            this.composeAttachment = "";
            this.composeAttachmentName = "";
            this.focusedField = 2;
            this.cursorPos = 0;
            return true;
        }

        // Delete button
        int deleteW = 50;
        int deleteX = replyX + replyW + 8;
        if (mx >= deleteX && mx < deleteX + deleteW && my >= btnBaseY && my < btnBaseY + replyH) {
            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new ComputerActionPayload("mail_delete", mail.id),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );
            this.inbox.remove(this.selectedMail);
            if (this.selectedMail >= this.inbox.size()) {
                this.selectedMail = this.inbox.size() - 1;
            }
            this.detailScroll = 0;
            this.setStatus("Mail deleted");
            return true;
        }

        return false;
    }

    private int getCharIndexAtX(String text, int targetX) {
        if (text.isEmpty()) return 0;
        for (int i = 0; i <= text.length(); i++) {
            int w = this.font.width(text.substring(0, i));
            if (w > targetX) {
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
        int contentTop = TITLE_BAR_H + TOOLBAR_H;
        if (mx < LIST_W && (int) mouseY >= contentTop) {
            this.listScroll -= (int) (scrollY * MAIL_ROW_H);
            this.listScroll = Math.max(0, this.listScroll);
            return true;
        }
        if (!this.composing) {
            this.detailScroll -= (int) (scrollY * LINE_HEIGHT * 3);
            this.detailScroll = Math.max(0, this.detailScroll);
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        // Escape
        if (keyCode == 256) {
            if (this.composing) {
                this.composing = false;
                this.focusedField = 0;
                this.cursorPos = 0;
            } else {
                Minecraft.getInstance().setScreen(this.parent);
            }
            return true;
        }

        if (!this.composing) {
            return super.keyPressed(event);
        }

        // Tab cycles focus
        if (keyCode == 258) {
            this.focusedField = (this.focusedField + 1) % 3;
            String currentText = getFieldText();
            this.cursorPos = currentText.length();
            this.cursorBlink = 0;
            return true;
        }

        String text = getFieldText();
        boolean handled = false;

        switch (keyCode) {
            case 257: // Enter
                if (this.focusedField == 2) {
                    // Insert newline in body
                    if (this.composeBody.length() < 1000) {
                        int cp = Math.min(this.cursorPos, this.composeBody.length());
                        this.composeBody = this.composeBody.substring(0, cp) + "\n" + this.composeBody.substring(cp);
                        this.cursorPos = cp + 1;
                    }
                } else if (this.focusedField == 0 || this.focusedField == 1) {
                    // Move to next field
                    this.focusedField++;
                    this.cursorPos = getFieldText().length();
                }
                handled = true;
                break;
            case 259: // Backspace
                if (this.cursorPos > 0 && !text.isEmpty()) {
                    int cp = Math.min(this.cursorPos, text.length());
                    String newText = text.substring(0, cp - 1) + text.substring(cp);
                    setFieldText(newText);
                    this.cursorPos = cp - 1;
                }
                handled = true;
                break;
            case 261: // Delete
                if (this.cursorPos < text.length()) {
                    String newText = text.substring(0, this.cursorPos) + text.substring(this.cursorPos + 1);
                    setFieldText(newText);
                }
                handled = true;
                break;
            case 263: // Left
                if (this.cursorPos > 0) this.cursorPos--;
                handled = true;
                break;
            case 262: // Right
                if (this.cursorPos < text.length()) this.cursorPos++;
                handled = true;
                break;
            case 265: // Up
                if (this.focusedField == 2) {
                    this.moveCursorUpDown(-1);
                } else if (this.focusedField > 0) {
                    this.focusedField--;
                    this.cursorPos = Math.min(this.cursorPos, getFieldText().length());
                }
                handled = true;
                break;
            case 264: // Down
                if (this.focusedField == 2) {
                    this.moveCursorUpDown(1);
                } else if (this.focusedField < 2) {
                    this.focusedField++;
                    this.cursorPos = Math.min(this.cursorPos, getFieldText().length());
                }
                handled = true;
                break;
            case 268: // Home
                if (this.focusedField == 2) {
                    moveCursorToLineStartCompose();
                } else {
                    this.cursorPos = 0;
                }
                handled = true;
                break;
            case 269: // End
                if (this.focusedField == 2) {
                    moveCursorToLineEndCompose();
                } else {
                    this.cursorPos = text.length();
                }
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
        if (!this.composing) return false;
        if (codePoint < 32 && codePoint != '\t') return false;

        String text = getFieldText();
        int maxLen;
        switch (this.focusedField) {
            case 0: maxLen = 64; break;
            case 1: maxLen = 100; break;
            default: maxLen = 1000; break;
        }

        if (text.length() >= maxLen) return true;
        int cp = Math.min(this.cursorPos, text.length());
        String newText = text.substring(0, cp) + codePoint + text.substring(cp);
        setFieldText(newText);
        this.cursorPos = cp + 1;
        this.cursorBlink = 0;
        return true;
    }

    private String getFieldText() {
        switch (this.focusedField) {
            case 0: return this.composeTo;
            case 1: return this.composeSubject;
            case 2: return this.composeBody;
            default: return "";
        }
    }

    private void setFieldText(String text) {
        switch (this.focusedField) {
            case 0: this.composeTo = text; break;
            case 1: this.composeSubject = text; break;
            case 2: this.composeBody = text; break;
        }
    }

    private void moveCursorUpDown(int direction) {
        int rightX = LIST_W + 1;
        int pad = 10;
        int innerW = this.width - rightX - pad * 2 - 6;
        List<String> wrapped = wordWrapSimple(this.composeBody, innerW);
        if (wrapped.isEmpty()) return;

        // Find current line
        int currentLine = 0;
        int charsSoFar = 0;
        int offsetInLine = 0;
        for (int i = 0; i < wrapped.size(); i++) {
            int lineLen = wrapped.get(i).length();
            if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                currentLine = i;
                offsetInLine = this.cursorPos - charsSoFar;
                break;
            }
            charsSoFar += lineLen;
        }

        int xPixel = this.font.width(wrapped.get(currentLine).substring(0, offsetInLine));
        int targetLine = currentLine + direction;

        if (targetLine < 0) {
            // Move to subject field
            this.focusedField = 1;
            this.cursorPos = Math.min(this.cursorPos, this.composeSubject.length());
            return;
        }
        if (targetLine >= wrapped.size()) {
            this.cursorPos = this.composeBody.length();
            return;
        }

        // Find position on target line
        charsSoFar = 0;
        for (int i = 0; i < targetLine; i++) {
            charsSoFar += wrapped.get(i).length();
        }
        int charInTarget = getCharIndexAtX(wrapped.get(targetLine), xPixel);
        this.cursorPos = charsSoFar + charInTarget;
    }

    private void moveCursorToLineStartCompose() {
        int rightX = LIST_W + 1;
        int pad = 10;
        int innerW = this.width - rightX - pad * 2 - 6;
        List<String> wrapped = wordWrapSimple(this.composeBody, innerW);
        int charsSoFar = 0;
        for (String line : wrapped) {
            int lineLen = line.length();
            if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                this.cursorPos = charsSoFar;
                return;
            }
            charsSoFar += lineLen;
        }
    }

    private void moveCursorToLineEndCompose() {
        int rightX = LIST_W + 1;
        int pad = 10;
        int innerW = this.width - rightX - pad * 2 - 6;
        List<String> wrapped = wordWrapSimple(this.composeBody, innerW);
        int charsSoFar = 0;
        for (String line : wrapped) {
            int lineLen = line.length();
            if (this.cursorPos >= charsSoFar && this.cursorPos <= charsSoFar + lineLen) {
                this.cursorPos = charsSoFar + lineLen;
                return;
            }
            charsSoFar += lineLen;
        }
    }

    private void sendMail() {
        if (this.composeTo.trim().isEmpty()) {
            this.setStatus("Please enter a recipient");
            return;
        }
        if (this.composeSubject.trim().isEmpty() && this.composeBody.trim().isEmpty()) {
            this.setStatus("Cannot send empty mail");
            return;
        }

        String json = "{\"to\":\"" + escapeJson(this.composeTo.trim())
                + "\",\"subject\":\"" + escapeJson(this.composeSubject)
                + "\",\"body\":\"" + escapeJson(this.composeBody)
                + "\",\"attachItem\":" + (!this.composeAttachment.isEmpty()) + "}";

        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("mail_send", json),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.setStatus("Sending...");
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
}
