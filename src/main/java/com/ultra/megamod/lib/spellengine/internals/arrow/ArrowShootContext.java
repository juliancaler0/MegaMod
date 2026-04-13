package com.ultra.megamod.lib.spellengine.internals.arrow;

import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;

import java.util.ArrayList;
import java.util.List;

public class ArrowShootContext {
    public static final ArrowShootContext empty() {
        return new ArrowShootContext();
    };

    public boolean firedBySpell = false;
    public List<Holder<Spell>> activeSpells = new ArrayList<>();
}
