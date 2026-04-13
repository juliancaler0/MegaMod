package com.ultra.megamod.feature.combat.rogues.effect;

import com.ultra.megamod.feature.combat.spell.SpellEffects;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Convenience accessors for Rogue-specific status effects.
 * Ported from net.rogues.effect.RogueEffects.
 *
 * All effects are registered through {@link SpellEffects}. This class
 * provides typed references for use by rogues-specific code (mixins, handlers).
 */
public class RogueEffects {

    /** Slice and Dice: +10% attack damage per amplifier (stacks to 9). */
    public static DeferredHolder<MobEffect, MobEffect> SLICE_AND_DICE = SpellEffects.SLICE_AND_DICE;

    /** Shock: -100% movement speed, -100% attack speed (stun). */
    public static DeferredHolder<MobEffect, MobEffect> SHOCK = SpellEffects.SHOCK;

    /** Shadow Step Buff: +20% movement speed. */
    public static DeferredHolder<MobEffect, MobEffect> SHADOW_STEP = SpellEffects.SHADOW_STEP_BUFF;

    /** Stealth: invisibility + -50% movement speed. */
    public static DeferredHolder<MobEffect, MobEffect> STEALTH = SpellEffects.STEALTH;

    /** Stealth Speed: +50% movement speed (applied alongside stealth). */
    public static DeferredHolder<MobEffect, MobEffect> STEALTH_SPEED = SpellEffects.STEALTH_SPEED;

    /** Shatter: -30% armor per amplifier (stacks to 5). */
    public static DeferredHolder<MobEffect, MobEffect> SHATTER = SpellEffects.SHATTER;

    /** Demoralize: -20% attack damage per amplifier (stacks to 5). */
    public static DeferredHolder<MobEffect, MobEffect> DEMORALIZE = SpellEffects.DEMORALIZE;

    /** Charge: +50% movement speed, +50% knockback resistance, cleanses movement impairments. */
    public static DeferredHolder<MobEffect, MobEffect> CHARGE = SpellEffects.CHARGE_BUFF;
}
