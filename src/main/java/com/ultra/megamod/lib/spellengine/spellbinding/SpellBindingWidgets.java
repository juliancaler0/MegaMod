package com.ultra.megamod.lib.spellengine.spellbinding;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;

import java.util.List;
import java.util.Objects;

public class SpellBindingWidgets {
    private static final Identifier Pl = Identifier.fromNamespaceAndPath("megamod", "textures/gui/" + SpellBinding.name + ".png");

    private static final int BUTTON_TEXTURE_U = 0;
    private static final int BUTTON_TEXTURE_V = 184;
    private static final int BUTTON_WIDTH = 108;
    private static final int BUTTON_HEIGHT = 24;
    static final int SPELL_ICON_SIZE = 16;
    static final int SPELL_ICON_INDENT = (int) Math.ceil((BUTTON_HEIGHT - SPELL_ICON_SIZE) / 2.0);
    private static final int ORB_INDENT = 1;
    private static final int ORB_ICON_SIZE = 13;
    private static final int ORB_TEXTURE_U = 242;
    private static final int ORB_TEXTURE_V = 242;
    private static final int BOTTOM_TEXT_OFFSET = 10;
    private static final int COLOR_GOOD = 0x36ff00;
    private static final int COLOR_BAD = 0xfc5c5c;
    private static final int COLOR_GOOD_BUT_DISABLED = 0x48890e;
    // Tier row constants
    static final int TIER_ROW_HEIGHT = 24;
    static final int TIER_ROW_ICON_Y_OFFSET = (TIER_ROW_HEIGHT - SPELL_ICON_SIZE) / 2;
    static final int TIER_ROW_WIDTH = 108;
    static final int SELECTION_INDICATOR_SIZE = 24;
    static final int SELECTION_INDICATOR_U = 224;
    static final int SELECTION_INDICATOR_V = 0;

    record SpellViewModel(Identifier id, Identifier icon, Component name) { }

    // New tier-based view models
    record TierRowViewModel(
        int tier,
        boolean shown,
        int x, int y, int width, int height,
        List<SpellIconViewModel> spellIcons
    ) {
        public boolean mouseOver(int mouseX, int mouseY) {
            if(!shown) { return false; }
            return (mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height);
        }
    }

    public record SpellIconViewModel(
        int originalIndex,
        int x, int y, int size,
        boolean isEnabled,
        boolean isDetailsPublic,
        SpellViewModel spell,
        SpellBinding.State binding
    ) {
        public boolean mouseOver(int mouseX, int mouseY) {
            return (mouseX >= x && mouseX < x + size) && (mouseY >= y && mouseY < y + size);
        }
    }

    public record SpellBookViewModel(
        int originalIndex,
        boolean shown,
        int x, int y, int width, int height,
        boolean isEnabled,
        ItemStack itemStack,
        SpellBinding.State binding
    ) {
        public boolean mouseOver(int mouseX, int mouseY) {
            if (!shown) { return false; }
            return (mouseX >= x && mouseX < x + width) && (mouseY >= y && mouseY < y + height);
        }

        public boolean isMouseOverIcon(int mouseX, int mouseY) {
            int iconX = x + SpellBindingWidgets.SPELL_ICON_INDENT;
            int iconY = y + SpellBindingWidgets.TIER_ROW_ICON_Y_OFFSET;
            return (mouseX >= iconX && mouseX < iconX + SpellBindingWidgets.SPELL_ICON_SIZE) &&
                   (mouseY >= iconY && mouseY < iconY + SpellBindingWidgets.SPELL_ICON_SIZE);
        }
    }

