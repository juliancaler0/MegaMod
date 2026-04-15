package com.ultra.megamod.lib.emf.mixin.mixins.optional;


import org.spongepowered.asm.mixin.Mixin;
import com.ultra.megamod.lib.etf.mixin.CancelTarget;

@Mixin(value = CancelTarget.class)
public abstract class MixinLivingEntityRenderer_ValueCapturing{}
