package com.ultra.megamod.mixin.spellpower;

import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;

/**
 * In the original Fabric SpellPower mod, this mixin injected into Potions.<clinit>
 * to trigger potion registration at the right time via Registry.register().
 *
 * In NeoForge, potion registration is handled by DeferredRegister, which automatically
 * registers at the correct time during mod loading. This mixin is kept as a no-op for
 * 1:1 port completeness.
 */
@Mixin(Potions.class)
public class SpellPowerPotionsMixin {
    // Registration is handled by SpellPowerMod.init() via DeferredRegister.
    // No clinit injection needed in NeoForge.
}
