package com.ultra.megamod.feature.combat.spell.client;

import com.ultra.megamod.feature.combat.spell.SpellBookItem;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD overlay that displays available spells when the player holds a SpellBookItem
 * in their offhand. Renders a vertical spell list on the left side of the screen
 * (offset from the equipment bar), showing spell names with school-colored bars.
 * The currently selected spell is highlighted. Keybind hints are shown at the bottom.
 */
public class SpellBookHudOverlay {

    // Layout
    private static final int LEFT_X = 4;
    private static final int ENTRY_HEIGHT = 16;
    private static final int ENTRY_GAP = 2;
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 14;
    private static final int ICON_SIZE = 10;
    private static final int TEXT_PAD_LEFT = 4;

    // Colors
    private static final int BG_FILL = 0xCC111111;
    private static final int BG_BORDER = 0xBB444444;
    private static final int SELECTED_BORDER = 0xFFFFAA00;
    private static final int SELECTED_GLOW = 0x44FFAA00;
    private static final int SPELL_NAME_COLOR = 0xFFEEEEEE;
    private static final int SPELL_NAME_SELECTED = 0xFFFFDD44;
    private static final int SCHOOL_LABEL_COLOR = 0xFFAAAAAA;
    private static final int HINT_COLOR = 0x99999999;
    private static final int TITLE_COLOR = 0xFFCCCCCC;
    private static final int TIER_DOT_ACTIVE = 0xFFFFDD44;
    private static final int TIER_DOT_INACTIVE = 0x44666666;
    private static final int COOLDOWN_COLOR = 0xFFFF6666;
    private static final int LOCKED_NAME_COLOR = 0xFF777777;
    private static final int LOCKED_BG_FILL = 0xCC0A0A0A;
    private static final int LOCKED_LEVEL_COLOR = 0xFFFF5555;
    private static final int NEXT_UNLOCK_COLOR = 0xFFAAAAFF;

