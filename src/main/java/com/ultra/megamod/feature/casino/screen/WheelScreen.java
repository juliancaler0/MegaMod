package com.ultra.megamod.feature.casino.screen;

import com.ultra.megamod.feature.casino.network.WheelBetPayload;
import com.ultra.megamod.feature.casino.network.WheelSyncPayload;
import com.ultra.megamod.feature.casino.wheel.WheelSegment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.casino.CasinoClientEvents;
import com.ultra.megamod.feature.casino.chips.ChipRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WheelScreen extends Screen {

    private final BlockPos wheelPos;
    private String phase = "BETTING";
    private int timer = 0;
    private int maxTimer = 600;
    private String resultDisplay = "";
    private int totalPot = 0;

    private final List<BetEntry> activeBets = new ArrayList<>();
    record BetEntry(String player, String segment, int amount, int color) {}

    private int selectedSegment = 0;
    private String betInputText = "10";
    private boolean betInputFocused = false;
    private int cursorBlink = 0;

    private static final int PANEL_W = 160;
    private static final int BG = 0xAA000000;
    private static final int BORDER = 0xFFD4AF37;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int GOLD = 0xFFD4AF37;
    private static final int DIM = 0xFF888899;

    public WheelScreen(BlockPos wheelPos) {
        super(Component.literal("Wheel"));
        this.wheelPos = wheelPos;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void tick() {
        super.tick();
        cursorBlink++;
        WheelSyncPayload sync = WheelSyncPayload.lastSync;
        if (sync != null) {
            WheelSyncPayload.lastSync = null;
            parseSync(sync.wheelStateJson());
        }
    }

    private void parseSync(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            this.phase = root.has("phase") ? root.get("phase").getAsString() : "BETTING";
            this.timer = root.has("timer") ? root.get("timer").getAsInt() : 0;
            this.maxTimer = root.has("maxTimer") ? root.get("maxTimer").getAsInt() : 600;
            this.totalPot = 0;
            if (root.has("resultDisplay")) this.resultDisplay = root.get("resultDisplay").getAsString();
            if (root.has("bets")) {
                for (var e : root.getAsJsonObject("bets").entrySet()) totalPot += e.getValue().getAsInt();
            }
            activeBets.clear();
            if (root.has("activeBets")) {
                for (JsonElement el : root.getAsJsonArray("activeBets")) {
                    JsonObject b = el.getAsJsonObject();
                    activeBets.add(new BetEntry(
                            b.get("player").getAsString(), b.get("segment").getAsString(),
                            b.get("amount").getAsInt(), b.has("color") ? b.get("color").getAsInt() : 0xFFFFFFFF));
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int rx = this.width - PANEL_W - 10;
        int ty = 10;
        int ph = Math.min(this.height - 20, 320);

        // Panel background
        g.fill(rx, ty, rx + PANEL_W, ty + ph, BG);
        g.fill(rx, ty, rx + PANEL_W, ty + 1, BORDER);
        g.fill(rx, ty + ph - 1, rx + PANEL_W, ty + ph, BORDER);
        g.fill(rx, ty, rx + 1, ty + ph, BORDER);
        g.fill(rx + PANEL_W - 1, ty, rx + PANEL_W, ty + ph, BORDER);

        g.drawString(this.font, "WHEEL OF FORTUNE", rx + 6, ty + 4, GOLD, false);

        int sec = timer / 20;
        String ps = switch (phase) {
            case "BETTING" -> "Betting: " + sec + "s";
            case "SPINNING" -> "Spinning...";
            case "RESULT" -> "Result: " + resultDisplay;
            case "COOLDOWN" -> "Next round: " + sec + "s";
            default -> phase;
        };
        g.drawString(this.font, ps, rx + 6, ty + 16, TEXT, false);

        // Timer bar
        int by = ty + 27;
        g.fill(rx + 6, by, rx + PANEL_W - 6, by + 4, 0xFF333333);
        if (maxTimer > 0) {
            int bw = (int) ((PANEL_W - 12) * ((float) timer / maxTimer));
            g.fill(rx + 6, by, rx + 6 + bw, by + 4, "BETTING".equals(phase) ? 0xFF4CAF50 : 0xFFFF9800);
        }

        g.drawString(this.font, "Pot: " + totalPot + " MC", rx + 6, by + 8, DIM, false);
        // Wallet balance
        int wallet = ChipRenderer.clientChipTotal;
        String walletStr = "Chips: " + wallet + " MC";
        int walletW = this.font.width(walletStr);
        g.drawString(this.font, walletStr, rx + PANEL_W - walletW - 6, by + 8, GOLD, false);
        int y = by + 22;

        // Active bets
        g.drawString(this.font, "Active Bets:", rx + 6, y, GOLD, false);
        y += 12;
        if (activeBets.isEmpty()) {
            g.drawString(this.font, "No bets yet", rx + 10, y, DIM, false);
            y += 12;
        } else {
            int max = Math.min(activeBets.size(), 8);
            for (int i = 0; i < max; i++) {
                BetEntry be = activeBets.get(i);
                g.fill(rx + 8, y + 1, rx + 12, y + 7, be.color | 0xFF000000);
                String s = be.player + ": " + be.amount + " on " + be.segment;
                if (this.font.width(s) > PANEL_W - 22)
                    s = be.player.substring(0, Math.min(6, be.player.length())) + ":" + be.amount + " " + be.segment;
                g.drawString(this.font, s, rx + 15, y, TEXT, false);
                y += 11;
            }
            if (activeBets.size() > 8) { g.drawString(this.font, "+" + (activeBets.size() - 8) + " more", rx + 10, y, DIM, false); y += 11; }
        }
        y += 4;

        // Betting controls
        if ("BETTING".equals(phase)) {
            g.fill(rx + 4, y, rx + PANEL_W - 4, y + 1, BORDER);
            y += 4;
            g.drawString(this.font, "Place Bet:", rx + 6, y, GOLD, false);
            y += 12;

            WheelSegment[] segs = WheelSegment.values();
            for (int i = 0; i < segs.length; i++) {
                boolean sel = i == selectedSegment;
                if (sel) g.fill(rx + 6, y, rx + PANEL_W - 6, y + 11, 0x44D4AF37);
                g.fill(rx + 8, y + 2, rx + 12, y + 8, segs[i].color | 0xFF000000);
                boolean hov = mouseX >= rx + 6 && mouseX < rx + PANEL_W - 6 && mouseY >= y && mouseY < y + 11;
                g.drawString(this.font, segs[i].displayName + " (" + segs[i].multiplier + "x)",
                        rx + 15, y + 1, sel ? GOLD : (hov ? 0xFFFFFFFF : TEXT), false);
                y += 11;
            }
            y += 4;

            g.drawString(this.font, "Amt:", rx + 6, y, DIM, false);
            int ix = rx + 32; int iw = 50;
            g.fill(ix, y - 1, ix + iw, y + 10, betInputFocused ? 0xFF333355 : 0xFF222233);
            g.drawString(this.font, betInputText + (betInputFocused && (cursorBlink / 10) % 2 == 0 ? "|" : ""),
                    ix + 3, y, 0xFF00FF00, false);

            int bx = ix + iw + 4; int bw = 40;
            boolean bh = mouseX >= bx && mouseX < bx + bw && mouseY >= y - 1 && mouseY < y + 10;
            g.fill(bx, y - 1, bx + bw, y + 10, bh ? 0xFF4CAF50 : 0xFF2E7D32);
            g.drawString(this.font, "BET", bx + 12, y, 0xFFFFFFFF, false);
        }

        g.drawString(this.font, "ESC close | F reopen", rx + 6, ty + ph - 12, DIM, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x(); int my = (int) event.y();
        if (!"BETTING".equals(phase)) return super.mouseClicked(event, consumed);

        int rx = this.width - PANEL_W - 10;
        int ty = 10; int by = ty + 27;
        int y = by + 22 + 12;
        int betsH = activeBets.isEmpty() ? 12 : Math.min(activeBets.size(), 8) * 11 + (activeBets.size() > 8 ? 11 : 0);
        y += betsH + 8 + 12;

        WheelSegment[] segs = WheelSegment.values();
        for (int i = 0; i < segs.length; i++) {
            if (mx >= rx + 6 && mx < rx + PANEL_W - 6 && my >= y && my < y + 11) { selectedSegment = i; return true; }
            y += 11;
        }
        y += 4;

        int ix = rx + 32; int iw = 50;
        if (mx >= ix && mx < ix + iw && my >= y - 1 && my < y + 10) { betInputFocused = true; return true; }
        int bx = ix + iw + 4; int bw = 40;
        if (mx >= bx && mx < bx + bw && my >= y - 1 && my < y + 10) { placeBet(); return true; }

        betInputFocused = false;
        return super.mouseClicked(event, consumed);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char ch = (char) event.codepoint();
        int modifiers = event.modifiers();
        if (betInputFocused && Character.isDigit(ch) && betInputText.length() < 7) {
            if (betInputText.equals("0")) betInputText = "";
            betInputText += ch;
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256) { this.onClose(); return true; }
        if (betInputFocused) {
            // Digit keys: 0-9 on main keyboard (48-57) and numpad (320-329)
            int digit = -1;
            if (keyCode >= 48 && keyCode <= 57) digit = keyCode - 48;
            else if (keyCode >= 320 && keyCode <= 329) digit = keyCode - 320;
            if (digit >= 0 && betInputText.length() < 7) {
                if (betInputText.equals("0")) betInputText = "";
                betInputText += digit;
                return true;
            }
            if (keyCode == 259 && !betInputText.isEmpty()) {
                betInputText = betInputText.substring(0, betInputText.length() - 1);
                if (betInputText.isEmpty()) betInputText = "0";
                return true;
            }
            if (keyCode == 257) { placeBet(); return true; }
            return true;
        }
        return super.keyPressed(event);
    }

    private void placeBet() {
        try {
            int amt = Integer.parseInt(betInputText);
            if (amt > 0 && selectedSegment >= 0 && selectedSegment < WheelSegment.values().length) {
                ClientPacketDistributor.sendToServer(
                        (CustomPacketPayload) new WheelBetPayload(wheelPos, selectedSegment, amt),
                        (CustomPacketPayload[]) new CustomPacketPayload[0]);
            }
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public void onClose() {
        ChipRenderer.clearDrag();
        CasinoClientEvents.onWheelDismissed();
        com.ultra.megamod.feature.casino.network.WheelSyncPayload.lastSync = null;
        super.onClose();
    }

    public boolean isPauseScreen() { return false; }
}
