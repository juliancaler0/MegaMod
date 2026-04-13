package com.ultra.megamod.lib.spellengine.api.spell.weakness;

import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ScopedWeakness(@Nullable Spell.Impact.Action.Type impact_type,
                             Spell.Impact.TargetModifier weakness) {
}
