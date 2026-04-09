package com.ultra.megamod.feature.villagerrefresh;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.Villager;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Admin Trade Editor — lets admins reroll individual trades, adjust villager level,
 * lock trades, and seek specific trades by name/enchantment.
 *
 * Uses cached offers to avoid calling villager.getOffers() on the client (throws in 1.21.11).
 * The server sends updated offers back via AdminTradeOffersPayload after each action.
 */
public class AdminTradeEditorScreen extends Screen {

    // Colors
    private static final int BG = 0xFF0E0E18;
    private static final int PANEL = 0xFF141420;
    private static final int BORDER = 0xFF6B3FA0;
    private static final int GOLD = 0xFFFFD700;
    private static final int TEXT = 0xFFCCCCDD;
    private static final int DIM = 0xFF888899;
    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int SLOT_BG = 0xFF1C1C2C;
    private static final int SLOT_BORDER = 0xFF2A2A3A;
    private static final int LOCKED_BG = 0xFF2C1C1C;
    private static final int LOCKED_BORDER = 0xFF5A3030;
    private static final int ROW_BG = 0xFF181828;
    private static final int ROW_ALT = 0xFF1A1A2E;
    private static final int ARROW_COLOR = 0xFFAABBCC;
    private static final int SEEK_COLOR = 0xFF44AAFF;

    private final Villager villager;
    private final int villagerEntityId;
    private final Set<Integer> lockedTrades = new HashSet<>();

    /** Client-side cached offers — never call villager.getOffers() on client */
    private MerchantOffers cachedOffers;
    private int cachedLevel;

    private int panelX, panelY, panelW, panelH;
    private int scrollOffset = 0;
    private static final int ROW_HEIGHT = 24;
    private static final int MAX_VISIBLE_ROWS = 8;

    // Search/Seek
    private EditBox searchBox;
    private String seekStatusMessage = null;
    private int seekStatusTicks = 0;

    public AdminTradeEditorScreen(Villager villager, MerchantOffers initialOffers) {
        super(Component.literal("Trade Editor"));
        this.villager = villager;
        this.villagerEntityId = villager.getId();
        this.cachedOffers = initialOffers != null ? initialOffers : new MerchantOffers();
        this.cachedLevel = villager.getVillagerData().level();
    }

    /** Called by AdminTradeOffersPayload handler when server sends updated offers */
    public void updateOffers(MerchantOffers newOffers, int newLevel) {
        boolean tradeAdded = newOffers.size() > (this.cachedOffers != null ? this.cachedOffers.size() : 0);
        this.cachedOffers = newOffers;
        this.cachedLevel = newLevel;
        // Auto-scroll to bottom when a new trade was added (e.g. Custom or Add Trade)
        if (tradeAdded && cachedOffers.size() > MAX_VISIBLE_ROWS) {
            scrollOffset = cachedOffers.size() - MAX_VISIBLE_ROWS;
        }
        rebuildButtons();
    }

