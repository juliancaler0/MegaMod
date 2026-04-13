package com.ultra.megamod.lib.spellengine.api.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;

public class SpellEntity {
    public interface Spawned {
        record Args(LivingEntity owner, Holder<Spell> spell, Spell.Impact.Action.Spawn spawnData, SpellHelper.ImpactContext context) { }
        void onSpawnedBySpell(Args args);
    }
}
