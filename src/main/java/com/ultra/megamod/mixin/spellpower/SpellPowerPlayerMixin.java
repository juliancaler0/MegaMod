package com.ultra.megamod.mixin.spellpower;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class SpellPowerPlayerMixin {
    // Attribute scope injection is disabled in SpellPower.
    // All attributes are injected at LivingEntity level via EntityAttributeModificationEvent.
    // This mixin is kept as a placeholder for future player-specific SpellPower features.
}
