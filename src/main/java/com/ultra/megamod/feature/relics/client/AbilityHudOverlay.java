package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.relics.network.AbilityCooldownSyncPayload;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import com.ultra.megamod.feature.relics.network.WeaponAbilitySyncPayload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Ability HUD with two distinct zones:
 * - Weapon abilities: horizontal bar centered above hotbar (cast via Right-Click, cycle via R+Scroll)
 * - Equipment bar: vertical on left side of screen (armor + shield + accessories)
 *   Accessories are selectable: G+Scroll cycles slots, G flips ability, R casts selected ability
 */
public class AbilityHudOverlay {
    private static final int BOX_SIZE = 22;
    private static final int BOX_GAP = 2;
    private static final int ICON_PAD = 3; // (22 - 16) / 2
    private static final int FONT_HEIGHT = 9;
    private static final int LEFT_MARGIN = 4;
    private static final int SECTION_GAP = 8;

    // Colors
    private static final int BOX_FILL = 0xCC111111;
    private static final int BOX_BORDER = 0xBB555555;
    private static final int BOX_SELECTED_BORDER = 0xFFFFAA00;
    private static final int BOX_SELECTED_GLOW = 0x44FFAA00;
    private static final int COOLDOWN_OVERLAY = 0xBB300000;
    private static final int COOLDOWN_TEXT = 0xFFFF6666;
    private static final int TOOLTIP_BG = 0xEE1A1A2E;
    private static final int TOOLTIP_BORDER = 0xCC6644AA;
    private static final int TOOLTIP_NAME = 0xFFFFDD44;
    private static final int TOOLTIP_ABILITY = 0xFFDDDDDD;
    private static final int TOOLTIP_TYPE = 0xFF88AAFF;
    private static final int TOOLTIP_KEYBIND = 0xFF888888;
    private static final int DIVIDER_COLOR = 0x55AAAAAA;

    private static final Map<String, Integer> maxCooldowns = new HashMap<>();
    private static long lastSyncVersion = -1;

    // Hint fade system
    private static long hintShowStartTime = 0;
    private static int lastAbilityHash = 0;
    private static final long HINT_DISPLAY_MS = 2500;
    private static final long HINT_FADE_MS = 500;

    // Cast notification (shows ability name below compass at top of screen)
    private static String castNotificationName = null;
    private static long castNotificationTime = 0;
    private static final long CAST_DISPLAY_MS = 1500;
    private static final long CAST_FADE_MS = 500;

    // Cache ItemStacks for accessories to avoid re-creating every frame
    private static final Map<String, ItemStack> accessoryStackCache = new HashMap<>();