    @Override
    protected void init() {
        panelW = 380;
        panelH = 68 + MAX_VISIBLE_ROWS * ROW_HEIGHT + 40;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        // Search box for seek feature
        searchBox = new EditBox(this.font, panelX + 130, panelY + 28, 120, 16, Component.literal("Search"));
        searchBox.setHint(Component.literal("seek or item:n>item:n"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(true);
        this.addRenderableWidget(searchBox);

        rebuildButtons();
    }

    private void rebuildButtons() {
        // Remove all widgets EXCEPT the search box, then re-add it
        String currentSearch = searchBox != null ? searchBox.getValue() : "";
        this.clearWidgets();

        // Re-create search box (clearWidgets removes it)
        searchBox = new EditBox(this.font, panelX + 130, panelY + 28, 120, 16, Component.literal("Search"));
        searchBox.setHint(Component.literal("seek or item:n>item:n"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(true);
        searchBox.setValue(currentSearch);
        this.addRenderableWidget(searchBox);

        int btnY = panelY + panelH - 30;

        // Level down
        this.addRenderableWidget(Button.builder(Component.literal("Level -"), b -> {
            int newLevel = Math.max(1, cachedLevel - 1);
            sendAction(AdminTradeEditPayload.ACTION_SET_LEVEL, 0, newLevel);
        }).bounds(panelX + 10, panelY + 28, 50, 16).build());

        // Level up
        this.addRenderableWidget(Button.builder(Component.literal("Level +"), b -> {
            int newLevel = Math.min(5, cachedLevel + 1);
            sendAction(AdminTradeEditPayload.ACTION_SET_LEVEL, 0, newLevel);
        }).bounds(panelX + 65, panelY + 28, 50, 16).build());

        // Reroll All
        this.addRenderableWidget(Button.builder(Component.literal("Reroll All"), b -> {
            sendAction(AdminTradeEditPayload.ACTION_REROLL_ALL, 0, 0);
        }).bounds(panelX + 10, btnY, 70, 18).build());

        // Add Trade
        this.addRenderableWidget(Button.builder(Component.literal("Add Trade"), b -> {
            sendAction(AdminTradeEditPayload.ACTION_ADD_TRADE, 0, 0);
        }).bounds(panelX + 85, btnY, 70, 18).build());

        // Custom Trade — create a fully custom trade from text input
        this.addRenderableWidget(Button.builder(Component.literal("Custom"), b -> {
            String term = searchBox != null ? searchBox.getValue().trim() : "";
            if (term.isEmpty() || !term.contains(">")) {
                seekStatusMessage = "Format: item:count>item:count";
                seekStatusTicks = 80;
                return;
            }
            sendCustomAction(term);
            seekStatusMessage = "Creating custom trade...";
            seekStatusTicks = 60;
        }).bounds(panelX + 160, btnY, 50, 18).build());

        // Seek All — seek on every unlocked trade slot at once
        this.addRenderableWidget(Button.builder(Component.literal("Seek All"), b -> {
            String term = searchBox != null ? searchBox.getValue().trim() : "";
            if (term.isEmpty()) return;
            if (cachedOffers == null) return;
            for (int i = 0; i < cachedOffers.size(); i++) {
                if (!lockedTrades.contains(i)) {
                    sendSeekAction(i, term);
                }
            }
        }).bounds(panelX + 215, btnY, 55, 18).build());

        // Close
        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> {
            this.onClose();
        }).bounds(panelX + panelW - 60, btnY, 50, 18).build());

        // Per-trade buttons
        if (cachedOffers == null || cachedOffers.isEmpty()) return;
        int rowStartY = panelY + 68;
        for (int i = 0; i < Math.min(cachedOffers.size(), MAX_VISIBLE_ROWS); i++) {
            int visibleIndex = i + scrollOffset;
            if (visibleIndex >= cachedOffers.size()) break;
            int ry = rowStartY + i * ROW_HEIGHT;
            final int tradeIdx = visibleIndex;

            // Lock toggle
            boolean isLocked = lockedTrades.contains(tradeIdx);
            this.addRenderableWidget(Button.builder(
                    Component.literal(isLocked ? "\uD83D\uDD12" : "\uD83D\uDD13"),
                    b -> {
                        if (lockedTrades.contains(tradeIdx)) {
                            lockedTrades.remove(tradeIdx);
                        } else {
                            lockedTrades.add(tradeIdx);
                        }
                        rebuildButtons();
                    }
            ).bounds(panelX + 8, ry + 2, 20, ROW_HEIGHT - 4).build());

            // Seek button — reroll this slot until search term is found
            this.addRenderableWidget(Button.builder(Component.literal("Seek"), b -> {
                String term = searchBox != null ? searchBox.getValue().trim() : "";
                if (!term.isEmpty()) {
                    sendSeekAction(tradeIdx, term);
                }
            }).bounds(panelX + panelW - 108, ry + 2, 44, ROW_HEIGHT - 4).build());

            // Reroll this trade
            this.addRenderableWidget(Button.builder(Component.literal("Reroll"), b -> {
                sendAction(AdminTradeEditPayload.ACTION_REROLL_SINGLE, tradeIdx, 0);
            }).bounds(panelX + panelW - 58, ry + 2, 48, ROW_HEIGHT - 4).build());
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // Panel
        g.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, BORDER);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);

        // Title
        String profName = getProfessionName();
        String title = "Trade Editor - " + profName + " (Level " + cachedLevel + ")";
        g.drawCenteredString(this.font, title, panelX + panelW / 2, panelY + 8, GOLD);

        // Level indicator stars
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < cachedLevel ? "\u2605 " : "\u2606 ");
        }
        g.drawString(this.font, stars.toString().trim(), panelX + 125, panelY + 48, GOLD, false);

