package com.ultra.megamod.feature.combat.client;

import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.combat.network.ClassChoicePayload;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.client.SpellUnlockClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fullscreen class selection screen shown to first-time players.
 * Displays five class cards arranged horizontally with descriptions,
 * weapon previews, and a confirm button. Cannot be dismissed without choosing.
 */
public class ClassSelectionScreen extends Screen {

    private static final PlayerClass[] CLASSES = {
            PlayerClass.PALADIN, PlayerClass.WARRIOR, PlayerClass.WIZARD,
            PlayerClass.ROGUE, PlayerClass.RANGER
    };

    /** Weapon/equipment preview text for each class. */
    private static final String[][] CLASS_WEAPONS = {
            {"Claymores, Hammers", "Holy Wands & Staves", "Kite Shields", "Paladin Armor"},
            {"Double Axes, Glaives", "Daggers, Sickles", "War Cries", "Berserker Armor"},
            {"Wands & Staves", "Arcane, Fire, Frost", "Spell Tomes", "Wizard Robes"},
            {"Daggers, Sickles", "Stealth & Evasion", "Shadow Step", "Assassin Armor"},
            {"Bows, Crossbows", "Spears & Traps", "Nature Magic", "Ranger Armor"},
    };

    // CLASS_SPELLS is now generated dynamically from SpellRegistry via SpellUnlockClientHelper

    /** Unicode symbols for class icons. */
    private static final String[] CLASS_ICONS = {
            "\u2694",  // Paladin - crossed swords
            "\u2620",  // Warrior - skull
            "\u2605",  // Wizard - star
            "\u2666",  // Rogue - diamond
            "\u2191",  // Ranger - arrow up
    };

    private PlayerClass selectedClass = null;
    private PlayerClass hoveredClass = null;
    private Button confirmButton;

    // Animation
    private float animationProgress = 0f;
    private float selectedPulse = 0f;

    // Card layout
    private int cardWidth;
    private int cardHeight;
    private int cardSpacing;
    private int cardsStartX;
    private int cardsY;

    public ClassSelectionScreen() {
        super(Component.literal("Choose Your Class"));
    }

