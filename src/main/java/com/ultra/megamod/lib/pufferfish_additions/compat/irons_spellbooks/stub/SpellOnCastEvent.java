package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/**
 * Compile-time stub mirroring {@code io.redspace.ironsspellbooks.api.events.SpellOnCastEvent}.
 */
public abstract class SpellOnCastEvent extends Event {
    public abstract Entity getEntity();

    public abstract String getSpellId();

    public abstract int getSpellLevel();

    public abstract SchoolType getSchoolType();

    public abstract CastSource getCastSource();
}
