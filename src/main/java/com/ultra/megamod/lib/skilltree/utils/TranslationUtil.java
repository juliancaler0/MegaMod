package com.ultra.megamod.lib.skilltree.utils;

import com.ultra.megamod.lib.skilltree.attributes.ConditionalAttributeModifier;
import com.ultra.megamod.lib.skilltree.node.ConditionalAttributeReward;
import com.ultra.megamod.lib.skilltree.skills.NodeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import com.ultra.megamod.mixin.spellengine.client.ItemStackTooltipAccessor;
import net.minecraft.core.Holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TranslationUtil {

    // Key skill Id, value spell Id
    public static final Map<String, Supplier<List<Component>>> resolvers = new HashMap<>();

    public static List<Component> resolve(String skillId) {
        var supplier = resolvers.get(skillId);
        if (supplier == null) {
            return List.of();
        }
        return supplier.get();
    }

    public static List<Component> resolveSpellDetails(Identifier spellId) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        return SpellTooltip.spellDescriptionWithDetails(spellId, player, ItemStack.EMPTY, 0);
    }

    public static List<Component> resolveConditionalAttributeTooltip(ConditionalAttributeReward.DataStructure data) {
        // TODO: Reimplement with 1.21.11 tooltip API
        var conditional = data.mapped();
        var lines = new ArrayList<Component>();
        lines.add(Component.translatable(conditional.condition().translationKey()));
        lines.add(Component.literal("  " + conditional.modifier().amount() + " " + conditional.attribute().unwrapKey().map(k -> k.identifier().toString()).orElse("?")));
        return lines;
    }

    public static List<Component> resolveAttributeModifierTooltip(NodeTypes.EntityAttributeReward attributeReward) {
        // TODO: Reimplement with 1.21.11 tooltip API
        var bonusLines = new ArrayList<Component>();
        var modifier = attributeReward.modifier();
        bonusLines.add(Component.literal("  " + modifier.amount() + " " + attributeReward.attribute().unwrapKey().map(k -> k.identifier().toString()).orElse("?")));
        return bonusLines;
    }
}
