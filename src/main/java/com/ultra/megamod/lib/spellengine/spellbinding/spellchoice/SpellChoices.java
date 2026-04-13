package com.ultra.megamod.lib.spellengine.spellbinding.spellchoice;

import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellChoice;
import org.jetbrains.annotations.Nullable;

public class SpellChoices {
    @Nullable
    public static SpellChoice from(ItemStack stack) {
        var choice = stack.get(SpellDataComponents.SPELL_CHOICE);
        return (choice != null && !choice.isEmpty()) ? choice : null;
    }
}