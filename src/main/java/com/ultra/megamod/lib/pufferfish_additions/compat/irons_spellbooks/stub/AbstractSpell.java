package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * Compile-time stub mirroring {@code io.redspace.ironsspellbooks.api.spells.AbstractSpell}.
 * Only loaded if Iron's Spellbooks is present at runtime (gated by {@code ModList.isLoaded}).
 */
public abstract class AbstractSpell {
    public abstract Identifier getSpellResource();

    public abstract Identifier getId();

    public abstract int getMinLevel();

    public abstract int getMaxLevel();

    public abstract CastType getCastType();

    public abstract SpellRarity getRarity(int spellLevel);

    public abstract int getMinLevelForRarity(SpellRarity rarity);

    public abstract int getManaCost(int spellLevel);

    public abstract int getEffectiveCastTime(int spellLevel, LivingEntity caster);
}
