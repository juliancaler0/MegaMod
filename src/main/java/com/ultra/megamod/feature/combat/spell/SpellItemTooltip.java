package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Adds spell information tooltips to weapons that have spell mappings via SpellAbilityBridge.
 * Shows spell name (colored by school), cast mode, damage/heal coefficient, and cooldown.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SpellItemTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // Get the registry name for this item
        String registryName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        // Check if this weapon has a spell mapping
        String spellId = SpellAbilityBridge.getSpellForWeapon(registryName);
        if (spellId == null) return;

        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) return;

        List<Component> tooltip = event.getToolTip();

        // Blank line separator
        tooltip.add(Component.empty());

        // Header
        tooltip.add(Component.literal("Spell: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(spell.name())
                .withStyle(Style.EMPTY.withColor(spell.school().color))));

        // School
        tooltip.add(Component.literal("  School: ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(spell.school().displayName)
                .withStyle(Style.EMPTY.withColor(spell.school().color))));

        // Cast mode
        String castModeStr = switch (spell.castMode()) {
            case INSTANT -> "Instant";
            case CHARGED -> String.format("Charged (%.1fs)", spell.castDuration());
            case CHANNELED -> String.format("Channeled (%.1fs)", spell.castDuration());
        };
        tooltip.add(Component.literal("  Cast: ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(castModeStr).withStyle(ChatFormatting.WHITE)));

        // Damage coefficient
        if (spell.damageCoefficient() > 0) {
            String dmgStr = String.format("%.0f%% %s Power", spell.damageCoefficient() * 100, spell.school().displayName);
            tooltip.add(Component.literal("  Damage: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(dmgStr).withStyle(ChatFormatting.RED)));
        }

        // Heal coefficient
        if (spell.healCoefficient() > 0) {
            String healStr = String.format("%.0f%% Healing Power", spell.healCoefficient() * 100);
            tooltip.add(Component.literal("  Healing: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(healStr).withStyle(ChatFormatting.GREEN)));
        }

        // Cooldown
        if (spell.cooldownSeconds() > 0) {
            String cdStr = String.format("%.1fs", spell.cooldownSeconds());
            tooltip.add(Component.literal("  Cooldown: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(cdStr).withStyle(ChatFormatting.AQUA)));
        }

        // Range
        if (spell.range() > 0) {
            tooltip.add(Component.literal("  Range: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(String.format("%.0f blocks", spell.range())).withStyle(ChatFormatting.WHITE)));
        }

        // Status effects
        if (spell.effects() != null && spell.effects().length > 0) {
            for (SpellDefinition.StatusEffectDef eff : spell.effects()) {
                String effectName = formatEffectName(eff.effectId());
                float durationSec = eff.durationTicks() / 20.0f;
                ChatFormatting color = eff.harmful() ? ChatFormatting.RED : ChatFormatting.GREEN;
                tooltip.add(Component.literal("  Applies: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(String.format("%s (%.1fs)", effectName, durationSec))
                        .withStyle(color)));
            }
        }

        // Exhaust cost
        if (spell.exhaust() > 0) {
            String exhaustStr = String.format("%.1f food", spell.exhaust() * 10f);
            tooltip.add(Component.literal("  Exhaust: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(exhaustStr).withStyle(ChatFormatting.GOLD)));
        }

        // Class / level requirement line retired with the class-selection system.
        // Spells' classRequirement is still on the SpellDefinition data but has no
        // runtime effect until the new skill tree port wires it back in.
    }

    /**
     * Converts an effect ID like "megamod:frost_slowness" to "Frost Slowness".
     */
    private static String formatEffectName(String effectId) {
        String path = effectId;
        int colonIdx = effectId.indexOf(':');
        if (colonIdx >= 0) {
            path = effectId.substring(colonIdx + 1);
        }
        // Convert underscores to spaces and capitalize each word
        String[] parts = path.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(capitalize(part));
        }
        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