    public static void drawSpellIcon(GuiGraphics context, SpellIconViewModel icon, int mouseX, int mouseY) {
        boolean mouseOver = icon.mouseOver(mouseX, mouseY);
        boolean alreadyApplied = icon.binding.state == SpellBinding.State.ApplyState.ALREADY_APPLIED;
        float alpha = (icon.isEnabled || alreadyApplied) ? 1.0f : 0.5f;

        // Draw hover highlight
        if (mouseOver && icon.isEnabled) {
            context.fill(icon.x - 1, icon.y - 1,
                    icon.x + icon.size + 1, icon.y + icon.size + 1,
                    0x40FFFFFF);
        }

        // Draw spell icon or item icon
        // Alpha color removed for 1.21.11 compat
         

        if (icon.spell != null && icon.spell.icon != null) {
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, icon.spell.icon, icon.x, icon.y,
                    0, 0, icon.size, icon.size, icon.size, icon.size);
        }

        // Alpha color removed for 1.21.11 compat
        
    }

    public static void drawSpellIconIndicator(GuiGraphics context, SpellIconViewModel icon) {
        boolean alreadyApplied = icon.binding.state == SpellBinding.State.ApplyState.ALREADY_APPLIED;
        if (alreadyApplied) {
            
            int indicatorOffset = (SpellBindingWidgets.SELECTION_INDICATOR_SIZE - SpellBindingWidgets.SPELL_ICON_SIZE) / 2;
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, Pl,
                    icon.x - indicatorOffset,
                    icon.y - indicatorOffset,
                    SpellBindingWidgets.SELECTION_INDICATOR_U,
                    SpellBindingWidgets.SELECTION_INDICATOR_V,
                    SpellBindingWidgets.SELECTION_INDICATOR_SIZE,
                    SpellBindingWidgets.SELECTION_INDICATOR_SIZE,
                    256, 256);
            
        }
    }

    public static void drawSpellBook(GuiGraphics context, Font font, SpellBookViewModel book, int mouseX, int mouseY) {
        if (!book.shown) { return; }  // Skip if not shown
        boolean mouseOver = book.mouseOver(mouseX, mouseY);
        boolean isUnlocked = book.isEnabled;

        // Draw hover highlight
//        if (mouseOver && book.isEnabled) {
//            context.fill(book.x, book.y,
//                    book.x + book.width, book.y + book.height,
//                    0x40FFFFFF);
//        }
        var vOffset = book.isEnabled
                ? (mouseOver ? SpellBindingWidgets.BUTTON_HEIGHT * 2 : 0)
                : SpellBindingWidgets.BUTTON_HEIGHT;
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, Pl,
                book.x,
                book.y,
                SpellBindingWidgets.BUTTON_TEXTURE_U,
                SpellBindingWidgets.BUTTON_TEXTURE_V + vOffset,
                SpellBindingWidgets.BUTTON_WIDTH,
                SpellBindingWidgets.BUTTON_HEIGHT,
                256, 256);

        // Draw book icon
        int iconX = book.x + SpellBindingWidgets.SPELL_ICON_INDENT;
        int iconY = book.y + SpellBindingWidgets.TIER_ROW_ICON_Y_OFFSET;

        // Alpha color removed for 1.21.11 compat
        
        context.renderItem(book.itemStack, iconX, iconY);
        // Alpha color removed for 1.21.11 compat
        

        // Draw book name
        Component bookName = book.itemStack.getHoverName();
        int textX = iconX + SpellBindingWidgets.SPELL_ICON_SIZE + 4;  // 4px gap after icon
        var textWidth = BUTTON_WIDTH - (SpellBindingWidgets.SPELL_ICON_SIZE + 4);
        if (textWidth < font.width(bookName)) {
            int textY = book.y + 3;
            drawTextWrapped(context, font, bookName, textX, textY, textWidth,
                    isUnlocked ? 0xFFFFFF : 0x808080);
        } else {
            int textY = book.y + (SpellBindingWidgets.TIER_ROW_HEIGHT - font.lineHeight) / 2;  // Vertically centered
            context.drawString(font, bookName, textX, textY,
                    isUnlocked ? 0xFFFFFF : 0x808080);
        }
    }


    public static void drawTextWrapped(GuiGraphics context, Font font, FormattedText text, int x, int y, int width, int color) {
        for(FormattedCharSequence orderedText : font.split(text, width)) {
            context.drawString(font, orderedText, x, y, color, true);
            Objects.requireNonNull(font);
            y += 9;
        }
    }
}
