package com.ultra.megamod.lib.emf.config.screen;

import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.config.EMFConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla {@code Screen}-based EMF config UI. Styled to match the ETF config
 * screen for consistency: left-aligned label column, right-aligned control
 * column, tab buttons at the top, Done + Save at the bottom.
 * <p>
 * Every field on {@link EMFConfig} is surfaced through one of three row types:
 * {@code bool} (ON/OFF button), {@code cycleEnum} (cycling button), or
 * {@code intSlider} (range slider).
 */
public class EMFConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int BUTTON_WIDTH = 160;
    private static final int LABEL_COL_X = 20;
    private static final int CONTROL_COL_X = 220;
    private static final int FIRST_ROW_Y = 50;

    private final @Nullable Screen parent;
    private final List<Tab> tabs = new ArrayList<>();
    private int activeTab = 0;

    public EMFConfigScreen(@Nullable Screen parent) {
        super(Component.literal("EMF — Entity Model Features"));
        this.parent = parent;
        buildTabs();
    }

    private void buildTabs() {
        EMFConfig cfg = EMF.config().getConfig();

        tabs.add(new Tab("General", List.of(
                Row.cycleEnum("Update Frequency", "How often per-entity model variants re-evaluate.",
                        EMFConfig.UpdateFrequency.values(), () -> cfg.modelUpdateFrequency, v -> cfg.modelUpdateFrequency = v),
                Row.bool("EBE Config Modify", "Allow EMF to modify Enhanced Block Entities config for compatibility.",
                        () -> cfg.allowEBEModConfigModify, v -> cfg.allowEBEModConfigModify = v),
                Row.bool("Double-chest Anim Fix", "Run a compatibility patch for double chest animations.",
                        () -> cfg.doubleChestAnimFix, v -> cfg.doubleChestAnimFix = v)
        )));

        tabs.add(new Tab("Player", List.of(
                Row.bool("Prevent First-Person Hand", "Don't run .jem animations on the first-person hand.",
                        () -> cfg.preventFirstPersonHandAnimating, v -> cfg.preventFirstPersonHandAnimating = v),
                Row.bool("Only Client Player Model", "Restrict player-model EMF overrides to the local client only.",
                        () -> cfg.onlyClientPlayerModel, v -> cfg.onlyClientPlayerModel = v),
                Row.bool("Reset Player Each Render", "Reset the player model transforms at each render call.",
                        () -> cfg.resetPlayerModelEachRender, v -> cfg.resetPlayerModelEachRender = v)
        )));

        tabs.add(new Tab("Performance", List.of(
                Row.intSlider("Animation LOD Distance", 0, 65, "Blocks beyond which animations simplify.",
                        () -> cfg.animationLODDistance, v -> cfg.animationLODDistance = v),
                Row.bool("Retain Detail On Low FPS", "Skip LOD simplification when FPS is low.",
                        () -> cfg.retainDetailOnLowFps, v -> cfg.retainDetailOnLowFps = v),
                Row.bool("Retain Detail On Larger Mobs", "Skip LOD simplification for big mobs.",
                        () -> cfg.retainDetailOnLargerMobs, v -> cfg.retainDetailOnLargerMobs = v),
                Row.bool("Skip Iris Shadow Pass", "Skip EMF animations during Iris's shadow render pass.",
                        () -> cfg.animationFrameSkipDuringIrisShadowPass, v -> cfg.animationFrameSkipDuringIrisShadowPass = v)
        )));

        tabs.add(new Tab("OptiFine", List.of(
                Row.bool("Variation Requires Base", "Only load variants when the base .jem exists.",
                        () -> cfg.enforceOptifineVariationRequiresDefaultModel, v -> cfg.enforceOptifineVariationRequiresDefaultModel = v),
                Row.bool("Subfolders Variant-Only", "Only treat subfolder files as variant lookups.",
                        () -> cfg.enforceOptifineSubFoldersVariantOnly, v -> cfg.enforceOptifineSubFoldersVariantOnly = v),
                Row.bool("Enforce OptiFine Syntax", "Reject EMF syntax extensions.",
                        () -> cfg.enforceOptiFineAnimSyntaxLimits, v -> cfg.enforceOptiFineAnimSyntaxLimits = v),
                Row.bool("Allow OptiFine Fallback Properties", "Fall back to OptiFine-location .properties files.",
                        () -> cfg.allowOptifineFallbackProperties, v -> cfg.allowOptifineFallbackProperties = v)
        )));

        tabs.add(new Tab("Debug", List.of(
                Row.cycleEnum("Render Mode", "Model render mode — green / lines / none.",
                        EMFConfig.RenderModeChoice.values(), () -> cfg.renderModeChoice, v -> cfg.renderModeChoice = v),
                Row.cycleEnum("Vanilla Hologram", "Show vanilla model overlay for comparison.",
                        EMFConfig.VanillaModelRenderMode.values(), () -> cfg.vanillaModelHologramRenderMode, v -> cfg.vanillaModelHologramRenderMode = v),
                Row.cycleEnum("Model Export Mode", "Export loaded model data to log / .jem file.",
                        EMFConfig.ModelPrintMode.values(), () -> cfg.modelExportMode, v -> cfg.modelExportMode = v),
                Row.bool("Log Model Creation", "Verbose logs on every .jem load.",
                        () -> cfg.logModelCreationData, v -> cfg.logModelCreationData = v),
                Row.bool("Debug On Right-Click", "Dump model info when right-clicking an entity.",
                        () -> cfg.debugOnRightClick, v -> cfg.debugOnRightClick = v),
                Row.bool("Only Debug On Hover", "Limit render-mode overlays to hovered entity.",
                        () -> cfg.onlyDebugRenderOnHover, v -> cfg.onlyDebugRenderOnHover = v),
                Row.bool("Show Reload Error Toast", "Toast on reload-time errors.",
                        () -> cfg.showReloadErrorToast, v -> cfg.showReloadErrorToast = v),
                Row.bool("Export Rotations", "Include rotation values in exported .jem output.",
                        () -> cfg.exportRotations, v -> cfg.exportRotations = v),
                Row.bool("Show Debug HUD", "Render EMF stats HUD overlay.",
                        () -> cfg.showDebugHud, v -> cfg.showDebugHud = v)
        )));
    }

    @Override
    protected void init() {
        super.init();
        int tabX = 10;
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            int idx = i;
            Button b = Button.builder(Component.literal(t.name), btn -> {
                activeTab = idx;
                rebuild();
            }).bounds(tabX, 8, 70, 20).build();
            if (idx == activeTab) b.active = false;
            addRenderableWidget(b);
            tabX += 74;
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save"), btn -> EMF.config().saveToFile())
                .bounds(this.width / 2 + 5, this.height - 28, 95, 20).build());

        layoutRows();
    }

    private void rebuild() {
        this.clearWidgets();
        init();
    }

    private void layoutRows() {
        Tab tab = tabs.get(activeTab);
        int y = FIRST_ROW_Y;
        for (Row row : tab.rows) {
            row.attach(this, CONTROL_COL_X, y);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.getTitle(), this.width / 2, 32, 0xFFFFFF);

        Tab tab = tabs.get(activeTab);
        int y = FIRST_ROW_Y + 6;
        for (Row row : tab.rows) {
            g.drawString(this.font, row.label, LABEL_COL_X, y, 0xCCCCCC);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public void onClose() {
        EMF.config().saveToFile();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    /** Describes a configurable row: a label + a factory that installs the widget. */
    private static final class Row {
        final String label;
        final Attacher attacher;

        private Row(String label, Attacher attacher) {
            this.label = label;
            this.attacher = attacher;
        }

        void attach(EMFConfigScreen screen, int x, int y) {
            attacher.attach(screen, x, y);
        }

        static Row bool(String label, String tooltip,
                        java.util.function.BooleanSupplier get,
                        java.util.function.Consumer<Boolean> set) {
            return new Row(label, (screen, x, y) -> {
                boolean[] current = new boolean[]{get.getAsBoolean()};
                Button[] holder = new Button[1];
                holder[0] = Button.builder(
                                Component.literal(current[0] ? "ON" : "OFF"),
                                btn -> {
                                    current[0] = !current[0];
                                    set.accept(current[0]);
                                    btn.setMessage(Component.literal(current[0] ? "ON" : "OFF"));
                                })
                        .bounds(x, y, BUTTON_WIDTH, 20)
                        .tooltip(Tooltip.create(Component.literal(tooltip)))
                        .build();
                screen.addRenderableWidget(holder[0]);
            });
        }

        static <E extends Enum<E>> Row cycleEnum(String label, String tooltip, E[] values,
                                                 java.util.function.Supplier<E> get,
                                                 java.util.function.Consumer<E> set) {
            return new Row(label, (screen, x, y) -> {
                int[] idx = new int[]{indexOf(values, get.get())};
                if (idx[0] < 0) idx[0] = 0;
                Button[] holder = new Button[1];
                E initial = values[idx[0]];
                holder[0] = Button.builder(
                                Component.literal(initial.name()),
                                btn -> {
                                    idx[0] = (idx[0] + 1) % values.length;
                                    E next = values[idx[0]];
                                    set.accept(next);
                                    btn.setMessage(Component.literal(next.name()));
                                })
                        .bounds(x, y, BUTTON_WIDTH, 20)
                        .tooltip(Tooltip.create(Component.literal(tooltip)))
                        .build();
                screen.addRenderableWidget(holder[0]);
            });
        }

        private static <E> int indexOf(E[] arr, E v) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == v) return i;
            }
            return -1;
        }

        static Row intSlider(String label, int min, int max, String tooltip,
                             java.util.function.IntSupplier get,
                             java.util.function.IntConsumer set) {
            return new Row(label, (screen, x, y) -> {
                double initialFraction = (double) (get.getAsInt() - min) / Math.max(1, (max - min));
                AbstractSliderButton slider = new AbstractSliderButton(x, y, BUTTON_WIDTH, 20,
                        Component.literal(label + ": " + get.getAsInt()), initialFraction) {
                    @Override
                    protected void updateMessage() {
                        int v = (int) Mth.lerp(value, min, max);
                        setMessage(Component.literal(label + ": " + v));
                    }

                    @Override
                    protected void applyValue() {
                        int v = (int) Mth.lerp(value, min, max);
                        set.accept(v);
                    }
                };
                slider.setTooltip(Tooltip.create(Component.literal(tooltip)));
                screen.addRenderableWidget(slider);
            });
        }
    }

    @FunctionalInterface
    private interface Attacher {
        void attach(EMFConfigScreen screen, int x, int y);
    }

    private record Tab(String name, List<Row> rows) {
    }
}