    @Override
    protected void init() {
        super.init();

        // Responsive card sizing
        cardWidth = Math.min(140, (this.width - 60) / 5 - 8);
        cardHeight = Math.min(220, this.height - 120);
        cardSpacing = 8;
        int totalWidth = (cardWidth + cardSpacing) * 5 - cardSpacing;
        cardsStartX = (this.width - totalWidth) / 2;
        cardsY = 55;

        // Confirm button (starts disabled)
        confirmButton = Button.builder(Component.literal("Confirm Selection"), btn -> onConfirm())
                .bounds(this.width / 2 - 80, this.height - 40, 160, 20)
                .build();
        confirmButton.active = false;
        this.addRenderableWidget(confirmButton);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Dark background with subtle gradient
        renderDarkBackground(gfx);

        // Animate in
        animationProgress = Math.min(1.0f, animationProgress + 0.04f);
        selectedPulse += 0.08f;

        float easedAlpha = easeOutCubic(animationProgress);

        // Title
        int titleAlpha = (int)(easedAlpha * 255) << 24;
        if (titleAlpha != 0) {
            String title = "Choose Your Class";
            int titleWidth = this.font.width(title);
            gfx.drawString(this.font, title, (this.width - titleWidth) / 2, 15,
                    0xFFFFFF | titleAlpha, true);

            String subtitle = "This choice defines your weapons, armor, and spells";
            int subWidth = this.font.width(subtitle);
            gfx.drawString(this.font, subtitle, (this.width - subWidth) / 2, 28,
                    0xBBBBBB | titleAlpha, false);
        }

        // Update hover state
        hoveredClass = null;
        for (int i = 0; i < CLASSES.length; i++) {
            int cx = cardsStartX + i * (cardWidth + cardSpacing);
            if (mouseX >= cx && mouseX < cx + cardWidth && mouseY >= cardsY && mouseY < cardsY + cardHeight) {
                hoveredClass = CLASSES[i];
            }
        }

        // Render class cards
        for (int i = 0; i < CLASSES.length; i++) {
            float cardDelay = i * 0.1f;
            float cardAlpha = Math.max(0, Math.min(1, (animationProgress - cardDelay) * 3f));
            if (cardAlpha > 0) {
                renderClassCard(gfx, i, cardAlpha, mouseX, mouseY);
            }
        }

        // Update confirm button state
        confirmButton.active = selectedClass != null;
        if (selectedClass != null) {
            String confirmText = "Confirm: " + selectedClass.getDisplayName();
            confirmButton.setMessage(Component.literal(confirmText));
        } else {
            confirmButton.setMessage(Component.literal("Select a Class"));
        }

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void renderDarkBackground(GuiGraphics gfx) {
        // Full dark background
        gfx.fill(0, 0, this.width, this.height, 0xE0101020);

        // Top and bottom decorative bars
        gfx.fill(0, 0, this.width, 2, 0xFF444488);
        gfx.fill(0, this.height - 2, this.width, this.height, 0xFF444488);

        // Subtle vignette effect at edges
        int vignetteAlpha = 0x40;
        for (int i = 0; i < 20; i++) {
            int alpha = vignetteAlpha - (i * 2);
            if (alpha <= 0) break;
            int color = alpha << 24;
            gfx.fill(i, i, this.width - i, i + 1, color);
            gfx.fill(i, this.height - i - 1, this.width - i, this.height - i, color);
        }
    }

    private void renderClassCard(GuiGraphics gfx, int index, float alpha, int mouseX, int mouseY) {
        PlayerClass cls = CLASSES[index];
        int cx = cardsStartX + index * (cardWidth + cardSpacing);
        int cy = cardsY;

        boolean isSelected = cls == selectedClass;
        boolean isHovered = cls == hoveredClass;

        int alphaInt = (int)(alpha * 255);
        int alphaShift = alphaInt << 24;

        // Card background
        int bgColor;
        if (isSelected) {
            // Pulsing glow for selected card
            float pulse = (float)(Math.sin(selectedPulse) * 0.15 + 0.85);
            int pulseAlpha = (int)(pulse * alphaInt);
            bgColor = (pulseAlpha << 24) | (cls.getColor() & 0x00FFFFFF);
            // Bright border
            drawCardBorder(gfx, cx - 2, cy - 2, cardWidth + 4, cardHeight + 4,
                    0xFF000000 | cls.getColor(), 2);
        } else if (isHovered) {
            bgColor = (alphaInt / 2 << 24) | 0x003355;
            drawCardBorder(gfx, cx - 1, cy - 1, cardWidth + 2, cardHeight + 2,
                    0x80FFFFFF, 1);
        } else {
            bgColor = (alphaInt / 3 << 24) | 0x001122;
        }

        gfx.fill(cx, cy, cx + cardWidth, cy + cardHeight, bgColor);

        // Inner card content area (slightly inset)
        int inset = 6;
        int contentX = cx + inset;
        int contentWidth = cardWidth - inset * 2;
        int textY = cy + inset;

        // Class icon (large, colored)
        int iconColor = (cls.getColor() & 0x00FFFFFF) | alphaShift;
        String icon = CLASS_ICONS[index];
        int iconWidth = this.font.width(icon);

        // Draw icon with glow effect when selected
        if (isSelected) {
            int glowColor = ((alphaInt / 4) << 24) | (cls.getColor() & 0x00FFFFFF);
            gfx.drawString(this.font, icon, cx + (cardWidth - iconWidth) / 2 - 1, textY, glowColor, false);
            gfx.drawString(this.font, icon, cx + (cardWidth - iconWidth) / 2 + 1, textY, glowColor, false);
            gfx.drawString(this.font, icon, cx + (cardWidth - iconWidth) / 2, textY - 1, glowColor, false);
            gfx.drawString(this.font, icon, cx + (cardWidth - iconWidth) / 2, textY + 1, glowColor, false);
        }
        gfx.drawString(this.font, icon, cx + (cardWidth - iconWidth) / 2, textY, iconColor, true);
        textY += 16;

        // Class name
        String name = cls.getDisplayName();
        int nameWidth = this.font.width(name);
        int nameColor = isSelected
                ? (0xFF000000 | cls.getColor())
                : (iconColor);
        gfx.drawString(this.font, name, cx + (cardWidth - nameWidth) / 2, textY, nameColor, true);
        textY += 14;

        // Divider line
        int divColor = (alphaInt / 3 << 24) | (cls.getColor() & 0x00FFFFFF);
        gfx.fill(contentX, textY, contentX + contentWidth, textY + 1, divColor);
        textY += 6;

        // Description (word-wrapped)
        int descColor = isSelected ? (alphaShift | 0xFFFFFF) : (alphaShift | 0xBBBBBB);
        textY = drawWrappedText(gfx, cls.getDescription(), contentX, textY, contentWidth, descColor);
        textY += 4;

        // "Best for:" line (highlighted)
        String bestFor = cls.getBestFor();
        if (bestFor != null && !bestFor.isEmpty()) {
            int bestForColor = isSelected
                    ? (alphaShift | 0xFFFF55)   // bright yellow when selected
                    : (alphaShift | 0xCCAA33);  // muted gold otherwise
            textY = drawWrappedText(gfx, bestFor, contentX, textY, contentWidth, bestForColor);
        }
        textY += 4;

        // Weapons header
        int headerColor = (alphaShift) | (cls.getColor() & 0x00FFFFFF);
        gfx.drawString(this.font, "Equipment:", contentX, textY, headerColor, false);
        textY += 11;

        // Weapon list
        int detailColor = isSelected ? (alphaShift | 0xDDDDDD) : (alphaShift | 0x999999);
        String[] weapons = CLASS_WEAPONS[index];
        for (String weapon : weapons) {
            if (textY + 10 > cy + cardHeight - 30) break;
            gfx.drawString(this.font, "\u2022 " + weapon, contentX + 2, textY, detailColor, false);
            textY += 10;
        }
        textY += 4;

        // Spells header — grouped by unlock level
        if (textY + 20 < cy + cardHeight - 6) {
            gfx.drawString(this.font, "Spell Unlocks:", contentX, textY, headerColor, false);
            textY += 11;

            TreeMap<Integer, List<SpellDefinition>> spellsByLevel = SpellUnlockClientHelper.getSpellsByLevel(cls);
            for (Map.Entry<Integer, List<SpellDefinition>> entry : spellsByLevel.entrySet()) {
                if (textY + 10 > cy + cardHeight - 16) break;

                int reqLevel = entry.getKey();
                List<SpellDefinition> spells = entry.getValue();

                // Level label in accent color
                int lvlLabelColor = (alphaShift) | (cls.getColor() & 0x00FFFFFF);
                String lvlLabel = "Lv." + reqLevel;
                gfx.drawString(this.font, lvlLabel, contentX + 2, textY, lvlLabelColor, false);

                // Spell names after the level label
                int spellX = contentX + 2 + this.font.width(lvlLabel) + 4;
                StringBuilder spellNames = new StringBuilder();
                for (int s = 0; s < spells.size(); s++) {
                    if (s > 0) spellNames.append(", ");
                    spellNames.append(spells.get(s).name());
                }

                // Truncate if too long
                String spellStr = spellNames.toString();
                int maxSpellW = contentWidth - (spellX - contentX);
                if (this.font.width(spellStr) > maxSpellW) {
                    while (this.font.width(spellStr + "..") > maxSpellW && spellStr.length() > 1) {
                        spellStr = spellStr.substring(0, spellStr.length() - 1);
                    }
                    spellStr = spellStr + "..";
                }

                gfx.drawString(this.font, spellStr, spellX, textY, detailColor, false);
                textY += 10;
            }
        }

        // Selection indicator at bottom of card
        if (isSelected) {
            String selText = "SELECTED";
            int selWidth = this.font.width(selText);
            int selY = cy + cardHeight - 14;
            gfx.drawString(this.font, selText, cx + (cardWidth - selWidth) / 2, selY,
                    0xFF000000 | cls.getColor(), true);
        }
    }

    /**
     * Draws word-wrapped text within a given width, returns the final Y position.
     */
    private int drawWrappedText(GuiGraphics gfx, String text, int x, int y, int maxWidth, int color) {
        // Simple word wrap
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (this.font.width(test) > maxWidth && line.length() > 0) {
                gfx.drawString(this.font, line.toString(), x, lineY, color, false);
                lineY += 10;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            gfx.drawString(this.font, line.toString(), x, lineY, color, false);
            lineY += 10;
        }
        return lineY;
    }

    private void drawCardBorder(GuiGraphics gfx, int x, int y, int w, int h, int color, int thickness) {
        // Top
        gfx.fill(x, y, x + w, y + thickness, color);
        // Bottom
        gfx.fill(x, y + h - thickness, x + w, y + h, color);
        // Left
        gfx.fill(x, y, x + thickness, y + h, color);
        // Right
        gfx.fill(x + w - thickness, y, x + w, y + h, color);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            // Check if a card was clicked
            for (int i = 0; i < CLASSES.length; i++) {
                int cx = cardsStartX + i * (cardWidth + cardSpacing);
                if (mouseX >= cx && mouseX < cx + cardWidth
                        && mouseY >= cardsY && mouseY < cardsY + cardHeight) {
                    selectedClass = CLASSES[i];
                    // Play click sound
                    Minecraft.getInstance().getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    return true;
                }
            }
        }
        return super.mouseClicked(event, consumed);
    }

    private void onConfirm() {
        if (selectedClass == null) return;

        // Send choice to server
        ClientPacketDistributor.sendToServer(new ClassChoicePayload(selectedClass.name()));

        // Close the screen
        this.onClose();
    }

    /**
     * Cannot be dismissed without choosing -- block ESC key.
     */
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == 256) { // ESC
            // Don't close -- player must pick a class
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float easeOutCubic(float t) {
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }
}
