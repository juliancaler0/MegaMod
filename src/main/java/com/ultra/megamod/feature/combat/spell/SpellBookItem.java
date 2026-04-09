package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.combat.spell.client.SpellBookClientCast;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * An offhand item that grants the player access to all spells of a given school,
 * bypassing skill node requirements. Only works while held in the offhand slot.
 */
public class SpellBookItem extends Item {

    private final String school;
    private final String displaySchool;
    private final int schoolColor;
    private final String classFilter; // Optional: filter by classRequirement instead of school

    public SpellBookItem(Properties props, String school, String displaySchool, int schoolColor) {
        this(props, school, displaySchool, schoolColor, null);
    }

    /**
     * @param classFilter if non-null, filters spells by classRequirement instead of school
     */
    public SpellBookItem(Properties props, String school, String displaySchool, int schoolColor, String classFilter) {
        super(props);
        this.school = school;
        this.displaySchool = displaySchool;
        this.schoolColor = schoolColor;
        this.classFilter = classFilter;
    }

    public String getSchool() {
        return school;
    }

    /**
     * Returns all spell IDs this book grants access to.
     * If classFilter is set, filters by classRequirement (e.g., "ROGUE", "WARRIOR").
     * Otherwise filters by school name (e.g., "ARCANE", "FIRE").
     */
    public List<String> getSpellIds() {
        if (classFilter != null) {
            return SpellRegistry.ALL_SPELLS.values().stream()
                .filter(s -> classFilter.equalsIgnoreCase(s.classRequirement()))
                .map(SpellDefinition::id)
                .toList();
        }
        return SpellRegistry.ALL_SPELLS.values().stream()
            .filter(s -> s.school().name().equalsIgnoreCase(school))
            .map(SpellDefinition::id)
            .toList();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal(displaySchool + " Spell Book").withStyle(s -> s.withColor(schoolColor)));
        tooltip.accept(Component.literal("Hold in offhand to cast " + displaySchool.toLowerCase() + " spells").withStyle(ChatFormatting.GRAY));

        List<String> spells = getSpellIds();
        if (!spells.isEmpty()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.literal("Grants access to:").withStyle(ChatFormatting.GOLD));
            for (String spellId : spells) {
                SpellDefinition def = SpellRegistry.get(spellId);
                if (def != null) {
                    tooltip.accept(Component.literal(" - " + def.name()).withStyle(ChatFormatting.YELLOW));
                }
            }
        }
    }

    /**
     * Right-click casting: when the player right-clicks while holding the spell book
     * (in either hand), cast the currently selected spell. This mirrors the R-key behavior
     * so players can cast intuitively with either input method.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // Server-side: the SpellBookCastPayload handler validates and casts
            return InteractionResult.CONSUME;
        }

        // Client-side: send the spell cast payload for the selected spell
        List<String> spells = getSpellIds();
        if (spells.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("\u00a76[\u00a7eMegaMod\u00a76] \u00a77No spells in this book."), true);
            return InteractionResult.FAIL;
        }

        // Delegate to a client-only helper to avoid loading client classes on server
        SpellBookClientCast.sendCast(player, spells, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
