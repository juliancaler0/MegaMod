package com.ultra.megamod.feature.mobhealth;

import com.ultra.megamod.feature.ui.UIHelper;
import java.util.UUID;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public class MobHealthDisplay {
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 5;
    private static final int HUD_TEXT_DIM = -5728136;
    private static final int SPEED_COLOR = 0xFF88CCFF;
    private static final int HOSTILE_COLOR = 0xFFFF5555;
    private static final int PASSIVE_COLOR = 0xFF55FF55;
    private static final int NEUTRAL_COLOR = 0xFFFFFF55;
    private static final int TAMED_COLOR = 0xFFFF88AA;
    private static final int VILLAGER_COLOR = 0xFFFFAA00;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "mob_health_display"),
                MobHealthDisplay::renderHealthDisplay);
    }

    private static void renderHealthDisplay(GuiGraphics graphics, DeltaTracker partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null || mc.options.hideGui) return;

        Entity entity = mc.crosshairPickEntity;
        if (!(entity instanceof LivingEntity target)) return;

        float currentHealth = target.getHealth();
        float maxHealth = target.getMaxHealth();
        if (maxHealth <= 0.0f) return;

        float healthPercent = Math.clamp(currentHealth / maxHealth, 0.0f, 1.0f);
        int screenWidth = graphics.guiWidth();
        int centerX = screenWidth / 2;

        // Build display name — prefix "Baby" for baby mobs
        Component name = target.getDisplayName();
        if (target.isBaby()) {
            name = Component.literal("Baby ").append(name);
        }

        // Entity type coloring: red=hostile, green=passive, yellow=neutral/NPC
        int nameColor = getEntityTypeColor(target);

        int nameWidth = mc.font.width((FormattedText) name);
        String healthText = String.format("%.1f / %.1f", currentHealth, maxHealth);
        int healthTextWidth = mc.font.width(healthText);

        // Extra info lines (horse stats, villager info, tamed owner)
        String extraLine1 = null;
        int extraLine1Width = 0;
        int extraLine1Color = SPEED_COLOR;

        String extraLine2 = null;
        int extraLine2Width = 0;
        int extraLine2Color = TAMED_COLOR;

        // Horse-specific: speed and jump height
        if (target instanceof AbstractHorse horse) {
            double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
            double blocksPerSec = speed * 43.178;
            double jump = horse.getAttributeValue(Attributes.JUMP_STRENGTH);
            double jumpBlocks = jumpStrengthToBlocks(jump);
            extraLine1 = String.format("Speed: %.1f b/s | Jump: %.1f blocks", blocksPerSec, jumpBlocks);
            extraLine1Width = mc.font.width(extraLine1);
            extraLine1Color = SPEED_COLOR;
        }

        // Villager profession and level
        if (target instanceof Villager villager) {
            Holder<VillagerProfession> profession = villager.getVillagerData().profession();
            int level = villager.getVillagerData().level();
            String profName = formatProfession(profession);
            if (profName.equals("None")) {
                extraLine1 = "Unemployed";
            } else if (profName.equals("Nitwit")) {
                extraLine1 = "Nitwit";
            } else {
                extraLine1 = profName + " Lv " + level;
            }
            extraLine1Width = mc.font.width(extraLine1);
            extraLine1Color = VILLAGER_COLOR;
        }

        // Tamed pet indicator with owner name
        if (target instanceof TamableAnimal tamed && tamed.isTame()) {
            LivingEntity owner = tamed.getOwner();
            if (owner != null) {
                String ownerName = owner instanceof Player p ? p.getGameProfile().name() : "???";
                extraLine2 = "\u2764 Tamed by " + ownerName;
                extraLine2Width = mc.font.width(extraLine2);
            }
        }

        // Calculate content width
        int contentWidth = Math.max(BAR_WIDTH, Math.max(nameWidth, healthTextWidth));
        if (extraLine1 != null) contentWidth = Math.max(contentWidth, extraLine1Width);
        if (extraLine2 != null) contentWidth = Math.max(contentWidth, extraLine2Width);

        int padX = 6;
        int padY = 4;

        // Position: centered, just below compass (compass ends at ~y=16)
        int panelY = 18;
        int panelX = centerX - contentWidth / 2 - padX;
        int panelW = contentWidth + padX * 2;

        // Panel height: name(12) + bar(5) + gap(2) + healthText(10) + optional extras(10 each)
        int innerH = 12 + BAR_HEIGHT + 2 + 10;
        if (extraLine1 != null) innerH += 10;
        if (extraLine2 != null) innerH += 10;
        int panelH = padY + innerH + padY;

        UIHelper.drawHudPanel(graphics, panelX, panelY, panelW, panelH);

        int drawY = panelY + padY;

        // Entity name with type coloring
        graphics.drawString(mc.font, name, centerX - nameWidth / 2, drawY, nameColor);
        drawY += 12;

        // Health bar border + fill
        int barLeft = centerX - BAR_WIDTH / 2;
        int barRight = centerX + BAR_WIDTH / 2;
        graphics.fill(barLeft - 1, drawY - 1, barRight + 1, drawY + BAR_HEIGHT + 1, 0xFF191919);
        int barColor = getHealthColor(healthPercent);
        int filledWidth = (int) (BAR_WIDTH * healthPercent);
        if (filledWidth > 0) {
            graphics.fill(barLeft, drawY, barLeft + filledWidth, drawY + BAR_HEIGHT, barColor);
        }
        drawY += BAR_HEIGHT + 2;

        // Health text
        graphics.drawString(mc.font, healthText, centerX - healthTextWidth / 2, drawY, HUD_TEXT_DIM);
        drawY += 10;

        // Extra line 1: horse stats or villager profession
        if (extraLine1 != null) {
            graphics.drawString(mc.font, extraLine1, centerX - extraLine1Width / 2, drawY, extraLine1Color);
            drawY += 10;
        }

        // Extra line 2: tamed pet owner
        if (extraLine2 != null) {
            graphics.drawString(mc.font, extraLine2, centerX - extraLine2Width / 2, drawY, extraLine2Color);
        }
    }

    private static int getEntityTypeColor(LivingEntity entity) {
        if (entity instanceof Monster) return HOSTILE_COLOR;
        if (entity instanceof Animal) return PASSIVE_COLOR;
        return NEUTRAL_COLOR;
    }

    private static String formatProfession(Holder<VillagerProfession> profession) {
        return profession.unwrapKey()
                .map(key -> toTitleCase(key.identifier().getPath()))
                .orElse("Unknown");
    }

    private static String toTitleCase(String snakeCase) {
        StringBuilder sb = new StringBuilder();
        for (String word : snakeCase.split("_")) {
            if (!word.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private static double jumpStrengthToBlocks(double strength) {
        return -0.1817584952 * strength * strength * strength
             + 3.689713992 * strength * strength
             + 2.128599134 * strength
             - 0.343930367;
    }

    private static int getHealthColor(float percent) {
        int red, green;
        if (percent > 0.5f) {
            float t = (percent - 0.5f) * 2.0f;
            green = 255;
            red = (int) (255.0f * (1.0f - t));
        } else {
            float t = percent * 2.0f;
            red = 255;
            green = (int) (255.0f * t);
        }
        return 0xFF000000 | (red << 16) | (green << 8);
    }
}
