package com.ultra.megamod.lib.etf.debug;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * F3-style debug overlay that surfaces ETF runtime stats when the user enables
 * {@code showDebugHud} in the config. Renders a small panel in the top-left corner with:
 * <ul>
 *   <li>Pack order head</li>
 *   <li>ETF texture / variator cache sizes</li>
 *   <li>Current entity, its variator suffix, current texture + emissive/enchant flags</li>
 * </ul>
 * Registered on the top-of-HUD layer so it sits above other overlays.
 */
public final class ETFDebugHud {

    private static final int BG = 0xC0101010;
    private static final int BORDER = 0xFF40A0FF;
    private static final int LABEL = 0xFF80C0FF;
    private static final int VALUE = 0xFFFFFFFF;

    private ETFDebugHud() {}

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(MegaMod.MODID, "etf_debug_hud"),
                ETFDebugHud::render);
    }

    private static void render(GuiGraphics g, DeltaTracker tracker) {
        if (!ETF.config().getConfig().showDebugHud) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int x = 4;
        int y = 60;
        int panelW = 220;
        int lineH = 10;
        int line = 0;

        ETFManager mgr = ETFManager.getInstance();
        int cacheSize = mgr.ETF_TEXTURE_CACHE.size();
        int packCount = mgr.KNOWN_RESOURCEPACK_ORDER.size();
        var currentState = ETFRenderContext.getCurrentEntityState();
        ETFTexture currentTex = ETFRenderContext.getCurrentTexture();

        int totalLines = 7; // title + 6 rows
        int panelH = 6 + totalLines * lineH + 4;

        g.fill(x, y, x + panelW, y + panelH, BG);
        g.fill(x, y, x + panelW, y + 1, BORDER);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, BORDER);
        g.fill(x, y, x + 1, y + panelH, BORDER);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, BORDER);

        int tx = x + 4;
        int ty = y + 4;
        g.drawString(mc.font, "ETF Debug", tx, ty + line * lineH, BORDER);
        line++;
        g.drawString(mc.font, "Packs: " + packCount + "  Cache: " + cacheSize, tx, ty + line * lineH, VALUE);
        line++;
        g.drawString(mc.font, "Entity: " + (currentState != null ? currentState.entityKey() : "-"), tx, ty + line * lineH, VALUE);
        line++;
        if (currentState != null) {
            int suffix = mgr.LAST_SUFFIX_OF_ENTITY.getInt(currentState.uuid());
            int rule = mgr.LAST_RULE_INDEX_OF_ENTITY.getInt(currentState.uuid());
            g.drawString(mc.font, "Variant: " + (suffix == -1 ? "-" : String.valueOf(suffix))
                    + "  Rule: " + (rule == -1 ? "-" : String.valueOf(rule)), tx, ty + line * lineH, VALUE);
        } else {
            g.drawString(mc.font, "Variant: -", tx, ty + line * lineH, VALUE);
        }
        line++;
        g.drawString(mc.font, "Texture: " + (currentTex != null ? currentTex.thisIdentifier : "-"), tx, ty + line * lineH, VALUE);
        line++;
        if (currentTex != null) {
            g.drawString(mc.font, "Emissive: " + currentTex.isEmissive() + "  Enchant: " + currentTex.isEnchanted(),
                    tx, ty + line * lineH, VALUE);
        } else {
            g.drawString(mc.font, "Emissive: -  Enchant: -", tx, ty + line * lineH, LABEL);
        }
        line++;
        g.drawString(mc.font, "ConfigBtn: Keybind opens config screen", tx, ty + line * lineH, LABEL);
    }
}
