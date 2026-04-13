package com.ultra.megamod.lib.spellengine.internals.arrow;

import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;

import java.util.List;

public interface ArrowExtension {
    void applyArrowPerks(Holder<Spell> spellEntry);
    List<Holder<Spell>> getCarriedSpells();
    boolean isInGround_SpellEngine();
}
