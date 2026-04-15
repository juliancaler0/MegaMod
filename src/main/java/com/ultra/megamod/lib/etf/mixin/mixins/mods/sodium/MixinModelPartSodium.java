package com.ultra.megamod.lib.etf.mixin.mixins.mods.sodium;

import org.spongepowered.asm.mixin.Mixin;

// todo seems to be no longer needed as sodium mixins to Cube now
import com.ultra.megamod.lib.etf.mixin.CancelTarget;

@Mixin(value = CancelTarget.class)
public abstract class MixinModelPartSodium { }
