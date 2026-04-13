package com.ultra.megamod.lib.spellengine.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.config.HudConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HudConfigScreen extends Screen {
    private Screen previous;
    private boolean partConfigVisible = false;
    private final ArrayList<AbstractWidget> partButtons = new ArrayList();
    private final Map<Part, Checkbox> checkBoxes = new HashMap<>();

    public HudConfigScreen(Screen previous) {
        super(Component.translatable("gui.spell_engine.hud"));
        this.previous = previous;
    }

    private HudConfig config() {
        return SpellEngineClient.hudConfig.value;
    }

    @Override
    protected void init() {
        var buttonWidth = 60;
        var padding = 5;
        var buttonHeight = 20;
        var centerX = (width / 2);
        var centerY = (height / 2);



        addRenderableWidget(
                Button.builder(Component.translatable("x"), button -> { onClose(); })
                        .bounds(5, 5, buttonHeight, buttonHeight)
                        .build()
        );

        int y = centerY - 50;

        addRenderableWidget(
                Button.builder(Component.translatable("gui.spell_engine.parts"), button -> { this.toggleParts(); })
                        .bounds(centerX - padding - buttonWidth - (buttonWidth/2), y, buttonWidth, buttonHeight)
                        .build()
        );
        addRenderableWidget(
                Button.builder(Component.translatable("gui.spell_engine.preset"), button -> { this.nextPreset(); })
                        .bounds(centerX - (buttonWidth/2), y, buttonWidth, buttonHeight)
                        .build()
        );
        addRenderableWidget(
                Button.builder(Component.translatable("gui.spell_engine.reset"), button -> { this.reset(); })
                        .bounds(centerX + padding + buttonWidth - (buttonWidth/2), y, buttonWidth, buttonHeight)
                        .build()
        );

        setupPartButtons();
    }

    private void setupPartButtons() {
        var centerX = (width / 2);
        var centerY = (height / 2);
        var buttonWidth = 60;

        int y = centerY - 20;
        int x = centerX - (buttonWidth/2);
        var targetButtons = createPartAdjustmentButtons(Part.TARGET, x, y);
        targetButtons.forEach(this::addRenderableWidget);
        partButtons.addAll(targetButtons);
        y += 28;
        var iconButtons = createPartAdjustmentButtons(Part.ICON, x, y);
        iconButtons.forEach(this::addRenderableWidget);
        partButtons.addAll(iconButtons);
        y += 28;
        var sizeButtons = createBarSizeButtons(x, y);
        sizeButtons.forEach(this::addRenderableWidget);
        partButtons.addAll(sizeButtons);

        setPartsVisibility(partConfigVisible);
    }

    enum Part { TARGET, ICON }
    enum Direction { LEFT, RIGHT, UP, DOWN }

    private List<AbstractWidget> createPartAdjustmentButtons(Part part, int x, int y) {
        var buttons = new ArrayList<AbstractWidget>();
        var buttonSize = 20;
        var spacing = 8;

        var checked = partData(part).visible;
        var checkBox = Checkbox.builder(Component.literal(""), font)
                .pos(x, y)
                .selected(checked)
                .build();

        buttons.add(checkBox);
        checkBoxes.put(part, checkBox);

        Button but;

        x += buttonSize + spacing;
        but = Button.builder(Component.literal("\u2190"), button -> { move(part, Direction.LEFT); })
                .bounds(x, y, buttonSize, buttonSize)
                .build();
        addRenderableWidget(but);
        buttons.add(but);

        x += buttonSize + spacing;
        but = Button.builder(Component.literal("\u2191"), button -> { move(part, Direction.UP); })
                .bounds(x, y, buttonSize, buttonSize)
                .build();
        addRenderableWidget(but);
        buttons.add(but);

        x += buttonSize + spacing;
        but = Button.builder(Component.literal("\u2193"), button -> { move(part, Direction.DOWN); })
                .bounds(x, y, buttonSize, buttonSize)
                .build();
        addRenderableWidget(but);
        buttons.add(but);

        x += buttonSize + spacing;
        but = Button.builder(Component.literal("\u2192"), button -> { move(part, Direction.RIGHT); })
                .bounds(x, y, buttonSize, buttonSize)
                .build();
        addRenderableWidget(but);
        buttons.add(but);

        return buttons;
    }

    private List<Button> createBarSizeButtons(int x, int y) {
        var buttons = new ArrayList<Button>();
        var buttonSize = 20;
        var spacing = 8;
        Button but;

        but = Button.builder(Component.literal("-"), button -> { changeBarWidth(false); })
                .bounds(x, y, buttonSize, buttonSize)
                .build();
        addRenderableWidget(but);
        buttons.add(but);

        x += buttonSize + spacing;
        but = Button.builder(Component.literal("+"), button -> { changeBarWidth(true); })
                .bounds(x, y, buttonSize, buttonSize)
                .build();
        addRenderableWidget(but);
        buttons.add(but);

        return buttons;
    }

    private void move(Part partType, Direction direction) {
        var part = partData(partType);
        if (part != null) {
            Vec2 diff = Vec2.ZERO;
            switch (direction) {
                case LEFT -> {
                    diff = new Vec2(-1, 0);
                }
                case RIGHT -> {
                    diff = new Vec2(1, 0);
                }
                case UP -> {
                    diff = new Vec2(0, -1);
                }
                case DOWN -> {
                    diff = new Vec2(0, 1);
                }
            }
            part.offset = part.offset.add(diff);
        }
    }

    private HudConfig.Part partData(Part partType) {
        switch (partType) {
            case TARGET -> {
                return config().castbar.target;
            }
            case ICON -> {
                return config().castbar.icon;
            }
        }
        assert true;
        return null;
    }

    private void changeBarWidth(boolean increase) {
        var diff = increase ? 1 : -1;
        var config = SpellEngineClient.hudConfig.value;
        if (!increase && config.castbar.width <= 0) {
            return;
        }
        config.castbar.width += diff;
    }

    private boolean partsVisible() {
        return partConfigVisible;
    }

    private void toggleParts() {
        setPartsVisibility(!partConfigVisible);
    }

    private void setPartsVisibility(boolean visibility) {
        partConfigVisible = visibility;
        for(var button: partButtons) {
            button.visible = partConfigVisible;
        }
    }

    @Override
    public void onClose() {
        this.save();
        this.minecraft.setScreen(previous);
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        HudRenderHelper.render(context, delta, true);
        if (partsVisible()) {
            var bigButtonWidth = 60;
            var centerX = (width / 2);
            var centerY = (height / 2);
            var lineSpacing = 28;
            int x = centerX - (bigButtonWidth/2) - 8;
            int y = centerY - 20 + 6;
            rightAlignedText(context, x, y, "gui.spell_engine.target");
            y += lineSpacing;
            rightAlignedText(context, x, y, "gui.spell_engine.icon");
            y += lineSpacing;
            rightAlignedText(context, x, y, "gui.spell_engine.bar_width");
        }
        for (var entry: checkBoxes.entrySet()) {
            partData(entry.getKey()).visible = entry.getValue().selected();
        }
    }

    private void rightAlignedText(GuiGraphics context, int x, int y, String text) {
        var translated = I18n.get(text);
        var width = font.width(translated);
        context.drawString(font, translated, x - width, y, 0xFFFFFF, false);
    }

    private Dragged dragged;
    private enum Dragged {
        CAST_BAR, HOT_BAR, ERROR_MESSAGE
    }

    // Note: mouseClicked/Released/Dragged signatures changed in 1.21.11 - avoid @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (HudRenderHelper.CastBarWidget.lastRendered != null && HudRenderHelper.CastBarWidget.lastRendered.contains(mouseX, mouseY)) {
            dragged = Dragged.CAST_BAR;
            return true;
        }
        if (HudRenderHelper.SpellHotBarWidget.lastRendered != null && HudRenderHelper.SpellHotBarWidget.lastRendered.contains(mouseX, mouseY)) {
            dragged = Dragged.HOT_BAR;
            return true;
        }
        if (HudRenderHelper.ErrorMessageWidget.lastRendered != null && HudRenderHelper.ErrorMessageWidget.lastRendered.contains(mouseX, mouseY)) {
            dragged = Dragged.ERROR_MESSAGE;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragged = null;
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragged != null) {
            var config = SpellEngineClient.hudConfig.value;
            switch (dragged) {
                case CAST_BAR -> {
                    config.castbar.base.offset = new Vec2(
                            (float) (config.castbar.base.offset.x + deltaX),
                            (float) (config.castbar.base.offset.y + deltaY));

                }
                case HOT_BAR -> {
                    config.hotbar.offset = new Vec2(
                            (float) (config.hotbar.offset.x + deltaX),
                            (float) (config.hotbar.offset.y + deltaY));
                }
                case ERROR_MESSAGE -> {
                    config.error_message.offset = new Vec2(
                            (float) (config.error_message.offset.x + deltaX),
                            (float) (config.error_message.offset.y + deltaY));
                }
            }
            return true;
        }
        return false;
    }

    private int lastSelectedPreset = 0;

    public void nextPreset() {
        lastSelectedPreset += 1;
        try {
            SpellEngineClient.hudConfig.value = HudConfig.presets.get(lastSelectedPreset).copy();
        } catch (Exception e) {
            lastSelectedPreset = 0;
            SpellEngineClient.hudConfig.value = HudConfig.presets.get(0).copy();
        }
        refreshPartButtons();
    }

    public void save() {
        SpellEngineClient.hudConfig.save();
    }

    public void reset() {
        SpellEngineClient.hudConfig.value = HudConfig.createDefault();
        refreshPartButtons();
    }

    private void refreshPartButtons() {
        for(var partButton: partButtons) {
            removeWidget(partButton);
        }
        partButtons.clear();
        setupPartButtons();
    }
}
