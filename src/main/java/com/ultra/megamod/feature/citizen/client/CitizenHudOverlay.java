package com.ultra.megamod.feature.citizen.client;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;

import java.util.List;

public class CitizenHudOverlay {

    private static long cachedOwnedCount = 0;
    private static long lastCountTick = -1;
    private static final int COUNT_UPDATE_INTERVAL = 40; // Update every 2 seconds

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            Identifier.fromNamespaceAndPath("megamod", "citizen_hud"),
            CitizenHudOverlay::render
        );
    }

    private static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Show citizen info when looking at one
        HitResult hit = mc.hitResult;
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (target instanceof MCEntityCitizen citizen) {
                renderCitizenTooltip(g, mc, citizen);
            }
        }

        // Update owned citizen count periodically instead of every frame
        long currentTick = mc.level.getGameTime();
        if (currentTick - lastCountTick >= COUNT_UPDATE_INTERVAL) {
            lastCountTick = currentTick;
            long count = 0;
            List<Entity> nearbyEntities = mc.level.getEntities(mc.player,
                mc.player.getBoundingBox().inflate(64), e -> e instanceof MCEntityCitizen);
            for (Entity e : nearbyEntities) {
                if (e instanceof MCEntityCitizen c) {
                    // TODO: MCEntityCitizen uses colony-based ownership, count all nearby for now
                    count++;
                }
            }
            cachedOwnedCount = count;
        }

        if (cachedOwnedCount > 0) {
            String text = "Citizens: " + cachedOwnedCount;
            int x = g.guiWidth() - mc.font.width(text) - 4;
            int y = 4;
            g.fill(x - 2, y - 1, x + mc.font.width(text) + 2, y + 10, 0x88000000);
            g.drawString(mc.font, text, x, y, 0xFFCCCCDD, false);
        }
    }

    private static void renderCitizenTooltip(GuiGraphics g, Minecraft mc, MCEntityCitizen citizen) {
        int x = g.guiWidth() / 2;
        int y = g.guiHeight() / 2 + 20;

        String name = citizen.getCitizenName();
        String job = citizen.getCitizenJobHandler().getColonyJob() != null
                ? citizen.getCitizenJobHandler().getColonyJob().getDisplayName() : "Unemployed";
        String hp = String.format("%.0f/%.0f HP", citizen.getHealth(), citizen.getMaxHealth());

        int maxW = Math.max(mc.font.width(name + " - " + job), mc.font.width(hp));
        int panelW = maxW + 8;
        int panelH = 26;
        int px = x - panelW / 2;

        g.fill(px, y, px + panelW, y + panelH, 0xCC0E0E18);
        g.fill(px, y, px + panelW, y + 1, 0xFF3A3A52);

        g.drawCenteredString(mc.font, name + " - " + job, x, y + 2, 0xFFFFD700);
        g.drawCenteredString(mc.font, hp, x, y + 13, citizen.getHealth() > citizen.getMaxHealth() * 0.5f ? 0xFF44BB44 : 0xFFBB4444);
    }
}
