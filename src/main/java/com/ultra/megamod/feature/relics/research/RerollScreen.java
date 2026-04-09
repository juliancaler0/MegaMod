package com.ultra.megamod.feature.relics.research;

import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RerollScreen extends Screen {
    private static final int WIDTH = 280;
    private static final int HEIGHT = 220;

    // Colors
    private static final int GOLD = 0xFFDAA520;
    private static final int CREAM = 0xFFF5E6C8;
    private static final int DIM = 0xFF666666;
    private static final int RED = 0xFFFF5555;
    private static final int GREEN = 0xFF55FF55;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int CYAN = 0xFF55FFFF;
    private static final int YELLOW = 0xFFFFFF55;

    private static final Set<String> VALID_MATERIALS = Set.of(
        "megamod:cerulean_ingot", "megamod:crystalline_shard",
        "megamod:spectral_silk", "megamod:umbra_ingot", "megamod:void_shard"
    );

    private static final Map<String, String> MATERIAL_NAMES = Map.of(
        "megamod:cerulean_ingot", "Cerulean Ingot",
        "megamod:crystalline_shard", "Crystalline Shard",
        "megamod:spectral_silk", "Spectral Silk",
        "megamod:umbra_ingot", "Umbra Ingot",
        "megamod:void_shard", "Void Shard"
    );

    private String statusMessage = "";
    private long statusTime = 0;

    // Cached button Y position (recalculated each frame in render, used in mouseClicked)
    private int cachedRerollBtnY = 0;
    private boolean cachedCanReroll = false;

    public RerollScreen() {
        super(Component.literal("Reroll Station"));
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int px = (this.width - WIDTH) / 2;
        int py = (this.height - HEIGHT) / 2;

        // Background
        UIHelper.drawScreenBg(g, px, py, WIDTH, HEIGHT);

        // Title bar
        UIHelper.drawTitleBar(g, px + 4, py + 4, WIDTH - 8, 20);
        UIHelper.drawCenteredTitle(g, this.font, "\u2726 Reroll Station \u2726", px + WIDTH / 2, py + 10);

        int cx = px + 10;
        int cy = py + 30;

        ItemStack heldItem = mc.player.getMainHandItem();
        boolean hasWeapon = !heldItem.isEmpty() && WeaponStatRoller.isWeaponInitialized(heldItem);

        // === WEAPON SECTION ===
        UIHelper.drawCard(g, cx, cy, WIDTH - 20, hasWeapon ? 32 : 24);
        if (hasWeapon) {
            g.renderItem(heldItem, cx + 4, cy + 8);
            String weaponName = heldItem.getHoverName().getString();
            if (weaponName.length() > 30) weaponName = weaponName.substring(0, 30) + "...";
            g.drawString(this.font, weaponName, cx + 24, cy + 4, WHITE, false);
            WeaponRarity rarity = WeaponStatRoller.getRarity(heldItem);
            int rarityColor = getRarityColor(rarity);
            g.drawString(this.font, rarity.getDisplayName() + " Weapon", cx + 24, cy + 16, rarityColor, false);
        } else {
            g.drawString(this.font, "Hold a weapon in your main hand", cx + 6, cy + 8, DIM, false);
        }
        cy += hasWeapon ? 36 : 28;

        // === CURRENT BONUSES SECTION ===
        if (hasWeapon) {
            List<BonusEntry> bonuses = getWeaponBonuses(heldItem);
            int maxVisible = 5;
            int bonusLines = Math.min(bonuses.size(), maxVisible);
            if (bonuses.size() > maxVisible) bonusLines++; // extra line for "+N more..."
            if (bonuses.isEmpty()) bonusLines = 1; // "No stat bonuses"
            int bonusHeight = bonusLines * 10 + 14;
            UIHelper.drawCard(g, cx, cy, WIDTH - 20, bonusHeight);
            g.drawString(this.font, "Current Bonuses:", cx + 6, cy + 2, GOLD, false);
            int by = cy + 12;
            int shown = 0;
            for (BonusEntry bonus : bonuses) {
                if (shown >= maxVisible) {
                    g.drawString(this.font, "  +" + (bonuses.size() - maxVisible) + " more...", cx + 6, by, DIM, false);
                    break;
                }
                g.drawString(this.font, "  " + bonus.formatted, cx + 6, by, CYAN, false);
                by += 10;
                shown++;
            }
            if (bonuses.isEmpty()) {
                g.drawString(this.font, "  No stat bonuses", cx + 6, cy + 12, DIM, false);
            }
            cy += bonusHeight + 4;
        }

        // === REQUIREMENTS SECTION ===
        UIHelper.drawCard(g, cx, cy, WIDTH - 20, 44);
        g.drawString(this.font, "Requirements:", cx + 6, cy + 2, GOLD, false);

        // Material check
        MaterialInfo matInfo = findMaterial(mc);
        int matLineY = cy + 14;
        if (matInfo != null) {
            g.drawString(this.font, "\u2714 " + matInfo.name + " x" + matInfo.count, cx + 6, matLineY, GREEN, false);
        } else {
            g.drawString(this.font, "\u2718 Need a dungeon material", cx + 6, matLineY, RED, false);
        }

        // Cost display
        int costLineY = cy + 26;
        if (hasWeapon) {
            WeaponRarity rarity = WeaponStatRoller.getRarity(heldItem);
            int cost = RerollPayload.getRerollCost(rarity);
            g.drawString(this.font, "Cost: " + cost + " MC", cx + 6, costLineY, GOLD, false);
        } else {
            g.drawString(this.font, "Cost: ---", cx + 6, costLineY, DIM, false);
        }
        int reqSectionBottom = cy + 48;
        cy = reqSectionBottom;

        // === REROLL BUTTON ===
        int btnW = 120;
        int btnH = 20;
        int btnX = px + (WIDTH - btnW) / 2;
        int btnY = cy;
        boolean hasMaterial = matInfo != null;
        boolean canReroll = hasWeapon && hasMaterial;
        boolean btnHovered = canReroll && mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        // Cache for mouseClicked
        cachedRerollBtnY = btnY;
        cachedCanReroll = canReroll;

        if (canReroll) {
            UIHelper.drawButton(g, btnX, btnY, btnW, btnH, btnHovered);
        } else {
            g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF444444);
            g.fill(btnX + 1, btnY + 1, btnX + btnW - 1, btnY + btnH - 1, 0xFF2A2A2A);
        }
        String btnLabel = "\u2726 REROLL STATS \u2726";
        int btnLabelW = this.font.width(btnLabel);
        g.drawString(this.font, btnLabel, btnX + (btnW - btnLabelW) / 2, btnY + 6, canReroll ? WHITE : DIM, false);

        // Why-can't-reroll hint
        if (!canReroll && hasWeapon) {
            String hint = !hasMaterial ? "Missing dungeon material" : "";
            if (!hint.isEmpty()) {
                int hintW = this.font.width(hint);
                g.drawString(this.font, hint, px + (WIDTH - hintW) / 2, btnY + btnH + 3, RED, false);
            }
        }

        // === STATUS MESSAGE (shows briefly after reroll action) ===
        if (!statusMessage.isEmpty() && System.currentTimeMillis() - statusTime < 3000) {
            int msgW = this.font.width(statusMessage);
            g.drawString(this.font, statusMessage, px + (WIDTH - msgW) / 2, btnY + btnH + 14, GREEN, false);
        }

        // === CLOSE BUTTON ===
        int closeBtnX = px + (WIDTH - 50) / 2;
        int closeBtnY = py + HEIGHT - 22;
        boolean closeHovered = mouseX >= closeBtnX && mouseX < closeBtnX + 50 && mouseY >= closeBtnY && mouseY < closeBtnY + 16;
        UIHelper.drawButton(g, closeBtnX, closeBtnY, 50, 16, closeHovered);
        int closeW = this.font.width("Close");
        g.drawString(this.font, "Close", closeBtnX + (50 - closeW) / 2, closeBtnY + 4, CREAM, false);

        // Material tooltip on hover over the requirements card area
        if (!hasMaterial && mouseX >= cx && mouseX < cx + WIDTH - 20 && mouseY >= matLineY - 2 && mouseY < matLineY + 10) {
            renderMaterialTooltip(g, mouseX, mouseY);
        }
    }

    /**
     * Reads the weapon_rolled_bonuses tag and formats each bonus for display.
     */
    private List<BonusEntry> getWeaponBonuses(ItemStack stack) {
        List<BonusEntry> bonuses = new ArrayList<>();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag bonusesTag = tag.getCompoundOrEmpty(WeaponStatRoller.KEY_ROLLED_BONUSES);
        int count = bonusesTag.getIntOr("count", 0);

        for (int i = 0; i < count; i++) {
            CompoundTag entry = bonusesTag.getCompoundOrEmpty("bonus_" + i);
            String name = entry.getStringOr("name", "Unknown");
            double value = entry.getDoubleOr("value", 0.0);
            boolean isPercent = entry.getBooleanOr("percent", false);
            int opOrdinal = entry.getIntOr("op", 0);

            String sign = value >= 0 ? "+" : "";
            String formatted;
            if (isPercent || opOrdinal == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.ordinal()) {
                double displayVal = opOrdinal == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.ordinal()
                    ? value * 100.0 : value;
                formatted = sign + String.format("%.1f%%", displayVal);
            } else {
                formatted = sign + String.format("%.1f", value);
            }
            bonuses.add(new BonusEntry(formatted + " " + name));
        }
        return bonuses;
    }

    /**
     * Converts a ChatFormatting rarity color to an ARGB int for rendering.
     */
    private int getRarityColor(WeaponRarity rarity) {
        ChatFormatting fmt = rarity.getNameColor();
        Integer color = fmt.getColor();
        if (color != null) {
            return 0xFF000000 | color;
        }
        return WHITE;
    }

    /**
     * Scans inventory for a valid dungeon material, returning info about the first found.
     */
    private MaterialInfo findMaterial(Minecraft mc) {
        if (mc.player == null) return null;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (VALID_MATERIALS.contains(itemId)) {
                String name = MATERIAL_NAMES.getOrDefault(itemId, "Dungeon Material");
                return new MaterialInfo(name, stack.getCount());
            }
        }
        return null;
    }

    private void renderMaterialTooltip(GuiGraphics g, int mouseX, int mouseY) {
        String[] materials = {"Cerulean Ingot", "Crystalline Shard", "Spectral Silk", "Umbra Ingot", "Void Shard"};
        int tipW = 130;
        int tipH = 12 + materials.length * 10;
        int tipX = mouseX + 10;
        int tipY = mouseY;
        if (tipX + tipW > this.width) tipX = mouseX - tipW - 4;

        UIHelper.drawTooltipBackground(g, tipX, tipY, tipW, tipH);
        g.drawString(this.font, "Valid materials:", tipX + 4, tipY + 2, GOLD, false);
        for (int i = 0; i < materials.length; i++) {
            g.drawString(this.font, " \u2022 " + materials[i], tipX + 4, tipY + 12 + i * 10, CREAM, false);
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        if (event.button() != 0) return super.mouseClicked(event, consumed);

        int mx = (int) event.x();
        int my = (int) event.y();
        int px = (this.width - WIDTH) / 2;
        int py = (this.height - HEIGHT) / 2;

        // Reroll button
        int btnW = 120;
        int btnH = 20;
        int btnX = px + (WIDTH - btnW) / 2;
        int btnY = cachedRerollBtnY;

        if (cachedCanReroll && mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH) {
            ClientPacketDistributor.sendToServer(new RerollPayload.RerollActionPayload("reroll"));
            statusMessage = "Rerolling...";
            statusTime = System.currentTimeMillis();
            return true;
        }

        // Close button
        int closeBtnX = px + (WIDTH - 50) / 2;
        int closeBtnY = py + HEIGHT - 22;
        if (mx >= closeBtnX && mx < closeBtnX + 50 && my >= closeBtnY && my < closeBtnY + 16) {
            this.onClose();
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record BonusEntry(String formatted) {}
    private record MaterialInfo(String name, int count) {}
}
