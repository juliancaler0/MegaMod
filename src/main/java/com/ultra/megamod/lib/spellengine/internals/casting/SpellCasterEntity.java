package com.ultra.megamod.lib.spellengine.internals.casting;

import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.internals.SpellCooldownManager;
import com.ultra.megamod.lib.spellengine.internals.arrow.ArrowShootContext;
import com.ultra.megamod.lib.spellengine.internals.melee.Melee;
import org.jetbrains.annotations.Nullable;

public interface SpellCasterEntity {
    SpellCooldownManager getCooldownManager();

    void setChannelTickIndex(int channelTickIndex);
    int getChannelTickIndex();

    void setSpellCastProcess(@Nullable SpellCast.Process process);
    @Nullable SpellCast.Process getSpellCastProcess();

    Spell getCurrentSpell(); // Used by Better Combat compatibility
    float getCurrentCastingSpeed();

    // Used for Archery
    void setArrowShootContext(ArrowShootContext shotContext);
    ArrowShootContext getArrowShootContext();

    boolean isBeaming();
    @Nullable
    Spell.Target.Beam getBeam();

    void setMeleeSkillAttack(Melee.ActiveAttack attack);
    float getExtraSlipperiness();
    void setActiveMeleeSkill(Holder<Spell> spell);
    Holder<Spell> getActiveMeleeSkill();

    default boolean isCastingSpell() {
        return getSpellCastProcess() != null;
    }
}