        // Search label
        g.drawString(this.font, "Seek:", panelX + 260, panelY + 32, SEEK_COLOR, false);

        // Trade rows
        int rowStartY = panelY + 68;

        if (cachedOffers == null || cachedOffers.isEmpty()) {
            g.drawCenteredString(this.font, "No trades available", panelX + panelW / 2, rowStartY + 30, DIM);
        }

        if (cachedOffers != null) {
            for (int i = 0; i < Math.min(cachedOffers.size(), MAX_VISIBLE_ROWS); i++) {
                int visibleIndex = i + scrollOffset;
                if (visibleIndex >= cachedOffers.size()) break;

                MerchantOffer offer = cachedOffers.get(visibleIndex);
                int ry = rowStartY + i * ROW_HEIGHT;
                boolean locked = lockedTrades.contains(visibleIndex);

                // Row background
                int rowBg = locked ? LOCKED_BG : (i % 2 == 0 ? ROW_BG : ROW_ALT);
                g.fill(panelX + 4, ry, panelX + panelW - 4, ry + ROW_HEIGHT, rowBg);

                // Trade number
                g.drawString(this.font, "#" + (visibleIndex + 1), panelX + 30, ry + 8, DIM, false);

                // Input item(s)
                ItemStack costA = offer.getBaseCostA();
                int itemX = panelX + 50;
                g.renderItem(costA, itemX, ry + 4);
                g.drawString(this.font, "x" + costA.getCount(), itemX + 17, ry + 10, TEXT, false);

                ItemStack costB = offer.getCostB();
                if (!costB.isEmpty()) {
                    itemX += 40;
                    g.renderItem(costB, itemX, ry + 4);
                    g.drawString(this.font, "x" + costB.getCount(), itemX + 17, ry + 10, TEXT, false);
                }

                // Arrow
                int arrowX = panelX + 150;
                g.drawString(this.font, "\u2192", arrowX, ry + 8, ARROW_COLOR, false);

                // Output item
                ItemStack result = offer.getResult();
                int outX = panelX + 165;
                g.renderItem(result, outX, ry + 4);
                g.drawString(this.font, "x" + result.getCount(), outX + 17, ry + 10, TEXT, false);

                // Uses info
                String uses = offer.getUses() + "/" + offer.getMaxUses();
                g.drawString(this.font, uses, panelX + 210, ry + 8, offer.isOutOfStock() ? RED : GREEN, false);

                // Locked indicator
                if (locked) {
                    g.drawString(this.font, "LOCKED", panelX + panelW - 160, ry + 8, RED, false);
                }
            }

            // Scroll hint
            if (cachedOffers.size() > MAX_VISIBLE_ROWS) {
                String scrollHint = "Scroll: " + (scrollOffset + 1) + "-" + Math.min(scrollOffset + MAX_VISIBLE_ROWS, cachedOffers.size()) + " of " + cachedOffers.size();
                g.drawCenteredString(this.font, scrollHint, panelX + panelW / 2, panelY + panelH - 42, DIM);
            }
        }

        // Status message (format errors, confirmations)
        if (seekStatusMessage != null && seekStatusTicks > 0) {
            int msgColor = seekStatusMessage.startsWith("Format:") ? RED : GREEN;
            g.drawCenteredString(this.font, seekStatusMessage, panelX + panelW / 2, panelY + 54, msgColor);
        }

        // Render widgets (buttons) on top
        super.render(g, mouseX, mouseY, partialTick);

