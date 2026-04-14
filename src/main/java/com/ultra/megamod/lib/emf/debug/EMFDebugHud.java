package com.ultra.megamod.lib.emf.debug;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.runtime.EmfActiveModel;
import com.ultra.megamod.lib.emf.runtime.EmfEntityVariantCache;
import com.ultra.megamod.lib.emf.runtime.EmfModelBinder;
import com.ultra.megamod.lib.emf.runtime.EmfModelManager;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * F3-style debug overlay for the EMF port.
 * <p>
 * Shows a panel in the top-left with:
 * <ul>
 *   <li>Number of active EMF model bindings (cache size)</li>
 *   <li>Current entity key + bound .jem (if any) for the entity being rendered</li>
 *   <li>Whether the active model declares a texture override</li>
 *   <li>Current cached UUID-variant count</li>
 * </ul>
 * Opt-in via {@code showDebugHud} on {@link com.ultra.megamod.lib.emf.config.EMFConfig}
 * — disabled by default.
 */
public final class EMFDebugHud {

    private static final int BG = 0xC0101010;
    private static final int BORDER = 0xFFFFB040;
    private static final int LABEL = 0xFFFFCC80;
    private static final int VALUE = 0xFFFFFFFF;

    private EMFDebugHud() {
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(MegaMod.MODID, "emf_debug_hud"),
                EMFDebugHud::render);
    }

    private static void render(GuiGraphics g, DeltaTracker tracker) {
        if (!EMF.config().getConfig().showDebugHud) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int x = 4;
        int y = 170;
        int panelW = 260;
        int lineH = 10;
        int line = 0;

        EmfModelManager mgr = EmfModelManager.getInstance();
        int cacheSize = mgr.snapshot().size();

        ETFEntityRenderState state = ETFRenderContext.getCurrentEntityState();
        String entityKey = EmfModelBinder.deriveEntityTypeKey(state);

        EmfActiveModel active = null;
        if (entityKey != null && !entityKey.isEmpty()) {
            active = mgr.bindForEntity(entityKey, state);
        }

        int totalLines = 7;
        int panelH = 6 + totalLines * lineH + 4;

        g.fill(x, y, x + panelW, y + panelH, BG);
        g.fill(x, y, x + panelW, y + 1, BORDER);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, BORDER);
        g.fill(x, y, x + 1, y + panelH, BORDER);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, BORDER);

        int tx = x + 4;
        int ty = y + 4;
        g.drawString(mc.font, "EMF Debug", tx, ty + line * lineH, BORDER);
        line++;
        g.drawString(mc.font, "Models cached: " + cacheSize, tx, ty + line * lineH, VALUE);
        line++;
        g.drawString(mc.font, "Entity: " + (entityKey != null ? entityKey : "-"), tx, ty + line * lineH, VALUE);
        line++;
        if (active != null) {
            g.drawString(mc.font, ".jem: " + active.sourceJemId, tx, ty + line * lineH, VALUE);
            line++;
            g.drawString(mc.font, "Tex override: "
                            + (active.jemTextureOverride == null ? "-" : active.jemTextureOverride.toString()),
                    tx, ty + line * lineH, VALUE);
            line++;
            g.drawString(mc.font, "Bones animated: " + active.definition.animationsByBone.size(),
                    tx, ty + line * lineH, VALUE);
            line++;
        } else {
            g.drawString(mc.font, ".jem: -", tx, ty + line * lineH, LABEL);
            line++;
            g.drawString(mc.font, "Tex override: -", tx, ty + line * lineH, LABEL);
            line++;
            g.drawString(mc.font, "Bones animated: -", tx, ty + line * lineH, LABEL);
            line++;
        }
        // Variant cache summary: entity suffix if known
        if (state != null) {
            Integer cachedSuffix = active != null
                    ? EmfEntityVariantCache.getInstance().getCachedSuffix(state.uuid(), active.sourceJemId)
                    : null;
            g.drawString(mc.font, "Variant: " + (cachedSuffix == null ? "-" : String.valueOf(cachedSuffix)),
                    tx, ty + line * lineH, VALUE);
        } else {
            g.drawString(mc.font, "Variant: -", tx, ty + line * lineH, LABEL);
        }
    }
}
