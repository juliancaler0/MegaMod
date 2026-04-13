package com.ultra.megamod.feature.combat.rogues.mixin.client;

/**
 * Whirlwind rendering mixin documentation.
 * Ported from net.rogues.mixin.client.LivingEntityRendererWhirlwind.
 *
 * In the Fabric source, this mixin rotated the entity model on the Y-axis
 * while channeling the "whirlwind" spell, creating a spinning visual effect.
 *
 * In MegaMod, this functionality is handled by the existing SpellEngine
 * LivingEntityRendererMixin, which checks for `spell.active.cast.animation_spin`
 * on any spell being channeled. The whirlwind spell definition sets this value
 * to achieve the same spin effect without a separate mixin.
 *
 * This class is retained for port completeness documentation.
 */
public class LivingEntityRendererWhirlwind {
    // Whirlwind spin rendering is handled by:
    // com.ultra.megamod.mixin.spellengine.client.render.LivingEntityRendererMixin
    // via the animation_spin property on the whirlwind SpellDefinition.
}
