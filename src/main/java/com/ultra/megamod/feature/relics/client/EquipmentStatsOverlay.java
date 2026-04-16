package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.*;

/**
 * Renders a dynamic-height equipment stats panel to the right of the inventory screen.
 * Panel sizes to fit content — grows down from inventory top, shifts up if needed.
 * Accounts for recipe book offset via getGuiLeft()/getXSize().
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class EquipmentStatsOverlay {

    // Vanilla inventory style colors
    private static final int INV_BG = 0xFFC6C6C6;
    private static final int INV_BORDER_DARK = 0xFF373737;
    private static final int INV_BORDER_MID = 0xFF8B8B8B;
    private static final int INV_BORDER_LIGHT = 0xFFFFFFFF;

    private static final int TITLE_COLOR = 0xFF404040;
    private static final int LABEL_COLOR = 0xFF555555;
    private static final int VALUE_COLOR = 0xFF333333;
    private static final int GREEN_VAL = 0xFF2D7A2D;
    private static final int RED_VAL = 0xFFB02020;
    private static final int GOLD_VAL = 0xFF9A7A22;
    private static final int BLUE_VAL = 0xFF2255AA;
    private static final int PURPLE_VAL = 0xFF7733AA;
    private static final int EFFECT_GOOD = 0xFF226622;
    private static final int EFFECT_BAD = 0xFF882222;
    private static final int DIVIDER = 0xFFAAAAAA;
    private static final int BOOST_COLOR = 0xFF33DDAA;  // Teal-green for stats buffed above base
    private static final int ACCENT_COMBAT = 0xFFFFAA00;
    private static final int ACCENT_ELEMENT = 0xFFFF6622;
    private static final int ACCENT_RESIST = 0xFF4488CC;
    private static final int ACCENT_SURVIVAL = 0xFF44AA44;
    private static final int ACCENT_ECONOMY = 0xFFCCAA22;
    private static final int ACCENT_EFFECTS = 0xFF8844AA;
    private static final int ACCENT_SKILLS = 0xFF44CCCC;
    private static final int ACCENT_RELICS = 0xFF7733AA;
    private static final int ACCENT_EQUIP = 0xFF6688BB;

    private static final int PANEL_W = 120;
    private static final int PADDING = 6;
    private static final int CONTENT_W = PANEL_W - PADDING * 2;
    private static final int SCROLL_STEP = 14;

    // Scroll state
    private static int scrollOffset = 0;
    private static int scrollMaxOffset = 0;
    // Screen-space panel bounds for scroll hit testing
    private static int spX, spY, spW, spH;

    @SubscribeEvent
    public static void onContainerRenderBg(ContainerScreenEvent.Render.Foreground event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        AbstractContainerScreen<?> containerScreen = event.getContainerScreen();
        boolean isInventory = containerScreen instanceof InventoryScreen;
        boolean isCreative = containerScreen instanceof CreativeModeInventoryScreen;
        if (!isInventory && !isCreative) return;

        int invLeft = containerScreen.getGuiLeft();
        int invW = containerScreen.getXSize();
        int invTop = containerScreen.getGuiTop();
        int invH = containerScreen.getYSize();

        int screenW = containerScreen.width;
        int screenH = containerScreen.height;

        int gap = 2;
        int panelX = invLeft + invW + gap;

        GuiGraphics g = event.getGuiGraphics();

        // Foreground event fires in a translated context where (0,0) = (guiLeft, guiTop).
        // Apply offset to all draw coordinates instead of using pose translation.
        int ox = -invLeft;
        int oy = -invTop;

        // Don't render if no room
        if (panelX + PANEL_W > screenW - 1) return;

        // === First pass: measure content height ===
        int contentH = measureContent(mc, screenH);
        if (contentH <= 0) return;

        int fullPanelH = contentH + PADDING * 2;
        int panelY = invTop;
        int maxH = screenH - 4;
        // Shift panel up if needed
        if (fullPanelH > maxH - panelY && panelY > 2) {
            int shift = Math.min(panelY - 2, fullPanelH - (maxH - panelY));
            panelY -= shift;
        }
        int availH = maxH - panelY;
        int visiblePanelH = Math.min(fullPanelH, availH);
        boolean needsScroll = fullPanelH > visiblePanelH;
        int visibleContentH = visiblePanelH - PADDING * 2;

        // Clamp scroll offset
        scrollMaxOffset = Math.max(0, contentH - visibleContentH);
        scrollOffset = Math.max(0, Math.min(scrollOffset, scrollMaxOffset));

        // Store screen-space bounds for scroll hit testing
        spX = panelX; spY = panelY; spW = PANEL_W; spH = visiblePanelH;

        // Draw panel background
        drawVanillaPanel(g, panelX + ox, panelY + oy, PANEL_W, visiblePanelH);

        // Scissor-clip content to panel area
        g.enableScissor(panelX + ox + 2, panelY + oy + 2,
                panelX + ox + PANEL_W - 2, panelY + oy + visiblePanelH - 2);
        int tx = panelX + PADDING + ox;
        int ty = panelY + PADDING + oy - scrollOffset;
        int bottomY = ty + contentH + PADDING;
        renderContent(g, mc, tx, ty, bottomY);
        g.disableScissor();

        // Scroll indicators
        if (needsScroll) {
            int arrowX = panelX + ox + PANEL_W - 11;
            if (scrollOffset > 0)
                g.drawString(mc.font, "\u25B2", arrowX, panelY + oy + 3, DIVIDER, false);
            if (scrollOffset < scrollMaxOffset)
                g.drawString(mc.font, "\u25BC", arrowX, panelY + oy + visiblePanelH - 10, DIVIDER, false);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (scrollMaxOffset <= 0) return;
        if (!(event.getScreen() instanceof InventoryScreen) && !(event.getScreen() instanceof CreativeModeInventoryScreen)) return;
        double mx = event.getMouseX();
        double my = event.getMouseY();
        if (mx >= spX && mx < spX + spW && my >= spY && my < spY + spH) {
            scrollOffset -= (int)(event.getScrollDeltaY() * SCROLL_STEP);
            scrollOffset = Math.max(0, Math.min(scrollOffset, scrollMaxOffset));
            event.setCanceled(true);
        }
    }

    /** Measure how tall the content will be without drawing anything. */
    private static int measureContent(Minecraft mc, int screenH) {
        int h = 0;

        // Title
        h += 11;

        // Defense
        h += 3; // divider
        h += 9; // HP
        h += 9; // Armor
        double toughness = getAttrValue(mc, Attributes.ARMOR_TOUGHNESS);
        if (toughness > 0) h += 9;
        double kbRes = getAttrValue(mc, Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes > 0) h += 9;
        double explKbRes = getAttrValue(mc, Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
        if (explKbRes > 0) h += 9;

        // Attributes
        h += 4; // divider
        h += 9 * 3; // Attack, Speed, Move (always shown)
        double reach = getAttrValue(mc, Attributes.ENTITY_INTERACTION_RANGE);
        if (reach != 3.0) h += 9;
        double knockback = getAttrValue(mc, Attributes.ATTACK_KNOCKBACK);
        if (knockback > 0) h += 9;
        double luck = getAttrValue(mc, Attributes.LUCK);
        if (luck != 0) h += 9;
        double sweep = getAttrValue(mc, Attributes.SWEEPING_DAMAGE_RATIO);
        if (sweep > 0) h += 9;

        // Equipment attributes (vanilla armor/item stats)
        int equipCount = 0;
        if (isModified(mc, Attributes.BURNING_TIME)) equipCount++;
        if (isModified(mc, Attributes.WATER_MOVEMENT_EFFICIENCY)) equipCount++;
        if (isModified(mc, Attributes.MOVEMENT_EFFICIENCY)) equipCount++;
        if (isModified(mc, Attributes.OXYGEN_BONUS)) equipCount++;
        if (isModified(mc, Attributes.SNEAKING_SPEED)) equipCount++;
        if (isModified(mc, Attributes.SUBMERGED_MINING_SPEED)) equipCount++;
        if (isModified(mc, Attributes.BLOCK_BREAK_SPEED)) equipCount++;
        if (isModified(mc, Attributes.MINING_EFFICIENCY)) equipCount++;
        if (isModified(mc, Attributes.JUMP_STRENGTH)) equipCount++;
        if (isModified(mc, Attributes.SAFE_FALL_DISTANCE)) equipCount++;
        if (isModified(mc, Attributes.FALL_DAMAGE_MULTIPLIER)) equipCount++;
        if (isModified(mc, Attributes.STEP_HEIGHT)) equipCount++;
        if (isModified(mc, Attributes.GRAVITY)) equipCount++;
        if (isModified(mc, Attributes.SCALE)) equipCount++;
        if (equipCount > 0) h += 4 + 10 + equipCount * 9;

        // Combat stats
        int combatCount = countNonZero(mc,
                MegaModAttributes.CRITICAL_CHANCE, MegaModAttributes.CRITICAL_DAMAGE,
                MegaModAttributes.DODGE_CHANCE, MegaModAttributes.LIFESTEAL,
                MegaModAttributes.THORNS_DAMAGE, MegaModAttributes.ARMOR_SHRED,
                MegaModAttributes.STUN_CHANCE, MegaModAttributes.COOLDOWN_REDUCTION,
                MegaModAttributes.ABILITY_POWER, MegaModAttributes.COMBO_SPEED,
                MegaModAttributes.SPELL_RANGE);
        if (combatCount > 0) h += 4 + 10 + combatCount * 9;

        // Elemental damage
        int eleDmgCount = countNonZero(mc,
                MegaModAttributes.FIRE_DAMAGE_BONUS, MegaModAttributes.ICE_DAMAGE_BONUS,
                MegaModAttributes.LIGHTNING_DAMAGE_BONUS, MegaModAttributes.POISON_DAMAGE_BONUS,
                MegaModAttributes.HOLY_DAMAGE_BONUS, MegaModAttributes.SHADOW_DAMAGE_BONUS);
        if (eleDmgCount > 0) h += 4 + 10 + eleDmgCount * 9;

        // Elemental resist
        int eleResCount = countNonZero(mc,
                MegaModAttributes.FIRE_RESISTANCE_BONUS, MegaModAttributes.ICE_RESISTANCE_BONUS,
                MegaModAttributes.LIGHTNING_RESISTANCE_BONUS, MegaModAttributes.POISON_RESISTANCE_BONUS,
                MegaModAttributes.HOLY_RESISTANCE_BONUS, MegaModAttributes.SHADOW_RESISTANCE_BONUS);
        if (eleResCount > 0) h += 4 + 10 + eleResCount * 9;

        // Survival
        int survCount = countNonZero(mc,
                MegaModAttributes.HEALTH_REGEN_BONUS, MegaModAttributes.FALL_DAMAGE_REDUCTION,
                MegaModAttributes.MINING_SPEED_BONUS, MegaModAttributes.SWIM_SPEED_BONUS,
                MegaModAttributes.JUMP_HEIGHT_BONUS, MegaModAttributes.XP_BONUS,
                MegaModAttributes.LOOT_FORTUNE, MegaModAttributes.HUNGER_EFFICIENCY,
                MegaModAttributes.COMBAT_XP_BONUS, MegaModAttributes.MINING_XP_BONUS,
                MegaModAttributes.FARMING_XP_BONUS, MegaModAttributes.ARCANE_XP_BONUS,
                MegaModAttributes.SURVIVAL_XP_BONUS,
                MegaModAttributes.BRILLIANCE, MegaModAttributes.BEAST_AFFINITY,
                MegaModAttributes.PREY_SENSE, MegaModAttributes.VEIN_SENSE,
                MegaModAttributes.EXCAVATION_REACH);
        if (survCount > 0) h += 4 + 10 + survCount * 9;

        // Economy
        int econCount = countNonZero(mc,
                MegaModAttributes.MEGACOIN_BONUS, MegaModAttributes.SHOP_DISCOUNT,
                MegaModAttributes.SELL_BONUS);
        if (econCount > 0) h += 4 + 10 + econCount * 9;

        // Effects
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (!effects.isEmpty()) h += 4 + 10 + effects.size() * 9;

        // Skills section removed — old skill system deleted (TODO: Reconnect with Pufferfish Skills API)

        // Relics
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        if (equipped != null && !equipped.isEmpty()) h += 4 + 10 + equipped.size() * 9;

        // Held item
        if (!mc.player.getMainHandItem().isEmpty()) h += 4 + 9;

        return h;
    }

    @SafeVarargs
    private static int countNonZero(Minecraft mc, Holder<Attribute>... attrs) {
        int count = 0;
        for (Holder<Attribute> attr : attrs) {
            if (getAttrValue(mc, attr) > 0) count++;
        }
        return count;
    }

    /** Render all content and return final ty. */
    private static int renderContent(GuiGraphics g, Minecraft mc, int tx, int ty, int bottomY) {
        // === Title ===
        g.drawString(mc.font, "STATS", tx, ty, TITLE_COLOR, false);
        g.fill(tx, ty + 9, tx + CONTENT_W, ty + 10, DIVIDER);
        ty += 12;

        // === Defense ===
        drawDivider(g, tx, ty, CONTENT_W);
        ty += 3;

        // Skill bonuses removed — old SkillSyncPayload deleted
        double maxHp = mc.player.getMaxHealth();
        double hp = mc.player.getHealth();
        double armor = mc.player.getArmorValue();
        double toughness = getAttrValue(mc, Attributes.ARMOR_TOUGHNESS);

        int hpCol = hp / maxHp > 0.5 ? GREEN_VAL : (hp / maxHp > 0.25 ? GOLD_VAL : RED_VAL);
        ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "HP", String.format("%.0f/%.0f", hp, maxHp), hpCol);
        boolean armorBoosted = isBoosted(mc, Attributes.ARMOR);
        ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Armor", String.valueOf((int) armor),
            armorBoosted ? BOOST_COLOR : VALUE_COLOR);
        if (toughness > 0)
            ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Tough", String.format("%.1f", toughness),
                isBoosted(mc, Attributes.ARMOR_TOUGHNESS) ? BOOST_COLOR : VALUE_COLOR);
        double kbRes = getAttrValue(mc, Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes > 0)
            ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "KBRes", String.format("%.0f%%", kbRes * 100), BLUE_VAL);
        double explKbRes = getAttrValue(mc, Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
        if (explKbRes > 0)
            ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "ExplRes", String.format("%.0f%%", explKbRes * 100), BLUE_VAL);

        // === Attributes ===
        if (ty + 10 < bottomY) {
            drawDivider(g, tx, ty + 1, CONTENT_W);
            ty += 4;

            // Skill bonuses removed — old SkillSyncPayload deleted
            double atkDmg = getAttrValue(mc, Attributes.ATTACK_DAMAGE);
            double atkSpd = getAttrValue(mc, Attributes.ATTACK_SPEED);
            double moveSpd = getAttrValue(mc, Attributes.MOVEMENT_SPEED);
            double reach = getAttrValue(mc, Attributes.ENTITY_INTERACTION_RANGE);
            double knockback = getAttrValue(mc, Attributes.ATTACK_KNOCKBACK);
            double luck = getAttrValue(mc, Attributes.LUCK);

            boolean atkBoosted = isBoosted(mc, Attributes.ATTACK_DAMAGE);
            ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Attack", String.format("%.1f", atkDmg),
                atkBoosted ? BOOST_COLOR : RED_VAL);
            ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Speed", String.format("%.2f", atkSpd),
                isBoosted(mc, Attributes.ATTACK_SPEED) ? BOOST_COLOR : VALUE_COLOR);
            ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Move", String.format("%.3f", moveSpd),
                isBoosted(mc, Attributes.MOVEMENT_SPEED) ? BOOST_COLOR : VALUE_COLOR);
            if (reach != 3.0 && ty + 9 < bottomY)
                ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Reach", String.format("%.1f", reach), BLUE_VAL);
            if (knockback > 0 && ty + 9 < bottomY)
                ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "KB", String.format("%.1f", knockback), VALUE_COLOR);
            if (luck != 0 && ty + 9 < bottomY)
                ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Luck", String.format("%.1f", luck), GREEN_VAL);
            double sweep = getAttrValue(mc, Attributes.SWEEPING_DAMAGE_RATIO);
            if (sweep > 0 && ty + 9 < bottomY)
                ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Sweep", String.format("%.0f%%", sweep * 100), RED_VAL);
        }

        // === Equipment Stats (vanilla armor/item attributes) ===
        if (ty + 10 < bottomY) {
            int equipCount = 0;
            if (isModified(mc, Attributes.BURNING_TIME)) equipCount++;
            if (isModified(mc, Attributes.WATER_MOVEMENT_EFFICIENCY)) equipCount++;
            if (isModified(mc, Attributes.MOVEMENT_EFFICIENCY)) equipCount++;
            if (isModified(mc, Attributes.OXYGEN_BONUS)) equipCount++;
            if (isModified(mc, Attributes.SNEAKING_SPEED)) equipCount++;
            if (isModified(mc, Attributes.SUBMERGED_MINING_SPEED)) equipCount++;
            if (isModified(mc, Attributes.BLOCK_BREAK_SPEED)) equipCount++;
            if (isModified(mc, Attributes.MINING_EFFICIENCY)) equipCount++;
            if (isModified(mc, Attributes.JUMP_STRENGTH)) equipCount++;
            if (isModified(mc, Attributes.SAFE_FALL_DISTANCE)) equipCount++;
            if (isModified(mc, Attributes.FALL_DAMAGE_MULTIPLIER)) equipCount++;
            if (isModified(mc, Attributes.STEP_HEIGHT)) equipCount++;
            if (isModified(mc, Attributes.GRAVITY)) equipCount++;
            if (isModified(mc, Attributes.SCALE)) equipCount++;

            if (equipCount > 0) {
                ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "EQUIPMENT", ACCENT_EQUIP);
                if (isModified(mc, Attributes.BURNING_TIME) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "Burn", Attributes.BURNING_TIME, "x%.2f", ACCENT_ELEMENT);
                if (isModified(mc, Attributes.WATER_MOVEMENT_EFFICIENCY) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "WaterMov", Attributes.WATER_MOVEMENT_EFFICIENCY, "+%.0f%%", BLUE_VAL);
                if (isModified(mc, Attributes.MOVEMENT_EFFICIENCY) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "MoveEff", Attributes.MOVEMENT_EFFICIENCY, "+%.0f%%", GREEN_VAL);
                if (isModified(mc, Attributes.OXYGEN_BONUS) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "O2 Bonus", Attributes.OXYGEN_BONUS, "+%.0f%%", BLUE_VAL);
                if (isModified(mc, Attributes.SNEAKING_SPEED) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "Sneak", Attributes.SNEAKING_SPEED, "%.2f", VALUE_COLOR);
                if (isModified(mc, Attributes.SUBMERGED_MINING_SPEED) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "SubMine", Attributes.SUBMERGED_MINING_SPEED, "%.2f", BLUE_VAL);
                if (isModified(mc, Attributes.BLOCK_BREAK_SPEED) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "BreakSpd", Attributes.BLOCK_BREAK_SPEED, "x%.2f", GOLD_VAL);
                if (isModified(mc, Attributes.MINING_EFFICIENCY) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "MineEff", Attributes.MINING_EFFICIENCY, "+%.0f", GOLD_VAL);
                if (isModified(mc, Attributes.JUMP_STRENGTH) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "Jump", Attributes.JUMP_STRENGTH, "%.2f", GREEN_VAL);
                if (isModified(mc, Attributes.SAFE_FALL_DISTANCE) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "SafeFall", Attributes.SAFE_FALL_DISTANCE, "%.1f", BLUE_VAL);
                if (isModified(mc, Attributes.FALL_DAMAGE_MULTIPLIER) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "FallDmg", Attributes.FALL_DAMAGE_MULTIPLIER, "x%.2f", RED_VAL);
                if (isModified(mc, Attributes.STEP_HEIGHT) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "StepH", Attributes.STEP_HEIGHT, "%.1f", VALUE_COLOR);
                if (isModified(mc, Attributes.GRAVITY) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "Gravity", Attributes.GRAVITY, "%.3f", VALUE_COLOR);
                if (isModified(mc, Attributes.SCALE) && ty + 9 < bottomY)
                    ty = drawModifiedRow(g, mc, tx, ty, CONTENT_W, "Scale", Attributes.SCALE, "x%.2f", PURPLE_VAL);
            }
        }

        // === Combat Stats ===
        if (ty + 10 < bottomY) {
            double critChance = getCustomAttr(mc, MegaModAttributes.CRITICAL_CHANCE);
            double critDamage = getCustomAttr(mc, MegaModAttributes.CRITICAL_DAMAGE);
            double dodge = getCustomAttr(mc, MegaModAttributes.DODGE_CHANCE);
            double lifesteal = getCustomAttr(mc, MegaModAttributes.LIFESTEAL);
            double thorns = getCustomAttr(mc, MegaModAttributes.THORNS_DAMAGE);
            double armorShred = getCustomAttr(mc, MegaModAttributes.ARMOR_SHRED);
            double stunChance = getCustomAttr(mc, MegaModAttributes.STUN_CHANCE);
            double cdr = getCustomAttr(mc, MegaModAttributes.COOLDOWN_REDUCTION);
            double abilityPower = getCustomAttr(mc, MegaModAttributes.ABILITY_POWER);
            double comboSpeed = getCustomAttr(mc, MegaModAttributes.COMBO_SPEED);
            double spellRange = getCustomAttr(mc, MegaModAttributes.SPELL_RANGE);

            boolean hasAny = critChance > 0 || critDamage > 0 || dodge > 0 || lifesteal > 0
                || thorns > 0 || armorShred > 0 || stunChance > 0 || cdr > 0 || abilityPower > 0 || comboSpeed > 0
                || spellRange > 0;

            if (hasAny) {
                ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "COMBAT", ACCENT_COMBAT);
                if (critChance > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Crit%", String.format("%.1f%%", critChance), GOLD_VAL);
                if (critDamage > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "CritDmg", String.format("+%.0f%%", critDamage), GOLD_VAL);
                if (dodge > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Dodge", String.format("%.1f%%", dodge), BLUE_VAL);
                if (lifesteal > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Leech", String.format("%.1f%%", lifesteal), GREEN_VAL);
                if (thorns > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Thorns", String.format("%.1f", thorns), PURPLE_VAL);
                if (armorShred > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Shred", String.format("%.1f%%", armorShred), RED_VAL);
                if (stunChance > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Stun%", String.format("%.1f%%", stunChance), GOLD_VAL);
                if (cdr > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "CDR", String.format("%.1f%%", cdr), BLUE_VAL);
                if (abilityPower > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "AP", String.format("+%.1f", abilityPower), PURPLE_VAL);
                if (comboSpeed > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Combo", String.format("+%.1f%%", comboSpeed), RED_VAL);
                if (spellRange > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Range", String.format("+%.0f%%", spellRange), PURPLE_VAL);
            }
        }

        // === Elemental Damage ===
        if (ty + 10 < bottomY) {
            double fireDmg = getCustomAttr(mc, MegaModAttributes.FIRE_DAMAGE_BONUS);
            double iceDmg = getCustomAttr(mc, MegaModAttributes.ICE_DAMAGE_BONUS);
            double lightDmg = getCustomAttr(mc, MegaModAttributes.LIGHTNING_DAMAGE_BONUS);
            double poisonDmg = getCustomAttr(mc, MegaModAttributes.POISON_DAMAGE_BONUS);
            double holyDmg = getCustomAttr(mc, MegaModAttributes.HOLY_DAMAGE_BONUS);
            double shadowDmg = getCustomAttr(mc, MegaModAttributes.SHADOW_DAMAGE_BONUS);

            boolean hasAny = fireDmg > 0 || iceDmg > 0 || lightDmg > 0 || poisonDmg > 0 || holyDmg > 0 || shadowDmg > 0;
            if (hasAny) {
                ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "ELEMENTAL", ACCENT_ELEMENT);
                if (fireDmg > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Fire", String.format("+%.1f", fireDmg), 0xFFCC4422);
                if (iceDmg > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Ice", String.format("+%.1f", iceDmg), 0xFF4488CC);
                if (lightDmg > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Light", String.format("+%.1f", lightDmg), 0xFFCCCC22);
                if (poisonDmg > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Poison", String.format("+%.1f", poisonDmg), 0xFF44AA44);
                if (holyDmg > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Holy", String.format("+%.1f", holyDmg), 0xFFCCCC88);
                if (shadowDmg > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Shadow", String.format("+%.1f", shadowDmg), 0xFF8844AA);
            }
        }

        // === Elemental Resistance ===
        if (ty + 10 < bottomY) {
            double fireRes = getCustomAttr(mc, MegaModAttributes.FIRE_RESISTANCE_BONUS);
            double iceRes = getCustomAttr(mc, MegaModAttributes.ICE_RESISTANCE_BONUS);
            double lightRes = getCustomAttr(mc, MegaModAttributes.LIGHTNING_RESISTANCE_BONUS);
            double poisonRes = getCustomAttr(mc, MegaModAttributes.POISON_RESISTANCE_BONUS);
            double holyRes = getCustomAttr(mc, MegaModAttributes.HOLY_RESISTANCE_BONUS);
            double shadowRes = getCustomAttr(mc, MegaModAttributes.SHADOW_RESISTANCE_BONUS);

            boolean hasAny = fireRes > 0 || iceRes > 0 || lightRes > 0 || poisonRes > 0 || holyRes > 0 || shadowRes > 0;
            if (hasAny) {
                ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "RESIST", ACCENT_RESIST);
                if (fireRes > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Fire", String.format("%.0f%%", fireRes), 0xFFCC4422);
                if (iceRes > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Ice", String.format("%.0f%%", iceRes), 0xFF4488CC);
                if (lightRes > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Light", String.format("%.0f%%", lightRes), 0xFFCCCC22);
                if (poisonRes > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Poison", String.format("%.0f%%", poisonRes), 0xFF44AA44);
                if (holyRes > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Holy", String.format("%.0f%%", holyRes), 0xFFCCCC88);
                if (shadowRes > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Shadow", String.format("%.0f%%", shadowRes), 0xFF8844AA);
            }
        }

        // === Survival / Utility ===
        if (ty + 10 < bottomY) {
            double hpRegen = getCustomAttr(mc, MegaModAttributes.HEALTH_REGEN_BONUS);
            double fallReduce = getCustomAttr(mc, MegaModAttributes.FALL_DAMAGE_REDUCTION);
            double mineSpeed = getCustomAttr(mc, MegaModAttributes.MINING_SPEED_BONUS);
            double swimSpeed = getCustomAttr(mc, MegaModAttributes.SWIM_SPEED_BONUS);
            double jumpHeight = getCustomAttr(mc, MegaModAttributes.JUMP_HEIGHT_BONUS);
            double xpBonus = getCustomAttr(mc, MegaModAttributes.XP_BONUS);
            double lootFortune = getCustomAttr(mc, MegaModAttributes.LOOT_FORTUNE);
            double hungerEff = getCustomAttr(mc, MegaModAttributes.HUNGER_EFFICIENCY);
            double combatXp = getCustomAttr(mc, MegaModAttributes.COMBAT_XP_BONUS);
            double miningXp = getCustomAttr(mc, MegaModAttributes.MINING_XP_BONUS);
            double farmingXp = getCustomAttr(mc, MegaModAttributes.FARMING_XP_BONUS);
            double arcaneXp = getCustomAttr(mc, MegaModAttributes.ARCANE_XP_BONUS);
            double survivalXp = getCustomAttr(mc, MegaModAttributes.SURVIVAL_XP_BONUS);
            double brilliance = getCustomAttr(mc, MegaModAttributes.BRILLIANCE);
            double beastAffinity = getCustomAttr(mc, MegaModAttributes.BEAST_AFFINITY);
            double preySense = getCustomAttr(mc, MegaModAttributes.PREY_SENSE);
            double veinSense = getCustomAttr(mc, MegaModAttributes.VEIN_SENSE);
            double excReach = getCustomAttr(mc, MegaModAttributes.EXCAVATION_REACH);

            boolean hasAny = hpRegen > 0 || fallReduce > 0 || mineSpeed > 0 || swimSpeed > 0
                || jumpHeight > 0 || xpBonus > 0 || lootFortune > 0 || hungerEff > 0
                || combatXp > 0 || miningXp > 0 || farmingXp > 0 || arcaneXp > 0 || survivalXp > 0
                || brilliance > 0 || beastAffinity > 0 || preySense > 0 || veinSense > 0 || excReach > 0;

            if (hasAny) {
                ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "SURVIVAL", ACCENT_SURVIVAL);
                if (hpRegen > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Regen", String.format("+%.1f", hpRegen), GREEN_VAL);
                if (fallReduce > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "FallRed", String.format("%.0f%%", fallReduce), BLUE_VAL);
                if (mineSpeed > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Mine+", String.format("+%.0f%%", mineSpeed), GOLD_VAL);
                if (swimSpeed > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Swim+", String.format("+%.0f%%", swimSpeed), BLUE_VAL);
                if (jumpHeight > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Jump+", String.format("+%.1f", jumpHeight), VALUE_COLOR);
                if (xpBonus > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "XP+", String.format("+%.0f%%", xpBonus), GREEN_VAL);
                if (lootFortune > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Loot+", String.format("+%.0f%%", lootFortune), GOLD_VAL);
                if (hungerEff > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Food+", String.format("+%.0f%%", hungerEff), GREEN_VAL);
                if (combatXp > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "CB XP", String.format("+%.0f%%", combatXp), 0xFFFF4444);
                if (miningXp > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "MI XP", String.format("+%.0f%%", miningXp), 0xFF44BBFF);
                if (farmingXp > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "FA XP", String.format("+%.0f%%", farmingXp), 0xFF44CC44);
                if (arcaneXp > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "AR XP", String.format("+%.0f%%", arcaneXp), 0xFF9966FF);
                if (survivalXp > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "SV XP", String.format("+%.0f%%", survivalXp), 0xFFCCAA44);
                if (brilliance > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Brill", String.format("+%.0f%%", brilliance), 0xFFFFDD44);
                if (beastAffinity > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Beast", String.format("+%.0f%%", beastAffinity), 0xFF44CC44);
                if (preySense > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Prey", String.format("+%.0f", preySense), 0xFFFF6644);
                if (veinSense > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Vein", String.format("+%.0f", veinSense), 0xFF44BBFF);
                if (excReach > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Reach", String.format("+%.1f", excReach), 0xFFCCAA44);
            }
        }

        // === Economy ===
        if (ty + 10 < bottomY) {
            double coinBonus = getCustomAttr(mc, MegaModAttributes.MEGACOIN_BONUS);
            double shopDisc = getCustomAttr(mc, MegaModAttributes.SHOP_DISCOUNT);
            double sellBonus = getCustomAttr(mc, MegaModAttributes.SELL_BONUS);

            boolean hasAny = coinBonus > 0 || shopDisc > 0 || sellBonus > 0;
            if (hasAny) {
                ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "ECONOMY", ACCENT_ECONOMY);
                if (coinBonus > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Coins+", String.format("+%.0f%%", coinBonus), GOLD_VAL);
                if (shopDisc > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Disc", String.format("%.0f%%", shopDisc), GREEN_VAL);
                if (sellBonus > 0 && ty + 9 < bottomY)
                    ty = drawStatRow(g, mc, tx, ty, CONTENT_W, "Sell+", String.format("+%.0f%%", sellBonus), GOLD_VAL);
            }
        }

        // === Active Effects ===
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (!effects.isEmpty() && ty + 14 < bottomY) {
            ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "EFFECTS", ACCENT_EFFECTS);

            for (MobEffectInstance effect : effects) {
                if (ty + 9 > bottomY) break;
                String name = effect.getEffect().value().getDescriptionId();
                int lastDot = name.lastIndexOf('.');
                String shortName = lastDot >= 0 ? name.substring(lastDot + 1) : name;
                shortName = shortName.substring(0, 1).toUpperCase() + shortName.substring(1);
                if (shortName.length() > 10) shortName = shortName.substring(0, 8) + "..";

                int amp = effect.getAmplifier();
                String ampStr = amp > 0 ? " " + toRoman(amp + 1) : "";
                int dur = effect.getDuration();
                String durStr = dur > 32000 * 20 ? "" : " " + formatDuration(dur);

                boolean beneficial = effect.getEffect().value().isBeneficial();
                int col = beneficial ? EFFECT_GOOD : EFFECT_BAD;

                g.drawString(mc.font, shortName + ampStr, tx + 1, ty, col, false);
                if (!durStr.isEmpty()) {
                    int durW = mc.font.width(durStr);
                    g.drawString(mc.font, durStr, tx + CONTENT_W - durW, ty, LABEL_COLOR, false);
                }
                ty += 9;
            }
        }

        // === Skill Levels === (removed — old skill system deleted, TODO: Reconnect with Pufferfish Skills API)

        // === Equipped Accessories ===
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        if (equipped != null && !equipped.isEmpty() && ty + 14 < bottomY) {
            ty = drawSectionHeader(g, mc, tx, ty, CONTENT_W, "RELICS", ACCENT_RELICS);

            for (Map.Entry<String, String> entry : equipped.entrySet()) {
                if (ty + 9 > bottomY) break;
                String itemName = formatItemId(entry.getValue());
                if (itemName.length() > 16) itemName = itemName.substring(0, 14) + "..";
                g.drawString(mc.font, itemName, tx + 1, ty, PURPLE_VAL, false);
                ty += 9;
            }
        }

        // === Held Item ===
        ItemStack held = mc.player.getMainHandItem();
        if (!held.isEmpty() && ty + 14 < bottomY) {
            drawDivider(g, tx, ty + 1, CONTENT_W);
            ty += 4;
            String heldName = held.getHoverName().getString();
            if (heldName.length() > 16) heldName = heldName.substring(0, 14) + "..";
            g.drawString(mc.font, heldName, tx + 1, ty, TITLE_COLOR, false);
        }

        return ty;
    }

    private static void drawVanillaPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, INV_BORDER_DARK);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, INV_BORDER_LIGHT);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, INV_BORDER_LIGHT);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, INV_BORDER_MID);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, INV_BORDER_MID);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, INV_BG);
    }

    private static void drawDivider(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, DIVIDER);
    }

    private static int drawStatRow(GuiGraphics g, Minecraft mc, int x, int y, int contentW, String label, String value, int valueColor) {
        g.drawString(mc.font, label, x + 1, y, LABEL_COLOR, false);
        int valW = mc.font.width(value);
        g.drawString(mc.font, value, x + contentW - valW, y, valueColor, false);
        return y + 9;
    }

    private static double getAttrValue(Minecraft mc, Holder<Attribute> attr) {
        if (mc.player == null) return 0;
        AttributeInstance inst = mc.player.getAttribute(attr);
        return inst != null ? inst.getValue() : 0;
    }

    private static double getCustomAttr(Minecraft mc, Holder<Attribute> attr) {
        return getAttrValue(mc, attr);
    }

    /** Returns true if the attribute's computed value is above its base (i.e. has active modifiers). */
    private static boolean isBoosted(Minecraft mc, Holder<Attribute> attr) {
        if (mc.player == null) return false;
        AttributeInstance inst = mc.player.getAttribute(attr);
        if (inst == null) return false;
        return inst.getValue() - inst.getBaseValue() > 0.01;
    }

    /** Returns true if the attribute has been modified from its base value (up or down). */
    private static boolean isModified(Minecraft mc, Holder<Attribute> attr) {
        if (mc.player == null) return false;
        AttributeInstance inst = mc.player.getAttribute(attr);
        if (inst == null) return false;
        return Math.abs(inst.getValue() - inst.getBaseValue()) > 0.001;
    }

    /** Draw a row for a modified vanilla attribute, colored teal if boosted, red if reduced. */
    private static int drawModifiedRow(GuiGraphics g, Minecraft mc, int x, int y, int contentW, String label, Holder<Attribute> attr, String fmt, int defaultColor) {
        AttributeInstance inst = mc.player.getAttribute(attr);
        if (inst == null) return y;
        double val = inst.getValue();
        double base = inst.getBaseValue();
        int color = val > base + 0.001 ? BOOST_COLOR : (val < base - 0.001 ? RED_VAL : defaultColor);
        return drawStatRow(g, mc, x, y, contentW, label, String.format(fmt, val), color);
    }

    /** Draw a section header with colored accent bar underneath. */
    private static int drawSectionHeader(GuiGraphics g, Minecraft mc, int tx, int ty, int contentW, String title, int accentColor) {
        drawDivider(g, tx, ty + 1, contentW);
        ty += 4;
        g.drawString(mc.font, title, tx, ty, LABEL_COLOR, false);
        int textW = mc.font.width(title);
        g.fill(tx, ty + 9, tx + textW, ty + 10, accentColor);
        return ty + 11;
    }

    private static String formatItemId(String itemId) {
        String name = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("_")) {
            if (sb.length() > 0) sb.append(" ");
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> String.valueOf(num);
        };
    }

    private static String formatDuration(int ticks) {
        int totalSec = ticks / 20;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return min > 0 ? min + ":" + String.format("%02d", sec) : sec + "s";
    }
}
