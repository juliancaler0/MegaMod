package com.ultra.megamod.lib.spellengine.spellbinding.spellchoice;

import com.mojang.blaze3d.systems.RenderSystem;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import com.ultra.megamod.lib.spellengine.client.util.SpellRender;

import java.util.ArrayList;
import java.util.List;

public class SpellChoiceScreen extends AbstractContainerScreen<SpellChoiceScreenHandler> {
    private static final int SPELL_ICON_SIZE = 16;
    private static final int ICON_SPACING = 16;

    private List<SpellIconViewModel> spellIcons = new ArrayList<>();

    public SpellChoiceScreen(SpellChoiceScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 20;
    }

    private List<SpellIconViewModel> createSpellIcons() {
        List<SpellIconViewModel> icons = new ArrayList<>();
        var world = this.minecraft.level;
        if (world == null) return icons;

        // Collect valid spells
        List<SpellData> spells = new ArrayList<>();
        for (int i = 0; i < SpellChoiceScreenHandler.MAXIMUM_SPELL_COUNT; i++) {
            var rawId = menu.spellId[i];
            if (rawId < 0) continue;

            var spellEntry = java.util.Optional.ofNullable(SpellRegistry.from(world).byId(rawId)).flatMap(s -> SpellRegistry.from(world).get(SpellRegistry.from(world).getResourceKey(s).get().identifier()));
            if (spellEntry.isEmpty()) continue;

            var spell = spellEntry.get();
            var spellId = spell.unwrapKey().get().identifier();
            var spellName = Component.translatable(SpellTooltip.spellTranslationKey(spellId));
            var spellIcon = SpellRender.iconTexture(spellId);

            spells.add(new SpellData(i, new SpellViewModel(spellId, spellIcon, spellName)));
        }

        if (spells.isEmpty()) return icons;

        // Calculate centered positioning with 16px gaps
        int iconCount = spells.size();
        int totalIconWidth = iconCount * SPELL_ICON_SIZE;
        int totalGapWidth = (iconCount - 1) * ICON_SPACING;
        int totalWidth = totalIconWidth + totalGapWidth;

        // Position icons horizontally centered together
        int startX = (this.width - totalWidth) / 2;
        int y = this.height / 2 + SPELL_ICON_SIZE;  // Vertically centered, and below that a bit

        int x = startX;
        for (var spellData : spells) {
            icons.add(new SpellIconViewModel(
                spellData.index,
                x, y,
                SPELL_ICON_SIZE,
                spellData.spell
            ));
            x += SPELL_ICON_SIZE + ICON_SPACING;
        }

        return icons;
    }

    // Disable Slot clicks to protect the read-only slot
    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType) {
        // Do nothing
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.spellIcons = createSpellIcons();
    }

    private void drawSpellIcon(GuiGraphics context, SpellIconViewModel icon, int mouseX, int mouseY) {
        boolean mouseOver = icon.mouseOver(mouseX, mouseY);

        // Draw hover highlight (white overlay)
        if (mouseOver) {
            context.fill(icon.x - 1, icon.y - 1,
                    icon.x + icon.size + 1, icon.y + icon.size + 1,
                    0x40FFFFFF);  // 25% white
        }

        // Draw spell icon texture
        // Shader color removed for 1.21.11
        

        if (icon.spell != null && icon.spell.icon != null) {
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, icon.spell.icon, icon.x, icon.y,
                    0, 0, icon.size, icon.size, icon.size, icon.size);
        }

        // Shader color removed for 1.21.11
        
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Draw text
        context.drawCenteredString(
                font,
                Component.translatable("gui.spell_engine.choose_for_item", menu.getChoiceItemStack().getHoverName()),
                this.width / 2, this.height / 2, 0xFFFFFF);

        // Draw spell details tooltip on hover (like SpellBindingScreen)
        var player = this.minecraft.player;
        var itemStack = menu.getChoiceItemStack();

        for (var icon : spellIcons) {
            if (icon.mouseOver(mouseX, mouseY) && icon.spell != null) {
                ArrayList<Component> tooltip = new ArrayList<>();
                tooltip.addAll(SpellTooltip.spellEntry(icon.spell.id, player, itemStack, true, 0));
                context.setTooltipForNextFrame(
                        this.font,
                        tooltip.stream().map(Component::getVisualOrderText).toList(),
                        mouseX,
                        mouseY);
                break;
            }
        }
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean consumed) {
        if (!consumed && event.button() == 0) {  // Left click
            int mx = (int) event.x();
            int my = (int) event.y();
            for (var icon : spellIcons) {
                if (icon.mouseOver(mx, my)) {
                    // Send selection to server
                    if (SpellChoiceScreen.this.minecraft.gameMode != null) {
                        SpellChoiceScreen.this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, icon.index);
                    }
                    this.onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, consumed);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        // Draw a vanilla-style dialog panel around the icons + title.
        // Source uses HandledScreen's default dim background; here we explicitly
        // paint a centered panel so the choice UI has chrome (user feedback).
        if (!spellIcons.isEmpty()) {
            int pad = 12;
            int iconY = spellIcons.get(0).y;
            int iconSize = spellIcons.get(0).size;
            int leftIconX = spellIcons.get(0).x;
            int rightIconX = spellIcons.get(spellIcons.size() - 1).x + spellIcons.get(spellIcons.size() - 1).size;

            int panelLeft = Math.min(leftIconX - pad, this.width / 2 - 90);
            int panelRight = Math.max(rightIconX + pad, this.width / 2 + 90);
            int panelTop = (this.height / 2) - pad - 4;
            int panelBottom = iconY + iconSize + pad;

            // Drop shadow
            context.fill(panelLeft + 2, panelTop + 2, panelRight + 2, panelBottom + 2, 0x80000000);
            // Body
            context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xF0100010);
            // Light inner border (1px inset)
            context.fill(panelLeft + 1, panelTop + 1, panelRight - 1, panelTop + 2, 0xFF5A5A7A);
            context.fill(panelLeft + 1, panelBottom - 2, panelRight - 1, panelBottom - 1, 0xFF28283C);
            context.fill(panelLeft + 1, panelTop + 2, panelLeft + 2, panelBottom - 2, 0xFF5A5A7A);
            context.fill(panelRight - 2, panelTop + 2, panelRight - 1, panelBottom - 2, 0xFF28283C);
            // Outer border
            context.fill(panelLeft, panelTop, panelRight, panelTop + 1, 0xFF000000);
            context.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, 0xFF000000);
            context.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, 0xFF000000);
            context.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0xFF000000);
        }

        // Draw spell icons (following SpellBindingScreen pattern)
        for (var icon : spellIcons) {
            drawSpellIcon(context, icon, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        // Draw title
        context.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // View model records
    private record SpellViewModel(Identifier id, Identifier icon, Component name) { }

    private record SpellIconViewModel(
        int index,           // Index in menu.spellId array
        int x, int y,        // Screen coordinates
        int size,            // Icon size (16)
        SpellViewModel spell // Spell data
    ) {
        public boolean mouseOver(int mouseX, int mouseY) {
            return (mouseX >= x && mouseX < x + size) &&
                   (mouseY >= y && mouseY < y + size);
        }
    }

    private record SpellData(int index, SpellViewModel spell) { }
}