        // Render item tooltip if hovering over a trade item
        if (cachedOffers != null) {
            ItemStack hoveredItem = ItemStack.EMPTY;
            for (int i = 0; i < Math.min(cachedOffers.size(), MAX_VISIBLE_ROWS); i++) {
                int visibleIndex = i + scrollOffset;
                if (visibleIndex >= cachedOffers.size()) break;
                MerchantOffer offer = cachedOffers.get(visibleIndex);
                int ry = rowStartY + i * ROW_HEIGHT;

                // Check costA hover
                ItemStack costA2 = offer.getBaseCostA();
                int costAx = panelX + 50;
                if (mouseX >= costAx && mouseX < costAx + 16 && mouseY >= ry + 4 && mouseY < ry + 20) {
                    hoveredItem = costA2;
                }

                // Check costB hover
                ItemStack costB2 = offer.getCostB();
                if (!costB2.isEmpty()) {
                    int costBx = costAx + 40;
                    if (mouseX >= costBx && mouseX < costBx + 16 && mouseY >= ry + 4 && mouseY < ry + 20) {
                        hoveredItem = costB2;
                    }
                }

                // Check result hover
                ItemStack result2 = offer.getResult();
                int resX = panelX + 165;
                if (mouseX >= resX && mouseX < resX + 16 && mouseY >= ry + 4 && mouseY < ry + 20) {
                    hoveredItem = result2;
                }
            }

            if (!hoveredItem.isEmpty()) {
                List<Component> lines = hoveredItem.getTooltipLines(
                        Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.ADVANCED);
                renderItemTooltip(g, lines, mouseX, mouseY);
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (cachedOffers == null) return true;
        int maxScroll = Math.max(0, cachedOffers.size() - MAX_VISIBLE_ROWS);
        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        rebuildButtons();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (seekStatusTicks > 0) {
            seekStatusTicks--;
        }
        if (villager.isRemoved() || (Minecraft.getInstance().player != null && Minecraft.getInstance().player.distanceTo(villager) > 12.0)) {
            this.onClose();
        }
    }

    private void sendAction(int action, int tradeIndex, int data) {
        long mask = 0L;
        for (int idx : lockedTrades) {
            if (idx < 64) mask |= (1L << idx);
        }
        ClientPacketDistributor.sendToServer(
                new AdminTradeEditPayload(villager.getId(), action, tradeIndex, data, mask, ""));
    }

    private void sendSeekAction(int tradeIndex, String searchTerm) {
        long mask = 0L;
        for (int idx : lockedTrades) {
            if (idx < 64) mask |= (1L << idx);
        }
        ClientPacketDistributor.sendToServer(
                new AdminTradeEditPayload(villager.getId(), AdminTradeEditPayload.ACTION_SEEK_TRADE, tradeIndex, 0, mask, searchTerm));
    }

    private void sendCustomAction(String tradeDescriptor) {
        ClientPacketDistributor.sendToServer(
                new AdminTradeEditPayload(villager.getId(), AdminTradeEditPayload.ACTION_CREATE_CUSTOM, 0, 0, 0L, tradeDescriptor));
    }

    private String getProfessionName() {
        var profKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
        if (profKey == null) return "Unknown";
        String path = profKey.identifier().getPath();
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }

    private void renderItemTooltip(GuiGraphics g, List<Component> lines, int mx, int my) {
        if (lines.isEmpty()) return;
        int maxW = 0;
        for (Component line : lines) {
            int w = this.font.width(line);
            if (w > maxW) maxW = w;
        }
        int ttW = maxW + 8;
        int ttH = lines.size() * 10 + 6;
        int ttX = mx + 12;
        int ttY = my - 8;
        if (ttX + ttW > this.width) ttX = mx - ttW - 4;
        if (ttY + ttH > this.height) ttY = this.height - ttH;
        if (ttY < 0) ttY = 0;

        UIHelper.drawTooltipBackground(g, ttX, ttY, ttW, ttH);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(this.font, lines.get(i), ttX + 4, ttY + 3 + i * 10, 0xFFFFFFFF, false);
        }
    }
}