    /** Called from AbilityKeybind when the player casts any ability. */
    public static void showCastNotification(String abilityName) {
        castNotificationName = abilityName;
        castNotificationTime = System.currentTimeMillis();
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "ability_hud"),
                AbilityHudOverlay::renderAbilityHud);
    }

    private static void renderAbilityHud(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) return;
        if (mc.screen != null) return;

        // Rebuild accessories when sync data changes
        long currentVersion = AccessoryPayload.AccessorySyncPayload.syncVersion;
        if (currentVersion != lastSyncVersion) {
            UnifiedAbilityBar.rebuildAccessories();
            rebuildAccessoryStackCache();
            lastSyncVersion = currentVersion;
            hintShowStartTime = System.currentTimeMillis();
        }

        List<UnifiedAbilityBar.AccessoryAbilityEntry> accessories = UnifiedAbilityBar.getAccessoryEntries();
        List<UnifiedAbilityBar.WeaponAbilityEntry> weapons = UnifiedAbilityBar.getWeaponEntries();
        boolean hasAcc = !accessories.isEmpty();
        boolean hasWpn = !weapons.isEmpty();

        // Detect ability set changes to trigger hint
        int currentHash = weapons.hashCode() * 31 + accessories.hashCode();
        if (currentHash != lastAbilityHash) {
            lastAbilityHash = currentHash;
            hintShowStartTime = System.currentTimeMillis();
        }

        Map<String, Integer> relicCd = AbilityCooldownSyncPayload.clientCooldowns;
        if (relicCd == null) relicCd = Map.of();
        Map<String, Integer> weaponCd = WeaponAbilitySyncPayload.clientWeaponCooldowns;
        if (weaponCd == null) weaponCd = Map.of();
        updateMaxCooldowns(relicCd);
        updateMaxCooldowns(weaponCd);

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        // ===== WEAPON HUD (centered horizontal, above hotbar) =====
        if (hasWpn) {
            renderWeaponBar(g, mc, weapons, relicCd, weaponCd, screenW, screenH);
        }

        // ===== LEFT EQUIPMENT BAR (vertical: armor + shield + accessories) =====
        renderLeftEquipmentBar(g, mc, accessories, relicCd, screenW, screenH);

        // ===== FADING HINT TEXT =====
        renderHints(g, mc, hasWpn, hasAcc, screenW, screenH);

        // ===== CAST NOTIFICATION (below compass at top) =====
        renderCastNotification(g, mc, screenW);
    }

    // ========================================================================
    // WEAPON BAR — horizontal, centered above hotbar
    // ========================================================================

    private static void renderWeaponBar(GuiGraphics g, Minecraft mc,
                                         List<UnifiedAbilityBar.WeaponAbilityEntry> weapons,
                                         Map<String, Integer> relicCd,
                                         Map<String, Integer> weaponCd,
                                         int screenW, int screenH) {
        int hotbarTop = screenH - 22;
        int wpnCount = weapons.size();
        int wpnZoneW = wpnCount * BOX_SIZE + (wpnCount - 1) * BOX_GAP;
        int barX = (screenW - wpnZoneW) / 2;
        int barY = hotbarTop - 38 - BOX_SIZE;

        int selectedWpn = UnifiedAbilityBar.getSelectedWeaponAbilityIndex();
        int selectedBoxX = -1;
        String selectedDisplay = null;
        String selectedAbility = null;
        int selectedCd = 0;

        for (int i = 0; i < wpnCount; i++) {
            UnifiedAbilityBar.WeaponAbilityEntry entry = weapons.get(i);
            boolean selected = (i == selectedWpn);
            int bx = barX + i * (BOX_SIZE + BOX_GAP);

            int cd = relicCd.getOrDefault(entry.abilityName(), 0);
            if (cd <= 0 && entry.isRpgSkill()) {
                cd = weaponCd.getOrDefault(entry.abilityName(), 0);
            }

            drawAbilityBox(g, mc, bx, barY, selected, null, cd, entry.abilityName());

            // Small type label in top-right corner
            String label = entry.isRpgSkill() ? "S" : "A";
            g.drawString(mc.font, label, bx + BOX_SIZE - mc.font.width(label) - 2, barY + 2, 0x55FFFFFF, false);

            if (selected) {
                selectedBoxX = bx;
                selectedDisplay = entry.displayName();
                selectedAbility = entry.abilityName();
                selectedCd = cd;
            }
        }

        // Tooltip above selected weapon
        if (selectedBoxX >= 0 && selectedDisplay != null) {
            drawWeaponTooltip(g, mc, selectedBoxX, barY, selectedDisplay, selectedAbility, selectedCd);
        }
    }

    // ========================================================================
    // LEFT EQUIPMENT BAR — vertical: armor + shield + accessories
    // ========================================================================

    private static void renderLeftEquipmentBar(GuiGraphics g, Minecraft mc,
                                                List<UnifiedAbilityBar.AccessoryAbilityEntry> accessories,
                                                Map<String, Integer> relicCd,
                                                int screenW, int screenH) {
        // Gather armor and shield from player
        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = mc.player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = mc.player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack offhand = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);

        // Skip offhand from equipment display if it has shield abilities (shown in accessory section instead)
        boolean shieldInAccessories = UnifiedAbilityBar.hasShieldAbilities();
        ItemStack[] equipSlots = shieldInAccessories
                ? new ItemStack[]{ helmet, chestplate, leggings, boots }
                : new ItemStack[]{ helmet, chestplate, leggings, boots, offhand };

        // Count non-empty equipment slots
        int equipCount = 0;
        for (ItemStack s : equipSlots) {
            if (!s.isEmpty()) equipCount++;
        }
        boolean hasEquip = equipCount > 0;
        boolean hasAcc = !accessories.isEmpty();
        if (!hasEquip && !hasAcc) return;

        // Calculate total height of the vertical bar
        int equipHeight = equipCount * BOX_SIZE + Math.max(equipCount - 1, 0) * BOX_GAP;
        int dividerHeight = (hasEquip && hasAcc) ? SECTION_GAP : 0;
        int accHeight = hasAcc ? accessories.size() * BOX_SIZE + (accessories.size() - 1) * BOX_GAP : 0;
        int totalHeight = equipHeight + dividerHeight + accHeight;

        // Vertically center on screen
        int startY = (screenH - totalHeight) / 2;
        int x = LEFT_MARGIN;
        int cursorY = startY;

        // ----- Armor + Shield (display only) -----
        for (ItemStack stack : equipSlots) {
            if (stack.isEmpty()) continue;
            drawEquipmentBox(g, mc, x, cursorY, stack);
            cursorY += BOX_SIZE + BOX_GAP;
        }

        // ----- Divider between equipment and accessories -----
        if (hasEquip && hasAcc) {
            int divY = cursorY + SECTION_GAP / 2 - 1;
            g.fill(x + 2, divY, x + BOX_SIZE - 2, divY + 1, DIVIDER_COLOR);
            cursorY += SECTION_GAP;
        }

        // ----- Accessory boxes (selectable, with abilities) -----
        if (!hasAcc) return;

        int selectedAcc = UnifiedAbilityBar.getSelectedAccessoryIndex();
        int selectedBoxY = -1;
        String selectedName = null;
        String selectedAbility = null;
        int selectedCd = 0;
        int selectedAbilityCount = 0;
        int selectedAbilityIdx = 0;
        ItemStack selectedStack = ItemStack.EMPTY;

        for (int i = 0; i < accessories.size(); i++) {
            UnifiedAbilityBar.AccessoryAbilityEntry entry = accessories.get(i);
            boolean selected = (i == selectedAcc);
            int by = cursorY + i * (BOX_SIZE + BOX_GAP);

            int abilityIdx = UnifiedAbilityBar.getSelectedAbilityIndex(entry.slotName());
            String abilityName = abilityIdx < entry.castableAbilities().size()
                    ? entry.castableAbilities().get(abilityIdx) : "";
            int cd = relicCd.getOrDefault(abilityName, 0);

            ItemStack iconStack;
            if ("OFFHAND".equals(entry.slotName()) && mc.player != null) {
                iconStack = mc.player.getItemBySlot(EquipmentSlot.OFFHAND);
            } else {
                iconStack = accessoryStackCache.getOrDefault(entry.itemId(), ItemStack.EMPTY);
            }
            drawAbilityBox(g, mc, x, by, selected, iconStack, cd, abilityName);

            // Multi-ability dots to the right of the box (vertical)
            if (entry.castableAbilities().size() > 1) {
                int dotCount = entry.castableAbilities().size();
                int dotTotalH = dotCount * 3 + (dotCount - 1);
                int dotStartY = by + (BOX_SIZE - dotTotalH) / 2;
                for (int d = 0; d < dotCount; d++) {
                    int dotColor = (d == abilityIdx) ? 0xFFFFDD44 : 0x66888888;
                    g.fill(x + BOX_SIZE + 2, dotStartY + d * 4,
                           x + BOX_SIZE + 4, dotStartY + d * 4 + 2, dotColor);
                }
            }

            if (selected) {
                selectedBoxY = by;
                selectedName = entry.relicName();
                selectedAbility = abilityName;
                selectedCd = cd;
                selectedAbilityCount = entry.castableAbilities().size();
                selectedAbilityIdx = abilityIdx;
                selectedStack = iconStack;
            }
        }

        // Tooltip to the right of selected accessory
        if (selectedBoxY >= 0 && selectedAbility != null && !selectedAbility.isEmpty()) {
            drawAccessoryTooltip(g, mc, x, selectedBoxY, selectedStack,
                    selectedName, selectedAbility, selectedCd,
                    selectedAbilityCount, selectedAbilityIdx, screenW);
        }
    }

    // ========================================================================
    // HINT TEXT — fading control hints
    // ========================================================================

    private static void renderHints(GuiGraphics g, Minecraft mc,
                                     boolean hasWpn, boolean hasAcc,
                                     int screenW, int screenH) {
        if (!hasWpn && !hasAcc) return;

        long elapsed = System.currentTimeMillis() - hintShowStartTime;
        if (hintShowStartTime <= 0 || elapsed >= HINT_DISPLAY_MS + HINT_FADE_MS) return;

        String hint;
        if (hasWpn && hasAcc) {
            hint = "[RClick] Wpn  [R+Scroll] Wpn Cycle  [R] Relic  [G+Scroll] Select  [G] Flip";
        } else if (hasWpn) {
            hint = "[RClick] Cast  [R+Scroll] Switch";
        } else {
            hint = "[R] Cast  [G+Scroll] Select  [G] Flip";
        }

        int alpha;
        if (elapsed < HINT_DISPLAY_MS) {
            alpha = 0x99;
        } else {
            float fadeProgress = (float) (elapsed - HINT_DISPLAY_MS) / HINT_FADE_MS;
            alpha = (int) (0x99 * (1.0f - fadeProgress));
        }
        if (alpha <= 4) return;

        int hintColor = (alpha << 24) | 0x00999999;
        int hotbarTop = screenH - 22;
        int hintY = hotbarTop - 38 - BOX_SIZE + BOX_SIZE + 2; // just below where weapon bar sits
        int hintX = (screenW - mc.font.width(hint)) / 2;
        g.drawString(mc.font, hint, hintX, hintY, hintColor, false);
    }

    // ========================================================================
    // CAST NOTIFICATION — ability name below compass at top of screen
    // ========================================================================

    private static void renderCastNotification(GuiGraphics g, Minecraft mc, int screenW) {
        if (castNotificationName == null || castNotificationTime <= 0) return;

        long elapsed = System.currentTimeMillis() - castNotificationTime;
        if (elapsed >= CAST_DISPLAY_MS + CAST_FADE_MS) {
            castNotificationName = null;
            return;
        }

        int alpha;
        if (elapsed < CAST_DISPLAY_MS) {
            alpha = 0xFF;
        } else {
            float fadeProgress = (float) (elapsed - CAST_DISPLAY_MS) / CAST_FADE_MS;
            alpha = (int) (0xFF * (1.0f - fadeProgress));
        }
        if (alpha <= 4) return;

        String text = castNotificationName;
        int textW = mc.font.width(text);
        int x = (screenW - textW) / 2;
        int y = 22; // below compass/direction indicator

        // Background pill
        int bgAlpha = (int) (0xBB * (alpha / 255.0f));
        int bgColor = (bgAlpha << 24) | 0x001A1A2E;
        int borderAlpha = (int) (0xCC * (alpha / 255.0f));
        int borderColor = (borderAlpha << 24) | 0x006644AA;
        g.fill(x - 5, y - 3, x + textW + 5, y + 11, borderColor);
        g.fill(x - 4, y - 2, x + textW + 4, y + 10, bgColor);

        int textColor = (alpha << 24) | 0x00FFDD44;
        g.drawString(mc.font, text, x, y, textColor, false);
    }

    // ========================================================================
    // Drawing helpers
    // ========================================================================

    /** Draw a single ability box with optional item icon, selection highlight, and cooldown overlay. */
    private static void drawAbilityBox(GuiGraphics g, Minecraft mc, int x, int y,
                                        boolean selected, ItemStack icon, int cd, String abilityName) {
        // Glow behind selected box
        if (selected) {
            g.fill(x - 1, y - 1, x + BOX_SIZE + 1, y + BOX_SIZE + 1, BOX_SELECTED_GLOW);
        }

        // Fill
        g.fill(x, y, x + BOX_SIZE, y + BOX_SIZE, BOX_FILL);

        // Border
        int border = selected ? BOX_SELECTED_BORDER : BOX_BORDER;
        g.fill(x, y, x + BOX_SIZE, y + 1, border);
        g.fill(x, y + BOX_SIZE - 1, x + BOX_SIZE, y + BOX_SIZE, border);
        g.fill(x, y, x + 1, y + BOX_SIZE, border);
        g.fill(x + BOX_SIZE - 1, y, x + BOX_SIZE, y + BOX_SIZE, border);

        // Item icon or abbreviation fallback
        if (icon != null && !icon.isEmpty()) {
            g.renderItem(icon, x + ICON_PAD, y + ICON_PAD);
        } else {
            String abbrev = getAbbreviation(abilityName);
            int tw = mc.font.width(abbrev);
            g.drawString(mc.font, abbrev, x + (BOX_SIZE - tw) / 2, y + (BOX_SIZE - FONT_HEIGHT) / 2, 0xFFCCCCCC, false);
        }

        // Cooldown overlay
        if (cd > 0) {
            int maxCd = maxCooldowns.getOrDefault(abilityName, cd);
            double frac = Math.min(1.0, (double) cd / (double) maxCd);
            int overlayH = (int) (BOX_SIZE * frac);
            g.fill(x + 1, y + BOX_SIZE - overlayH, x + BOX_SIZE - 1, y + BOX_SIZE - 1, COOLDOWN_OVERLAY);

            String cdText = String.valueOf((int) Math.ceil((double) cd / 20.0));
            int cdW = mc.font.width(cdText);
            g.drawString(mc.font, cdText, x + (BOX_SIZE - cdW) / 2, y + (BOX_SIZE - FONT_HEIGHT) / 2, COOLDOWN_TEXT, false);
        }
    }

    /** Draw a non-interactive equipment box (armor/shield) — display only. */
    private static void drawEquipmentBox(GuiGraphics g, Minecraft mc, int x, int y, ItemStack stack) {
        g.fill(x, y, x + BOX_SIZE, y + BOX_SIZE, BOX_FILL);
        // Gray border
        g.fill(x, y, x + BOX_SIZE, y + 1, BOX_BORDER);
        g.fill(x, y + BOX_SIZE - 1, x + BOX_SIZE, y + BOX_SIZE, BOX_BORDER);
        g.fill(x, y, x + 1, y + BOX_SIZE, BOX_BORDER);
        g.fill(x + BOX_SIZE - 1, y, x + BOX_SIZE, y + BOX_SIZE, BOX_BORDER);

        if (!stack.isEmpty()) {
            g.renderItem(stack, x + ICON_PAD, y + ICON_PAD);
        }
    }

    /** Tooltip appearing to the right of the selected accessory box. */
    private static void drawAccessoryTooltip(GuiGraphics g, Minecraft mc,
                                              int boxX, int boxY, ItemStack icon,
                                              String itemName, String abilityName, int cd,
                                              int abilityCount, int abilityIdx, int screenW) {
        String line1 = itemName != null ? itemName : abilityName;
        String line2 = (itemName != null && !abilityName.equals(itemName)) ? abilityName : null;
        String line3 = cd > 0 ? "CD: " + (int) Math.ceil((double) cd / 20.0) + "s" : "[R]";

        int maxW = mc.font.width(line1);
        if (line2 != null) maxW = Math.max(maxW, mc.font.width(line2));
        maxW = Math.max(maxW, mc.font.width(line3));

        int panelW = maxW + 10;
        int lineCount = line2 != null ? 3 : 2;
        int panelH = 6 + lineCount * 10;

        // Position to the right of the box
        int tooltipX = boxX + BOX_SIZE + 6;
        int tooltipY = boxY + BOX_SIZE / 2 - panelH / 2;

        // Clamp to screen edges
        if (tooltipX + panelW > screenW - 2) tooltipX = boxX - panelW - 6;
        if (tooltipY < 2) tooltipY = 2;

        // Background + border
        g.fill(tooltipX - 1, tooltipY - 1, tooltipX + panelW + 1, tooltipY + panelH + 1, TOOLTIP_BORDER);
        g.fill(tooltipX, tooltipY, tooltipX + panelW, tooltipY + panelH, TOOLTIP_BG);

        int textX = tooltipX + 4;
        int textY = tooltipY + 4;

        // Line 1: item name
        g.drawString(mc.font, line1, textX, textY, TOOLTIP_NAME, false);
        textY += 10;

        // Line 2: ability name (if different from item)
        if (line2 != null) {
            g.drawString(mc.font, line2, textX, textY, TOOLTIP_ABILITY, false);
            textY += 10;
        }

        // Line 3: cooldown or keybind hint
        int line3Color = cd > 0 ? COOLDOWN_TEXT : TOOLTIP_KEYBIND;
        g.drawString(mc.font, line3, textX, textY, line3Color, false);

        // Ability page indicator (e.g. "1/3")
        if (abilityCount > 1) {
            String pageStr = (abilityIdx + 1) + "/" + abilityCount;
            int pageW = mc.font.width(pageStr);
            g.drawString(mc.font, pageStr, tooltipX + panelW - pageW - 4, textY, TOOLTIP_TYPE, false);
        }
    }

    /** Tooltip above the selected weapon box (horizontal bar). */
    private static void drawWeaponTooltip(GuiGraphics g, Minecraft mc,
                                           int boxX, int barY,
                                           String displayName, String abilityName, int cd) {
        String line1 = displayName;
        String line2 = cd > 0 ? "CD: " + (int) Math.ceil((double) cd / 20.0) + "s" : "[RClick]";

        int maxW = Math.max(mc.font.width(line1), mc.font.width(line2));
        int panelW = maxW + 10;
        int panelH = 6 + 2 * 10;

        int tooltipX = boxX + BOX_SIZE / 2 - panelW / 2;
        int tooltipY = barY - panelH - 4;
        int screenW = g.guiWidth();
        if (tooltipX < 2) tooltipX = 2;
        if (tooltipX + panelW > screenW - 2) tooltipX = screenW - 2 - panelW;
        if (tooltipY < 2) tooltipY = 2;

        g.fill(tooltipX - 1, tooltipY - 1, tooltipX + panelW + 1, tooltipY + panelH + 1, TOOLTIP_BORDER);
        g.fill(tooltipX, tooltipY, tooltipX + panelW, tooltipY + panelH, TOOLTIP_BG);

        int textX = tooltipX + 4;
        int textY = tooltipY + 4;

        g.drawString(mc.font, line1, textX, textY, TOOLTIP_NAME, false);
        textY += 10;

        int line2Color = cd > 0 ? COOLDOWN_TEXT : TOOLTIP_KEYBIND;
        g.drawString(mc.font, line2, textX, textY, line2Color, false);
    }

    // ========================================================================
    // Utility
    // ========================================================================

    private static void rebuildAccessoryStackCache() {
        accessoryStackCache.clear();
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        if (equipped == null) return;
        for (Map.Entry<String, String> entry : equipped.entrySet()) {
            String itemId = entry.getValue();
            if (itemId != null && !itemId.isEmpty()) {
                try {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
                    accessoryStackCache.put(itemId, new ItemStack(item));
                } catch (Exception ignored) {}
            }
        }
    }

    private static void updateMaxCooldowns(Map<String, Integer> cooldowns) {
        for (Map.Entry<String, Integer> entry : cooldowns.entrySet()) {
            int ticks = entry.getValue();
            if (ticks > 0) {
                int currentMax = maxCooldowns.getOrDefault(entry.getKey(), 0);
                if (ticks > currentMax) maxCooldowns.put(entry.getKey(), ticks);
            } else {
                maxCooldowns.remove(entry.getKey());
            }
        }
    }

    private static String getAbbreviation(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] words = name.split("[\\s_]+");
        if (words.length == 1) {
            return words[0].length() >= 2
                    ? ("" + Character.toUpperCase(words[0].charAt(0)) + Character.toUpperCase(words[0].charAt(1)))
                    : words[0].toUpperCase();
        }
        return "" + Character.toUpperCase(words[0].charAt(0)) + Character.toUpperCase(words[1].charAt(0));
    }
}