    // Cached spell list to avoid rebuilding every frame
    private static List<SpellDefinition> cachedSpells = List.of();
    private static String cachedSchool = "";
    private static int cachedSchoolColor = 0xFFFFFFFF;
    private static String cachedDisplaySchool = "";

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "spell_book_hud"),
                SpellBookHudOverlay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) return;
        if (mc.screen != null) return;

        // Check offhand for spell book
        ItemStack offhand = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.isEmpty() || !(offhand.getItem() instanceof SpellBookItem book)) {
            // No spell book — reset state and hide
            if (!cachedSpells.isEmpty()) {
                SpellBookSelection.reset();
                cachedSpells = List.of();
                cachedSchool = "";
            }
            return;
        }

        // Rebuild spell list if school changed
        String school = book.getSchool();
        if (!school.equals(cachedSchool)) {
            rebuildSpellCache(book);
            // Reset selection if the school changed
            if (!school.equals(SpellBookSelection.getLastBookSchool())) {
                SpellBookSelection.reset();
                SpellBookSelection.setLastBookSchool(school);
            }
        }

        if (cachedSpells.isEmpty()) return;

        // Clamp selection
        SpellBookSelection.clamp(cachedSpells.size());
        int selected = SpellBookSelection.getSelected();

        int screenH = g.guiHeight();

        // Calculate vertical positioning — center the spell list on screen
        int totalEntries = cachedSpells.size();
        int listHeight = totalEntries * (BAR_HEIGHT + ENTRY_GAP) - ENTRY_GAP;
        int titleHeight = 14; // school title line
        int hintHeight = 12; // keybind hint at bottom
        int totalHeight = titleHeight + 4 + listHeight + 6 + hintHeight;
        int startY = (screenH - totalHeight) / 2;

        // Position to the right of the equipment bar (which sits at LEFT_X with 22px boxes)
        int x = LEFT_X + 26;

        // ===== School Title =====
        String title = cachedDisplaySchool + " Spells";
        g.drawString(mc.font, title, x, startY, cachedSchoolColor, true);
        int cursorY = startY + titleHeight + 2;

        // ===== Spell Entries =====
        // Class/skill unlock gating retired; every spell in the book is castable.
        for (int i = 0; i < totalEntries; i++) {
            SpellDefinition spell = cachedSpells.get(i);
            boolean isSelected = (i == selected);
            int entryY = cursorY + i * (BAR_HEIGHT + ENTRY_GAP);
            renderSpellEntry(g, mc, x, entryY, spell, isSelected, true);
        }

        int hintY = cursorY + totalEntries * (BAR_HEIGHT + ENTRY_GAP) + 4;

        // ===== Keybind Hints =====
        String hint = "[R] Cast  [G] Next Spell";
        g.drawString(mc.font, hint, x, hintY, HINT_COLOR, false);
    }

    private static void renderSpellEntry(GuiGraphics g, Minecraft mc, int x, int y,
                                          SpellDefinition spell, boolean selected, boolean unlocked) {
        int barW = BAR_WIDTH;

        // Glow behind selected entry (only if unlocked)
        if (selected && unlocked) {
            g.fill(x - 1, y - 1, x + barW + 1, y + BAR_HEIGHT + 1, SELECTED_GLOW);
        }

        // Background fill — dimmer for locked spells
        g.fill(x, y, x + barW, y + BAR_HEIGHT, unlocked ? BG_FILL : LOCKED_BG_FILL);

        // Left accent bar (school color for unlocked, gray for locked)
        int schoolColor = unlocked ? spell.school().color : 0xFF444444;
        g.fill(x, y, x + 2, y + BAR_HEIGHT, schoolColor);

        // Border
        int border = selected && unlocked ? SELECTED_BORDER : BG_BORDER;
        g.fill(x, y, x + barW, y + 1, border);
        g.fill(x, y + BAR_HEIGHT - 1, x + barW, y + BAR_HEIGHT, border);
        g.fill(x, y, x + 1, y + BAR_HEIGHT, border);
        g.fill(x + barW - 1, y, x + barW, y + BAR_HEIGHT, border);

        if (unlocked) {
            // Cast mode icon (abbreviated letter)
            String modeChar = switch (spell.castMode()) {
                case INSTANT -> "I";
                case CHARGED -> "C";
                case CHANNELED -> "H";
            };
            int modeColor = switch (spell.castMode()) {
                case INSTANT -> 0xFF88FF88;
                case CHARGED -> 0xFFFFCC44;
                case CHANNELED -> 0xFF44AAFF;
            };
            g.drawString(mc.font, modeChar, x + TEXT_PAD_LEFT, y + (BAR_HEIGHT - 9) / 2, modeColor, false);

            // Spell name
            int nameColor = selected ? SPELL_NAME_SELECTED : SPELL_NAME_COLOR;
            String name = spell.name();
            // Truncate if too long
            int maxNameW = barW - TEXT_PAD_LEFT - 12 - 4;
            if (mc.font.width(name) > maxNameW) {
                while (mc.font.width(name + "..") > maxNameW && name.length() > 1) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name + "..";
            }
            g.drawString(mc.font, name, x + TEXT_PAD_LEFT + 10, y + (BAR_HEIGHT - 9) / 2, nameColor, false);

            // Tier dots on the right side
            int dotCount = Math.min(spell.tier() + 1, 5);
            int dotStartX = x + barW - 4 - dotCount * 4;
            for (int d = 0; d < dotCount; d++) {
                g.fill(dotStartX + d * 4, y + BAR_HEIGHT - 4, dotStartX + d * 4 + 2, y + BAR_HEIGHT - 2, TIER_DOT_ACTIVE);
            }
        }
        // Locked-entry rendering retired along with the class unlock system.

        // Selected indicator arrow (only for unlocked)
        if (selected && unlocked) {
            g.drawString(mc.font, "\u25B6", x - 8, y + (BAR_HEIGHT - 9) / 2, SELECTED_BORDER, false);
        } else if (selected && !unlocked) {
            // Locked selected indicator — lock symbol
            g.drawString(mc.font, "\u2716", x - 8, y + (BAR_HEIGHT - 9) / 2, LOCKED_LEVEL_COLOR, false);
        }
    }

    private static void rebuildSpellCache(SpellBookItem book) {
        cachedSchool = book.getSchool();
        List<String> spellIds = book.getSpellIds();
        List<SpellDefinition> spells = new ArrayList<>();
        for (String id : spellIds) {
            SpellDefinition def = SpellRegistry.get(id);
            if (def != null) {
                spells.add(def);
            }
        }
        // Sort by tier then name
        spells.sort((a, b) -> {
            int cmp = Integer.compare(a.tier(), b.tier());
            return cmp != 0 ? cmp : a.name().compareTo(b.name());
        });
        cachedSpells = spells;

        // Determine display school name and color from the first spell's school
        if (!spells.isEmpty()) {
            cachedSchoolColor = spells.get(0).school().color;
            cachedDisplaySchool = spells.get(0).school().displayName;
        } else {
            cachedSchoolColor = 0xFFFFFFFF;
            cachedDisplaySchool = cachedSchool;
        }
    }

    /**
     * Returns the currently selected spell ID, or null if no book is held or no spells available.
     * Used by AbilityKeybind to send cast requests.
     */
    public static String getSelectedSpellId() {
        int idx = SpellBookSelection.getSelected();
        if (idx >= 0 && idx < cachedSpells.size()) {
            return cachedSpells.get(idx).id();
        }
        return null;
    }

    /**
     * Returns the display name of the currently selected spell, for cast notifications.
     */
    public static String getSelectedSpellName() {
        int idx = SpellBookSelection.getSelected();
        if (idx >= 0 && idx < cachedSpells.size()) {
            return cachedSpells.get(idx).name();
        }
        return null;
    }

    /**
     * Returns the number of spells in the current book.
     */
    public static int getSpellCount() {
        return cachedSpells.size();
    }

    /**
     * Returns true if the player currently has a spell book in their offhand.
     */
    public static boolean isSpellBookActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ItemStack offhand = mc.player.getItemInHand(InteractionHand.OFF_HAND);
        return !offhand.isEmpty() && offhand.getItem() instanceof SpellBookItem;
    }
}
