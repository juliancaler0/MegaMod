package com.ultra.megamod.mixin.spellpower;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;

/**
 * In the original Fabric SpellPower mod, this mixin injected into EntityAttributes.<clinit>
 * to trigger attribute registration at the right time via Registry.registerReference().
 *
 * In NeoForge, attribute registration is handled by DeferredRegister, which automatically
 * registers at the correct time during mod loading. This mixin is kept as a no-op for
 * 1:1 port completeness.
 */
@Mixin(value = Attributes.class, priority = 10000)
public class SpellPowerEntityAttributesMixin {
    // Registration is handled by SpellPowerMod.init() via DeferredRegister.
    // No clinit injection needed in NeoForge.
}
