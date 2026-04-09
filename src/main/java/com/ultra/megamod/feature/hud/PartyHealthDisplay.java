package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.hud.network.PartyHealthPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.List;

/**
 * Displays party member health bars on the left edge of the screen.
 * Shows name, health bar, and HP text for each party member.
 */
public class PartyHealthDisplay {

    private static final int PANEL_W = 90;
    private static final int MEMBER_H = 22;
    private static final int BAR_H = 4;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "party_health"),
            PartyHealthDisplay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        List<PartyHealthPayload.MemberInfo> members = PartyHealthPayload.clientMembers;
        if (members.isEmpty()) return;

        int x = 4;
        int y = 76; // Below PlayerInfoHud (panel ends at y=72)

        int totalH = members.size() * MEMBER_H + 4;
        UIHelper.drawHudPanel(g, x, y, PANEL_W, totalH);

        int my = y + 2;
        for (PartyHealthPayload.MemberInfo m : members) {
            // Name (truncate if long)
            String name = m.name();
            if (name.length() > 10) name = name.substring(0, 9) + "..";
            int nameColor = m.online() ? 0xFFCCCCCC : 0xFF666666;
            g.drawString(mc.font, name, x + 4, my + 1, nameColor, false);

            // HP text
            String hpText = String.format("%.0f/%.0f", m.health(), m.maxHealth());
            int hpW = mc.font.width(hpText);
            g.drawString(mc.font, hpText, x + PANEL_W - hpW - 4, my + 1, 0xFF999999, false);

            // Health bar
            int barX = x + 4;
            int barY = my + 12;
            int barW = PANEL_W - 8;

            // Background
            g.fill(barX, barY, barX + barW, barY + BAR_H, 0xFF333333);

            // Fill
            float pct = m.maxHealth() > 0 ? Math.clamp(m.health() / m.maxHealth(), 0, 1) : 0;
            int fillW = (int)(barW * pct);
            int barColor = pct > 0.5f ? 0xFF44CC44 : (pct > 0.25f ? 0xFFCCCC44 : 0xFFCC4444);
            if (!m.online()) barColor = 0xFF555555;
            if (fillW > 0) {
                g.fill(barX, barY, barX + fillW, barY + BAR_H, barColor);
            }

            my += MEMBER_H;
        }
    }
}
