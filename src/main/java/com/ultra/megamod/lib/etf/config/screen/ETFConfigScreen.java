package com.ultra.megamod.lib.etf.config.screen;

import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.config.ETFConfig;
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
 * Vanilla {@code Screen}-based ETF config UI. Lays out every field from
 * {@link ETFConfig} as a checkbox, slider or cycle button. On close, the handler is
 * asked to persist the current config to disk.
 */
public class ETFConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int BUTTON_WIDTH = 160;
    private static final int LABEL_COL_X = 20;
    private static final int CONTROL_COL_X = 220;
    private static final int FIRST_ROW_Y = 50;

    private final @Nullable Screen parent;
    private final List<Tab> tabs = new ArrayList<>();
    private int activeTab = 0;

    public ETFConfigScreen(@Nullable Screen parent) {
        super(Component.literal("ETF — Entity Texture Features"));
        this.parent = parent;
        buildTabs();
    }

    private void buildTabs() {
        ETFConfig cfg = ETF.config().getConfig();

        tabs.add(new Tab("Textures", List.of(
                Row.bool("Enable Custom Textures", "Master toggle for random textures.", () -> cfg.enableCustomTextures, v -> cfg.enableCustomTextures = v),
                Row.cycleEnum("Update Frequency", "How often random textures re-evaluate.", ETFConfig.UpdateFrequency.values(), () -> cfg.textureUpdateFrequency_V2, v -> cfg.textureUpdateFrequency_V2 = v),
                Row.bool("Custom Block Entities", "Apply random textures to block entities.", () -> cfg.enableCustomBlockEntities, v -> cfg.enableCustomBlockEntities = v),
                Row.bool("Disable Vanilla Variants", "Skip textures placed in vanilla directories.", () -> cfg.disableVanillaDirectoryVariantTextures, v -> cfg.disableVanillaDirectoryVariantTextures = v)
        )));

        tabs.add(new Tab("Emissive", List.of(
                Row.bool("Enable Emissive", "Enable emissive overlay rendering.", () -> cfg.enableEmissiveTextures, v -> cfg.enableEmissiveTextures = v),
                Row.bool("Emissive Block Entities", "Enable emissive for block entities.", () -> cfg.enableEmissiveBlockEntities, v -> cfg.enableEmissiveBlockEntities = v),
                Row.cycleEnum("Emissive Mode", "DULL keeps shading, BRIGHT is always full-bright.", ETFConfig.EmissiveRenderModes.values(), () -> cfg.emissiveRenderMode, v -> cfg.emissiveRenderMode = v),
                Row.bool("Always Check _e Suffix", "Check the _e suffix even if not declared in optifine.properties.", () -> cfg.alwaysCheckVanillaEmissiveSuffix, v -> cfg.alwaysCheckVanillaEmissiveSuffix = v),
                Row.bool("Armor + Trims", "Apply ETF to armor textures and trims.", () -> cfg.enableArmorAndTrims, v -> cfg.enableArmorAndTrims = v),
                Row.bool("Enchanted Overlays", "Render _enchanted.png overlays as enchant glint.", () -> cfg.enableEnchantedTextures, v -> cfg.enableEnchantedTextures = v)
        )));

        tabs.add(new Tab("Skins", List.of(
                Row.bool("Enable Skin Features", "Apply ETF processing to player skins.", () -> cfg.skinFeaturesEnabled, v -> cfg.skinFeaturesEnabled = v),
                Row.cycleEnum("Transparency Mode", "Which skins get ETF transparency.", ETFConfig.SkinTransparencyMode.values(), () -> cfg.skinTransparencyMode, v -> cfg.skinTransparencyMode = v),
                Row.bool("Transparent Extra Pixels", "Preserve transparency in the extra pixels region.", () -> cfg.skinTransparencyInExtraPixels, v -> cfg.skinTransparencyInExtraPixels = v),
                Row.bool("Enemy Team Skin Features", "Apply skin features to players on enemy teams.", () -> cfg.enableEnemyTeamPlayersSkinFeatures, v -> cfg.enableEnemyTeamPlayersSkinFeatures = v),
                Row.bool("3D Skin Layer Patch", "Enable the 3D skin-layer compatibility patch.", () -> cfg.use3DSkinLayerPatch, v -> cfg.use3DSkinLayerPatch = v)
        )));

        tabs.add(new Tab("Blinking", List.of(
                Row.bool("Enable Blinking", "Enable periodic blink textures on mobs.", () -> cfg.enableBlinking, v -> cfg.enableBlinking = v),
                Row.intSlider("Blink Frequency", 1, 1024, "Ticks between blinks.", () -> cfg.blinkFrequency, v -> cfg.blinkFrequency = v),
                Row.intSlider("Blink Length", 1, 20, "Ticks each blink lasts.", () -> cfg.blinkLength, v -> cfg.blinkLength = v)
        )));

        tabs.add(new Tab("OptiFine", List.of(
                Row.bool("Allow Weird True-Random Skips", "Match OptiFine's odd gap behaviour.", () -> cfg.optifine_allowWeirdSkipsInTrueRandom, v -> cfg.optifine_allowWeirdSkipsInTrueRandom = v),
                Row.bool("Prevent Base In OptiFine Dir", "Ignore base textures placed in optifine/.", () -> cfg.optifine_preventBaseTextureInOptifineDirectory, v -> cfg.optifine_preventBaseTextureInOptifineDirectory = v),
                Row.cycleEnum("Illegal Path Mode", "Allow paths that MC would otherwise reject.", ETFConfig.IllegalPathMode.values(), () -> cfg.illegalPathSupportMode, v -> cfg.illegalPathSupportMode = v)
        )));

        tabs.add(new Tab("Debug", List.of(
                Row.cycleEnum("Debug Log Mode", "Where ETF prints its debug info.", ETFConfig.DebugLogMode.values(), () -> cfg.debugLoggingMode, v -> cfg.debugLoggingMode = v),
                Row.bool("Log Texture Init", "Log every ETFTexture creation.", () -> cfg.logTextureDataInitialization, v -> cfg.logTextureDataInitialization = v),
                Row.bool("Show Debug HUD", "Render the ETF stats HUD overlay (F3-like).", () -> cfg.showDebugHud, v -> cfg.showDebugHud = v)
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
        addRenderableWidget(Button.builder(Component.literal("Save"), btn -> ETF.config().saveToFile())
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
        ETF.config().saveToFile();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    /**
     * Describes a configurable row: a label + a factory that installs the widget.
     */
    private static final class Row {
        final String label;
        final Attacher attacher;

        private Row(String label, Attacher attacher) {
            this.label = label;
            this.attacher = attacher;
        }

        void attach(ETFConfigScreen screen, int x, int y) {
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
        void attach(ETFConfigScreen screen, int x, int y);
    }

    private record Tab(String name, List<Row> rows) {}
}
