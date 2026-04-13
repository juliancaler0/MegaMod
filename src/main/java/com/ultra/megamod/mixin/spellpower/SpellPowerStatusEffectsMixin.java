package com.ultra.megamod.mixin.spellpower;

import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;

/**
 * In the original Fabric SpellPower mod, this mixin injected into StatusEffects.<clinit>
 * to trigger status effect registration at the right time via Registry.register().
 *
 * In NeoForge, status effect registration is handled by DeferredRegister, which automatically
 * registers at the correct time during mod loading. This mixin is kept as a no-op for
 * 1:1 port completeness.
 */
@Mixin(MobEffects.class)
public class SpellPowerStatusEffectsMixin {
    // Registration is handled by SpellPowerMod.init() via DeferredRegister.
    // No clinit injection needed in NeoForge.
}
