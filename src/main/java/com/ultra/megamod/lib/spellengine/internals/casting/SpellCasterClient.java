package com.ultra.megamod.lib.spellengine.internals.casting;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.internals.melee.Melee;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpellCasterClient extends SpellCasterEntity {
    List<Entity> getCurrentTargets();
    Entity getCurrentFirstTarget();

    SpellCast.Attempt startSpellCast(ItemStack itemStack, Holder<Spell> spellEntry);
    @Nullable SpellCast.Progress getSpellCastProgress();
    boolean isCastingSpell();
    void cancelSpellCast();

    void onAttacksAvailable(List<Melee.Attack> attacks);
    Melee.ActiveAttack getCurrentSkillAttack();
}
