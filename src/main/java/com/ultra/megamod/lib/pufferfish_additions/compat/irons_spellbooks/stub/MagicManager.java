package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;

import net.minecraft.world.entity.LivingEntity;

/**
 * Compile-time stub mirroring {@code io.redspace.ironsspellbooks.capabilities.magic.MagicManager}.
 */
public final class MagicManager {
    private MagicManager() { }

    /** Upstream constant; used by the continuous-cast mana-per-second calculation. */
    public static final int CONTINUOUS_CAST_TICK_INTERVAL = 10;

    /**
     * Stub: returns 0 when Iron's Spellbooks is absent. Upstream returns the effective cooldown
     * (in ticks) considering cooldown reductions.
     */
    public static int getEffectiveSpellCooldown(final AbstractSpell spell, final LivingEntity caster, final CastSource castSource) {
        return 0;
    }
}
